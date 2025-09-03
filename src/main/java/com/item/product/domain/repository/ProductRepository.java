package com.item.product.domain.repository;

import com.item.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de dominio que define los contratos para acceder a los datos.
 * Esta interfaz pertenece a la capa de dominio y no depende de tecnologías específicas.
 */
public interface ProductRepository {
    
    /**
     * Guarda un producto
     */
    Product save(Product product);
    
    /**
     * Busca un producto por su ID
     */
    Optional<Product> findById(String id);
    
    /**
     * Busca todos los productos
     */
    List<Product> findAll();
    
    /**
     * Busca productos por categoría
     */
    List<Product> findByCategory(String category);
    
    /**
     * Busca productos por marca
     */
    List<Product> findByBrand(String brand);
    
    /**
     * Busca productos que tengan stock disponible
     */
    List<Product> findAvailableProducts();
    
    /**
     * Elimina un producto por su ID
     */
    void deleteById(String id);
    
    /**
     * Verifica si existe un producto con el ID especificado
     */
    boolean existsById(String id);
    
    /**
     * Busca productos por nombre (búsqueda parcial)
     */
    List<Product> findByNameContaining(String name);
    
    /**
     * Busca productos similares basados en categoría y filtros opcionales
     * @param category Categoría del producto original
     * @param excludeId ID del producto a excluir
     * @param maxPrice Precio máximo permitido (opcional)
     * @param limit Número máximo de productos a retornar
     * @return Lista de productos similares
     */
    List<Product> findSimilarProducts(String category, String excludeId, Double maxPrice, int limit);
}
