
package DatabaseConnection;

import app.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Opens MySQL connections for the application screens.
 */
public class ConnectionDB {
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

	public static Connection getConnection() {
		try {
			Class.forName(DRIVER);
			return DriverManager.getConnection(AppConfig.dbUrl(), AppConfig.dbUser(), AppConfig.dbPassword());
		} catch (Exception error) {
			System.err.println("Database connection failed: " + error.getMessage());
			return null;
		}
	}
}
