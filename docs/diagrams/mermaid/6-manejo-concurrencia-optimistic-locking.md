# Diagrama de Secuencia: Manejo de Concurrencia (Optimistic Locking)

Este diagrama muestra cómo el sistema maneja cuando dos empleados intentan modificar el mismo inventario al mismo tiempo.

```mermaid
sequenceDiagram
    participant Emp1 as Empleado 1
    participant Emp2 as Empleado 2
    participant Service as InventoryService
    participant Database as Base de Datos

    Note over Database: Estado inicial: Stock = 50, Version = 10

    par Dos empleados trabajan simultáneamente
        Emp1->>Service: Ajustar stock: -5 unidades
    and
        Emp2->>Service: Ajustar stock: +10 unidades
    end

    Note over Service: Ambos leen el mismo estado inicial

    par Lectura concurrente
        Service->>Database: Emp1 lee inventario
        Database-->>Service: Stock = 50, Version = 10
    and
        Service->>Database: Emp2 lee inventario
        Database-->>Service: Stock = 50, Version = 10
    end

    Note over Service: Emp1 calcula: 50 - 5 = 45<br/>Emp2 calcula: 50 + 10 = 60

    Service->>Database: Emp1 intenta guardar: Stock = 45, Version = 10
    Note over Database: ✓ Primera actualización exitosa<br/>Stock = 45, Version = 11
    Database-->>Emp1: 200 OK - Stock actualizado a 45

    Service->>Database: Emp2 intenta guardar: Stock = 60, Version = 10
    Note over Database: ✗ CONFLICTO: Version actual es 11, no 10
    Database-->>Service: Error de concurrencia

    rect rgb(255, 240, 240)
        Note over Service: REINTENTO AUTOMÁTICO
        
        Service->>Service: Esperar 100ms
        Service->>Database: Emp2 lee inventario actualizado
        Database-->>Service: Stock = 45, Version = 11
        
        Note over Service: Emp2 recalcula: 45 + 10 = 55
        
        Service->>Database: Emp2 guarda: Stock = 55, Version = 11
        Note over Database: ✓ Actualización exitosa<br/>Stock = 55, Version = 12
        Database-->>Emp2: 200 OK - Stock actualizado a 55
    end

    Note over Database: Estado final: Stock = 55, Version = 12<br/>✓ Ambos ajustes aplicados: 50 - 5 + 10 = 55

```

## Descripción del Flujo

1. **Dos empleados simultáneamente**: Uno vende 5 unidades, otro recibe 10 unidades
2. **Lectura inicial**: Ambos leen el mismo estado (Stock = 50)
3. **Primera escritura exitosa**: Empleado 1 actualiza a 45 (Version pasa a 11)
4. **Detección de conflicto**: Empleado 2 intenta guardar pero la versión ya cambió
5. **Reintento automático**: El sistema reintenta con los valores actualizados
6. **Segunda escritura exitosa**: Empleado 2 actualiza correctamente a 55
7. **Resultado correcto**: Ambas operaciones se aplican sin perder ninguna

## Cómo Funciona el Control de Versiones

### Campo Version en la Base de Datos
```java
@Entity
public class Inventory {
    private Long id;
    private Integer availableQty;
    
    @Version  // Este campo controla la concurrencia
    private Integer version;
}
```

### Query de Actualización
```sql
UPDATE inventory 
SET available_qty = ?, 
    version = version + 1
WHERE id = ? 
  AND version = ?  -- Solo actualiza si la versión coincide
```

### Lógica de Detección
- Si la versión coincide → Actualización exitosa
- Si la versión NO coincide → Otra persona modificó primero → Reintentar

## Configuración de Reintentos

- **Número de reintentos**: 3 intentos máximo
- **Tiempo de espera**: 100ms, luego 200ms, luego 400ms (backoff exponencial)
- **Qué hacer si fallan todos**: Devolver error 409 Conflict

## Ventajas de este Enfoque

✅ **No bloquea la base de datos**: Múltiples empleados pueden leer simultáneamente  
✅ **Detección automática de conflictos**: Usa el campo `version` como control  
✅ **Resolución automática**: La mayoría de conflictos se resuelven con reintentos  
✅ **Sin pérdida de datos**: Ambas operaciones siempre se aplican correctamente  
✅ **Escalable**: Funciona con múltiples servidores y sucursales  

## Casos de Uso Reales

- **Caja simultánea**: Dos cajeros venden el mismo producto al mismo tiempo
- **Venta + Recepción**: Un empleado vende mientras otro recibe mercadería
- **Múltiples tiendas**: El sistema puede tener varias instancias corriendo en paralelo
