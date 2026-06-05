package app;

import java.sql.Connection;
import java.sql.Statement;

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

			seedSlots(statement);
		} catch (Exception error) {
			System.err.println("Unable to initialize enhanced database schema: " + error.getMessage());
		}
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
