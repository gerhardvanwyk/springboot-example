package org.wyk.application.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when the user session is not authenticated. Authentication fails.
 * The message contains more details on the reason for failure.
 */
@Getter
public class SessionNotAllowedException extends AuthenticationException {

    private Object jsonError;

    /**
     * @param msg - Error message for logs
     * @param jsonError - Json Object that can be send to other clients with more detailed information
     */
    public SessionNotAllowedException(String msg, Object jsonError) {
        super(msg);
        this.jsonError = jsonError;
    }

}
