package com.bank.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.bank.model.Account;

public interface AccountDao {

	long createAccount(Connection con, Account account) throws SQLException;

	void activateAccount(Connection con, long accountId) throws SQLException;

	List<Account> findAccountByCustomerId(long customerid) throws SQLException;

	void deposit(Connection con, long accountId, BigDecimal amount) throws SQLException;

	Account findByIdAndCustomerId(Connection con, long accountId, long customerId) throws SQLException;

	void withdraw(Connection con, long customerId, BigDecimal amount) throws SQLException;

	Account findByAccountNumber(String accountNumber) throws SQLException;

	Account findByIdAndCustomerIdForUpdate(Connection con, long accountId, long customerId) throws SQLException;

	Account findByAccountNumberForUpdate(Connection con, String accountNumber) throws SQLException;
	
	void debit(Connection con, long accountId, BigDecimal amount) throws SQLException;

	void credit(Connection con, long accountId, BigDecimal amount) throws SQLException;
	
	boolean existsByCustomerIdAndAccountType(long customerId, String accountType) throws SQLException;
}
