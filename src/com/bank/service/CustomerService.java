package com.bank.service;

import java.time.LocalDate;

import com.bank.model.Customer;

public interface CustomerService {

	public boolean registerCustomer(Customer customer);

	Customer login(String loginInput, String password);

	boolean isCustomerRegistered(String loginInput);

	boolean isCustomerBlocked(String loginInput);

	Customer getCustomerProfile(long customerId);

	boolean changePassword(long customerId, String oldPassword, String newPassword);

	boolean forgotPassword(String loginInput, LocalDate dateOfBirth, String newPassword);

	Customer verifyCustomerForPasswordReset(String loginInput, LocalDate dateOfBirth);
}
