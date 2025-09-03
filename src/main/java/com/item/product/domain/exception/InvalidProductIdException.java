package com.item.product.domain.exception;

/**
 * Excepción que se lanza cuando el ID del producto proporcionado no es válido.
 */
public class InvalidProductIdException extends RuntimeException {
    
    private final String invalidId;
    
    public InvalidProductIdException(String id) {
        super("ID de producto inválido: " + id); // Mensaje por defecto
        this.invalidId = id;
    }
    
    /**
     * Obtiene el ID inválido que causó la excepción.
     */
    public String getInvalidId() {
        return invalidId;
    }
}
