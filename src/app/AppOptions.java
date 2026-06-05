package app;

import java.time.Year;

/**
 * Shared dropdown values for registration and vehicle forms.
 */
public final class AppOptions {
	private static final int FIRST_BIRTH_YEAR = 1930;

	private static final String[] CAR_TYPES = new String[] { "Select -/-", "Sedan", "Hatchback", "Coupe",
			"Convertible", "Station Wagon", "SUV", "Crossover SUV (CUV)", "Luxury SUV", "Luxury Car",
			"Sports Car", "Pickup Truck", "Truck", "Van", "Minivan", "MPV", "Compact Car", "Subcompact Car",
			"Hybrid", "Plug-in Hybrid", "Electric Vehicle (EV)", "Motorcycle" };

	private AppOptions() {
		// Utility class; instances are not needed.
	}

	public static String[] birthYearsThroughCurrentYear() {
		int currentYear = Year.now().getValue();
		String[] years = new String[(currentYear - FIRST_BIRTH_YEAR) + 1];
		for (int index = 0; index < years.length; index++) {
			years[index] = String.valueOf(FIRST_BIRTH_YEAR + index);
		}
		return years;
	}

	public static String[] carTypes() {
		return CAR_TYPES.clone();
	}
}
