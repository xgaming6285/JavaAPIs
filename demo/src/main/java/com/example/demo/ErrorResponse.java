package com.example.demo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}