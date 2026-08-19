package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.model.Account;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

public class BalanceFrame extends Frame {

    public BalanceFrame(DashboardFrame parent, Account inputAccount) {
        super("Banking System - Balance");
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.findByAccountNo(inputAccount.getAccountNo());

        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("ACCOUNT BALANCE"), BorderLayout.NORTH);

        Panel content = new Panel(new GridBagLayout());
        content.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        Panel balanceCard = new Panel(new GridLayout(2, 1, 4, 4));
        balanceCard.setBackground(UIStyle.PRIMARY);
        balanceCard.setPreferredSize(new Dimension(420, 140));
        Label title = new Label("Current Balance", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(Color.WHITE);
        Label value = new Label("৳ " + String.format("%,.2f", account.getBalance()), Label.CENTER);
        value.setFont(new Font("Segoe UI", Font.BOLD, 38));
        value.setForeground(UIStyle.GOLD);
        balanceCard.add(title);
        balanceCard.add(value);

        gc = UIStyle.gbc(0, 0);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        content.add(balanceCard, gc);

        Label detailsHeader = UIStyle.heading("Account Details", UIStyle.PURPLE, 19);
        gc = UIStyle.gbc(0, 1);
        gc.gridwidth = 2;
        gc.insets = new Insets(24, 12, 8, 12);
        content.add(detailsHeader, gc);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        addDetailRow(content, 2, "Account Number:", String.valueOf(account.getAccountNo()));
        addDetailRow(content, 3, "Account Holder:", account.getHolderName());
        addDetailRow(content, 4, "Account Type:", account.getAccountType());
        addDetailRow(content, 5, "Account Status:", account.getStatus());
        addDetailRow(content, 6, "Opening Date:", account.getOpenDate() != null ? sdf.format(account.getOpenDate()) : "-");
        addDetailRow(content, 7, "Branch:", account.getBranch());

        Label footer = new Label("Your account is active and in good standing.", Label.CENTER);
        footer.setFont(UIStyle.FONT_SMALL);
        footer.setForeground(UIStyle.SUCCESS);
        gc = UIStyle.gbc(0, 8);
        gc.gridwidth = 2;
        gc.insets = new Insets(24, 12, 8, 12);
        gc.anchor = GridBagConstraints.CENTER;
        content.add(footer, gc);

        add(content, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(600, 620);
        setLocationRelativeTo(parent);
    }

    private void addDetailRow(Panel content, int row, String labelText, String valueText) {
        GridBagConstraints gc = UIStyle.gbc(0, row);
        content.add(UIStyle.label(labelText), gc);
        gc = UIStyle.gbc(1, row);
        Label value = UIStyle.heading(valueText, UIStyle.TEXT_DARK, 17);
        content.add(value, gc);
    }
}
