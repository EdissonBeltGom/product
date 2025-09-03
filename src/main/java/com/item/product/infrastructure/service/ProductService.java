package com.item.product.infrastructure.service;

import com.item.product.application.dto.ProductResponseDto;
import com.item.product.application.usecase.GetSimilarProductsUseCase;
import com.item.product.application.usecase.GetProductUseCase;
import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;
import com.item.product.infrastructure.config.MessageKeys;
import com.item.product.infrastructure.service.MessageService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de productos con patrones de resiliencia implementados.
 * Utiliza Circuit Breaker, Rate Limiting, Retry y Timeout para manejar fallos de manera elegante.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final GetProductUseCase getProductUseCase;
    private final GetSimilarProductsUseCase getSimilarProductsUseCase;
    private final MessageService messageService;
    private final RateLimitService rateLimitService;
    
    /**
     * Obtiene un producto por ID con Circuit Breaker y Rate Limiting
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @RateLimiter(name = "productEndpoint", fallbackMethod = "rateLimitFallback")
    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @TimeLimiter(name = "productService", fallbackMethod = "getProductFallback")
    public CompletableFuture<ProductResponseDto> getProductById(String id) {
        log.info("Obteniendo producto con ID: {}", id);
        
        try {
            ProductResponseDto product = getProductUseCase.getById(id);
            log.info("Producto obtenido exitosamente: {}", product.getId());
            return CompletableFuture.completedFuture(product);
        } catch (ProductNotFoundException | InvalidProductIdException e) {
            // Propagar excepciones específicas del dominio
            log.error("Error de dominio al obtener producto con ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener producto", e);
        }
    }
    
    /**
     * Obtiene todos los productos con Circuit Breaker y Rate Limiting
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getAllProductsFallback")
    @RateLimiter(name = "userRateLimit", fallbackMethod = "rateLimitFallback")
    @Retry(name = "productService", fallbackMethod = "getAllProductsFallback")
    @TimeLimiter(name = "productService", fallbackMethod = "getAllProductsFallback")
    public CompletableFuture<List<ProductResponseDto>> getAllProducts() {
        log.info("Obteniendo todos los productos");
        
        try {
            List<ProductResponseDto> products = getProductUseCase.getAll();
            log.info("Se obtuvieron {} productos exitosamente", products.size());
            return CompletableFuture.completedFuture(products);
        } catch (Exception e) {
            log.error("Error al obtener todos los productos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos", e);
        }
    }
    
    /**
     * Obtiene productos por categoría con Circuit Breaker y Rate Limiting
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsByCategoryFallback")
    @RateLimiter(name = "searchRateLimit", fallbackMethod = "rateLimitFallback")
    @Retry(name = "productService", fallbackMethod = "getProductsByCategoryFallback")
    @TimeLimiter(name = "productService", fallbackMethod = "getProductsByCategoryFallback")
    public CompletableFuture<List<ProductResponseDto>> getProductsByCategory(String category) {
        log.info("Obteniendo productos por categoría: {}", category);
        
        try {
            List<ProductResponseDto> products = getProductUseCase.getByCategory(category);
            log.info("Se obtuvieron {} productos de la categoría {}", products.size(), category);
            return CompletableFuture.completedFuture(products);
        } catch (Exception e) {
            log.error("Error al obtener productos por categoría {}: {}", category, e.getMessage());
            throw new RuntimeException("Error al obtener productos por categoría", e);
        }
    }
    
    /**
     * Obtiene productos similares con Circuit Breaker y Rate Limiting
     */
    @CircuitBreaker(name = "similarProductsService", fallbackMethod = "getSimilarProductsFallback")
    @RateLimiter(name = "similarProductsRateLimit", fallbackMethod = "rateLimitFallback")
    @Retry(name = "similarProductsService", fallbackMethod = "getSimilarProductsFallback")
    @TimeLimiter(name = "similarProductsService", fallbackMethod = "getSimilarProductsFallback")
    public CompletableFuture<List<ProductResponseDto>> getSimilarProducts(String productId, Double maxPrice, Integer limit) {
        log.info("Obteniendo productos similares para el producto: {}", productId);
        
        try {
            List<ProductResponseDto> similarProducts = getSimilarProductsUseCase.getSimilarProducts(productId, maxPrice, limit);
            log.info("Se obtuvieron {} productos similares para el producto {}", similarProducts.size(), productId);
            return CompletableFuture.completedFuture(similarProducts);
        } catch (ProductNotFoundException | InvalidProductIdException e) {
            // Propagar excepciones específicas del dominio
            log.error("Error de dominio al obtener productos similares para el producto {}: {}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener productos similares para el producto {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error al obtener productos similares", e);
        }
    }
    
    // =============================================================================
    // MÉTODOS DE FALLBACK PARA CIRCUIT BREAKER
    // =============================================================================
    
    /**
     * Fallback para obtener un producto individual
     */
    public CompletableFuture<ProductResponseDto> getProductFallback(String id, Exception ex) {
        log.warn("Circuit Breaker activado para getProductById. ID: {}, Error: {}", id, ex.getMessage());
        
        return CompletableFuture.completedFuture(
            ProductResponseDto.builder()
                .id(id)
                .title("Producto temporalmente no disponible")
                .description("El servicio de productos está experimentando problemas técnicos. Por favor, intente más tarde.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category("N/A")
                .seller(null)
                .available(false)
                .build()
        );
    }
    
    /**
     * Fallback para obtener todos los productos
     */
    public CompletableFuture<List<ProductResponseDto>> getAllProductsFallback(Exception ex) {
        log.warn("Circuit Breaker activado para getAllProducts. Error: {}", ex.getMessage());
        
        return CompletableFuture.completedFuture(List.of(
            ProductResponseDto.builder()
                .id("fallback-1")
                .title("Servicio temporalmente no disponible")
                .description("El servicio de productos está experimentando problemas técnicos. Por favor, intente más tarde.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category("Sistema")
                .seller(null)
                .available(false)
                .build()
        ));
    }
    
    /**
     * Fallback para obtener productos por categoría
     */
    public CompletableFuture<List<ProductResponseDto>> getProductsByCategoryFallback(String category, Exception ex) {
        log.warn("Circuit Breaker activado para getProductsByCategory. Categoría: {}, Error: {}", category, ex.getMessage());
        
        return CompletableFuture.completedFuture(List.of(
            ProductResponseDto.builder()
                .id("fallback-category-" + category)
                .title("Productos de " + category + " temporalmente no disponibles")
                .description("El servicio de productos está experimentando problemas técnicos. Por favor, intente más tarde.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category(category)
                .seller(null)
                .available(false)
                .build()
        ));
    }
    
    /**
     * Fallback para obtener productos similares
     */
    public CompletableFuture<List<ProductResponseDto>> getSimilarProductsFallback(String productId, Double maxPrice, Integer limit, Exception ex) {
        log.warn("Circuit Breaker activado para getSimilarProducts. Producto: {}, Error: {}", productId, ex.getMessage());
        
        return CompletableFuture.completedFuture(List.of(
            ProductResponseDto.builder()
                .id("fallback-similar-" + productId)
                .title("Productos similares temporalmente no disponibles")
                .description("El servicio de recomendaciones está experimentando problemas técnicos. Por favor, intente más tarde.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category("N/A")
                .seller(null)
                .available(false)
                .build()
        ));
    }
    
    // =============================================================================
    // MÉTODOS DE FALLBACK PARA RATE LIMITING
    // =============================================================================
    
    /**
     * Fallback para Rate Limiting en productos individuales
     */
    public CompletableFuture<ProductResponseDto> rateLimitFallback(String id, Exception ex) {
        log.warn("Rate limit alcanzado para getProductById. ID: {}", id);
        
        return CompletableFuture.completedFuture(
            ProductResponseDto.builder()
                .id(id)
                .title("Límite de solicitudes alcanzado")
                .description("Has alcanzado el límite de solicitudes para este endpoint. Por favor, espera un momento antes de intentar nuevamente.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category("N/A")
                .seller(null)
                .available(false)
                .build()
        );
    }
    
    /**
     * Fallback para Rate Limiting en listas de productos
     */
    public CompletableFuture<List<ProductResponseDto>> rateLimitFallback(Exception ex) {
        log.warn("Rate limit alcanzado para getAllProducts");
        
        return CompletableFuture.completedFuture(List.of(
            ProductResponseDto.builder()
                .id("rate-limit-1")
                .title("Límite de solicitudes alcanzado")
                .description("Has alcanzado el límite de solicitudes para este endpoint. Por favor, espera un momento antes de intentar nuevamente.")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .condition("N/A")
                .stock(0)
                .category("Sistema")
                .seller(null)
                .available(false)
                .build()
        ));
    }
}
