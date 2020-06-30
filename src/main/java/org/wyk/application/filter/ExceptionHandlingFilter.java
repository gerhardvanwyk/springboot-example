package org.wyk.application.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.wyk.application.ErrorHandler;
import org.wyk.application.dto.ApiError;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
/**
 * This is a solution to the problem of errors not being handled when occurring in filters.
 */
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ErrorHandler errorHandler;

    public ExceptionHandlingFilter(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        } catch (IOException  e) {
            setErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, request, response, e);
            log.error("Could not establish connection", e);
        } catch (ServletException e) {
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, response, e);
            log.error("Application Error occurred", e);
        }
    }

    public void setErrorResponse(HttpStatus status, HttpServletRequest request, HttpServletResponse response, Throwable ex){
        response.setStatus(status.value());
        response.setContentType("application/json");
        // A class used for errors
        ApiError apiError = new ApiError(status, ex, errorHandler);
        try {
            String json = apiError.convertToJson();
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
        }
    }
}
