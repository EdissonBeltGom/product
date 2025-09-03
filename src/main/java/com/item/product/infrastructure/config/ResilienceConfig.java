package com.item.product.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de patrones de resiliencia usando Resilience4j.
 * Define configuraciones para Circuit Breaker, Retry, Timeout y otros patrones.
 */
@Configuration
public class ResilienceConfig {
    
    /**
     * Configuración del Circuit Breaker para servicios de productos
     */
    @Bean
    public CircuitBreakerConfig productServiceCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // Umbral de fallos para abrir el circuito (50% de fallos)
                .failureRateThreshold(50)
                // Número mínimo de llamadas antes de calcular el umbral de fallos
                .minimumNumberOfCalls(5)
                // Tiempo de espera antes de cambiar de OPEN a HALF_OPEN
                .waitDurationInOpenState(Duration.ofSeconds(30))
                // Tamaño de la ventana deslizante para calcular fallos
                .slidingWindowSize(10)
                // Número de llamadas permitidas en estado HALF_OPEN
                .permittedNumberOfCallsInHalfOpenState(3)
                // Excepciones que no se consideran fallos
                .ignoreExceptions(
                    IllegalArgumentException.class,
                    java.util.NoSuchElementException.class
                )
                // Excepciones que se consideran fallos
                .recordExceptions(
                    RuntimeException.class,
                    Exception.class
                )
                .build();
    }
    
    /**
     * Configuración del Circuit Breaker para servicios externos
     */
    @Bean
    public CircuitBreakerConfig externalServiceCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // Umbral más estricto para servicios externos
                .failureRateThreshold(30)
                // Más llamadas antes de calcular el umbral
                .minimumNumberOfCalls(10)
                // Tiempo de espera más largo para servicios externos
                .waitDurationInOpenState(Duration.ofSeconds(120))
                // Ventana más grande para servicios externos
                .slidingWindowSize(20)
                // Número de llamadas permitidas en estado HALF_OPEN
                .permittedNumberOfCallsInHalfOpenState(5)
                // Excepciones específicas para servicios externos
                .ignoreExceptions(
                    IllegalArgumentException.class
                )
                .recordExceptions(
                    java.net.ConnectException.class,
                    java.net.SocketTimeoutException.class,
                    java.io.IOException.class,
                    RuntimeException.class
                )
                .build();
    }
    
    /**
     * Configuración del Circuit Breaker para el repositorio
     */
    @Bean
    public CircuitBreakerConfig repositoryCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // Umbral más permisivo para el repositorio local
                .failureRateThreshold(70)
                // Menos llamadas antes de calcular el umbral
                .minimumNumberOfCalls(3)
                // Tiempo de espera más corto para el repositorio
                .waitDurationInOpenState(Duration.ofSeconds(15))
                // Ventana más pequeña para el repositorio
                .slidingWindowSize(5)
                // Número de llamadas permitidas en estado HALF_OPEN
                .permittedNumberOfCallsInHalfOpenState(2)
                // Excepciones específicas para el repositorio
                .ignoreExceptions(
                    IllegalArgumentException.class,
                    java.util.NoSuchElementException.class
                )
                .recordExceptions(
                    java.io.IOException.class,
                    RuntimeException.class
                )
                .build();
    }
    
    /**
     * Configuración del Time Limiter para servicios
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                // Timeout de 5 segundos para operaciones
                .timeoutDuration(Duration.ofSeconds(5))
                // Cancelar automáticamente si se excede el timeout
                .cancelRunningFuture(true)
                .build();
    }
}
