package org.wyk.application.controller;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class ErrorController extends AbstractErrorController {

    private final ErrorProperties errorProperties;

    public ErrorController(ErrorAttributes errorAttributes, ServerProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
        Assert.notNull(errorProperties, "ErrorProperties must not be null");
        this.errorProperties = errorProperties.getError();
    }

    @Override
    public String getErrorPath() {
        return errorProperties.getPath();
    }

    @PostMapping("/authError")
    public ModelAndView error(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, Object> model = getErrorAttributes(request, getErrorAttributeOptions(request));
        model.put("status", HttpStatus.UNAUTHORIZED);
        model.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());

        if (isIncludeMessage(request)) {
            Exception ex = (Exception) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            String error = (ex == null) ? "Server Error Occurred" : ex.getLocalizedMessage();
            model.put("message", error);

            if(Objects.requireNonNull(ex).getClass().equals(ProviderNotFoundException.class)){
                model.put("status", HttpStatus.SERVICE_UNAVAILABLE);
                model.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
                //TODO this should be configurable
                model.put("message", "Open ID Connect server is not running ");
            }
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/error", model);
        return modelAndView;
    }

    @GetMapping("/error")
    public ModelAndView errorHtml() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error.html");
        return modelAndView;
    }

    /**
     *  Builds the Error Message Options from the application configuration.
     *  {
     *
     *  }
     * @param request
     * @return
     */
    protected ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request) {

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
        switch (getErrorProperties().getIncludeStacktrace()) {
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
        switch (getErrorProperties().getIncludeMessage()) {
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
        switch (getErrorProperties().getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return getErrorsParameter(request);
            default:
                return false;
        }
    }

    /**
     * Provide access to the error properties.
     * @return the error properties
     */
    protected ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }

}
