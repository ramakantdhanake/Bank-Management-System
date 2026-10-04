package com.bank.dao;

import java.sql.SQLException;
import java.time.LocalDate;

import com.bank.model.Customer;

public interface CustomerDao {

	public boolean registerCustomer(Customer customer) throws SQLException;

	boolean existsByMobileNumber(String mobileNumber) throws SQLException;

	boolean existsByEmail(String email) throws SQLException;

	Customer findByMobileOrEmail(String loginInput) throws SQLException;

	void updateFailedLoginAttempts(long customerId, int attempts) throws SQLException;

	void blockCustomer(long customerId, int attempts, java.time.LocalDateTime blockedUntil) throws SQLException;

	void resetFailedLoginAttempts(long customerId) throws SQLException;

	void unblockCustomer(long customerId) throws SQLException;

	Customer findById(long customerId) throws SQLException;

	void updatePassword(long customerId, String passwordHash) throws SQLException;

	Customer findByEmailOrMobileAndDateOfBirth(String loginInput, LocalDate dateOfBirth) throws SQLException;

}
