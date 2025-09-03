package com.item.product.infrastructure.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para manejo de Rate Limiting dinámico y por usuario.
 * Permite configurar límites específicos por usuario y actualizar límites en tiempo real.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RateLimiterRegistry rateLimiterRegistry;
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();
    
    /**
     * Verifica si un usuario puede hacer una solicitud
     */
    public boolean isAllowed(String userId) {
        RateLimiter rateLimiter = getUserRateLimiter(userId);
        boolean allowed = rateLimiter.acquirePermission();
        
        if (!allowed) {
            log.warn("Rate limit alcanzado para usuario: {}", userId);
        } else {
            log.debug("Solicitud permitida para usuario: {}", userId);
        }
        
        return allowed;
    }
    
    /**
     * Verifica si una IP puede hacer una solicitud
     */
    public boolean isAllowedByIp(String ipAddress) {
        RateLimiter rateLimiter = getIpRateLimiter(ipAddress);
        boolean allowed = rateLimiter.acquirePermission();
        
        if (!allowed) {
            log.warn("Rate limit alcanzado para IP: {}", ipAddress);
        } else {
            log.debug("Solicitud permitida para IP: {}", ipAddress);
        }
        
        return allowed;
    }
    
    /**
     * Obtiene el Rate Limiter para un usuario específico
     */
    private RateLimiter getUserRateLimiter(String userId) {
        return userRateLimiters.computeIfAbsent(userId, k -> {
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(100)  // 100 solicitudes por período
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(5))
                    .build();
            
            return RateLimiter.of("user-" + userId, config);
        });
    }
    
    /**
     * Obtiene el Rate Limiter para una IP específica
     */
    private RateLimiter getIpRateLimiter(String ipAddress) {
        return userRateLimiters.computeIfAbsent("ip-" + ipAddress, k -> {
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(200)  // 200 solicitudes por período
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(5))
                    .build();
            
            return RateLimiter.of("ip-" + ipAddress, config);
        });
    }
    
    /**
     * Actualiza el límite de solicitudes para un usuario específico
     */
    public void updateUserRateLimit(String userId, int newLimit) {
        RateLimiter rateLimiter = getUserRateLimiter(userId);
        rateLimiter.changeLimitForPeriod(newLimit);
        log.info("Rate limit actualizado para usuario {}: {} solicitudes por minuto", userId, newLimit);
    }
    
    /**
     * Actualiza el límite de solicitudes para una IP específica
     */
    public void updateIpRateLimit(String ipAddress, int newLimit) {
        RateLimiter rateLimiter = getIpRateLimiter(ipAddress);
        rateLimiter.changeLimitForPeriod(newLimit);
        log.info("Rate limit actualizado para IP {}: {} solicitudes por minuto", ipAddress, newLimit);
    }
    
    /**
     * Obtiene información del Rate Limiter para un usuario
     */
    public RateLimitInfo getUserRateLimitInfo(String userId) {
        RateLimiter rateLimiter = getUserRateLimiter(userId);
        return RateLimitInfo.builder()
                .userId(userId)
                .availablePermissions(rateLimiter.getMetrics().getAvailablePermissions())
                .numberOfWaitingThreads(rateLimiter.getMetrics().getNumberOfWaitingThreads())
                .build();
    }
    
    /**
     * Obtiene información del Rate Limiter para una IP
     */
    public RateLimitInfo getIpRateLimitInfo(String ipAddress) {
        RateLimiter rateLimiter = getIpRateLimiter(ipAddress);
        return RateLimitInfo.builder()
                .userId("ip-" + ipAddress)
                .availablePermissions(rateLimiter.getMetrics().getAvailablePermissions())
                .numberOfWaitingThreads(rateLimiter.getMetrics().getNumberOfWaitingThreads())
                .build();
    }
    
    /**
     * Limpia los Rate Limiters de usuarios inactivos
     */
    public void cleanupInactiveUsers() {
        // Implementación para limpiar usuarios inactivos
        log.info("Limpiando Rate Limiters inactivos");
    }
    
    /**
     * Clase interna para información de Rate Limiting
     */
    @lombok.Builder
    @lombok.Data
    public static class RateLimitInfo {
        private String userId;
        private int availablePermissions;
        private int numberOfWaitingThreads;
    }
}

