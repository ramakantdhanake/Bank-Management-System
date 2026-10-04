package com.bank.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {

	private long customerId;

	private String fullName;

	private LocalDate dateOfBirth;

	private String mobileNumber;

	private String email;

	private String address;

	private String passwordHash;

	private String status;

	private int failedLoginAttempts;

	private LocalDateTime createdAt;
	
	private LocalDateTime blockedUntil;
	
	private String transactionPinHash;
	

	// Register ke liye

	public Customer(String fullName, LocalDate dateOfBirth, String mobileNumber, String email, String address,
			String passwordHash) {
		super();
		this.fullName = fullName;
		this.dateOfBirth = dateOfBirth;
		this.mobileNumber = mobileNumber;
		this.email = email;
		this.address = address;
		this.passwordHash = passwordHash;
	}

	// ye pura kam me ayega
	public Customer(long customerId, String fullName, LocalDate dateOfBirth, String mobileNumber, String email,
			String address, String passwordHash, String status, int failedLoginAttempts, LocalDateTime createdAt) {
		super();
		this.customerId = customerId;
		this.fullName = fullName;
		this.dateOfBirth = dateOfBirth;
		this.mobileNumber = mobileNumber;
		this.email = email;
		this.address = address;
		this.passwordHash = passwordHash;
		this.status = status;
		this.failedLoginAttempts = failedLoginAttempts;
		this.createdAt = createdAt;
	}
	
	public Customer() {
		// TODO Auto-generated constructor stub
	}

	public LocalDateTime getBlockedUntil() {
	    return blockedUntil;
	}

	public void setBlockedUntil(LocalDateTime blockedUntil) {
	    this.blockedUntil = blockedUntil;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getTransactionPinHash() {
		return transactionPinHash;
	}

	public void setTransactionPinHash(String transactionPinHash) {
		this.transactionPinHash = transactionPinHash;
	}

}