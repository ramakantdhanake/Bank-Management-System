package com.bank.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.bank.config.DBConnection;
import com.bank.dao.AccountDao;
import com.bank.dao.AccountDaoImpl;
import com.bank.dao.CustomerDao;
import com.bank.dao.CustomerDaoImpl;
import com.bank.dao.TransactionDao;
import com.bank.dao.TransactionDaoImpl;
import com.bank.dto.TransactionResultDTO;
import com.bank.dto.TransferResultsDto;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.DatabaseException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.security.PasswordUtil;
import com.bank.util.AccountNumberGenerator;
import com.bank.util.ReferenceNumberGenerator;
import com.bank.validation.CustomerValidator;

public class AccountServiceImpl implements AccountService {

	private AccountDao accountDao = new AccountDaoImpl();
	private TransactionDao transactionDao = new TransactionDaoImpl();
	private CustomerDao customerDao = new CustomerDaoImpl();

	// open new account
	@Override
	public Account openAccount(long customerId, String accountType, BigDecimal innitialDeposit) {

		if (!"SAVINGS".equals(accountType) && !"CURRENT".equals(accountType) && !"ZERO_BALANCE".equals(accountType)) {

			throw new BusinessRuleException("Invalid account type.");
		}

		if (innitialDeposit == null) {
			throw new ValidationException("Invalid initial deposit amount.");
		}

		if (innitialDeposit.compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("Initial deposit cannot be negative.");
		}

		if (innitialDeposit.scale() > 2) {
			throw new ValidationException("Initial deposit can have maximum 2 decimal places.");
		}

		if ("SAVINGS".equals(accountType) && innitialDeposit.compareTo(new BigDecimal("1000")) < 0) {

			throw new BusinessRuleException("Savings Account requires a minimum initial deposit of ₹1,000.");
		}

		if ("CURRENT".equals(accountType) && innitialDeposit.compareTo(new BigDecimal("3000")) < 0) {

			throw new BusinessRuleException("Current Account requires a minimum initial deposit of ₹3,000.");
		}

//		if (!CustomerValidator.isValidAmount(innitialDeposit)) {
//			throw new ValidationException("Invalid initial deposit amount.");
//		}

		try {
			if (accountDao.existsByCustomerIdAndAccountType(customerId, accountType)) {

				if ("SAVINGS".equals(accountType)) {
					throw new BusinessRuleException(
							"You already have a Savings Account. You cannot open another Savings Account.");
				}

				if ("CURRENT".equals(accountType)) {
					throw new BusinessRuleException(
							"You already have a Current Account. You cannot open another Current Account.");
				}

				if ("ZERO_BALANCE".equals(accountType)) {
					throw new BusinessRuleException(
							"You already have a Zero Balance Account. You cannot open another Zero Balance Account.");
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException("Unable to check existing accounts right now.", e);
		}

		String accountNumber = AccountNumberGenerator.generateAccountNumber();

		Account account = new Account(accountNumber, customerId, accountType, innitialDeposit);

		Connection con = null;

		try {
			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			long accountId = accountDao.createAccount(con, account);
			account.setAccountId(accountId);

			String referenceNumber = ReferenceNumberGenerator.generateRefferenceNumber();

			Transaction transaction = new Transaction(referenceNumber, accountId, null, "ACCOUNT_OPENING",
					innitialDeposit, BigDecimal.ZERO, innitialDeposit);
			transaction.setStatus("SUCCESS");
			transaction.setDescription("Initial account opening deposit");
			transactionDao.saveTransaction(con, transaction);
			accountDao.activateAccount(con, accountId);
			con.commit();
			account.setStatus("ACTIVE");
			return account;

		} catch (Exception e) {

			try {
				if (con != null) {
					con.rollback();
				}
			} catch (Exception rollbackException) {
				rollbackException.printStackTrace();
			}
			throw new DatabaseException("unable to open account right now.", e);
		} finally {
			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		}

	}

	// get customers accounts by id

	@Override
	public List<Account> getCustomerAccounts(long customerId) {

		try {
			return accountDao.findAccountByCustomerId(customerId);
		} catch (SQLException e) {
			throw new DatabaseException("unable to fetch details right now.", e);
		}

	}

	// deposit amount
	@Override
	public TransactionResultDTO deposit(long customerId, long accountId, BigDecimal amount, String transactionPin) {

		if (!CustomerValidator.isValidAmount(amount)) {
			throw new ValidationException("Please enter a valid amount maximum 2 decimal number");
		}

		if (transactionPin == null || !transactionPin.matches("\\d{4}")) {
			throw new ValidationException("Transaction PIN must be exactly 4 digits.");
		}

		Customer customer;
		try {
			customer = customerDao.findById(customerId);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to verify customer details.", e);
		}

		if (customer == null) {
			throw new BusinessRuleException("Customer details not found.");
		}

		if (customer.getTransactionPinHash() == null) {
			throw new BusinessRuleException("Transaction PIN is not set.");
		}

		if (!PasswordUtil.verifyPassword(transactionPin, customer.getTransactionPinHash())) {
			throw new BusinessRuleException("Invalid Transaction PIN.");
		}

		Connection con = null;

		try {
			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			Account account = accountDao.findByIdAndCustomerId(con, accountId, customerId);

			if (account == null) {
				throw new BusinessRuleException("Account not found");
			}

			if (!"ACTIVE".equals(account.getStatus())) {
				throw new BusinessRuleException("Deposit not allow this account beacause your account is not active.");
			}

			BigDecimal balanceBefore = account.getBalance();
			BigDecimal balanceAfter = balanceBefore.add(amount);

			accountDao.deposit(con, accountId, amount);

			String refferenceNumber = ReferenceNumberGenerator.generateRefferenceNumber();

			Transaction transaction = new Transaction(refferenceNumber, accountId, null, "DEPOSIT", amount,
					balanceBefore, balanceAfter);

			transaction.setStatus("SUCCESS");
			transaction.setDescription("Money deposited into account.");

			transactionDao.saveTransaction(con, transaction);

			TransactionResultDTO result = new TransactionResultDTO();

			result.setAccountNumber(account.getAccountNumber());
			result.setAmount(amount);
			result.setBalanceBefore(balanceBefore);
			result.setBalanceAfter(balanceAfter);
			result.setStatus("SUCCESS");

			con.commit();

			return result;

		} catch (BusinessRuleException | ValidationException e) {
			try {
				if (con != null) {
					con.rollback();
				}
			} catch (SQLException rollbackException) {
			}

			throw e;
		} catch (SQLException e) {
			try {
				if (con != null) {
					con.rollback();
				}
			} catch (SQLException rollbackException) {
			}

			throw new DatabaseException("unable to process deposit right now.", e);
		} finally {

			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {
				}
			}

		}
	}

	// withdraw
	@Override
	public TransactionResultDTO withdraw(long customerId, long accountId, BigDecimal amount, String transactionPin) {

		if (!CustomerValidator.isValidAmount(amount)) {
			throw new ValidationException("Invalid withdrawal amount.");
		}

		if (transactionPin == null || !transactionPin.matches("\\d{4}")) {
			throw new ValidationException("Transaction PIN must be exactly 4 digits.");
		}

		Customer customer;
		try {
			customer = customerDao.findById(customerId);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to verify customer details.", e);
		}

		if (customer == null) {
			throw new BusinessRuleException("Customer details not found.");
		}

		if (customer.getTransactionPinHash() == null) {
			throw new BusinessRuleException("Transaction PIN is not set.");
		}

		if (!PasswordUtil.verifyPassword(transactionPin, customer.getTransactionPinHash())) {
			throw new BusinessRuleException("Invalid Transaction PIN.");
		}

		Connection con = null;

		try {
			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			Account account = accountDao.findByIdAndCustomerId(con, accountId, customerId);

			if (account == null) {
				throw new AccountNotFoundException("Account not found.");
			}

			if (!"ACTIVE".equals(account.getStatus())) {
				throw new BusinessRuleException("Account is not active.");
			}

			BigDecimal balanceBefore = account.getBalance();

			if (balanceBefore.compareTo(amount) < 0) {
				throw new BusinessRuleException("Insufficient balance.");
			}

			BigDecimal balanceAfter = balanceBefore.subtract(amount);

			accountDao.withdraw(con, accountId, amount);

			Transaction transaction = new Transaction(ReferenceNumberGenerator.generateRefferenceNumber(), accountId,
					null, "WITHDRAW", amount, balanceBefore, balanceAfter);

			transaction.setStatus("SUCCESS");
			transactionDao.saveTransaction(con, transaction);

			TransactionResultDTO result = new TransactionResultDTO();

			result.setAccountNumber(account.getAccountNumber());
			result.setAmount(amount);
			result.setBalanceBefore(balanceBefore);
			result.setBalanceAfter(balanceAfter);
			result.setStatus("SUCCESS");

			con.commit();

			return result;
		} catch (ValidationException | BusinessRuleException | AccountNotFoundException e) {

			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}

			throw e;

		} catch (SQLException e) {

			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}

			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new DatabaseException("Withdrawal failed due to database error.", e);

		} finally {

			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	@Override
	public TransferResultsDto transfer(long customerId, long senderAccountId, String receiverAccountNumber,
			BigDecimal amount, String transactionPin) {

		// 1. Basic validation
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ValidationException("Transfer amount must be greater than zero.");
		}

		if (receiverAccountNumber == null || !receiverAccountNumber.matches("\\d{12}")) {
			throw new ValidationException("Invalid receiver account number.");
		}

		if (transactionPin == null || !transactionPin.matches("\\d{4}")) {
			throw new ValidationException("Transaction PIN must be exactly 4 digits.");
		}

		// 2. Verify customer and transaction PIN
		Customer customer;

		try {
			customer = customerDao.findById(customerId);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to verify transaction details right now.", e);
		}

		if (customer == null) {
			throw new BusinessRuleException("Customer details not found.");
		}

		if (customer.getTransactionPinHash() == null) {
			throw new BusinessRuleException("Transaction PIN is not set for this account.");
		}

		if (!PasswordUtil.verifyPassword(transactionPin, customer.getTransactionPinHash())) {

			throw new BusinessRuleException("Invalid Transaction PIN.");
		}

		Connection con = null;

		try {

			// 3. Start database transaction
			con = DBConnection.getConnection();
			con.setAutoCommit(false);

			// 4. Lock sender account
			Account senderAccount = accountDao.findByIdAndCustomerIdForUpdate(con, senderAccountId, customerId);

			if (senderAccount == null) {
				throw new AccountNotFoundException("Sender account not found.");
			}

			// 5. Sender account status check
			if (!"ACTIVE".equals(senderAccount.getStatus())) {
				throw new BusinessRuleException("Sender account is not active.");
			}

			// 6. Lock receiver account
			Account receiverAccount = accountDao.findByAccountNumberForUpdate(con, receiverAccountNumber);

			if (receiverAccount == null) {
				throw new AccountNotFoundException("Receiver account not found.");
			}

			// 7. Receiver account status check
			if (!"ACTIVE".equals(receiverAccount.getStatus())) {
				throw new BusinessRuleException("Receiver account is not active.");
			}

			// 8. Prevent transfer to same account
			if (senderAccount.getAccountId() == receiverAccount.getAccountId()) {

				throw new BusinessRuleException("You cannot transfer money to the same account.");
			}

			// 9. Check sufficient balance
			BigDecimal senderBalanceBefore = senderAccount.getBalance();

			if (senderBalanceBefore.compareTo(amount) < 0) {
				throw new BusinessRuleException("Insufficient balance.");
			}

			// 10. Calculate balances
			BigDecimal senderBalanceAfter = senderBalanceBefore.subtract(amount);

			BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

			BigDecimal receiverBalanceAfter = receiverBalanceBefore.add(amount);

			// 11. Generate one reference number
			String referenceNumber = ReferenceNumberGenerator.generateRefferenceNumber();

			// 12. Debit sender
			accountDao.debit(con, senderAccount.getAccountId(), amount);

			// 13. Create sender transaction
			Transaction debitTransaction = new Transaction();

			debitTransaction.setReferenceNumber(referenceNumber);
			debitTransaction.setAccountId(senderAccount.getAccountId());
			debitTransaction.setRelatedAccountId(receiverAccount.getAccountId());
			debitTransaction.setTransactionType("TRANSFER_DEBIT");
			debitTransaction.setAmount(amount);
			debitTransaction.setBalanceBefore(senderBalanceBefore);
			debitTransaction.setBalanceAfter(senderBalanceAfter);
			debitTransaction.setStatus("SUCCESS");
			debitTransaction.setDescription("Transfer to account " + receiverAccountNumber);

			transactionDao.saveTransaction(con, debitTransaction);

			// 14. Credit receiver
			accountDao.credit(con, receiverAccount.getAccountId(), amount);

			// 15. Create receiver transaction
			Transaction creditTransaction = new Transaction();

			creditTransaction.setReferenceNumber(referenceNumber);
			creditTransaction.setAccountId(receiverAccount.getAccountId());
			creditTransaction.setRelatedAccountId(senderAccount.getAccountId());
			creditTransaction.setTransactionType("TRANSFER_CREDIT");
			creditTransaction.setAmount(amount);
			creditTransaction.setBalanceBefore(receiverBalanceBefore);
			creditTransaction.setBalanceAfter(receiverBalanceAfter);
			creditTransaction.setStatus("SUCCESS");
			creditTransaction.setDescription("Transfer from account " + senderAccount.getAccountNumber());

			transactionDao.saveTransaction(con, creditTransaction);

			// 16. Commit everything
			con.commit();

			// 17. Prepare result
			TransferResultsDto result = new TransferResultsDto();

			result.setReferenceNumber(referenceNumber);
			result.setSenderAccountNumber(senderAccount.getAccountNumber());
			result.setReceiverAccountNumber(receiverAccount.getAccountNumber());
			result.setAmount(amount);
			result.setSenderBalanceBefore(senderBalanceBefore);
			result.setSenderBalanceAfter(senderBalanceAfter);
			result.setStatus("SUCCESS");

			return result;

		} catch (ValidationException | BusinessRuleException | AccountNotFoundException e) {

			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}

			throw e;

		} catch (SQLException e) {

			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}

			throw new DatabaseException("Transfer failed. Please try again later.", e);

		} finally {

			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	@Override
	public boolean existsByCustomerIdAndAccountType(long customerId, String accountType) {

		try {
			return accountDao.existsByCustomerIdAndAccountType(customerId, accountType);

		} catch (SQLException e) {
			throw new DatabaseException("Unable to check existing account details right now.", e);
		}
	}

}
