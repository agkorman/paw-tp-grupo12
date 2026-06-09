package ar.edu.itba.paw.webapp.view;

/**
 * View-layer holder describing a relative time as a message code plus the quantity argument.
 * The actual localized text is resolved in the JSP/tag with {@code <spring:message>}, so message
 * resolution never happens in the controller or in Java.
 */
public final class RelativeTime {

    private final String code;
    private final long quantity;

    RelativeTime(final String code, final long quantity) {
        this.code = code;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public long getQuantity() {
        return quantity;
    }
}
