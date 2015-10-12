package de.hska.ld.oidc.client.exception;


public class AuthenticationNotValidException extends Exception {
    private static final long serialVersionUID = -8219254245594998419L;

    public AuthenticationNotValidException() {
        super();
    }

    public AuthenticationNotValidException(String message) {
        super(message);
    }

    public AuthenticationNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationNotValidException(Throwable cause) {
        super(cause);
    }
}
