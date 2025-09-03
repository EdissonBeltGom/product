
package com.item.product.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de seguridad y secrets del sistema.
 * Los valores se obtienen de variables de entorno con valores por defecto para desarrollo.
 */
@Component
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityConfig {
    
    /**
     * API Key para autenticación de servicios externos
     */
    private String apiKey = "default-dev-api-key";
    
    /**
     * Secret para firmar tokens JWT
     */
    private String jwtSecret = "default-jwt-secret-key-change-in-production";
    
    /**
     * Tiempo de expiración del token en segundos (default: 1 hora)
     */
    private int tokenExpiration = 3600;
    
    /**
     * Tiempo de expiración del refresh token en segundos (default: 7 días)
     */
    private int refreshTokenExpiration = 604800;
    
    /**
     * Algoritmo de firma JWT
     */
    private String jwtAlgorithm = "HS256";
    
    /**
     * Issuer del token JWT
     */
    private String jwtIssuer = "product-api";
    
    /**
     * Audience del token JWT
     */
    private String jwtAudience = "product-users";
    
    /**
     * Configuración de rate limiting
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();
    
    /**
     * Configuración de CORS
     */
    private CorsConfig cors = new CorsConfig();
    
    /**
     * Configuración de rate limiting
     */
    @Data
    public static class RateLimitConfig {
        /**
         * Número máximo de requests por minuto por IP
         */
        private int maxRequestsPerMinute = 100;
        
        /**
         * Número máximo de requests por hora por IP
         */
        private int maxRequestsPerHour = 1000;
        
        /**
         * Tiempo de bloqueo en segundos cuando se excede el límite
         */
        private int blockTimeSeconds = 300;
    }
    
    /**
     * Configuración de CORS
     */
    @Data
    public static class CorsConfig {
        /**
         * Orígenes permitidos para CORS
         */
        private String[] allowedOrigins = {"http://localhost:3000", "http://localhost:8080"};
        
        /**
         * Métodos HTTP permitidos
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
        
        /**
         * Headers permitidos
         */
        private String[] allowedHeaders = {"*"};
        
        /**
         * Si se permiten credenciales
         */
        private boolean allowCredentials = true;
        
        /**
         * Tiempo máximo de cache para preflight requests
         */
        private int maxAgeSeconds = 3600;
    }
}
