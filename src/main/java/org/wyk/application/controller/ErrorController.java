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
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.wyk.application.ErrorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class ErrorController extends AbstractErrorController {

    private final ErrorProperties errorProperties;

    private final ErrorHandler errorHandler;

    public ErrorController(ErrorAttributes errorAttributes, ServerProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
        Assert.notNull(errorProperties, "ErrorProperties must not be null");
        this.errorProperties = errorProperties.getError();
        this.errorHandler = new ErrorHandler(errorProperties);
    }

    @Override
    public String getErrorPath() {
        return errorProperties.getPath();
    }

    @PostMapping("/authError")
    public ModelAndView error(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, Object> model = getErrorAttributes(request, errorHandler.getErrorAttributeOptions(request));
        model.put("status", HttpStatus.UNAUTHORIZED);
        //model.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());

        //TODO move this to APIError handled by the filter
        if (model.containsKey("message")) {
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
    public ModelAndView errorHtml(ModelMap model) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addAllObjects(model);
        modelAndView.setViewName("error.html");
        return modelAndView;
    }
}
