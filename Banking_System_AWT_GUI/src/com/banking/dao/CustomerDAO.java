package com.banking.dao;

import com.banking.db.DBConnection;
import com.banking.exception.DatabaseOperationException;
import com.banking.exception.DuplicateAccountException;
import com.banking.model.Customer;

import java.sql.*;

public class CustomerDAO {

    public boolean existsByEmailOrPhone(String email, String phone) {
        String sql = "SELECT customer_id FROM customer WHERE email = ? OR phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check existing customer", e);
        }
    }

    public long createCustomer(String name, String email, String phone, String address, String passwordHash)
            throws DuplicateAccountException {
        if (existsByEmailOrPhone(email, phone)) {
            throw new DuplicateAccountException("An account with this Email or Phone already exists.");
        }
        String sql = "INSERT INTO customer (name, email, phone, address, password_hash) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Could not generate Customer ID.");
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to create customer", e);
        }
    }

    public Customer findById(long customerId) {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find customer", e);
        }
    }

    public void updateProfile(long customerId, String name, String email, String phone, String address) {
        String sql = "UPDATE customer SET name = ?, email = ?, phone = ?, address = ? WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setLong(5, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update profile", e);
        }
    }

    public void updatePassword(long customerId, String newPasswordHash) {
        String sql = "UPDATE customer SET password_hash = ? WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update password", e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        return c;
    }
}
