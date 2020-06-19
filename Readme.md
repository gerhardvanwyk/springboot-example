# springboot-example
Springboot with a RestController, UIController using thymeleaf and security using keycloak



## HTTP Request
The request url will first be matched againts a security configuration in org.wyk.application.spring.KeycloakWebSecurityConfiguration.configure
if permission is allowed, either by being authenticated already or that the request match an anonomous or un authenticated pattern
it will be allowed through.

Next it will be check by if it matches any of the MVC path or resource handlers patterns. org.wyk.application.spring.WebMvcConfiguration


## Authentication Process

The UsernamePasswordAuthenticationToken is created by the UsernamePasswordAuthenticationFilter, this is configured in 
the KeycloakWebSecurityConfiguration.
The UsernamePasswordAuthenticationFilter filters on POST '/login'. This action is initiated by the login.html form.
The filter calls the AuthenticationManager. We have registered our own KeycloakUsernamePasswordAuthenticationProvider
with the AuthenticationManager to handle (username + credential) authentication. 

The steps in the Authentication process is as follows:

## The Filter chain


**1** ExceptionHandlingFilter 
>Own Exception Handling. Catch IO and Servlet Exceptions
    
**2** WebAsyncManagerIntegrationFilter
>Provides integration between the SecurityContext and Spring Web's WebAsyncManager
    
**3** SecurityContextPersistenceFilter
>Populates the SecurityContextHolder with information obtained from the configured SecurityContextRepository prior to 
the request and stores it back in the repository once the request has completed and clearing the context holder. 
By default it uses an HttpSessionSecurityContextRepository. See this class for information HttpSession related 
configuration options. This filter will only execute once per request, to resolve servlet container (specifically Weblogic) incompatibilities.
This filter MUST be executed BEFORE any authentication processing mechanisms. Authentication processing mechanisms 
(e.g. BASIC, CAS processing filters etc) expect the SecurityContextHolder to contain a valid SecurityContext by the time they execute. 
    
**4** HeaderWriterFilter
>Filter implementation to add headers to the current response. Can be useful to add certain headers which enable 
browser protection. Like X-Frame-Options, X-XSS-Protection and X-Content-Type-Options.
    
**5** CsrfFilter
>Applies CSRF protection using a synchronizer token pattern. Developers are required to ensure that CsrfFilter is invoked 
>for any request that allows state to change. Typically this just means that they should ensure their web application 
>follows proper REST semantics (i.e. do not change state with the HTTP methods GET, HEAD, TRACE, OPTIONS).
>Typically the CsrfTokenRepository implementation chooses to store the CsrfToken in HttpSession with 
>HttpSessionCsrfTokenRepository wrapped by a LazyCsrfTokenRepository. This is preferred to storing the token in a cookie 
>which can be modified by a client application.   
    
**6** LogoutFilter
>Logs a principal out.
>Polls a series of LogoutHandlers. The handlers should be specified in the order they are required. Generally you will
>want to call logout handlers TokenBasedRememberMeServices and SecurityContextLogoutHandler (in that order).
>After logout, a redirect will be performed to the URL determined by either the configured LogoutSuccessHandler 
>or the logoutSuccessUrl, depending on which constructor was used.     
    
**7** UsernamePasswordAuthenticationFilter
>Processes an authentication form submission. Called AuthenticationProcessingFilter prior to Spring Security 3.0. Login forms 
>must present two parameters to this filter: a username and password. The default parameter names to use are contained 
>in the static fields SPRING_SECURITY_FORM_USERNAME_KEY and SPRING_SECURITY_FORM_PASSWORD_KEY. The parameter names can 
>also be changed by setting the usernameParameter and passwordParameter properties.
>This filter by default responds to the URL /login.
>> Here we pass in our own provider @see KeycloakUsernamePasswordAuthenticationProvider 
    
    
**8** KeycloakAuthenticationProcessingFilter
>Match uri POST '/sso/login' by default, changed it to POST '/login'   
>
>>or 
>
>Request must contain the 'Authorization' Header (Bearer)
>
>>or
>
>OAuth2 'access_token'
>
>>or
>
> a Cookie matching KEYCLOAK_ADAPTER_STATE

> A  RequestAuthenticator is create and used to authenticate the request.
> 

**9** RequestCacheAwareFilter
>Responsible for reconstituting the saved request if one is cached and it matches the current request. It will call 
>getMatchingRequest on the configured RequestCache. If the method returns a value (a wrapper of the saved request), 
>it will pass this to the filter chain's doFilter method. If null is returned by the cache, the original request is 
>used and the filter has no effect.     
    
**10** SecurityContextHolderAwareRequestFilter
>A Filter which populates the ServletRequest with a request wrapper which implements the servlet API security methods.
>In pre servlet 3 environment the wrapper class used is SecurityContextHolderAwareRequestWrapper. See its javadoc for 
>the methods that are implemented.
>In a servlet 3 environment SecurityContextHolderAwareRequestWrapper is extended to provide the following additional methods:
 
     HttpServletRequest.authenticate(HttpServletResponse) - Allows the user to determine if they are authenticated and 
     if not send the user to the login page. See setAuthenticationEntryPoint(AuthenticationEntryPoint).
     HttpServletRequest.login(String, String) - Allows the user to authenticate using the AuthenticationManager. 
     See setAuthenticationManager(AuthenticationManager).
     HttpServletRequest.logout() - Allows the user to logout using the LogoutHandlers configured in Spring Security. 
     See setLogoutHandlers(List).
     AsyncContext.start(Runnable) - Automatically copy the SecurityContext from the SecurityContextHolder found on the 
     Thread that invoked AsyncContext.start(Runnable) to the Thread that processes the Runnable.
    
    
**11** KeycloakSecurityContextRequestFilter
>RefreshableKeycloakSecurityContext --> refresh the Keycloak context ??? 
    
    
**12** KeycloakAuthenticatedActionsFilter
    
    
**13** AnonymousAuthenticationFilter
>Detects if there is no Authentication object in the SecurityContextHolder, and populates it with one if needed. 
>Populate it with a AnonymousAuthenticationToken for Anonymous Access
    
**14** SessionManagementFilter
>Detects that a user has been authenticated since the start of the request and, if they have, calls the configured 
>SessionAuthenticationStrategy to perform any session-related activity such as activating session-fixation protection 
>mechanisms or checking for multiple concurrent logins.     
    
**15** ExceptionTranslationFilter
>Handles any AccessDeniedException and AuthenticationException thrown within the filter chain.
>This filter is necessary because it provides the bridge between Java exceptions and HTTP responses. It is solely 
>concerned with maintaining the user interface. This filter does not do any actual security enforcement.
>If an AuthenticationException is detected, the filter will launch the authenticationEntryPoint. This allows common 
>handling of authentication failures originating from any subclass of AbstractSecurityInterceptor.
>If an AccessDeniedException is detected, the filter will determine whether or not the user is an anonymous user. If 
>they are an anonymous user, the authenticationEntryPoint will be launched. If they are not an anonymous user, the 
>filter will delegate to the AccessDeniedHandler. By default the filter will use AccessDeniedHandlerImpl.
>To use this filter, it is necessary to specify the following properties:
 
     authenticationEntryPoint indicates the handler that should commence the authentication process if an 
     AuthenticationException is detected. Note that this may also switch the current protocol from http to https for an SSL login.
     requestCache determines the strategy used to save a request during the authentication process in order 
     that it may be retrieved and reused once the user has authenticated. The default implementation is HttpSessionRequestCache.
    
**16** FilterSecurityInterceptor
>Performs security handling of HTTP resources via a filter implementation.
>The SecurityMetadataSource required by this security interceptor is of type FilterInvocationSecurityMetadataSource.
>Refer to AbstractSecurityInterceptor for details on the workflow. 
    
## The Request Security Process

### Notes:
org.keycloak.adapters.PreAuthActionsHandler gets called in the org.keycloak.adapters.tomcat.AbstractKeycloakAuthenticatorValve
before any Filters in Catalina Valves (StandardEngineValve --> ErrorReportValve) 

Specific to keycloak handlers request with the:
* k_logout --> logout of keycloak (needs a valid JWT token)
* k_push_not_before --> Investigation needed (needs a valid JWT token)
* k_test_available --> Investigation needed
* k_jwks --> Investigatin needed


### Authorization Code Flow

1. Load the (Keycloak) configuration known as the KeycloakDeployment from the application.yml file and from the Keycloak
server.

2. Use the GET Token URL endpoint to obtain a AccessTokenResponse

Request: ``` POST http://127.0.0.1:8080/auth/realms/example/protocol/openid-connect/token ```

Header: ``` Content Type: application/x-www-form-urlencoded ```

Header: ``` Authorization: Basic {clientId}:{clientSecret} ```   This is read from the application.yml

Body: ``` client_id=exampl-app-id&username=admin&password=94875l2&grant_type=password ```

Response:
```json
{
  "access_token" : "eyJhbGciOiJSUzI1.....8aP1_iauQf-VpOchP70ADnnsfB54J8MAw5RQA0JaUCsBRHIsTMsowzadpERuvj3HvD7YScOQ",
  "expires_in" : 300,
  "refresh_expires_in" : 1800,
  "refresh_token" : "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA.....0TC8em6dSPK6ISN-yvTHoOqXg8-l2pYYhyYQHUHlGhs",
  "token_type" : "bearer",
  "id_token" : null,
  "not-before-policy" : 0,
  "session_state" : "ec2905ce-7519-4678-bcc4-3d5a27731885",
  "scope" : "profile email" 
}
```

Value token returned -- needs to be decoded?

3. Register the session with keycloak & get identity (Refresh token)

4. Add token to session --> for subsequent request

5. Schedule Refresh

6. Logout handler for Session --> Back to keycloak

You can log out of a web application in multiple ways. For Java EE servlet containers, you can call 
HttpServletRequest.logout(). For other browser applications, you can redirect the browser to
 http://auth-server/auth/realms/{realm-name}/protocol/openid-connect/logout?redirect_uri=encodedRedirectUri, 
 which logs you out if you have an SSO session with your browser.

7. Save session info --> browser


