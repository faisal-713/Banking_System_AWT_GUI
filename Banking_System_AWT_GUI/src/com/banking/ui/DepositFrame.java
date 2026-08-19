package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.InvalidAmountException;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class DepositFrame extends Frame {

    private final DashboardFrame parent;
    private final Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private TextField amountField;
    private Choice methodChoice;
    private TextField noteField;

    public DepositFrame(DashboardFrame parent, Account account) {
        super("Banking System - Deposit");
        this.parent = parent;
        this.account = account;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("DEPOSIT MONEY"), BorderLayout.NORTH);

        Panel form = new Panel(new GridBagLayout());
        form.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        form.add(UIStyle.label("Account Number:"), gc);
        gc = UIStyle.gbc(1, 0);
        TextField accNoField = UIStyle.textField(18);
        accNoField.setText(String.valueOf(account.getAccountNo()));
        accNoField.setEditable(false);
        form.add(accNoField, gc);

        gc = UIStyle.gbc(0, 1);
        form.add(UIStyle.label("Account Holder:"), gc);
        gc = UIStyle.gbc(1, 1);
        TextField holderField = UIStyle.textField(18);
        holderField.setText(account.getHolderName());
        holderField.setEditable(false);
        form.add(holderField, gc);

        gc = UIStyle.gbc(0, 2);
        form.add(UIStyle.label("Amount (৳):"), gc);
        gc = UIStyle.gbc(1, 2);
        amountField = UIStyle.textField(18);
        form.add(amountField, gc);

        gc = UIStyle.gbc(0, 3);
        form.add(UIStyle.label("Payment Method:"), gc);
        gc = UIStyle.gbc(1, 3);
        methodChoice = UIStyle.choice("Cash", "Cheque", "Mobile Banking", "Bank Transfer");
        methodChoice.setPreferredSize(new Dimension(280, 42));
        form.add(methodChoice, gc);

        gc = UIStyle.gbc(0, 4);
        form.add(UIStyle.label("Note (Optional):"), gc);
        gc = UIStyle.gbc(1, 4);
        noteField = UIStyle.textField(18);
        form.add(noteField, gc);

        Button depositBtn = UIStyle.successButton("DEPOSIT");
        gc = UIStyle.gbc(0, 5);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(20, 12, 8, 12);
        form.add(depositBtn, gc);

        Label note = new Label("Note: Your money is safe with us.", Label.CENTER);
        note.setFont(UIStyle.FONT_SMALL);
        note.setForeground(UIStyle.SUCCESS);
        gc = UIStyle.gbc(0, 6);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(note, gc);

        add(form, BorderLayout.CENTER);

        depositBtn.addActionListener(e -> doDeposit());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(560, 520);
        setLocationRelativeTo(parent);
    }

    private void doDeposit() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            BigDecimal newBalance = accountDAO.deposit(account.getAccountNo(), amount);
            String desc = "Deposit (" + methodChoice.getSelectedItem() + ")"
                    + (noteField.getText().trim().isEmpty() ? "" : " - " + noteField.getText().trim());
            transactionDAO.insert(account.getAccountNo(), Transaction.DEPOSIT, amount, newBalance, desc, null);

            UIStyle.showSuccess(this, "৳ " + String.format("%,.2f", amount) + " has been deposited successfully.\nNew Balance: ৳ " + String.format("%,.2f", newBalance));
            parent.loadData();
            dispose();
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Please enter a valid Amount.");
        } catch (InvalidAmountException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UIStyle.showError(this, "Failed to deposit. (" + ex.getMessage() + ")");
        }
    }
}
