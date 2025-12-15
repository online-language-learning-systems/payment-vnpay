package com.hub.common_library.exception;

import com.hub.common_library.utils.MessagesUtils;

public class NotFoundException extends RuntimeException {

    private final String message;

    public NotFoundException(String errorCode, Object... var2) {
        this.message = MessagesUtils.getMessage(errorCode, var2);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
