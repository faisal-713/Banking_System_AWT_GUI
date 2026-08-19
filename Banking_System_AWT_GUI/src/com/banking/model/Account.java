package com.banking.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Account {
    private long accountNo;
    private long customerId;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private Date openDate;
    private String branch;

    private String holderName;
    private String email;
    private String phone;

    public Account() {
    }

    public long getAccountNo() { return accountNo; }
    public void setAccountNo(long accountNo) { this.accountNo = accountNo; }

    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
