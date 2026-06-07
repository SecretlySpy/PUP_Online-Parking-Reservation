package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Authenticates accounts without placing password values in SQL predicates, which
 * allows hashed credentials and transparent migration of legacy records.
 */
public final class AuthenticationService {
	private AuthenticationService() {
		// Utility class; instances are not needed.
	}

	public static boolean authenticateCustomer(Connection conn, String username, char[] password) throws Exception {
		return authenticate(conn, "useraccount", username, password);
	}

	public static boolean authenticateAdmin(Connection conn, String username, char[] password) throws Exception {
		return authenticate(conn, "adminaccount", username, password);
	}

	private static boolean authenticate(Connection conn, String tableName, String username, char[] password)
			throws Exception {
		String selectSql = "select Username, Password from " + tableName + " where Username=? limit 1";
		try (PreparedStatement statement = conn.prepareStatement(selectSql)) {
			statement.setString(1, username);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return false;
				}

				String storedPassword = resultSet.getString("Password");
				if (!PasswordSecurity.verify(password, storedPassword)) {
					return false;
				}

				if (PasswordSecurity.needsRehash(storedPassword)) {
					upgradePasswordHash(conn, tableName, username, password);
				}
				return true;
			}
		}
	}

	private static void upgradePasswordHash(Connection conn, String tableName, String username, char[] password)
			throws Exception {
		String passwordHash = PasswordSecurity.hash(password);
		String updateSql = "update " + tableName + " set Password=?, RepeatPassword=? where Username=?";
		try (PreparedStatement statement = conn.prepareStatement(updateSql)) {
			statement.setString(1, passwordHash);
			statement.setString(2, passwordHash);
			statement.setString(3, username);
			statement.executeUpdate();
		}
	}
}
