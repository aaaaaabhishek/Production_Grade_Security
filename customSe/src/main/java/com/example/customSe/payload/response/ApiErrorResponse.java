package com.example.customSe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;
    private String correlationId;
    private String error;
    private String message;
    private String path;
    private int status;
    public ApiErrorResponse(String correlationId, String error, String message, String path, int status) {
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.error = error;
        this.message = message;
        this.path = path;
        this.status = status;
    }
}
