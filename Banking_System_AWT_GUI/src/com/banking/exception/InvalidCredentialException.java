package com.banking.exception;

public class InvalidCredentialException extends BankingException {
    public InvalidCredentialException(String message) {
        super(message);
    }
}
