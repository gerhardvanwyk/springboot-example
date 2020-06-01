package org.wyk.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @GetMapping(value = {"/", "/home"}, produces = {"text/html"})
    public String home(){
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(Model model, Principal principal){
        log.info("AccessToken: " + securityContext.getTokenString());
        log.info("User: {} / {}", accessToken.getPreferredUsername(), accessToken.getName());
        log.info("Principal: {}", principal.getName());
        return "redirect:index";

    }
}
