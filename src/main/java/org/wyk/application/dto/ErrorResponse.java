package org.wyk.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 {
 "error": "invalid_grant",
 "error_description": "Account is not fully set up"
 }
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    @JsonProperty
    private String error;

    @JsonProperty
    private String error_description;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer httpStatus;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String httpReason;
}
