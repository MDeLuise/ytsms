package com.github.mdeluise.ytsms.exception;

public class MaximumNumberOfUsersReachedExceptions extends RuntimeException {
    public MaximumNumberOfUsersReachedExceptions() {
        super("Maximum number of user reached.");
    }
}
