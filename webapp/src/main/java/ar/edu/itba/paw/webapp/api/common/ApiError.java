package ar.edu.itba.paw.webapp.api.common;

import java.io.Serializable;

/** Diagnostic messages are safe technical descriptions; clients translate the code. */
public class ApiError implements Serializable {

    private final String code;
    private final String message;

    public ApiError(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
