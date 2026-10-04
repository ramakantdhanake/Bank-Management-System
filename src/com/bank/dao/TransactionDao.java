package com.bank.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.bank.model.Transaction;

public interface TransactionDao {

	boolean saveTransaction(Connection con, Transaction transaction) throws SQLException;

	List<Transaction> findTransactionsByAccountId(long accountId) throws SQLException;

	List<Transaction> findTransactionsByAccountId(long accountId, int days) throws SQLException;

}
