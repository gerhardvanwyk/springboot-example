package org.wyk.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServerProperties.class)
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String... args){
        logger.info("Starting example Application");
        SpringApplication.run(Application.class, args);
    }

}
