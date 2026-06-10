package br.com.ibmec.researchstars.auth;

import br.com.ibmec.researchstars.professor.dto.ApiErrorResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        var errorsByField = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errorsByField.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var fieldErrors = errorsByField.entrySet().stream()
                .map(error -> new ApiErrorResponse.FieldErrorResponse(error.getKey(), error.getValue()))
                .toList();
        return new ApiErrorResponse(
                Instant.now(),
                400,
                "Dados inválidos",
                fieldErrors
        );
    }
}
