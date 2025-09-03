# Implementación del Patrón Rate Limiting

## 📋 Descripción General

Este documento describe la implementación del patrón **Rate Limiting** en el proyecto Product API usando **Resilience4j**. El Rate Limiting controla la cantidad de solicitudes que un cliente puede hacer en un período de tiempo específico, protegiendo la aplicación contra sobrecarga y abuso.

## 🏗️ Arquitectura Implementada

### Estructura de Archivos

```
src/main/java/com/item/product/infrastructure/
├── service/
│   ├── ProductService.java              # Servicio con Rate Limiting
│   └── RateLimitService.java           # Servicio de Rate Limiting dinámico
└── controller/
    └── ProductController.java           # Controlador con Rate Limiting por IP
```

### Dependencias Utilizadas

```xml
<!-- Resilience4j para patrones de resiliencia -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- Spring Boot Actuator para monitoreo -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 🔧 Configuración del Rate Limiting

### Configuración General

```properties
# Configuración general de Rate Limiting
resilience4j.ratelimiter.configs.default.limit-for-period=100
resilience4j.ratelimiter.configs.default.limit-refresh-period=1m
resilience4j.ratelimiter.configs.default.timeout-duration=5s
resilience4j.ratelimiter.configs.default.register-health-indicator=true
```

### Configuraciones Específicas por Endpoint

#### 1. **productEndpoint** (Productos individuales)
```properties
resilience4j.ratelimiter.instances.productEndpoint.limit-for-period=50
resilience4j.ratelimiter.instances.productEndpoint.limit-refresh-period=1m
resilience4j.ratelimiter.instances.productEndpoint.timeout-duration=3s
```

#### 2. **userRateLimit** (Usuarios generales)
```properties
resilience4j.ratelimiter.instances.userRateLimit.limit-for-period=200
resilience4j.ratelimiter.instances.userRateLimit.limit-refresh-period=1h
resilience4j.ratelimiter.instances.userRateLimit.timeout-duration=10s
```

#### 3. **similarProductsRateLimit** (Productos similares)
```properties
resilience4j.ratelimiter.instances.similarProductsRateLimit.limit-for-period=30
resilience4j.ratelimiter.instances.similarProductsRateLimit.limit-refresh-period=1m
resilience4j.ratelimiter.instances.similarProductsRateLimit.timeout-duration=2s
```

#### 4. **searchRateLimit** (Búsquedas)
```properties
resilience4j.ratelimiter.instances.searchRateLimit.limit-for-period=100
resilience4j.ratelimiter.instances.searchRateLimit.limit-refresh-period=5m
resilience4j.ratelimiter.instances.searchRateLimit.timeout-duration=5s
```

## 🚀 Implementación en el Código

### 1. Servicio de Rate Limiting Dinámico

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RateLimiterRegistry rateLimiterRegistry;
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();
    
    /**
     * Verifica si una IP puede hacer una solicitud
     */
    public boolean isAllowedByIp(String ipAddress) {
        RateLimiter rateLimiter = getIpRateLimiter(ipAddress);
        boolean allowed = rateLimiter.acquirePermission();
        
        if (!allowed) {
            log.warn("Rate limit alcanzado para IP: {}", ipAddress);
        }
        
        return allowed;
    }
    
    /**
     * Actualiza el límite de solicitudes para una IP específica
     */
    public void updateIpRateLimit(String ipAddress, int newLimit) {
        RateLimiter rateLimiter = getIpRateLimiter(ipAddress);
        rateLimiter.changeLimitForPeriod(newLimit);
        log.info("Rate limit actualizado para IP {}: {} solicitudes por minuto", ipAddress, newLimit);
    }
}
```

### 2. Servicio de Productos con Rate Limiting

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
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
        } catch (Exception e) {
            log.error("Error al obtener producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener producto", e);
        }
    }
    
    /**
     * Fallback para Rate Limiting
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
}
```

### 3. Controlador con Rate Limiting por IP

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final RateLimitService rateLimitService;
    
    /**
     * Obtiene un producto por su ID con Circuit Breaker y Rate Limiting
     */
    public CompletableFuture<ResponseEntity<ProductResponseDto>> getProductById(
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
                        .price(BigDecimal.ZERO)
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
            .thenApply(product -> ResponseEntity.ok(product))
            .exceptionally(throwable -> {
                log.error("Error al obtener producto con ID {}: {}", id, throwable.getMessage());
                throw new RuntimeException("Error al obtener producto", throwable);
            });
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
```

## 📊 Monitoreo y Métricas

### Endpoints de Monitoreo

```properties
# Habilitar endpoints de monitoreo incluyendo Rate Limiters
management.endpoints.web.exposure.include=health,info,metrics,circuitbreakers,retries,timelimiters,ratelimiters
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
```

### URLs de Monitoreo

- **Health Check**: `http://localhost:8080/ProductsApi/actuator/health`
- **Rate Limiters**: `http://localhost:8080/ProductsApi/actuator/ratelimiters`
- **Métricas**: `http://localhost:8080/ProductsApi/actuator/metrics`
- **Rate Limit Info**: `http://localhost:8080/ProductsApi/api/products/rate-limit-info`

### Métricas Disponibles

```json
{
  "ratelimiters": {
    "productEndpoint": {
      "availablePermissions": 45,
      "numberOfWaitingThreads": 0
    },
    "userRateLimit": {
      "availablePermissions": 180,
      "numberOfWaitingThreads": 2
    },
    "similarProductsRateLimit": {
      "availablePermissions": 25,
      "numberOfWaitingThreads": 1
    }
  }
}
```

## 🛡️ Beneficios de la Implementación

### ✅ **Protección contra DDoS**
- Limita ataques de denegación de servicio
- Protege recursos del servidor
- Previene sobrecarga del sistema

### ✅ **Control de Costos**
- Evita uso excesivo de recursos
- Controla costos de infraestructura
- Optimiza el rendimiento

### ✅ **Experiencia de Usuario Justa**
- Distribuye recursos equitativamente
- Previene monopolización por usuarios
- Garantiza disponibilidad para todos

### ✅ **Cumplimiento de SLA**
- Garantiza rendimiento para todos
- Cumple con acuerdos de nivel de servicio
- Mantiene la calidad del servicio

### ✅ **Monitoreo en Tiempo Real**
- Detecta patrones anómalos
- Permite ajustes dinámicos
- Proporciona visibilidad completa

## 🔄 Estrategias de Rate Limiting Implementadas

### 1. **Token Bucket Algorithm**
- Permite ráfagas de tráfico
- Recuperación gradual de tokens
- Ideal para APIs con tráfico variable

### 2. **Rate Limiting por IP**
- Control granular por dirección IP
- Detección de IPs reales (X-Forwarded-For, X-Real-IP)
- Configuración dinámica por IP

### 3. **Rate Limiting por Endpoint**
- Límites específicos por funcionalidad
- Configuración diferenciada según criticidad
- Fallbacks específicos por endpoint

### 4. **Rate Limiting Dinámico**
- Actualización de límites en tiempo real
- Configuración por usuario/IP específico
- Limpieza automática de usuarios inactivos

## 🔧 Configuración por Entorno

### Desarrollo
```properties
# Configuración más permisiva para desarrollo
resilience4j.ratelimiter.instances.productEndpoint.limit-for-period=1000
resilience4j.ratelimiter.instances.productEndpoint.limit-refresh-period=1m
resilience4j.ratelimiter.instances.userRateLimit.limit-for-period=2000
resilience4j.ratelimiter.instances.userRateLimit.limit-refresh-period=1h
```

### Producción
```properties
# Configuración más estricta para producción
resilience4j.ratelimiter.instances.productEndpoint.limit-for-period=50
resilience4j.ratelimiter.instances.productEndpoint.limit-refresh-period=1m
resilience4j.ratelimiter.instances.userRateLimit.limit-for-period=200
resilience4j.ratelimiter.instances.userRateLimit.limit-refresh-period=1h
```

## 🚨 Escenarios de Uso

### 1. **Usuario Normal**
- 200 solicitudes por hora para endpoints generales
- 50 solicitudes por minuto para productos individuales
- 30 solicitudes por minuto para productos similares

### 2. **Usuario Premium** (Futuro)
- 500 solicitudes por hora para endpoints generales
- 100 solicitudes por minuto para productos individuales
- 60 solicitudes por minuto para productos similares

### 3. **API Key Específica** (Futuro)
- Límites personalizados por API Key
- Configuración dinámica según plan de suscripción
- Monitoreo específico por cliente

## 📈 Próximos Pasos

### 1. **Implementar Autenticación**
```java
@RateLimiter(name = "premiumUserRateLimit", fallbackMethod = "premiumRateLimitFallback")
public CompletableFuture<ProductResponseDto> getProductById(String id, String userType) {
    // Rate limiting diferente para usuarios premium vs básicos
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

### 4. **Testing de Rate Limiting**
- Tests de carga con límites simulados
- Validación de comportamiento del Rate Limiting
- Tests de recuperación automática

## 🔍 Troubleshooting

### Rate Limiting Siempre Activo
```bash
# Verificar configuración
curl http://localhost:8080/ProductsApi/actuator/ratelimiters

# Verificar logs
tail -f logs/application.log | grep "Rate limit"
```

### Fallbacks No Se Ejecutan
```java
// Verificar que el método de fallback tenga la firma correcta
public CompletableFuture<ProductResponseDto> rateLimitFallback(String id, Exception ex) {
    // Método de fallback
}
```

### Configuración No Se Aplica
```properties
# Verificar que las propiedades estén en el archivo correcto
resilience4j.ratelimiter.instances.productEndpoint.limit-for-period=50
```

## 📚 Referencias

- [Resilience4j Rate Limiter Documentation](https://resilience4j.readme.io/docs/ratelimiter)
- [Rate Limiting Patterns](https://microservices.io/patterns/reliability/rate-limiting.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [API Rate Limiting Best Practices](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)

