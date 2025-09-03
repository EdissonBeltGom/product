# Retry Pattern - Implementación

## **Descripción General**

El **Retry Pattern** ha sido implementado en el proyecto siguiendo una estrategia por capas y responsabilidades, utilizando **Resilience4j** como biblioteca principal.

## **Arquitectura de Implementación**

### **1. Capas de Implementación**

#### **A. Capa de Repositorio (Más Baja)**
- **Ubicación**: `ProductRepositoryAdapter`
- **Configuración**: `repositoryRetry`
- **Estrategia**: Más permisiva para operaciones de lectura
- **Máximo de reintentos**: 3
- **Backoff**: Exponencial (2x)

#### **B. Capa de Servicios Externos (Media)**
- **Configuración**: `externalServiceRetry`
- **Estrategia**: Más agresiva para servicios externos
- **Máximo de reintentos**: 5
- **Backoff**: Exponencial (3x)

#### **C. Capa de Servicios de Negocio (Alta)**
- **Configuración**: `businessServiceRetry`
- **Estrategia**: Selectiva para lógica de negocio
- **Máximo de reintentos**: 2
- **Backoff**: Exponencial (1.5x)

### **2. Configuraciones por Tipo de Operación**

#### **A. Operaciones de Lectura**
```properties
resilience4j.retry.instances.repositoryRetry.max-attempts=3
resilience4j.retry.instances.repositoryRetry.wait-duration=500ms
resilience4j.retry.instances.repositoryRetry.enable-exponential-backoff=true
resilience4j.retry.instances.repositoryRetry.exponential-backoff-multiplier=2
```

#### **B. Operaciones de Escritura**
```properties
resilience4j.retry.instances.writeOperation.max-attempts=2
resilience4j.retry.instances.writeOperation.wait-duration=1s
resilience4j.retry.instances.writeOperation.enable-exponential-backoff=true
resilience4j.retry.instances.writeOperation.exponential-backoff-multiplier=1.5
```

#### **C. Servicios Externos**
```properties
resilience4j.retry.instances.externalServiceRetry.max-attempts=5
resilience4j.retry.instances.externalServiceRetry.wait-duration=2s
resilience4j.retry.instances.externalServiceRetry.enable-exponential-backoff=true
resilience4j.retry.instances.externalServiceRetry.exponential-backoff-multiplier=3
```

## **3. Estrategia de Excepciones**

### **Excepciones que SÍ se Reintentan**
- `java.io.IOException` - Problemas de archivo/red
- `org.springframework.dao.DataAccessException` - Problemas de base de datos
- `java.net.ConnectException` - Problemas de conexión
- `java.net.SocketTimeoutException` - Timeouts de red
- `org.springframework.web.client.HttpServerErrorException` - Errores 5xx de servicios externos

### **Excepciones que NO se Reintentan**
- `com.item.product.domain.exception.ProductNotFoundException` - Excepción de negocio
- `com.item.product.domain.exception.InvalidProductIdException` - Excepción de validación
- `java.lang.IllegalArgumentException` - Errores de validación
- `java.util.NoSuchElementException` - Elementos no encontrados

## **4. Implementación en Código**

### **A. ProductRepositoryAdapter**
```java
@Override
@Retry(name = "repositoryRetry", fallbackMethod = "findByIdFallback")
public Optional<Product> findById(String id) {
    // Operación que puede fallar
}

public Optional<Product> findByIdFallback(String id, Exception ex) {
    log.warn("Repository retry failed for findById. ID: {}, Error: {}", id, ex.getMessage());
    return Optional.empty();
}
```

### **B. Métodos con Retry Implementado**
- `findById()` - Búsqueda por ID
- `findAll()` - Obtener todos los productos
- `findByCategory()` - Búsqueda por categoría
- `findSimilarProducts()` - Productos similares

## **5. Monitoreo y Métricas**

### **A. RetryMetricsService**
- **Ubicación**: `src/main/java/com/item/product/infrastructure/service/RetryMetricsService.java`
- **Funcionalidad**: Monitoreo automático de reintentos
- **Métricas**: Intentos exitosos, fallidos, tasa de éxito, promedio de intentos

### **B. RetryMetricsController**
- **Ubicación**: `src/main/java/com/item/product/infrastructure/controller/RetryMetricsController.java`
- **Endpoints**:
  - `GET /api/retry-metrics` - Todas las métricas
  - `GET /api/retry-metrics/{retryName}` - Métricas específicas
  - `GET /api/retry-metrics/summary` - Resumen general
  - `DELETE /api/retry-metrics` - Resetear métricas

### **C. Métricas Disponibles**
- **Intentos Exitosos**: Número de operaciones que se completaron después de reintentos
- **Intentos Fallidos**: Número de operaciones que fallaron después de agotar reintentos
- **Tasa de Éxito**: Porcentaje de operaciones exitosas
- **Promedio de Intentos**: Promedio de reintentos por operación
- **Máximo de Intentos**: Mayor número de reintentos realizados

## **6. Configuración por Ambiente**

### **Desarrollo**
- **Reintentos**: Más permisivos para facilitar desarrollo
- **Logging**: Detallado para debugging
- **Métricas**: Habilitadas para monitoreo

### **Producción**
- **Reintentos**: Más conservadores para evitar sobrecarga
- **Logging**: Resumido para performance
- **Métricas**: Habilitadas para monitoreo en tiempo real

## **7. Beneficios de la Implementación**

### **A. Resiliencia**
- **Recuperación Automática**: El sistema se recupera automáticamente de fallos temporales
- **Mejor UX**: Los usuarios no ven errores por problemas transitorios
- **Estabilidad**: Mayor estabilidad en entornos con problemas de red/DB

### **B. Monitoreo**
- **Visibilidad**: Métricas detalladas del comportamiento de reintentos
- **Alertas**: Posibilidad de configurar alertas basadas en métricas
- **Optimización**: Datos para ajustar configuraciones

### **C. Flexibilidad**
- **Configurabilidad**: Diferentes estrategias según el tipo de operación
- **Escalabilidad**: Fácil agregar nuevas instancias de retry
- **Mantenibilidad**: Código limpio y bien documentado

## **8. Ejemplos de Uso**

### **A. Consultar Métricas**
```bash
# Obtener todas las métricas
curl http://localhost:8080/ProductsApi/api/retry-metrics

# Obtener métricas específicas
curl http://localhost:8080/ProductsApi/api/retry-metrics/repositoryRetry

# Obtener resumen
curl http://localhost:8080/ProductsApi/api/retry-metrics/summary
```

### **B. Monitoreo en Tiempo Real**
```bash
# Ver métricas de Actuator
curl http://localhost:8080/ProductsApi/actuator/retries
```

## **9. Consideraciones de Performance**

### **A. Impacto en Latencia**
- **Backoff Exponencial**: Evita sobrecarga en servicios externos
- **Límites de Reintentos**: Previene latencia excesiva
- **Fallbacks**: Respuestas rápidas cuando se agotan reintentos

### **B. Recursos del Sistema**
- **Thread Pool**: Uso eficiente de hilos durante reintentos
- **Memory**: Gestión adecuada de memoria en fallbacks
- **CPU**: Minimización del impacto en CPU

## **10. Próximos Pasos**

### **A. Mejoras Futuras**
- **Retry con Jitter**: Agregar variación aleatoria para evitar thundering herd
- **Retry Selectivo**: Reintentar solo en ciertos códigos de error HTTP
- **Retry Adaptativo**: Ajustar configuración basado en métricas

### **B. Integración**
- **Prometheus**: Exportar métricas a Prometheus
- **Grafana**: Dashboards para visualización
- **Alerting**: Alertas automáticas basadas en métricas

---

**Nota**: Esta implementación sigue las mejores prácticas de Resilience4j y proporciona una base sólida para la resiliencia de la aplicación.

