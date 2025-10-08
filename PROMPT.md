# Documentación de Uso de Inteligencia Artificial

Este documento detalla cómo se utilizó la Inteligencia Artificial durante el desarrollo de este proyecto de gestión de inventario.

## Tabla de Contenidos
1. [ChatGPT - Comprensión de Requisitos](#chatgpt---comprensión-de-requisitos)
2. [Bolt AI Builder - Estructura del Proyecto](#bolt-ai-builder---estructura-del-proyecto)
3. [GitHub Copilot - Desarrollo y Documentación](#github-copilot---desarrollo-y-documentación)

---

## ChatGPT - Comprensión de Requisitos

### Objetivo
Entender la consigna del proyecto y obtener ideas sobre la arquitectura y características a implementar.

### Contexto
Ya tenía definido que quería usar programación reactiva con Arquitectura Hexagonal, pero necesitaba orientación sobre mejores prácticas y componentes adicionales.

### Prompt Utilizado
```
Estoy desarrollando un sistema de gestión de inventario en Java con Spring Boot. 
Quiero implementarlo usando programación reactiva (WebFlux) y arquitectura hexagonal 
(puertos y adaptadores). El sistema consta de una backend que expone diferentes endpoints para consultar y actualizar una bbdd. 
Los endpoints son sobre gestion de inventario como consultar inventario, actualizar stock en una tienda, etc.

Podrias decirme que debería incluir en este proyecto para que cumpla con las mejores practicas de programacion en Java y que sea un proyecto solido. 

Quiero que me des una lista de ideas que te parezcan importantes para implementar en este proyecto
```

### Resultado
ChatGPT proporcionó ideas clave como:
- Implementar un manejador global de errores (`GlobalErrorHandler`)
- Configurar logging centralizado con diferentes niveles
- Implementar autenticación JWT para endpoints seguros
- Usar validaciones personalizadas con anotaciones
- Aplicar manejo de concurrencia con optimistic locking

---

## Bolt AI Builder - Estructura del Proyecto

### Objetivo
Generar la estructura base del proyecto con Spring Boot, programación reactiva y arquitectura hexagonal.

### Herramienta
[Bolt AI Builder](https://bolt.new/) - Una IA especializada en crear proyectos completos, principalmente orientada a frontend pero adaptable para backend.

### Prompt Utilizado
```
Sos un dev backend con Java y Spring Boot, con experiencia en arquitectura hexagonal, microservicios y buenas practicas. Quiero que me generes un proyecto de ejemplo en Java con Spring Boot para un sistema de gestion de inventario de una cadena de tiendas.

Lo que necesito hacer es:
- Estructura de carpetas hexagonal: domain, application, infrastructure.
- Spring WebFlux configurado.
- API REST basica de inventario:
  - GET /inventory/{productSku}/stores
  - GET /inventory/{productSku}/stores/{storeId}
  - PUT /inventory/{productSku}/stores/{storeId}
  - POST /inventory/{productSku}/stores/{storeId}/adjustments
- Modelo de datos en SQLite con stores, products, inventory.
- Concurrencia: manejar actualizaciones de stock con version para control optimista.
- Seguridad simple con JWT (puede ser mock o minimal).
- Handler global de errores y catalogo basico de errores.
- Logging y Swagger/OpenAPI.

Criterios:
- Priorizar consistencia sobre disponibilidad.
- Codigo limpio y legible, usa Lombok
- Usar programacion reactiva (Mono/Flux)
- SQLite

Mi estructura de base de datos es la siguiente:
stores
* id (PK) 
* name 
* is_active (bool) 
* created_at, updated_at 

products 
* id (PK) 
* sku (único) 
* name 
* description 
* is_active (bool) 
* created_at, updated_at 

inventory 
* id (PK, FK -> products) 
* store_id (PK, FK -> stores) 
* available_qty (int, no negativo) 
* version (int) 
* updated_at 

Un product puede estar en muchos stores, es decir que cada fila de inventory representa el stock actual de un producto en una tienda.

La estrucutra que quiero del proyecto es la siguiente:
domain (modelos y puertos)
application (dtos y servicios)
infraestructure (controladores, y repositorios)
```

### Resultado
La IA generó un proyecto que incluía:
- Estructura de carpetas siguiendo arquitectura hexagonal (domain, application, infrastructure)
- Configuración de Spring WebFlux
- Endpoints básicos RESTful
- Configuración de dependencias en `pom.xml`, algunas estaban mal o deprecadas.
- Clases de dominio, servicios y controladores básicos

Este resultado sirvió como punto de partida que luego fue personalizado y expandido según los requisitos específicos del proyecto.

**Aclaracion**: El resultado tenia muchos errores y partes incompletas, pero sirvió como un buen proyecto inicial.

---

## GitHub Copilot - Desarrollo y Documentación

### 1. Control Personalizado de Errores

#### Objetivo
Implementar un sistema robusto de manejo de errores con respuestas personalizadas.

#### Prompt Utilizado
```
Necesito que me ayudes a implementar un sistema de manejo de errores personalizado.
Lo que quiero es:
1. Crear excepciones personalizadas para diferentes casos de negocio 
   (Producto no encontrado, Insuficiente Stock, etc.)
2. Implementar un GlobalErrorHandler que capture todas las excepciones
3. Retornar respuestas JSON consistentes con el formato:
   {
     "timestamp": "...",
     "status": 404,
     "error": "Not Found",
     "message": "Mensaje descriptivo",
     "path": "/api/inventory/123"
   }
4. Manejar tanto errores de negocio como errores técnicos
```

#### Resultado
GitHub Copilot generó:
- Excepciones personalizadas en el paquete `domain/exception`
- `GlobalErrorHandler` en `infrastructure/config`
- DTOs para respuestas de error estandarizadas
- Manejo apropiado de errores reactivos con `onErrorResume` y `onErrorMap`

**Aclaracion**: Igual que antes, el código generado necesitó ajustes y revisiones para que funcione completamente, pero sirvió como una base sólida.

---

### 2. Documentación del Proyecto

#### Objetivo
Crear toda la documentación técnica requerida por la consigna del proyecto.

#### Prompt Utilizado
```
Necesito generar la documentacion completa para mi proyecto de gestion de inventario. 
El proyecto usa Spring Boot WebFlux con arquitectura hexagonal y maneja:
- Autenticación JWT
- Endpoints para modificar stock de productos de inventario
- Ajuste incremental de stock con control de concurrencia
- Manejo de errores personalizado

Necesito crear los siguientes documentos:
1. README.md: Descripcion del proyecto, tecnologias usadas, instrucciones de 
   instalacion y ejecucion. Seria como una documetnacion general del proyecto.
2. RUN.md: Guía detallada paso a paso para ejecutar el proyecto. Incluir documentacion tanto para windows como para Linux y Mac
3. docs/DOCUMENTATION.md: Documentación tecnica completa que incluya:
   - Arquitectura del sistema
   - Descripcion de endpoints con ejemplos de request/response
   - Autenticacion
   - Manejo de concurrencia
   - Gestion de errores

Incluir en la documentacion diagramas los diagramas de secuencia que representan:
   - Login (docs/diagrams/diagrama login.png)
   - Actualizar stock: (docs/diagrams/diagrama actualizar stock.png)
   - Concurrencia: (docs/diagrams/diagrama concurrencia.png)
   - Control de errores (docs/diagrams/diagrama errores.png)

Tambien comentar que estan las colecciones de Postman para probar los endpoints en docs/postman/

La documentacion tiene que ser clara, profesional y completa
```

#### Resultado
GitHub Copilot generó:
- `README.md` con descripción completa del proyecto
- `RUN.md` con instrucciones paso a paso
- `DOCUMENTATION.md` en el directorio `docs/`

---

### 3. Tests Unitarios y de Integración

#### Objetivo
Crear tests completos con mocks para validar la funcionalidad del sistema.

#### Prompt Utilizado
```
Necesito que me ayudes a crear tests unitarios y de integracion
Los tests que necesito son:
1. InventoryServiceTest: Pruebas unitarias del servicio que incluyan:
   - Busqueda de productos en el inventario (por ID, por SKU, todos)
   - Actualizacion de stock (revisar los dos endpoints de actualizacion)
   - Casos de error (producto no encontrado, stock insuficiente, conflicto de versión)
   
2. InventoryControllerTest: Pruebas del controlador que validen los endpoints:
   - GET /inventory/{productSku}/stores
   - GET /inventory/{productSku}/stores/{storeId}
   - PUT /inventory/{productSku}/stores/{storeId}
   - POST /inventory/{productSku}/stores/{storeId}/adjustments

Usar Mockito para mocks y JUnit 5 para las pruebas.
Usar StepVerifier para validar flujos reactivos (Mono/Flux)
```

#### Resultado
GitHub Copilot generó:
- `InventoryServiceTest.java` con cobertura completa de casos de uso
- `InventoryControllerTest.java` con tests de endpoints
- Uso de `@Mock` y `@InjectMocks` para inyección de dependencias
- `StepVerifier` para validación de flujos reactivos (Mono/Flux)
- Tests para escenarios exitosos y de error
- Configuración de mocks con `when().thenReturn()` y `verify()`
- Cobertura de casos edge como concurrencia y validaciones

---

## Conclusión

El uso de Inteligencia Artificial fue fundamental en diferentes etapas del proyecto:

1. **Planificación**: ChatGPT ayudó a identificar componentes esenciales y mejores prácticas
2. **Scaffolding**: Bolt AI Builder aceleró la creación de la estructura inicial del proyecto
3. **Desarrollo**: GitHub Copilot sirvió como asistente para implementar funcionalidades
4. **Documentación**: Se automatizó la creación de documentación técnica completa
5. **Testing**: Facilitó la creación de tests robustos con mocks apropiados

Si bien la IA proporcionó una base sólida, todo el código fue revisado, personalizado y adaptado 
para cumplir con los requisitos específicos del proyecto y seguir las mejores prácticas de 
desarrollo de software.

