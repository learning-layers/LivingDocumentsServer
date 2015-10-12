package de.hska.ld.oidc.client.exception;

public class CreationFailedException extends Exception {
    private static final long serialVersionUID = 2987482280355207073L;

    public CreationFailedException() {
        super();
    }

    public CreationFailedException(String message) {
        super(message);
    }

    public CreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreationFailedException(Throwable cause) {
        super(cause);
    }

    public CreationFailedException(Long newDocumentId) {
        super(String.valueOf(newDocumentId));
    }
}
