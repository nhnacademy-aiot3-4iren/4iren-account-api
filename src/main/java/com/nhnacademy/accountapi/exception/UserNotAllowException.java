package com.nhnacademy.accountapi.exception;

public class UserNotAllowException extends RuntimeException {
    public UserNotAllowException(String message) {
        super(message);
    }
}
