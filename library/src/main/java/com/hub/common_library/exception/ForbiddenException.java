package com.hub.common_library.exception;

import com.hub.common_library.utils.MessagesUtils;

public class ForbiddenException extends RuntimeException {
    private String message;

    public ForbiddenException(String errorCode, Object... var2) {
        this.message = MessagesUtils.getMessage(errorCode, var2);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
