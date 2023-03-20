package com.github.mdeluise.ytsms.exception;

public class FetchingModeException extends RuntimeException {
    public FetchingModeException() {
        super("The used fetching mode does not provide this function");
    }
}
