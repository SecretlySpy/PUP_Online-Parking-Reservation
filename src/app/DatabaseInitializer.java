package app;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates enhancement tables at runtime so existing local databases keep working
 * even before the latest SQL script is imported manually.
 */
public final class DatabaseInitializer {
	private DatabaseInitializer() {
		// Utility class; instances are not needed.
	}

	public static void ensureEnhancementSchema(Connection conn) {
		if (conn == null) {
			return;
		}

		try (Statement statement = conn.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS parking_slots ("
					+ "slot_id int NOT NULL, floor varchar(20) NOT NULL, slot_type varchar(30) NOT NULL, "
					+ "base_status varchar(20) NOT NULL DEFAULT 'available', is_active tinyint(1) NOT NULL DEFAULT 1, "
					+ "created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (slot_id), "
					+ "KEY idx_parking_slots_floor_type (floor, slot_type, base_status))");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS reservations ("
					+ "reservation_id int NOT NULL AUTO_INCREMENT, reservation_code varchar(40) NOT NULL, "
					+ "username varchar(45) NOT NULL, slot_id int NOT NULL, start_time datetime NOT NULL, "
					+ "end_time datetime NOT NULL, status varchar(20) NOT NULL DEFAULT 'reserved', "
					+ "customer_name varchar(120) NOT NULL, email varchar(120) NOT NULL, phone varchar(45) NOT NULL, "
					+ "vehicle_plate varchar(45) NOT NULL, qr_payload varchar(255) NOT NULL, "
					+ "created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (reservation_id), "
					+ "UNIQUE KEY uk_reservations_code (reservation_code), "
					+ "KEY idx_reservations_slot_time_status (slot_id, start_time, end_time, status), "
					+ "KEY idx_reservations_user_created (username, created_at))");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS notification_log ("
					+ "notification_id int NOT NULL AUTO_INCREMENT, reservation_code varchar(40) NOT NULL, "
					+ "channel varchar(20) NOT NULL, recipient varchar(120) NOT NULL, message varchar(255) NOT NULL, "
					+ "status varchar(20) NOT NULL DEFAULT 'queued', created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
					+ "PRIMARY KEY (notification_id), KEY idx_notification_reservation (reservation_code))");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS system_activity ("
					+ "activity_id int NOT NULL AUTO_INCREMENT, actor varchar(80) NOT NULL, "
					+ "activity_type varchar(40) NOT NULL, activity_message varchar(255) NOT NULL, "
					+ "created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (activity_id), "
					+ "KEY idx_system_activity_created (created_at))");

			ensureCredentialSchema(conn, statement, "useraccount");
			ensureCredentialSchema(conn, statement, "adminaccount");
			ensurePasswordResetSchema(statement);
			seedSlots(statement);
		} catch (Exception error) {
			System.err.println("Unable to initialize enhanced database schema: " + error.getMessage());
		}
	}

	private static void ensureCredentialSchema(Connection conn, Statement statement, String tableName)
			throws SQLException {
		if (!tableExists(conn, tableName)) {
			return;
		}

		normalizePrimaryKey(conn, statement, tableName);
		if (columnSize(conn, tableName, "Email") < 120 || columnSize(conn, tableName, "Password") < 255
				|| columnSize(conn, tableName, "RepeatPassword") < 255) {
			statement.executeUpdate("ALTER TABLE `" + tableName + "` MODIFY `Email` varchar(120) NOT NULL, "
					+ "MODIFY `Password` varchar(255) NOT NULL, MODIFY `RepeatPassword` varchar(255) NOT NULL");
		}
		ensureUniqueIndex(conn, statement, tableName, "uk_" + tableName + "_email", "Email");
	}

	private static void normalizePrimaryKey(Connection conn, Statement statement, String tableName) throws SQLException {
		List<String> primaryKeyColumns = primaryKeyColumns(conn, tableName);
		boolean hasUsernamePrimaryKey = primaryKeyColumns.size() == 1
				&& "Username".equalsIgnoreCase(primaryKeyColumns.get(0));

		if (!primaryKeyColumns.isEmpty() && !hasUsernamePrimaryKey) {
			statement.executeUpdate("ALTER TABLE `" + tableName + "` DROP PRIMARY KEY");
			primaryKeyColumns.clear();
		}

		if (primaryKeyColumns.isEmpty()) {
			statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD PRIMARY KEY (`Username`)");
		}
	}

	private static void ensurePasswordResetSchema(Statement statement) throws SQLException {
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS password_reset_tokens ("
				+ "reset_id int NOT NULL AUTO_INCREMENT, account_type varchar(20) NOT NULL, "
				+ "username varchar(45) NOT NULL, token_hash char(64) NOT NULL, expires_at datetime NOT NULL, "
				+ "used_at datetime NULL, created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (reset_id), UNIQUE KEY uk_password_reset_token_hash (token_hash), "
				+ "KEY idx_password_reset_account (account_type, username, expires_at, used_at))");
	}

	private static boolean tableExists(Connection conn, String tableName) throws SQLException {
		DatabaseMetaData metadata = conn.getMetaData();
		try (ResultSet tables = metadata.getTables(conn.getCatalog(), null, tableName, new String[] { "TABLE" })) {
			return tables.next();
		}
	}

	private static List<String> primaryKeyColumns(Connection conn, String tableName) throws SQLException {
		List<String> columns = new ArrayList<String>();
		DatabaseMetaData metadata = conn.getMetaData();
		try (ResultSet primaryKeys = metadata.getPrimaryKeys(conn.getCatalog(), null, tableName)) {
			while (primaryKeys.next()) {
				columns.add(primaryKeys.getString("COLUMN_NAME"));
			}
		}
		return columns;
	}

	private static int columnSize(Connection conn, String tableName, String columnName) throws SQLException {
		DatabaseMetaData metadata = conn.getMetaData();
		try (ResultSet columns = metadata.getColumns(conn.getCatalog(), null, tableName, columnName)) {
			if (columns.next()) {
				return columns.getInt("COLUMN_SIZE");
			}
		}
		return 0;
	}

	private static void ensureUniqueIndex(Connection conn, Statement statement, String tableName, String indexName,
			String columnName) throws SQLException {
		if (indexExists(conn, tableName, indexName)) {
			return;
		}
		statement.executeUpdate("CREATE UNIQUE INDEX `" + indexName + "` ON `" + tableName + "` (`" + columnName
				+ "`)");
	}

	private static boolean indexExists(Connection conn, String tableName, String indexName) throws SQLException {
		DatabaseMetaData metadata = conn.getMetaData();
		try (ResultSet indexes = metadata.getIndexInfo(conn.getCatalog(), null, tableName, false, false)) {
			while (indexes.next()) {
				if (indexName.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
					return true;
				}
			}
		}
		return false;
	}

	private static void seedSlots(Statement statement) throws Exception {
		statement.executeUpdate("INSERT IGNORE INTO parking_slots (slot_id, floor, slot_type, base_status, is_active) "
				+ "VALUES (1,'Ground','Standard','available',1),(2,'Ground','Standard','available',1),"
				+ "(3,'Ground','Compact','available',1),(4,'Ground','Accessible','available',1),"
				+ "(5,'Second','Standard','available',1),(6,'Second','Standard','available',1),"
				+ "(7,'Second','Compact','available',1),(8,'Second','EV','available',1),"
				+ "(9,'Third','Standard','available',1),(10,'Third','Compact','available',1),"
				+ "(11,'Third','Motorcycle','available',1),(12,'Third','Motorcycle','available',1)");
	}
}
