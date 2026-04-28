package com.dataprotection.dataprotection.exception;

public class LoginAttemptFailedException extends RuntimeException {

    private final int remainingAttempts;

    public LoginAttemptFailedException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
