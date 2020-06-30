package org.wyk.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.springframework.security.core.Authentication;

@Data
public class Identity {

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    @JsonProperty
    private String accessToken;

    public Identity(Authentication authentication){
        KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal =
                (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) authentication.getPrincipal();
        this.name = principal.getName();
        this.accessToken = principal.getKeycloakSecurityContext() == null ? "": principal.getKeycloakSecurityContext().getTokenString();
        this.type = principal.getKeycloakSecurityContext() == null ? "" : principal.getKeycloakSecurityContext().getToken().getType();
    }

}
