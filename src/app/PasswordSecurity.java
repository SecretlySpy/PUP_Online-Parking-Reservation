package app;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Centralizes password hashing and validation. Existing plain-text seed records
 * are still verifiable so they can be upgraded after a successful login.
 */
public final class PasswordSecurity {
	private static final String HASH_PREFIX = "pbkdf2_sha256";
	private static final int CURRENT_ITERATIONS = 120000;
	private static final int SALT_BYTES = 16;
	private static final int HASH_BYTES = 32;
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int MAX_PASSWORD_LENGTH = 128;
	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordSecurity() {
		// Utility class; instances are not needed.
	}

	public static String hash(char[] password) throws GeneralSecurityException {
		byte[] salt = new byte[SALT_BYTES];
		RANDOM.nextBytes(salt);
		byte[] hash = pbkdf2(password, salt, CURRENT_ITERATIONS);
		return HASH_PREFIX + "$" + CURRENT_ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(hash);
	}

	public static boolean verify(char[] password, String storedPassword) throws GeneralSecurityException {
		if (storedPassword == null || storedPassword.trim().isEmpty()) {
			return false;
		}

		if (!isHashed(storedPassword)) {
			return constantTimeLegacyEquals(password, storedPassword);
		}

		String[] parts = storedPassword.split("\\$");
		if (parts.length != 4) {
			return false;
		}

		int iterations = Integer.parseInt(parts[1]);
		byte[] salt = Base64.getDecoder().decode(parts[2]);
		byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
		byte[] actualHash = pbkdf2(password, salt, iterations);
		return constantTimeEquals(actualHash, expectedHash);
	}

	public static boolean needsRehash(String storedPassword) {
		if (!isHashed(storedPassword)) {
			return true;
		}

		String[] parts = storedPassword.split("\\$");
		if (parts.length != 4) {
			return true;
		}

		try {
			return Integer.parseInt(parts[1]) < CURRENT_ITERATIONS;
		} catch (NumberFormatException ex) {
			return true;
		}
	}

	public static String strengthError(char[] password) {
		if (password == null || password.length == 0) {
			return "Password is required.";
		}
		if (password.length < MIN_PASSWORD_LENGTH) {
			return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.";
		}
		if (password.length > MAX_PASSWORD_LENGTH) {
			return "Password must be " + MAX_PASSWORD_LENGTH + " characters or fewer.";
		}

		boolean hasUppercase = false;
		boolean hasLowercase = false;
		boolean hasDigit = false;
		boolean hasSymbol = false;
		for (char value : password) {
			if (Character.isUpperCase(value)) {
				hasUppercase = true;
			} else if (Character.isLowerCase(value)) {
				hasLowercase = true;
			} else if (Character.isDigit(value)) {
				hasDigit = true;
			} else {
				hasSymbol = true;
			}
		}

		if (!hasUppercase || !hasLowercase || !hasDigit || !hasSymbol) {
			return "Password must include uppercase, lowercase, number, and symbol characters.";
		}
		return null;
	}

	public static boolean matches(char[] first, char[] second) {
		if (first == null || second == null) {
			return false;
		}

		int difference = first.length ^ second.length;
		int maxLength = Math.max(first.length, second.length);
		for (int index = 0; index < maxLength; index++) {
			char firstValue = index < first.length ? first[index] : 0;
			char secondValue = index < second.length ? second[index] : 0;
			difference |= firstValue ^ secondValue;
		}
		return difference == 0;
	}

	public static void clear(char[] password) {
		if (password != null) {
			Arrays.fill(password, '\0');
		}
	}

	private static boolean isHashed(String storedPassword) {
		return storedPassword != null && storedPassword.startsWith(HASH_PREFIX + "$");
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
		KeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BYTES * Byte.SIZE);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		return factory.generateSecret(spec).getEncoded();
	}

	private static boolean constantTimeLegacyEquals(char[] password, String storedPassword) {
		byte[] actual = new String(password).getBytes(StandardCharsets.UTF_8);
		byte[] expected = storedPassword.getBytes(StandardCharsets.UTF_8);
		try {
			return constantTimeEquals(actual, expected);
		} finally {
			Arrays.fill(actual, (byte) 0);
			Arrays.fill(expected, (byte) 0);
		}
	}

	private static boolean constantTimeEquals(byte[] actual, byte[] expected) {
		if (actual == null || expected == null) {
			return false;
		}

		int difference = actual.length ^ expected.length;
		int maxLength = Math.max(actual.length, expected.length);
		for (int index = 0; index < maxLength; index++) {
			byte actualValue = index < actual.length ? actual[index] : 0;
			byte expectedValue = index < expected.length ? expected[index] : 0;
			difference |= actualValue ^ expectedValue;
		}
		return difference == 0;
	}
}
