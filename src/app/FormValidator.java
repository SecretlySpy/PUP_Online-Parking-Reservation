package app;

import java.time.LocalDateTime;

/**
 * Shared validation rules for forms that need predictable error messages.
 */
public final class FormValidator {
	private FormValidator() {
		// Utility class; instances are not needed.
	}

	public static String required(String value, String label) {
		if (value == null || value.trim().isEmpty()) {
			return label + " is required.";
		}
		return null;
	}

	public static String email(String value) {
		if (value == null || !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			return "Enter a valid email address.";
		}
		return null;
	}

	public static String timeRange(LocalDateTime startTime, LocalDateTime endTime) {
		if (startTime == null || endTime == null) {
			return "Choose a reservation date and time range.";
		}
		if (!endTime.isAfter(startTime)) {
			return "End time must be later than start time.";
		}
		return null;
	}

	public static String firstError(String... errors) {
		for (String error : errors) {
			if (error != null) {
				return error;
			}
		}
		return null;
	}
}
