# Product API - Arquitectura Limpia en Cebolla

Este proyecto implementa una API REST para **consulta de productos** utilizando la **Arquitectura Limpia en Cebolla (Onion Architecture)** con Spring Boot y persistencia en archivos JSON.

## 🏗️ Arquitectura

La aplicación está estructurada en capas concéntricas siguiendo los principios de la Arquitectura Limpia:

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │   Controllers   │  │   Repositories  │  │   Config     │  │
│  └─────────────────┘  └─────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │   Use Cases     │  │      DTOs       │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │     Models      │  │  Repositories   │  │   Services   │  │
│  └─────────────────┘  └─────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Capas de la Arquitectura

#### 1. **Domain Layer** (Capa más interna)
- **Models**: Entidades de dominio puras sin dependencias externas
- **Repositories**: Interfaces que definen contratos para acceso a datos
- **Services**: Lógica de negocio del dominio

#### 2. **Application Layer**
- **Use Cases**: Orquestan la lógica de negocio
- **DTOs**: Objetos de transferencia de datos

#### 3. **Infrastructure Layer** (Capa más externa)
- **Controllers**: Endpoints REST de consulta
- **Repositories**: Implementaciones concretas de acceso a datos (JSON)
- **Config**: Configuraciones de la aplicación

## 🚀 Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.5.5**
- **Jackson** (para manejo de JSON)
- **SpringDoc OpenAPI** (Swagger UI)
- **Lombok**
- **Maven**

## 📁 Estructura del Proyecto

```
src/main/java/com/item/product/
├── domain/
│   ├── model/
│   │   └── Product.java
│   ├── repository/
│   │   └── ProductRepository.java
│   └── service/
├── application/
│   ├── usecase/
│   │   └── GetProductUseCase.java
│   └── dto/
│       └── ProductResponseDto.java
└── infrastructure/
    ├── controller/
    │   └── ProductController.java
    ├── persistence/
    │   └── ProductRepositoryAdapter.java
    └── config/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        └── OpenApiConfig.java

data/
└── products.json
```

## 🛠️ Instalación y Ejecución

### Prerrequisitos
- Java 21
- Maven 3.6+

### Pasos para ejecutar

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd product
```

2. **Compilar el proyecto**
```bash
mvn clean compile
```

3. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

4. **Acceder a la aplicación**
- API REST: http://localhost:8090
- **Swagger UI**: http://localhost:8090/ProductsApi/swagger-ui/index.html


## 📚 API Endpoints

### Productos (Solo Consulta)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Obtener todos los productos |
| GET | `/api/products/{id}` | Obtener producto por ID |
| GET | `/api/products/{id}/similar` | Buscar productos similares por categoría del producto consultado |
| GET | `/api/products/category/{category}` | Obtener productos por categoría |



### Ejemplo de uso

#### Obtener todos los productos
```bash
curl http://localhost:8090/api/products
```

#### Obtener un producto por ID
```bash
curl http://localhost:8090/api/products/1
```

#### Obtener productos por categoría
```bash
curl http://localhost:8090/api/products/category/Electrónicos
```

#### Obtener productos similares
```bash
curl http://localhost:8090/api/1/similar
```


## 🔍 Swagger UI

La aplicación incluye **Swagger UI** para documentación interactiva de la API:

### Acceso a Swagger
- **URL**: http://localhost:8090/ProductsApi/swagger-ui/index.html
- **Descripción**: Interfaz web interactiva para probar todos los endpoints de consulta
- **Características**:
  - Documentación completa de todos los endpoints de consulta
  - Ejemplos de respuestas
  - Interfaz para ejecutar peticiones directamente
  - Esquemas de datos detallados
  - Códigos de respuesta documentados

### Características de la documentación
- ✅ **Endpoints de consulta documentados** con descripciones detalladas
- ✅ **Parámetros validados** con ejemplos
- ✅ **Respuestas documentadas** con códigos de estado
- ✅ **Esquemas de datos** para DTOs de respuesta
- ✅ **Ejemplos de uso** para cada operación
- ✅ **Validaciones** mostradas en la documentación

## 💾 Persistencia

La aplicación utiliza **persistencia en archivos JSON** localizados en `./data/products.json`. Esta implementación:

- **Es simple y portable**: No requiere base de datos
- **Es fácil de debuggear**: Los datos son legibles directamente
- **Mantiene la arquitectura limpia**: El dominio no conoce detalles de persistencia
- **Es thread-safe**: Usa locks para operaciones concurrentes
- **Es configurable**: La ruta del archivo se puede configurar en `application.properties`

### Estructura del archivo JSON
```json
[
  {
  "id": "1",
  "title": "Laptop Gaming Pro",
  "price": 1299.99,
  "currency": "USD",
  "description": "Laptop para gaming de alto rendimiento con las últimas tecnologías",
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "condition": "Nuevo",
  "stock": 15,
  "category": "Electrónicos",
  "seller": {
    "id": "seller123",
    "name": "TechStore",
    "rating": 4.5
  },
  "specifications": [
    {
      "name": "Procesador",
      "value": "Intel i7"
    },
    {
      "name": "RAM",
      "value": "16GB"
    }
  ],
  "brand": "GamingPro",
  "createdAt": "2025-09-03T19:28:55.870Z",
  "updatedAt": "2025-09-03T19:28:55.870Z",
  "available": true
}
]
```

## 🧪 Testing

Para ejecutar las pruebas:

```bash
mvn test
```

## 🔧 Configuración

La configuración se encuentra en `src/main/resources/application.properties`:

- **Puerto**: 8090
- **Archivo de datos**: `./data/products.json`
- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/api-docs`
- **Logging**: DEBUG para desarrollo

## 📋 Principios de la Arquitectura Limpia

1. **Independencia de frameworks**: El dominio no depende de frameworks externos
2. **Testabilidad**: Fácil de probar gracias a la inversión de dependencias
3. **Independencia de UI**: La lógica de negocio es independiente de la interfaz
4. **Independencia de persistencia**: El dominio no conoce detalles de almacenamiento
5. **Independencia de agentes externos**: El dominio no depende de servicios externos

## 🔄 Ventajas de la Persistencia en JSON

- **Simplicidad**: No requiere configuración de base de datos
- **Portabilidad**: Los datos se pueden mover fácilmente
- **Legibilidad**: Los datos son humanos legibles
- **Versionado**: Fácil de versionar con Git
- **Backup**: Fácil de hacer backup y restore
- **Desarrollo**: Ideal para desarrollo y testing

## 🎯 Ventajas de Swagger UI

- **Documentación interactiva**: Prueba endpoints directamente desde el navegador
- **Auto-generada**: Se actualiza automáticamente con el código
- **Validación visual**: Muestra esquemas y validaciones
- **Ejemplos incluidos**: Ejemplos de peticiones y respuestas
- **Fácil integración**: Se integra perfectamente con Spring Boot
- **Estándar OpenAPI**: Compatible con herramientas de terceros

## 🎯 Características de la API de Consulta

- **Solo operaciones de lectura**: No permite modificar datos
- **Múltiples filtros**: Por ID, categoría, disponibilidad y búsqueda por nombre
- **Respuestas consistentes**: Formato JSON estandarizado
- **Documentación completa**: Swagger UI con ejemplos
- **Arquitectura limpia**: Separación clara de responsabilidades
