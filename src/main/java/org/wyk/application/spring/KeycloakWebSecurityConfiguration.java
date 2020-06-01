package org.wyk.application.spring;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.representations.AccessToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * When DEBUG is enabled it prints all the HTTP filters, request, responses etc. Not for Production
 */
@EnableWebSecurity(debug = true)
@Configuration
public class KeycloakWebSecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

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
     * @param http
     * @throws Exception
     */
    @Override
	 protected void configure(HttpSecurity http) throws Exception {
	  		http
                    .authorizeRequests()

                    //permit all request to resources/**
                    .antMatchers("resources/**").permitAll()

                    //permit all request to '/login' url
                    .antMatchers("/login*").permitAll()

                    //for api request, token expected
                    .antMatchers("/api/**").hasRole("authorized-user")

                    //For all other urls we expect the role authenticated-user, if not redirect to the login form and we permit all request to reach to login screen.
                    .anyRequest().authenticated().and().formLogin().loginPage("/login");
	  		        //--------------------------------------

          	}

    /**
     * Creates the Session Authentication Strategy (management of the session). The app only do the basics,
     * keycloak can expire etc sessions.
     * Addes Session Registry Implementation
     * @return
     */
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
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
