package org.wyk.application.spring;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

public class CustomRestTemplate extends RestTemplate {

    @Override
    protected void handleResponse(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        ResponseErrorHandler errorHandler = getErrorHandler();
        boolean hasError = errorHandler.hasError(response);
        if (logger.isDebugEnabled()) {
            try {
                int code = response.getRawStatusCode();
                HttpStatus status = HttpStatus.resolve(code);
                if (hasError) {
                    logger.error("Error Response " + (status != null ? status : code));
                }else{
                    logger.debug("Response " + (status != null ? status : code));
                }
            }
            catch (IOException ex) {
                // ignore
            }
        }
    }
}
