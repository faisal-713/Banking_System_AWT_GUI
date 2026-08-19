package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class WithdrawFrame extends Frame {

    private final DashboardFrame parent;
    private Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private TextField amountField;
    private TextField noteField;
    private Label balanceLabel;

    public WithdrawFrame(DashboardFrame parent, Account account) {
        super("Banking System - Withdraw");
        this.parent = parent;
        this.account = account;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("WITHDRAW MONEY"), BorderLayout.NORTH);

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
        form.add(UIStyle.label("Available Balance:"), gc);
        gc = UIStyle.gbc(1, 2);
        balanceLabel = UIStyle.heading("৳ " + String.format("%,.2f", account.getBalance()), UIStyle.SUCCESS, 18);
        form.add(balanceLabel, gc);

        gc = UIStyle.gbc(0, 3);
        form.add(UIStyle.label("Amount (৳):"), gc);
        gc = UIStyle.gbc(1, 3);
        amountField = UIStyle.textField(18);
        form.add(amountField, gc);

        gc = UIStyle.gbc(0, 4);
        form.add(UIStyle.label("Note (Optional):"), gc);
        gc = UIStyle.gbc(1, 4);
        noteField = UIStyle.textField(18);
        form.add(noteField, gc);

        Button withdrawBtn = UIStyle.dangerButton("WITHDRAW");
        gc = UIStyle.gbc(0, 5);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(20, 12, 8, 12);
        form.add(withdrawBtn, gc);

        Label note = new Label("Please check your balance before making a withdrawal.", Label.CENTER);
        note.setFont(UIStyle.FONT_SMALL);
        note.setForeground(UIStyle.DANGER);
        gc = UIStyle.gbc(0, 6);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(note, gc);

        add(form, BorderLayout.CENTER);

        withdrawBtn.addActionListener(e -> doWithdraw());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(560, 500);
        setLocationRelativeTo(parent);
    }

    private void doWithdraw() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            BigDecimal newBalance = accountDAO.withdraw(account.getAccountNo(), amount);
            String desc = "Withdrawal" + (noteField.getText().trim().isEmpty() ? "" : " - " + noteField.getText().trim());
            transactionDAO.insert(account.getAccountNo(), Transaction.WITHDRAW, amount, newBalance, desc, null);

            UIStyle.showSuccess(this, "৳ " + String.format("%,.2f", amount) + " has been withdrawn successfully.\nNew Balance: ৳ " + String.format("%,.2f", newBalance));
            parent.loadData();
            dispose();
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Please enter a valid Amount.");
        } catch (InvalidAmountException | InsufficientBalanceException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UIStyle.showError(this, "Failed to withdraw. (" + ex.getMessage() + ")");
        }
    }
}
