package com.banking.dao;

import com.banking.db.DBConnection;
import com.banking.exception.*;
import com.banking.model.Account;
import com.banking.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.*;

public class AccountDAO {


    public long createAccount(long customerId, String accountType, BigDecimal initialDeposit, String pinHash) {
        String sql = "INSERT INTO account (customer_id, account_type, balance, pin_hash) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, customerId);
            ps.setString(2, accountType);
            ps.setBigDecimal(3, initialDeposit);
            ps.setString(4, pinHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Could not generate Account Number.");
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to create account", e);
        }
    }


    public Account login(long accountNo, String pin) throws InvalidCredentialException {
        String sql = "SELECT a.*, c.name AS holder_name, c.email, c.phone " +
                "FROM account a JOIN customer c ON a.customer_id = c.customer_id " +
                "WHERE a.account_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new InvalidCredentialException("Invalid Account Number or PIN. Please try again.");
                }
                String storedHash = rs.getString("pin_hash");
                if (!PasswordUtil.matches(pin, storedHash)) {
                    throw new InvalidCredentialException("Invalid Account Number or PIN. Please try again.");
                }
                if (!"ACTIVE".equalsIgnoreCase(rs.getString("status"))) {
                    throw new InvalidCredentialException("This account is currently not active.");
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to login", e);
        }
    }

    public Account findByAccountNo(long accountNo) {
        String sql = "SELECT a.*, c.name AS holder_name, c.email, c.phone " +
                "FROM account a JOIN customer c ON a.customer_id = c.customer_id " +
                "WHERE a.account_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find account", e);
        }
    }

    public boolean accountExists(long accountNo) {
        return findByAccountNo(accountNo) != null;
    }

    public BigDecimal deposit(long accountNo, BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Please enter a valid amount greater than zero.");
        }
        String sql = "UPDATE account SET balance = balance + ? WHERE account_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, accountNo);
            ps.executeUpdate();
            return getBalance(con, accountNo);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deposit", e);
        }
    }

    /** Withdraw: checks balance, decreases it and returns the new balance */
    public BigDecimal withdraw(long accountNo, BigDecimal amount)
            throws InvalidAmountException, InsufficientBalanceException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Please enter a valid amount greater than zero.");
        }
        try (Connection con = DBConnection.getConnection()) {
            BigDecimal current = getBalance(con, accountNo);
            if (current.compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance. Transaction cannot be completed.");
            }
            String sql = "UPDATE account SET balance = balance - ? WHERE account_no = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBigDecimal(1, amount);
                ps.setLong(2, accountNo);
                ps.executeUpdate();
            }
            return getBalance(con, accountNo);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to withdraw", e);
        }
    }

    public BigDecimal transfer(long fromAccountNo, long toAccountNo, BigDecimal amount)
            throws InvalidAmountException, InsufficientBalanceException, InvalidTransferException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Please enter a valid amount greater than zero.");
        }
        if (fromAccountNo == toAccountNo) {
            throw new InvalidTransferException("You cannot transfer to your own account.");
        }
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            if (!accountExistsInTx(con, toAccountNo)) {
                throw new InvalidTransferException("Transfer failed. Please check the receiver account number.");
            }
            BigDecimal current = getBalance(con, fromAccountNo);
            if (current.compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance. Transaction cannot be completed.");
            }
            try (PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE account SET balance = balance - ? WHERE account_no = ?")) {
                ps1.setBigDecimal(1, amount);
                ps1.setLong(2, fromAccountNo);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE account SET balance = balance + ? WHERE account_no = ?")) {
                ps2.setBigDecimal(1, amount);
                ps2.setLong(2, toAccountNo);
                ps2.executeUpdate();
            }
            BigDecimal newBalance = getBalance(con, fromAccountNo);
            con.commit();
            return newBalance;
        } catch (InsufficientBalanceException | InvalidTransferException e) {
            rollbackQuietly(con);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(con);
            throw new DatabaseOperationException("Failed to transfer", e);
        } finally {
            closeQuietly(con);
        }
    }

    public void updatePin(long accountNo, String newPinHash) {
        String sql = "UPDATE account SET pin_hash = ? WHERE account_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPinHash);
            ps.setLong(2, accountNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update PIN", e);
        }
    }

    public boolean verifyPin(long accountNo, String pin) {
        String sql = "SELECT pin_hash FROM account WHERE account_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PasswordUtil.matches(pin, rs.getString("pin_hash"));
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to verify PIN", e);
        }
    }

    private BigDecimal getBalance(Connection con, long accountNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT balance FROM account WHERE account_no = ?")) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                throw new SQLException("Account not found: " + accountNo);
            }
        }
    }

    private boolean accountExistsInTx(Connection con, long accountNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT account_no FROM account WHERE account_no = ? AND status='ACTIVE'")) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void rollbackQuietly(Connection con) {
        try {
            if (con != null) con.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException ignored) {
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountNo(rs.getLong("account_no"));
        a.setCustomerId(rs.getLong("customer_id"));
        a.setAccountType(rs.getString("account_type"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setStatus(rs.getString("status"));
        a.setOpenDate(rs.getDate("open_date"));
        a.setBranch(rs.getString("branch"));
        a.setHolderName(rs.getString("holder_name"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        return a;
    }
}
