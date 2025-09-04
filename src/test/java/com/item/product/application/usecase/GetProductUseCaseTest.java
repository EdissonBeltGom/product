package com.item.product.application.usecase;

import com.item.product.application.dto.ProductResponseDto;
import com.item.product.domain.exception.InvalidProductIdException;
import com.item.product.domain.exception.ProductNotFoundException;
import com.item.product.domain.model.Product;
import com.item.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductUseCase Tests")
class GetProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetProductUseCase getProductUseCase;

    private Product testProduct;
    private ProductResponseDto expectedDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("PROD123")
                .title("Laptop Gaming Pro")
                .description("Laptop para gaming de alto rendimiento")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .images(Arrays.asList("image1.jpg", "image2.jpg"))
                .condition("Nuevo")
                .stock(15)
                .category("Electrónicos")
                .seller(Map.of("id", "seller123", "name", "TechStore"))
                .specifications(Arrays.asList(
                        Map.of("name", "Procesador", "value", "Intel i7"),
                        Map.of("name", "RAM", "value", "16GB")
                ))
                .brand("GamingPro")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();

        expectedDto = ProductResponseDto.builder()
                .id("PROD123")
                .title("Laptop Gaming Pro")
                .description("Laptop para gaming de alto rendimiento")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .images(Arrays.asList("image1.jpg", "image2.jpg"))
                .condition("Nuevo")
                .stock(15)
                .category("Electrónicos")
                .seller(Map.of("id", "seller123", "name", "TechStore"))
                .specifications(Arrays.asList(
                        Map.of("name", "Procesador", "value", "Intel i7"),
                        Map.of("name", "RAM", "value", "16GB")
                ))
                .brand("GamingPro")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Debería retornar producto cuando ID es válido y producto existe")
        void shouldReturnProductWhenValidIdAndProductExists() {
            // Arrange
            String validId = "PROD123";
            when(productRepository.findById(validId)).thenReturn(Optional.of(testProduct));

            // Act
            ProductResponseDto result = getProductUseCase.getById(validId);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDto.getId(), result.getId());
            assertEquals(expectedDto.getTitle(), result.getTitle());
            assertEquals(expectedDto.getPrice(), result.getPrice());
            assertEquals(expectedDto.isAvailable(), result.isAvailable());
            verify(productRepository).findById(validId);
        }

        @Test
        @DisplayName("Debería lanzar InvalidProductIdException cuando ID es null")
        void shouldThrowInvalidProductIdExceptionWhenIdIsNull() {
            // Act & Assert
            InvalidProductIdException exception = assertThrows(
                    InvalidProductIdException.class,
                    () -> getProductUseCase.getById(null)
            );
            
            assertEquals("ID de producto inválido: null", exception.getMessage());
            assertEquals(null, exception.getInvalidId());
            verify(productRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Debería lanzar InvalidProductIdException cuando ID está vacío")
        void shouldThrowInvalidProductIdExceptionWhenIdIsEmpty() {
            // Act & Assert
            InvalidProductIdException exception = assertThrows(
                    InvalidProductIdException.class,
                    () -> getProductUseCase.getById("")
            );
            
            assertEquals("ID de producto inválido: ", exception.getMessage());
            assertEquals("", exception.getInvalidId());
            verify(productRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Debería lanzar InvalidProductIdException cuando ID contiene caracteres especiales")
        void shouldThrowInvalidProductIdExceptionWhenIdContainsSpecialCharacters() {
            // Act & Assert
            InvalidProductIdException exception = assertThrows(
                    InvalidProductIdException.class,
                    () -> getProductUseCase.getById("PROD-123")
            );
            
            assertEquals("ID de producto inválido: PROD-123", exception.getMessage());
            assertEquals("PROD-123", exception.getInvalidId());
            verify(productRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Debería lanzar InvalidProductIdException cuando ID contiene espacios")
        void shouldThrowInvalidProductIdExceptionWhenIdContainsSpaces() {
            // Act & Assert
            InvalidProductIdException exception = assertThrows(
                    InvalidProductIdException.class,
                    () -> getProductUseCase.getById("PROD 123")
            );
            
            assertEquals("ID de producto inválido: PROD 123", exception.getMessage());
            assertEquals("PROD 123", exception.getInvalidId());
            verify(productRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Debería lanzar ProductNotFoundException cuando ID es válido pero producto no existe")
        void shouldThrowProductNotFoundExceptionWhenValidIdButProductNotExists() {
            // Arrange
            String validId = "PROD999";
            when(productRepository.findById(validId)).thenReturn(Optional.empty());

            // Act & Assert
            ProductNotFoundException exception = assertThrows(
                    ProductNotFoundException.class,
                    () -> getProductUseCase.getById(validId)
            );
            
            assertEquals("Producto no encontrado con ID: PROD999", exception.getMessage());
            assertEquals("PROD999", exception.getProductId());
            verify(productRepository).findById(validId);
        }
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay productos")
        void shouldReturnEmptyListWhenNoProducts() {
            // Arrange
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = getProductUseCase.getAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Debería retornar lista de productos cuando existen productos")
        void shouldReturnProductListWhenProductsExist() {
            // Arrange
            Product product2 = Product.builder()
                    .id("PROD456")
                    .title("Mouse Gaming")
                    .description("Mouse para gaming")
                    .price(new BigDecimal("59.99"))
                    .currency("USD")
                    .stock(0)
                    .category("Accesorios")
                    .build();

            List<Product> products = Arrays.asList(testProduct, product2);
            when(productRepository.findAll()).thenReturn(products);

            // Act
            List<ProductResponseDto> result = getProductUseCase.getAll();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("PROD123", result.get(0).getId());
            assertEquals("PROD456", result.get(1).getId());
            assertTrue(result.get(0).isAvailable()); // stock > 0
            assertFalse(result.get(1).isAvailable()); // stock = 0
            verify(productRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getByCategory Tests")
    class GetByCategoryTests {

        @Test
        @DisplayName("Debería retornar productos filtrados por categoría")
        void shouldReturnProductsFilteredByCategory() {
            // Arrange
            String category = "Electrónicos";
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByCategory(category)).thenReturn(products);

            // Act
            List<ProductResponseDto> result = getProductUseCase.getByCategory(category);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Electrónicos", result.get(0).getCategory());
            verify(productRepository).findByCategory(category);
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay productos en la categoría")
        void shouldReturnEmptyListWhenNoProductsInCategory() {
            // Arrange
            String category = "Ropa";
            when(productRepository.findByCategory(category)).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = getProductUseCase.getByCategory(category);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository).findByCategory(category);
        }
    }

    @Nested
    @DisplayName("getAvailableProducts Tests")
    class GetAvailableProductsTests {

        @Test
        @DisplayName("Debería retornar solo productos con stock disponible")
        void shouldReturnOnlyProductsWithAvailableStock() {
            // Arrange
            Product productWithStock = testProduct;
            Product productWithoutStock = Product.builder()
                    .id("PROD456")
                    .title("Mouse Gaming")
                    .stock(0)
                    .category("Accesorios")
                    .build();

            List<Product> allProducts = Arrays.asList(productWithStock, productWithoutStock);
            List<Product> availableProducts = Arrays.asList(productWithStock);
            when(productRepository.findAvailableProducts()).thenReturn(availableProducts);

            // Act
            List<ProductResponseDto> result = getProductUseCase.getAvailableProducts();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).isAvailable());
            verify(productRepository).findAvailableProducts();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay productos disponibles")
        void shouldReturnEmptyListWhenNoAvailableProducts() {
            // Arrange
            when(productRepository.findAvailableProducts()).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = getProductUseCase.getAvailableProducts();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository).findAvailableProducts();
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Debería retornar productos que contengan el nombre buscado")
        void shouldReturnProductsContainingSearchName() {
            // Arrange
            String searchTerm = "Laptop";
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByNameContaining(searchTerm)).thenReturn(products);

            // Act
            List<ProductResponseDto> result = getProductUseCase.searchByName(searchTerm);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getTitle().contains(searchTerm));
            verify(productRepository).findByNameContaining(searchTerm);
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay coincidencias en el nombre")
        void shouldReturnEmptyListWhenNoNameMatches() {
            // Arrange
            String searchTerm = "Inexistente";
            when(productRepository.findByNameContaining(searchTerm)).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = getProductUseCase.searchByName(searchTerm);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository).findByNameContaining(searchTerm);
        }
    }

    @Nested
    @DisplayName("mapToResponseDto Tests")
    class MapToResponseDtoTests {

        @Test
        @DisplayName("Debería mapear correctamente producto con stock disponible")
        void shouldMapCorrectlyProductWithAvailableStock() {
            // Arrange
            Product productWithStock = Product.builder()
                    .id("PROD123")
                    .title("Test Product")
                    .stock(10)
                    .build();

            when(productRepository.findById("PROD123")).thenReturn(Optional.of(productWithStock));

            // Act
            ProductResponseDto result = getProductUseCase.getById("PROD123");

            // Assert
            assertNotNull(result);
            assertEquals("PROD123", result.getId());
            assertEquals("Test Product", result.getTitle());
            assertEquals(10, result.getStock());
            assertTrue(result.isAvailable());
        }

        @Test
        @DisplayName("Debería mapear correctamente producto sin stock disponible")
        void shouldMapCorrectlyProductWithoutAvailableStock() {
            // Arrange
            Product productWithoutStock = Product.builder()
                    .id("PROD456")
                    .title("Test Product")
                    .stock(0)
                    .build();

            when(productRepository.findById("PROD456")).thenReturn(Optional.of(productWithoutStock));

            // Act
            ProductResponseDto result = getProductUseCase.getById("PROD456");

            // Assert
            assertNotNull(result);
            assertEquals("PROD456", result.getId());
            assertEquals("Test Product", result.getTitle());
            assertEquals(0, result.getStock());
            assertFalse(result.isAvailable());
        }

        @Test
        @DisplayName("Debería mapear correctamente producto con stock null")
        void shouldMapCorrectlyProductWithNullStock() {
            // Arrange
            Product productWithNullStock = Product.builder()
                    .id("PROD789")
                    .title("Test Product")
                    .stock(null)
                    .build();

            when(productRepository.findById("PROD789")).thenReturn(Optional.of(productWithNullStock));

            // Act
            ProductResponseDto result = getProductUseCase.getById("PROD789");

            // Assert
            assertNotNull(result);
            assertEquals("PROD789", result.getId());
            assertEquals("Test Product", result.getTitle());
            assertNull(result.getStock());
            assertFalse(result.isAvailable());
        }
    }
}
