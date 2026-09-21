package com.centresportifets.athlets_backend.core;

import java.util.Map;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.centresportifets.athlets_backend.email.EmailSendingException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> missing(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erreur", "Ces données sont déjà utilisées ou incompatibles avec les associations existantes."));
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<?> mailFailure() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("erreur", "L'envoi du courriel a échoué. Veuillez réessayer."));
    }
}
