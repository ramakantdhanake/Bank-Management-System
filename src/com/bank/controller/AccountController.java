package com.bank.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import com.bank.dto.TransactionResultDTO;
import com.bank.dto.TransferResultsDto;
import com.bank.exception.DatabaseException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.AccountService;
import com.bank.service.AccountServiceImpl;
import com.bank.service.TransactionService;
import com.bank.service.TransactionServiceImpl;
import com.bank.util.ConsoleUtil;
import com.bank.validation.CustomerValidator;

public class AccountController {

	private Scanner sc = new Scanner(System.in);
	private AccountService accountService = new AccountServiceImpl();
	private TransactionService transactionService = new TransactionServiceImpl();

	// Helper to print active accounts
	private void renderAccounts(List<Account> accounts) {
		System.out.println(ConsoleUtil.B_CYAN + "\n  ┌─── YOUR ACTIVE BANK ACCOUNTS ──────────────────────────┐"
				+ ConsoleUtil.RESET);
		for (Account acc : accounts) {
			System.out.printf(
					ConsoleUtil.B_CYAN + "  │ " + ConsoleUtil.WHITE + "Account No: " + ConsoleUtil.BOLD + "%-14s"
							+ ConsoleUtil.RESET + " Type: %-10s Balance: " + ConsoleUtil.B_GREEN + "₹%-10s "
							+ ConsoleUtil.B_CYAN + "│%n" + ConsoleUtil.RESET,
					acc.getAccountNumber(), acc.getAccountType(), acc.getBalance());
		}
		System.out.println(ConsoleUtil.B_CYAN + "  └────────────────────────────────────────────────────────┘"
				+ ConsoleUtil.RESET);
	}

	// 1. VIEW ACCOUNTS
	public void viewAccounts(long customerId) {
		try {
			List<Account> accounts = accountService.getCustomerAccounts(customerId);
			if (accounts == null || accounts.isEmpty()) {
				ConsoleUtil.printError("You do not have any registered bank accounts.");
				return;
			}
			renderAccounts(accounts);
		} catch (DatabaseException e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}

	// 2. OPEN NEW ACCOUNT
	public void openAccount(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("OPEN NEW ACCOUNT • PORTFOLIO");

		while (true) {
			System.out.println(
					ConsoleUtil.B_CYAN + "  [1] " + ConsoleUtil.WHITE + "Savings Account      (Min Deposit: ₹1,000)");
			System.out.println(
					ConsoleUtil.B_CYAN + "  [2] " + ConsoleUtil.WHITE + "Current Account      (Min Deposit: ₹3,000)");
			System.out.println(
					ConsoleUtil.B_CYAN + "  [3] " + ConsoleUtil.WHITE + "Zero Balance Account (Min Deposit: ₹0)");
			System.out
					.println(ConsoleUtil.B_CYAN + "  [4] " + ConsoleUtil.RED + "Back to Dashboard" + ConsoleUtil.RESET);
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Select Account Type : " + ConsoleUtil.RESET);

			String choice = sc.nextLine().trim();
			String accountType;

			if ("1".equals(choice))
				accountType = "SAVINGS";
			else if ("2".equals(choice))
				accountType = "CURRENT";
			else if ("3".equals(choice))
				accountType = "ZERO_BALANCE";
			else if ("4".equals(choice))
				return;
			else {
				ConsoleUtil.printError("Invalid selection! Please enter 1, 2, 3 or 4.");
				continue;
			}

			try {
				if (accountService.existsByCustomerIdAndAccountType(customerId, accountType)) {
					ConsoleUtil.printError("You already hold a " + accountType + " account with us.");
					continue;
				}
			} catch (DatabaseException e) {
				ConsoleUtil.printError("Database connection issue.");
				return;
			}

			BigDecimal initialDeposit = BigDecimal.ZERO;
			if (!"ZERO_BALANCE".equals(accountType)) {
				while (true) {
					System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Initial Deposit Amount : ₹" + ConsoleUtil.RESET);
					String amtStr = sc.nextLine().trim();
					try {
						initialDeposit = new BigDecimal(amtStr);
						if ("SAVINGS".equals(accountType) && initialDeposit.compareTo(new BigDecimal("1000")) < 0) {
							ConsoleUtil.printError("Savings Account requires at least ₹1,000.");
							continue;
						}
						if ("CURRENT".equals(accountType) && initialDeposit.compareTo(new BigDecimal("3000")) < 0) {
							ConsoleUtil.printError("Current Account requires at least ₹3,000.");
							continue;
						}
						break;
					} catch (NumberFormatException e) {
						ConsoleUtil.printError("Invalid amount number.");
					}
				}
			}

			if (ConsoleUtil.confirmAction(sc,
					"Confirm opening " + accountType + " account with initial deposit ₹" + initialDeposit + "?")) {
				try {
					Account account = accountService.openAccount(customerId, accountType, initialDeposit);
					ConsoleUtil.printSuccessBox("ACCOUNT OPENED SUCCESSFULLY",
							"Account No: " + account.getAccountNumber() + " | Type: " + account.getAccountType()
									+ " | Balance: ₹" + account.getBalance());
					return;
				} catch (Exception e) {
					ConsoleUtil.printError(e.getMessage());
				}
			} else {
				return;
			}
		}
	}

	// 3. DEPOSIT MONEY
	public void depositMoney(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("CASH DEPOSIT • INSTANT CREDIT");

		try {
			List<Account> accounts = accountService.getCustomerAccounts(customerId);
			if (accounts == null || accounts.isEmpty()) {
				ConsoleUtil.printError("No accounts found for deposit.");
				return;
			}

			renderAccounts(accounts);

			if (!ConsoleUtil.confirmAction(sc, "Do you want to proceed with deposit?")) {
				return;
			}

			long accountId;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter 12-Digit Account Number : " + ConsoleUtil.RESET);
				String accNum = sc.nextLine().trim();
				Account selected = null;
				for (Account a : accounts) {
					if (a.getAccountNumber().equals(accNum)) {
						selected = a;
						break;
					}
				}
				if (selected == null) {
					ConsoleUtil.printError("Account number not found in your list.");
					continue;
				}
				if (!"ACTIVE".equals(selected.getStatus())) {
					ConsoleUtil.printError("Account is currently inactive.");
					continue;
				}
				accountId = selected.getAccountId();
				break;
			}

			BigDecimal amount;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Deposit Amount : ₹" + ConsoleUtil.RESET);
				try {
					amount = new BigDecimal(sc.nextLine().trim());
					if (CustomerValidator.isValidAmount(amount))
						break;
					ConsoleUtil.printError("Enter a valid positive amount.");
				} catch (NumberFormatException e) {
					ConsoleUtil.printError("Invalid numeric amount.");
				}
			}

			String transactionPin;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter 4-Digit Transaction PIN : " + ConsoleUtil.RESET);
				transactionPin = sc.nextLine().trim();
				if (transactionPin.matches("\\d{4}"))
					break;
				ConsoleUtil.printError("Transaction PIN must be exactly 4 digits.");
			}

			if (ConsoleUtil.confirmAction(sc, "Confirm deposit of ₹" + amount + "?")) {
				TransactionResultDTO res = accountService.deposit(customerId, accountId, amount, transactionPin);
				System.out.println(
						ConsoleUtil.B_GREEN + "\n  ┌─── TRANSACTION RECEIPT ────────────────────────────────┐");
				System.out.println("  │ Status        : " + ConsoleUtil.BOLD + "SUCCESSFUL" + ConsoleUtil.RESET
						+ ConsoleUtil.B_GREEN);
				System.out.println("  │ Deposited     : ₹" + res.getAmount());
				System.out.println("  │ Updated Balance: ₹" + res.getBalanceAfter());
				System.out.println("  └────────────────────────────────────────────────────────┘" + ConsoleUtil.RESET);
			} else {
				System.out.println(ConsoleUtil.RED + "\n  Transaction cancelled." + ConsoleUtil.RESET);
			}

		} catch (Exception e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}

	// 4. WITHDRAW MONEY
	public void withdrawMoney(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("CASH WITHDRAWAL • INSTANT DEBIT");

		try {
			List<Account> accounts = accountService.getCustomerAccounts(customerId);
			if (accounts == null || accounts.isEmpty()) {
				ConsoleUtil.printError("No accounts found for withdrawal.");
				return;
			}

			renderAccounts(accounts);

			if (!ConsoleUtil.confirmAction(sc, "Do you want to proceed with withdrawal?")) {
				return;
			}

			long accountId;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter 12-Digit Account Number : " + ConsoleUtil.RESET);
				String accNum = sc.nextLine().trim();
				Account selected = null;
				for (Account a : accounts) {
					if (a.getAccountNumber().equals(accNum)) {
						selected = a;
						break;
					}
				}
				if (selected == null) {
					ConsoleUtil.printError("Account number not found.");
					continue;
				}
				if (!"ACTIVE".equals(selected.getStatus())) {
					ConsoleUtil.printError("Account is inactive.");
					continue;
				}
				accountId = selected.getAccountId();
				break;
			}

			BigDecimal amount;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Withdrawal Amount : ₹" + ConsoleUtil.RESET);
				try {
					amount = new BigDecimal(sc.nextLine().trim());
					if (CustomerValidator.isValidAmount(amount))
						break;
					ConsoleUtil.printError("Enter a valid positive amount.");
				} catch (NumberFormatException e) {
					ConsoleUtil.printError("Invalid numeric amount.");
				}
			}

			String transactionPin;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter 4-Digit Transaction PIN : " + ConsoleUtil.RESET);
				transactionPin = sc.nextLine().trim();
				if (transactionPin.matches("\\d{4}"))
					break;
				ConsoleUtil.printError("Transaction PIN must be 4 digits.");
			}

			if (ConsoleUtil.confirmAction(sc, "Confirm cash withdrawal of ₹" + amount + "?")) {
				TransactionResultDTO res = accountService.withdraw(customerId, accountId, amount, transactionPin);
				System.out.println(
						ConsoleUtil.B_GREEN + "\n  ┌─── TRANSACTION RECEIPT ────────────────────────────────┐");
				System.out.println("  │ Status        : " + ConsoleUtil.BOLD + "SUCCESSFUL" + ConsoleUtil.RESET
						+ ConsoleUtil.B_GREEN);
				System.out.println("  │ Withdrawn     : ₹" + res.getAmount());
				System.out.println("  │ Remaining Bal : ₹" + res.getBalanceAfter());
				System.out.println("  └────────────────────────────────────────────────────────┘" + ConsoleUtil.RESET);
			} else {
				System.out.println(ConsoleUtil.RED + "\n  Transaction cancelled." + ConsoleUtil.RESET);
			}

		} catch (Exception e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}

	// 5. TRANSFER MONEY
	public void transferMoney(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("FUND TRANSFER • FAST NEFT / IMPS");

		try {
			List<Account> accounts = accountService.getCustomerAccounts(customerId);
			if (accounts == null || accounts.isEmpty()) {
				ConsoleUtil.printError("You do not have any registered accounts.");
				return;
			}

			renderAccounts(accounts);

			if (!ConsoleUtil.confirmAction(sc, "Do you want to proceed with transfer?")) {
				return;
			}

			long senderAccountId;
			String senderAccNum;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Sender Account Number : " + ConsoleUtil.RESET);
				senderAccNum = sc.nextLine().trim();
				Account selected = null;
				for (Account a : accounts) {
					if (a.getAccountNumber().equals(senderAccNum)) {
						selected = a;
						break;
					}
				}
				if (selected == null) {
					ConsoleUtil.printError("Sender account not found.");
					continue;
				}
				senderAccountId = selected.getAccountId();
				break;
			}

			String receiverAccNum;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Beneficiary Account Number : " + ConsoleUtil.RESET);
				receiverAccNum = sc.nextLine().trim();
				if (!CustomerValidator.isValidAccountNumber(receiverAccNum)) {
					ConsoleUtil.printError("Invalid beneficiary account format.");
					continue;
				}
				if (senderAccNum.equals(receiverAccNum)) {
					ConsoleUtil.printError("Sender and beneficiary accounts cannot be identical.");
					continue;
				}
				break;
			}

			BigDecimal amount;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Transfer Amount : ₹" + ConsoleUtil.RESET);
				try {
					amount = new BigDecimal(sc.nextLine().trim());
					if (CustomerValidator.isValidAmount(amount))
						break;
					ConsoleUtil.printError("Enter a valid amount.");
				} catch (NumberFormatException e) {
					ConsoleUtil.printError("Invalid numeric amount.");
				}
			}

			String transactionPin;
			while (true) {
				System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter 4-Digit Transaction PIN : " + ConsoleUtil.RESET);
				transactionPin = sc.nextLine().trim();
				if (transactionPin.matches("\\d{4}"))
					break;
				ConsoleUtil.printError("Transaction PIN must be 4 digits.");
			}

			System.out.println(ConsoleUtil.YELLOW + "\n  ┌─── TRANSFER VERIFICATION ──────────────────────────────┐");
			System.out.println("  │ From Account : " + ConsoleUtil.WHITE + senderAccNum + ConsoleUtil.YELLOW);
			System.out.println("  │ To Account   : " + ConsoleUtil.WHITE + receiverAccNum + ConsoleUtil.YELLOW);
			System.out.println("  │ Amount       : " + ConsoleUtil.BOLD + ConsoleUtil.WHITE + "₹" + amount
					+ ConsoleUtil.RESET + ConsoleUtil.YELLOW);
			System.out.println("  └────────────────────────────────────────────────────────┘" + ConsoleUtil.RESET);

			if (ConsoleUtil.confirmAction(sc, "Are you sure you want to transfer this amount?")) {
				TransferResultsDto res = accountService.transfer(customerId, senderAccountId, receiverAccNum, amount,
						transactionPin);
				System.out.println(
						ConsoleUtil.B_GREEN + "\n  ╔════════════════════════════════════════════════════════╗");
				System.out.println("  ║                TRANSFER SUCCESSFUL                     ║");
				System.out.println("  ╚════════════════════════════════════════════════════════╝");
				System.out.println("  │ Reference ID  : " + res.getReferenceNumber());
				System.out.println("  │ Amount Sent   : ₹" + res.getAmount());
				System.out.println("  │ Remainder Bal : ₹" + res.getSenderBalanceAfter() + ConsoleUtil.RESET);
			} else {
				System.out.println(ConsoleUtil.RED + "\n  Transfer cancelled." + ConsoleUtil.RESET);
			}

		} catch (Exception e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}

	// 6. TRANSACTION HISTORY (VIP PASSBOOK TABLE)
	public void transactionHistory(long customerId) {
		System.out.println();
		ConsoleUtil.printHeader("ACCOUNT STATEMENT • PASSBOOK");

		try {
			List<Account> accounts = accountService.getCustomerAccounts(customerId);
			if (accounts == null || accounts.isEmpty()) {
				ConsoleUtil.printError("No accounts found.");
				return;
			}

			renderAccounts(accounts);

			System.out.print(ConsoleUtil.B_CYAN + "  ➤ Enter Account Number : " + ConsoleUtil.RESET);
			String accNum = sc.nextLine().trim();
			Account selected = null;
			for (Account a : accounts) {
				if (a.getAccountNumber().equals(accNum)) {
					selected = a;
					break;
				}
			}
			if (selected == null) {
				ConsoleUtil.printError("Account unauthorized or not found.");
				return;
			}

			System.out.print(
					ConsoleUtil.B_CYAN + "  ➤ Number of Days History (Press Enter for 10 days) : " + ConsoleUtil.RESET);
			String dStr = sc.nextLine().trim();
			int days = dStr.isEmpty() ? 10 : Integer.parseInt(dStr);

			List<Transaction> list = transactionService.getTransactionsByAccount(customerId, selected.getAccountId(),
					days);
			if (list == null || list.isEmpty()) {
				ConsoleUtil.printError("No transactions recorded in the last " + days + " days.");
				return;
			}

			System.out.println("\n" + ConsoleUtil.B_CYAN + "═".repeat(120) + ConsoleUtil.RESET);
			System.out.printf(ConsoleUtil.BOLD + "%-18s %-16s %-10s %-14s %-14s %-10s %-25s%n" + ConsoleUtil.RESET,
					"Reference ID", "Type", "Amount", "Bal Before", "Bal After", "Status", "Date & Time");
			System.out.println(ConsoleUtil.B_CYAN + "─".repeat(120) + ConsoleUtil.RESET);

			for (Transaction t : list) {
				String color = "DEPOSIT".equals(t.getTransactionType())
						|| "TRANSFER_CREDIT".equals(t.getTransactionType())
						|| "ACCOUNT_OPENING".equals(t.getTransactionType()) ? ConsoleUtil.B_GREEN : ConsoleUtil.RED;

				System.out.printf("%-18s %-16s " + color + "₹%-9s " + ConsoleUtil.RESET + "₹%-13s ₹%-13s %-10s %-25s%n",
						t.getReferenceNumber(), t.getTransactionType(), t.getAmount(), t.getBalanceBefore(),
						t.getBalanceAfter(), t.getStatus(), t.getCreatedAt());
			}
			System.out.println(ConsoleUtil.B_CYAN + "═".repeat(120) + ConsoleUtil.RESET);

		} catch (Exception e) {
			ConsoleUtil.printError(e.getMessage());
		}
	}
}
