package com.gestion.tramites.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // Constructor sin argumentos
    public ResourceNotFoundException() {
        super();
    }

    // Constructor que toma un mensaje general
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor que toma mensaje y causa
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // *** NUEVO CONSTRUCTOR PARA RESOLVER EL ERROR ACTUAL ***
    // Constructor para indicar un recurso no encontrado por un campo y su valor
    public ResourceNotFoundException(String resourceName, String fieldName, Long fieldValue) {
        super(String.format("%s no encontrada con %s : '%s'", resourceName, fieldName, fieldValue));
    }
}