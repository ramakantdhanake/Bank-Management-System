package com.bank.dao;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bank.config.DBConnection;
import com.bank.model.Customer;

public class CustomerDaoImpl implements CustomerDao {

	@Override
	public boolean registerCustomer(Customer customer) throws SQLException {

		String sql = "INSERT INTO customers "
				+ "(full_name, date_of_birth, mobile_number, email, address, password_hash,transaction_pin_hash) "
				+ "VALUES (?, ?, ?, ?, ?, ?,?)";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, customer.getFullName());
			pstmt.setDate(2, java.sql.Date.valueOf(customer.getDateOfBirth()));
			pstmt.setString(3, customer.getMobileNumber());
			pstmt.setString(4, customer.getEmail());
			pstmt.setString(5, customer.getAddress());
			pstmt.setString(6, customer.getPasswordHash());
			pstmt.setString(7, customer.getTransactionPinHash());

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						customer.setCustomerId(rs.getLong(1));
					}
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean existsByMobileNumber(String mobileNumber) throws SQLException {

		String sql = "SELECT 1 FROM customers WHERE mobile_number = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, mobileNumber);

			return pstmt.executeQuery().next();
		}
	}

	@Override
	public boolean existsByEmail(String email) throws SQLException {

		String sql = "SELECT 1 FROM customers WHERE email = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, email);

			return pstmt.executeQuery().next();
		}
	}

	@Override
	public Customer findByMobileOrEmail(String loginInput) throws SQLException {

		String sql = "SELECT customer_id, full_name, date_of_birth, "
				+ "mobile_number, email, address, password_hash, transaction_pin_hash,"
				+ "status, failed_login_attempts,blocked_until,created_at " + "FROM customers "
				+ "WHERE mobile_number = ? OR email = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, loginInput);
			pstmt.setString(2, loginInput);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					Customer customer = new Customer(rs.getLong("customer_id"), rs.getString("full_name"),
							rs.getDate("date_of_birth").toLocalDate(), rs.getString("mobile_number"),
							rs.getString("email"), rs.getString("address"), rs.getString("password_hash"),
							rs.getString("status"), rs.getInt("failed_login_attempts"),
							rs.getTimestamp("created_at").toLocalDateTime());

					customer.setTransactionPinHash(rs.getString("transaction_pin_hash"));

					customer.setBlockedUntil(rs.getTimestamp("blocked_until") != null
							? rs.getTimestamp("blocked_until").toLocalDateTime()
							: null);

					return customer;
				}

			}
		}

		return null;
	}

	@Override
	public void updateFailedLoginAttempts(long customerId, int attempts) throws SQLException {

		String sql = "UPDATE customers SET failed_login_attempts = ? WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, attempts);
			pstmt.setLong(2, customerId);

			pstmt.executeUpdate();
		}
	}

	@Override
	public void blockCustomer(long customerId, int attempts, LocalDateTime blockedUntil) throws SQLException {

		String sql = "UPDATE customers " + "SET failed_login_attempts = ?, status = 'BLOCKED', blocked_until = ? "
				+ "WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, attempts);
			pstmt.setTimestamp(2, Timestamp.valueOf(blockedUntil));
			pstmt.setLong(3, customerId);

			pstmt.executeUpdate();
		}
	}

	@Override
	public void resetFailedLoginAttempts(long customerId) throws SQLException {
		String sql = "UPDATE customers SET failed_login_attempts = 0 WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, customerId);

			pstmt.executeUpdate();

		}

	}

	@Override
	public void unblockCustomer(long customerId) throws SQLException {

		String sql = "UPDATE customers " + "SET status = 'ACTIVE', " + "failed_login_attempts = 0, "
				+ "blocked_until = NULL " + "WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, customerId);
			pstmt.executeUpdate();
		}

	}

	@Override
	public Customer findById(long customerId) throws SQLException {

		String sql = "SELECT customer_id, full_name, date_of_birth, " + "mobile_number, email, address, password_hash, "
				+ "transaction_pin_hash, status, failed_login_attempts, " + "blocked_until, created_at "
				+ "FROM customers " + "WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setLong(1, customerId);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {

					Customer customer = new Customer(rs.getLong("customer_id"), rs.getString("full_name"),
							rs.getDate("date_of_birth").toLocalDate(), rs.getString("mobile_number"),
							rs.getString("email"), rs.getString("address"), rs.getString("password_hash"),
							rs.getString("status"), rs.getInt("failed_login_attempts"),
							rs.getTimestamp("created_at").toLocalDateTime());

					customer.setTransactionPinHash(rs.getString("transaction_pin_hash"));

					customer.setBlockedUntil(rs.getTimestamp("blocked_until") != null
							? rs.getTimestamp("blocked_until").toLocalDateTime()
							: null);

					return customer;
				}
			}
		}

		return null;
	}

	@Override
	public void updatePassword(long customerId, String passwordHash) throws SQLException {

		String sql = "UPDATE customers SET password_hash = ? WHERE customer_id = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, passwordHash);
			ps.setLong(2, customerId);

			ps.executeUpdate();
		}
	}

	@Override
	public Customer findByEmailOrMobileAndDateOfBirth(String loginInput, LocalDate dateOfBirth) throws SQLException {

		String sql = "SELECT customer_id, full_name, date_of_birth, " + "mobile_number, email, address, password_hash, "
				+ "transaction_pin_hash, status, failed_login_attempts, " + "blocked_until, created_at "
				+ "FROM customers " + "WHERE (mobile_number = ? OR email = ?) " + "AND date_of_birth = ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, loginInput);
			ps.setString(2, loginInput);
			ps.setDate(3, java.sql.Date.valueOf(dateOfBirth));

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					Customer customer = new Customer();

					customer.setCustomerId(rs.getLong("customer_id"));
					customer.setFullName(rs.getString("full_name"));

					java.sql.Date dob = rs.getDate("date_of_birth");
					if (dob != null) {
						customer.setDateOfBirth(dob.toLocalDate());
					}

					customer.setMobileNumber(rs.getString("mobile_number"));
					customer.setEmail(rs.getString("email"));
					customer.setAddress(rs.getString("address"));
					customer.setPasswordHash(rs.getString("password_hash"));
					customer.setTransactionPinHash(rs.getString("transaction_pin_hash"));
					customer.setStatus(rs.getString("status"));
					customer.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));

					java.sql.Timestamp blockedUntil = rs.getTimestamp("blocked_until");

					if (blockedUntil != null) {
						customer.setBlockedUntil(blockedUntil.toLocalDateTime());
					}

					java.sql.Timestamp createdAt = rs.getTimestamp("created_at");

					if (createdAt != null) {
						customer.setCreatedAt(createdAt.toLocalDateTime());
					}

					return customer;
				}
			}
		}

		return null;
	}
}
