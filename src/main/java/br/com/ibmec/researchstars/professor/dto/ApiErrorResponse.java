package br.com.ibmec.researchstars.professor.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        List<FieldErrorResponse> fieldErrors
) {

    public ApiErrorResponse(Instant timestamp, int status, String message) {
        this(timestamp, status, message, List.of());
    }

    public record FieldErrorResponse(String field, String message) {
    }
}
