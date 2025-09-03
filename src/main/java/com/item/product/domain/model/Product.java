package com.item.product.domain.model;

import com.item.product.infrastructure.config.MessageKeys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entidad de dominio Product que representa el modelo de negocio central.
 * Esta es la capa más interna de la arquitectura en cebolla.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private List<String> images;
    private String condition;
    private Integer stock;
    private String category;
    private Map<String, Object> seller;
    private List<Map<String, String>> specifications;
    private String brand;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Valida si el producto tiene stock disponible
     */
    public boolean hasStock() {
        return stock != null && stock > 0;
    }
    
    /**
     * Reduce el stock del producto
     */
    public void reduceStock(int quantity) {
        if (stock != null && stock >= quantity) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Stock insuficiente");
        }
    }
    
    /**
     * Aumenta el stock del producto
     */
    public void increaseStock(int quantity) {
        if (stock == null) {
            stock = 0;
        }
        this.stock += quantity;
    }
    
    /**
     * Valida si el precio es válido
     */
    public boolean isValidPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Valida si el ID del producto es válido
     */
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && id.matches("^[a-zA-Z0-9]+$");
    }
}
