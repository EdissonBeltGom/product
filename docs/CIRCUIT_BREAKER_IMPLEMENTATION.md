# Implementación del Patrón Circuit Breaker

## 📋 Descripción General

Este documento describe la implementación del patrón **Circuit Breaker** en el proyecto Product API usando **Resilience4j**. El Circuit Breaker protege la aplicación contra fallos en cascada y mejora la resiliencia del sistema.

## 🏗️ Arquitectura Implementada

### Estructura de Archivos

```
src/main/java/com/item/product/infrastructure/
├── config/
│   ├── ResilienceConfig.java              # Configuración de Resilience4j
│   └── SecurityConfig.java               # Configuración de seguridad
├── service/
│   └── ProductService.java               # Servicio con Circuit Breaker
└── controller/
    └── ProductController.java            # Controlador actualizado
```

### Dependencias Agregadas

```xml
<!-- Resilience4j para patrones de resiliencia -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-all</artifactId>
    <version>2.2.0</version>
</dependency>
```

## 🔧 Configuración del Circuit Breaker

### Configuración General

```properties
# Configuración general de Resilience4j
resilience4j.circuitbreaker.configs.default.register-health-indicator=true
resilience4j.circuitbreaker.configs.default.sliding-window-size=10
resilience4j.circuitbreaker.configs.default.minimum-number-of-calls=5
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=50
resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.configs.default.permitted-number-of-calls-in-half-open-state=3
```

### Configuraciones Específicas

#### 1. **productService** (Operaciones básicas de productos)
```properties
resilience4j.circuitbreaker.instances.productService.sliding-window-size=10
resilience4j.circuitbreaker.instances.productService.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.productService.wait-duration-in-open-state=30s
```

#### 2. **similarProductsService** (Operaciones de recomendación)
```properties
resilience4j.circuitbreaker.instances.similarProductsService.sliding-window-size=15
resilience4j.circuitbreaker.instances.similarProductsService.minimum-number-of-calls=8
resilience4j.circuitbreaker.instances.similarProductsService.failure-rate-threshold=40
resilience4j.circuitbreaker.instances.similarProductsService.wait-duration-in-open-state=45s
```

## 🚀 Implementación en el Código

### 1. Servicio con Circuit Breaker

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    /**
     * Obtiene un producto por ID con Circuit Breaker
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @TimeLimiter(name = "productService", fallbackMethod = "getProductFallback")
    public CompletableFuture<ProductResponseDto> getProductById(String id) {
        log.info("Obteniendo producto con ID: {}", id);
        
        try {
            ProductResponseDto product = getProductUseCase.getById(id);
            log.info("Producto obtenido exitosamente: {}", product.getId());
            return CompletableFuture.completedFuture(product);
        } catch (Exception e) {
            log.error("Error al obtener producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener producto", e);
        }
    }
}
```

### 2. Métodos de Fallback

```java
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
            .price(0.0)
            .currency("USD")
            .condition("N/A")
            .stock(0)
            .category("N/A")
            .seller("Sistema")
            .available(false)
            .build()
    );
}
```

### 3. Controlador Actualizado

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Obtiene un producto por su ID con Circuit Breaker
     */
    public CompletableFuture<ResponseEntity<ProductResponseDto>> getProductById(
            @PathVariable String id) {
        log.info("Solicitud recibida para obtener producto con ID: {}", id);
        
        return productService.getProductById(id)
            .thenApply(product -> {
                log.info("Producto obtenido exitosamente: {}", product.getId());
                return ResponseEntity.ok(product);
            })
            .exceptionally(throwable -> {
                log.error("Error al obtener producto con ID {}: {}", id, throwable.getMessage());
                throw new RuntimeException("Error al obtener producto", throwable);
            });
    }
}
```

## 🔄 Estados del Circuit Breaker

### 1. **CLOSED** (Cerrado - Normal)
- ✅ Las llamadas pasan normalmente
- ✅ Se registran los fallos
- ✅ Si se excede el umbral de fallos → **OPEN**

### 2. **OPEN** (Abierto - Fallo)
- ❌ Las llamadas fallan inmediatamente
- ❌ Se ejecuta el método de fallback
- ⏰ Después del tiempo de espera → **HALF_OPEN**

### 3. **HALF_OPEN** (Semi-abierto - Prueba)
- 🔍 Se permiten algunas llamadas de prueba
- ✅ Si son exitosas → **CLOSED**
- ❌ Si fallan → **OPEN**

## 📊 Monitoreo y Métricas

### Endpoints de Monitoreo

```properties
# Habilitar endpoints de monitoreo
management.endpoints.web.exposure.include=health,info,metrics,circuitbreakers,retries,timelimiters
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
```

### URLs de Monitoreo

- **Health Check**: `http://localhost:8080/ProductsApi/actuator/health`
- **Circuit Breakers**: `http://localhost:8080/ProductsApi/actuator/circuitbreakers`
- **Métricas**: `http://localhost:8080/ProductsApi/actuator/metrics`

### Métricas Disponibles

```json
{
  "circuitbreakers": {
    "productService": {
      "state": "CLOSED",
      "failureRate": 0.0,
      "numberOfFailedCalls": 0,
      "numberOfSuccessfulCalls": 10,
      "numberOfNotPermittedCalls": 0
    },
    "similarProductsService": {
      "state": "CLOSED",
      "failureRate": 0.0,
      "numberOfFailedCalls": 0,
      "numberOfSuccessfulCalls": 5,
      "numberOfNotPermittedCalls": 0
    }
  }
}
```

## 🛡️ Beneficios de la Implementación

### ✅ **Protección contra Fallos en Cascada**
- Evita que un fallo se propague a toda la aplicación
- Aísla problemas en servicios específicos

### ✅ **Respuestas Degradadas Elegantes**
- Los usuarios reciben respuestas útiles incluso en fallos
- No se muestran errores técnicos al usuario final

### ✅ **Recuperación Automática**
- El sistema se recupera automáticamente cuando el problema se resuelve
- No requiere intervención manual

### ✅ **Monitoreo en Tiempo Real**
- Visibilidad completa del estado de los servicios
- Alertas automáticas cuando se activa el Circuit Breaker

### ✅ **Configuración Flexible**
- Diferentes configuraciones por servicio
- Ajuste fino según las necesidades del negocio

## 🔧 Configuración por Entorno

### Desarrollo
```properties
# Configuración más permisiva para desarrollo
resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=70
resilience4j.circuitbreaker.instances.productService.wait-duration-in-open-state=15s
```

### Producción
```properties
# Configuración más estricta para producción
resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=30
resilience4j.circuitbreaker.instances.productService.wait-duration-in-open-state=60s
```

## 🚨 Escenarios de Uso

### 1. **Servicio de Base de Datos Lento**
- Circuit Breaker se activa después de 5 fallos
- Usuarios reciben productos de fallback
- Sistema se recupera automáticamente

### 2. **Servicio Externo No Disponible**
- Recomendaciones fallan → Circuit Breaker activado
- Usuarios ven mensaje de "recomendaciones temporalmente no disponibles"
- Sistema continúa funcionando para otras operaciones

### 3. **Sobrecarga del Sistema**
- Timeout activado después de 5 segundos
- Usuarios reciben respuesta rápida de fallback
- Sistema se protege contra sobrecarga

## 📈 Próximos Pasos

### 1. **Implementar Alertas**
```java
@EventListener
public void onCircuitBreakerStateChanged(CircuitBreakerOnStateTransitionEvent event) {
    if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
        // Enviar alerta al equipo de DevOps
        alertService.sendAlert("Circuit Breaker activado: " + event.getCircuitBreakerName());
    }
}
```

### 2. **Métricas Avanzadas**
- Integración con Prometheus/Grafana
- Dashboards personalizados
- Alertas automáticas

### 3. **Configuración Dinámica**
- Cambio de configuración en tiempo real
- Ajuste automático según métricas
- Configuración por servicio específico

### 4. **Testing de Resiliencia**
- Tests de carga con fallos simulados
- Validación de comportamiento del Circuit Breaker
- Tests de recuperación automática

## 🔍 Troubleshooting

### Circuit Breaker Siempre Abierto
```bash
# Verificar configuración
curl http://localhost:8080/ProductsApi/actuator/circuitbreakers

# Verificar logs
tail -f logs/application.log | grep "Circuit Breaker"
```

### Fallbacks No Se Ejecutan
```java
// Verificar que el método de fallback tenga la firma correcta
public CompletableFuture<ProductResponseDto> getProductFallback(String id, Exception ex) {
    // Método de fallback
}
```

### Configuración No Se Aplica
```properties
# Verificar que las propiedades estén en el archivo correcto
resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=50
```

## 📚 Referencias

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Microservices Patterns](https://microservices.io/patterns/reliability/circuit-breaker.html)

