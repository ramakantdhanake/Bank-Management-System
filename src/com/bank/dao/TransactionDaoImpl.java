package com.bank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bank.config.DBConnection;
import com.bank.model.Transaction;

public class TransactionDaoImpl implements TransactionDao {

	@Override
	public boolean saveTransaction(Connection con, Transaction transaction) throws SQLException {

		String sql = "INSERT INTO transactions " + "(reference_number, account_id, related_account_id, "
				+ "transaction_type, amount, balance_before, balance_after, " + "status, description) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, transaction.getReferenceNumber());
			pstmt.setLong(2, transaction.getAccountId());

			if (transaction.getRelatedAccountId() != null) {
				pstmt.setLong(3, transaction.getRelatedAccountId());
			} else {
				pstmt.setNull(3, java.sql.Types.BIGINT);
			}

			pstmt.setString(4, transaction.getTransactionType());
			pstmt.setBigDecimal(5, transaction.getAmount());
			pstmt.setBigDecimal(6, transaction.getBalanceBefore());
			pstmt.setBigDecimal(7, transaction.getBalanceAfter());
			pstmt.setString(8, transaction.getStatus());
			pstmt.setString(9, transaction.getDescription());

			return pstmt.executeUpdate() > 0;

		}
	}

	@Override
	public List<Transaction> findTransactionsByAccountId(long accountId) throws SQLException {

		List<Transaction> transactions = new ArrayList<>();

		String sql = "SELECT transaction_id, reference_number, account_id, "
				+ "related_account_id, transaction_type, amount, "
				+ "balance_before, balance_after, status, description, " + "created_at " + "FROM transactions "
				+ "WHERE account_id = ? " + "ORDER BY created_at DESC";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setLong(1, accountId);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					Transaction transaction = new Transaction();

					transaction.setTransactionId(rs.getLong("transaction_id"));

					transaction.setReferenceNumber(rs.getString("reference_number"));

					transaction.setAccountId(rs.getLong("account_id"));

					long relatedAccountId = rs.getLong("related_account_id");

					if (rs.wasNull()) {
						transaction.setRelatedAccountId(null);
					} else {
						transaction.setRelatedAccountId(relatedAccountId);
					}

					transaction.setTransactionType(rs.getString("transaction_type"));

					transaction.setAmount(rs.getBigDecimal("amount"));

					transaction.setBalanceBefore(rs.getBigDecimal("balance_before"));

					transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));

					transaction.setStatus(rs.getString("status"));

					transaction.setDescription(rs.getString("description"));

					if (rs.getTimestamp("created_at") != null) {
						transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					}

					transactions.add(transaction);
				}
			}
		}

		return transactions;
	}

	@Override
	public List<Transaction> findTransactionsByAccountId(long accountId, int days) throws SQLException {

		List<Transaction> transactions = new ArrayList<>();

		String sql = "SELECT transaction_id, reference_number, account_id, "
				+ "related_account_id, transaction_type, amount, "
				+ "balance_before, balance_after, status, description, created_at " + "FROM transactions "
				+ "WHERE account_id = ? " + "AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) "
				+ "ORDER BY created_at DESC";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setLong(1, accountId);
			ps.setInt(2, days);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					Transaction transaction = new Transaction();

					transaction.setTransactionId(rs.getLong("transaction_id"));

					transaction.setReferenceNumber(rs.getString("reference_number"));

					transaction.setAccountId(rs.getLong("account_id"));

					long relatedAccountId = rs.getLong("related_account_id");

					if (rs.wasNull()) {
						transaction.setRelatedAccountId(null);
					} else {
						transaction.setRelatedAccountId(relatedAccountId);
					}

					transaction.setTransactionType(rs.getString("transaction_type"));

					transaction.setAmount(rs.getBigDecimal("amount"));

					transaction.setBalanceBefore(rs.getBigDecimal("balance_before"));

					transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));

					transaction.setStatus(rs.getString("status"));

					transaction.setDescription(rs.getString("description"));

					if (rs.getTimestamp("created_at") != null) {
						transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					}

					transactions.add(transaction);
				}
			}
		}

		return transactions;
	}

}
