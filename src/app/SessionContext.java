package app;

/**
 * Holds the signed-in user for the current desktop session.
 */
public final class SessionContext {
	private static String username;
	private static UserRole role;

	private SessionContext() {
		// Utility class; instances are not needed.
	}

	public static void signIn(String signedInUsername, UserRole signedInRole) {
		username = signedInUsername;
		role = signedInRole;
	}

	public static void signOut() {
		username = null;
		role = null;
	}

	public static String username() {
		return username;
	}

	public static UserRole role() {
		return role;
	}

	public static boolean isAdmin() {
		return role == UserRole.ADMIN;
	}
}
