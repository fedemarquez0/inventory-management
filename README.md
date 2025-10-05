# Inventory Management System - Reactive Version

Sistema de gestión de inventario con arquitectura hexagonal y programación reactiva usando Spring WebFlux.

## 🚀 Características Principales

- **Arquitectura Hexagonal**: Separación clara de responsabilidades
- **Programación Reactiva**: Uso de Reactor (Mono/Flux) para operaciones no bloqueantes
- **Base de Datos Reactiva**: R2DBC con SQLite para persistencia reactiva
- **Autenticación JWT**: Seguridad reactiva con Spring Security WebFlux
- **Control de Concurrencia**: Optimistic locking con retry automático
- **Documentación API**: Swagger/OpenAPI integrado
- **Manejo de Errores**: Error handler personalizado con códigos de error específicos
- **Logs**: Sistema de logging completo con SLF4J y Logback

## 📋 Requisitos

- Java 21
- Maven 3.6+

## 🛠️ Tecnologías

- Spring Boot 3.5.6
- Spring WebFlux (Programación Reactiva)
- Spring Data R2DBC (Base de datos reactiva)
- Spring Security (Seguridad reactiva)
- R2DBC SQLite Driver
- JWT (jsonwebtoken)
- Lombok
- Swagger/OpenAPI
- Project Reactor

## 🏗️ Arquitectura

```
src/main/java/com/meli/inventorymanagement/
├── application/              # Capa de aplicación
│   ├── dto/                 # Data Transfer Objects
│   ├── mapper/              # Mappers de entidad a DTO
│   └── service/             # Servicios de aplicación (lógica reactiva)
├── domain/                   # Capa de dominio
│   └── model/               # Entidades de dominio (R2DBC)
├── infrastructure/           # Capa de infraestructura
│   ├── adapter/
│   │   ├── input/rest/      # Controladores REST (WebFlux)
│   │   └── output/persistence/ # Repositorios R2DBC
│   ├── config/              # Configuraciones
│   ├── exception/           # Manejo de excepciones reactivo
│   └── security/            # Seguridad reactiva y JWT
└── common/
    └── constant/            # Constantes y códigos de error
```

## 🔧 Instalación y Ejecución

1. **Clonar el repositorio**
```bash
cd inventory-management
```

2. **Compilar el proyecto**
```bash
mvnw clean install
```

3. **Ejecutar la aplicación**
```bash
mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Documentación API (Swagger)

Una vez iniciada la aplicación, acceder a:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## 🔐 Autenticación

### Usuarios de Prueba

| Username | Password | Role | Acceso |
|----------|----------|------|--------|
| admin | 12345 | ADMIN | Todas las tiendas |
| user_dinosaurio | 12345 | STORE_USER | Shopping Dinosaurio Mall |
| user_maipu | 12345 | STORE_USER | Centro Maipu 712 |
| user_nuevo_centro | 12345 | STORE_USER | Nuevo Centro Shopping |

### Obtener Token JWT

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "12345"
}
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin"
}
```

### Usar el Token

Incluir en el header de las peticiones:
```
Authorization: Bearer {token}
```

## 🎯 Endpoints Principales

### Inventario

- **GET** `/api/inventory/{productSku}/stores` - Listar inventario del producto en todas las tiendas (Admin)
- **GET** `/api/inventory/{productSku}/stores/{storeId}` - Ver inventario específico
- **PUT** `/api/inventory/{productSku}/stores/{storeId}` - Establecer cantidad absoluta
- **POST** `/api/inventory/{productSku}/stores/{storeId}/adjustments` - Ajustar cantidad (+ o -)

## ⚡ Programación Reactiva

### Características Reactivas

1. **Controladores No Bloqueantes**: Retornan `Mono<T>` o `Flux<T>`
2. **Repositorios Reactivos**: Extienden `R2dbcRepository`
3. **Servicios Reactivos**: Composición de operaciones con operadores reactivos
4. **Seguridad Reactiva**: WebFilter y ReactiveSecurityContextHolder
5. **Retry Reactivo**: Manejo de fallos de optimistic locking con backoff

### Ejemplo de Flujo Reactivo

```java
public Mono<InventoryResponse> updateInventory(String sku, Long storeId, InventoryUpdateRequest request) {
    return Mono.zip(
            productRepository.findBySku(sku),
            storeRepository.findById(storeId)
    )
    .flatMap(tuple -> {
        // Operación de actualización
        return inventoryRepository.save(inventory);
    })
    .flatMap(this::enrichInventoryWithRelations)
    .map(inventoryMapper::toResponse)
    .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
    .onErrorMap(/* manejo de errores */);
}
```

## 🔄 Control de Concurrencia

El sistema implementa **Optimistic Locking** con versioning automático:

- Cada operación de actualización incrementa la versión
- Conflictos de concurrencia generan `OptimisticLockingFailureException`
- Retry automático con backoff exponencial (3 intentos)
- Sin bloqueos de base de datos - máxima concurrencia

## 📊 Base de Datos

### SQLite Reactivo (R2DBC)

- Persistencia en archivo: `inventory.db`
- Inicialización automática con `schema.sql` y `data.sql`
- Pool de conexiones configurado (10-20 conexiones)

### Entidades Principales

- **Product**: Productos (SKU único)
- **Store**: Tiendas
- **Inventory**: Stock por producto y tienda
- **User**: Usuarios del sistema
- **UserStorePermission**: Permisos de acceso

## 🛡️ Seguridad

### Configuración Reactiva

- Spring Security WebFlux
- JWT con expiración configurable (24h por defecto)
- Filtros reactivos (no bloqueantes)
- Control de permisos por tienda con AOP reactivo

### Roles y Permisos

- **ADMIN**: Acceso total a todas las tiendas
- **STORE_USER**: Acceso solo a tiendas asignadas

## 📝 Logging

Configuración en `application.yml`:
- Logs en consola y archivo rotativo
- Nivel DEBUG para el paquete principal
- Archivo: `logs/inventory-management.log`
- Rotación: 50MB por archivo, 30 días de retención

## 🧪 Testing

```bash
# Ejecutar tests
mvnw test

# Con cobertura
mvnw test jacoco:report
```

## ⚙️ Configuración

Archivo: `src/main/resources/application.yml`

```yaml
spring:
  r2dbc:
    url: r2dbc:sqlite:inventory.db
    pool:
      initial-size: 10
      max-size: 20

jwt:
  secret: mySecretKeyForInventoryManagementSystemThatIsLongEnoughForHS256Algorithm
  expiration: 86400000 # 24 horas

server:
  port: 8080
```

## 🚨 Manejo de Errores

Códigos de error personalizados:

- `PRODUCT_NOT_FOUND` (404)
- `STORE_NOT_FOUND` (404)
- `INVENTORY_NOT_FOUND` (404)
- `INSUFFICIENT_STOCK` (409)
- `INVALID_CREDENTIALS` (401)
- `STORE_PERMISSION_DENIED` (403)
- `OPTIMISTIC_LOCK_FAILURE` (409)
- Y más...

## 📦 Estructura de Respuestas

### Éxito
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 25,
  "version": 0,
  "updatedAt": "2025-10-05T10:30:00"
}
```

### Error
```json
{
  "errorCode": "PRODUCT_NOT_FOUND",
  "message": "Product not found",
  "details": "Product with SKU ABC-123 not found",
  "timestamp": "2025-10-05T10:30:00",
  "path": "/api/inventory/ABC-123/stores/1"
}
```

## 🔍 Monitoreo

Actuator endpoints disponibles:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/loggers`

## 📈 Rendimiento

Ventajas de la programación reactiva:

- **No bloqueante**: Miles de requests concurrentes con pocos threads
- **Backpressure**: Control de flujo automático
- **Composición**: Operaciones encadenadas eficientemente
- **Escalabilidad**: Mejor uso de recursos del sistema

## 🤝 Contribuir

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto es de código abierto para fines educativos.

## 👥 Contacto

Para consultas y soporte: inventory@example.com

---

**Desarrollado con ❤️ usando Spring WebFlux y Project Reactor**

