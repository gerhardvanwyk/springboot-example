

## HTTP Request
The request url will first be matched againts a security configuration in org.wyk.application.spring.KeycloakWebSecurityConfiguration.configure
if permission is allowed, either by being authenticated already or that the request match an anonomous or un authenticated pattern
it will be allowed through.

Next it will be check by if it matches any of the MVC path or resource handlers patterns. org.wyk.application.spring.WebMvcConfiguration
