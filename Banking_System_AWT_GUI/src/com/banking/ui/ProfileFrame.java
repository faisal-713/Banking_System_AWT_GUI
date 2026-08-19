package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.CustomerDAO;
import com.banking.exception.InvalidInputException;
import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.util.PasswordUtil;

import java.awt.*;
import java.awt.event.*;

public class ProfileFrame extends Frame {

    private final DashboardFrame parent;
    private Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField addressField;
    private boolean editable = false;
    private Button editSaveBtn;

    public ProfileFrame(DashboardFrame parent, Account account) {
        super("Banking System - Profile");
        this.parent = parent;
        this.account = account;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("PROFILE / SETTINGS"), BorderLayout.NORTH);

        Panel content = new Panel(new GridLayout(1, 3, 14, 14));
        content.setBackground(UIStyle.LIGHT_BG);

        content.add(buildProfileCard());
        content.add(buildSettingsCard());
        content.add(buildHelpCard());

        add(content, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(1020, 560);
        setLocationRelativeTo(parent);
    }

    private Panel buildProfileCard() {
        Panel card = new Panel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);

        Label title = new Label("Profile Information", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(UIStyle.PRIMARY_DARK);
        card.add(title, BorderLayout.NORTH);

        Panel form = new Panel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        form.add(UIStyle.label("Account No:"), gc);
        gc = UIStyle.gbc(1, 0);
        Label accNoLbl = UIStyle.heading(String.valueOf(account.getAccountNo()), UIStyle.TEXT_DARK, 16);
        form.add(accNoLbl, gc);

        gc = UIStyle.gbc(0, 1);
        form.add(UIStyle.label("Full Name:"), gc);
        gc = UIStyle.gbc(1, 1);
        nameField = UIStyle.textField(16);
        nameField.setText(account.getHolderName());
        nameField.setEditable(false);
        form.add(nameField, gc);

        gc = UIStyle.gbc(0, 2);
        form.add(UIStyle.label("Email:"), gc);
        gc = UIStyle.gbc(1, 2);
        emailField = UIStyle.textField(16);
        emailField.setText(account.getEmail());
        emailField.setEditable(false);
        form.add(emailField, gc);

        gc = UIStyle.gbc(0, 3);
        form.add(UIStyle.label("Phone:"), gc);
        gc = UIStyle.gbc(1, 3);
        phoneField = UIStyle.textField(16);
        phoneField.setText(account.getPhone());
        phoneField.setEditable(false);
        form.add(phoneField, gc);

        gc = UIStyle.gbc(0, 4);
        form.add(UIStyle.label("Address:"), gc);
        gc = UIStyle.gbc(1, 4);
        addressField = UIStyle.textField(16);
        addressField.setEditable(false);
        form.add(addressField, gc);

        Customer customer = customerDAO.findById(account.getCustomerId());
        if (customer != null) {
            addressField.setText(customer.getAddress() == null ? "" : customer.getAddress());
        }

        editSaveBtn = UIStyle.purpleButton("EDIT PROFILE");
        gc = UIStyle.gbc(0, 5);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(16, 12, 8, 12);
        form.add(editSaveBtn, gc);

        card.add(form, BorderLayout.CENTER);

        editSaveBtn.addActionListener(e -> toggleEdit());

        return card;
    }

    private void toggleEdit() {
        if (!editable) {
            editable = true;
            nameField.setEditable(true);
            emailField.setEditable(true);
            phoneField.setEditable(true);
            addressField.setEditable(true);
            editSaveBtn.setLabel("SAVE PROFILE");
        } else {
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    throw new InvalidInputException("Name, Email and Phone cannot be empty.");
                }
                if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
                    throw new InvalidInputException("Please enter a valid Email.");
                }
                customerDAO.updateProfile(account.getCustomerId(), name, email, phone, address);
                UIStyle.showSuccess(this, "Profile updated successfully.");
                editable = false;
                nameField.setEditable(false);
                emailField.setEditable(false);
                phoneField.setEditable(false);
                addressField.setEditable(false);
                editSaveBtn.setLabel("EDIT PROFILE");
                parent.loadData();
            } catch (InvalidInputException ex) {
                UIStyle.showError(this, ex.getMessage());
            } catch (RuntimeException ex) {
                UIStyle.showError(this, "Failed to update profile. (" + ex.getMessage() + ")");
            }
        }
    }

    private Panel buildSettingsCard() {
        Panel card = new Panel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);

        Label title = new Label("Settings", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(UIStyle.PRIMARY_DARK);
        card.add(title, BorderLayout.NORTH);

        Panel buttons = new Panel(new GridLayout(4, 1, 10, 10));
        buttons.setBackground(Color.WHITE);

        Button changePinBtn = UIStyle.primaryButton("CHANGE PIN");
        Button changePasswordBtn = UIStyle.purpleButton("CHANGE PASSWORD");
        Button notificationsBtn = UIStyle.neutralButton("NOTIFICATION SETTINGS");
        Button securityBtn = UIStyle.neutralButton("SECURITY SETTINGS");

        buttons.add(changePinBtn);
        buttons.add(changePasswordBtn);
        buttons.add(notificationsBtn);
        buttons.add(securityBtn);

        Panel wrapper = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(buttons);
        card.add(wrapper, BorderLayout.CENTER);

        changePinBtn.addActionListener(e -> showChangePinDialog());
        changePasswordBtn.addActionListener(e -> showChangePasswordDialog());
        notificationsBtn.addActionListener(e -> UIStyle.showInfo(this, "Notification Settings",
                "SMS and Email notifications are always enabled.", UIStyle.PRIMARY_DARK));
        securityBtn.addActionListener(e -> UIStyle.showInfo(this, "Security Settings",
                "Your account is protected with SHA-256 encryption.", UIStyle.PRIMARY_DARK));

        return card;
    }

    private void showChangePinDialog() {
        Dialog dialog = new Dialog(this, "Change PIN", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        dialog.add(UIStyle.label("Current PIN:"), gc);
        TextField currentPin = UIStyle.pinField(14);
        gc = UIStyle.gbc(1, 0);
        dialog.add(currentPin, gc);

        gc = UIStyle.gbc(0, 1);
        dialog.add(UIStyle.label("New PIN (4 digits):"), gc);
        TextField newPin = UIStyle.pinField(14);
        gc = UIStyle.gbc(1, 1);
        dialog.add(newPin, gc);

        gc = UIStyle.gbc(0, 2);
        dialog.add(UIStyle.label("Confirm New PIN:"), gc);
        TextField confirmPin = UIStyle.pinField(14);
        gc = UIStyle.gbc(1, 2);
        dialog.add(confirmPin, gc);

        Button saveBtn = UIStyle.successButton("UPDATE PIN");
        gc = UIStyle.gbc(0, 3);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        dialog.add(saveBtn, gc);

        saveBtn.addActionListener(e -> {
            if (!accountDAO.verifyPin(account.getAccountNo(), currentPin.getText().trim())) {
                UIStyle.showError(dialog, "Current PIN is incorrect.");
                return;
            }
            String np = newPin.getText().trim();
            if (!np.matches("^\\d{4}$")) {
                UIStyle.showError(dialog, "New PIN must be a 4-digit number.");
                return;
            }
            if (!np.equals(confirmPin.getText().trim())) {
                UIStyle.showError(dialog, "New PIN and Confirm PIN do not match.");
                return;
            }
            accountDAO.updatePin(account.getAccountNo(), PasswordUtil.hash(np));
            UIStyle.showSuccess(dialog, "PIN changed successfully.");
            dialog.dispose();
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(this, "Change Password", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        dialog.add(UIStyle.label("New Password:"), gc);
        TextField newPassword = UIStyle.pinField(14);
        gc = UIStyle.gbc(1, 0);
        dialog.add(newPassword, gc);

        gc = UIStyle.gbc(0, 1);
        dialog.add(UIStyle.label("Confirm Password:"), gc);
        TextField confirmPassword = UIStyle.pinField(14);
        gc = UIStyle.gbc(1, 1);
        dialog.add(confirmPassword, gc);

        Button saveBtn = UIStyle.successButton("UPDATE PASSWORD");
        gc = UIStyle.gbc(0, 2);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        dialog.add(saveBtn, gc);

        saveBtn.addActionListener(e -> {
            String np = newPassword.getText().trim();
            if (np.length() < 4) {
                UIStyle.showError(dialog, "Password must be at least 4 characters long.");
                return;
            }
            if (!np.equals(confirmPassword.getText().trim())) {
                UIStyle.showError(dialog, "Password and Confirm Password do not match.");
                return;
            }
            customerDAO.updatePassword(account.getCustomerId(), PasswordUtil.hash(np));
            UIStyle.showSuccess(dialog, "Password changed successfully.");
            dialog.dispose();
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Panel buildHelpCard() {
        Panel card = new Panel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);

        Label title = new Label("Logout", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(UIStyle.PRIMARY_DARK);
        card.add(title, BorderLayout.NORTH);

        Panel top = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        top.setBackground(Color.WHITE);
        Button logoutBtn = UIStyle.dangerButton("LOGOUT");
        top.add(logoutBtn);
        logoutBtn.addActionListener(e -> {
            dispose();
            parent.dispose();
            new LoginFrame().setVisible(true);
        });

        Panel bottom = new Panel(new BorderLayout(6, 6));
        bottom.setBackground(Color.WHITE);
        Label helpTitle = new Label("Need Help?");
        helpTitle.setFont(UIStyle.FONT_LABEL);
        helpTitle.setForeground(UIStyle.PURPLE);
        Label supportInfo = new Label("<html>Contact our support:<br>support@bank.com<br>01234-567890</html>");
        // AWT Label doesn't support HTML - use TextArea instead
        TextArea supportArea = new TextArea(
                "Contact our support:\n\u2022 support@bank.com\n\u2022 01234-567890",
                4, 20, TextArea.SCROLLBARS_NONE);
        supportArea.setEditable(false);
        supportArea.setFont(UIStyle.FONT_SMALL);
        supportArea.setBackground(UIStyle.LIGHT_BG);

        Panel helpPanel = new Panel(new BorderLayout(4, 4));
        helpPanel.setBackground(Color.WHITE);
        helpPanel.add(helpTitle, BorderLayout.NORTH);
        helpPanel.add(supportArea, BorderLayout.CENTER);

        bottom.add(helpPanel, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);

        return card;
    }
}
