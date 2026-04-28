package com.dataprotection.dataprotection.exception;

public class SuspiciousLoginAttemptException extends RuntimeException {

    public SuspiciousLoginAttemptException(String message) {
        super(message);
    }
}
