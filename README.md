# 🏪 Sistema de Gestión de Inventario

Sistema profesional de gestión de inventario construido con **Spring Boot 3.5.6** y **Spring WebFlux**, implementando arquitectura hexagonal, programación reactiva y control de concurrencia optimista.

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Contexto del Sistema](#-contexto-del-sistema)
- [Características Principales](#-características-principales)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Arquitectura](#-arquitectura)
- [Prácticas de Programación](#-prácticas-de-programación)
- [Manejo de Concurrencia](#-manejo-de-concurrencia)
- [Seguridad](#-seguridad)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Inicio Rápido](#-inicio-rápido)
- [Pruebas con Postman](#-pruebas-con-postman)
- [Documentación Adicional](#-documentación-adicional)
- [Licencia](#-licencia)

---

## 🎯 Descripción General

Este sistema de gestión de inventario es una aplicación empresarial que permite gestionar el stock de productos en múltiples tiendas de forma segura, eficiente y reactiva. Implementa las mejores prácticas de desarrollo de software moderno, incluyendo arquitectura limpia, programación reactiva y control de concurrencia robusto.

### Casos de Uso Principales

- **Gestión de Stock**: Consulta, actualización y ajuste de inventario por producto y tienda
- **Autenticación y Autorización**: Sistema JWT con roles (Admin y Store User)
- **Control de Permisos**: Los usuarios tienen acceso granular por tienda
- **Operaciones Concurrentes**: Manejo seguro de múltiples operaciones simultáneas sobre el mismo inventario

---

## 🏬 Contexto del Sistema

Este sistema funciona como **backend centralizado** para una cadena de tiendas minoristas. La arquitectura está diseñada para servir a múltiples clientes:

### Arquitectura del Sistema

![Diagrama General](docs/diagrams/diagrama%20general.png)

### Flujo de Operaciones

#### 1. **Tiendas Minoristas**
Cada tienda cuenta con terminales de punto de venta que se conectan al backend para:
- **Consultar stock disponible** antes de realizar una venta
- **Actualizar inventario** cuando llega nueva mercancía
- **Registrar ventas** con ajustes incrementales negativos
- **Gestionar su propio inventario** con permisos limitados a su tienda

**Autenticación:** Usuarios con rol `STORE_USER` tienen acceso solo a su(s) tienda(s) asignada(s).

#### 2. **Página Web Pública**
Una aplicación web orientada al cliente que se conecta al backend para:
- **Mostrar disponibilidad de productos** en tiempo real
- **Consultar stock por tienda** para que los clientes sepan dónde hay disponibilidad
- **Información de inventario** solo lectura, sin capacidad de modificar

**Autenticación:** Puede usar una cuenta con permisos de solo lectura o endpoints públicos específicos.

#### 3. **Administración Central**
Usuarios administradores con rol `ADMIN` tienen:
- **Acceso completo** a todas las tiendas
- **Capacidad de consultar** inventario global
- **Gestión de productos** y configuraciones
- **Monitoreo** de operaciones y logs

---

## ✨ Características Principales

### 🏗️ Arquitectura y Diseño

- **Arquitectura Hexagonal (Puertos y Adaptadores)**: Separación clara entre lógica de negocio e infraestructura
- **Domain-Driven Design (DDD)**: Modelado centrado en el dominio del negocio
- **SOLID Principles**: Código mantenible y extensible
- **Clean Code**: Código legible y bien documentado

### ⚡ Programación Reactiva

- **Spring WebFlux**: Framework reactivo no bloqueante
- **Project Reactor**: Uso de `Mono` y `Flux` para operaciones asíncronas
- **Backpressure**: Manejo inteligente de flujos de datos
- **R2DBC**: Base de datos completamente reactiva

### 🔒 Seguridad

- **JWT Authentication**: Tokens seguros con expiración configurable
- **Spring Security Reactive**: Seguridad adaptada para WebFlux
- **Role-Based Access Control (RBAC)**: Roles de Admin y Store User
- **Store-Level Permissions**: Control granular por tienda usando AOP
- **Password Encryption**: BCrypt para hash de contraseñas

### 🔄 Control de Concurrencia

- **Optimistic Locking**: Control de versiones para evitar conflictos
- **Retry Mechanism**: Reintentos automáticos con backoff exponencial
- **Idempotencia**: Operaciones seguras y predecibles

### 📊 Observabilidad

- **Logging Estructurado**: SLF4J + Logback con múltiples niveles
- **Request Tracing**: Logs detallados de cada operación con usuario e IP
- **Health Checks**: Spring Boot Actuator para monitoreo
- **API Documentation**: Swagger/OpenAPI 3.0 integrado

### ⚡ Rendimiento

- **Connection Pooling**: Pool de conexiones R2DBC optimizado
- **Operaciones No Bloqueantes**: Máximo aprovechamiento de recursos
- **Índices de Base de Datos**: Optimización de consultas

---

## 🛠️ Tecnologías Utilizadas

### Core Framework
- **Java 21**: Última versión LTS con mejoras de rendimiento
- **Spring Boot 3.5.6**: Framework principal
- **Maven**: Gestión de dependencias y construcción

### Stack Reactivo
- **Spring WebFlux**: Web framework reactivo
- **Spring Data R2DBC**: Acceso reactivo a base de datos
- **Project Reactor**: Biblioteca de programación reactiva
- **R2DBC H2 Driver**: Driver reactivo para H2

### Seguridad
- **Spring Security**: Framework de seguridad
- **JWT (jjwt 0.11.5)**: JSON Web Tokens
- **BCrypt**: Encriptación de contraseñas

### Base de Datos
- **H2 Database**: Base de datos embebida (file-based)
- **R2DBC**: Reactive Relational Database Connectivity

### Utilidades
- **Lombok**: Reducción de código boilerplate
- **Validation API**: Validación de datos
- **SpringDoc OpenAPI**: Documentación automática de API

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking framework
- **Spring Boot Test**: Testing integrado
- **Reactor Test**: Testing para código reactivo

### DevOps
- **Spring Boot DevTools**: Hot reload en desarrollo
- **Spring Boot Actuator**: Métricas y health checks
- **Logback**: Sistema de logging avanzado

---

## 🏛️ Arquitectura

### Arquitectura Hexagonal (Puertos y Adaptadores)

El proyecto implementa una arquitectura hexagonal completa, dividiendo el sistema en tres capas principales:

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                      │
│  ┌─────────────────────┐         ┌─────────────────────┐   │
│  │  Input Adapters     │         │  Output Adapters    │   │
│  │  - REST Controllers │         │  - R2DBC Repos      │   │
│  │  - Security Filters │         │  - JWT Adapter      │   │
│  └──────────┬──────────┘         └──────────┬──────────┘   │
│             │                               │               │
└─────────────┼───────────────────────────────┼───────────────┘
              │                               │
┌─────────────┼───────────────────────────────┼───────────────┐
│             ▼                               ▲               │
│        ┌─────────────────────────────────────────┐         │
│        │      APPLICATION LAYER                  │         │
│        │  - Services (Business Logic)            │         │
│        │  - DTOs (Data Transfer)                 │         │
│        │  - Mappers (Entity ↔ DTO)               │         │
│        └─────────────────────────────────────────┘         │
│                          │                                  │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                          ▼                                  │
│              ┌──────────────────────┐                       │
│              │   DOMAIN LAYER       │                       │
│              │  - Models/Entities   │                       │
│              │  - Ports (Interfaces)│                       │
│              │  - Business Rules    │                       │
│              │  - Exceptions        │                       │
│              └──────────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

#### Capa de Dominio (Domain Layer)
- **Responsabilidad**: Lógica de negocio pura, independiente de frameworks
- **Componentes**:
  - `model`: Entidades de dominio (Inventory, Product, Store, User)
  - `port`: Interfaces que definen contratos (InventoryPort, ProductPort, etc.)
  - `exception`: Excepciones de negocio (BusinessException)

#### Capa de Aplicación (Application Layer)
- **Responsabilidad**: Orquestación de casos de uso y lógica de aplicación
- **Componentes**:
  - `service`: Servicios que implementan casos de uso reactivos
  - `dto`: Objetos de transferencia de datos
  - `mapper`: Conversión entre entidades y DTOs

#### Capa de Infraestructura (Infrastructure Layer)
- **Responsabilidad**: Implementaciones técnicas y frameworks
- **Componentes**:
  - `adapter.input.rest`: Controladores REST (InventoryController, AuthController)
  - `adapter.output.persistence`: Repositorios R2DBC
  - `security`: JWT, filtros, autenticación
  - `config`: Configuraciones de Spring
  - `exception`: Manejo global de errores

### Flujo de Datos Reactivo

```
Request → Controller → Service → Port → Repository → Database
   ↓                      ↓               ↓
Mono/Flux          Business Logic    R2DBC Query
   ↓                      ↓               ↓
Response ← DTO ← Mapper ← Entity ← Reactive Result
```

---

## 💻 Prácticas de Programación

### SOLID Principles

1. **Single Responsibility**: Cada clase tiene una única responsabilidad
   - Servicios separados para inventario y autenticación
   - Mappers dedicados para transformaciones

2. **Open/Closed**: Abierto para extensión, cerrado para modificación
   - Uso de interfaces (Ports) para nuevas implementaciones
   - ErrorCode enum extensible

3. **Liskov Substitution**: Las implementaciones son intercambiables
   - Repositories implementan contratos de Port

4. **Interface Segregation**: Interfaces específicas y cohesivas
   - Ports separados por entidad (ProductPort, StorePort, etc.)

5. **Dependency Inversion**: Dependencias sobre abstracciones
   - Services dependen de Ports, no de implementaciones concretas

### Clean Code

- **Nombres Descriptivos**: Variables y métodos con nombres claros
- **Funciones Pequeñas**: Métodos con una sola responsabilidad
- **Comentarios Mínimos**: Código auto-documentado
- **Manejo de Errores**: Excepciones específicas con códigos de error
- **DRY (Don't Repeat Yourself)**: Reutilización de código

### Programación Reactiva

- **Operadores Reactor**: map, flatMap, filter, switchIfEmpty
- **Error Handling**: onErrorMap, onErrorResume, doOnError
- **Logging**: doOnSuccess, doOnError para trazabilidad
- **Composition**: Composición de Monos y Flux para flujos complejos

---

## 🔄 Manejo de Concurrencia

### Optimistic Locking

El sistema implementa **Optimistic Locking** para manejar operaciones concurrentes sobre el inventario:

```java
@Data
public class Inventory {
    private Long id;
    private Integer availableQty;
    @Builder.Default
    private Integer version = 0;  // Control de versión
}
```

#### Funcionamiento

1. **Lectura**: Se lee el registro con su versión actual
2. **Modificación**: Se actualiza el campo `availableQty`
3. **Escritura**: Se guarda verificando que la versión no haya cambiado
4. **Conflicto**: Si la versión cambió, se lanza `OptimisticLockingFailureException`

### Retry Mechanism

Cuando ocurre un conflicto de concurrencia, el sistema reintenta automáticamente:

```java
.retryWhen(Retry.backoff(3, Duration.ofMillis(100))
    .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
    .doBeforeRetry(signal -> log.warn("Retrying... attempt: {}", signal.totalRetries())))
```

- **Máximo 3 reintentos**
- **Backoff exponencial** (100ms, 200ms, 400ms)
- **Solo para conflictos de concurrencia**

### Escenarios de Concurrencia

#### Caso 1: Dos Ventas Simultáneas
```
Usuario A lee stock: 10 (version=1)
Usuario B lee stock: 10 (version=1)
Usuario A vende 3 → stock=7 (version=2) ✓
Usuario B vende 2 → CONFLICTO → Reintento
Usuario B relee stock: 7 (version=2)
Usuario B vende 2 → stock=5 (version=3) ✓
```

#### Caso 2: Venta y Reposición Simultáneas
```
Venta (-5) y Reposición (+100) al mismo tiempo
Ambas operaciones se serializan correctamente
Resultado final es consistente
```

---

## 🔐 Seguridad

### Autenticación JWT

1. **Login**: Usuario envía credenciales → recibe JWT token
2. **Request**: Cliente incluye token en header `Authorization: Bearer <token>`
3. **Validación**: Filtro valida token y extrae información del usuario
4. **Contexto**: Se establece SecurityContext reactivo

### Roles y Permisos

#### Roles
- **ADMIN**: Acceso total a todas las tiendas
- **STORE_USER**: Acceso limitado a tiendas asignadas
- **WEB_USER**: Acceso solo lectura a información de inventario

#### Control de Acceso (AOP)

```java
@RequireStorePermission  // Verifica permisos de tienda
@RequireStorePermission(adminOnly = true)  // Solo administradores
```

El aspecto `StorePermissionAspect` intercepta métodos y valida:
- Usuario autenticado
- Role del usuario
- Permisos específicos sobre la tienda

### Usuarios Predefinidos

| Username | Password | Role | Acceso |
|----------|----------|------|--------|
| admin | 12345 | ADMIN | Todas las tiendas |
| user_dinosaurio | 12345 | STORE_USER | Shopping Dinosaurio Mall (ID: 1) |
| user_maipu | 12345 | STORE_USER | Centro Maipu 712 (ID: 2) |
| user_nuevo_centro | 12345 | STORE_USER | Nuevo Centro Shopping (ID: 3) |
| web | 12345 | WEB_USER | Solo consulta de inventario por producto |

---

## 📁 Estructura del Proyecto

```
inventory-management/
├── docs/                           # Documentación
│   ├── diagrams/                   # Diagramas de arquitectura y flujo
│   │   ├──mermaid/               # Diagramas en formato Mermaid
│   │   ├── diagrama general.png   # Diagrama del sistema completo
│   │   ├── diagrama login.png     # Flujo de autenticación
│   │   ├── diagrama actualizar stock.png  # Ajuste de inventario
│   │   ├── diagrama concurencia.png       # Manejo de concurrencia
│   │   └── diagrama control errores.png   # Manejo de errores
│   ├── postman/                    # Colecciones para pruebas
│   │   ├── collections/           # Colecciones de Postman
│   │   └── environments/          # Variables de entorno
│   └── DOCUMENTATION.md            # Documentación técnica completa
├── logs/                           # Archivos de log
├── src/
│   ├── main/
│   │   ├── java/com/meli/inventorymanagement/
│   │   │   ├── application/        # Capa de aplicación
│   │   │   │   ├── dto/           # Data Transfer Objects
│   │   │   │   ├── mapper/        # Mappers Entity ↔ DTO
│   │   │   │   └── service/       # Servicios de aplicación
│   │   │   ├── common/            # Componentes compartidos
│   │   │   │   └── constant/      # Constantes y ErrorCodes
│   │   │   ├── domain/            # Capa de dominio (核心)
│   │   │   │   ├── exception/     # Excepciones de negocio
│   │   │   │   ├── model/         # Entidades de dominio
│   │   │   │   └── port/          # Puertos (interfaces)
│   │   │   └── infrastructure/    # Capa de infraestructura
│   │   │       ├── adapter/       # Adaptadores
│   │   │       │   ├── input/rest/    # Controllers REST
│   │   │       │   └── output/        # Repositorios, adapters
│   │   │       ├── config/        # Configuraciones Spring
│   │   │       ├── exception/     # Manejo global de errores
│   │   │       └── security/      # JWT, Security, AOP
│   │   └── resources/
│   │       ├── application.yml    # Configuración principal
│   │       ├── schema.sql         # Schema de base de datos
│   │       └── data.sql           # Datos iniciales
│   └── test/                      # Tests unitarios e integración
├── target/                        # Archivos compilados
├── inventory-db.mv.db             # Base de datos H2 (file)
├── pom.xml                        # Configuración Maven
├── README.md                      # Este archivo
└── RUN.md                         # Guía de ejecución
```

### Módulos Principales

#### Application Layer
- **InventoryService**: Gestión de inventario (CRUD, ajustes)
- **AuthService**: Autenticación y generación de tokens
- **DTOs**: AuthRequest, AuthResponse, InventoryRequest, InventoryResponse
- **Mappers**: Transformaciones entre entidades y DTOs

#### Domain Layer
- **Models**: Inventory, Product, Store, User, UserStorePermission
- **Ports**: InventoryPort, ProductPort, StorePort, UserPort, AuthenticationPort
- **Exceptions**: BusinessException con ErrorCode

#### Infrastructure Layer
- **Controllers**: InventoryController, AuthController
- **Repositories**: R2DBC repositories (InventoryRepository, etc.)
- **Security**: JwtUtil, JwtAuthenticationFilter, StorePermissionAspect
- **Config**: SecurityConfig, R2dbcConfig, OpenApiConfig

---

## 🚀 Inicio Rápido

**Windows:**
```cmd
# 1. Clonar el repositorio
git clone <repository-url>
cd inventory-management

# 2. Compilar el proyecto
mvnw clean package

# 3. Ejecutar la aplicación
mvnw spring-boot:run

# 4. Acceder a Swagger UI
# http://localhost:8080/swagger-ui.html
```

**Linux/macOS:**
```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd inventory-management

# 2. Compilar el proyecto
./mvnw clean package

# 3. Ejecutar la aplicación
./mvnw spring-boot:run

# 4. Acceder a Swagger UI
# http://localhost:8080/swagger-ui.html
```

Para instrucciones detalladas paso a paso, ver **[RUN.md](RUN.md)**

---

## 📮 Pruebas con Postman

El proyecto incluye **colecciones completas de Postman** listas para usar, ubicadas en `docs/postman/`. Esto te permite probar todos los endpoints de manera rápida y sencilla.

### 📦 Colecciones Incluidas

#### 1. **Auth Collection** (`Auth.postman_collection.json`)
Contiene requests para autenticación:
- Login como Admin
- Login como usuarios de tienda

#### 2. **Inventory Collection** (`Inventory.postman_collection.json`)
Operaciones completas de inventario:
- Consultar inventario por producto y tienda
- Listar todo el inventario de un producto
- Actualizar cantidad absoluta
- Ajustes incrementales (ventas/reposiciones)

#### 3. **Documentation Collection** (`Documentation.postman_collection.json`)
Ejemplos de la documentación técnica:
- Swagger UI
- OpenAPI documentación
- Actuator Health

### 🌍 Environment

**Environment:** `dev.postman_environment.json`

Variables preconfiguradas:
- `base_url`: http://localhost:8080
- `token`: Se actualiza automáticamente al hacer login

---

## 🧪 Testing

**Windows:**
```cmd
# Ejecutar todos los tests
mvnw test

# Ejecutar con reporte de cobertura
mvnw clean test jacoco:report
```

**Linux/macOS:**
```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar con reporte de cobertura
./mvnw clean test jacoco:report
```

---

## 📚 Documentación Adicional

### Documentos Principales

- **[RUN.md](RUN.md)**: Guía completa de ejecución para Windows, Linux y macOS
- **[docs/DOCUMENTATION.md](docs/DOCUMENTATION.md)**: Documentación técnica de la API
  - Endpoints detallados con ejemplos
  - Códigos de error completos
  - Diagramas de secuencia
  - Modelos de datos
  - Ejemplos de requests/responses

### Recursos Visuales

- **[docs/diagrams/diagrama general.png](docs/diagrams/diagrama%20general.png)**: Arquitectura completa del sistema
- **[docs/diagrams/diagrama login.png](docs/diagrams/diagrama%20login.png)**: Flujo de autenticación JWT
- **[docs/diagrams/diagrama actualizar stock.png](docs/diagrams/diagrama%20actualizar%20stock.png)**: Proceso de ajuste de inventario
- **[docs/diagrams/diagrama concurencia.png](docs/diagrams/diagrama%20concurencia.png)**: Manejo de optimistic locking
- **[docs/diagrams/diagrama control errores.png](docs/diagrams/diagrama%20control%20errores.png)**: Sistema de manejo de errores

### Herramientas de Prueba

- **[docs/postman/](docs/postman/)**: Colecciones completas de Postman
- **Swagger UI**: http://localhost:8080/swagger-ui.html (cuando el servidor esté corriendo)

---

## 💡 Casos de Uso Típicos

### 1. Venta en Tienda
```
1. Usuario de tienda hace login
2. Consulta stock disponible del producto
3. Realiza ajuste negativo (ej: -3 unidades vendidas)
4. Sistema valida stock suficiente
5. Actualiza inventario con control de concurrencia
```

### 2. Reposición de Stock
```
1. Usuario autorizado hace login
2. Consulta inventario actual
3. Realiza ajuste positivo (ej: +50 unidades recibidas)
4. Sistema actualiza inventario
```

### 3. Consulta desde Web Pública
```
1. Aplicación web consulta disponibilidad
2. Sistema retorna stock en todas las tiendas
3. Cliente ve dónde hay disponibilidad
```

---

**¡Gracias por usar el Sistema de Gestión de Inventario!** 🎉
