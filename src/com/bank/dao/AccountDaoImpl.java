package com.bank.dao;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bank.config.DBConnection;
import com.bank.model.Account;

public class AccountDaoImpl implements AccountDao {

	// account create
	@Override
	public long createAccount(Connection con, Account account) throws SQLException {

		String sql = "INSERT INTO accounts " + "(account_number, customer_id, account_type, balance) "
				+ "VALUES (?, ?, ?, ?)";

		try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, account.getAccountNumber());
			pstmt.setLong(2, account.getCustomerId());
			pstmt.setString(3, account.getAccountType());
			pstmt.setBigDecimal(4, account.getBalance());

			pstmt.executeUpdate();

			try (ResultSet rs = pstmt.getGeneratedKeys()) {

				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		}

		throw new SQLException("Account ID could not be generated.");
	}

	// check activate
	@Override
	public void activateAccount(Connection con, long accountId) throws SQLException {

		String sql = "UPDATE accounts SET status = 'ACTIVE' WHERE account_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, accountId);
			pstmt.executeUpdate();
		}
	}

	// find customer
	@Override
	public List<Account> findAccountByCustomerId(long customerid) throws SQLException {

		List<Account> accounts = new ArrayList<Account>();

		String sql = "SELECT account_id, account_number, customer_id,account_type, balance, status, created_at FROM accounts WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, customerid);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {

					Account account = new Account();

					account.setAccountId(rs.getLong("account_id"));
					account.setAccountNumber(rs.getString("account_number"));
					account.setCustomerId(rs.getLong("customer_id"));
					account.setAccountType(rs.getString("account_type"));
					account.setBalance(rs.getBigDecimal("balance"));
					account.setStatus(rs.getString("status"));

					if (rs.getTimestamp("created_at") != null) {
						account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					}

					accounts.add(account);

				}
			}

		}
		return accounts;
	}

	// deposit
	@Override
	public void deposit(Connection con, long accountId, BigDecimal amount) throws SQLException {

		String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setBigDecimal(1, amount);
			pstmt.setLong(2, accountId);
			pstmt.executeUpdate();
		}

	}

	// find by id and customer id
	@Override
	public Account findByIdAndCustomerId(Connection con, long accountId, long customerId) throws SQLException {

		String sql = "SELECT account_id, account_number, customer_id, " + "account_type, balance, status, created_at "
				+ "FROM accounts " + "WHERE account_id = ? AND customer_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, accountId);
			pstmt.setLong(2, customerId);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {

					Account account = new Account();

					account.setAccountId(rs.getLong("account_id"));
					account.setAccountNumber(rs.getString("account_number"));
					account.setCustomerId(rs.getLong("customer_id"));
					account.setAccountType(rs.getString("account_type"));
					account.setBalance(rs.getBigDecimal("balance"));
					account.setStatus(rs.getString("status"));

					if (rs.getTimestamp("created_at") != null) {
						account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					}

					return account;
				}
			}
		}

		return null;
	}

	// withdraw
	@Override
	public void withdraw(Connection con, long customerId, BigDecimal amount) throws SQLException {

		String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setBigDecimal(1, amount);
			pstmt.setLong(2, customerId);

			pstmt.executeUpdate();
		}

	}

	// find account number to transaction
	@Override
	public Account findByAccountNumber(String accountNumber) throws SQLException {

		String sql = "SELECT account_id, account_number, customer_id, " + "account_type, balance, status, created_at "
				+ "FROM accounts " + "WHERE account_number = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, accountNumber);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {

					return new Account(rs.getLong("account_id"), rs.getString("account_number"),
							rs.getLong("customer_id"), rs.getString("account_type"), rs.getBigDecimal("balance"),
							rs.getString("status"), rs.getTimestamp("created_at").toLocalDateTime());
				}
			}
		}

		return null;
	}

	@Override
	public Account findByIdAndCustomerIdForUpdate(Connection con, long accountId, long customerId) throws SQLException {

		String sql = "SELECT account_id, account_number, customer_id, " + "account_type, balance, status, created_at "
				+ "FROM accounts " + "WHERE account_id = ? AND customer_id = ? " + "FOR UPDATE";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, accountId);
			pstmt.setLong(2, customerId);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					return new Account(rs.getLong("account_id"), rs.getString("account_number"),
							rs.getLong("customer_id"), rs.getString("account_type"), rs.getBigDecimal("balance"),
							rs.getString("status"), rs.getTimestamp("created_at").toLocalDateTime());
				}
			}
		}

		return null;
	}

	@Override
	public Account findByAccountNumberForUpdate(Connection con, String accountNumber) throws SQLException {

		String sql = "SELECT account_id, account_number, customer_id, " + "account_type, balance, status, created_at "
				+ "FROM accounts " + "WHERE account_number = ? " + "FOR UPDATE";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, accountNumber);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					return new Account(rs.getLong("account_id"), rs.getString("account_number"),
							rs.getLong("customer_id"), rs.getString("account_type"), rs.getBigDecimal("balance"),
							rs.getString("status"), rs.getTimestamp("created_at").toLocalDateTime());
				}
			}
		}

		return null;
	}

	@Override
	public boolean existsByCustomerIdAndAccountType(long customerId, String accountType) throws SQLException {

		String sql = "SELECT COUNT(*) FROM accounts WHERE customer_id = ? AND account_type = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setLong(1, customerId);
			ps.setString(2, accountType);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		}

		return false;
	}

	@Override
	public void debit(Connection con, long accountId, BigDecimal amount) throws SQLException {

		String sql = "UPDATE accounts SET balance = balance - ? " + "WHERE account_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setBigDecimal(1, amount);
			pstmt.setLong(2, accountId);

			pstmt.executeUpdate();
		}
	}

	@Override
	public void credit(Connection con, long accountId, BigDecimal amount) throws SQLException {

		String sql = "UPDATE accounts SET balance = balance + ? " + "WHERE account_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setBigDecimal(1, amount);
			pstmt.setLong(2, accountId);

			pstmt.executeUpdate();
		}
	}

}
