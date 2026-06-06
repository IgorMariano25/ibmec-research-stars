package br.com.ibmec.researchstars.professor;

import br.com.ibmec.researchstars.professor.dto.ApiErrorResponse;
import br.com.ibmec.researchstars.professor.exception.ProfessorConflictException;
import br.com.ibmec.researchstars.professor.exception.ProfessorForbiddenException;
import br.com.ibmec.researchstars.professor.exception.ProfessorIntegrationException;
import br.com.ibmec.researchstars.professor.exception.ProfessorNotFoundException;
import br.com.ibmec.researchstars.professor.exception.ProfessorStateException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ProfessorController.class)
public class ProfessorExceptionHandler {

    @ExceptionHandler(ProfessorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(ProfessorNotFoundException exception) {
        return new ApiErrorResponse(Instant.now(), 404, exception.getMessage());
    }

    @ExceptionHandler(ProfessorConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleConflict(ProfessorConflictException exception) {
        return new ApiErrorResponse(Instant.now(), 409, exception.getMessage());
    }

    @ExceptionHandler(ProfessorStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleState(ProfessorStateException exception) {
        return new ApiErrorResponse(Instant.now(), 400, exception.getMessage());
    }

    @ExceptionHandler(ProfessorForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleForbidden(ProfessorForbiddenException exception) {
        return new ApiErrorResponse(Instant.now(), 403, exception.getMessage());
    }

    @ExceptionHandler(ProfessorIntegrationException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiErrorResponse handleIntegration(ProfessorIntegrationException exception) {
        return new ApiErrorResponse(Instant.now(), 502, exception.getMessage());
    }
}
