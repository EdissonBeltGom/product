package com.item.product.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de servicios externos y APIs.
 * Los valores se obtienen de variables de entorno con valores por defecto para desarrollo.
 */
@Component
@ConfigurationProperties(prefix = "app.external")
@Data
public class ExternalServiceConfig {
    
    /**
     * Configuración de servicios de recomendación
     */
    private RecommendationServiceConfig recommendation = new RecommendationServiceConfig();
    
    /**
     * Configuración de servicios de análisis
     */
    private AnalyticsServiceConfig analytics = new AnalyticsServiceConfig();
    
    /**
     * Configuración de servicios de notificación
     */
    private NotificationServiceConfig notification = new NotificationServiceConfig();
    
    /**
     * Configuración del servicio de recomendación
     */
    @Data
    public static class RecommendationServiceConfig {
        /**
         * URL base del servicio de recomendación
         */
        private String baseUrl = "http://localhost:8081";
        
        /**
         * API Key para autenticación
         */
        private String apiKey = "default-recommendation-api-key";
        
        /**
         * Timeout de conexión en milisegundos
         */
        private int connectionTimeout = 5000;
        
        /**
         * Timeout de lectura en milisegundos
         */
        private int readTimeout = 10000;
        
        /**
         * Número máximo de reintentos
         */
        private int maxRetries = 3;
        
        /**
         * Tiempo de espera entre reintentos en milisegundos
         */
        private int retryDelay = 1000;
    }
    
    /**
     * Configuración del servicio de análisis
     */
    @Data
    public static class AnalyticsServiceConfig {
        /**
         * URL base del servicio de análisis
         */
        private String baseUrl = "http://localhost:8082";
        
        /**
         * API Key para autenticación
         */
        private String apiKey = "default-analytics-api-key";
        
        /**
         * Timeout de conexión en milisegundos
         */
        private int connectionTimeout = 3000;
        
        /**
         * Timeout de lectura en milisegundos
         */
        private int readTimeout = 5000;
        
        /**
         * Si el servicio está habilitado
         */
        private boolean enabled = true;
    }
    
    /**
     * Configuración del servicio de notificación
     */
    @Data
    public static class NotificationServiceConfig {
        /**
         * URL base del servicio de notificación
         */
        private String baseUrl = "http://localhost:8083";
        
        /**
         * API Key para autenticación
         */
        private String apiKey = "default-notification-api-key";
        
        /**
         * Timeout de conexión en milisegundos
         */
        private int connectionTimeout = 2000;
        
        /**
         * Timeout de lectura en milisegundos
         */
        private int readTimeout = 3000;
        
        /**
         * Si el servicio está habilitado
         */
        private boolean enabled = true;
        
        /**
         * Configuración de email
         */
        private EmailConfig email = new EmailConfig();
        
        /**
         * Configuración de SMS
         */
        private SmsConfig sms = new SmsConfig();
        
        /**
         * Configuración de email
         */
        @Data
        public static class EmailConfig {
            /**
             * Servidor SMTP
             */
            private String smtpHost = "smtp.gmail.com";
            
            /**
             * Puerto SMTP
             */
            private int smtpPort = 587;
            
            /**
             * Usuario SMTP
             */
            private String smtpUsername = "noreply@product-api.com";
            
            /**
             * Contraseña SMTP
             */
            private String smtpPassword = "default-email-password";
            
            /**
             * Si usar TLS
             */
            private boolean useTls = true;
        }
        
        /**
         * Configuración de SMS
         */
        @Data
        public static class SmsConfig {
            /**
             * API Key del proveedor de SMS
             */
            private String apiKey = "default-sms-api-key";
            
            /**
             * URL del servicio de SMS
             */
            private String serviceUrl = "https://api.twilio.com";
            
            /**
             * Número de teléfono remitente
             */
            private String fromNumber = "+1234567890";
        }
    }
}
