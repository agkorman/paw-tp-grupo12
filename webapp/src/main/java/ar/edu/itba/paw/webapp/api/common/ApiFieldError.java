package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A machine-readable validation error associated with an input field. */
public class ApiFieldError implements Serializable {

    private final String field;
    private final String code;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> params;

    public ApiFieldError(final String field, final String code) {
        this(field, code, Collections.emptyMap());
    }

    public ApiFieldError(final String field, final String code, final Map<String, Object> params) {
        this.field = field;
        this.code = code;
        this.params = params == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }

    public String getField() {
        return field;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
