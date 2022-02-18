package com.aircraftcarrier.framework.tookit.validation;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lzp
 */
@Getter
@Setter
public class ErrorMessage {

    /**
     * propertyPath
     */
    private String propertyPath;

    /**
     * message
     */
    private String message;

    /**
     * ErrorMessage()
     */
    private ErrorMessage() {
    }

    /**
     * ErrorMessage
     *
     * @param propertyPath
     * @param message
     */
    public ErrorMessage(String propertyPath, String message) {
        this.propertyPath = propertyPath;
        this.message = message;
    }
}
