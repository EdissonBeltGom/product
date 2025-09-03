package com.item.product.infrastructure.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Servicio para manejar mensajes del sistema.
 * Proporciona métodos para obtener mensajes localizados.
 */
@Service
public class MessageService {
    
    private final MessageSource messageSource;
    
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Obtiene un mensaje por su clave usando el locale actual.
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
    
    /**
     * Obtiene un mensaje por su clave con argumentos usando el locale actual.
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * Obtiene un mensaje por su clave usando un locale específico.
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
    
    /**
     * Obtiene un mensaje por su clave con argumentos usando un locale específico.
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
    
    /**
     * Obtiene un mensaje de error.
     */
    public String getErrorMessage(String key) {
        return getMessage(key);
    }
    
    /**
     * Obtiene un mensaje de error con argumentos.
     */
    public String getErrorMessage(String key, Object... args) {
        return getMessage(key, args);
    }
    
    /**
     * Obtiene un mensaje de validación.
     */
    public String getValidationMessage(String key) {
        return getMessage(key);
    }
    
    /**
     * Obtiene un mensaje de validación con argumentos.
     */
    public String getValidationMessage(String key, Object... args) {
        return getMessage(key, args);
    }
    
    /**
     * Obtiene un mensaje de log.
     */
    public String getLogMessage(String key) {
        return getMessage(key);
    }
    
    /**
     * Obtiene un mensaje de log con argumentos.
     */
    public String getLogMessage(String key, Object... args) {
        return getMessage(key, args);
    }
}
