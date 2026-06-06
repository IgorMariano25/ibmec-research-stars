package br.com.ibmec.researchstars.professor.dto;

import java.time.Instant;

public record ApiErrorResponse(Instant timestamp, int status, String message) {
}
