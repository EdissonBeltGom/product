package com.item.product.infrastructure.controller;

import com.item.product.application.dto.ProductResponseDto;
import com.item.product.infrastructure.service.ProductService;
import com.item.product.infrastructure.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;

/**
 * Controlador REST que expone los endpoints de consulta de productos.
 * Utiliza el servicio con Circuit Breaker y Rate Limiting para mayor resiliencia.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Productos", description = "API para consulta de productos")
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final RateLimitService rateLimitService;
    
    /**
     * Obtiene un producto por su ID con Circuit Breaker y Rate Limiting
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalles completos de un producto",
        description = "Obtiene todos los detalles de un producto específico por su ID para mostrar en la página de detalle"
    )
     @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponseDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "id": "1",
                      "title": "Laptop Gaming Pro",
                      "price": 1299.99,
                      "currency": "USD",
                      "description": "Laptop para gaming de alto rendimiento con RTX 4070 y las últimas tecnologías de refrigeración",
                      "images": [
                        "https://example.com/laptop-gaming-1.jpg",
                        "https://example.com/laptop-gaming-2.jpg",
                        "https://example.com/laptop-gaming-3.jpg"
                      ],
                      "condition": "Nuevo",
                      "stock": 15,
                      "category": "Electrónicos",
                      "seller": {
                        "id": "seller123",
                        "name": "TechStore",
                        "rating": 4.8,
                        "reviews": 1250
                      },
                      "specifications": [
                        {
                          "name": "Procesador",
                          "value": "Intel Core i7-13700H"
                        },
                        {
                          "name": "RAM",
                          "value": "16GB DDR5"
                        },
                        {
                          "name": "Almacenamiento",
                          "value": "512GB SSD NVMe"
                        },
                        {
                          "name": "Tarjeta Gráfica",
                          "value": "NVIDIA RTX 4070 8GB"
                        },
                        {
                          "name": "Pantalla",
                          "value": "15.6 - Full HD 144Hz"
                        }
                      ],
                      "brand": "GamingPro",
                      "createdAt": "2024-01-15T10:30:00",
                      "updatedAt": "2024-01-15T10:30:00",
                      "available": true
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00",
                      "status": 404,
                      "error": "Producto no encontrado",
                      "message": "Producto no encontrado con ID: 999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de producto inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00",
                      "status": 400,
                      "error": "ID de producto inválido",
                      "message": "ID de producto inválido: invalid-id"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Límite de solicitudes alcanzado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00",
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Has alcanzado el límite de solicitudes. Intenta nuevamente en 1 minuto."
                    }
                    """
                )
            )
        )
    })
    public CompletableFuture<ResponseEntity<ProductResponseDto>> getProductById(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable String id,
            HttpServletRequest request) {
        
        String userIp = getClientIpAddress(request);
        log.info("Solicitud recibida para obtener producto con ID: {} desde IP: {}", id, userIp);
        
        // Verificar rate limiting por IP
        if (!rateLimitService.isAllowedByIp(userIp)) {
            log.warn("Rate limit alcanzado para IP: {}", userIp);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After", "60")
                    .body(ProductResponseDto.builder()
                        .id(id)
                        .title("Límite de solicitudes alcanzado")
                        .description("Has excedido el límite de solicitudes. Intenta nuevamente en 1 minuto.")
                        .price(java.math.BigDecimal.ZERO)
                        .currency("USD")
                        .condition("N/A")
                        .stock(0)
                        .category("N/A")
                        .seller(null)
                        .available(false)
                        .build())
            );
        }
        
        return productService.getProductById(id)
            .thenApply(product -> {
                log.info("Producto obtenido exitosamente: {}", product.getId());
                return ResponseEntity.ok(product);
            })
            .exceptionally(throwable -> {
                log.error("Error al obtener producto con ID {}: {}", id, throwable.getMessage());
                // Extraer la excepción original del CompletionException
                Throwable cause = throwable.getCause();
                if (cause instanceof ProductNotFoundException) {
                    throw new ProductNotFoundException(((ProductNotFoundException) cause).getProductId());
                } else if (cause instanceof InvalidProductIdException) {
                    throw new InvalidProductIdException(((InvalidProductIdException) cause).getInvalidId());
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException("Error al obtener producto", throwable);
                }
            });
    }
    
    /**
     * Obtiene todos los productos con Circuit Breaker y Rate Limiting
     */
    @GetMapping
    @Operation(
        summary = "Obtener todos los productos",
        description = "Obtiene la lista completa de productos"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de productos obtenida exitosamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProductResponseDto.class)
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "Límite de solicitudes alcanzado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class)
        )
    )
    public CompletableFuture<ResponseEntity<List<ProductResponseDto>>> getAllProducts(HttpServletRequest request) {
        String userIp = getClientIpAddress(request);
        log.info("Solicitud recibida para obtener todos los productos desde IP: {}", userIp);
        
        // Verificar rate limiting por IP
        if (!rateLimitService.isAllowedByIp(userIp)) {
            log.warn("Rate limit alcanzado para IP: {}", userIp);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After", "60")
                    .body(List.of(ProductResponseDto.builder()
                        .id("rate-limit-1")
                        .title("Límite de solicitudes alcanzado")
                        .description("Has excedido el límite de solicitudes. Intenta nuevamente en 1 minuto.")
                        .price(java.math.BigDecimal.ZERO)
                        .currency("USD")
                        .condition("N/A")
                        .stock(0)
                        .category("Sistema")
                        .seller(null)
                        .available(false)
                        .build()))
            );
        }
        
        return productService.getAllProducts()
            .thenApply(products -> {
                log.info("Se obtuvieron {} productos exitosamente", products.size());
                return ResponseEntity.ok(products);
            })
            .exceptionally(throwable -> {
                log.error("Error al obtener todos los productos: {}", throwable.getMessage());
                // Extraer la excepción original del CompletionException
                Throwable cause = throwable.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException("Error al obtener productos", throwable);
                }
            });
    }
    
    /**
     * Obtiene productos por categoría con Circuit Breaker y Rate Limiting
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Obtener productos por categoría",
        description = "Obtiene todos los productos de una categoría específica"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de productos de la categoría",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProductResponseDto.class)
        )
    )
    @ApiResponse(
        responseCode = "429",
        description = "Límite de solicitudes alcanzado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class)
        )
    )
    public CompletableFuture<ResponseEntity<List<ProductResponseDto>>> getProductsByCategory(
            @Parameter(description = "Categoría de productos", required = true, example = "Electrónicos")
            @PathVariable String category,
            HttpServletRequest request) {
        
        String userIp = getClientIpAddress(request);
        log.info("Solicitud recibida para obtener productos de la categoría: {} desde IP: {}", category, userIp);
        
        // Verificar rate limiting por IP
        if (!rateLimitService.isAllowedByIp(userIp)) {
            log.warn("Rate limit alcanzado para IP: {}", userIp);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After", "60")
                    .body(List.of(ProductResponseDto.builder()
                        .id("rate-limit-category-" + category)
                        .title("Límite de solicitudes alcanzado")
                        .description("Has excedido el límite de solicitudes. Intenta nuevamente en 1 minuto.")
                        .price(java.math.BigDecimal.ZERO)
                        .currency("USD")
                        .condition("N/A")
                        .stock(0)
                        .category(category)
                        .seller(null)
                        .available(false)
                        .build()))
            );
        }
        
        return productService.getProductsByCategory(category)
            .thenApply(products -> {
                log.info("Se obtuvieron {} productos de la categoría {}", products.size(), category);
                return ResponseEntity.ok(products);
            })
            .exceptionally(throwable -> {
                log.error("Error al obtener productos de la categoría {}: {}", category, throwable.getMessage());
                // Extraer la excepción original del CompletionException
                Throwable cause = throwable.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException("Error al obtener productos por categoría", throwable);
                }
            });
    }
    
    /**
     * Obtiene productos similares con Circuit Breaker y Rate Limiting
     */
    @GetMapping("/{id}/similar")
    @Operation(
        summary = "Obtener productos similares",
        description = "Obtiene productos similares basados en la categoría del producto original, " +
                     "excluyendo el producto original y aplicando filtros opcionales"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos similares obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de producto inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Límite de solicitudes alcanzado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.item.product.infrastructure.config.ErrorResponse.class)
            )
        )
    })
    public CompletableFuture<ResponseEntity<List<ProductResponseDto>>> getSimilarProducts(
            @Parameter(description = "ID del producto original", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "Precio máximo permitido para los productos similares", required = false, example = "1500.0")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Número máximo de productos a retornar (default: 10, max: 50)", required = false, example = "5")
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        
        String userIp = getClientIpAddress(request);
        log.info("Solicitud recibida para obtener productos similares del producto: {} desde IP: {}", id, userIp);
        
        // Verificar rate limiting por IP (más restrictivo para productos similares)
        if (!rateLimitService.isAllowedByIp(userIp)) {
            log.warn("Rate limit alcanzado para IP: {}", userIp);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After", "60")
                    .body(List.of(ProductResponseDto.builder()
                        .id("rate-limit-similar-" + id)
                        .title("Límite de solicitudes alcanzado")
                        .description("Has excedido el límite de solicitudes para recomendaciones. Intenta nuevamente en 1 minuto.")
                        .price(java.math.BigDecimal.ZERO)
                        .currency("USD")
                        .condition("N/A")
                        .stock(0)
                        .category("N/A")
                        .seller(null)
                        .available(false)
                        .build()))
            );
        }
        
        return productService.getSimilarProducts(id, maxPrice, limit)
            .thenApply(similarProducts -> {
                log.info("Se obtuvieron {} productos similares para el producto {}", similarProducts.size(), id);
                return ResponseEntity.ok(similarProducts);
            })
            .exceptionally(throwable -> {
                log.error("Error al obtener productos similares del producto {}: {}", id, throwable.getMessage());
                // Extraer la excepción original del CompletionException
                Throwable cause = throwable.getCause();
                if (cause instanceof ProductNotFoundException) {
                    throw new ProductNotFoundException(((ProductNotFoundException) cause).getProductId());
                } else if (cause instanceof InvalidProductIdException) {
                    throw new InvalidProductIdException(((InvalidProductIdException) cause).getInvalidId());
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException("Error al obtener productos similares", throwable);
                }
            });
    }
    
    /**
     * Obtiene información de Rate Limiting para la IP del cliente
     */
    @GetMapping("/rate-limit-info")
    @Operation(
        summary = "Obtener información de Rate Limiting",
        description = "Obtiene información sobre el estado del Rate Limiting para la IP del cliente"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Información de Rate Limiting",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RateLimitService.RateLimitInfo.class)
        )
    )
    public ResponseEntity<RateLimitService.RateLimitInfo> getRateLimitInfo(HttpServletRequest request) {
        String userIp = getClientIpAddress(request);
        RateLimitService.RateLimitInfo info = rateLimitService.getIpRateLimitInfo(userIp);
        log.info("Información de Rate Limiting solicitada para IP: {}", userIp);
        return ResponseEntity.ok(info);
    }
    
    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
