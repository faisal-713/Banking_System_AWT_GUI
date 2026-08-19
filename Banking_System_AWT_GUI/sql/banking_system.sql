CREATE DATABASE IF NOT EXISTS banking_system;
USE banking_system;


CREATE TABLE IF NOT EXISTS customer (
    customer_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL UNIQUE,
    address       VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB AUTO_INCREMENT=1;


CREATE TABLE IF NOT EXISTS account (
    account_no    BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT NOT NULL,
    account_type  VARCHAR(30) NOT NULL DEFAULT 'Savings Account',
    balance       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    pin_hash      VARCHAR(255) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    open_date     DATE NOT NULL DEFAULT (CURRENT_DATE),
    branch        VARCHAR(100) NOT NULL DEFAULT 'Main Branch',
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1001;


CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no       BIGINT NOT NULL,
    type             VARCHAR(20) NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    balance_after    DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255),
    to_account_no    BIGINT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_account FOREIGN KEY (account_no)
        REFERENCES account(account_no) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1;

CREATE INDEX idx_txn_account ON transactions(account_no);
CREATE INDEX idx_txn_date ON transactions(transaction_date);
