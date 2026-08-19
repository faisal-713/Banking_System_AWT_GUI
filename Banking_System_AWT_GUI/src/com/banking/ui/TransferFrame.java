package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class TransferFrame extends Frame {

    private final DashboardFrame parent;
    private final Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private TextField toAccountField;
    private TextField holderNameField;
    private TextField amountField;
    private TextField noteField;

    public TransferFrame(DashboardFrame parent, Account account) {
        super("Banking System - Transfer");
        this.parent = parent;
        this.account = account;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("TRANSFER MONEY"), BorderLayout.NORTH);

        Panel form = new Panel(new GridBagLayout());
        form.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        form.add(UIStyle.label("From Account:"), gc);
        gc = UIStyle.gbc(1, 0);
        TextField fromField = UIStyle.textField(18);
        fromField.setText(account.getAccountNo() + " - " + account.getAccountType());
        fromField.setEditable(false);
        form.add(fromField, gc);

        gc = UIStyle.gbc(0, 1);
        form.add(UIStyle.label("To Account Number:"), gc);
        gc = UIStyle.gbc(1, 1);
        toAccountField = UIStyle.textField(18);
        form.add(toAccountField, gc);

        gc = UIStyle.gbc(0, 2);
        form.add(UIStyle.label("Account Holder Name:"), gc);
        gc = UIStyle.gbc(1, 2);
        holderNameField = UIStyle.textField(18);
        holderNameField.setEditable(false);
        form.add(holderNameField, gc);

        Button checkBtn = UIStyle.linkButton("Verify Account »");
        gc = UIStyle.gbc(1, 3);
        gc.anchor = GridBagConstraints.EAST;
        form.add(checkBtn, gc);

        gc = UIStyle.gbc(0, 4);
        form.add(UIStyle.label("Amount (৳):"), gc);
        gc = UIStyle.gbc(1, 4);
        amountField = UIStyle.textField(18);
        form.add(amountField, gc);

        gc = UIStyle.gbc(0, 5);
        form.add(UIStyle.label("Note (Optional):"), gc);
        gc = UIStyle.gbc(1, 5);
        noteField = UIStyle.textField(18);
        form.add(noteField, gc);

        Button transferBtn = UIStyle.purpleButton("TRANSFER");
        gc = UIStyle.gbc(0, 6);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(20, 12, 8, 12);
        form.add(transferBtn, gc);

        Label note = new Label("Make sure all information is correct before confirming.", Label.CENTER);
        note.setFont(UIStyle.FONT_SMALL);
        note.setForeground(UIStyle.PURPLE);
        gc = UIStyle.gbc(0, 7);
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(note, gc);

        add(form, BorderLayout.CENTER);

        checkBtn.addActionListener(e -> verifyToAccount());
        transferBtn.addActionListener(e -> doTransfer());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(600, 600);
        setLocationRelativeTo(parent);
    }

    private void verifyToAccount() {
        try {
            long toAccNo = Long.parseLong(toAccountField.getText().trim());
            Account toAccount = accountDAO.findByAccountNo(toAccNo);
            if (toAccount == null) {
                holderNameField.setText("");
                UIStyle.showError(this, "No account was found with this Account Number.");
            } else {
                holderNameField.setText(toAccount.getHolderName());
            }
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Please enter a valid Account Number.");
        }
    }

    private void doTransfer() {
        try {
            long toAccNo = Long.parseLong(toAccountField.getText().trim());
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            BigDecimal newBalance = accountDAO.transfer(account.getAccountNo(), toAccNo, amount);

            String note = noteField.getText().trim();
            String descOut = "Transfer to " + toAccNo + (note.isEmpty() ? "" : " - " + note);
            String descIn = "Transfer from " + account.getAccountNo() + (note.isEmpty() ? "" : " - " + note);

            transactionDAO.insert(account.getAccountNo(), Transaction.TRANSFER, amount, newBalance, descOut, toAccNo);
            Account toAccount = accountDAO.findByAccountNo(toAccNo);
            transactionDAO.insert(toAccNo, Transaction.TRANSFER_IN, amount, toAccount.getBalance(), descIn, account.getAccountNo());

            UIStyle.showSuccess(this, "৳ " + String.format("%,.2f", amount) + " has been transferred successfully.\nNew Balance: ৳ " + String.format("%,.2f", newBalance));
            parent.loadData();
            dispose();
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Please enter a valid Account Number and Amount.");
        } catch (InvalidAmountException | InsufficientBalanceException | InvalidTransferException ex) {
            UIStyle.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UIStyle.showError(this, "Failed to transfer. (" + ex.getMessage() + ")");
        }
    }
}
