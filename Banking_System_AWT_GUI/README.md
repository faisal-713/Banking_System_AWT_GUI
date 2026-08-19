# BANKING SYSTEM — Java AWT + MySQL JDBC

A complete banking system GUI built with Java AWT, connected to a real MySQL database via JDBC. There is no dummy/demo data anywhere — Register, Login, Deposit, Withdraw, Transfer, Balance, Transaction History, Profile Edit, and PIN/Password changes are all saved directly to MySQL.

## Project Structure (classes split by package)

```
src/com/banking/
 ├── Main.java                     → Entry point (starts with LoginFrame)
 ├── db/
 │    └── DBConnection.java        → MySQL JDBC connection (reads config from db.properties)
 ├── model/
 │    ├── Customer.java
 │    ├── Account.java
 │    └── Transaction.java
 ├── dao/                          → All SQL queries live here (Data Access Layer)
 │    ├── CustomerDAO.java
 │    ├── AccountDAO.java          → login, register, deposit, withdraw, transfer, pin change
 │    └── TransactionDAO.java      → insert/select transaction history
 ├── exception/                    → Custom checked exceptions (matching the exception-handling table)
 │    ├── BankingException.java
 │    ├── InvalidCredentialException.java
 │    ├── DuplicateAccountException.java
 │    ├── InvalidAmountException.java
 │    ├── InsufficientBalanceException.java
 │    ├── InvalidTransferException.java
 │    ├── NoDataFoundException.java
 │    ├── InvalidInputException.java
 │    └── DatabaseOperationException.java
 ├── util/
 │    └── PasswordUtil.java        → Hashes PIN/Password with SHA-256
 └── ui/                           → All AWT screens (matching Design 3, large fonts/fields, colorful)
      ├── UIStyle.java             → Shared colors, fonts, button/field builders
      ├── LoginFrame.java
      ├── RegisterFrame.java
      ├── DashboardFrame.java
      ├── DepositFrame.java
      ├── WithdrawFrame.java
      ├── TransferFrame.java
      ├── BalanceFrame.java
      ├── TransactionHistoryFrame.java
      └── ProfileFrame.java        → Edit Profile, Change PIN, Change Password, Logout

sql/banking_system.sql             → Script to create the database and tables
db.properties                      → MySQL URL/Username/Password configuration
lib/                                → Put the mysql-connector-j jar here (see below)
```

## 1) Create the MySQL database

```bash
mysql -u root -p < sql/banking_system.sql
```

This creates the `banking_system` database and three tables: `customer`, `account`, `transactions`.
No sample/demo rows are inserted — the first user is created through the Register form.

## 2) Update db.properties

Set your own MySQL username/password in `db.properties`:

```
db.url=jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
```

## 3) Get the MySQL Connector/J (JDBC Driver)

This sandbox environment couldn't download from Maven Central due to network restrictions, so you'll need to download it yourself and place it in the `lib/` folder:

- Official page: https://dev.mysql.com/downloads/connector/j/
- Or Maven Central: https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/

Place the `mysql-connector-j-8.x.x.jar` file inside `lib/`.

## 4) Compile

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

## 5) Run (with the JDBC jar on the classpath)

**Linux/Mac:**
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" com.banking.Main
```

**Windows:**
```bash
java -cp "out;lib/mysql-connector-j-8.x.x.jar" com.banking.Main
```

## Feature summary

| Screen | What it does |
|---|---|
| Login | Log in with Account Number + PIN (verified against the database) |
| Register | Creates a new Customer + Account in MySQL, then goes straight to the Dashboard |
| Dashboard | Live balance, quick actions, and the 6 most recent transactions |
| Deposit / Withdraw / Transfer | Every action is saved as a transaction in the DB; balance updates in real time |
| Balance | Full account details |
| Transaction History | Complete transaction list + CSV export |
| Profile | Edit Name/Email/Phone/Address, Change PIN, Change Password |

## Exception handling (implemented per the exception-handling table)

- `InvalidCredentialException` → thrown on Login when the Account Number or PIN is wrong
- `DuplicateAccountException` → thrown on Register when the Email/Phone already exists
- `InvalidAmountException` → thrown on Deposit/Withdraw/Transfer for a zero/negative amount
- `InsufficientBalanceException` → thrown on Withdraw/Transfer when the balance is too low
- `InvalidTransferException` → thrown on Transfer for an invalid receiver account
- `NoDataFoundException` → thrown in History when there are no transactions
- `InvalidInputException` → thrown on Register/Profile Edit for badly formatted input
- `DatabaseOperationException` → thrown when a DB connection/query fails (unchecked, wraps SQLException in the DAO layer)

Every exception is shown to the user as a friendly dialog message.

## Notes

- PINs and Passwords are hashed with `SHA-256` before being stored — never in plain text.
- All screens use large fonts (18–28px) and large input fields.
- Color palette: `#0D47A1`, `#1565C0`, `#E3F2FD`, `#FFFFFF`, `#212121`, `#2E7D32`, `#C62828`, `#6A1B9A`
- Login and Register have direct navigation buttons ("Register Now" / "Login Here") — clicking one opens the other frame instantly.
- The project has already been successfully compiled with `javac` (only the JDBC driver jar is needed at runtime).
