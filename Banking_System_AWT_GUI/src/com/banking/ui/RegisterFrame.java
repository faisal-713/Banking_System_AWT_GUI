package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.CustomerDAO;
import com.banking.exception.DuplicateAccountException;
import com.banking.exception.InvalidInputException;
import com.banking.model.Account;
import com.banking.util.PasswordUtil;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class RegisterFrame extends Frame {

    private final TextField nameField;
    private final TextField emailField;
    private final TextField phoneField;
    private final TextField addressField;
    private final Choice accountTypeChoice;
    private final TextField initialDepositField;
    private final TextField pinField;
    private final TextField confirmPinField;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public RegisterFrame() {
        super("Banking System - Register");
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);

        add(UIStyle.titleBar("CREATE NEW ACCOUNT"), BorderLayout.NORTH);

        Panel form = new Panel(new GridBagLayout());
        form.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        Label subtitle = UIStyle.heading("Fill in your details to get started", UIStyle.PRIMARY, 17);
        gc = UIStyle.gbc(0, 0);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(6, 12, 20, 12);
        form.add(subtitle, gc);

        int row = 1;

        Label personalHeader = UIStyle.heading("Personal Information", UIStyle.PURPLE, 18);
        gc = UIStyle.gbc(0, row++);
        gc.gridwidth = 2;
        form.add(personalHeader, gc);

        nameField = addRow(form, row++, "Full Name:");
        emailField = addRow(form, row++, "Email:");
        phoneField = addRow(form, row++, "Phone Number:");
        addressField = addRow(form, row++, "Address:");

        Label accHeader = UIStyle.heading("Account Information", UIStyle.PURPLE, 18);
        gc = UIStyle.gbc(0, row++);
        gc.gridwidth = 2;
        gc.insets = new Insets(20, 12, 10, 12);
        form.add(accHeader, gc);

        Label accTypeLbl = UIStyle.label("Account Type:");
        gc = UIStyle.gbc(0, row);
        form.add(accTypeLbl, gc);
        accountTypeChoice = UIStyle.choice("Savings Account", "Current Account", "Student Account");
        accountTypeChoice.setPreferredSize(new Dimension(280, 42));
        gc = UIStyle.gbc(1, row++);
        form.add(accountTypeChoice, gc);

        initialDepositField = addRow(form, row++, "Initial Deposit (৳):");
        pinField = addPinRow(form, row++, "PIN (4 digits):");
        confirmPinField = addPinRow(form, row++, "Confirm PIN:");

        Button registerBtn = UIStyle.purpleButton("REGISTER");
        Button clearBtn = UIStyle.neutralButton("CLEAR");
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        btnPanel.setBackground(UIStyle.LIGHT_BG);
        btnPanel.add(registerBtn);
        btnPanel.add(clearBtn);
        gc = UIStyle.gbc(0, row++);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(20, 12, 8, 12);
        form.add(btnPanel, gc);

        Panel loginLinkPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        loginLinkPanel.setBackground(UIStyle.LIGHT_BG);
        Label already = UIStyle.label("Already have an account?");
        already.setFont(UIStyle.FONT_SMALL);
        Button loginLink = UIStyle.linkButton("Login Here");
        loginLinkPanel.add(already);
        loginLinkPanel.add(loginLink);
        gc = UIStyle.gbc(0, row);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(loginLinkPanel, gc);

        ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scroll.setBackground(UIStyle.LIGHT_BG);
        scroll.add(form);
        add(scroll, BorderLayout.CENTER);

        // ---------------- Actions ----------------
        registerBtn.addActionListener(e -> doRegister());
        clearBtn.addActionListener(e -> clearForm());
        loginLink.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        setSize(680, 780);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private TextField addRow(Panel form, int row, String labelText) {
        GridBagConstraints gc = UIStyle.gbc(0, row);
        form.add(UIStyle.label(labelText), gc);
        TextField tf = UIStyle.textField(22);
        gc = UIStyle.gbc(1, row);
        form.add(tf, gc);
        return tf;
    }

    private TextField addPinRow(Panel form, int row, String labelText) {
        GridBagConstraints gc = UIStyle.gbc(0, row);
        form.add(UIStyle.label(labelText), gc);
        TextField tf = UIStyle.pinField(22);
        gc = UIStyle.gbc(1, row);
        form.add(tf, gc);
        return tf;
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        initialDepositField.setText("");
        pinField.setText("");
        confirmPinField.setText("");
        accountTypeChoice.select(0);
    }

    private void doRegister() {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String accountType = accountTypeChoice.getSelectedItem();
            String depositText = initialDepositField.getText().trim();
            String pin = pinField.getText().trim();
            String confirmPin = confirmPinField.getText().trim();

            validateInputs(name, email, phone, depositText, pin, confirmPin);

            BigDecimal initialDeposit = new BigDecimal(depositText);

            String passwordHash = PasswordUtil.hash(pin); // simple app: pin used to secure customer login too
            long customerId = customerDAO.createCustomer(name, email, phone, address, passwordHash);
            String pinHash = PasswordUtil.hash(pin);
            long accountNo = accountDAO.createAccount(customerId, accountType, initialDeposit, pinHash);

            UIStyle.showSuccess(this,
                    "Registration successful!\nYour Account Number: " + accountNo + "\nYou are now logged in.");

            Account account = accountDAO.findByAccountNo(accountNo);
            dispose();
            new DashboardFrame(account).setVisible(true);

        } catch (InvalidInputException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (DuplicateAccountException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Initial Deposit must be a valid number.");
        } catch (RuntimeException ex) {
            UIStyle.showError(this, "The system is currently unavailable. Please try again later.\n(" + ex.getMessage() + ")");
        }
    }

    private void validateInputs(String name, String email, String phone, String depositText,
                                 String pin, String confirmPin) throws InvalidInputException {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || depositText.isEmpty()
                || pin.isEmpty() || confirmPin.isEmpty()) {
            throw new InvalidInputException("Please fill in all required fields.");
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            throw new InvalidInputException("Please enter a valid Email Address.");
        }
        if (!phone.matches("^[0-9+\\-\\s]{7,20}$")) {
            throw new InvalidInputException("Please enter a valid Phone Number.");
        }
        if (!pin.matches("^\\d{4}$")) {
            throw new InvalidInputException("PIN must be a 4-digit number.");
        }
        if (!pin.equals(confirmPin)) {
            throw new InvalidInputException("PIN and Confirm PIN do not match.");
        }
    }
}
