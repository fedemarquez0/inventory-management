# Sistema de Gestión de Inventario

Sistema de gestión de inventario para cadenas de tiendas minoristas construido con **Java 17** y **Spring Boot 3.2**, implementando arquitectura hexagonal, control de concurrencia optimista y autenticación JWT.

## Características Principales

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- **Control de Concurrencia**: Implementación de Optimistic Locking con `@Version`
- **Seguridad JWT**: Autenticación basada en tokens Bearer
- **Documentación API**: Swagger/OpenAPI 3.0 completamente integrado
- **Manejo de Errores**: Sistema centralizado con códigos de error específicos
- **Logging**: Sistema de logs estructurado con SLF4J
- **Tests**: Suite completa de pruebas unitarias con JUnit 5 y Mockito

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **JWT (jjwt 0.12.3)**
- **SQLite**
- **Lombok**
- **Swagger/OpenAPI**
- **Maven**
- **JUnit 5**
- **Mockito**

## Modelo de Datos

### Tablas

#### stores
- `id` (PK, BIGINT)
- `name` (VARCHAR, UNIQUE)
- `is_active` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### products
- `id` (PK, BIGINT)
- `sku` (VARCHAR, UNIQUE)
- `name` (VARCHAR)
- `description` (VARCHAR)
- `is_active` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### inventory
- `id` (PK, BIGINT)
- `product_id` (FK → products)
- `store_id` (FK → stores)
- `available_qty` (INTEGER, >= 0)
- `version` (INTEGER) - Control de concurrencia optimista
- `updated_at` (TIMESTAMP)

## API Endpoints

### Autenticación

#### POST `/api/auth/login`
Autenticación de usuario y obtención de token JWT.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin"
}
```

**Usuarios de prueba:**
- `admin` / `admin123`
- `store1` / `store123`
- `store2` / `store123`

### Inventario

#### GET `/api/inventory/{productSku}/stores`
Obtiene el stock del producto en todas las tiendas.

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": 1,
    "productSku": "LAPTOP-001",
    "productName": "Gaming Laptop",
    "storeId": 1,
    "storeName": "Store Downtown",
    "availableQty": 10,
    "version": 0,
    "updatedAt": "2025-10-04T10:30:00"
  }
]
```

#### GET `/api/inventory/{productSku}/stores/{storeId}`
Obtiene el stock del producto en una tienda específica.

**Headers:**
```
Authorization: Bearer {token}
```

#### PUT `/api/inventory/{productSku}/stores/{storeId}`
Establece el stock absoluto de un producto en una tienda.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "availableQty": 25
}
```

#### POST `/api/inventory/{productSku}/stores/{storeId}/adjustments`
Ajusta el inventario (positivo para entradas, negativo para salidas).

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "adjustment": -5
}
```

## Control de Concurrencia

El sistema implementa **Optimistic Locking** usando la anotación `@Version` de JPA:

- Cada registro de inventario tiene un campo `version`
- Al actualizar, JPA verifica que la versión coincida
- Si hay conflicto, lanza `ObjectOptimisticLockingFailureException`
- El servicio reintenta automáticamente hasta 3 veces con backoff de 100ms

### Ejemplo de Flujo Concurrente

1. Usuario A lee inventario (version=0, qty=10)
2. Usuario B lee inventario (version=0, qty=10)
3. Usuario A actualiza qty=8 (version se incrementa a 1) ✓
4. Usuario B intenta actualizar qty=9 (espera version=0, pero es 1) ✗
5. Sistema reintenta automáticamente con datos actualizados

## Manejo de Errores

### Códigos de Error

#### Inventario (INV-XXX)
- `INV-001`: Product not found
- `INV-002`: Store not found
- `INV-003`: Inventory not found
- `INV-004`: Insufficient stock
- `INV-005`: Negative quantity not allowed
- `INV-006`: Concurrent modification detected
- `INV-007`: Product already exists
- `INV-008`: Store already exists
- `INV-009`: Invalid adjustment

#### Autenticación (AUTH-XXX)
- `AUTH-001`: Authentication failed
- `AUTH-002`: Invalid token
- `AUTH-003`: Session expired
- `AUTH-004`: Unauthorized access
- `AUTH-005`: Invalid credentials

#### Validación (VAL-XXX)
- `VAL-001`: Validation error
- `VAL-002`: Invalid request parameters

#### Sistema (SYS-XXX)
- `SYS-001`: Internal server error
- `SYS-002`: Database error
- `SYS-003`: Service unavailable

### Formato de Respuesta de Error

```json
{
  "errorCode": "INV-004",
  "message": "Insufficient stock available",
  "details": "Current: 5, Adjustment: -10",
  "timestamp": "2025-10-04T10:30:00",
  "path": "/api/inventory/LAPTOP-001/stores/1/adjustments",
  "validationErrors": []
}
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+

### Pasos

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd inventory-management
```

2. **Compilar el proyecto**
```bash
mvn clean install
```

3. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

4. **Acceder a la aplicación**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## Ejecutar Tests

```bash
mvn test
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/inventory/management/
│   │   ├── application/
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── mapper/           # Mappers entre entidades y DTOs
│   │   │   └── service/          # Servicios de aplicación
│   │   ├── domain/
│   │   │   ├── model/            # Entidades del dominio
│   │   │   └── port/             # Interfaces de puertos (hexagonal)
│   │   ├── infrastructure/
│   │   │   ├── adapter/
│   │   │   │   ├── input/rest/   # Controladores REST
│   │   │   │   └── output/persistence/ # Repositorios JPA
│   │   │   ├── config/           # Configuraciones
│   │   │   ├── exception/        # Manejo de excepciones
│   │   │   └── security/         # Seguridad y JWT
│   │   └── common/
│   │       ├── constant/         # Constantes (ErrorCode)
│   │       └── util/             # Utilidades
│   └── resources/
│       ├── application.yml       # Configuración principal
│       └── data.sql             # Datos iniciales
└── test/
    └── java/com/inventory/management/
        ├── controller/          # Tests de controladores
        └── service/             # Tests de servicios
```

## Configuración

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:sqlite:inventory.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080

jwt:
  secret: mySecretKeyForInventoryManagementSystemThatIsLongEnoughForHS256Algorithm
  expiration: 86400000  # 24 horas

logging:
  level:
    com.inventory.management: DEBUG
  file:
    name: logs/inventory-management.log
```

## Datos de Prueba

El sistema viene con datos de prueba precargados:

### Tiendas
- Store Downtown (ID: 1)
- Store Mall (ID: 2)
- Store Online (ID: 3)

### Productos
- LAPTOP-001: Gaming Laptop
- PHONE-001: Smartphone Pro
- TABLET-001: Tablet Plus
- HEADSET-001: Wireless Headset

## Características de Seguridad

1. **Autenticación JWT**: Tokens con expiración de 24 horas
2. **Endpoints Protegidos**: Todos los endpoints de inventario requieren autenticación
3. **Password Encoding**: BCrypt para hash de contraseñas
4. **CORS**: Configurado para permitir requests cross-origin
5. **Session Stateless**: No se mantiene estado de sesión en el servidor

## Logging

Los logs se guardan en:
- Consola: Nivel INFO
- Archivo: `logs/inventory-management.log` con nivel DEBUG
- SQL queries: DEBUG (útil para troubleshooting)

## Buenas Prácticas Implementadas

1. **Arquitectura Hexagonal**: Separación de responsabilidades
2. **Inyección de Dependencias**: Constructor injection con Lombok
3. **Manejo de Excepciones**: Centralizado con `@RestControllerAdvice`
4. **Validación**: Bean Validation con anotaciones Jakarta
5. **Transacciones**: `@Transactional` con niveles apropiados
6. **DTOs**: Separación entre entidades y objetos de transferencia
7. **Retry Pattern**: Reintentos automáticos para conflictos de concurrencia
8. **Logging**: Estructurado y con niveles apropiados
9. **Tests**: Cobertura de servicios y controladores

## Mejoras Futuras

- [ ] Implementar cache con Redis
- [ ] Agregar métricas con Micrometer/Prometheus
- [ ] Implementar Circuit Breaker con Resilience4j
- [ ] Agregar integración con sistema de eventos (Kafka/RabbitMQ)
- [ ] Implementar auditoría completa con Spring Data Envers
- [ ] Agregar soporte para múltiples bases de datos
- [ ] Implementar API Gateway
- [ ] Agregar monitoreo con ELK Stack

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.
