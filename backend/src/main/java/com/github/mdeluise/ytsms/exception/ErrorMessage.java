package com.github.mdeluise.ytsms.exception;

import java.util.Date;

public record ErrorMessage(int statusCode, Date timestamp, String message, String description, String cause) {
}
