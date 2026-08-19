package com.banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String TRANSFER = "TRANSFER";
    public static final String TRANSFER_IN = "TRANSFER_IN";

    private long transactionId;
    private long accountNo;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private Long toAccountNo;
    private Timestamp transactionDate;

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getAccountNo() { return accountNo; }
    public void setAccountNo(long accountNo) { this.accountNo = accountNo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getToAccountNo() { return toAccountNo; }
    public void setToAccountNo(Long toAccountNo) { this.toAccountNo = toAccountNo; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }
}
