package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.exception.InvalidCredentialException;
import com.banking.model.Account;

import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends Frame {

    private final TextField accNoField;
    private final TextField pinField;
    private final Checkbox rememberMe;
    private final AccountDAO accountDAO = new AccountDAO();

    public LoginFrame() {
        super("Banking System - Login");
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);

        add(UIStyle.titleBar("BANKING SYSTEM"), BorderLayout.NORTH);

        Panel center = new Panel(new GridBagLayout());
        center.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        Label subtitle = UIStyle.heading("Secure   •   Simple   •   Reliable", UIStyle.PRIMARY, 16);
        gc = UIStyle.gbc(0, 0);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        center.add(subtitle, gc);

        Label loginHeading = UIStyle.heading("Login to Your Account", UIStyle.TEXT_DARK, 20);
        gc = UIStyle.gbc(0, 1);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(6, 12, 22, 12);
        center.add(loginHeading, gc);

        Label accLbl = UIStyle.label("Account Number:");
        gc = UIStyle.gbc(0, 2);
        center.add(accLbl, gc);

        accNoField = UIStyle.textField(18);
        gc = UIStyle.gbc(1, 2);
        center.add(accNoField, gc);

        Label pinLbl = UIStyle.label("PIN:");
        gc = UIStyle.gbc(0, 3);
        center.add(pinLbl, gc);

        pinField = UIStyle.pinField(18);
        gc = UIStyle.gbc(1, 3);
        center.add(pinField, gc);

        rememberMe = new Checkbox("Remember Me", true);
        rememberMe.setFont(UIStyle.FONT_SMALL);
        gc = UIStyle.gbc(0, 4);
        center.add(rememberMe, gc);

        Button loginBtn = UIStyle.primaryButton("LOGIN");
        Button clearBtn = UIStyle.neutralButton("CLEAR");
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(UIStyle.LIGHT_BG);
        btnPanel.add(loginBtn);
        btnPanel.add(clearBtn);
        gc = UIStyle.gbc(0, 5);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        center.add(btnPanel, gc);

        Panel registerPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        registerPanel.setBackground(UIStyle.LIGHT_BG);
        Label noAcc = UIStyle.label("Don't have an account?");
        noAcc.setFont(UIStyle.FONT_SMALL);
        Button registerLink = UIStyle.linkButton("Register Now");
        registerPanel.add(noAcc);
        registerPanel.add(registerLink);
        gc = UIStyle.gbc(0, 6);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        center.add(registerPanel, gc);

        add(center, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> doLogin());
        pinField.addActionListener(e -> doLogin());
        clearBtn.addActionListener(e -> {
            accNoField.setText("");
            pinField.setText("");
        });
        registerLink.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setSize(560, 520);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void doLogin() {
        String accText = accNoField.getText().trim();
        String pin = pinField.getText().trim();

        if (accText.isEmpty() || pin.isEmpty()) {
            UIStyle.showError(this, "Please enter both Account Number and PIN.");
            return;
        }
        long accNo;
        try {
            accNo = Long.parseLong(accText);
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Account Number must be numeric.");
            return;
        }
        try {
            Account account = accountDAO.login(accNo, pin);
            dispose();
            new DashboardFrame(account).setVisible(true);
        } catch (InvalidCredentialException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UIStyle.showError(this, "The system is currently unavailable. Please try again later.\n(" + ex.getMessage() + ")");
        }
    }
}
