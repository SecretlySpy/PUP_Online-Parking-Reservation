package usermanagement;

import app.PasswordSecurity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Issues one-time password reset links and applies password changes after the
 * submitted token is validated.
 */
public class PasswordResetService {
	private static final int SOCKET_TIMEOUT_MS = 15000;
	private static final int RESET_TOKEN_BYTES = 32;
	private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
	private static final SecureRandom RANDOM = new SecureRandom();

	public PasswordResetRequestResult requestCustomerPasswordReset(Connection conn, String accountInput)
			throws Exception {
		return requestPasswordReset(conn, AccountScope.CUSTOMER, accountInput);
	}

	public PasswordResetCompletionResult completeCustomerPasswordReset(Connection conn, String tokenOrLink,
			char[] newPassword) throws Exception {
		return completePasswordReset(conn, AccountScope.CUSTOMER, tokenOrLink, newPassword);
	}

	private PasswordResetRequestResult requestPasswordReset(Connection conn, AccountScope scope, String accountInput)
			throws Exception {
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

			Account account = findAccount(conn, scope, identifier);
			if (account == null || isBlank(account.email)) {
				conn.commit();
				return PasswordResetRequestResult.notSent();
			}

			expirePendingTokens(conn, scope, account.username);
			String token = generateResetToken();
			String tokenHash = sha256Hex(token);
			Instant expiresAt = Instant.now().plus(RESET_TOKEN_TTL);
			insertResetToken(conn, scope, account.username, tokenHash, expiresAt);

			String resetLink = buildResetLink(token);
			sendResetEmail(account, resetLink, expiresAt);

			conn.commit();
			return PasswordResetRequestResult.sent(account.username, account.email, expiresAt);
		} catch (Exception ex) {
			rollbackQuietly(conn);
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	private PasswordResetCompletionResult completePasswordReset(Connection conn, AccountScope scope, String tokenOrLink,
			char[] newPassword) throws Exception {
		if (conn == null) {
			throw new IllegalStateException("Database connection is not available.");
		}

		String token = extractToken(tokenOrLink);
		if (token.isEmpty()) {
			throw new IllegalArgumentException("Paste the reset link or token from your email.");
		}

		String strengthError = PasswordSecurity.strengthError(newPassword);
		if (strengthError != null) {
			throw new IllegalArgumentException(strengthError);
		}

		boolean originalAutoCommit = conn.getAutoCommit();
		try {
			conn.setAutoCommit(false);

			TokenRecord tokenRecord = findActiveToken(conn, scope, sha256Hex(token));
			String passwordHash = PasswordSecurity.hash(newPassword);
			updatePassword(conn, scope, tokenRecord.username, passwordHash);
			markTokenUsed(conn, tokenRecord.resetId);

			conn.commit();
			return new PasswordResetCompletionResult(tokenRecord.username);
		} catch (Exception ex) {
			rollbackQuietly(conn);
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	private Account findAccount(Connection conn, AccountScope scope, String identifier) throws SQLException {
		String sql = "select Username, Email from " + scope.tableName + " where Username=? or Email=? limit 2";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, identifier);
			ps.setString(2, identifier);

			try (ResultSet rs = ps.executeQuery()) {
				Account account = null;
				while (rs.next()) {
					if (account != null) {
						return null;
					}
					account = new Account(rs.getString("Username"), rs.getString("Email"));
				}
				return account;
			}
		}
	}

	private void expirePendingTokens(Connection conn, AccountScope scope, String username) throws SQLException {
		String sql = "update password_reset_tokens set used_at=CURRENT_TIMESTAMP "
				+ "where account_type=? and username=? and used_at is null";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, scope.accountType);
			ps.setString(2, username);
			ps.executeUpdate();
		}
	}

	private void insertResetToken(Connection conn, AccountScope scope, String username, String tokenHash,
			Instant expiresAt) throws SQLException {
		String sql = "insert into password_reset_tokens (account_type, username, token_hash, expires_at) "
				+ "values (?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, scope.accountType);
			ps.setString(2, username);
			ps.setString(3, tokenHash);
			ps.setTimestamp(4, Timestamp.from(expiresAt));
			ps.executeUpdate();
		}
	}

	private TokenRecord findActiveToken(Connection conn, AccountScope scope, String tokenHash) throws SQLException {
		String sql = "select reset_id, username from password_reset_tokens "
				+ "where account_type=? and token_hash=? and used_at is null and expires_at > CURRENT_TIMESTAMP "
				+ "order by created_at desc limit 1 for update";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, scope.accountType);
			ps.setString(2, tokenHash);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new IllegalArgumentException("The reset link is invalid, expired, or already used.");
				}
				return new TokenRecord(rs.getInt("reset_id"), rs.getString("username"));
			}
		}
	}

	private void updatePassword(Connection conn, AccountScope scope, String username, String passwordHash)
			throws SQLException {
		String sql = "update " + scope.tableName + " set Password=?, RepeatPassword=? where Username=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, passwordHash);
			ps.setString(2, passwordHash);
			ps.setString(3, username);

			int updatedRows = ps.executeUpdate();
			if (updatedRows != 1) {
				throw new SQLException("Unable to update the password for the matched account.");
			}
		}
	}

	private void markTokenUsed(Connection conn, int resetId) throws SQLException {
		String sql = "update password_reset_tokens set used_at=CURRENT_TIMESTAMP where reset_id=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, resetId);
			ps.executeUpdate();
		}
	}

	private void sendResetEmail(Account account, String resetLink, Instant expiresAt) throws IOException {
		SmtpConfiguration config = SmtpConfiguration.load();
		String subject = "PUP Parking password reset";
		String message = buildResetMessage(config.fromAddress, account, subject, resetLink, expiresAt);

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

			session.command("MAIL FROM:<" + cleanHeader(config.fromAddress) + ">", 250);
			session.command("RCPT TO:<" + cleanHeader(account.email) + ">", 250, 251);
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

		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, config.host, config.port, true);
		sslSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
		sslSocket.startHandshake();
		return sslSocket;
	}

	private String buildResetMessage(String fromAddress, Account account, String subject, String resetLink,
			Instant expiresAt) {
		String expiration = DateTimeFormatter.RFC_1123_DATE_TIME
				.format(ZonedDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
		String body = "Hello " + account.username + ",\n\n"
				+ "We received a request to reset your PUP Online Parking Reservation password.\n\n"
				+ "Use this secure reset link within 30 minutes:\n" + resetLink + "\n\n"
				+ "If your desktop app does not open the link automatically, copy the full link into the Reset Password dialog.\n\n"
				+ "This link expires on " + expiration + " and can be used only once.\n"
				+ "If you did not request this reset, you can ignore this email.\n";

		return "From: " + cleanHeader(fromAddress) + "\r\n"
				+ "To: " + cleanHeader(account.email) + "\r\n"
				+ "Subject: " + cleanHeader(subject) + "\r\n"
				+ "Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()) + "\r\n"
				+ "MIME-Version: 1.0\r\n"
				+ "Content-Type: text/plain; charset=UTF-8\r\n"
				+ "\r\n"
				+ body;
	}

	private String buildResetLink(String token) {
		String baseUrl = setting("parking.reset.baseUrl", "PARKING_PASSWORD_RESET_BASE_URL");
		if (baseUrl.isEmpty()) {
			baseUrl = "https://pup-parking.local/reset-password";
		}
		return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=" + token;
	}

	private String extractToken(String tokenOrLink) {
		String value = tokenOrLink == null ? "" : tokenOrLink.trim();
		int tokenIndex = value.indexOf("token=");
		if (tokenIndex < 0) {
			return value;
		}

		String token = value.substring(tokenIndex + "token=".length());
		int queryEnd = token.indexOf('&');
		if (queryEnd >= 0) {
			token = token.substring(0, queryEnd);
		}
		int fragmentStart = token.indexOf('#');
		if (fragmentStart >= 0) {
			token = token.substring(0, fragmentStart);
		}
		return URLDecoder.decode(token, StandardCharsets.UTF_8);
	}

	private String generateResetToken() {
		byte[] token = new byte[RESET_TOKEN_BYTES];
		RANDOM.nextBytes(token);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
	}

	private String sha256Hex(String value) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte current : hash) {
			hex.append(String.format("%02x", current & 0xff));
		}
		return hex.toString();
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

	private enum AccountScope {
		CUSTOMER("customer", "useraccount");

		private final String accountType;
		private final String tableName;

		AccountScope(String accountType, String tableName) {
			this.accountType = accountType;
			this.tableName = tableName;
		}
	}

	private static class Account {
		private final String username;
		private final String email;

		private Account(String username, String email) {
			this.username = username;
			this.email = email;
		}
	}

	private static class TokenRecord {
		private final int resetId;
		private final String username;

		private TokenRecord(int resetId, String username) {
			this.resetId = resetId;
			this.username = username;
		}
	}

	public static class PasswordResetRequestResult {
		private final String username;
		private final String email;
		private final Instant expiresAt;
		private final boolean emailSent;

		private PasswordResetRequestResult(String username, String email, Instant expiresAt, boolean emailSent) {
			this.username = username;
			this.email = email;
			this.expiresAt = expiresAt;
			this.emailSent = emailSent;
		}

		private static PasswordResetRequestResult sent(String username, String email, Instant expiresAt) {
			return new PasswordResetRequestResult(username, email, expiresAt, true);
		}

		private static PasswordResetRequestResult notSent() {
			return new PasswordResetRequestResult(null, null, null, false);
		}

		public String getUsername() {
			return username;
		}

		public Instant getExpiresAt() {
			return expiresAt;
		}

		public boolean isEmailSent() {
			return emailSent;
		}

		public String getMaskedEmail() {
			if (email == null) {
				return "";
			}

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

	public static class PasswordResetCompletionResult {
		private final String username;

		private PasswordResetCompletionResult(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
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
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, true);
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
