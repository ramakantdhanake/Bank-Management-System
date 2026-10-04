package com.bank.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bank.dao.CustomerDao;
import com.bank.dao.CustomerDaoImpl;
import com.bank.exception.AuthenticationException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.DatabaseException;
import com.bank.exception.DuplicateCostumerException;
import com.bank.exception.ValidationException;
import com.bank.model.Customer;
import com.bank.security.PasswordUtil;
import com.bank.validation.CustomerValidator;

public class CustomerServiceImpl implements CustomerService {

	private CustomerDao customerDao = new CustomerDaoImpl();

	@Override
	public boolean registerCustomer(Customer customer) {

		if (!CustomerValidator.isValidFullName(customer.getFullName())) {
			throw new ValidationException("Invalid full name.");
		}

		if (!CustomerValidator.isValidDateOfBirth(customer.getDateOfBirth())) {
			throw new ValidationException("Invalid date of birth.");
		}

		if (!CustomerValidator.isValidMobileNumber(customer.getMobileNumber())) {
			throw new ValidationException("Invalid mobile number.");
		}

		if (!CustomerValidator.isValidEmail(customer.getEmail())) {
			throw new ValidationException("Invalid email.");

		}

		if (!CustomerValidator.isValidAddress(customer.getAddress())) {
			throw new ValidationException("Invalid address.");
		}

		if (!CustomerValidator.isValidPassword(customer.getPasswordHash())) {
			throw new ValidationException("Invalid password.");
		}

		String hashedPassword = PasswordUtil.hashPassword(customer.getPasswordHash());

		customer.setPasswordHash(hashedPassword);

		try {

			if (customerDao.existsByMobileNumber(customer.getMobileNumber())) {
				throw new DuplicateCostumerException("Mobile number is already registered.");
			}

			if (customerDao.existsByEmail(customer.getEmail())) {
				throw new DuplicateCostumerException("Email is already registered.");

			}

		} catch (SQLException e) {
			throw new DatabaseException("Unable to check customer details.", e);
		}

		try {
			return customerDao.registerCustomer(customer);
		} catch (SQLException e) {

			if (e.getErrorCode() == 1062) {
				throw new DuplicateCostumerException("Mobile number or email already registered.");
			}

			throw new DatabaseException("Unable to register customer.", e);
		}
	}

	@Override
	public Customer login(String loginInput, String password) {

		try {

			Customer customer = customerDao.findByMobileOrEmail(loginInput);

			if (customer == null) {
				throw new AuthenticationException("Mobile number or email is not registered.");
			}

			if (customer.getBlockedUntil() != null
					&& !customer.getBlockedUntil().isAfter(java.time.LocalDateTime.now())) {

				customerDao.unblockCustomer(customer.getCustomerId());

				customer.setStatus("ACTIVE");
				customer.setFailedLoginAttempts(0);
				customer.setBlockedUntil(null);
//				throw new AuthenticationException("Your account is temporarily blocked. Please try again later.");
			}

			if (!PasswordUtil.verifyPassword(password, customer.getPasswordHash())) {

				int attempts = customer.getFailedLoginAttempts() + 1;

				if (attempts >= 3) {
					LocalDateTime blockUntil = LocalDateTime.now().plusHours(24);

					customerDao.blockCustomer(customer.getCustomerId(), attempts, blockUntil);
					throw new AuthenticationException(
							"Too many failed attempts. Your account is blocked for 24 hours.");
				}

				customerDao.updateFailedLoginAttempts(customer.getCustomerId(), attempts);

				int remainingAttempts = 3 - attempts;

				throw new AuthenticationException("Incorrect password. " + remainingAttempts + " attempts remaining.");
			}

			if (!"ACTIVE".equals(customer.getStatus())) {
				throw new AuthenticationException("your account is not active.");
			}

			customerDao.resetFailedLoginAttempts(customer.getCustomerId());

			return customer;

		} catch (SQLException e) {
			throw new DatabaseException("unable to process login right now.", e);
		}

	}

	@Override
	public boolean isCustomerRegistered(String loginInput) {

		try {
			Customer customer = customerDao.findByMobileOrEmail(loginInput);
			return customer != null;

		} catch (SQLException e) {
			throw new DatabaseException("Unable to check customer details right now.", e);
		}
	}

	@Override
	public boolean isCustomerBlocked(String loginInput) {

		try {
			Customer customer = customerDao.findByMobileOrEmail(loginInput);

			if (customer == null) {
				return false;
			}

			if ("BLOCKED".equals(customer.getStatus())) {

				if (customer.getBlockedUntil() != null
						&& customer.getBlockedUntil().isAfter(java.time.LocalDateTime.now())) {

					return true;
				}

				customerDao.unblockCustomer(customer.getCustomerId());
				return false;
			}

			return false;

		} catch (SQLException e) {

			throw new DatabaseException("Unable to check account status right now.", e);
		}
	}

	@Override
	public Customer getCustomerProfile(long customerId) {
		try {
			return customerDao.findById(customerId);
		} catch (SQLException e) {
			throw new DatabaseException("unable to fetch customer profile.", e);
		}
	}

	@Override
	public boolean changePassword(long customerId, String oldPassword, String newPassword) {

		try {
			Customer customer = customerDao.findById(customerId);

			if (customer == null) {
				throw new AuthenticationException("Customer not found.");
			}

			if (!PasswordUtil.verifyPassword(oldPassword, customer.getPasswordHash())) {
				throw new AuthenticationException("old password is incorrect.");
			}

			if (PasswordUtil.verifyPassword(newPassword, customer.getPasswordHash())) {
				throw new BusinessRuleException("New password must be different from old password.");
			}

			if (!CustomerValidator.isValidPassword(newPassword)) {
				throw new ValidationException("New password is invalid.");
			}

			String newPasswordHash = PasswordUtil.hashPassword(newPassword);

			customer.setPasswordHash(newPasswordHash);

			// dao

			customerDao.updatePassword(customerId, newPasswordHash);

			return true;

		} catch (SQLException e) {
			throw new DatabaseException("Unable to change password.", e);
		}

	}

	@Override
	public boolean forgotPassword(String loginInput, LocalDate dateOfBirth, String newPassword) {

		try {

			// 1. Validate new password
			if (!CustomerValidator.isValidPassword(newPassword)) {
				throw new ValidationException("New password does not meet the required criteria.");
			}

			// 2. Find customer using Email/Mobile + Date of Birth
			Customer customer = customerDao.findByEmailOrMobileAndDateOfBirth(loginInput, dateOfBirth);

			// 3. Customer not found
			if (customer == null) {
				throw new AuthenticationException("Invalid date of birth.");
			}

			// 4. Check new password is different from old password
			if (PasswordUtil.verifyPassword(newPassword, customer.getPasswordHash())) {

				throw new BusinessRuleException("New password must be different from old password.");
			}

			// 5. Hash new password
			String newPasswordHash = PasswordUtil.hashPassword(newPassword);

			// 6. Update password in database
			customerDao.updatePassword(customer.getCustomerId(), newPasswordHash);

			return true;

		} catch (SQLException e) {

			throw new DatabaseException("Unable to reset password right now.", e);
		}
	}

	@Override
	public Customer verifyCustomerForPasswordReset(String loginInput, LocalDate dateOfBirth) {

		try {

			Customer customer = customerDao.findByEmailOrMobileAndDateOfBirth(loginInput, dateOfBirth);

			if (customer == null) {
				throw new AuthenticationException("Invalid date of birth.");
			}

			return customer;

		} catch (SQLException e) {

			throw new DatabaseException("Unable to verify customer details right now.", e);
		}
	}

}
