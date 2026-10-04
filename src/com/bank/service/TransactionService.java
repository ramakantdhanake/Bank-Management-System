package com.bank.service;

import java.util.List;

import com.bank.model.Transaction;

public interface TransactionService {

	List<Transaction> getTransactionsByAccount(long customerId, long accountId);

	List<Transaction> getTransactionsByAccount(long customerId, long accountId, int days);

}
