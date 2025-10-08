# Diagrama de Secuencia: Ajuste Incremental de Stock (Adjustment)

Este diagrama muestra cómo un empleado de sucursal ajusta el stock (suma o resta) de un producto.

## ¿Qué es la Verificación de Permisos?

Es un sistema de seguridad basado en **AOP (Aspect-Oriented Programming)** que intercepta automáticamente las peticiones antes de ejecutar el ajuste de stock.

**Reglas:**
- **Usuarios ADMIN**: Pueden ajustar el stock de CUALQUIER tienda
- **Usuarios STORE_USER**: Solo pueden ajustar el stock de la tienda que tienen asignada
- **Validación automática**: Se verifica comparando el `storeId` del request con el `storeId` asignado al usuario en la BD

**Ejemplo:**
- Usuario "juan_tienda1" (asignado a tienda 1) → Solo puede ajustar stock de tienda 1
- Usuario "admin" (rol ADMIN) → Puede ajustar stock de cualquier tienda

```mermaid
sequenceDiagram
    actor Empleado as Empleado de Sucursal
    participant Controller as InventoryController
    participant Aspect as StorePermissionAspect<br/>(AOP Interceptor)
    participant SecurityContext as Spring Security Context
    participant UserPort as UserPort
    participant Service as InventoryService
    participant Repository as InventoryRepository
    participant DB as Base de Datos

    Note over Empleado,DB: FASE 1: VERIFICACIÓN DE PERMISOS (ANTES DEL SERVICE)
    
    Empleado->>Controller: POST /api/inventory/SHIRT-001/stores/1/adjustments<br/>{adjustment: -5}<br/>Header: Bearer token
    
    Note over Controller: Anotación @RequireStorePermission<br/>activa el interceptor AOP
    
    Controller->>Aspect: Interceptar petición automáticamente
    activate Aspect
    
    Aspect->>Aspect: Extraer storeId del path = 1
    Aspect->>SecurityContext: Obtener usuario del token
    SecurityContext-->>Aspect: Username: "juan_tienda1"
    
    Aspect->>UserPort: findByUsername("juan_tienda1")
    activate UserPort
    UserPort->>DB: SELECT * FROM users WHERE username = 'juan_tienda1'
    activate DB
    DB-->>UserPort: User record
    deactivate DB
    UserPort-->>Aspect: User{username: "juan_tienda1", role: "STORE_USER", storeId: 1}
    deactivate UserPort
    
    Aspect->>Aspect: Evaluar permisos
    
    alt Usuario es ADMIN
        Note over Aspect: ✅ ADMIN tiene acceso a todas las tiendas
        Aspect->>Aspect: Permitir acceso
    else Usuario es STORE_USER y storeId coincide
        Note over Aspect: ✅ Usuario tiene acceso a esta tienda
        Aspect->>Aspect: Permitir acceso
    else storeId NO coincide
        Note over Aspect: ❌ Usuario intenta acceder a otra tienda
        Aspect-->>Empleado: 403 FORBIDDEN<br/>{errorCode: "AUTH-008",<br/>message: "Access denied to store"}
    end
    
    deactivate Aspect
    
    Note over Empleado,DB: FASE 2: PROCESAMIENTO DEL AJUSTE
    
    Controller->>Service: adjustInventory(sku, storeId, adjustment)
    activate Service
    
    Service->>Service: Validar adjustment != 0
    
    Service->>Repository: findByProductSkuAndStoreId(SHIRT-001, 1)
    activate Repository
    Repository->>DB: SELECT * FROM inventory<br/>JOIN product ON...<br/>WHERE sku='SHIRT-001' AND store_id=1
    activate DB
    DB-->>Repository: Inventory record (availableQty: 20, version: 5)
    deactivate DB
    Repository-->>Service: Inventory{availableQty: 20, version: 5}
    deactivate Repository
    
    Service->>Service: Calcular nueva cantidad<br/>newQty = 20 + (-5) = 15
    Service->>Service: Validar newQty >= 0
    
    Service->>Repository: save(Inventory{availableQty: 15, version: 6})
    activate Repository
    Repository->>DB: UPDATE inventory<br/>SET available_qty=15, version=6<br/>WHERE id=? AND version=5
    activate DB
    
    alt Version coincide (sin conflicto)
        DB-->>Repository: 1 row updated
        Repository-->>Service: Guardado exitoso
    else Optimistic Lock (version no coincide)
        DB-->>Repository: 0 rows updated
        Repository-->>Service: OptimisticLockingFailureException
        Note over Service: Reintento automático<br/>(hasta 3 veces)
        Service->>Repository: Releer inventario actual
        Repository->>DB: SELECT con version actualizada
        DB-->>Repository: Inventory con nueva version
        Repository-->>Service: Datos actualizados
        Service->>Service: Recalcular y validar
        Service->>Repository: Guardar con version correcta
        Repository->>DB: UPDATE con nueva version
        DB-->>Repository: Actualizado exitosamente
        Repository-->>Service: Guardado exitoso
    end
    
    deactivate DB
    deactivate Repository
    
    Service->>Repository: Obtener datos completos (join con Product y Store)
    activate Repository
    Repository->>DB: SELECT inventory.*, product.*, store.*<br/>FROM inventory<br/>JOIN product ON...<br/>JOIN store ON...
    activate DB
    DB-->>Repository: Complete result set
    deactivate DB
    Repository-->>Service: InventoryResponse completo
    deactivate Repository
    
    Service-->>Controller: InventoryResponse
    deactivate Service
    Controller-->>Empleado: 200 OK<br/>{productSku: "SHIRT-001", availableQty: 15, ...}
```

## Descripción del Flujo

### FASE 1: Verificación de Permisos (Automática vía AOP)
1. **Interceptor se activa**: El `@RequireStorePermission` dispara el `StorePermissionAspect`
2. **Extrae storeId**: Del path del request (`/stores/1/`)
3. **Obtiene usuario**: Del token JWT en el header
4. **Consulta BD**: Busca el usuario y su rol/tienda asignada
5. **Evalúa reglas**:
   - Si es ADMIN → ✅ Permitir
   - Si es STORE_USER → Comparar `storeId` del request vs `storeId` del usuario
6. **Bloquea o permite**: Si no coincide, devuelve 403 y NO continúa

### FASE 2: Procesamiento del Ajuste (Solo si pasó la fase 1)
7. **Validaciones de negocio**: Ajuste != 0, inventario existe
8. **Cálculo**: Nueva cantidad = actual + ajuste
9. **Validación de stock**: No puede quedar negativo
10. **Guardado con Optimistic Locking**: Manejo de concurrencia
11. **Respuesta**: Inventario actualizado

## Request/Response

**Request (venta de 5 unidades):**
```json
POST /api/inventory/SHIRT-001/stores/1/adjustments
{
  "adjustment": -5
}
```

**Request (recepción de 10 unidades):**
```json
{
  "adjustment": 10
}
```

**Response exitosa (200):**
```json
{
  "productSku": "SHIRT-001",
  "productName": "Camisa Azul",
  "storeId": 1,
  "storeName": "Tienda Centro",
  "availableQty": 15,
  "version": 6,
  "updatedAt": "2025-10-07T15:45:30"
}
```

## Errores Posibles

| Código | Descripción |
|--------|-------------|
| 400 | Adjustment = 0 o stock resultante negativo |
| 403 | Usuario sin permiso para esa tienda |
| 404 | Inventario no existe |
| 409 | Error de concurrencia (múltiples ajustes simultáneos) |

## Casos de Uso

- **Venta**: `adjustment: -5` (reduce stock)
- **Recepción de mercadería**: `adjustment: 20` (aumenta stock)
- **Devolución**: `adjustment: 2` (aumenta stock)
- **Merma/Robo**: `adjustment: -3` (reduce stock)

## Nota sobre Concurrencia

Si dos empleados ajustan el stock al mismo tiempo, el sistema:
1. Detecta el conflicto automáticamente (campo `version`)
2. Reintenta hasta 3 veces con los valores actualizados
3. Garantiza que ambos ajustes se apliquen correctamente
