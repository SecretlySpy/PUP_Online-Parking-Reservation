package app;

/**
 * Centralizes runtime configuration so the application can run locally, from an IDE,
 * or beside Docker without editing Java source files.
 */
public final class AppConfig {
	private static final String DEFAULT_DB_HOST = "localhost";
	private static final String DEFAULT_DB_PORT = "3306";
	private static final String DEFAULT_DB_NAME = "onlineparkingreservation";
	private static final String DEFAULT_DB_USER = "root";
	private static final String DEFAULT_DB_PASSWORD = "kakashijirachisohaer";

	private AppConfig() {
		// Utility class; instances are not needed.
	}

	public static String dbHost() {
		return read("opr.db.host", "OPR_DB_HOST", DEFAULT_DB_HOST);
	}

	public static String dbPort() {
		return read("opr.db.port", "OPR_DB_PORT", DEFAULT_DB_PORT);
	}

	public static String dbName() {
		return read("opr.db.name", "OPR_DB_NAME", DEFAULT_DB_NAME);
	}

	public static String dbUser() {
		return read("opr.db.user", "OPR_DB_USER", DEFAULT_DB_USER);
	}

	public static String dbPassword() {
		return read("opr.db.password", "OPR_DB_PASSWORD", DEFAULT_DB_PASSWORD);
	}

	public static String dbUrl() {
		String template = "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000&socketTimeout=10000";
		return String.format(template, dbHost(), dbPort(), dbName());
	}

	private static String read(String propertyName, String environmentName, String fallback) {
		String propertyValue = System.getProperty(propertyName);
		if (propertyValue != null && !propertyValue.trim().isEmpty()) {
			return propertyValue.trim();
		}

		String environmentValue = System.getenv(environmentName);
		if (environmentValue != null && !environmentValue.trim().isEmpty()) {
			return environmentValue.trim();
		}

		return fallback;
	}
}
