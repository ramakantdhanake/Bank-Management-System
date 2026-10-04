package com.bank.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.bank.config.DBConnection;
import com.bank.dao.AccountDao;
import com.bank.dao.AccountDaoImpl;
import com.bank.dao.TransactionDao;
import com.bank.dao.TransactionDaoImpl;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.DatabaseException;
import com.bank.model.Account;
import com.bank.model.Transaction;

public class TransactionServiceImpl implements TransactionService {

	private AccountDao accountDao = new AccountDaoImpl();
	private TransactionDao transactionDao = new TransactionDaoImpl();

	@Override
	public List<Transaction> getTransactionsByAccount(long customerId, long accountId) {

		try (Connection con = DBConnection.getConnection()) {

			Account account = accountDao.findByIdAndCustomerId(con, accountId, customerId);

			if (account == null) {
				throw new AccountNotFoundException(
						"Account not found or you are not authorized to access this account.");
			}

			return transactionDao.findTransactionsByAccountId(accountId);

		} catch (SQLException e) {
			throw new DatabaseException("Failed to fetch transaction history.", e);
		}
	}

	@Override
	public List<Transaction> getTransactionsByAccount(long customerId, long accountId, int days) {

		try (Connection con = DBConnection.getConnection()) {

			Account account = accountDao.findByIdAndCustomerId(con, accountId, customerId);

			if (account == null) {
				throw new AccountNotFoundException(
						"Account not found or you are not authorized to access this account.");
			}

			return transactionDao.findTransactionsByAccountId(accountId, days);

		} catch (SQLException e) {
			throw new DatabaseException("Failed to fetch transaction history.", e);
		}
	}

}
