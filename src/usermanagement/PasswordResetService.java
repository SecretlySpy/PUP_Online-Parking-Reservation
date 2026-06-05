package usermanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class PasswordResetService {
	private static final int SOCKET_TIMEOUT_MS = 15000;
	private static final int TEMPORARY_PASSWORD_LENGTH = 10;
	private static final char[] PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
	private static final SecureRandom RANDOM = new SecureRandom();

	public PasswordResetResult resetCustomerPassword(Connection conn, String accountInput) throws Exception {
		if (conn == null) {
			throw new IllegalStateException("Database connection is not available.");
		}

		String identifier = accountInput == null ? "" : accountInput.trim();
		if (identifier.isEmpty()) {
			throw new IllegalArgumentException("Please enter your username or registered email address.");
		}

		boolean originalAutoCommit = conn.getAutoCommit();
		try {
			conn.setAutoCommit(false);

			Account account = findCustomerAccount(conn, identifier);
			String temporaryPassword = generateTemporaryPassword();
			updatePassword(conn, account, temporaryPassword);
			sendResetEmail(account, temporaryPassword);

			conn.commit();
			return new PasswordResetResult(account.username, account.email);
		} catch (Exception ex) {
			rollbackQuietly(conn);
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	private Account findCustomerAccount(Connection conn, String identifier) throws SQLException {
		String sql = "select Username, Email from useraccount where Username=? or Email=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, identifier);
			ps.setString(2, identifier);

			try (ResultSet rs = ps.executeQuery()) {
				Account account = null;
				while (rs.next()) {
					if (account != null) {
						throw new IllegalArgumentException(
								"More than one account matched. Please reset using the registered email address.");
					}
					account = new Account(rs.getString("Username"), rs.getString("Email"));
				}

				if (account == null) {
					throw new IllegalArgumentException("No user account was found for that username or email address.");
				}

				if (isBlank(account.email)) {
					throw new IllegalArgumentException("This account does not have a registered email address.");
				}

				return account;
			}
		}
	}

	private void updatePassword(Connection conn, Account account, String temporaryPassword) throws SQLException {
		String sql = "update useraccount set Password=?, RepeatPassword=? where Username=? and Email=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, temporaryPassword);
			ps.setString(2, temporaryPassword);
			ps.setString(3, account.username);
			ps.setString(4, account.email);

			int updatedRows = ps.executeUpdate();
			if (updatedRows != 1) {
				throw new SQLException("Unable to update the password for the matched account.");
			}
		}
	}

	private void sendResetEmail(Account account, String temporaryPassword) throws IOException {
		SmtpConfiguration config = SmtpConfiguration.load();
		String subject = "PUP Parking password reset";
		String message = buildResetMessage(config.fromAddress, account, subject, temporaryPassword);

		try (SmtpSession session = new SmtpSession(createSocket(config))) {
			session.expectGreeting();
			session.greet();

			if (config.useStartTls && !config.useSsl) {
				session.command("STARTTLS", 220);
				session.startTls(config.host, config.port);
				session.greet();
			}

			if (!isBlank(config.username)) {
				session.authenticate(config.username, config.password);
			}

			session.command("MAIL FROM:<" + config.fromAddress + ">", 250);
			session.command("RCPT TO:<" + account.email + ">", 250, 251);
			session.command("DATA", 354);
			session.writeMessage(message);
			session.expectResponse("sending message body", 250);
			session.quit();
		}
	}

	private Socket createSocket(SmtpConfiguration config) throws IOException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(config.host, config.port), SOCKET_TIMEOUT_MS);
		socket.setSoTimeout(SOCKET_TIMEOUT_MS);

		if (!config.useSsl) {
			return socket;
		}

		SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(socket, config.host, config.port,
				true);
		sslSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
		sslSocket.startHandshake();
		return sslSocket;
	}

	private String buildResetMessage(String fromAddress, Account account, String subject, String temporaryPassword) {
		String body = "Hello " + account.username + ",\n\n"
				+ "We received a request to reset your PUP Online Parking Reservation password.\n\n"
				+ "Username: " + account.username + "\n"
				+ "Temporary password: " + temporaryPassword + "\n\n"
				+ "Please log in with this temporary password and change it after signing in.\n"
				+ "If you did not request this reset, please contact an administrator.\n";

		return "From: " + cleanHeader(fromAddress) + "\r\n"
				+ "To: " + cleanHeader(account.email) + "\r\n"
				+ "Subject: " + cleanHeader(subject) + "\r\n"
				+ "Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()) + "\r\n"
				+ "MIME-Version: 1.0\r\n"
				+ "Content-Type: text/plain; charset=UTF-8\r\n"
				+ "\r\n"
				+ body;
	}

	private String generateTemporaryPassword() {
		StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
		for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
			password.append(PASSWORD_CHARS[RANDOM.nextInt(PASSWORD_CHARS.length)]);
		}
		return password.toString();
	}

	private void rollbackQuietly(Connection conn) {
		try {
			conn.rollback();
		} catch (SQLException ignored) {
			// The original reset failure is more useful to show to the user.
		}
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static String cleanHeader(String value) {
		return value == null ? "" : value.replace("\r", "").replace("\n", "");
	}

	private static String setting(String propertyName, String envName) {
		String value = System.getProperty(propertyName);
		if (isBlank(value)) {
			value = System.getenv(envName);
		}
		return value == null ? "" : value.trim();
	}

	private static int intSetting(String propertyName, String envName, int defaultValue) {
		String value = setting(propertyName, envName);
		if (value.isEmpty()) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new IllegalStateException(propertyName + " must be a number.");
		}
	}

	private static boolean booleanSetting(String propertyName, String envName, boolean defaultValue) {
		String value = setting(propertyName, envName);
		if (value.isEmpty()) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	private static String encode(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static class Account {
		private final String username;
		private final String email;

		private Account(String username, String email) {
			this.username = username;
			this.email = email;
		}
	}

	public static class PasswordResetResult {
		private final String username;
		private final String email;

		private PasswordResetResult(String username, String email) {
			this.username = username;
			this.email = email;
		}

		public String getUsername() {
			return username;
		}

		public String getMaskedEmail() {
			int atIndex = email.indexOf('@');
			if (atIndex <= 1) {
				return email;
			}

			StringBuilder masked = new StringBuilder();
			masked.append(email.charAt(0));
			for (int i = 1; i < atIndex; i++) {
				masked.append('*');
			}
			masked.append(email.substring(atIndex));
			return masked.toString();
		}
	}

	private static class SmtpConfiguration {
		private final String host;
		private final int port;
		private final String username;
		private final String password;
		private final String fromAddress;
		private final boolean useStartTls;
		private final boolean useSsl;

		private SmtpConfiguration(String host, int port, String username, String password, String fromAddress,
				boolean useStartTls, boolean useSsl) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.fromAddress = fromAddress;
			this.useStartTls = useStartTls;
			this.useSsl = useSsl;
		}

		private static SmtpConfiguration load() {
			String host = setting("parking.smtp.host", "PARKING_SMTP_HOST");
			boolean useSsl = booleanSetting("parking.smtp.ssl", "PARKING_SMTP_SSL", false);
			int port = intSetting("parking.smtp.port", "PARKING_SMTP_PORT", useSsl ? 465 : 587);
			String username = setting("parking.smtp.username", "PARKING_SMTP_USERNAME");
			String password = setting("parking.smtp.password", "PARKING_SMTP_PASSWORD");
			String fromAddress = setting("parking.smtp.from", "PARKING_SMTP_FROM");
			boolean useStartTls = booleanSetting("parking.smtp.starttls", "PARKING_SMTP_STARTTLS", !useSsl);

			if (fromAddress.isEmpty()) {
				fromAddress = username;
			}

			if (host.isEmpty() || fromAddress.isEmpty()) {
				throw new IllegalStateException(
						"SMTP email is not configured. Set PARKING_SMTP_HOST and PARKING_SMTP_FROM, or the matching parking.smtp.* system properties.");
			}

			if (!username.isEmpty() && password.isEmpty()) {
				throw new IllegalStateException("SMTP password is missing. Set PARKING_SMTP_PASSWORD.");
			}

			return new SmtpConfiguration(host, port, username, password, fromAddress, useStartTls, useSsl);
		}
	}

	private static class SmtpSession implements AutoCloseable {
		private Socket socket;
		private BufferedReader reader;
		private BufferedWriter writer;

		private SmtpSession(Socket socket) throws IOException {
			setSocket(socket);
		}

		private void setSocket(Socket socket) throws IOException {
			this.socket = socket;
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		}

		private void expectGreeting() throws IOException {
			expectResponse("connecting to SMTP server", 220);
		}

		private void greet() throws IOException {
			try {
				command("EHLO localhost", 250);
			} catch (IOException ex) {
				command("HELO localhost", 250);
			}
		}

		private void startTls(String host, int port) throws IOException {
			SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(socket, host, port, true);
			sslSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
			sslSocket.startHandshake();
			setSocket(sslSocket);
		}

		private void authenticate(String username, String password) throws IOException {
			command("AUTH LOGIN", 334);
			command(encode(username), 334);
			command(encode(password), 235);
		}

		private void command(String command, int... expectedCodes) throws IOException {
			writer.write(command);
			writer.write("\r\n");
			writer.flush();
			expectResponse(command, expectedCodes);
		}

		private void writeMessage(String message) throws IOException {
			String normalized = message.replace("\r\n", "\n").replace('\r', '\n');
			String[] lines = normalized.split("\n", -1);
			for (String line : lines) {
				if (line.startsWith(".")) {
					writer.write(".");
				}
				writer.write(line);
				writer.write("\r\n");
			}
			writer.write(".\r\n");
			writer.flush();
		}

		private void expectResponse(String action, int... expectedCodes) throws IOException {
			SmtpResponse response = readResponse();
			for (int expectedCode : expectedCodes) {
				if (response.code == expectedCode) {
					return;
				}
			}
			throw new IOException("SMTP error while " + action + ": " + response.message);
		}

		private SmtpResponse readResponse() throws IOException {
			String line = reader.readLine();
			if (line == null) {
				throw new IOException("SMTP server closed the connection.");
			}

			int code = parseResponseCode(line);
			String codeText = line.substring(0, 3);
			StringBuilder message = new StringBuilder(line);

			while (line.length() > 3 && line.charAt(3) == '-') {
				line = reader.readLine();
				if (line == null) {
					throw new IOException("SMTP server closed the connection.");
				}
				message.append('\n').append(line);
				if (line.startsWith(codeText + " ")) {
					break;
				}
			}

			return new SmtpResponse(code, message.toString());
		}

		private int parseResponseCode(String line) throws IOException {
			if (line.length() < 3) {
				throw new IOException("Invalid SMTP response: " + line);
			}
			try {
				return Integer.parseInt(line.substring(0, 3));
			} catch (NumberFormatException ex) {
				throw new IOException("Invalid SMTP response: " + line, ex);
			}
		}

		private void quit() {
			try {
				command("QUIT", 221, 250);
			} catch (IOException ignored) {
				// The email was already accepted by the server.
			}
		}

		@Override
		public void close() throws IOException {
			socket.close();
		}
	}

	private static class SmtpResponse {
		private final int code;
		private final String message;

		private SmtpResponse(int code, String message) {
			this.code = code;
			this.message = message;
		}
	}
}
