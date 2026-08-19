package com.banking.ui;

import com.banking.dao.TransactionDAO;
import com.banking.exception.NoDataFoundException;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class TransactionHistoryFrame extends Frame {

    private final Account account;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private java.awt.List list;
    private List<Transaction> transactions;

    public TransactionHistoryFrame(DashboardFrame parent, Account account) {
        super("Banking System - Transaction History");
        this.account = account;

        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);
        add(UIStyle.titleBar("TRANSACTION HISTORY"), BorderLayout.NORTH);

        Panel content = new Panel(new BorderLayout(10, 10));
        content.setBackground(UIStyle.LIGHT_BG);

        Label header = new Label("Account No: " + account.getAccountNo() + "   |   " + account.getHolderName(), Label.CENTER);
        header.setFont(UIStyle.FONT_SECTION);
        header.setForeground(UIStyle.PRIMARY_DARK);
        content.add(header, BorderLayout.NORTH);

        list = new java.awt.List(20);
        list.setFont(new Font("Consolas", Font.PLAIN, 15));
        content.add(list, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(UIStyle.LIGHT_BG);
        Button exportBtn = UIStyle.primaryButton("EXPORT STATEMENT");
        Button closeBtn = UIStyle.neutralButton("CLOSE");
        bottom.add(exportBtn);
        bottom.add(closeBtn);
        content.add(bottom, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        exportBtn.addActionListener(e -> exportStatement());
        closeBtn.addActionListener(e -> dispose());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        loadHistory();

        setSize(900, 640);
        setLocationRelativeTo(parent);
    }

    private void loadHistory() {
        list.removeAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        try {
            transactions = transactionDAO.getAll(account.getAccountNo());
            list.add(String.format("%-16s %-24s %-12s %-14s %-14s", "Date", "Description", "Type", "Amount", "Balance"));
            for (Transaction t : transactions) {
                String sign = Transaction.DEPOSIT.equals(t.getType()) || Transaction.TRANSFER_IN.equals(t.getType()) ? "+" : "-";
                String row = String.format("%-16s %-24s %-12s %s৳%-13s ৳%s",
                        sdf.format(t.getTransactionDate()),
                        truncate(t.getDescription() == null ? t.getType() : t.getDescription(), 24),
                        t.getType(), sign,
                        String.format("%,.2f", t.getAmount()),
                        String.format("%,.2f", t.getBalanceAfter()));
                list.add(row);
            }
        } catch (NoDataFoundException ex) {
            list.add("  " + ex.getMessage());
        }
    }

    private String truncate(String s, int len) {
        return s.length() > len ? s.substring(0, len - 1) + "…" : s;
    }

    private void exportStatement() {
        if (transactions == null || transactions.isEmpty()) {
            UIStyle.showError(this, "There are no transactions to export.");
            return;
        }
        FileDialog fd = new FileDialog(this, "Save Statement", FileDialog.SAVE);
        fd.setFile("statement_" + account.getAccountNo() + ".csv");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (dir == null || file == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        try (PrintWriter pw = new PrintWriter(new FileWriter(dir + file))) {
            pw.println("Date,Description,Type,Amount,BalanceAfter");
            for (Transaction t : transactions) {
                pw.printf("%s,%s,%s,%s,%s%n",
                        sdf.format(t.getTransactionDate()),
                        (t.getDescription() == null ? t.getType() : t.getDescription()).replace(",", " "),
                        t.getType(), t.getAmount(), t.getBalanceAfter());
            }
            UIStyle.showSuccess(this, "Statement saved successfully:\n" + dir + file);
        } catch (IOException ex) {
            UIStyle.showError(this, "Failed to export: " + ex.getMessage());
        }
    }
}
