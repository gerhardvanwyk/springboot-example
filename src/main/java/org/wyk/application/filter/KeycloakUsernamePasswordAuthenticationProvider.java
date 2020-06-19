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
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Use an username and password to Authenticate & Authorise a User.
 *
 * 1. Loads the KeycloakDeployment
 * 2. Get An Access-, Id- and Refresh Token from Keycloak (The AccessToken is both the access_token and id_token )
 * 3. The access token gets translated with JWT
 * 4. Create a RefreshableKeycloakSecurityContext
 */
@Slf4j
@Component
public class KeycloakUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final ObjectMapper mapper;
    private final KeycloakSpringBootConfigResolver resolver;
    private final CloseableHttpClient template;

    public KeycloakUsernamePasswordAuthenticationProvider(final ObjectMapper mapper, final KeycloakSpringBootConfigResolver resolver,
                                                          final CloseableHttpClient restTemplate) {
        this.mapper = mapper;
        this.resolver = resolver;
        this.template = restTemplate;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) {
        log.debug(authentication.getName());
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;

        // This resolve the deployment against a running keycloak server
        // and loads the configured properties
        // We cannot read from a keycloak.json file
        // We load the configuration through an application.yml
        final KeycloakDeployment deployment = resolver.resolve(null);

        final String request = "client_id=" + deployment.getResourceName() + "&username=" + token.getName() + "&password=" +
                token.getCredentials() + "&grant_type=password";

        final String tokenUrl = deployment.getTokenUrl();

        if(tokenUrl == null)
            throw new SessionNotAllowedException("OIDC server is not reachable at " + deployment.getAuthServerBaseUrl() );

        final HttpPost post = new HttpPost(new URI(tokenUrl));
        post.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        //This is a bit of the hack we are getting the client secret from the deployment loaded
        final Map<String, String> header = new HashMap<>();
        deployment.getClientAuthenticator().setClientCredentials(deployment, header, header);
        post.addHeader("Authorization", header.get("Authorization"));

        //The request itself
        post.setEntity(new StringEntity(request));

        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        final CloseableHttpResponse response = template.execute(post);
        final String body = IOUtils.toString(response.getEntity().getContent());

        switch (response.getStatusLine().getStatusCode()){

            case HttpStatus.SC_UNAUTHORIZED : {

                //User is not authorized
                log.debug("Un-Authorized User " + token.getName());

                //Creates a json object of the error
                final ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);
                errorResponse.setHttpReason(response.getStatusLine().getReasonPhrase());
                errorResponse.setHttpStatus(response.getStatusLine().getStatusCode());

                throw new SessionNotAllowedException("The user is not authorized. " + errorResponse.getError_description(), errorResponse );
            }

            case HttpStatus.SC_OK: {
                //User is authorized
                log.debug("Authorized User " + token.getName());

                //Creates a json object of the error
                final AccessTokenResponse clientToken = mapper.readValue(body, AccessTokenResponse.class);

                final String accessTokenStr = clientToken.getToken();

                try {
                    //The access token is both the Access token and the IDToken
                    final AccessToken accessToken = JsonSerialization.readValue(new JWSInput(accessTokenStr).getContent(), AccessToken.class);
                    log.debug("Access Token: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(accessToken));

                    final RefreshableKeycloakSecurityContext keycloakSecurityContext =
                            new RefreshableKeycloakSecurityContext(deployment, null, accessTokenStr, accessToken, clientToken.getIdToken(), accessToken, clientToken.getRefreshToken());

                    final String principalName = AdapterUtils.getPrincipalName(deployment, accessToken);
                    final KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = new KeycloakPrincipal<>(principalName, keycloakSecurityContext);


                    token = new UsernamePasswordAuthenticationToken(principal, token.getCredentials(), token.getAuthorities());

                } catch (IOException e) {
                    throw new SessionNotAllowedException("Session could not be stored", e);
                }

                log.debug("Access Token Response: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(clientToken));

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
                final ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);
                errorResponse.setHttpReason(response.getStatusLine().getReasonPhrase());
                errorResponse.setHttpStatus(response.getStatusLine().getStatusCode());

                throw new SessionNotAllowedException("The user is not authorized. " + errorResponse.getError_description(), errorResponse );
            }
        }

        return token;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
