package com.item.product.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para las respuestas de productos en la capa de aplicación.
 * Define la estructura de datos para devolver información de productos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta de un producto")
public class ProductResponseDto {
    
    @Schema(description = "Identificador único del producto", example = "1")
    private String id;
    
    @Schema(description = "Título del producto", example = "Laptop Gaming Pro")
    private String title;
    
    @Schema(description = "Precio del producto", example = "1299.99")
    private BigDecimal price;
    
    @Schema(description = "Moneda del precio", example = "USD")
    private String currency;
    
    @Schema(description = "Descripción detallada del producto", example = "Laptop para gaming de alto rendimiento con las últimas tecnologías")
    private String description;
    
    @Schema(description = "Arreglo de URLs de imágenes del producto", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;
    
    @Schema(description = "Condición del producto", example = "Nuevo")
    private String condition;
    
    @Schema(description = "Cantidad disponible en stock", example = "15")
    private Integer stock;
    
    @Schema(description = "Categoría del producto", example = "Electrónicos")
    private String category;
    
    @Schema(description = "Información del vendedor", example = "{\"id\": \"seller123\", \"name\": \"TechStore\", \"rating\": 4.5}")
    private Map<String, Object> seller;
    
    @Schema(description = "Especificaciones técnicas del producto", example = "[{\"name\": \"Procesador\", \"value\": \"Intel i7\"}, {\"name\": \"RAM\", \"value\": \"16GB\"}]")
    private List<Map<String, String>> specifications;
    
    @Schema(description = "Marca del producto", example = "GamingPro")
    private String brand;
    
    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Indica si el producto está disponible (tiene stock)", example = "true")
    private boolean available;
}
