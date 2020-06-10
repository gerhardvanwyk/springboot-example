package org.wyk.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.resource.AuthorizationResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UIController {

    private final KeycloakSecurityContext securityContext;
    private final AccessToken accessToken;


    /**
     * 'value' - URL pattern
     * 'produces' - MIME type
     * @return
     */
    @GetMapping(value = {"/home"}, produces = {"text/html"})
    public String getHome(){
        return "index";
    }

    /**
     * This is necessary -- might be a better way
     * The post login action redirect to '/home' but still use the post method
     * @return
     */
    @PostMapping(value = {"/home"}, produces = {"text/html"})
    public String postHome(){
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        log.debug("We are in control, please do not stress about your future");
        return "login";
    }

}
