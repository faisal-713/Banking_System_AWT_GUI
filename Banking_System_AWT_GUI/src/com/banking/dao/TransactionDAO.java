package com.banking.dao;

import com.banking.db.DBConnection;
import com.banking.exception.DatabaseOperationException;
import com.banking.exception.NoDataFoundException;
import com.banking.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(long accountNo, String type, BigDecimal amount, BigDecimal balanceAfter,
                        String description, Long toAccountNo) {
        String sql = "INSERT INTO transactions (account_no, type, amount, balance_after, description, to_account_no) " +
                "VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setBigDecimal(4, balanceAfter);
            ps.setString(5, description);
            if (toAccountNo != null) {
                ps.setLong(6, toAccountNo);
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save transaction", e);
        }
    }

    public List<Transaction> getRecent(long accountNo, int limit) {
        String sql = "SELECT * FROM transactions WHERE account_no = ? ORDER BY transaction_date DESC, transaction_id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load transaction history", e);
        }
    }

    public List<Transaction> getAll(long accountNo) throws NoDataFoundException {
        String sql = "SELECT * FROM transactions WHERE account_no = ? ORDER BY transaction_date DESC, transaction_id DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            if (list.isEmpty()) {
                throw new NoDataFoundException("No transactions found for the selected period.");
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load transaction history", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getLong("transaction_id"));
        t.setAccountNo(rs.getLong("account_no"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setBalanceAfter(rs.getBigDecimal("balance_after"));
        t.setDescription(rs.getString("description"));
        long to = rs.getLong("to_account_no");
        t.setToAccountNo(rs.wasNull() ? null : to);
        t.setTransactionDate(rs.getTimestamp("transaction_date"));
        return t;
    }
}
