package com.github.mdeluise.ytsms.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        this("Operation not authorized");
    }
}
