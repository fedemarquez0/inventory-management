# 📖 Documentación Técnica - Sistema de Gestión de Inventario

## Tabla de Contenidos

- [Introducción](#introducción)
- [Contexto del Sistema](#contexto-del-sistema)
- [Pruebas con Postman](#pruebas-con-postman)
- [API Reference](#api-reference)
  - [Autenticación](#autenticación)
  - [Endpoints de Inventario](#endpoints-de-inventario)
- [Modelos de Datos](#modelos-de-datos)
- [Códigos de Error](#códigos-de-error)
- [Diagramas de Secuencia](#diagramas-de-secuencia)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Base de Datos](#base-de-datos)

---

## Introducción

Esta documentación proporciona una referencia completa de la API REST del Sistema de Gestión de Inventario. El sistema está construido con Spring Boot 3.5.6 y Spring WebFlux, implementando programación reactiva completa.

**URL Base:** `http://localhost:8080`

**Características principales:**
- API REST completamente reactiva (Mono/Flux)
- Autenticación JWT con roles (ADMIN, STORE_USER)
- Control de concurrencia con Optimistic Locking
- Documentación interactiva con Swagger/OpenAPI
- Colecciones de Postman listas para usar

---

## Contexto del Sistema

El sistema funciona como **backend centralizado** para una cadena de tiendas minoristas, sirviendo a múltiples tipos de clientes:

### Arquitectura General

```
Tiendas (POS) ──┐
                │
                ├──► Backend Centralizado ◄──── Página Web Pública
                │    (Sistema de Inventario)
Tiendas (POS) ──┘
```

### Actores del Sistema

1. **Tiendas Minoristas (POS/Terminales)**
   - Consultan stock antes de vender
   - Actualizan inventario (reposiciones)
   - Registran ventas (ajustes negativos)
   - Acceso limitado a su(s) tienda(s)

2. **Página Web Pública**
   - Consulta disponibilidad de productos
   - Muestra stock por tienda
   - Solo lectura, sin modificaciones

3. **Administradores**
   - Acceso completo a todas las tiendas
   - Gestión global de inventario
   - Monitoreo y configuración

---

## Pruebas con Postman

El proyecto incluye **colecciones completas de Postman** para facilitar las pruebas de la API. Todas las colecciones están preconfiguradas y listas para usar.

### 📂 Ubicación

```
docs/postman/
├── collections/
│   ├── Auth.postman_collection.json
│   ├── Inventory.postman_collection.json
│   └── Documentation.postman_collection.json
└── environments/
    └── dev.postman_environment.json
```

### 📦 Colecciones Disponibles

#### 1. **Auth Collection**
Pruebas de autenticación:
- ✅ Login exitoso (Admin)
- ✅ Login usuarios de tienda

#### 2. **Inventory Collection**
Operaciones de inventario:
- 📋 Consultar inventario por producto y tienda
- 📋 Listar inventario de un producto en todas las tiendas
- ✏️ Actualizar cantidad absoluta
- ➕➖ Ajustes incrementales (ventas/reposiciones)

--- 

## API Reference

Base URL: `http://localhost:8080`

### Autenticación

Todos los endpoints (excepto `/api/auth/login`) requieren autenticación mediante JWT token en el header:

```
Authorization: Bearer <token>
```

#### POST /api/auth/login

Autentica un usuario y devuelve un JWT token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "12345"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

**Errores Posibles:**
- `400 Bad Request`: Credenciales inválidas
- `AUTH-005`: Invalid username or password

**Ejemplo con curl:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"12345\"}"
```

---

### Endpoints de Inventario

#### GET /api/inventory/{productSku}/stores

Obtiene el inventario de un producto en todas las tiendas. **Solo ADMIN**.

**Path Parameters:**
- `productSku` (string): SKU del producto (ej: "REM-001-BL-M")

**Headers:**
```
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 1,
    "storeName": "Shopping Dinosaurio Mall",
    "availableQty": 50,
    "version": 0,
    "updatedAt": "2025-10-07T10:30:00"
  },
  {
    "id": 2,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 2,
    "storeName": "Centro Maipu 712",
    "availableQty": 30,
    "version": 0,
    "updatedAt": "2025-10-07T10:30:00"
  }
]
```

**Errores Posibles:**
- `401 Unauthorized`: Token inválido o expirado
- `403 Forbidden`: Usuario no es ADMIN
- `404 Not Found`: Producto no existe
- `INV-001`: Product not found
- `AUTH-009`: Administrator access required

**Ejemplo con curl:**
```bash
curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores" \
  -H "Authorization: Bearer <admin-token>"
```

---

#### GET /api/inventory/{productSku}/stores/{storeId}

Obtiene el inventario de un producto en una tienda específica.

**Path Parameters:**
- `productSku` (string): SKU del producto
- `storeId` (long): ID de la tienda

**Headers:**
```
Authorization: Bearer <token>
```

**Permisos:**
- ADMIN: Acceso a cualquier tienda
- STORE_USER: Solo tiendas asignadas

**Response (200 OK):**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 50,
  "version": 0,
  "updatedAt": "2025-10-07T10:30:00"
}
```

**Errores Posibles:**
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Sin permisos para esta tienda
- `404 Not Found`: Inventario no encontrado
- `INV-002`: Store not found
- `INV-003`: Inventory not found
- `AUTH-008`: Access denied to the specified store

**Ejemplo con curl:**
```bash
curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1" \
  -H "Authorization: Bearer <token>"
```

---

#### PUT /api/inventory/{productSku}/stores/{storeId}

Establece la cantidad absoluta de stock para un producto en una tienda.

**Path Parameters:**
- `productSku` (string): SKU del producto
- `storeId` (long): ID de la tienda

**Request Body:**
```json
{
  "availableQty": 100
}
```

**Validaciones:**
- `availableQty` debe ser >= 0
- Usuario debe tener permisos sobre la tienda

**Response (200 OK):**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 100,
  "version": 1,
  "updatedAt": "2025-10-07T11:00:00"
}
```

**Errores Posibles:**
- `400 Bad Request`: Cantidad negativa
- `403 Forbidden`: Sin permisos
- `404 Not Found`: Producto o tienda no existe
- `INV-005`: Negative quantity not allowed

**Ejemplo con curl:**
```bash
curl -X PUT "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"availableQty\":100}"
```

---

#### POST /api/inventory/{productSku}/stores/{storeId}/adjustments

Ajusta el inventario de forma incremental (positivo = entrada, negativo = venta).

**Path Parameters:**
- `productSku` (string): SKU del producto
- `storeId` (long): ID de la tienda

**Request Body:**
```json
{
  "adjustment": -5
}
```

**Validaciones:**
- `adjustment` no puede ser 0
- El resultado no puede ser negativo
- Control de concurrencia con optimistic locking

**Response (200 OK):**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 95,
  "version": 2,
  "updatedAt": "2025-10-07T11:15:00"
}
```

**Errores Posibles:**
- `400 Bad Request`: Ajuste = 0 o stock insuficiente
- `INV-004`: Insufficient stock available
- `INV-009`: Invalid inventory adjustment
- `INV-006`: Concurrent modification detected (con retry automático)

**Ejemplo con curl (Venta):**
```bash
curl -X POST "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1/adjustments" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"adjustment\":-5}"
```

**Ejemplo con curl (Entrada):**
```bash
curl -X POST "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1/adjustments" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"adjustment\":50}"
```

---

## Modelos de Datos

### InventoryResponse

Representa el inventario de un producto en una tienda.

```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 50,
  "version": 0,
  "updatedAt": "2025-10-07T10:30:00"
}
```

**Campos:**
- `id` (Long): ID único del registro de inventario
- `productSku` (String): Código SKU del producto
- `productName` (String): Nombre descriptivo del producto
- `storeId` (Long): ID de la tienda
- `storeName` (String): Nombre de la tienda
- `availableQty` (Integer): Cantidad disponible en stock
- `version` (Integer): Versión para control de concurrencia
- `updatedAt` (LocalDateTime): Última actualización

---

### AuthRequest

Credenciales para autenticación.

```json
{
  "username": "admin",
  "password": "12345"
}
```

**Campos:**
- `username` (String, requerido): Nombre de usuario
- `password` (String, requerido): Contraseña

---

### AuthResponse

Respuesta de autenticación exitosa.

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

**Campos:**
- `token` (String): JWT token para autenticación
- `type` (String): Tipo de token (siempre "Bearer")
- `username` (String): Nombre de usuario autenticado
- `role` (String): Rol del usuario (ADMIN o STORE_USER)

---

### InventoryUpdateRequest

Solicitud para establecer cantidad absoluta.

```json
{
  "availableQty": 100
}
```

**Campos:**
- `availableQty` (Integer, requerido): Nueva cantidad (>= 0)

---

### InventoryAdjustmentRequest

Solicitud para ajuste incremental.

```json
{
  "adjustment": -5
}
```

**Campos:**
- `adjustment` (Integer, requerido): Cantidad a ajustar (+ o -, != 0)

---

## Códigos de Error

El sistema utiliza códigos de error estructurados con el formato `CATEGORÍA-XXX`.

### Errores de Inventario (INV-XXX)

| Código | Mensaje | Descripción |
|--------|---------|-------------|
| INV-001 | Product not found | Producto no existe |
| INV-002 | Store not found | Tienda no existe |
| INV-003 | Inventory not found | Inventario no encontrado |
| INV-004 | Insufficient stock available | Stock insuficiente para la operación |
| INV-005 | Negative quantity not allowed | La cantidad no puede ser negativa |
| INV-006 | Concurrent modification detected | Conflicto de concurrencia (se reintenta) |
| INV-007 | Product with this SKU already exists | SKU duplicado |
| INV-008 | Store with this name already exists | Nombre de tienda duplicado |
| INV-009 | Invalid inventory adjustment | Ajuste inválido (ej: 0) |
| INV-010 | Invalid SKU format provided | Formato de SKU incorrecto |
| INV-011 | Inventory operation could not be completed | Operación falló |

### Errores de Autenticación (AUTH-XXX)

| Código | Mensaje | Descripción |
|--------|---------|-------------|
| AUTH-001 | Authentication failed | Fallo general de autenticación |
| AUTH-002 | Invalid or expired token | Token JWT inválido o expirado |
| AUTH-003 | Session has expired | Sesión expirada |
| AUTH-004 | Unauthorized access | Acceso no autorizado |
| AUTH-005 | Invalid username or password | Credenciales incorrectas |
| AUTH-006 | User not found | Usuario no existe |
| AUTH-007 | User not authenticated | Usuario no autenticado |
| AUTH-008 | Access denied to the specified store | Sin permisos para esta tienda |
| AUTH-009 | Administrator access required | Solo administradores |
| AUTH-010 | Insufficient permissions for store operations | Permisos insuficientes |
| AUTH-011 | Error processing authentication token | Error al procesar token |
| AUTH-012 | User role insufficient | Rol insuficiente |
| AUTH-013 | User account is inactive | Cuenta inactiva |
| AUTH-014 | Error processing password | Error en contraseña |
| AUTH-015 | Error checking user permissions | Error verificando permisos |

### Errores de Validación (VAL-XXX)

| Código | Mensaje | Descripción |
|--------|---------|-------------|
| VAL-001 | Validation error | Error de validación general |
| VAL-002 | Invalid request parameters | Parámetros inválidos |
| VAL-003 | Required parameter is missing | Parámetro requerido faltante |
| VAL-004 | Parameter format is invalid | Formato de parámetro incorrecto |
| VAL-005 | Request body is required | Body requerido |
| VAL-006 | Invalid JSON format in request | JSON mal formado |

### Errores de Sistema (SYS-XXX)

| Código | Mensaje | Descripción |
|--------|---------|-------------|
| SYS-001 | Internal server error | Error interno del servidor |
| SYS-002 | Database operation failed | Error en base de datos |
| SYS-003 | Service temporarily unavailable | Servicio no disponible |
| SYS-004 | Error accessing system resource | Error accediendo recurso |
| SYS-005 | System configuration error | Error de configuración |
| SYS-006 | Database connection failed | Conexión BD fallida |
| SYS-007 | Database transaction failed | Transacción fallida |
| SYS-008 | External service communication failed | Servicio externo falló |

### Formato de Respuesta de Error

```json
{
  "timestamp": "2025-10-07T11:30:00",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "INV-004",
  "message": "Insufficient stock available",
  "details": "Current: 5, Adjustment: -10, Result would be: -5",
  "path": "/api/inventory/REM-001-BL-M/stores/1/adjustments"
}
```

---

## Diagramas de Secuencia

### 1. Autenticación y Login

![Diagrama de Login](diagrams/diagrama%20login.png)

**Flujo:**
1. Cliente envía credenciales (username, password)
2. AuthController recibe la petición
3. AuthService valida las credenciales
4. UserDetailsService busca el usuario en BD
5. Se verifica la contraseña con BCrypt
6. JwtUtil genera el token JWT
7. Se devuelve el token al cliente

---

### 2. Ajuste Incremental de Stock

![Diagrama de Ajuste de Stock](diagrams/diagrama%20actualizar%20stock.png)

**Flujo:**
1. Cliente envía request con ajuste (ej: -5)
2. JwtAuthenticationFilter valida el token
3. StorePermissionAspect verifica permisos
4. InventoryService procesa el ajuste
5. Se valida que el resultado no sea negativo
6. Se guarda con control de versión
7. Se devuelve el inventario actualizado

---

### 3. Manejo de Concurrencia (Optimistic Locking)

![Diagrama de Concurrencia](diagrams/diagrama%20concurencia.png)

**Flujo:**
1. Usuario A lee inventario (version=1)
2. Usuario B lee inventario (version=1)
3. Usuario A actualiza primero → version=2 ✓
4. Usuario B intenta actualizar → OptimisticLockingFailureException
5. Sistema reintenta automáticamente (hasta 3 veces)
6. Usuario B relee inventario (version=2)
7. Usuario B actualiza exitosamente → version=3 ✓

**Configuración de Retry:**
- Máximo 3 reintentos
- Backoff exponencial: 100ms, 200ms, 400ms
- Solo para OptimisticLockingFailureException

---

### 4. Manejo de Errores y Excepciones

![Diagrama de Control de Errores](diagrams/diagrama%20control%20errores.png)

**Flujo:**
1. Se produce un error en cualquier capa
2. Se lanza BusinessException con ErrorCode
3. GlobalExceptionHandler intercepta la excepción
4. Se construye respuesta estructurada con:
   - Timestamp
   - Código de error
   - Mensaje
   - Detalles adicionales
5. Se registra en logs
6. Se devuelve al cliente

---

## Ejemplos de Uso

### Flujo Completo: Usuario de Tienda

#### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user_dinosaurio","password":"12345"}'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyX2Rpbm9zYXVyaW8iLCJyb2xlIjoiU1RPUkVfVVNFUiIsImlhdCI6MTY5Njc2ODgwMCwiZXhwIjoxNjk2ODU1MjAwfQ.xyz",
  "type": "Bearer",
  "username": "user_dinosaurio",
  "role": "STORE_USER"
}
```

#### 2. Consultar Stock de su Tienda
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta:**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 50,
  "version": 0,
  "updatedAt": "2025-10-07T10:00:00"
}
```

#### 3. Registrar una Venta (ajuste negativo)
```bash
curl -X POST "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1/adjustments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-3}'
```

**Respuesta:**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 47,
  "version": 1,
  "updatedAt": "2025-10-07T11:30:00"
}
```

#### 4. Registrar Reposición de Stock
```bash
curl -X POST "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1/adjustments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":50}'
```

**Respuesta:**
```json
{
  "id": 1,
  "productSku": "REM-001-BL-M",
  "productName": "Remera Básica Blanca M",
  "storeId": 1,
  "storeName": "Shopping Dinosaurio Mall",
  "availableQty": 97,
  "version": 2,
  "updatedAt": "2025-10-07T12:00:00"
}
```

#### 5. Intentar Acceder a Otra Tienda (ERROR)
```bash
curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores/2" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta (403 Forbidden):**
```json
{
  "timestamp": "2025-10-07T12:05:00",
  "status": 403,
  "error": "Forbidden",
  "errorCode": "AUTH-008",
  "message": "Access denied to the specified store",
  "details": "User user_dinosaurio does not have permission for store 2",
  "path": "/api/inventory/REM-001-BL-M/stores/2"
}
```

---

### Flujo Completo: Administrador

#### 1. Login como Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"12345"}'
```

#### 2. Consultar Stock en Todas las Tiendas
```bash
ADMIN_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 1,
    "storeName": "Shopping Dinosaurio Mall",
    "availableQty": 97,
    "version": 2,
    "updatedAt": "2025-10-07T12:00:00"
  },
  {
    "id": 2,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 2,
    "storeName": "Centro Maipu 712",
    "availableQty": 30,
    "version": 0,
    "updatedAt": "2025-10-07T10:00:00"
  },
  {
    "id": 3,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 3,
    "storeName": "Nuevo Centro Shopping",
    "availableQty": 45,
    "version": 0,
    "updatedAt": "2025-10-07T10:00:00"
  }
]
```

#### 3. Actualizar Stock en Cualquier Tienda
```bash
curl -X PUT "http://localhost:8080/api/inventory/REM-001-BL-M/stores/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"availableQty":100}'
```

---

### Flujo Completo: Usuario Web

#### 1. Login como Web User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"web","password":"12345"}'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3ZWIiLCJyb2xlIjoiV0VCX1VTRVIiLCJpYXQiOjE2OTY3Njg4MDAsImV4cCI6MTY5Njg1NTIwMH0.xyz",
  "type": "Bearer",
  "username": "web",
  "role": "WEB_USER"
}
```

#### 2. Consultar Stock en Todas las Tiendas (PERMITIDO)
```bash
WEB_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores" \
  -H "Authorization: Bearer $WEB_TOKEN"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 1,
    "storeName": "Shopping Dinosaurio Mall",
    "availableQty": 25,
    "version": 0,
    "updatedAt": "2025-10-08T10:00:00"
  },
  {
    "id": 2,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 2,
    "storeName": "Centro Maipu 712",
    "availableQty": 18,
    "version": 0,
    "updatedAt": "2025-10-08T10:00:00"
  },
  {
    "id": 3,
    "productSku": "REM-001-BL-M",
    "productName": "Remera Básica Blanca M",
    "storeId": 3,
    "storeName": "Nuevo Centro Shopping",
    "availableQty": 35,
    "version": 0,
    "updatedAt": "2025-10-08T10:00:00"
  }
]
```

#### 3. Intentar Consultar Tienda Específica (DENEGADO)
```bash
curl -X GET "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1" \
  -H "Authorization: Bearer $WEB_TOKEN"
```

**Respuesta (403 Forbidden):**
```json
{
  "timestamp": "2025-10-08T12:10:00",
  "status": 403,
  "error": "Forbidden",
  "errorCode": "INSUFFICIENT_PERMISSIONS",
  "message": "Web users can only access product inventory queries",
  "details": "User web attempted to access non-web endpoint",
  "path": "/api/inventory/REM-001-BL-M/stores/1"
}
```

#### 4. Intentar Actualizar Stock (DENEGADO)
```bash
curl -X PUT "http://localhost:8080/api/inventory/REM-001-BL-M/stores/1" \
  -H "Authorization: Bearer $WEB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"availableQty":100}'
```

**Respuesta (403 Forbidden):**
```json
{
  "timestamp": "2025-10-08T12:15:00",
  "status": 403,
  "error": "Forbidden",
  "errorCode": "INSUFFICIENT_PERMISSIONS",
  "message": "Web users can only access product inventory queries",
  "details": "User web attempted to access non-web endpoint",
  "path": "/api/inventory/REM-001-BL-M/stores/1"
}
```

**Resumen de Permisos WEB_USER:**
- ✅ **PERMITIDO**: `GET /api/inventory/{productSku}/stores` - Consultar inventario de producto en todas las tiendas
- ❌ **DENEGADO**: `GET /api/inventory/{productSku}/stores/{storeId}` - Consultar tienda específica
- ❌ **DENEGADO**: `PUT /api/inventory/{productSku}/stores/{storeId}` - Actualizar stock
- ❌ **DENEGADO**: `POST /api/inventory/{productSku}/stores/{storeId}/adjustments` - Ajustes de inventario

---

## Base de Datos

### Esquema

#### Tabla: products
```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabla: stores
```sql
CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabla: users
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabla: inventory
```sql
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    available_qty INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (store_id) REFERENCES stores(id),
    UNIQUE(product_id, store_id)
);
```

#### Tabla: user_store_permissions
```sql
CREATE TABLE user_store_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (store_id) REFERENCES stores(id),
    UNIQUE(user_id, store_id)
);
```

### Índices

- `idx_inventory_product_id` en inventory(product_id)
- `idx_inventory_store_id` en inventory(store_id)
- `idx_user_store_permissions_user_id` en user_store_permissions(user_id)
- `idx_user_store_permissions_store_id` en user_store_permissions(store_id)
- `idx_products_sku` en products(sku)
- `idx_users_username` en users(username)

### Datos de Prueba

#### Tiendas
| ID | Nombre |
|----|--------|
| 1 | Shopping Dinosaurio Mall |
| 2 | Centro Maipu 712 |
| 3 | Nuevo Centro Shopping |

#### Usuarios
| Username | Password | Role | Tiendas |
|----------|----------|------|---------|
| admin | 12345 | ADMIN | Todas |
| user_dinosaurio | 12345 | STORE_USER | 1 |
| user_maipu | 12345 | STORE_USER | 2 |
| user_nuevo_centro | 12345 | STORE_USER | 3 |
| web | 12345 | WEB_USER | Solo consulta de inventario por producto |

#### Productos (Ejemplos)
- REM-001-BL-M: Remera Básica Blanca M
- REM-002-NG-L: Remera Básica Negra L
- BUZ-001-GR-M: Buzo Hoodie Gris M
- PAN-001-NG-32: Pantalón Chino Negro 32
- CAM-001-BL-M: Camisa Blanca Oxford M
- *(Ver data.sql para lista completa)*

---

## Configuración

### application.yml

**Puerto del servidor:**
```yaml
server:
  port: 8080
```

**Base de datos:**
```yaml
spring:
  r2dbc:
    url: r2dbc:h2:file:///./inventory-db
    username: sa
    password:
```

**JWT:**
```yaml
jwt:
  secret: mySecretKeyForInventoryManagementSystem...
  expiration: 86400000  # 24 horas
```

**Logging:**
```yaml
logging:
  level:
    com.meli.inventorymanagement: DEBUG
  file:
    name: logs/inventory-management.log
```

---

## Swagger / OpenAPI

### Acceso

**URL:** http://localhost:8080/swagger-ui.html

### Características

- Documentación interactiva de todos los endpoints
- Pruebas en vivo desde el navegador
- Autenticación JWT integrada
- Modelos de datos completos
- Ejemplos de request/response

### Uso

1. Abre Swagger UI en tu navegador
2. Encuentra el endpoint `/api/auth/login`
3. Haz clic en "Try it out"
4. Ingresa credenciales (ej: admin/12345)
5. Ejecuta y copia el token
6. Haz clic en el botón "Authorize" (candado)
7. Ingresa: `Bearer <tu-token>`
8. Ahora puedes probar todos los endpoints

---

## Health Checks

### Endpoint de Salud

**URL:** http://localhost:8080/actuator/health

**Respuesta:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## Logging

### Niveles de Log

- **ERROR**: Errores críticos
- **WARN**: Advertencias (autenticación fallida, reintentos)
- **INFO**: Información general de operaciones
- **DEBUG**: Detalles de ejecución (queries, validaciones)

### Archivo de Logs

**Ubicación:** `logs/inventory-management.log`

**Rotación:**
- Máximo 50MB por archivo
- 30 días de historial
- Compresión automática

### Ejemplo de Log

```
2025-10-07 11:30:15 [reactor-http-nio-2] INFO  InventoryController - POST /api/inventory/REM-001-BL-M/stores/1/adjustments - User: user_dinosaurio - IP: 127.0.0.1 - Adjusting by: -5
2025-10-07 11:30:15 [reactor-http-nio-2] DEBUG InventoryService - Adjusting inventory for product SKU: REM-001-BL-M in store: 1 by: -5
2025-10-07 11:30:15 [reactor-http-nio-2] INFO  InventoryService - Inventory adjusted successfully. New quantity: 45, Version: 1
2025-10-07 11:30:15 [reactor-http-nio-2] INFO  InventoryController - Successfully adjusted inventory for product REM-001-BL-M in store 1 by -5 - Final quantity: 45 - User: user_dinosaurio - IP: 127.0.0.1
```

---

## Troubleshooting

### Error: Token Expirado

**Síntoma:**
```json
{
  "errorCode": "AUTH-002",
  "message": "Invalid or expired token"
}
```

**Solución:** Generar nuevo token con `/api/auth/login`

---

### Error: Stock Insuficiente

**Síntoma:**
```json
{
  "errorCode": "INV-004",
  "message": "Insufficient stock available",
  "details": "Current: 5, Adjustment: -10, Result would be: -5"
}
```

**Solución:** Verificar stock actual antes de realizar la operación

---

### Error: Sin Permisos para Tienda

**Síntoma:**
```json
{
  "errorCode": "AUTH-008",
  "message": "Access denied to the specified store"
}
```

**Solución:** 
- Verificar que el usuario tenga permisos asignados
- Usar un token de ADMIN para acceso total

---

## Mas Información

Para más información, consulta:
- [README.md](../README.md) - Visión general del proyecto
- [RUN.md](../RUN.md) - Guía de ejecución

---

