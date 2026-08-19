package com.banking.exception;

public class DuplicateAccountException extends BankingException {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
