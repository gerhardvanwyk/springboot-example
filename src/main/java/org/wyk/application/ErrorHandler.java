package org.wyk.application;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;

import javax.servlet.http.HttpServletRequest;

public  class ErrorHandler {

    private final ErrorProperties errorProperties;

    public ErrorHandler(ServerProperties serverProperties) {
        this.errorProperties = serverProperties.getError();
    }

    /**
     *  Builds the Error Message Options from the application configuration.
     *  {
     *
     *  }
     * @param request
     * @return
     */
    public ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request) {

        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();

        if (this.errorProperties.isIncludeException()) {
            options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
        }
        if (isIncludeStackTrace(request)) {
            options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        if (isIncludeMessage(request)) {
            options = options.including(ErrorAttributeOptions.Include.MESSAGE);
        }
        if (isIncludeBindingErrors(request)) {
            options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
        }
        return options;
    }

    /**
     * Determine if the stacktrace attribute should be included.
     * @param request the source request
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(HttpServletRequest request) {
        switch (this.errorProperties.getIncludeStacktrace()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
            case ON_TRACE_PARAM:
                return getTraceParameter(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the message attribute should be included.
     * @param request the source request
     * @return if the message attribute should be included
     */
    protected boolean isIncludeMessage(HttpServletRequest request) {
        switch (this.errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return getMessageParameter(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the errors attribute should be included.
     * @param request the source request
     * @return if the errors attribute should be included
     */
    protected boolean isIncludeBindingErrors(HttpServletRequest request) {
        switch (this.errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return getErrorsParameter(request);
            default:
                return false;
        }
    }

    protected boolean getTraceParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "trace");
    }

    protected boolean getMessageParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "message");
    }

    protected boolean getErrorsParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "errors");
    }

    protected boolean getBooleanParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            return false;
        }
        return !"false".equalsIgnoreCase(parameter);
    }
}
