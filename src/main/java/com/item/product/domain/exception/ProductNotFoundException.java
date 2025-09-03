package com.item.product.domain.exception;

/**
 * Excepción que se lanza cuando no se encuentra un producto con el ID especificado.
 */
public class ProductNotFoundException extends RuntimeException {
    
    private final String productId;
    
    public ProductNotFoundException(String id) {
        super("Producto no encontrado con ID: " + id); // Mensaje por defecto
        this.productId = id;
    }
    
    /**
     * Obtiene el ID del producto que no se encontró.
     */
    public String getProductId() {
        return productId;
    }
}
