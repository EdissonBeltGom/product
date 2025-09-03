package com.item.product.infrastructure.config;

import com.item.product.infrastructure.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validador de secrets y configuración crítica del sistema.
 * Se ejecuta al inicio de la aplicación para validar que todos los secrets necesarios estén configurados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecretValidator {
    
    private final Environment environment;
    private final SecurityConfig securityConfig;
    private final ExternalServiceConfig externalServiceConfig;
    private final MessageService messageService;
    
    /**
     * Valida los secrets al inicio de la aplicación
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateSecrets() {
        log.info("Iniciando validación de secrets y configuración...");
        
        try {
            validateSecuritySecrets();
            validateExternalServiceSecrets();
            validateEnvironmentSpecificSecrets();
            
            log.info("✅ Validación de secrets completada exitosamente");
        } catch (Exception e) {
            log.error("❌ Error en la validación de secrets: {}", e.getMessage());
            throw new IllegalStateException("Configuración de secrets inválida", e);
        }
    }
    
    /**
     * Valida los secrets de seguridad
     */
    private void validateSecuritySecrets() {
        log.info("Validando secrets de seguridad...");
        
        // Validar API Key
        if (!StringUtils.hasText(securityConfig.getApiKey()) || 
            securityConfig.getApiKey().equals("default-dev-api-key")) {
            log.warn("⚠️ API_KEY no configurada, usando valor por defecto (solo para desarrollo)");
        }
        
        // Validar JWT Secret
        if (!StringUtils.hasText(securityConfig.getJwtSecret()) || 
            securityConfig.getJwtSecret().equals("default-jwt-secret-key-change-in-production")) {
            log.warn("⚠️ JWT_SECRET no configurado, usando valor por defecto (solo para desarrollo)");
        }
        
        // Validar configuración de rate limiting
        if (securityConfig.getRateLimit().getMaxRequestsPerMinute() <= 0) {
            throw new IllegalStateException("maxRequestsPerMinute debe ser mayor a 0");
        }
        
        if (securityConfig.getRateLimit().getMaxRequestsPerHour() <= 0) {
            throw new IllegalStateException("maxRequestsPerHour debe ser mayor a 0");
        }
        
        log.info("✅ Secrets de seguridad validados");
    }
    
    /**
     * Valida los secrets de servicios externos
     */
    private void validateExternalServiceSecrets() {
        log.info("Validando configuración de servicios externos...");
        
        // Validar servicio de recomendación
        validateServiceConfig("Recomendación", 
            externalServiceConfig.getRecommendation().getBaseUrl(),
            externalServiceConfig.getRecommendation().getApiKey());
        
        // Validar servicio de análisis
        if (externalServiceConfig.getAnalytics().isEnabled()) {
            validateServiceConfig("Análisis", 
                externalServiceConfig.getAnalytics().getBaseUrl(),
                externalServiceConfig.getAnalytics().getApiKey());
        }
        
        // Validar servicio de notificación
        if (externalServiceConfig.getNotification().isEnabled()) {
            validateServiceConfig("Notificación", 
                externalServiceConfig.getNotification().getBaseUrl(),
                externalServiceConfig.getNotification().getApiKey());
            
            // Validar configuración de email si está habilitado
            validateEmailConfig();
            
            // Validar configuración de SMS si está habilitado
            validateSmsConfig();
        }
        
        log.info("✅ Configuración de servicios externos validada");
    }
    
    /**
     * Valida la configuración de un servicio específico
     */
    private void validateServiceConfig(String serviceName, String baseUrl, String apiKey) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException(String.format("URL base no configurada para el servicio %s", serviceName));
        }
        
        if (!StringUtils.hasText(apiKey) || apiKey.startsWith("default-")) {
            log.warn("⚠️ API Key no configurada para el servicio {} (solo para desarrollo)", serviceName);
        }
    }
    
    /**
     * Valida la configuración de email
     */
    private void validateEmailConfig() {
        var emailConfig = externalServiceConfig.getNotification().getEmail();
        
        if (!StringUtils.hasText(emailConfig.getSmtpHost())) {
            throw new IllegalStateException("SMTP Host no configurado");
        }
        
        if (emailConfig.getSmtpPort() <= 0 || emailConfig.getSmtpPort() > 65535) {
            throw new IllegalStateException("Puerto SMTP inválido");
        }
        
        if (!StringUtils.hasText(emailConfig.getSmtpUsername())) {
            throw new IllegalStateException("Usuario SMTP no configurado");
        }
        
        if (!StringUtils.hasText(emailConfig.getSmtpPassword()) || 
            emailConfig.getSmtpPassword().equals("default-email-password")) {
            log.warn("⚠️ Contraseña SMTP no configurada (solo para desarrollo)");
        }
    }
    
    /**
     * Valida la configuración de SMS
     */
    private void validateSmsConfig() {
        var smsConfig = externalServiceConfig.getNotification().getSms();
        
        if (!StringUtils.hasText(smsConfig.getServiceUrl())) {
            throw new IllegalStateException("URL del servicio SMS no configurada");
        }
        
        if (!StringUtils.hasText(smsConfig.getApiKey()) || 
            smsConfig.getApiKey().equals("default-sms-api-key")) {
            log.warn("⚠️ API Key SMS no configurada (solo para desarrollo)");
        }
        
        if (!StringUtils.hasText(smsConfig.getFromNumber())) {
            throw new IllegalStateException("Número de teléfono remitente no configurado");
        }
    }
    
    /**
     * Valida secrets específicos del entorno
     */
    private void validateEnvironmentSpecificSecrets() {
        String activeProfile = getActiveProfile();
        log.info("Validando configuración para el perfil: {}", activeProfile);
        
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            validateProductionSecrets();
        } else if ("test".equals(activeProfile)) {
            validateTestSecrets();
        } else {
            log.info("Perfil de desarrollo detectado, validación básica completada");
        }
    }
    
    /**
     * Valida secrets específicos de producción
     */
    private void validateProductionSecrets() {
        log.info("Validando secrets de producción...");
        
        // En producción, los secrets por defecto no deben usarse
        if (securityConfig.getApiKey().equals("default-dev-api-key")) {
            throw new IllegalStateException("API_KEY debe configurarse en producción");
        }
        
        if (securityConfig.getJwtSecret().equals("default-jwt-secret-key-change-in-production")) {
            throw new IllegalStateException("JWT_SECRET debe configurarse en producción");
        }
        
        // Validar que las URLs de servicios externos no sean localhost en producción
        if (externalServiceConfig.getRecommendation().getBaseUrl().contains("localhost")) {
            throw new IllegalStateException("URL del servicio de recomendación no puede ser localhost en producción");
        }
        
        if (externalServiceConfig.getAnalytics().isEnabled() && 
            externalServiceConfig.getAnalytics().getBaseUrl().contains("localhost")) {
            throw new IllegalStateException("URL del servicio de análisis no puede ser localhost en producción");
        }
        
        log.info("✅ Secrets de producción validados");
    }
    
    /**
     * Valida secrets específicos de testing
     */
    private void validateTestSecrets() {
        log.info("Validando configuración de testing...");
        
        // En testing, algunos valores por defecto son aceptables
        if (!StringUtils.hasText(securityConfig.getApiKey())) {
            log.warn("API_KEY no configurada para testing");
        }
        
        if (!StringUtils.hasText(securityConfig.getJwtSecret())) {
            log.warn("JWT_SECRET no configurado para testing");
        }
        
        log.info("✅ Configuración de testing validada");
    }
    
    /**
     * Obtiene el perfil activo
     */
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0];
        }
        return "default";
    }
}
