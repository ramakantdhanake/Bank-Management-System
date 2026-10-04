package com.bank.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

	public static String hashPassword(String password) {

		SecureRandom secureRandom = new SecureRandom();
		byte[] salt = new byte[16];
		secureRandom.nextBytes(salt);

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(salt);

			byte[] hashedPassword = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

			String saltString = Base64.getEncoder().encodeToString(salt);

			String hashString = Base64.getEncoder().encodeToString(hashedPassword);

			return saltString + ":" + hashString;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("hashing algorithm not available.", e);

		}

	}

	public static boolean verifyPassword(String password, String storePassword) {

		String[] parts = storePassword.split(":");

		byte[] salt = Base64.getDecoder().decode(parts[0]);

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

			messageDigest.update(salt);

			byte[] hashedPassword = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

			String hashString = Base64.getEncoder().encodeToString(hashedPassword);

			return hashString.equals(parts[1]);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("hashing algorithm not available", e);

		}

	}

}
