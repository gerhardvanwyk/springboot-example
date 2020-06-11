package org.wyk.application.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.wyk.application.dto.ErrorResponse;
import org.wyk.application.exception.SessionNotAllowedException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class KeycloakUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    final ObjectMapper mapper;
    final KeycloakSpringBootConfigResolver resolver;
    final CloseableHttpClient template;


    public KeycloakUsernamePasswordAuthenticationProvider(ObjectMapper mapper, KeycloakSpringBootConfigResolver resolver,
                                                          CloseableHttpClient restTemplate) {
        this.mapper = mapper;
        this.resolver = resolver;
        this.template = restTemplate;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) {
        log.debug(authentication.getName());
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;

        //This resolve the deployment against a running keycloak server
        KeycloakDeployment deployment = resolver.resolve(null);

        String request = "client_id=" + deployment.getResourceName() + "&username=" + token.getName() + "&password=" +
                token.getCredentials() + "&grant_type=password";

        HttpPost post = new HttpPost(new URI(deployment.getTokenUrl()));
        post.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);


        //This is a bit of the hack we are getting the client secret from the deployment loaded
        Map<String, String> header = new HashMap<>();
        deployment.getClientAuthenticator().setClientCredentials(deployment, header, header);
        post.addHeader("Authorization", header.get("Authorization"));

        //The request itself
        post.setEntity(new StringEntity(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        CloseableHttpResponse response = template.execute(post);

        switch (response.getStatusLine().getStatusCode()){
            case HttpStatus.SC_UNAUTHORIZED : {

                //User is not authorized
                log.debug("Un-Authorized User " + token.getName());

                //Creates a json object of the error
                String body = IOUtils.toString(response.getEntity().getContent());
                ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);
                errorResponse.setHttpReason(response.getStatusLine().getReasonPhrase());
                errorResponse.setHttpStatus(response.getStatusLine().getStatusCode());

                throw new SessionNotAllowedException("The user is not authorized. " + errorResponse.getError_description(), errorResponse );
            }
            case HttpStatus.SC_OK: {
                //User is authorized
                log.debug("Authorized User " + token.getName());

                //Creates a json object of the error
                String body = IOUtils.toString(response.getEntity().getContent());

                AccessTokenResponse accessTokenResponse = mapper.readValue(body, AccessTokenResponse.class);
                log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(accessTokenResponse));

//                RefreshableKeycloakSecurityContext skSession = new RefreshableKeycloakSecurityContext(deployment, null, tokenString, token, null, null, null);
//                String principalName = AdapterUtils.getPrincipalName(deployment, token);
//                final KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = new KeycloakPrincipal<RefreshableKeycloakSecurityContext>(principalName, skSession);
//                final Set<String> roles = AdapterUtils.getRolesFromSecurityContext(skSession);

                context.setAuthentication(token);
                break;
            }
            case HttpStatus.SC_FORBIDDEN:{
                //Applications is not allowed
                log.debug("Application is not allowed " + deployment.getResourceName());
                break;
            }
            default:{

                //some other error
                log.error("Error occurred during the authentication process. Status: " + response.getStatusLine().getStatusCode() + " Reason: "  + response.getStatusLine().getReasonPhrase());

                //Creates a json object of the error
                String body = IOUtils.toString(response.getEntity().getContent());
                ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);
                errorResponse.setHttpReason(response.getStatusLine().getReasonPhrase());
                errorResponse.setHttpStatus(response.getStatusLine().getStatusCode());

                throw new SessionNotAllowedException("The user is not authorized. " + errorResponse.getError_description(), errorResponse );
            }
        }


//        if(response.getStatusCode().is2xxSuccessful()){
//            AccessTokenResponse accessTokenResponse = mapper.readValue(response.getBody(), AccessTokenResponse.class);
//            AccessToken accessToken = new AccessToken();
//
//            RefreshableKeycloakSecurityContext skSession = new RefreshableKeycloakSecurityContext(deployment, null, tokenString, token, null, null, null);
//            String principalName = AdapterUtils.getPrincipalName(deployment, token);
//            final KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = new KeycloakPrincipal<RefreshableKeycloakSecurityContext>(principalName, skSession);
//            final Set<String> roles = AdapterUtils.getRolesFromSecurityContext(skSession);
//
//            List<GrantedAuthority> authorityList = new ArrayList(1);
//            authorityList.add((GrantedAuthority) () -> accessTokenResponse.getToken());
//
////            KeycloakPrincipal
//
//            token = new UsernamePasswordAuthenticationToken(accessTokenResponse, token.getCredentials(), authorityList);
//            log.debug("AccessTokenResponse: " + response.getBody());
//        }else if(response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)){
//            log.debug("Response: " + response.getBody());
//            token.setAuthenticated(false);
//        }
//        else{
//            String error = response.getBody();
//            throw  new RuntimeException("Could not authenticate the user " + response.getStatusCodeValue() + ", ERROR: \n" + error);
//        }
//        log.debug("Username: " + token.getName(), ", Password: " + token.getCredentials() + ", " + mapper.writeValueAsString(response));
        return token;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
