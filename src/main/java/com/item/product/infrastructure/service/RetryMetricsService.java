package com.item.product.infrastructure.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para monitorear métricas de Retry Pattern.
 * Proporciona información sobre reintentos, fallos y estadísticas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryMetricsService {
    
    private final RetryRegistry retryRegistry;
    private final Map<String, RetryMetrics> metricsCache = new ConcurrentHashMap<>();
    
    @EventListener(ApplicationReadyEvent.class)
    public void setupRetryMetrics() {
        log.info("Configurando métricas de Retry Pattern...");
        
        // Configurar listeners para cada instancia de retry
        setupRetryListener("repositoryRetry");
        setupRetryListener("writeOperation");
        setupRetryListener("externalServiceRetry");
        setupRetryListener("businessServiceRetry");
        setupRetryListener("productService");
        setupRetryListener("similarProductsService");
        
        log.info("Métricas de Retry Pattern configuradas exitosamente");
    }
    
    /**
     * Configura listeners para una instancia específica de retry
     */
    private void setupRetryListener(String retryName) {
        try {
            Retry retry = retryRegistry.retry(retryName);
            
            retry.getEventPublisher()
                .onRetry(event -> {
                    log.warn("Retry attempt {} for {}. Error: {}", 
                        event.getNumberOfRetryAttempts(), retryName, event.getLastThrowable().getMessage());
                    updateMetrics(retryName, event.getNumberOfRetryAttempts(), false);
                })
                .onError(event -> {
                    log.error("Retry failed after {} attempts for {}. Final error: {}", 
                        event.getNumberOfRetryAttempts(), retryName, event.getLastThrowable().getMessage());
                    updateMetrics(retryName, event.getNumberOfRetryAttempts(), true);
                })
                .onSuccess(event -> {
                    log.info("Retry succeeded for {} after {} attempts", retryName, event.getNumberOfRetryAttempts());
                    updateMetrics(retryName, event.getNumberOfRetryAttempts(), false);
                });
                
        } catch (Exception e) {
            log.warn("No se pudo configurar listener para retry: {}", retryName);
        }
    }
    
    /**
     * Actualiza las métricas de retry
     */
    private void updateMetrics(String retryName, int attempts, boolean failed) {
        RetryMetrics metrics = metricsCache.computeIfAbsent(retryName, k -> new RetryMetrics());
        
        if (failed) {
            metrics.incrementFailedAttempts();
        } else {
            metrics.incrementSuccessfulAttempts();
        }
        
        metrics.updateMaxAttempts(attempts);
        metrics.updateTotalAttempts(attempts);
    }
    
    /**
     * Obtiene las métricas de retry para una instancia específica
     */
    public RetryMetrics getRetryMetrics(String retryName) {
        return metricsCache.getOrDefault(retryName, new RetryMetrics());
    }
    
    /**
     * Obtiene todas las métricas de retry
     */
    public Map<String, RetryMetrics> getAllRetryMetrics() {
        return new ConcurrentHashMap<>(metricsCache);
    }
    
    /**
     * Resetea las métricas de retry
     */
    public void resetRetryMetrics() {
        metricsCache.clear();
        log.info("Métricas de Retry Pattern reseteadas");
    }
    
    /**
     * Clase interna para almacenar métricas de retry
     */
    public static class RetryMetrics {
        private long successfulAttempts = 0;
        private long failedAttempts = 0;
        private int maxAttempts = 0;
        private long totalAttempts = 0;
        
        public void incrementSuccessfulAttempts() {
            successfulAttempts++;
        }
        
        public void incrementFailedAttempts() {
            failedAttempts++;
        }
        
        public void updateMaxAttempts(int attempts) {
            if (attempts > maxAttempts) {
                maxAttempts = attempts;
            }
        }
        
        public void updateTotalAttempts(int attempts) {
            totalAttempts += attempts;
        }
        
        // Getters
        public long getSuccessfulAttempts() { return successfulAttempts; }
        public long getFailedAttempts() { return failedAttempts; }
        public int getMaxAttempts() { return maxAttempts; }
        public long getTotalAttempts() { return totalAttempts; }
        
        public double getSuccessRate() {
            long total = successfulAttempts + failedAttempts;
            return total > 0 ? (double) successfulAttempts / total * 100 : 0.0;
        }
        
        public double getAverageAttempts() {
            long total = successfulAttempts + failedAttempts;
            return total > 0 ? (double) totalAttempts / total : 0.0;
        }
    }
}

