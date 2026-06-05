package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Database access layer for slots, reservation scheduling, history, and reports.
 */
public class ReservationRepository {
	private static final DateTimeFormatter CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private Connection conn;

	public ReservationRepository(Connection conn) {
		this.conn = conn;
		DatabaseInitializer.ensureEnhancementSchema(conn);
	}

	public List<ParkingSlot> findSlots(String floor, String type, String availability, LocalDateTime startTime,
			LocalDateTime endTime) throws Exception {
		List<ParkingSlot> slots = loadSlots(floor, type);
		Map<Integer, ReservationRecord> overlaps = loadOverlappingReservations(startTime, endTime);

		List<ParkingSlot> filteredSlots = new ArrayList<ParkingSlot>();
		for (ParkingSlot slot : slots) {
			if (!"available".equalsIgnoreCase(slot.getBaseStatus())) {
				slot.setAvailability("Maintenance");
			} else if (overlaps.containsKey(slot.getSlotId())) {
				ReservationRecord reservation = overlaps.get(slot.getSlotId());
				slot.setAvailability(statusLabel(reservation.getStatus()));
				slot.setReservationCode(reservation.getReservationCode());
			} else {
				slot.setAvailability("Available");
			}

			if (matchesAvailability(slot, availability)) {
				filteredSlots.add(slot);
			}
		}
		return filteredSlots;
	}

	public String createReservation(String username, int slotId, LocalDateTime startTime, LocalDateTime endTime,
			String customerName, String email, String phone, String vehiclePlate) throws Exception {
		String validationError = FormValidator.firstError(FormValidator.required(username, "Username"),
				FormValidator.required(customerName, "Customer name"), FormValidator.email(email),
				FormValidator.required(phone, "Phone"), FormValidator.required(vehiclePlate, "Vehicle plate"),
				FormValidator.timeRange(startTime, endTime));
		if (validationError != null) {
			throw new IllegalArgumentException(validationError);
		}
		if (startTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
			throw new IllegalArgumentException("Reservation start time cannot be in the past.");
		}

		if (!isSlotAvailable(slotId, startTime, endTime)) {
			throw new IllegalArgumentException("Selected slot is no longer available for that time range.");
		}

		String reservationCode = buildReservationCode(slotId);
		String qrPayload = "PUP-PARKING|" + reservationCode + "|slot=" + slotId + "|user=" + username;
		String sql = "insert into reservations "
				+ "(reservation_code,username,slot_id,start_time,end_time,status,customer_name,email,phone,vehicle_plate,qr_payload) "
				+ "values (?,?,?,?,?,'reserved',?,?,?,?,?)";

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, reservationCode);
			statement.setString(2, username);
			statement.setInt(3, slotId);
			statement.setTimestamp(4, Timestamp.valueOf(startTime));
			statement.setTimestamp(5, Timestamp.valueOf(endTime));
			statement.setString(6, customerName);
			statement.setString(7, email);
			statement.setString(8, phone);
			statement.setString(9, vehiclePlate);
			statement.setString(10, qrPayload);
			statement.executeUpdate();
		}

		logNotification(reservationCode, "email", email, "Reservation " + reservationCode + " confirmed.");
		logActivity(username, "reservation_created", "Created reservation " + reservationCode + " for slot " + slotId);
		return reservationCode;
	}

	public List<ReservationRecord> findReservations(String username, String status, String floor) throws Exception {
		String sql = "select r.reservation_id,r.reservation_code,r.username,r.slot_id,s.floor,s.slot_type,"
				+ "r.start_time,r.end_time,r.status,r.customer_name,r.email,r.phone,r.vehicle_plate,r.qr_payload "
				+ "from reservations r join parking_slots s on r.slot_id=s.slot_id where 1=1";
		List<Object> parameters = new ArrayList<Object>();

		if (username != null && !username.trim().isEmpty()) {
			sql += " and r.username=?";
			parameters.add(username.trim());
		}
		if (status != null && !status.trim().equalsIgnoreCase("All")) {
			sql += " and r.status=?";
			parameters.add(status.trim().toLowerCase(Locale.ROOT));
		}
		if (floor != null && !floor.trim().equalsIgnoreCase("All")) {
			sql += " and s.floor=?";
			parameters.add(floor.trim());
		}
		sql += " order by r.start_time desc";

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			bind(statement, parameters);
			try (ResultSet resultSet = statement.executeQuery()) {
				return readReservations(resultSet);
			}
		}
	}

	public void updateReservationStatus(String reservationCode, String status, String actor) throws Exception {
		if (reservationCode == null || reservationCode.trim().isEmpty()) {
			throw new IllegalArgumentException("Select a reservation first.");
		}
		if (status == null || status.trim().isEmpty()) {
			throw new IllegalArgumentException("Choose a reservation status.");
		}

		String normalizedStatus = status.trim().toLowerCase(Locale.ROOT);
		try (PreparedStatement statement = conn
				.prepareStatement("update reservations set status=? where reservation_code=?")) {
			statement.setString(1, normalizedStatus);
			statement.setString(2, reservationCode.trim());
			int updated = statement.executeUpdate();
			if (updated == 0) {
				throw new IllegalArgumentException("Reservation was not found.");
			}
		}
		logActivity(actor == null ? "admin" : actor, "reservation_status",
				"Updated reservation " + reservationCode + " to " + normalizedStatus);
	}

	public void deleteReservation(String reservationCode, String actor) throws Exception {
		if (reservationCode == null || reservationCode.trim().isEmpty()) {
			throw new IllegalArgumentException("Select a reservation first.");
		}

		String normalizedCode = reservationCode.trim();
		try (PreparedStatement statement = conn.prepareStatement("delete from reservations where reservation_code=?")) {
			statement.setString(1, normalizedCode);
			int deleted = statement.executeUpdate();
			if (deleted == 0) {
				throw new IllegalArgumentException("Reservation was not found.");
			}
		}
		logActivity(actor == null ? "user" : actor, "reservation_deleted",
				"Deleted reservation " + normalizedCode);
	}

	public void updateSlotBaseStatus(int slotId, String baseStatus, String actor) throws Exception {
		if (slotId <= 0) {
			throw new IllegalArgumentException("Select a slot first.");
		}
		if (baseStatus == null || baseStatus.trim().isEmpty()) {
			throw new IllegalArgumentException("Choose a slot status.");
		}

		String normalizedStatus = baseStatus.trim().toLowerCase(Locale.ROOT);
		try (PreparedStatement statement = conn.prepareStatement("update parking_slots set base_status=? where slot_id=?")) {
			statement.setString(1, normalizedStatus);
			statement.setInt(2, slotId);
			int updated = statement.executeUpdate();
			if (updated == 0) {
				throw new IllegalArgumentException("Slot was not found.");
			}
		}
		logActivity(actor == null ? "admin" : actor, "slot_status",
				"Updated slot " + slotId + " to " + normalizedStatus);
	}

	public Map<String, Integer> reportCounts() throws Exception {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		counts.put("slots", scalar("select count(*) from parking_slots where is_active=1"));
		counts.put("available_now", findSlots("All", "All", "Available", LocalDateTime.now(),
				LocalDateTime.now().plusHours(1)).size());
		counts.put("reserved", scalar("select count(*) from reservations where status='reserved'"));
		counts.put("occupied", scalar("select count(*) from reservations where status='occupied'"));
		counts.put("completed", scalar("select count(*) from reservations where status='completed'"));
		counts.put("cancelled", scalar("select count(*) from reservations where status='cancelled'"));
		return counts;
	}

	public List<String[]> recentActivity() throws Exception {
		List<String[]> rows = new ArrayList<String[]>();
		String sql = "select actor,activity_type,activity_message,created_at from system_activity order by created_at desc limit 50";
		try (PreparedStatement statement = conn.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				rows.add(new String[] { resultSet.getString("actor"), resultSet.getString("activity_type"),
						resultSet.getString("activity_message"), resultSet.getString("created_at") });
			}
		}
		return rows;
	}

	public String[] customerDefaults(String username) throws Exception {
		String sql = "select concat(FirstName,' ',LastName) as FullName, Email, MobileNumber, PlateNumber "
				+ "from useraccount where Username=? limit 1";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, username);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return new String[] { resultSet.getString("FullName"), resultSet.getString("Email"),
							resultSet.getString("MobileNumber"), resultSet.getString("PlateNumber") };
				}
			}
		}
		return new String[] { "", "", "", "" };
	}

	private List<ParkingSlot> loadSlots(String floor, String type) throws Exception {
		String sql = "select slot_id,floor,slot_type,base_status from parking_slots where is_active=1";
		List<Object> parameters = new ArrayList<Object>();
		if (floor != null && !floor.trim().equalsIgnoreCase("All")) {
			sql += " and floor=?";
			parameters.add(floor.trim());
		}
		if (type != null && !type.trim().equalsIgnoreCase("All")) {
			sql += " and slot_type=?";
			parameters.add(type.trim());
		}
		sql += " order by slot_id";

		List<ParkingSlot> slots = new ArrayList<ParkingSlot>();
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			bind(statement, parameters);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					slots.add(new ParkingSlot(resultSet.getInt("slot_id"), resultSet.getString("floor"),
							resultSet.getString("slot_type"), resultSet.getString("base_status")));
				}
			}
		}
		return slots;
	}

	private Map<Integer, ReservationRecord> loadOverlappingReservations(LocalDateTime startTime, LocalDateTime endTime)
			throws Exception {
		Map<Integer, ReservationRecord> reservations = new HashMap<Integer, ReservationRecord>();
		if (startTime == null || endTime == null) {
			startTime = LocalDateTime.now();
			endTime = startTime.plusHours(1);
		}

		String sql = "select r.reservation_id,r.reservation_code,r.username,r.slot_id,s.floor,s.slot_type,"
				+ "r.start_time,r.end_time,r.status,r.customer_name,r.email,r.phone,r.vehicle_plate,r.qr_payload "
				+ "from reservations r join parking_slots s on r.slot_id=s.slot_id "
				+ "where r.status in ('reserved','occupied') and r.start_time < ? and r.end_time > ?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setTimestamp(1, Timestamp.valueOf(endTime));
			statement.setTimestamp(2, Timestamp.valueOf(startTime));
			try (ResultSet resultSet = statement.executeQuery()) {
				for (ReservationRecord reservation : readReservations(resultSet)) {
					reservations.put(reservation.getSlotId(), reservation);
				}
			}
		}
		return reservations;
	}

	private boolean isSlotAvailable(int slotId, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
		String sql = "select count(*) from reservations where slot_id=? and status in ('reserved','occupied') "
				+ "and start_time < ? and end_time > ?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, slotId);
			statement.setTimestamp(2, Timestamp.valueOf(endTime));
			statement.setTimestamp(3, Timestamp.valueOf(startTime));
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() && resultSet.getInt(1) == 0;
			}
		}
	}

	private List<ReservationRecord> readReservations(ResultSet resultSet) throws Exception {
		List<ReservationRecord> reservations = new ArrayList<ReservationRecord>();
		while (resultSet.next()) {
			reservations.add(new ReservationRecord(resultSet.getInt("reservation_id"),
					resultSet.getString("reservation_code"), resultSet.getString("username"),
					resultSet.getInt("slot_id"), resultSet.getString("floor"), resultSet.getString("slot_type"),
					resultSet.getTimestamp("start_time").toLocalDateTime(),
					resultSet.getTimestamp("end_time").toLocalDateTime(), resultSet.getString("status"),
					resultSet.getString("customer_name"), resultSet.getString("email"), resultSet.getString("phone"),
					resultSet.getString("vehicle_plate"), resultSet.getString("qr_payload")));
		}
		return reservations;
	}

	private boolean matchesAvailability(ParkingSlot slot, String availability) {
		return availability == null || availability.trim().equalsIgnoreCase("All")
				|| slot.getAvailability().equalsIgnoreCase(availability.trim());
	}

	private String statusLabel(String status) {
		if ("occupied".equalsIgnoreCase(status)) {
			return "Occupied";
		}
		if ("reserved".equalsIgnoreCase(status)) {
			return "Reserved";
		}
		return status == null ? "Available" : status;
	}

	private String buildReservationCode(int slotId) {
		return "RSV-" + LocalDateTime.now().format(CODE_FORMAT) + "-S" + slotId;
	}

	private void logNotification(String reservationCode, String channel, String recipient, String message) throws Exception {
		String sql = "insert into notification_log (reservation_code,channel,recipient,message,status) values (?,?,?,?,?)";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, reservationCode);
			statement.setString(2, channel);
			statement.setString(3, recipient);
			statement.setString(4, message);
			statement.setString(5, "queued");
			statement.executeUpdate();
		}
	}

	private void logActivity(String actor, String activityType, String message) throws Exception {
		String sql = "insert into system_activity (actor,activity_type,activity_message) values (?,?,?)";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, actor);
			statement.setString(2, activityType);
			statement.setString(3, message);
			statement.executeUpdate();
		}
	}

	private int scalar(String sql) throws Exception {
		try (PreparedStatement statement = conn.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			return resultSet.next() ? resultSet.getInt(1) : 0;
		}
	}

	private void bind(PreparedStatement statement, List<Object> parameters) throws Exception {
		for (int index = 0; index < parameters.size(); index++) {
			statement.setObject(index + 1, parameters.get(index));
		}
	}
}
