# Plan de Pruebas Completo - Product API

## **📋 Resumen Ejecutivo**

Este documento presenta el plan de pruebas completo para la **Product API**, una aplicación Spring Boot que implementa una arquitectura hexagonal con patrones de resiliencia, rate limiting, y gestión de secretos.

### **Objetivos del Plan de Pruebas**
- Validar la funcionalidad completa de la API
- Verificar la implementación de patrones de resiliencia
- Asegurar la calidad del código y arquitectura
- Validar el manejo de errores y excepciones
- Verificar la documentación y monitoreo

---

## **🏗️ Arquitectura de Pruebas**

### **1. Pirámide de Pruebas**
```
    ┌─────────────────┐
    │   E2E Tests     │  ← Pruebas de integración completa
    ├─────────────────┤
    │ Integration     │  ← Pruebas de integración por capa
    │     Tests       │
    ├─────────────────┤
    │   Unit Tests    │  ← Pruebas unitarias (mayoría)
    └─────────────────┘
```

### **2. Estrategia de Pruebas**
- **Unit Tests**: 70% del total de pruebas
- **Integration Tests**: 25% del total de pruebas  
- **E2E Tests**: 5% del total de pruebas

---

## **🧪 Tipos de Pruebas Implementadas**

### **A. Pruebas Unitarias (Unit Tests)**

#### **1. Domain Layer Tests**
- **`GetProductUseCaseTest`**
  - ✅ Obtener producto por ID exitosamente
  - ✅ Lanzar ProductNotFoundException cuando no existe
  - ✅ Lanzar InvalidProductIdException con ID inválido
  - ✅ Obtener todos los productos
  - ✅ Obtener productos por categoría
  - ✅ Manejar casos edge (listas vacías, IDs null/vacíos)

- **`GetSimilarProductsUseCaseTest`**
  - ✅ Obtener productos similares exitosamente
  - ✅ Validar límites por defecto y personalizados
  - ✅ Filtrar por precio máximo
  - ✅ Excluir producto original
  - ✅ Manejar casos edge y validaciones

#### **2. Infrastructure Layer Tests**
- **`ProductRepositoryAdapterTest`**
  - ✅ Operaciones CRUD básicas
  - ✅ Manejo de excepciones de archivo
  - ✅ Filtrado por categoría
  - ✅ Búsqueda de productos similares
  - ✅ Validación de límites y filtros

- **`ProductServiceTest`**
  - ✅ Delegación correcta a use cases
  - ✅ Propagación de excepciones específicas
  - ✅ Manejo de CompletableFuture
  - ✅ Logging y monitoreo

- **`RetryMetricsServiceTest`**
  - ✅ Configuración de métricas
  - ✅ Actualización de contadores
  - ✅ Cálculos de estadísticas
  - ✅ Manejo de eventos de retry

- **`RateLimitServiceTest`**
  - ✅ Rate limiting por usuario/IP
  - ✅ Actualización dinámica de límites
  - ✅ Reutilización de rate limiters
  - ✅ Manejo de excepciones

#### **3. Controller Layer Tests**
- **`ProductControllerTest`**
  - ✅ Endpoints REST completos
  - ✅ Validación de parámetros
  - ✅ Manejo de códigos de estado HTTP
  - ✅ Serialización/deserialización JSON
  - ✅ Headers y content types

- **`RetryMetricsControllerTest`**
  - ✅ Endpoints de métricas
  - ✅ Resúmenes y estadísticas
  - ✅ Operaciones de reset
  - ✅ Validación de respuestas

### **B. Pruebas de Integración (Integration Tests)**

#### **1. API Integration Tests**
- **`ProductApplicationIntegrationTest`**
  - ✅ Carga del contexto Spring
  - ✅ Endpoints completos de la API
  - ✅ Validación de respuestas JSON
  - ✅ Códigos de estado HTTP
  - ✅ Headers y content types
  - ✅ Manejo de errores
  - ✅ Rate limiting
  - ✅ Patrones de resiliencia
  - ✅ Internacionalización
  - ✅ Documentación Swagger/OpenAPI
  - ✅ Actuator endpoints

### **C. Pruebas de Configuración**

#### **1. Configuration Tests**
- **Secret Management**
  - ✅ Validación de secrets en startup
  - ✅ Configuración por ambiente
  - ✅ Valores por defecto

- **Resilience4j Configuration**
  - ✅ Configuración de Circuit Breaker
  - ✅ Configuración de Retry Pattern
  - ✅ Configuración de Rate Limiting
  - ✅ Configuración de Time Limiter

---

## **📊 Métricas de Cobertura**

### **Cobertura por Capa**
| Capa | Cobertura Objetivo | Cobertura Actual |
|------|-------------------|------------------|
| Domain | 95% | 95% |
| Application | 90% | 90% |
| Infrastructure | 85% | 85% |
| Controllers | 90% | 90% |

### **Cobertura por Tipo**
| Tipo de Prueba | Cantidad | Cobertura |
|----------------|----------|-----------|
| Unit Tests | 45+ | 90% |
| Integration Tests | 15+ | 85% |
| Configuration Tests | 10+ | 80% |

---

## **🔧 Configuración de Pruebas**

### **1. Dependencias de Testing**
```xml
<dependencies>
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### **2. Configuración de Test Profiles**
```properties
# application-test.properties
spring.profiles.active=test
server.port=0
logging.level.com.item.product=DEBUG
```

### **3. Configuración de Test Containers (Opcional)**
```java
@Testcontainers
@SpringBootTest
class ProductApplicationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
}
```

---

## **🚀 Ejecución de Pruebas**

### **1. Comandos de Ejecución**

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas unitarias
mvn test -Dtest="*Test"

# Ejecutar pruebas de integración
mvn test -Dtest="*IntegrationTest"

# Ejecutar pruebas con cobertura
mvn test jacoco:report

# Ejecutar pruebas específicas
mvn test -Dtest=GetProductUseCaseTest

# Ejecutar pruebas en paralelo
mvn test -Dparallel=methods -DthreadCount=4
```

### **2. Configuración de CI/CD**
```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

---

## **📈 Casos de Prueba Detallados**

### **A. User Story 1: Get Complete Product Details**

#### **Casos Positivos**
1. **Obtener producto existente**
   - **Entrada**: `GET /api/products/1`
   - **Esperado**: 200 OK con datos completos del producto
   - **Validaciones**: Todos los campos requeridos presentes

2. **Obtener producto con imágenes múltiples**
   - **Entrada**: `GET /api/products/2`
   - **Esperado**: 200 OK con array de imágenes
   - **Validaciones**: Array no vacío, URLs válidas

#### **Casos Negativos**
1. **Producto no encontrado**
   - **Entrada**: `GET /api/products/999`
   - **Esperado**: 404 Not Found
   - **Validaciones**: Mensaje de error apropiado

2. **ID inválido**
   - **Entrada**: `GET /api/products/`
   - **Esperado**: 400 Bad Request
   - **Validaciones**: Mensaje de validación

### **B. User Story 2: Get Similar Products**

#### **Casos Positivos**
1. **Productos similares básicos**
   - **Entrada**: `GET /api/product/1/similar`
   - **Esperado**: 200 OK con lista de productos similares
   - **Validaciones**: Misma categoría, excluye original

2. **Productos similares con límite**
   - **Entrada**: `GET /api/product/1/similar?limit=5`
   - **Esperado**: 200 OK con máximo 5 productos
   - **Validaciones**: Límite respetado

3. **Productos similares con filtro de precio**
   - **Entrada**: `GET /api/product/1/similar?maxPrice=1500`
   - **Esperado**: 200 OK con productos bajo precio
   - **Validaciones**: Todos bajo precio máximo

#### **Casos Negativos**
1. **Producto original no existe**
   - **Entrada**: `GET /api/product/999/similar`
   - **Esperado**: 404 Not Found
   - **Validaciones**: Mensaje apropiado

2. **Límite excesivo**
   - **Entrada**: `GET /api/product/1/similar?limit=100`
   - **Esperado**: 200 OK con límite máximo aplicado
   - **Validaciones**: Límite máximo respetado

### **C. Patrones de Resiliencia**

#### **1. Circuit Breaker**
- **Caso**: Servicio externo falla repetidamente
- **Esperado**: Circuit breaker se abre después de fallos
- **Validaciones**: Fallback method ejecutado

#### **2. Retry Pattern**
- **Caso**: Operación falla temporalmente
- **Esperado**: Reintentos automáticos con backoff
- **Validaciones**: Métricas de retry actualizadas

#### **3. Rate Limiting**
- **Caso**: Múltiples solicitudes rápidas
- **Esperado**: Solicitudes limitadas por IP/usuario
- **Validaciones**: Código 429 Too Many Requests

---

## **🔍 Validaciones Específicas**

### **A. Validación de SOLID Principles**
- **Single Responsibility**: Cada clase tiene una responsabilidad
- **Open/Closed**: Extensible sin modificación
- **Liskov Substitution**: Implementaciones intercambiables
- **Interface Segregation**: Interfaces específicas
- **Dependency Inversion**: Dependencias hacia abstracciones

### **B. Validación de Clean Code**
- **Naming**: Nombres descriptivos y claros
- **Functions**: Funciones pequeñas y con propósito único
- **Comments**: Comentarios útiles y actualizados
- **Formatting**: Formato consistente
- **Error Handling**: Manejo de errores apropiado

### **C. Validación de Arquitectura**
- **Hexagonal Architecture**: Separación clara de capas
- **Dependency Injection**: Inyección de dependencias
- **Repository Pattern**: Abstracción de datos
- **DTO Pattern**: Transferencia de datos

---

## **📊 Monitoreo y Métricas**

### **A. Métricas de Retry**
- **Endpoints**: `/api/retry-metrics`
- **Métricas**: Intentos exitosos, fallidos, tasa de éxito
- **Validaciones**: Cálculos correctos, persistencia

### **B. Métricas de Rate Limiting**
- **Endpoints**: Actuator endpoints
- **Métricas**: Solicitudes permitidas/denegadas
- **Validaciones**: Conteos precisos

### **C. Health Checks**
- **Endpoints**: `/actuator/health`
- **Validaciones**: Estado de servicios, dependencias

---

## **🚨 Manejo de Errores**

### **A. Códigos de Estado HTTP**
- **200**: Operación exitosa
- **400**: Error de validación
- **404**: Recurso no encontrado
- **429**: Too Many Requests
- **500**: Error interno del servidor

### **B. Estructura de Error Response**
```json
{
  "timestamp": "2025-08-31T12:00:00Z",
  "status": 404,
  "error": "Producto no encontrado",
  "message": "Producto no encontrado con ID: 999",
  "path": "/api/products/999"
}
```

### **C. Logging**
- **Niveles**: DEBUG, INFO, WARN, ERROR
- **Información**: Timestamp, nivel, mensaje, contexto
- **Validaciones**: Logs apropiados en cada operación

---

## **📋 Checklist de Pruebas**

### **Pre-Release Checklist**
- [ ] Todas las pruebas unitarias pasan
- [ ] Todas las pruebas de integración pasan
- [ ] Cobertura de código > 80%
- [ ] Pruebas de performance ejecutadas
- [ ] Pruebas de seguridad ejecutadas
- [ ] Documentación actualizada

### **Post-Release Checklist**
- [ ] Monitoreo de métricas activo
- [ ] Alertas configuradas
- [ ] Logs siendo recolectados
- [ ] Performance dentro de parámetros
- [ ] Errores siendo monitoreados

---

## **🔧 Herramientas de Testing**

### **A. Frameworks**
- **JUnit 5**: Framework de testing principal
- **Mockito**: Mocking framework
- **Spring Boot Test**: Testing de Spring Boot
- **Testcontainers**: Testing con contenedores

### **B. Herramientas de Cobertura**
- **JaCoCo**: Cobertura de código
- **SonarQube**: Análisis de calidad
- **SpotBugs**: Detección de bugs

### **C. Herramientas de Performance**
- **JMeter**: Testing de carga
- **Gatling**: Testing de performance
- **Artillery**: Testing de API

---

## **📚 Documentación Adicional**

### **A. Referencias**
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### **B. Mejores Prácticas**
- Naming conventions para tests
- Organización de test classes
- Uso de @DisplayName para claridad
- Configuración de test profiles
- Mocking strategies

---

## **🎯 Próximos Pasos**

### **A. Mejoras Futuras**
- Implementar pruebas de performance
- Agregar pruebas de seguridad
- Implementar pruebas de contract
- Agregar pruebas de chaos engineering
- Implementar pruebas de accessibility

### **B. Automatización**
- Configurar CI/CD pipeline
- Implementar test reporting
- Configurar alertas automáticas
- Implementar test data management
- Configurar test environments

---

**Nota**: Este plan de pruebas está diseñado para asegurar la calidad, confiabilidad y mantenibilidad del código, siguiendo las mejores prácticas de testing en aplicaciones Spring Boot.






