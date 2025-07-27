package com.gestion.tramites.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Puedes añadir más manejadores de excepciones aquí si es necesario
    // Por ejemplo, para DataIntegrityViolationException (claves duplicadas, etc.)
    // @ExceptionHandler(DataIntegrityViolationException.class)
    // public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    //    Map<String, String> errors = new HashMap<>();
    //    errors.put("error", "Error de integridad de datos: " + ex.getRootCause().getMessage());
    //    return new ResponseEntity<>(errors, HttpStatus.CONFLICT); // 409 Conflict
    // }
}
