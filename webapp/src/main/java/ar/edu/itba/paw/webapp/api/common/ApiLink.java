package ar.edu.itba.paw.webapp.api.common;

import java.io.Serializable;

public class ApiLink implements Serializable {

    private final String href;

    public ApiLink(final String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
