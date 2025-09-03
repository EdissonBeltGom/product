# Gestión de Secrets y Configuración Segura

## 📋 Descripción General

Este documento describe cómo se manejan los secrets y la configuración segura en el proyecto Product API. Se utiliza la estrategia de **Variables de Entorno** para mantener los secrets fuera del código fuente.

## 🏗️ Arquitectura de Secrets

### Estructura de Configuración

```
src/main/resources/
├── application.properties          # Configuración base con variables de entorno
├── application-dev.properties      # Configuración para desarrollo
├── application-prod.properties     # Configuración para producción
└── env.example                    # Ejemplo de variables de entorno
```

### Clases de Configuración

- `SecurityConfig`: Configuración de seguridad y autenticación
- `ExternalServiceConfig`: Configuración de servicios externos
- `SecretValidator`: Validador de secrets al inicio de la aplicación

## 🔧 Configuración por Entorno

### Desarrollo (application-dev.properties)

```properties
# Secrets para desarrollo (valores por defecto seguros)
app.security.api-key=dev-api-key-12345
app.security.jwt-secret=dev-jwt-secret-key-change-in-production
```

### Producción (application-prod.properties)

```properties
# Secrets para producción (requieren variables de entorno)
app.security.api-key=${API_KEY}
app.security.jwt-secret=${JWT_SECRET}
```

## 🚀 Configuración de Variables de Entorno

### 1. Crear archivo .env

```bash
# Copiar el archivo de ejemplo
cp env.example .env

# Editar con valores reales
nano .env
```

### 2. Variables de Entorno Críticas

```bash
# Seguridad
export API_KEY="your-production-api-key-here"
export JWT_SECRET="your-super-secure-jwt-secret-key-here"

# Servicios externos
export RECOMMENDATION_API_KEY="your-recommendation-service-api-key"
export ANALYTICS_API_KEY="your-analytics-service-api-key"
export NOTIFICATION_API_KEY="your-notification-service-api-key"

# Email
export SMTP_PASSWORD="your-smtp-password-here"

# SMS
export SMS_API_KEY="your-sms-provider-api-key"
```

### 3. Configuración en Docker

```dockerfile
# Dockerfile
FROM openjdk:21-jdk-slim
COPY target/product-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  product-service:
    build: .
    environment:
      - API_KEY=${API_KEY}
      - JWT_SECRET=${JWT_SECRET}
      - RECOMMENDATION_API_KEY=${RECOMMENDATION_API_KEY}
    env_file:
      - .env
```

### 4. Configuración en Kubernetes

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: product-secrets
type: Opaque
data:
  api-key: <base64-encoded-value>
  jwt-secret: <base64-encoded-value>
  smtp-password: <base64-encoded-value>
```

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  template:
    spec:
      containers:
      - name: product-service
        env:
        - name: API_KEY
          valueFrom:
            secretKeyRef:
              name: product-secrets
              key: api-key
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: product-secrets
              key: jwt-secret
```

## 🔍 Validación de Secrets

### SecretValidator

El `SecretValidator` se ejecuta automáticamente al inicio de la aplicación y valida:

- ✅ Presencia de secrets críticos
- ✅ Valores por defecto en desarrollo
- ✅ Configuración específica por entorno
- ✅ URLs válidas para servicios externos

### Logs de Validación

```
INFO  - Iniciando validación de secrets y configuración...
INFO  - Validando secrets de seguridad...
WARN  - ⚠️ API_KEY no configurada, usando valor por defecto (solo para desarrollo)
INFO  - ✅ Secrets de seguridad validados
INFO  - Validando configuración de servicios externos...
INFO  - ✅ Configuración de servicios externos validada
INFO  - Validando configuración para el perfil: default
INFO  - Perfil de desarrollo detectado, validación básica completada
INFO  - ✅ Validación de secrets completada exitosamente
```

## 🛡️ Mejores Prácticas de Seguridad

### ✅ Hacer

- Usar variables de entorno en producción
- Validar secrets al inicio de la aplicación
- Usar diferentes secrets por entorno
- Rotar secrets regularmente
- Usar secrets largos y complejos para JWT
- Documentar todos los secrets necesarios

### ❌ No hacer

- Commitear secrets en el código
- Usar secrets hardcodeados
- Usar secrets por defecto en producción
- Loggear secrets sensibles
- Compartir secrets por email/chat
- Usar secrets débiles o cortos

## 🔄 Rotación de Secrets

### Proceso de Rotación

1. **Generar nuevos secrets**
   ```bash
   # Generar nuevo JWT secret
   openssl rand -base64 64
   
   # Generar nueva API key
   openssl rand -hex 32
   ```

2. **Actualizar variables de entorno**
   ```bash
   # Actualizar .env
   JWT_SECRET=new-super-secure-jwt-secret
   API_KEY=new-api-key-here
   ```

3. **Reiniciar aplicación**
   ```bash
   # Reiniciar servicio
   docker-compose restart product-service
   ```

4. **Validar funcionamiento**
   ```bash
   # Verificar logs
   docker-compose logs product-service
   ```

## 🚨 Troubleshooting

### Error: "Configuración de secrets inválida"

**Causa**: Secrets críticos no configurados en producción

**Solución**:
```bash
# Configurar variables de entorno
export API_KEY="your-api-key"
export JWT_SECRET="your-jwt-secret"

# Reiniciar aplicación
./mvnw spring-boot:run
```

### Error: "URL del servicio no puede ser localhost en producción"

**Causa**: URLs de servicios externos apuntan a localhost en producción

**Solución**:
```bash
# Configurar URLs de producción
export RECOMMENDATION_SERVICE_URL="https://recommendation.production.com"
export ANALYTICS_SERVICE_URL="https://analytics.production.com"
```

### Error: "SMTP Host no configurado"

**Causa**: Configuración de email incompleta

**Solución**:
```bash
# Configurar SMTP
export SMTP_HOST="smtp.gmail.com"
export SMTP_USERNAME="noreply@company.com"
export SMTP_PASSWORD="your-smtp-password"
```

## 📚 Referencias

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
- [12 Factor App - Config](https://12factor.net/config)
- [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/)
- [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)

## 🔐 Seguridad Adicional

### Cifrado de Secrets (Opcional)

Para mayor seguridad, se puede implementar cifrado con Jasypt:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

```properties
# application.properties
app.encrypted.password=ENC(encrypted-password-here)
```

```java
// Configuración Jasypt
@Bean
public StringEncryptor stringEncryptor() {
    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    encryptor.setPassword(System.getenv("JASYPT_PASSWORD"));
    return encryptor;
}
```

### Vault Integration (Futuro)

Para proyectos más complejos, considerar HashiCorp Vault:

```java
@VaultPropertySource(value = "secret/product-service")
public class VaultConfig {
    // Configuración automática
}
```


