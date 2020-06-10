package org.wyk.application.spring;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfiguration {

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

}
