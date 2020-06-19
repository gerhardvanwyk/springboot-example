package org.wyk.application.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wyk.application.filter.ExceptionHandlingFilter;
import org.wyk.application.filter.KeycloakUsernamePasswordAuthenticationProvider;

import javax.servlet.http.HttpServletRequest;

/**
 * When DEBUG is enabled it prints all the HTTP filters, request, responses etc. Not for Production
 */
@EnableWebSecurity(debug = true)
@KeycloakConfiguration
public class KeycloakWebSecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    CloseableHttpClient httpClient;


    /**
     * This set the AuthenticationProvider to Keycloak - The AuthenticationProvider
     * will do check the Keycloak token (OpenID) and authenticate the request.
     * @param auth
     * @throws Exception
     */
    @SneakyThrows
    @Override
    public void configure(AuthenticationManagerBuilder auth) {

        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());

        //Ads the authentication provider for a Token (API REST endpoints)
        auth.authenticationProvider(keycloakAuthenticationProvider);

        //Adds the authentication provider for a Username & Password (web UI)
        auth.authenticationProvider(new KeycloakUsernamePasswordAuthenticationProvider(mapper(), keycloakConfigResolver(), httpClient));

    }

    @Bean
    protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
        KeycloakAuthenticationProcessingFilter filter = new KeycloakAuthenticationProcessingFilter(authenticationManagerBean());
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        filter.setFilterProcessesUrl("/api/**");
        return filter;
    }


    /**
     * 	&#064;Override
     * 	protected void configure(HttpSecurity http) throws Exception {
     * 		http.authorizeRequests().antMatchers(&quot;/**&quot;).hasRole(&quot;USER&quot;).and().formLogin()
     * 				.usernameParameter(&quot;username&quot;) // default is username
     * 				.passwordParameter(&quot;password&quot;) // default is password
     * 				.loginPage(&quot;/authentication/login&quot;) // default is /login with an HTTP get
     * 				.failureUrl(&quot;/authentication/login?failed&quot;) // default is /login?error
     * 				.loginProcessingUrl(&quot;/authentication/login/process&quot;); // default is /login
     * 																		// with an HTTP
     * 																		// post
     * 	}
     * 	This set the permission for the resource the actual physical mapping to the file
     * 	must still happen.
     * @param http
     * @throws Exception
     */
    @Override
	 protected void configure(HttpSecurity http) throws Exception {
        http
                //CSFR protection enabled
                .csrf()
                    .requireCsrfProtectionMatcher(keycloakCsrfRequestMatcher()).and()

                //Session management - Save the session locally
                .sessionManagement()
                    .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).and()

                //Adds the ExceptionHandlingFilter before all others
                // Catch IO and Servlet Exceptions -- These are not handled by the Controller exception handling
                .addFilterBefore(exceptionHandlingFilter(), WebAsyncManagerIntegrationFilter.class)

                //Add a Authentications Step @see KeycloakUsernamePasswordAuthenticationProvider
                .addFilterBefore(keycloakAuthenticationProcessingFilter(), RequestCacheAwareFilter.class)

                //Handles Bearer Request - Authenticate against Keycloak (RefreshableToken)
                //RefreshableKeycloakSecurityContext
                .addFilterAfter(keycloakSecurityContextRequestFilter(), SecurityContextHolderAwareRequestFilter.class)

                //?
                .addFilterAfter(keycloakAuthenticatedActionsRequestFilter(), KeycloakSecurityContextRequestFilter.class)

                // Access denied handling
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint()).and()

                //Logout
                .logout()
                    .addLogoutHandler(keycloakLogoutHandler())
                    .logoutUrl("/logout").permitAll()
                    .logoutSuccessUrl("/login").and()

                //Restrict access based on URL pattern
                .authorizeRequests()

                    //permit all request with the pattern '/*.groovy'
                    .antMatchers("/*.groovy").permitAll()

                    //permit all request with the pattern '/login*'
                    .antMatchers("/login*").permitAll()

                    //permit all request with pattern '/error'
                    .antMatchers("/error*").permitAll()

                    //match api pattern , and require role 'authorized-user'
                    .antMatchers("/api/**").hasRole("authorized-user")

                    //For other patterns we require already authenticated user - no Roles
                    .anyRequest().authenticated().and()

                //Login page HTML form
                .formLogin().loginPage("/login").successForwardUrl("/home").failureForwardUrl("/error");
                //--------------------------------------

    }

    private ExceptionHandlingFilter exceptionHandlingFilter() {
        return new ExceptionHandlingFilter();
    }

    @Bean
    public ObjectMapper mapper(){
        return new ObjectMapper();
    }

    /**
     * Creates the Session Authentication Strategy (management of the session). The app only do the basics,
     * keycloak can expire etc sessions.
     * Addes Session Registry Implementation
     * @return
     */
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Entry point for Un Authenticated request, we set the default login url
     * else it goes to keycloak's login page.
     * This also mean we have to call the authentication endpoint manually after
     * the user has submitted a username and password.
     * @return
     * @throws Exception
     */
    @Bean
    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
        KeycloakAuthenticationEntryPoint entryPoint =  new KeycloakAuthenticationEntryPoint(adapterDeploymentContext());
        entryPoint.setLoginUri("/login");
        return entryPoint;
    }

    /**
     * Starting from Keycloak Spring Boot Adapter 7.0.0, due to some issues, the automatic discovery of the Keycloak
     * configuration from the application.properties (or application.yml) file will not work.
     * To overcome this problem we create this bean
     * @return
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    public KeycloakSecurityContextRequestFilter keycloakSecurityContextRequestFilter(){
        return new KeycloakSecurityContextRequestFilter();
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccessToken getAccessToken() {
        return ((KeycloakPrincipal) getRequest().getUserPrincipal()).getKeycloakSecurityContext().getToken();
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public KeycloakSecurityContext getKeycloakSecurityContext() {
        return ((KeycloakPrincipal) getRequest().getUserPrincipal()).getKeycloakSecurityContext();
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
