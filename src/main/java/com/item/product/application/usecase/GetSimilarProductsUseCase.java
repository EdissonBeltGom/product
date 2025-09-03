package com.item.product.application.usecase;

import com.item.product.application.dto.ProductResponseDto;
import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;
import com.item.product.domain.model.Product;
import com.item.product.domain.repository.ProductRepository;
import com.item.product.infrastructure.config.MessageKeys;
import com.item.product.infrastructure.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener productos similares basados en categoría y filtros opcionales.
 * Implementa la lógica de negocio para mostrar recomendaciones de productos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetSimilarProductsUseCase {
    
    private final ProductRepository productRepository;
    private final MessageService messageService;
    
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    
    /**
     * Obtiene productos similares al producto especificado
     * @param productId ID del producto original
     * @param maxPrice Precio máximo permitido (opcional)
     * @param limit Número máximo de productos a retornar (opcional, default: 10)
     * @return Lista de productos similares
     */
    public List<ProductResponseDto> getSimilarProducts(String productId, Double maxPrice, Integer limit) {
        log.info(messageService.getLogMessage(MessageKeys.LOG_GETTING_SIMILAR_PRODUCTS, productId));
        
        // Validar ID del producto
        validateProductId(productId);
        
        // Obtener el producto original
        Product originalProduct = getOriginalProduct(productId);
        
        // Validar y establecer límite
        int validatedLimit = validateAndSetLimit(limit);
        
        // Obtener productos similares
        List<Product> similarProducts = productRepository.findSimilarProducts(
            originalProduct.getCategory(),
            productId,
            maxPrice,
            validatedLimit
        );
        
        // Mapear a DTOs
        List<ProductResponseDto> similarProductsDto = similarProducts.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
        
        log.info(messageService.getLogMessage(MessageKeys.LOG_SIMILAR_PRODUCTS_FOUND, 
            String.valueOf(similarProductsDto.size()), productId));
        
        return similarProductsDto;
    }
    
    /**
     * Valida que el ID del producto sea válido
     */
    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            log.error(messageService.getErrorMessage(MessageKeys.ERROR_INVALID_PRODUCT_ID, "null"));
            throw new InvalidProductIdException(messageService.getErrorMessage(MessageKeys.ERROR_INVALID_PRODUCT_ID, "null"));
        }
        
        try {
            // Validar que sea un número válido
            Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            log.error(messageService.getErrorMessage(MessageKeys.ERROR_INVALID_PRODUCT_ID, productId));
            throw new InvalidProductIdException(messageService.getErrorMessage(MessageKeys.ERROR_INVALID_PRODUCT_ID, productId));
        }
    }
    
    /**
     * Obtiene el producto original y valida que exista
     */
    private Product getOriginalProduct(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> {
                log.error(messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND, productId));
                return new ProductNotFoundException(messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND, productId));
            });
    }
    
    /**
     * Valida y establece el límite de productos
     */
    private int validateAndSetLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        
        if (limit <= 0) {
            log.warn(messageService.getValidationMessage(MessageKeys.VALIDATION_LIMIT_INVALID, String.valueOf(limit)));
            return DEFAULT_LIMIT;
        }
        
        if (limit > MAX_LIMIT) {
            log.warn(messageService.getValidationMessage(MessageKeys.VALIDATION_LIMIT_TOO_HIGH, String.valueOf(limit)));
            return MAX_LIMIT;
        }
        
        return limit;
    }
    
    /**
     * Mapea un producto del dominio a DTO de respuesta
     */
    private ProductResponseDto mapToResponseDto(Product product) {
        return ProductResponseDto.builder()
            .id(product.getId())
            .title(product.getTitle())
            .price(product.getPrice())
            .currency(product.getCurrency())
            .description(product.getDescription())
            .images(product.getImages())
            .condition(product.getCondition())
            .stock(product.getStock())
            .category(product.getCategory())
            .seller(product.getSeller())
            .specifications(product.getSpecifications())
            .brand(product.getBrand())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .available(product.getStock() > 0)
            .build();
    }
}

