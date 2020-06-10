package org.wyk.application.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Here we override the defualt web mvc configuration as pulled by @EnableWebMvc
 * @see WebMvcConfigurer it holds default methods to override.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                //If enabled a method mapped to "/users" also matches to "/users/"
                .setUseTrailingSlashMatch(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //A request with the pattern '/*.groovy' -> location for the physical resource can be found at the root of the classpath.
        registry.addResourceHandler("/*.groovy").addResourceLocations("classpath:");

    }
}
