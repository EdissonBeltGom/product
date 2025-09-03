# Sistema de Mensajes Centralizados

## 📋 Descripción

Este proyecto implementa un sistema de mensajes centralizados que permite la **internacionalización (i18n)** y el **mantenimiento centralizado** de todos los mensajes del sistema.

## 🏗️ Arquitectura

```
src/main/resources/
├── messages.properties          # Mensajes en español (por defecto)
├── messages_en.properties       # Mensajes en inglés
└── messages_pt.properties       # Mensajes en portugués (futuro)

src/main/java/com/item/product/infrastructure/
├── config/
│   ├── MessageKeys.java         # Constantes de claves de mensajes
│   └── MessageConfig.java       # Configuración de MessageSource
└── service/
    └── MessageService.java      # Servicio para obtener mensajes
```

## 🚀 Características

### ✅ **Internacionalización (i18n)**
- Soporte para múltiples idiomas
- Cambio automático según el header `Accept-Language`
- Fallback a español por defecto

### ✅ **Mantenimiento Centralizado**
- Todos los mensajes en archivos `.properties`
- Constantes tipadas en `MessageKeys.java`
- Fácil modificación sin recompilar

### ✅ **Tipos de Mensajes**
- **Errores**: Mensajes de excepción y validación
- **Logs**: Mensajes para logging
- **Validaciones**: Mensajes de validación de formularios
- **Respuestas**: Mensajes de respuesta de API
- **Descripciones**: Descripciones para documentación

## 📝 Uso del Sistema

### 1. **Obtener Mensajes Básicos**

```java
@Service
public class MiServicio {
    
    private final MessageService messageService;
    
    public MiServicio(MessageService messageService) {
        this.messageService = messageService;
    }
    
    public void ejemplo() {
        // Mensaje simple
        String mensaje = messageService.getMessage("error.product.not.found");
        
        // Mensaje con argumentos
        String mensajeConArgs = messageService.getErrorMessage(
            MessageKeys.ERROR_PRODUCT_NOT_FOUND, 
            "123"
        );
    }
}
```

### 2. **Usar Constantes de MessageKeys**

```java
// ✅ RECOMENDADO - Usar constantes
String mensaje = messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND, id);

// ❌ NO RECOMENDADO - Usar strings hardcodeados
String mensaje = messageService.getErrorMessage("error.product.not.found", id);
```

### 3. **Mensajes Específicos por Tipo**

```java
// Mensajes de error
String error = messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_INVALID_ID, id);

// Mensajes de log
String log = messageService.getLogMessage(MessageKeys.LOG_PRODUCT_SAVED, productId);

// Mensajes de validación
String validation = messageService.getValidationMessage(MessageKeys.VALIDATION_PRODUCT_NAME_REQUIRED);
```

### 4. **Internacionalización**

```java
// Usar locale específico
Locale english = new Locale("en");
String englishMessage = messageService.getMessage(
    MessageKeys.ERROR_PRODUCT_NOT_FOUND, 
    english, 
    "123"
);

// Usar locale actual (del header Accept-Language)
String currentMessage = messageService.getErrorMessage(
    MessageKeys.ERROR_PRODUCT_NOT_FOUND, 
    "123"
);
```

## 📁 Estructura de Archivos de Mensajes

### **messages.properties (Español)**

```properties
# =============================================================================
# MENSAJES DE ERROR
# =============================================================================
error.product.not.found=Producto no encontrado con ID: {0}
error.product.invalid.id=ID de producto inválido: {0}
error.stock.insufficient=Stock insuficiente

# =============================================================================
# MENSAJES DE LOG
# =============================================================================
log.product.saved=Producto guardado con ID: {0}
log.product.deleted=Producto eliminado con ID: {0}

# =============================================================================
# MENSAJES DE VALIDACIÓN
# =============================================================================
validation.product.name.required=El nombre del producto es obligatorio
validation.product.price.required=El precio es obligatorio
```

### **messages_en.properties (Inglés)**

```properties
# =============================================================================
# ERROR MESSAGES
# =============================================================================
error.product.not.found=Product not found with ID: {0}
error.product.invalid.id=Invalid product ID: {0}
error.stock.insufficient=Insufficient stock

# =============================================================================
# LOG MESSAGES
# =============================================================================
log.product.saved=Product saved with ID: {0}
log.product.deleted=Product deleted with ID: {0}

# =============================================================================
# VALIDATION MESSAGES
# =============================================================================
validation.product.name.required=Product name is required
validation.product.price.required=Price is required
```

## 🔧 Configuración

### **MessageConfig.java**

```java
@Configuration
public class MessageConfig {
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("es")); // Español por defecto
        return localeResolver;
    }
}
```

## 📋 Convenciones de Nomenclatura

### **Claves de Mensajes**

```
{tipo}.{categoría}.{acción}
```

**Ejemplos:**
- `error.product.not.found`
- `log.product.saved`
- `validation.product.name.required`
- `response.error.validation`

### **Tipos de Mensajes**

| Prefijo | Descripción | Ejemplo |
|---------|-------------|---------|
| `error.` | Errores del sistema | `error.product.not.found` |
| `log.` | Mensajes de logging | `log.product.saved` |
| `validation.` | Validaciones de formularios | `validation.product.name.required` |
| `response.` | Respuestas de API | `response.error.validation` |
| `description.` | Descripciones para documentación | `description.product.title` |

## 🌐 Internacionalización

### **Cambiar Idioma**

El idioma se determina automáticamente por el header HTTP:

```http
Accept-Language: en-US,en;q=0.9,es;q=0.8
```

### **Agregar Nuevo Idioma**

1. **Crear archivo de mensajes**:
   ```
   src/main/resources/messages_fr.properties
   ```

2. **Traducir mensajes**:
   ```properties
   error.product.not.found=Produit non trouvé avec l'ID: {0}
   error.product.invalid.id=ID de produit invalide: {0}
   ```

3. **El sistema lo detectará automáticamente**

## 🔍 Ejemplos de Uso en el Código

### **En Excepciones**

```java
public class ProductNotFoundException extends RuntimeException {
    
    private final String productId;
    
    public ProductNotFoundException(String id) {
        super("Producto no encontrado con ID: " + id); // Mensaje por defecto
        this.productId = id;
    }
    
    public String getProductId() {
        return productId;
    }
}
```

### **En GlobalExceptionHandler**

```java
@ExceptionHandler(ProductNotFoundException.class)
public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
    ErrorResponse errorResponse = ErrorResponse.builder()
            .error(messageService.getErrorMessage(MessageKeys.RESPONSE_ERROR_PRODUCT_NOT_FOUND))
            .message(messageService.getErrorMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND, ex.getProductId()))
            .build();
    
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
}
```

### **En Repositorio**

```java
log.info(messageService.getLogMessage(MessageKeys.LOG_PRODUCT_SAVED, savedProduct.getId()));
log.error(messageService.getErrorMessage(MessageKeys.ERROR_FILE_READ), e);
```

## ✅ Beneficios

1. **Mantenibilidad**: Todos los mensajes en un lugar
2. **Internacionalización**: Soporte para múltiples idiomas
3. **Consistencia**: Mensajes uniformes en toda la aplicación
4. **Flexibilidad**: Fácil cambio de idioma sin recompilar
5. **Tipado**: Constantes que previenen errores de tipeo
6. **Escalabilidad**: Fácil agregar nuevos idiomas

## 🚀 Próximos Pasos

- [ ] Agregar más idiomas (portugués, francés)
- [ ] Crear validadores personalizados con mensajes centralizados
- [ ] Implementar cache de mensajes para mejor rendimiento
- [ ] Agregar soporte para mensajes dinámicos con parámetros complejos

