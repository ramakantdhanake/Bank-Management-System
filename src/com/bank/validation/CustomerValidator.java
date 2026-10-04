package com.bank.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

public class CustomerValidator {

	// 1 firstName

	public static boolean isValidFullName(String fullName) {

		if (fullName == null || fullName.trim().isEmpty()) {
			return false;
		}

		if (fullName.length() < 2 || fullName.length() > 100) {
			return false;
		}

		String regex = "^[a-zA-Z]+(?: [a-zA-Z]+)*$";

		return fullName.matches(regex);

	}

	// 2 Date of Birth

	public static boolean isValidDateOfBirth(LocalDate dateOfBirth) {

		if (dateOfBirth == null) {
			return false;
		}

		LocalDate today = LocalDate.now();

		if (dateOfBirth.isAfter(today)) {
			return false;

		}

		int age = Period.between(dateOfBirth, today).getYears();

		return age >= 18;

	}

	// mobile number

	public static boolean isValidMobileNumber(String number) {

		String regex = "^[6-9]\\d{9}$";
		
//		String regex = "^(\\+91|91|0)?[6-9]\\d{9}$";

		if (number == null) {
			return false;
		}

		return number.matches(regex);

	}

	// email

	public static boolean isValidEmail(String email) {

		if (email == null || email.trim().isEmpty()) {
			return false;
		}

//		String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		String regex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

		return email.matches(regex);
	}

	// address

	public static boolean isValidAddress(String address) {

		String regex = "^[a-zA-Z0-9\\s.,'#-]{3,255}$";

		if (address == null || address.trim().isEmpty()) {
			return false;

		}

		return address.matches(regex);

	}

	// password

	public static boolean isValidPassword(String password) {

		String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

		if (password == null || password.trim().isEmpty()) {
			return false;
		}

		return password.matches(regex);

	}

	// ----------------------------

	// amount

	public static boolean isValidAmount(BigDecimal amount) {

		if (amount == null) {
			return false;
		}

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {

			return false;
		}

		if (amount.stripTrailingZeros().scale() > 2) {
			return false;
		}

		return true;

	}

	// account number
	public static boolean isValidAccountNumber(String accountNumber) {

		if (accountNumber == null || accountNumber.trim().isEmpty()) {
			return false;
		}

		String regex = "^\\d{12}$";

		return accountNumber.matches(regex);
	}

}
