package ar.edu.itba.paw.webapp.exception;

public class WebAppConfigurationException extends RuntimeException {

    public WebAppConfigurationException(final String message) {
        super(message);
    }

    public WebAppConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
