package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
	
	private long accountId;
	
	private String accountNumber;
	
	private long customerId;
	
	private String accountType;
	
	private BigDecimal balance;
	
	private String status;
	
	private LocalDateTime createdAt;

	// New Account

	public Account(String accountNumber, long customerId, String accountType, BigDecimal balance) {
		super();
		this.accountNumber = accountNumber;
		this.customerId = customerId;
		this.accountType = accountType;
		this.balance = balance;
	}
	
	// Full Account

	public Account(long accountId, String accountNumber, long customerId, String accountType, BigDecimal balance,
			String status, LocalDateTime createdAt) {
		super();
		this.accountId = accountId;
		this.accountNumber = accountNumber;
		this.customerId = customerId;
		this.accountType = accountType;
		this.balance = balance;
		this.status = status;
		this.createdAt = createdAt;
	}



	public Account() {
		// default
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	

}
