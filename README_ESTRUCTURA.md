# 📦 Inventory Management - Estructura del Proyecto

## 🏗️ Arquitectura Hexagonal (Ports & Adapters)

Este proyecto implementa una arquitectura hexagonal simple y práctica para gestionar inventario de productos en múltiples tiendas.

---

## 📂 Estructura Completa del Proyecto

```
inventory-management/
│
├── src/main/java/com/meli/inventorymanagement/
│   │
│   ├── 🔷 domain/                                    # CAPA DE DOMINIO (Núcleo del negocio)
│   │   │
│   │   ├── model/                                    # Modelos de dominio (POJOs puros)
│   │   │   ├── Inventory.java                       # Modelo de inventario (sin anotaciones de BD)
│   │   │   ├── Product.java                         # Modelo de producto
│   │   │   ├── Store.java                           # Modelo de tienda
│   │   │   ├── User.java                            # Modelo de usuario
│   │   │   └── UserStorePermission.java             # Modelo de permisos usuario-tienda
│   │   │
│   │   ├── port/                                     # Puertos (interfaces/contratos)
│   │   │   ├── InventoryPort.java                   # Contrato para operaciones de inventario
│   │   │   ├── ProductPort.java                     # Contrato para operaciones de productos
│   │   │   ├── StorePort.java                       # Contrato para operaciones de tiendas
│   │   │   ├── UserPort.java                        # Contrato para operaciones de usuarios
│   │   │   ├── AuthenticationPort.java              # Contrato para autenticación de usuarios
│   │   │   └── TokenGeneratorPort.java              # Contrato para generación de tokens JWT
│   │   │
│   │   └── exception/                                # Excepciones de dominio
│   │       └── BusinessException.java               # Excepción de negocio del dominio
│   │
│   ├── 🔶 application/                               # CAPA DE APLICACIÓN (Casos de uso)
│   │   │
│   │   ├── service/                                  # Servicios de aplicación (lógica de negocio)
│   │   │   ├── InventoryService.java                # Casos de uso de inventario (consultar, actualizar, ajustar)
│   │   │   └── AuthService.java                     # Casos de uso de autenticación
│   │   │
│   │   ├── dto/                                      # Data Transfer Objects
│   │   │   ├── InventoryResponse.java               # DTO de respuesta de inventario
│   │   │   ├── InventoryUpdateRequest.java          # DTO para actualizar inventario (cantidad absoluta)
│   │   │   ├── InventoryAdjustmentRequest.java      # DTO para ajustar inventario (incremento/decremento)
│   │   │   ├── AuthRequest.java                     # DTO de solicitud de autenticación
│   │   │   └── AuthResponse.java                    # DTO de respuesta con token JWT
│   │   │
│   │   └── mapper/                                   # Mapeadores (Dominio ↔ DTO)
│   │       └── InventoryMapper.java                 # Convierte entre Inventory (dominio) y DTOs
│   │
│   ├── 🔸 infrastructure/                            # CAPA DE INFRAESTRUCTURA (Detalles técnicos)
│   │   │
│   │   ├── adapter/                                  # Adaptadores
│   │   │   │
│   │   │   ├── input/                               # Adaptadores de ENTRADA
│   │   │   │   └── rest/                            # API REST (controllers)
│   │   │   │       ├── InventoryController.java     # Endpoints de inventario (GET, PUT, PATCH)
│   │   │   │       └── AuthController.java          # Endpoint de login
│   │   │   │
│   │   │   └── output/                              # Adaptadores de SALIDA
│   │   │       │
│   │   │       ├── persistence/                     # Persistencia de datos
│   │   │       │   │
│   │   │       │   ├── entity/                      # Entidades de BD (con anotaciones R2DBC)
│   │   │       │   │   ├── InventoryEntity.java     # Entidad de tabla 'inventory'
│   │   │       │   │   ├── ProductEntity.java       # Entidad de tabla 'products'
│   │   │       │   │   ├── StoreEntity.java         # Entidad de tabla 'stores'
│   │   │       │   │   ├── UserEntity.java          # Entidad de tabla 'users'
│   │   │       │   │   └── UserStorePermissionEntity.java  # Entidad de tabla 'user_store_permissions'
│   │   │       │   │
│   │   │       │   ├── InventoryRepository.java     # Repositorio Spring Data R2DBC para inventario
│   │   │       │   ├── InventoryPersistenceAdapter.java  # Implementa InventoryPort, convierte Entity ↔ Domain
│   │   │       │   │
│   │   │       │   ├── ProductRepository.java       # Repositorio para productos
│   │   │       │   ├── ProductPersistenceAdapter.java    # Implementa ProductPort
│   │   │       │   │
│   │   │       │   ├── StoreRepository.java         # Repositorio para tiendas
│   │   │       │   ├── StorePersistenceAdapter.java      # Implementa StorePort
│   │   │       │   │
│   │   │       │   ├── UserRepository.java          # Repositorio para usuarios
│   │   │       │   ├── UserPersistenceAdapter.java       # Implementa UserPort
│   │   │       │   │
│   │   │       │   └── AuthenticationAdapter.java   # Implementa AuthenticationPort
│   │   │       │
│   │   │       └── security/                        # Adaptadores de seguridad
│   │   │           └── JwtTokenAdapter.java         # Implementa TokenGeneratorPort para JWT
│   │   │
│   │   ├── config/                                   # Configuraciones de Spring
│   │   │   ├── AppConfig.java                       # Configuración general (PasswordEncoder, etc.)
│   │   │   ├── R2dbcConfig.java                     # Configuración de R2DBC (BD reactiva)
│   │   │   ├── SecurityConfig.java                  # Configuración de seguridad JWT
│   │   │   ├── WebFluxConfig.java                   # Configuración de WebFlux (CORS, etc.)
│   │   │   └── OpenApiConfig.java                   # Configuración de Swagger/OpenAPI
│   │   │
│   │   ├── exception/                                # Manejo de excepciones de infraestructura
│   │   │   ├── ErrorResponse.java                   # DTO de respuesta de error
│   │   │   └── GlobalExceptionHandler.java          # Manejador global de excepciones
│   │   │
│   │   └── security/                                 # Seguridad y autenticación
│   │       ├── JwtUtil.java                         # Utilidad para generar/validar tokens JWT
│   │       ├── JwtAuthenticationFilter.java         # Filtro para validar JWT en requests
│   │       ├── UserDetailsService.java              # Servicio para cargar usuarios (autenticación)
│   │       ├── StorePermissionAspect.java           # AOP para validar permisos de tienda
│   │       └── RequireStorePermission.java          # Anotación para requerir permiso de tienda
│   │
│   ├── common/                                       # Código común
│   │   └── constant/
│   │       └── ErrorCode.java                       # Enumeración de códigos de error
│   │
│   └── InventoryManagementApplication.java          # Clase principal de Spring Boot
│
├── src/main/resources/
│   ├── application.yml                               # Configuración de la aplicación
│   ├── schema.sql                                    # Script SQL para crear tablas
│   └── data.sql                                      # Script SQL para datos iniciales
│
├── src/test/java/com/meli/inventorymanagement/
│   ├── controller/
│   │   ├── InventoryControllerTest.java             # Tests de integración del controller
│   │   └── TestSecurityConfig.java                  # Configuración de seguridad para tests
│   │
│   └── service/
│       └── InventoryServiceTest.java                # Tests unitarios del servicio
│
├── src/test/resources/
│   └── application-test.yml                          # Configuración para tests
│
├── pom.xml                                           # Dependencias Maven
├── README.md                                         # Documentación principal
└── README_ESTRUCTURA.md                             # Este archivo - Documentación de arquitectura
```

---

## 📋 Descripción Detallada por Capa

### 🔷 **DOMAIN** (Capa de Dominio - El Corazón del Sistema)

Esta es la capa **más importante** y **pura**. No depende de nada externo (sin Spring, sin BD, sin frameworks).

#### **domain/model/** - Modelos de Dominio
Son las entidades de negocio **sin** anotaciones de frameworks (solo Lombok).

| Archivo | Función |
|---------|---------|
| `Inventory.java` | Representa el stock de un producto en una tienda específica. Contiene: id, productId, storeId, availableQty, version (control de concurrencia optimista) |
| `Product.java` | Representa un producto del catálogo. Contiene: id, sku (código único), name, description |
| `Store.java` | Representa una tienda física. Contiene: id, name, location, isActive |
| `User.java` | Representa un usuario del sistema. Contiene: id, username, password, role (ADMIN/STORE_MANAGER) |
| `UserStorePermission.java` | Representa los permisos de un usuario sobre una tienda específica. Permite control granular de acceso por tienda |

**Ejemplo de modelo puro:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    private Long id;
    private Long productId;
    private Long storeId;
    private Integer availableQty;
    private Long version;  // Para optimistic locking
    // SIN @Table, @Column, etc.
}
```

#### **domain/port/** - Puertos (Interfaces)
Son **contratos** que define el dominio. La infraestructura los implementa.

| Archivo | Función |
|---------|---------|
| `InventoryPort.java` | Define operaciones para gestionar inventario:<br>• `findByProductSkuAndStoreId()` - Buscar inventario específico<br>• `findAllByProductSku()` - Buscar inventario de un producto en todas las tiendas<br>• `save()` - Guardar/actualizar inventario<br>• `existsByProductIdAndStoreId()` - Verificar existencia |
| `ProductPort.java` | Define operaciones para productos:<br>• `findBySku()` - Buscar producto por SKU<br>• `findById()` - Buscar producto por ID<br>• `existsBySku()` - Verificar si existe un SKU |
| `StorePort.java` | Define operaciones para tiendas:<br>• `findById()` - Buscar tienda por ID<br>• `findAll()` - Listar todas las tiendas activas<br>• `existsById()` - Verificar existencia de tienda |
| `UserPort.java` | Define operaciones para usuarios:<br>• `findByUsername()` - Buscar usuario por nombre<br>• `hasStorePermission()` - Verificar si un usuario tiene permiso sobre una tienda |
| `AuthenticationPort.java` | Define el contrato para autenticación:<br>• `authenticate()` - Valida credenciales de usuario<br>• Retorna un `Mono<User>` con el usuario autenticado |
| `TokenGeneratorPort.java` | Define el contrato para generación de tokens:<br>• `generateToken()` - Genera un token JWT para un usuario autenticado<br>• Abstrae la implementación específica de JWT |

**Beneficio:** Los servicios dependen de estas **interfaces**, no de implementaciones concretas (Inversión de Dependencias).

#### **domain/exception/** - Excepciones de Dominio

| Archivo | Función |
|---------|---------|
| `BusinessException.java` | Excepción personalizada para errores de negocio del dominio. Contiene código de error y mensaje. Se lanza cuando se violan reglas de negocio (ej: stock negativo, producto no encontrado) |

---

### 🔶 **APPLICATION** (Capa de Aplicación - Casos de Uso)

Contiene la **lógica de negocio** y orquesta el flujo de datos.

#### **application/service/** - Servicios de Aplicación

| Archivo | Función |
|---------|---------|
| `InventoryService.java` | **Casos de uso de inventario:**<br>• `getInventoryByProductSku()` - Consultar inventario de un producto en todas las tiendas<br>• `getInventoryByProductSkuAndStore()` - Consultar inventario específico de una tienda<br>• `updateInventory()` - Actualizar cantidad absoluta de stock<br>• `adjustInventory()` - Ajustar stock con incremento/decremento relativo (+5, -10)<br>• **Validaciones:** Stock no negativo, producto/tienda existen, control de concurrencia<br>• **Enriquecimiento:** Combina datos de Inventory + Product + Store para respuestas completas |
| `AuthService.java` | **Casos de uso de autenticación:**<br>• `login()` - Valida credenciales (username/password)<br>• Genera token JWT usando `TokenGeneratorPort`<br>• Usa `AuthenticationPort` para autenticar al usuario<br>• Retorna `AuthResponse` con el token |

**Importante:** Los servicios **NO** usan repositorios directamente, usan los **Puertos** (interfaces). Esto permite testear fácilmente con mocks.

#### **application/dto/** - Data Transfer Objects

| Archivo | Función |
|---------|---------|
| `InventoryResponse.java` | DTO para devolver datos de inventario al cliente. Incluye:<br>• Datos del producto (sku, name, description)<br>• Datos de la tienda (name, location)<br>• Cantidad disponible |
| `InventoryUpdateRequest.java` | DTO para recibir actualización de inventario (cantidad absoluta):<br>• `availableQty` - Nueva cantidad total<br>• `version` - Para control de concurrencia optimista |
| `InventoryAdjustmentRequest.java` | DTO para recibir ajuste de inventario (cantidad relativa):<br>• `adjustment` - Incremento (+5) o decremento (-10)<br>• `version` - Para control de concurrencia |
| `AuthRequest.java` | DTO para recibir credenciales de login:<br>• `username` - Nombre de usuario<br>• `password` - Contraseña en texto plano (se encripta en backend) |
| `AuthResponse.java` | DTO para devolver respuesta de autenticación:<br>• `token` - Token JWT generado<br>• `username` - Usuario autenticado<br>• `role` - Rol del usuario (ADMIN/STORE_MANAGER) |

#### **application/mapper/** - Mapeadores

| Archivo | Función |
|---------|---------|
| `InventoryMapper.java` | **Componente de mapeo** que convierte entre:<br>• `Inventory` (modelo de dominio) → `InventoryResponse` (DTO)<br>• Combina información de Product y Store para crear respuestas enriquecidas<br>• Usa `@Component` para ser inyectado en servicios |

---

### 🔸 **INFRASTRUCTURE** (Capa de Infraestructura - Detalles Técnicos)

Contiene los **adaptadores** que conectan el dominio con el mundo exterior (BD, REST, seguridad).

#### **infrastructure/adapter/input/rest/** - Adaptadores de Entrada (API REST)

| Archivo | Función |
|---------|---------|
| `InventoryController.java` | **Endpoints REST de inventario:**<br>• `GET /api/inventory/{sku}/stores` - Listar inventario de un producto en todas las tiendas<br>• `GET /api/inventory/{sku}/stores/{storeId}` - Ver inventario específico de una tienda<br>• `PUT /api/inventory/{sku}/stores/{storeId}` - Actualizar cantidad absoluta<br>• `PATCH /api/inventory/{sku}/stores/{storeId}/adjust` - Ajustar cantidad con incremento/decremento<br>• **Seguridad:** Requiere autenticación JWT y permisos de tienda (vía `@RequireStorePermission`) |
| `AuthController.java` | **Endpoint de autenticación:**<br>• `POST /api/auth/login` - Login con username/password<br>• Retorna token JWT en `AuthResponse`<br>• **Público:** No requiere autenticación previa |

#### **infrastructure/adapter/output/persistence/** - Adaptadores de Salida (Persistencia)

##### **entity/** - Entidades de Base de Datos
Son clases **con anotaciones** de Spring Data R2DBC que se mapean a tablas de la BD.

| Archivo | Función |
|---------|---------|
| `InventoryEntity.java` | Entidad de tabla `inventory`:<br>• **Anotaciones:** `@Table`, `@Id`, `@Column`, `@Version`<br>• **Campos:** id, productId, storeId, availableQty, version<br>• **Optimistic Locking:** Usa `@Version` para evitar conflictos de concurrencia |
| `ProductEntity.java` | Entidad de tabla `products`:<br>• **Campos:** id, sku (único), name, description<br>• **Índice único:** En columna `sku` |
| `StoreEntity.java` | Entidad de tabla `stores`:<br>• **Campos:** id, name, location, isActive<br>• Permite activar/desactivar tiendas sin eliminarlas |
| `UserEntity.java` | Entidad de tabla `users`:<br>• **Campos:** id, username (único), password (hasheado con BCrypt), role<br>• **Seguridad:** Password nunca se almacena en texto plano |
| `UserStorePermissionEntity.java` | Entidad de tabla `user_store_permissions`:<br>• **Campos:** id, userId, storeId<br>• **Clave única compuesta:** (userId, storeId)<br>• Define qué usuarios pueden acceder a qué tiendas |

##### **Repositorios**
Spring Data R2DBC que hacen las consultas a la BD (reactivo con Reactor - Mono/Flux).

| Archivo | Función |
|---------|---------|
| `InventoryRepository.java` | **Interface que extiende `ReactiveCrudRepository<InventoryEntity, Long>`**<br>• `findByProductSku()` - Buscar inventario por SKU (join con products)<br>• `findByProductSkuAndStoreId()` - Buscar inventario específico<br>• `existsByProductIdAndStoreId()` - Verificar existencia<br>• **Queries personalizadas:** Usa `@Query` con SQL nativo para joins |
| `ProductRepository.java` | **Interface reactiva para productos:**<br>• `findBySku()` - Buscar por código SKU<br>• `existsBySku()` - Verificar si existe un SKU<br>• **Métodos derivados:** Spring Data genera automáticamente las queries |
| `StoreRepository.java` | **Interface reactiva para tiendas:**<br>• `findByIsActiveTrue()` - Listar tiendas activas<br>• Métodos estándar de CRUD reactivo |
| `UserRepository.java` | **Interface reactiva para usuarios:**<br>• `findByUsername()` - Buscar usuario por nombre (para login)<br>• `hasStorePermission()` - Query personalizada para verificar permisos<br>• **Join complejo:** Con tabla de permisos |

##### **Adaptadores de Persistencia**
**Implementan los Puertos** del dominio y convierten entre `Entity` (BD) ↔ `Model` (Dominio).

| Archivo | Función |
|---------|---------|
| `InventoryPersistenceAdapter.java` | **Implementa `InventoryPort`**<br>• Usa `InventoryRepository` internamente<br>• **Conversión:** `InventoryEntity` ↔ `Inventory` (modelo de dominio)<br>• **Anotado:** `@Component` para inyección de dependencias |
| `ProductPersistenceAdapter.java` | **Implementa `ProductPort`**<br>• Convierte `ProductEntity` ↔ `Product`<br>• Abstrae los detalles de R2DBC del dominio |
| `StorePersistenceAdapter.java` | **Implementa `StorePort`**<br>• Convierte `StoreEntity` ↔ `Store`<br>• Filtra tiendas inactivas cuando corresponde |
| `UserPersistenceAdapter.java` | **Implementa `UserPort`**<br>• Convierte `UserEntity` ↔ `User`<br>• **Seguridad:** Gestiona verificación de permisos de tienda |
| `AuthenticationAdapter.java` | **Implementa `AuthenticationPort`**<br>• Valida credenciales usando `UserPort` y `PasswordEncoder`<br>• Retorna `Mono<User>` con el usuario autenticado<br>• **Seguridad:** Compara password hasheado con BCrypt |

**Flujo de datos:**
```
Service → Port (interface) → PersistenceAdapter → Repository → BD
   ↓                                ↓
Domain Model (puro)         Entity (con anotaciones R2DBC)
```

##### **infrastructure/adapter/output/security/** - Adaptadores de Seguridad

| Archivo | Función |
|---------|---------|
| `JwtTokenAdapter.java` | **Implementa `TokenGeneratorPort`**<br>• Genera tokens JWT usando `JwtUtil`<br>• Abstrae la implementación de JWT del dominio<br>• Permite cambiar fácilmente el mecanismo de tokens |

#### **infrastructure/config/** - Configuraciones

| Archivo | Función |
|---------|---------|
| `AppConfig.java` | **Configuración general de la aplicación:**<br>• `@Bean PasswordEncoder` - BCrypt para hashear passwords<br>• Otros beans compartidos de la aplicación |
| `R2dbcConfig.java` | **Configuración de R2DBC (acceso a BD reactivo):**<br>• Conexión a H2 Database en modo archivo<br>• Pool de conexiones reactivas<br>• Inicialización de esquema (schema.sql, data.sql) |
| `SecurityConfig.java` | **Configuración de Spring Security:**<br>• **Rutas públicas:** `/api/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`<br>• **Rutas protegidas:** `/api/inventory/**` requiere JWT<br>• Registra `JwtAuthenticationFilter`<br>• Deshabilita CSRF (API REST stateless)<br>• CORS configurado |
| `WebFluxConfig.java` | **Configuración de Spring WebFlux:**<br>• CORS global (permite localhost:3000 para frontend)<br>• Validadores personalizados<br>• Message converters |
| `OpenApiConfig.java` | **Configuración de Swagger/OpenAPI:**<br>• Documentación automática de API<br>• Configuración de seguridad JWT en Swagger UI<br>• Metadatos: título, versión, descripción<br>• **Acceso:** http://localhost:8080/swagger-ui.html |

#### **infrastructure/exception/** - Manejo de Errores

| Archivo | Función |
|---------|---------|
| `ErrorResponse.java` | **DTO estándar para respuestas de error:**<br>• `timestamp` - Momento del error<br>• `status` - Código HTTP (400, 404, 500, etc.)<br>• `error` - Tipo de error<br>• `message` - Mensaje descriptivo<br>• `path` - Ruta del endpoint que falló |
| `GlobalExceptionHandler.java` | **Manejador global de excepciones:**<br>• Usa `@RestControllerAdvice` para capturar todas las excepciones<br>• **Maneja:**<br>&nbsp;&nbsp;• `BusinessException` → 400 Bad Request<br>&nbsp;&nbsp;• `EntityNotFoundException` → 404 Not Found<br>&nbsp;&nbsp;• `OptimisticLockingFailureException` → 409 Conflict<br>&nbsp;&nbsp;• Excepciones genéricas → 500 Internal Server Error<br>• Retorna `ErrorResponse` consistente |

#### **infrastructure/security/** - Seguridad y Autenticación

| Archivo | Función |
|---------|---------|
| `JwtUtil.java` | **Utilidad para manejar tokens JWT:**<br>• `generateToken()` - Crea JWT con claims (username, role)<br>• `extractUsername()` - Extrae username del token<br>• `validateToken()` - Valida firma y expiración<br>• **Configuración:** Clave secreta (application.yml), tiempo de expiración<br>• Usa biblioteca `io.jsonwebtoken` (jjwt) |
| `JwtAuthenticationFilter.java` | **Filtro que intercepta cada request:**<br>• Extrae token JWT del header `Authorization: Bearer <token>`<br>• Valida el token usando `JwtUtil`<br>• Carga el usuario usando `UserDetailsService`<br>• Establece autenticación en el contexto de seguridad<br>• **Se ejecuta antes de cada endpoint protegido** |
| `UserDetailsService.java` | **Servicio de Spring Security para cargar usuarios:**<br>• Implementa `ReactiveUserDetailsService`<br>• Usa `UserPort` para buscar usuarios por username<br>• Convierte `User` (dominio) → `UserDetails` (Spring Security)<br>• **Usado por:** `JwtAuthenticationFilter` para validar credenciales |
| `StorePermissionAspect.java` | **Aspecto AOP para validar permisos de tienda:**<br>• Intercepta métodos anotados con `@RequireStorePermission`<br>• Extrae `storeId` del parámetro del método<br>• Valida que el usuario actual tenga permiso sobre esa tienda<br>• **Lanza excepción** si no tiene permiso<br>• **Usa:** `@Around` advice de Spring AOP |
| `RequireStorePermission.java` | **Anotación personalizada para endpoints:**<br>• Se coloca en métodos del controller que requieren permiso de tienda<br>• **Ejemplo:** `@RequireStorePermission` en `updateInventory()`<br>• El aspecto `StorePermissionAspect` la procesa<br>• **Nivel de retención:** RUNTIME |

#### **common/constant/** - Constantes Comunes

| Archivo | Función |
|---------|---------|
| `ErrorCode.java` | **Enumeración de códigos de error del sistema:**<br>• `PRODUCT_NOT_FOUND` - Producto no existe<br>• `STORE_NOT_FOUND` - Tienda no existe<br>• `INVENTORY_NOT_FOUND` - Inventario no encontrado<br>• `INVALID_QUANTITY` - Cantidad inválida (negativa)<br>• `INSUFFICIENT_STOCK` - Stock insuficiente<br>• `CONCURRENT_UPDATE` - Error de concurrencia<br>• `UNAUTHORIZED` - Sin permisos<br>• **Beneficio:** Códigos consistentes en toda la aplicación |

---

### 🧪 **TESTS**

#### **controller/** - Tests de Controladores

| Archivo | Función |
|---------|---------|
| `InventoryControllerTest.java` | **Tests de integración del API REST:**<br>• Usa `@WebFluxTest` para testing de controllers<br>• Usa `WebTestClient` para hacer requests HTTP simulados<br>• **Mockea:** Los servicios de aplicación<br>• **Valida:** Respuestas HTTP, códigos de estado, estructura JSON<br>• **Casos de prueba:**<br>&nbsp;&nbsp;• GET inventario exitoso<br>&nbsp;&nbsp;• GET con producto no encontrado (404)<br>&nbsp;&nbsp;• PUT actualización exitosa<br>&nbsp;&nbsp;• PATCH ajuste de inventario<br>&nbsp;&nbsp;• Validaciones de entrada |
| `TestSecurityConfig.java` | **Configuración de seguridad para tests:**<br>• Deshabilita JWT en tests (para simplificar)<br>• Permite acceso sin autenticación a todos los endpoints<br>• **Solo se usa en contexto de testing** |

#### **service/** - Tests de Servicios

| Archivo | Función |
|---------|---------|
| `InventoryServiceTest.java` | **Tests unitarios del servicio de inventario:**<br>• Usa `@ExtendWith(MockitoExtension.class)`<br>• **Mockea:** Todos los puertos (InventoryPort, ProductPort, StorePort)<br>• **Valida:** Lógica de negocio, validaciones, mapeos<br>• **Casos de prueba:**<br>&nbsp;&nbsp;• Consulta de inventario exitosa<br>&nbsp;&nbsp;• Actualización con validación de stock negativo<br>&nbsp;&nbsp;• Ajuste de inventario con incremento/decremento<br>&nbsp;&nbsp;• Manejo de productos/tiendas no encontrados<br>&nbsp;&nbsp;• Control de concurrencia optimista<br>• **No toca BD:** Tests puros y rápidos |

---

## 🔄 Flujo de una Request Típica

### Ejemplo: `GET /api/inventory/REM-001-BL-M/stores/1`

```
1. 🌐 Cliente hace request HTTP
   GET /api/inventory/REM-001-BL-M/stores/1
   Header: Authorization: Bearer <JWT_TOKEN>
        ↓
2. 🔒 JwtAuthenticationFilter (infrastructure/security)
   • Extrae token del header
   • Valida firma y expiración con JwtUtil
   • Carga usuario con UserDetailsService
   • Establece contexto de seguridad
        ↓
3. 🛡️ StorePermissionAspect (infrastructure/security)
   • Intercepta por anotación @RequireStorePermission
   • Valida que el usuario tenga permiso sobre storeId=1
   • Si no tiene permiso → lanza excepción 403
        ↓
4. 🎮 InventoryController (infrastructure/adapter/input/rest)
   • Método: getInventoryByProductAndStore(sku, storeId)
   • Valida parámetros de entrada
   • Llama al servicio
        ↓
5. 💼 InventoryService (application/service)
   • Método: getInventoryByProductSkuAndStore(sku, storeId)
   • Orquesta la lógica de negocio
   • Llama a múltiples puertos
        ↓
6. 🔌 InventoryPort (domain/port - INTERFACE)
   • Método: findByProductSkuAndStoreId(sku, storeId)
   • Contrato que el servicio invoca
        ↓
7. ⚙️ InventoryPersistenceAdapter (infrastructure/adapter/output/persistence)
   • Implementa InventoryPort
   • Llama al repositorio
        ↓
8. 📚 InventoryRepository (infrastructure/adapter/output/persistence)
   • Ejecuta query R2DBC con join a products y stores
   • Retorna Mono<InventoryEntity>
        ↓
9. 💾 Base de Datos H2
   • Ejecuta SQL: SELECT * FROM inventory i JOIN products p ...
   • Retorna fila con datos
        ↓
10. 📦 InventoryEntity (infrastructure/adapter/output/persistence/entity)
    • Objeto con anotaciones @Table, @Column
        ↓
11. 🔄 PersistenceAdapter convierte Entity → Model
    • InventoryEntity → Inventory (domain/model)
    • Sin anotaciones, modelo puro
        ↓
12. 🔍 Service enriquece con datos adicionales
    • Usa ProductPort.findById() → Product
    • Usa StorePort.findById() → Store
    • Combina: Inventory + Product + Store
        ↓
13. 🗺️ InventoryMapper (application/mapper)
    • Convierte Inventory → InventoryResponse (DTO)
    • Estructura lista para JSON
        ↓
14. 📤 Controller retorna ResponseEntity
    • Status: 200 OK
    • Body: InventoryResponse en JSON
        ↓
15. 🌐 Cliente recibe JSON
    {
      "productSku": "REM-001-BL-M",
      "productName": "Remera Básica",
      "storeName": "Tienda Centro",
      "storeLocation": "Av. Principal 123",
      "availableQty": 150
    }
```

---

## 🎯 Principios Aplicados

### ✅ Arquitectura Hexagonal (Ports & Adapters)
- **Dominio independiente**: No conoce Spring, R2DBC, frameworks
- **Puertos (interfaces)**: Definen contratos entre capas
- **Adaptadores**: Implementan puertos y conectan con tecnologías externas
- **Inversión de dependencias**: Todo apunta hacia el dominio

### ✅ SOLID Principles
- **Single Responsibility**: Cada clase tiene una única responsabilidad
- **Open/Closed**: Abierto a extensión (nuevos adaptadores), cerrado a modificación (dominio)
- **Liskov Substitution**: Los adaptadores son intercambiables via interfaces
- **Interface Segregation**: Puertos específicos (no interfaces gordas)
- **Dependency Inversion**: Dependemos de abstracciones (puertos), no de implementaciones

### ✅ Clean Code
- Separación de responsabilidades clara por capas
- Nombres descriptivos y autodocumentados
- Código DRY (Don't Repeat Yourself)
- Comentarios solo cuando agregan valor

### ✅ Reactive Programming
- **Spring WebFlux**: Framework no bloqueante
- **R2DBC**: Acceso reactivo a base de datos
- **Reactor**: Mono (0-1 elemento) y Flux (0-N elementos)
- **Backpressure**: Control de flujo de datos

### ✅ Seguridad
- **JWT**: Tokens stateless para autenticación
- **BCrypt**: Hash seguro de passwords
- **Permisos granulares**: Control por tienda y usuario
- **AOP**: Validación de permisos de forma declarativa
- **Validación en múltiples capas**: Controller, Service, Aspect

### ✅ Testing
- **Tests unitarios**: Servicios con mocks (rápidos, aislados)
- **Tests de integración**: Controllers con WebTestClient
- **Cobertura**: Casos happy path y edge cases
- **Mocking**: Mockito para aislar dependencias

---

## 🚀 Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17+ | Lenguaje de programación |
| **Spring Boot** | 3.x | Framework principal |
| **Spring WebFlux** | 3.x | Programación reactiva (no bloqueante) |
| **Spring Security** | 3.x | Autenticación y autorización |
| **R2DBC** | - | Driver reactivo para base de datos |
| **H2 Database** | - | Base de datos en memoria/archivo |
| **Lombok** | - | Reducción de boilerplate (@Data, @Builder) |
| **JJWT** | 0.11+ | Generación y validación de JWT |
| **JUnit 5** | - | Framework de testing |
| **Mockito** | - | Mocking para tests unitarios |
| **Reactor** | - | Biblioteca de programación reactiva (Mono/Flux) |
| **SpringDoc OpenAPI** | - | Documentación automática de API (Swagger) |
| **Maven** | 3.8+ | Gestión de dependencias y build |

---

## 📌 Convenciones de Código

### Estructura de Paquetes
- **domain**: Núcleo del negocio, sin dependencias externas
  - `model/`: POJOs puros con Lombok
  - `port/`: Interfaces que definen contratos
  - `exception/`: Excepciones de dominio

- **application**: Casos de uso y orquestación
  - `service/`: Lógica de negocio
  - `dto/`: Request/Response objects
  - `mapper/`: Conversión Domain ↔ DTO

- **infrastructure**: Detalles técnicos e implementaciones
  - `adapter/input/rest/`: Controllers (API REST)
  - `adapter/output/persistence/`: Repositorios y entidades
  - `adapter/output/security/`: Adaptadores de seguridad
  - `config/`: Configuraciones de Spring
  - `security/`: JWT, filtros, aspectos
  - `exception/`: Manejo global de errores

### Nomenclatura
- **Modelos de dominio**: Sustantivos simples (`Inventory`, `Product`)
- **Puertos**: Terminan en `Port` (`InventoryPort`)
- **Adaptadores**: Terminan en `Adapter` (`InventoryPersistenceAdapter`)
- **Servicios**: Terminan en `Service` (`InventoryService`)
- **Controllers**: Terminan en `Controller` (`InventoryController`)
- **DTOs**: Terminan en `Request`/`Response` según su uso
- **Entities**: Terminan en `Entity` (`InventoryEntity`)

### Programación Reactiva
- Usar `Mono<T>` para operaciones que retornan 0-1 elemento
- Usar `Flux<T>` para operaciones que retornan 0-N elementos
- **No bloquear**: Evitar `.block()` excepto en tests
- **Composición**: Usar operadores `.map()`, `.flatMap()`, `.filter()`

---

## 📚 Documentación Adicional

### Acceso a Documentación
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console (si está habilitado)

### Archivos de Configuración
- **application.yml**: Configuración principal (puerto, BD, JWT)
- **application-test.yml**: Configuración para tests
- **schema.sql**: Script de creación de tablas
- **data.sql**: Datos de prueba iniciales

### Endpoints Principales
```
POST   /api/auth/login                              # Autenticación
GET    /api/inventory/{sku}/stores                  # Listar inventario por SKU
GET    /api/inventory/{sku}/stores/{storeId}        # Inventario específico
PUT    /api/inventory/{sku}/stores/{storeId}        # Actualizar cantidad absoluta
PATCH  /api/inventory/{sku}/stores/{storeId}/adjust # Ajustar cantidad (+/-)
```

### Datos de Prueba (data.sql)
- **Usuarios**: admin/admin123, manager1/pass123
- **Productos**: REM-001-BL-M, JEAN-002-AZ-L, ZAPT-003-NG-42
- **Tiendas**: Tienda Centro, Tienda Norte, Tienda Sur

---

## 🔧 Comandos Útiles

### Compilar y ejecutar
```bash
# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

### Ejecutar tests
```bash
# Todos los tests
mvnw.cmd test

# Tests específicos
mvnw.cmd test -Dtest=InventoryServiceTest
mvnw.cmd test -Dtest=InventoryControllerTest
```

### Generar JAR
```bash
mvnw.cmd clean package
java -jar target/inventory-management-0.0.1-SNAPSHOT.jar
```

---

## 🎓 Ventajas de Esta Arquitectura

### ✅ Mantenibilidad
- Código organizado y fácil de navegar
- Responsabilidades claras
- Cambios localizados (cambiar BD no afecta al dominio)

### ✅ Testabilidad
- Dominio puro (fácil de testear sin frameworks)
- Servicios testables con mocks de puertos
- Tests rápidos (sin BD en tests unitarios)

### ✅ Flexibilidad
- Fácil cambiar implementaciones (R2DBC → JPA, JWT → OAuth2)
- Agregar nuevos adaptadores sin tocar dominio
- Múltiples interfaces (REST, GraphQL, gRPC)

### ✅ Escalabilidad
- Programación reactiva (no bloqueante)
- Manejo eficiente de concurrencia
- Preparado para microservicios

---

**¡Arquitectura hexagonal reactiva, limpia y mantenible!** 🎉
