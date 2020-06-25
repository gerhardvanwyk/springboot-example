package org.wyk.application.spring;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Here we override the defualt web mvc configuration as pulled by @EnableWebMvc
 * @see WebMvcConfigurer it holds default methods to override.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Bean
    public CloseableHttpClient clientConnectionManager() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        //Set the maximum number of total open connections.
        connectionManager.setMaxTotal(10);
        //Set the maximum number of concurrent connections per route, which is 2 by default.
        connectionManager.setDefaultMaxPerRoute(4);
        //10 seconds for a stale connection
        connectionManager.setValidateAfterInactivity(10_000);

        return HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(5_000)
                        .setSocketTimeout(10_000)
                        .build()

                )
                .setConnectionManager(connectionManager)
                .build();

    }

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
