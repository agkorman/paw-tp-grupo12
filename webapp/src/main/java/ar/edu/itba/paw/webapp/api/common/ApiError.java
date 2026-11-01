package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** Diagnostic messages are safe technical descriptions; clients translate the code. */
public class ApiError implements Serializable {

    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ApiFieldError> fieldErrors;

    public ApiError(final String code, final String message) {
        this(code, message, Collections.emptyList());
    }

    public ApiError(final String code, final String message, final List<ApiFieldError> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(List.copyOf(fieldErrors));
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }
}
