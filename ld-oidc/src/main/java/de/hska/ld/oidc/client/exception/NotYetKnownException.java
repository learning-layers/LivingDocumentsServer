package de.hska.ld.oidc.client.exception;

public class NotYetKnownException extends Exception {
    private static final long serialVersionUID = 2813428375275356725L;

    public NotYetKnownException() {
        super();
    }

    public NotYetKnownException(String message) {
        super(message);
    }

    public NotYetKnownException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotYetKnownException(Throwable cause) {
        super(cause);
    }

}
