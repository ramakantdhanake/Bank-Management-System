package com.bank.service;

import java.math.BigDecimal;
import java.util.List;

import com.bank.dto.TransactionResultDTO;
import com.bank.dto.TransferResultsDto;
import com.bank.model.Account;

public interface AccountService {

	Account openAccount(long customerId, String accountType, BigDecimal innitialDeposit);

	List<Account> getCustomerAccounts(long customerId);

	TransactionResultDTO deposit(long customerId, long accountId, BigDecimal amount, String transactionPin);

	TransactionResultDTO withdraw(long customerId, long accountId, BigDecimal amount, String transactionPin);

	TransferResultsDto transfer(long customerId, long senderAccountId, String receiverAccountNumber, BigDecimal amount,String transactionPin);

	boolean existsByCustomerIdAndAccountType(long customerId, String accountType);
}
