package app;

/**
 * Read model for a parking slot plus its computed availability for a selected time range.
 */
public class ParkingSlot {
	private int slotId;
	private String floor;
	private String slotType;
	private String baseStatus;
	private String availability;
	private String reservationCode;

	public ParkingSlot(int slotId, String floor, String slotType, String baseStatus) {
		this.slotId = slotId;
		this.floor = floor;
		this.slotType = slotType;
		this.baseStatus = baseStatus;
		this.availability = "Available";
		this.reservationCode = "";
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

	public String getBaseStatus() {
		return baseStatus;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public String getReservationCode() {
		return reservationCode;
	}

	public void setReservationCode(String reservationCode) {
		this.reservationCode = reservationCode;
	}
}
