package com.item.product.infrastructure.config;

/**
 * Constantes para las claves de mensajes del sistema.
 * Centraliza todas las claves utilizadas en los archivos de mensajes.
 */
public final class MessageKeys {
    
    private MessageKeys() {
        // Clase de utilidad, no instanciable
    }
    
    // =============================================================================
    // ERROR MESSAGES
    // =============================================================================
    public static final String ERROR_PRODUCT_NOT_FOUND = "error.product.not.found";
    public static final String ERROR_PRODUCT_INVALID_ID = "error.product.invalid.id";
    public static final String ERROR_INVALID_PRODUCT_ID = "error.invalid.product.id";
    public static final String ERROR_STOCK_INSUFFICIENT = "error.stock.insufficient";
    public static final String ERROR_VALIDATION = "error.validation";
    public static final String ERROR_VALIDATION_INVALID_DATA = "error.validation.invalid.data";
    public static final String ERROR_INTERNAL_SERVER = "error.internal.server";
    public static final String ERROR_UNEXPECTED = "error.unexpected";
    public static final String ERROR_FILE_INIT = "error.file.init";
    public static final String ERROR_FILE_WRITE = "error.file.write";
    public static final String ERROR_FILE_READ = "error.file.read";
    public static final String ERROR_FILE_WRITE_PRODUCTS = "error.file.write.products";
    
    // =============================================================================
    // LOG MESSAGES
    // =============================================================================
    public static final String LOG_FILE_CREATED = "log.file.created";
    public static final String LOG_PRODUCT_SAVED = "log.product.saved";
    public static final String LOG_PRODUCT_DELETED = "log.product.deleted";
    public static final String LOG_FILE_INIT_ERROR = "log.file.init.error";
    public static final String LOG_GETTING_SIMILAR_PRODUCTS = "log.getting.similar.products";
    public static final String LOG_SIMILAR_PRODUCTS_FOUND = "log.similar.products.found";
    
    // =============================================================================
    // VALIDATION MESSAGES
    // =============================================================================
    public static final String VALIDATION_PRODUCT_NAME_REQUIRED = "validation.product.name.required";
    public static final String VALIDATION_PRODUCT_PRICE_REQUIRED = "validation.product.price.required";
    public static final String VALIDATION_PRODUCT_PRICE_POSITIVE = "validation.product.price.positive";
    public static final String VALIDATION_PRODUCT_STOCK_REQUIRED = "validation.product.stock.required";
    public static final String VALIDATION_PRODUCT_STOCK_POSITIVE_OR_ZERO = "validation.product.stock.positive.or.zero";
    public static final String VALIDATION_LIMIT_INVALID = "validation.limit.invalid";
    public static final String VALIDATION_LIMIT_TOO_HIGH = "validation.limit.too.high";
    
    // =============================================================================
    // RESPONSE MESSAGES
    // =============================================================================
    public static final String RESPONSE_ERROR_VALIDATION = "response.error.validation";
    public static final String RESPONSE_ERROR_PRODUCT_NOT_FOUND = "response.error.product.not.found";
    public static final String RESPONSE_ERROR_PRODUCT_INVALID_ID = "response.error.product.invalid.id";
    public static final String RESPONSE_ERROR_ARGUMENT = "response.error.argument";
    public static final String RESPONSE_ERROR_INTERNAL_SERVER = "response.error.internal.server";
    
    // =============================================================================
    // DESCRIPTION MESSAGES
    // =============================================================================
    public static final String DESCRIPTION_PRODUCT_ID = "description.product.id";
    public static final String DESCRIPTION_PRODUCT_TITLE = "description.product.title";
    public static final String DESCRIPTION_PRODUCT_PRICE = "description.product.price";
    public static final String DESCRIPTION_PRODUCT_CURRENCY = "description.product.currency";
    public static final String DESCRIPTION_PRODUCT_DESCRIPTION = "description.product.description";
    public static final String DESCRIPTION_PRODUCT_IMAGES = "description.product.images";
    public static final String DESCRIPTION_PRODUCT_CONDITION = "description.product.condition";
    public static final String DESCRIPTION_PRODUCT_STOCK = "description.product.stock";
    public static final String DESCRIPTION_PRODUCT_CATEGORY = "description.product.category";
    public static final String DESCRIPTION_PRODUCT_SELLER = "description.product.seller";
    public static final String DESCRIPTION_PRODUCT_SPECIFICATIONS = "description.product.specifications";
    public static final String DESCRIPTION_PRODUCT_BRAND = "description.product.brand";
    public static final String DESCRIPTION_PRODUCT_CREATED_AT = "description.product.createdAt";
    public static final String DESCRIPTION_PRODUCT_UPDATED_AT = "description.product.updatedAt";
    public static final String DESCRIPTION_PRODUCT_AVAILABLE = "description.product.available";
}
