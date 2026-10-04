package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

	private long transactionId;

	private String referenceNumber;

	private long accountId;

	private Long relatedAccountId;

	private String transactionType;

	private BigDecimal amount;

	private BigDecimal balanceBefore;

	private BigDecimal balanceAfter;

	private String status;

	private String description;

	private LocalDateTime createdAt;

	// transaction ke liye

	public Transaction(String referenceNumber, long accountId, Long relatedAccountId,
			String transactionType, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
		super();
		this.referenceNumber = referenceNumber;
		this.accountId = accountId;
		this.relatedAccountId = relatedAccountId;
		this.transactionType = transactionType;
		this.amount = amount;
		this.balanceBefore = balanceBefore;
		this.balanceAfter = balanceAfter;
	}

	public Transaction(long transactionId, String referenceNumber, long accountId, Long relatedAccountId,
			String transactionType, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String status,
			String description, LocalDateTime createdAt) {
		super();
		this.transactionId = transactionId;
		this.referenceNumber = referenceNumber;
		this.accountId = accountId;
		this.relatedAccountId = relatedAccountId;
		this.transactionType = transactionType;
		this.amount = amount;
		this.balanceBefore = balanceBefore;
		this.balanceAfter = balanceAfter;
		this.status = status;
		this.description = description;
		this.createdAt = createdAt;
	}

	public Transaction() {
		// TODO Auto-generated constructor stub
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public Long getRelatedAccountId() {
		return relatedAccountId;
	}

	public void setRelatedAccountId(Long relatedAccountId) {
		this.relatedAccountId = relatedAccountId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBalanceBefore() {
		return balanceBefore;
	}

	public void setBalanceBefore(BigDecimal balanceBefore) {
		this.balanceBefore = balanceBefore;
	}

	public BigDecimal getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(BigDecimal balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
