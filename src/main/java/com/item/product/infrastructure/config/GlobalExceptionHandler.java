package com.item.product.infrastructure.config;

import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;
import com.item.product.infrastructure.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para manejar errores de manera consistente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final MessageService messageService;
    
    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }
    
    /**
     * Maneja excepciones de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_VALIDATION))
                .message(messageService.getErrorMessage(MessageKeys.ERROR_VALIDATION_INVALID_DATA))
                .details(errors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Maneja excepciones cuando no se encuentra un producto
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_PRODUCT_NOT_FOUND))
                .message(messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND, ex.getProductId()))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Maneja excepciones cuando el ID del producto es inválido
     */
    @ExceptionHandler(InvalidProductIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductIdException(InvalidProductIdException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_PRODUCT_INVALID_ID))
                .message(messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_INVALID_ID, ex.getInvalidId()))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Maneja excepciones de argumentos ilegales
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_ARGUMENT))
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Maneja excepciones generales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_INTERNAL_SERVER))
                .message(messageService.getErrorMessage(MessageKeys.ERROR_UNEXPECTED))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
