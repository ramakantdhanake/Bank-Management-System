package com.bank.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import com.bank.exception.AuthenticationException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.DatabaseException;
import com.bank.exception.DuplicateCostumerException;
import com.bank.exception.ValidationException;
import com.bank.model.Customer;
import com.bank.security.PasswordUtil;
import com.bank.service.CustomerService;
import com.bank.service.CustomerServiceImpl;
import com.bank.util.ConsoleUtil;
import com.bank.validation.CustomerValidator;

public class CustomerController {

	private Scanner sc = new Scanner(System.in);
	private CustomerService customerService = new CustomerServiceImpl();
	private AccountController accountControler = new AccountController();

	// 1. REGISTER CUSTOMER

	public void registerCustomer() {
		System.out.println();
		ConsoleUtil.printHeader("CUSTOMER REGISTRATION • CREATE NEW ACCOUNT");

		String fullName;
		LocalDate dateOfBirth;
		String mobileNumber;
		String email;
		String address;
		String password;

		// First, Middle & Last Name Min 2 required
		while (true) {
			System.out.println(ConsoleUtil.B_CYAN + "\n  ┌─── PERSONAL DETAILS ───────────────────────────────────┐"
					+ ConsoleUtil.RESET);
			System.out
					.print(ConsoleUtil.WHITE + "  │ Enter First Name                           : " + ConsoleUtil.RESET);
			String firstName = sc.nextLine().trim();

			System.out
					.print(ConsoleUtil.WHITE + "  │ Enter Middle Name (Press Enter to skip)   : " + ConsoleUtil.RESET);
			String middleName = sc.nextLine().trim();

			System.out
					.print(ConsoleUtil.WHITE + "  │ Enter Last Name (Press Enter to skip)     : " + ConsoleUtil.RESET);
			String lastName = sc.nextLine().trim();
			System.out.println(ConsoleUtil.B_CYAN + "  └────────────────────────────────────────────────────────┘"
					+ ConsoleUtil.RESET);

			int count = 0;
			if (!firstName.isEmpty())
				count++;
			if (!middleName.isEmpty())
				count++;
			if (!lastName.isEmpty())
				count++;

			if (count < 2) {
				ConsoleUtil.printError("You must provide at least 2 names (e.g., First Name & Last Name).");
				continue;
			}

			boolean validLetters = true;
			if (!firstName.isEmpty() && !firstName.matches("^[a-zA-Z]+$"))
				validLetters = false;
			if (!middleName.isEmpty() && !middleName.matches("^[a-zA-Z]+$"))
				validLetters = false;
			if (!lastName.isEmpty() && !lastName.matches("^[a-zA-Z]+$"))
				validLetters = false;

			if (!validLetters) {
				ConsoleUtil.printError("Names must contain alphabets only.");
				continue;
			}

			StringBuilder sb = new StringBuilder();
			if (!firstName.isEmpty())
				sb.append(firstName);
			if (!middleName.isEmpty()) {
				if (sb.length() > 0)
					sb.append(" ");
				sb.append(middleName);
			}
			if (!lastName.isEmpty()) {
				if (sb.length() > 0)
					sb.append(" ");
				sb.append(lastName);
			}
			fullName = sb.toString();
			break;
		}

		// Date of Birth
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Enter Date of Birth (YYYY-MM-DD)           : " + ConsoleUtil.RESET);
			String dobInput = sc.nextLine().trim();

			try {
				dateOfBirth = LocalDate.parse(dobInput);
				if (CustomerValidator.isValidDateOfBirth(dateOfBirth)) {
					break;
				}
				ConsoleUtil.printError("Invalid Date of Birth. Age must be at least 18 years.");
			} catch (DateTimeParseException e) {
				ConsoleUtil.printError("Invalid format! Use YYYY-MM-DD format.");
			}
		}

		// Mobile Number
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Enter 10-Digit Mobile Number               : " + ConsoleUtil.RESET);
			mobileNumber = sc.nextLine().trim();

			if (CustomerValidator.isValidMobileNumber(mobileNumber)) {
				if (customerService.isCustomerRegistered(mobileNumber)) {
					ConsoleUtil.printError("Mobile number is already registered!");
					if (ConsoleUtil.confirmAction(sc, "Do you want to enter a different mobile number?")) {
						continue;
					} else {
						return;
					}
				}
				break;
			}
			ConsoleUtil.printError("Invalid mobile number! Must be 10 digits.");
		}

		// Email
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Enter Email Address                        : " + ConsoleUtil.RESET);
			email = sc.nextLine().trim();

			if (CustomerValidator.isValidEmail(email)) {
				if (customerService.isCustomerRegistered(email)) {
					ConsoleUtil.printError("Email is already registered!");
					if (ConsoleUtil.confirmAction(sc, "Do you want to enter a different email?")) {
						continue;
					} else {
						return;
					}
				}
				break;
			}
			ConsoleUtil.printError("Invalid email format!");
		}

		// Address
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Enter Residential Address                  : " + ConsoleUtil.RESET);
			address = sc.nextLine().trim();

			if (CustomerValidator.isValidAddress(address)) {
				break;
			}
			ConsoleUtil.printError("Address cannot be empty or invalid.");
		}

		// Password
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Create Strong Password                     : " + ConsoleUtil.RESET);
			password = sc.nextLine();

			if (CustomerValidator.isValidPassword(password)) {
				break;
			}
			ConsoleUtil.printError("Password must have 8+ chars, 1 uppercase, 1 digit, 1 special character.");
		}

		// Transaction PIN
		String transactionPin;
		while (true) {
			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Set 4-Digit Transaction PIN                : " + ConsoleUtil.RESET);
			transactionPin = sc.nextLine().trim();

			if (transactionPin.matches("\\d{4}")) {
				break;
			}
			ConsoleUtil.printError("Transaction PIN must be exactly 4 digits.");
		}

		Customer customer = new Customer(fullName, dateOfBirth, mobileNumber, email, address, password);
		customer.setTransactionPinHash(PasswordUtil.hashPassword(transactionPin));

		try {
			boolean result = customerService.registerCustomer(customer);
			if (result) {
				ConsoleUtil.printSuccessBox("REGISTRATION SUCCESSFUL",
						"Welcome to RD Bank! Your Customer ID is: " + customer.getCustomerId());

				if (ConsoleUtil.confirmAction(sc, "Do you want to open a Bank Account now?")) {
					accountControler.openAccount(customer.getCustomerId());
				}
			} else {
				ConsoleUtil.printError("Registration failed. Please try again.");
			}
		} catch (ValidationException | DuplicateCostumerException | DatabaseException e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}

	// 2. LOGIN MENU

	public void login() {
		while (true) {
			System.out.println();
			ConsoleUtil.printHeader("CUSTOMER LOGIN • SECURE ACCESS");

			System.out.println(ConsoleUtil.B_CYAN + "  [1] " + ConsoleUtil.WHITE + "Login with Email");
			System.out.println(ConsoleUtil.B_CYAN + "  [2] " + ConsoleUtil.WHITE + "Login with Mobile");
			System.out.println(ConsoleUtil.B_CYAN + "  [3] " + ConsoleUtil.WHITE + "Forgot Password");
			System.out
					.println(ConsoleUtil.B_CYAN + "  [4] " + ConsoleUtil.RED + "Back to Main Menu" + ConsoleUtil.RESET);
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Select an option : " + ConsoleUtil.RESET);

			String option = sc.nextLine().trim();

			if ("1".equals(option)) {
				loginWithEmail();
				return;
			} else if ("2".equals(option)) {
				loginWithMobile();
				return;
			} else if ("3".equals(option)) {
				forgotPassword();
			} else if ("4".equals(option)) {
				return;
			} else {
				ConsoleUtil.printError("Invalid selection! Please enter 1, 2, 3 or 4.");
			}
		}
	}

	private void loginWithEmail() {
		String email = null;
		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter Registered Email : " + ConsoleUtil.RESET);
			email = sc.nextLine().trim();

			try {
				if (!customerService.isCustomerRegistered(email)) {
					ConsoleUtil.printError("Email is not registered with us.");
					if (ConsoleUtil.confirmAction(sc, "Do you want to re-enter email?")) {
						continue;
					} else {
						return;
					}
				}

				if (customerService.isCustomerBlocked(email)) {
					ConsoleUtil.printError("Account is locked for 24 hours due to multiple failed attempts.");
					return;
				}
				break;
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}

		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Password : " + ConsoleUtil.RESET);
			String password = sc.nextLine();

			try {
				Customer customer = customerService.login(email, password);
				ConsoleUtil.printSuccessBox("LOGIN SUCCESS", "Welcome back, " + customer.getFullName() + "!");
				showDashboard(customer);
				return;
			} catch (AuthenticationException e) {
				ConsoleUtil.printError(e.getMessage());
				if (!e.getMessage().contains("attempts remaining")) {
					return;
				}
				if (!ConsoleUtil.confirmAction(sc, "Do you want to retry password?")) {
					return;
				}
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}
	}

	private void loginWithMobile() {
		String mobile = null;
		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter Registered Mobile Number : " + ConsoleUtil.RESET);
			mobile = sc.nextLine().trim();

			try {
				if (!customerService.isCustomerRegistered(mobile)) {
					ConsoleUtil.printError("Mobile number is not registered with us.");
					if (ConsoleUtil.confirmAction(sc, "Do you want to re-enter mobile number?")) {
						continue;
					} else {
						return;
					}
				}

				if (customerService.isCustomerBlocked(mobile)) {
					ConsoleUtil.printError("Account is locked for 24 hours due to multiple failed attempts.");
					return;
				}
				break;
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}

		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Password : " + ConsoleUtil.RESET);
			String password = sc.nextLine();

			try {
				Customer customer = customerService.login(mobile, password);
				ConsoleUtil.printSuccessBox("LOGIN SUCCESS", "Welcome back, " + customer.getFullName() + "!");
				showDashboard(customer);
				return;
			} catch (AuthenticationException e) {
				ConsoleUtil.printError(e.getMessage());
				if (!e.getMessage().contains("attempts remaining")) {
					return;
				}
				if (!ConsoleUtil.confirmAction(sc, "Do you want to retry password?")) {
					return;
				}
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}
	}

	// 3. FORGOT PASSWORD (VIP OTP UI)

	public void forgotPassword() {
		System.out.println();
		ConsoleUtil.printHeader("RESET PASSWORD • OTP VERIFICATION");

		String mobileNumber;
		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter Registered Mobile Number : " + ConsoleUtil.RESET);
			mobileNumber = sc.nextLine().trim();

			try {
				if (!customerService.isCustomerRegistered(mobileNumber)) {
					ConsoleUtil.printError("Mobile number does not exist.");
					if (ConsoleUtil.confirmAction(sc, "Do you want to try again?")) {
						continue;
					} else {
						return;
					}
				}
				break;
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}

		SecureRandom random = new SecureRandom();
		String generatedOtp = String.format("%06d", random.nextInt(1000000));
		long otpExpiryTime = System.currentTimeMillis() + (60 * 1000);

		System.out.println(ConsoleUtil.YELLOW + "\n  ┌─── SECURE OTP GATEWAY ─────────────────────────────────┐");
		System.out.println(
				"  │ " + ConsoleUtil.WHITE + "Generated 6-Digit OTP : " + ConsoleUtil.BOLD + ConsoleUtil.B_GREEN
						+ generatedOtp + ConsoleUtil.RESET + ConsoleUtil.YELLOW + "                        │");
		System.out.println("  │ " + ConsoleUtil.WHITE + "Validity               : 1 Minute Only                  "
				+ ConsoleUtil.YELLOW + "│");
		System.out.println("  └────────────────────────────────────────────────────────┘" + ConsoleUtil.RESET);

		int attempts = 0;
		boolean otpVerified = false;

		while (attempts < 5) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter 6-Digit OTP : " + ConsoleUtil.RESET);
			String enteredOtp = sc.nextLine().trim();

			if (System.currentTimeMillis() > otpExpiryTime) {
				ConsoleUtil.printError("OTP expired! (1 minute time limit exceeded).");
				return;
			}

			if (generatedOtp.equals(enteredOtp)) {
				ConsoleUtil.printSuccessBox("OTP VERIFIED", "Identity confirmed via mobile OTP.");
				otpVerified = true;
				break;
			} else {
				attempts++;
				int remaining = 5 - attempts;
				ConsoleUtil.printError("Invalid OTP. Remaining attempts: " + remaining);
				if (remaining > 0 && !ConsoleUtil.confirmAction(sc, "Do you want to re-enter OTP?")) {
					return;
				}
			}
		}

		if (!otpVerified) {
			ConsoleUtil.printError("Maximum OTP attempts reached.");
			return;
		}

		LocalDate dateOfBirth = null;
		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter Date of Birth (YYYY-MM-DD) : " + ConsoleUtil.RESET);
			String dobInput = sc.nextLine().trim();

			try {
				dateOfBirth = LocalDate.parse(dobInput);
				customerService.verifyCustomerForPasswordReset(mobileNumber, dateOfBirth);
				ConsoleUtil.printSuccessBox("DOB MATCHED", "Personal details successfully authenticated.");
				break;
			} catch (DateTimeParseException | AuthenticationException e) {
				ConsoleUtil.printError(e.getMessage());
				if (!ConsoleUtil.confirmAction(sc, "Do you want to re-enter Date of Birth?")) {
					return;
				}
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				return;
			}
		}

		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Enter New Password : " + ConsoleUtil.RESET);
			String newPassword = sc.nextLine();

			if (!CustomerValidator.isValidPassword(newPassword)) {
				ConsoleUtil.printError("Password must contain 8+ chars, uppercase, digit, special character.");
				continue;
			}

			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Confirm New Password : " + ConsoleUtil.RESET);
			String confirmPassword = sc.nextLine();

			if (!newPassword.equals(confirmPassword)) {
				ConsoleUtil.printError("Passwords do not match!");
				continue;
			}

			try {
				boolean result = customerService.forgotPassword(mobileNumber, dateOfBirth, newPassword);
				if (result) {
					ConsoleUtil.printSuccessBox("PASSWORD UPDATED",
							"Your password has been changed. Please login now.");
					return;
				}
			} catch (ValidationException | BusinessRuleException | DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
			}
		}
	}

	// 4. CUSTOMER DASHBOARD & PROFILE

	public void showDashboard(Customer customer) {
		while (true) {
			System.out.println();
			ConsoleUtil.printHeader("CUSTOMER DASHBOARD • RD BANK OF INDIA");

			System.out.println(ConsoleUtil.B_CYAN + ConsoleUtil.BOLD + "  [1] " + ConsoleUtil.WHITE + "View Accounts");
			System.out.println(ConsoleUtil.B_CYAN + "  [2] " + ConsoleUtil.WHITE + "Open New Account");
			System.out.println(ConsoleUtil.B_CYAN + "  [3] " + ConsoleUtil.WHITE + "Deposit Money");
			System.out.println(ConsoleUtil.B_CYAN + "  [4] " + ConsoleUtil.WHITE + "Withdraw Money");
			System.out.println(ConsoleUtil.B_CYAN + "  [5] " + ConsoleUtil.WHITE + "Transfer Money");
			System.out.println(ConsoleUtil.B_CYAN + "  [6] " + ConsoleUtil.WHITE + "Transaction History");
			System.out.println(ConsoleUtil.B_CYAN + "  [7] " + ConsoleUtil.WHITE + "My Profile");
			System.out.println(ConsoleUtil.B_CYAN + "  [8] " + ConsoleUtil.WHITE + "Change Password");
			System.out.println(ConsoleUtil.B_CYAN + "  [9] " + ConsoleUtil.RED + "Logout" + ConsoleUtil.RESET);
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Select an option : " + ConsoleUtil.RESET);

			String input = sc.nextLine().trim();

			switch (input) {
			case "1":
				accountControler.viewAccounts(customer.getCustomerId());
				break;
			case "2":
				accountControler.openAccount(customer.getCustomerId());
				break;
			case "3":
				accountControler.depositMoney(customer.getCustomerId());
				break;
			case "4":
				accountControler.withdrawMoney(customer.getCustomerId());
				break;
			case "5":
				accountControler.transferMoney(customer.getCustomerId());
				break;
			case "6":
				accountControler.transactionHistory(customer.getCustomerId());
				break;
			case "7":
				viewProfile(customer.getCustomerId());
				break;
			case "8":
				changePassword(customer.getCustomerId());
				break;
			case "9":
				if (ConsoleUtil.confirmAction(sc, "Are you sure you want to log out?")) {
					System.out.println(
							ConsoleUtil.B_GREEN + "\n  ✔ Successfully logged out. Thank you!" + ConsoleUtil.RESET);
					return;
				}
				break;
			default:
				ConsoleUtil.printError("Invalid choice! Please select 1 through 9.");
			}
		}
	}

	public void viewProfile(long customerId) {
		Customer customer = customerService.getCustomerProfile(customerId);

		System.out.println();
		ConsoleUtil.printHeader("MY PROFILE • ACCOUNT OWNER");

		System.out.println(ConsoleUtil.B_CYAN + "  ┌────────────────────────────────────────────────────────┐"
				+ ConsoleUtil.RESET);
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : " + ConsoleUtil.BOLD + "%-33s "
				+ ConsoleUtil.B_CYAN + "│%n" + ConsoleUtil.RESET, "Customer ID", customer.getCustomerId());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : " + ConsoleUtil.BOLD + "%-33s "
				+ ConsoleUtil.B_CYAN + "│%n" + ConsoleUtil.RESET, "Full Name", customer.getFullName());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : %-33s " + ConsoleUtil.B_CYAN
				+ "│%n" + ConsoleUtil.RESET, "Date of Birth", customer.getDateOfBirth());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : %-33s " + ConsoleUtil.B_CYAN
				+ "│%n" + ConsoleUtil.RESET, "Mobile Number", customer.getMobileNumber());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : %-33s " + ConsoleUtil.B_CYAN
				+ "│%n" + ConsoleUtil.RESET, "Email", customer.getEmail());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : %-33s " + ConsoleUtil.B_CYAN
				+ "│%n" + ConsoleUtil.RESET, "Address", customer.getAddress());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : " + ConsoleUtil.B_GREEN + "%-33s "
				+ ConsoleUtil.B_CYAN + "│%n" + ConsoleUtil.RESET, "Account Status", customer.getStatus());
		System.out.printf(ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "%-18s : %-33s " + ConsoleUtil.B_CYAN
				+ "│%n" + ConsoleUtil.RESET, "Member Since", customer.getCreatedAt().toLocalDate());
		System.out.println(ConsoleUtil.B_CYAN + "  └────────────────────────────────────────────────────────┘"
				+ ConsoleUtil.RESET);
	}

	public void changePassword(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("SECURITY SETTINGS • CHANGE PASSWORD");

		while (true) {
			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Current Password : " + ConsoleUtil.RESET);
			String oldPassword = sc.nextLine();

			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter New Password     : " + ConsoleUtil.RESET);
			String newPassword = sc.nextLine();

			if (!CustomerValidator.isValidPassword(newPassword)) {
				ConsoleUtil.printError("New password does not meet security criteria.");
				continue;
			}

			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Confirm New Password   : " + ConsoleUtil.RESET);
			String confirmPassword = sc.nextLine();

			if (!newPassword.equals(confirmPassword)) {
				ConsoleUtil.printError("Passwords do not match!");
				continue;
			}

			try {
				boolean result = customerService.changePassword(customerId, oldPassword, newPassword);
				if (result) {
					ConsoleUtil.printSuccessBox("PASSWORD CHANGED", "Login password has been updated securely.");
					break;
				}
			} catch (AuthenticationException | ValidationException | BusinessRuleException e) {
				ConsoleUtil.printError(e.getMessage());
				if (!ConsoleUtil.confirmAction(sc, "Do you want to retry changing password?")) {
					break;
				}
			} catch (DatabaseException e) {
				ConsoleUtil.printError(e.getMessage());
				break;
			}
		}
	}
}