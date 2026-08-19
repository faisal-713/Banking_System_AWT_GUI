package com.banking.ui;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.model.Account;
import com.banking.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class DashboardFrame extends Frame {

    private Account account;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private Label balanceValueLabel;
    private List<Transaction> recentTransactions;
    private java.awt.List transactionList;

    public DashboardFrame(Account account) {
        super("Banking System - Dashboard");
        this.account = account;
        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BG);

        add(buildHeader(), BorderLayout.NORTH);

        Panel center = new Panel(new GridBagLayout());
        center.setBackground(UIStyle.LIGHT_BG);
        GridBagConstraints gc;

        gc = UIStyle.gbc(0, 0);
        gc.fill = GridBagConstraints.BOTH;
        center.add(buildBalanceCard(), gc);

        gc = UIStyle.gbc(1, 0);
        gc.fill = GridBagConstraints.BOTH;
        center.add(buildQuickActionsCard(), gc);

        gc = UIStyle.gbc(0, 1);
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        center.add(buildRecentTransactionsCard(), gc);

        add(center, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setSize(1000, 700);
        setLocationRelativeTo(null);
        loadData();
    }

    private Panel buildHeader() {
        Panel header = new Panel(new BorderLayout());
        header.setBackground(UIStyle.PRIMARY_DARK);
        header.setPreferredSize(new Dimension(100, 90));

        Panel left = new Panel(new GridLayout(2, 1));
        left.setBackground(UIStyle.PRIMARY_DARK);
        Label welcome = new Label("Welcome, " + account.getHolderName());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(Color.WHITE);
        Label info = new Label("Account No: " + account.getAccountNo() + "     |     Type: " + account.getAccountType());
        info.setFont(UIStyle.FONT_SUBTITLE);
        info.setForeground(UIStyle.LIGHT_BG);
        left.add(welcome);
        left.add(info);

        Panel leftWrap = new Panel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        leftWrap.setBackground(UIStyle.PRIMARY_DARK);
        leftWrap.add(left);

        Button logoutBtn = UIStyle.dangerButton("LOGOUT");
        Panel right = new Panel(new FlowLayout(FlowLayout.RIGHT, 20, 22));
        right.setBackground(UIStyle.PRIMARY_DARK);
        right.add(logoutBtn);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        header.add(leftWrap, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private Panel buildBalanceCard() {
        Panel card = new Panel(new BorderLayout(10, 10));
        card.setBackground(UIStyle.PRIMARY);
        card.setPreferredSize(new Dimension(380, 220));

        Label title = new Label("Account Balance", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(Color.WHITE);

        balanceValueLabel = new Label("৳ 0.00", Label.CENTER);
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        balanceValueLabel.setForeground(UIStyle.GOLD);

        Panel wrapper = new Panel(new GridLayout(2, 1, 5, 5));
        wrapper.setBackground(UIStyle.PRIMARY);
        wrapper.add(title);
        wrapper.add(balanceValueLabel);

        card.add(wrapper, BorderLayout.CENTER);
        return card;
    }

    private Panel buildQuickActionsCard() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(Color.WHITE);
        outer.setPreferredSize(new Dimension(380, 220));

        Label title = new Label("Quick Actions", Label.CENTER);
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(UIStyle.PRIMARY_DARK);
        outer.add(title, BorderLayout.NORTH);

        Panel grid = new Panel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(Color.WHITE);

        Button depositBtn = UIStyle.successButton("DEPOSIT");
        Button withdrawBtn = UIStyle.dangerButton("WITHDRAW");
        Button transferBtn = UIStyle.purpleButton("TRANSFER");
        Button balanceBtn = UIStyle.primaryButton("BALANCE");

        grid.add(depositBtn);
        grid.add(withdrawBtn);
        grid.add(transferBtn);
        grid.add(balanceBtn);
        outer.add(grid, BorderLayout.CENTER);

        depositBtn.addActionListener(e -> new DepositFrame(this, account).setVisible(true));
        withdrawBtn.addActionListener(e -> new WithdrawFrame(this, account).setVisible(true));
        transferBtn.addActionListener(e -> new TransferFrame(this, account).setVisible(true));
        balanceBtn.addActionListener(e -> new BalanceFrame(this, account).setVisible(true));

        return outer;
    }

    private Panel buildRecentTransactionsCard() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(Color.WHITE);

        Panel titleBar = new Panel(new BorderLayout());
        titleBar.setBackground(Color.WHITE);
        Label title = new Label("Recent Transactions");
        title.setFont(UIStyle.FONT_SECTION);
        title.setForeground(UIStyle.PRIMARY_DARK);
        titleBar.add(title, BorderLayout.WEST);

        Button viewAllBtn = UIStyle.linkButton("View All »");
        titleBar.add(viewAllBtn, BorderLayout.EAST);
        outer.add(titleBar, BorderLayout.NORTH);

        transactionList = new java.awt.List(8);
        transactionList.setFont(new Font("Consolas", Font.PLAIN, 15));
        outer.add(transactionList, BorderLayout.CENTER);

        Panel bottomBar = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomBar.setBackground(Color.WHITE);
        Button profileBtn = UIStyle.neutralButton("PROFILE / SETTINGS");
        bottomBar.add(profileBtn);
        outer.add(bottomBar, BorderLayout.SOUTH);

        viewAllBtn.addActionListener(e -> new TransactionHistoryFrame(this, account).setVisible(true));
        profileBtn.addActionListener(e -> new ProfileFrame(this, account).setVisible(true));

        return outer;
    }

    public void loadData() {
        account = accountDAO.findByAccountNo(account.getAccountNo());
        balanceValueLabel.setText("৳ " + formatAmount(account.getBalance()));

        transactionList.removeAll();
        recentTransactions = transactionDAO.getRecent(account.getAccountNo(), 6);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        if (recentTransactions.isEmpty()) {
            transactionList.add("  No transactions yet.");
        }
        for (Transaction t : recentTransactions) {
            String sign = Transaction.DEPOSIT.equals(t.getType()) || Transaction.TRANSFER_IN.equals(t.getType()) ? "+" : "-";
            String row = String.format("%-16s %-14s %-10s %s৳ %-12s Bal: ৳ %s",
                    sdf.format(t.getTransactionDate()),
                    t.getDescription() == null ? t.getType() : t.getDescription(),
                    t.getType(),
                    sign, formatAmount(t.getAmount()), formatAmount(t.getBalanceAfter()));
            transactionList.add(row);
        }
    }

    private String formatAmount(java.math.BigDecimal amount) {
        return String.format("%,.2f", amount);
    }

    public Account getAccount() {
        return account;
    }
}
