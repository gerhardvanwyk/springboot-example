package org.wyk.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.wyk.application.dto.Identity;

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
    @GetMapping(value = {"/home", "/"}, produces = {"text/html"})
    public String getHome(){
        return "index";
    }

    /**
     * This is necessary -- might be a better way
     * The post login action redirect to '/home' but still use the post method
     * @return
     */
    @PostMapping(value = {"/home"}, produces = {"text/html"})
    public ModelAndView postHome(){
        log.debug("POST Login in");
        ModelAndView modelAndView = new ModelAndView("index");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Identity identity = new Identity(auth);
        modelAndView.addObject("identity", identity);

        return modelAndView;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
