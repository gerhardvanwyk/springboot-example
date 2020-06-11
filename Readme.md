

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
1. Load the (Keyclaok) configuration known as the KeycloakDeployment from the applicaion.yml file and from the Keyclaok
server.
2. Use the GET Token URL endpoint to obtain a AccessTokenResponse
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
