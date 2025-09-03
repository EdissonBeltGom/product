package com.item.product.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.item.product.domain.model.Product;
import com.item.product.domain.repository.ProductRepository;
import com.item.product.infrastructure.config.MessageKeys;
import com.item.product.infrastructure.service.MessageService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Adaptador del repositorio que implementa la interfaz del dominio usando archivos JSON.
 * Mapea entre archivos JSON y modelos de dominio.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {
    
    @Value("${app.data.file.path}")
    private String dataFilePath;
    
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @PostConstruct
    public void init() {
        // Configurar ObjectMapper para manejar fechas
        objectMapper.registerModule(new JavaTimeModule());
        
        // Crear directorio si no existe
        try {
            Path path = Paths.get(dataFilePath);
            Files.createDirectories(path.getParent());
            
            // Crear archivo si no existe
            if (!Files.exists(path)) {
                Files.write(path, "[]".getBytes());
                log.info(messageService.getLogMessage(MessageKeys.LOG_FILE_CREATED, dataFilePath));
            }
        } catch (IOException e) {
            log.error(messageService.getLogMessage(MessageKeys.LOG_FILE_INIT_ERROR), e);
            throw new RuntimeException(messageService.getErrorMessage(MessageKeys.ERROR_FILE_INIT), e);
        }
    }
    
    @Override
    public Product save(Product product) {
        lock.writeLock().lock();
        try {
            List<Product> products = readAllProducts();
            Product savedProduct;
            
            if (product.getId() == null) {
                // Nuevo producto
                String newId = generateNewId(products);
                savedProduct = Product.builder()
                        .id(newId)
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
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            } else {
                // Actualizar producto existente
                products.removeIf(p -> p.getId().equals(product.getId()));
                savedProduct = Product.builder()
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
                        .updatedAt(LocalDateTime.now())
                        .build();
            }
            
            products.add(savedProduct);
            writeAllProducts(products);
            
            log.info(messageService.getLogMessage(MessageKeys.LOG_PRODUCT_SAVED, savedProduct.getId()));
            return savedProduct;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    @Retry(name = "repositoryRetry", fallbackMethod = "findByIdFallback")
    public Optional<Product> findById(String id) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Fallback para findById cuando se agotan los reintentos
     */
    public Optional<Product> findByIdFallback(String id, Exception ex) {
        log.warn("Repository retry failed for findById. ID: {}, Error: {}", id, ex.getMessage());
        return Optional.empty();
    }
    
    @Override
    @Retry(name = "repositoryRetry", fallbackMethod = "findAllFallback")
    public List<Product> findAll() {
        lock.readLock().lock();
        try {
            return readAllProducts();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Fallback para findAll cuando se agotan los reintentos
     */
    public List<Product> findAllFallback(Exception ex) {
        log.warn("Repository retry failed for findAll. Error: {}", ex.getMessage());
        return List.of(); // Retorna lista vacía en caso de fallo
    }
    
    @Override
    @Retry(name = "repositoryRetry", fallbackMethod = "findByCategoryFallback")
    public List<Product> findByCategory(String category) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Fallback para findByCategory cuando se agotan los reintentos
     */
    public List<Product> findByCategoryFallback(String category, Exception ex) {
        log.warn("Repository retry failed for findByCategory. Category: {}, Error: {}", category, ex.getMessage());
        return List.of(); // Retorna lista vacía en caso de fallo
    }
    
    @Override
    public List<Product> findByBrand(String brand) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream()
                    .filter(p -> brand.equals(p.getBrand()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<Product> findAvailableProducts() {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream()
                    .filter(Product::hasStock)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            List<Product> products = readAllProducts();
            products.removeIf(p -> p.getId().equals(id));
            writeAllProducts(products);
            log.info(messageService.getLogMessage(MessageKeys.LOG_PRODUCT_DELETED, id));
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean existsById(String id) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream().anyMatch(p -> p.getId().equals(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<Product> findByNameContaining(String name) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            return products.stream()
                    .filter(p -> p.getTitle() != null && 
                            p.getTitle().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    @Retry(name = "repositoryRetry", fallbackMethod = "findSimilarProductsFallback")
    public List<Product> findSimilarProducts(String category, String excludeId, Double maxPrice, int limit) {
        lock.readLock().lock();
        try {
            List<Product> products = readAllProducts();
            
            return products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().equals(category))
                    .filter(p -> !p.getId().equals(excludeId))
                    .filter(p -> maxPrice == null || p.getPrice().doubleValue() <= maxPrice)
                    .limit(limit)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Fallback para findSimilarProducts cuando se agotan los reintentos
     */
    public List<Product> findSimilarProductsFallback(String category, String excludeId, Double maxPrice, int limit, Exception ex) {
        log.warn("Repository retry failed for findSimilarProducts. Category: {}, ExcludeId: {}, Error: {}", category, excludeId, ex.getMessage());
        return List.of(); // Retorna lista vacía en caso de fallo
    }
    
    /**
     * Genera un nuevo ID único para productos
     */
    private String generateNewId(List<Product> products) {
        int maxId = products.stream()
                .mapToInt(p -> {
                    try {
                        return Integer.parseInt(p.getId());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.valueOf(maxId + 1);
    }
    
    /**
     * Lee todos los productos del archivo JSON
     */
    private List<Product> readAllProducts() {
        try {
            File file = new File(dataFilePath);
            if (!file.exists() || file.length() == 0) {
                return List.of();
            }
            
            String content = Files.readString(file.toPath());
            return objectMapper.readValue(content, new TypeReference<List<Product>>() {});
        } catch (IOException e) {
            log.error(messageService.getErrorMessage(MessageKeys.ERROR_FILE_READ), e);
            return List.of();
        }
    }
    
    /**
     * Escribe todos los productos al archivo JSON
     */
    private void writeAllProducts(List<Product> products) {
        try {
            String content = objectMapper.writeValueAsString(products);
            Files.write(Paths.get(dataFilePath), content.getBytes());
        } catch (IOException e) {
            log.error(messageService.getErrorMessage(MessageKeys.ERROR_FILE_WRITE_PRODUCTS), e);
            throw new RuntimeException(messageService.getErrorMessage(MessageKeys.ERROR_FILE_WRITE), e);
        }
    }
}
