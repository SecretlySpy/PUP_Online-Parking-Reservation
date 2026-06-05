package app;

import java.time.LocalDateTime;

/**
 * Read model for reservation history, admin review, and reports.
 */
public class ReservationRecord {
	private int reservationId;
	private String reservationCode;
	private String username;
	private int slotId;
	private String floor;
	private String slotType;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String status;
	private String customerName;
	private String email;
	private String phone;
	private String vehiclePlate;
	private String qrPayload;

	public ReservationRecord(int reservationId, String reservationCode, String username, int slotId, String floor,
			String slotType, LocalDateTime startTime, LocalDateTime endTime, String status, String customerName,
			String email, String phone, String vehiclePlate, String qrPayload) {
		this.reservationId = reservationId;
		this.reservationCode = reservationCode;
		this.username = username;
		this.slotId = slotId;
		this.floor = floor;
		this.slotType = slotType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.customerName = customerName;
		this.email = email;
		this.phone = phone;
		this.vehiclePlate = vehiclePlate;
		this.qrPayload = qrPayload;
	}

	public int getReservationId() {
		return reservationId;
	}

	public String getReservationCode() {
		return reservationCode;
	}

	public String getUsername() {
		return username;
	}

	public int getSlotId() {
		return slotId;
	}

	public String getFloor() {
		return floor;
	}

	public String getSlotType() {
		return slotType;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public String getStatus() {
		return status;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getVehiclePlate() {
		return vehiclePlate;
	}

	public String getQrPayload() {
		return qrPayload;
	}
}
