package com.bank.dto;

import java.math.BigDecimal;

public class TransferResultsDto {

	private String referenceNumber;
	private String senderAccountNumber;
	private String receiverAccountNumber;
	private BigDecimal amount;
	private BigDecimal senderBalanceBefore;
	private BigDecimal senderBalanceAfter;
	private String status;

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getSenderAccountNumber() {
		return senderAccountNumber;
	}

	public void setSenderAccountNumber(String senderAccountNumber) {
		this.senderAccountNumber = senderAccountNumber;
	}

	public String getReceiverAccountNumber() {
		return receiverAccountNumber;
	}

	public void setReceiverAccountNumber(String receiverAccountNumber) {
		this.receiverAccountNumber = receiverAccountNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getSenderBalanceBefore() {
		return senderBalanceBefore;
	}

	public void setSenderBalanceBefore(BigDecimal senderBalanceBefore) {
		this.senderBalanceBefore = senderBalanceBefore;
	}

	public BigDecimal getSenderBalanceAfter() {
		return senderBalanceAfter;
	}

	public void setSenderBalanceAfter(BigDecimal senderBalanceAfter) {
		this.senderBalanceAfter = senderBalanceAfter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
