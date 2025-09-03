package com.item.product.application.usecase;

import com.item.product.application.dto.ProductResponseDto;
import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;
import com.item.product.domain.model.Product;
import com.item.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener productos.
 * Orquesta la lógica de negocio para obtener productos.
 */
@Service
@RequiredArgsConstructor
public class GetProductUseCase {
    
    private final ProductRepository productRepository;
    
    /**
     * Obtiene un producto por su ID
     */
    public ProductResponseDto getById(String id) {
        // Validar que el ID sea válido
        if (!Product.isValidId(id)) {
            throw new InvalidProductIdException(id);
        }
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        return mapToResponseDto(product);
    }
    
    /**
     * Obtiene todos los productos
     */
    public List<ProductResponseDto> getAll() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene productos por categoría
     */
    public List<ProductResponseDto> getByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene productos disponibles (con stock)
     */
    public List<ProductResponseDto> getAvailableProducts() {
        List<Product> products = productRepository.findAvailableProducts();
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca productos por nombre
     */
    public List<ProductResponseDto> searchByName(String name) {
        List<Product> products = productRepository.findByNameContaining(name);
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapea el producto de dominio a DTO de respuesta
     */
    private ProductResponseDto mapToResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .images(product.getImages())
                .condition(product.getCondition())
                .stock(product.getStock())
                .category(product.getCategory())
                .seller(product.getSeller())
                .specifications(product.getSpecifications())
                .brand(product.getBrand())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .available(product.hasStock())
                .build();
    }
}

