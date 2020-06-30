package org.wyk.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.wyk.application.ErrorHandler;

import java.time.LocalDateTime;

@Data
public class ApiError {

    @JsonIgnore
    private ErrorHandler errorHandler;

    @JsonProperty
    private String status;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty
    private String message;

    @JsonProperty
    //TODO implement a filter here
    @JsonInclude(JsonInclude.Include.CUSTOM)
    private String debugMessage;

    @JsonProperty
    //TODO implement a filter here
    @JsonInclude(JsonInclude.Include.CUSTOM)
    private String exception;

    @JsonProperty
    //TODO implement a filter here
    @JsonInclude(JsonInclude.Include.CUSTOM)
    private String stack;

    @JsonProperty
    //TODO implement a filter here
    @JsonInclude(JsonInclude.Include.CUSTOM)
    private String bindingErrors;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }
    public ApiError(HttpStatus status) {
        this();
        this.status = status.toString();
    }

    public ApiError(HttpStatus status, Throwable ex, ErrorHandler errorHandler) {
        this(status);
        this.errorHandler = errorHandler;


    }

    public ApiError(HttpStatus status, Throwable ex, ErrorHandler errorHandler, String message ) {
        this(status, ex, errorHandler);
        this.message = message;
    }

    public String convertToJson() throws JsonProcessingException {
        if (this == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper.writeValueAsString(this);
    }

    //Setters and getters
}
