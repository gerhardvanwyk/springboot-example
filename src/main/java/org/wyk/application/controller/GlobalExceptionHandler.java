package org.wyk.application.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        log.error("Request: " + req.getRequestURL() + " raised ", ex);
        //Todo logout the user
        //@ see https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-exceptionhandler
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.addObject("url", req.getRequestURL());
        mav.setStatus(HttpStatus.OK);
        mav.setViewName("error.html");
        return mav;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ModelAndView handleAuthError(HttpServletRequest req, Exception ex) {
        log.error("Request: " + req.getRequestURL() + " raised " + ex);
        //Todo logout the user
        //@ see https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-exceptionhandler
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error.html");
        return mav;
    }
}
