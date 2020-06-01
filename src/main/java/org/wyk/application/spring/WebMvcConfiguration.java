package org.wyk.application.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Here we override the defualt web mvc configuration as pulled by @EnableWebMvc
 * @see WebMvcConfigurer it holds default methods to override.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("index");
//        registry.addViewController("/home").setViewName("index");
//        registry.addViewController("/index").setViewName("index");
//        registry.addViewController("/login").setViewName("login");
//    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                //If enabled a method mapped to "/users" also matches to "/users/"
                .setUseTrailingSlashMatch(true);
    }
}
