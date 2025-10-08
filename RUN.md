# 🚀 Guía de Ejecución - Sistema de Gestión de Inventario

Esta guía te llevará paso a paso para ejecutar el proyecto en tu entorno local.

## 📋 Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Verificación de Requisitos](#verificación-de-requisitos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Compilación del Proyecto](#compilación-del-proyecto)
- [Ejecución de la Aplicación](#ejecución-de-la-aplicación)
- [Verificación de la Instalación](#verificación-de-la-instalación)
- [Detener la Aplicación](#detener-la-aplicación)
- [Ejecución de Tests](#ejecución-de-tests)
- [Troubleshooting](#troubleshooting)

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

### 1. Java Development Kit (JDK) 21

**Versión requerida:** Java 21 o superior

**Descargar:**
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- OpenJDK: https://adoptium.net/

### 2. Maven 3.6+

**Nota:** El proyecto incluye Maven Wrapper (`mvnw`), por lo que **NO es estrictamente necesario** instalar Maven globalmente. El wrapper descargará automáticamente la versión correcta.

**Si prefieres instalar Maven:**
- Descargar: https://maven.apache.org/download.cgi

### 3. Git (Opcional)

Solo necesario si vas a clonar el repositorio.

**Descargar:** https://git-scm.com/downloads

---

## ✅ Verificación de Requisitos

### Paso 1: Verificar Java

Abre una terminal (CMD en Windows) y ejecuta:

```cmd
java -version
```

**Salida esperada:**
```
java version "21.0.x" ...
Java(TM) SE Runtime Environment ...
```

Si no está instalado o la versión es menor a 21, instala JDK 21.

---

### Paso 2: Verificar JAVA_HOME (Importante)

```cmd
echo %JAVA_HOME%
```

**Salida esperada:**
```
C:\Program Files\Java\jdk-21
```

**Si no está configurado:**

1. Busca "Variables de entorno" en el menú de Windows
2. Click en "Variables de entorno..."
3. En "Variables del sistema", click "Nueva..."
4. Nombre: `JAVA_HOME`
5. Valor: Ruta a tu JDK (ej: `C:\Program Files\Java\jdk-21`)
6. Click "Aceptar"
7. **Reinicia la terminal**

---

### Paso 3: Verificar Maven Wrapper

Navega a la carpeta del proyecto y ejecuta:

```cmd
mvnw -version
```

**Salida esperada:**
```
Apache Maven 3.9.x
Maven home: C:\Users\...\m2\wrapper\...
Java version: 21.0.x
```

Si funciona, ¡estás listo! No necesitas instalar Maven.

---

## 💿 Instalación y Configuración

### Opción A: Clonar desde Git

Si tienes el repositorio en Git:

```cmd
git clone <repository-url>
cd inventory-management
```

### Opción B: Descomprimir ZIP

Si tienes un archivo ZIP del proyecto:

1. Descomprime el archivo en una carpeta de tu elección
2. Abre CMD y navega a esa carpeta:

```cmd
cd C:\Users\tuusuario\Documents\Github\inventory-management
```

---

## 🔨 Compilación del Proyecto

### Paso 1: Limpiar Compilaciones Previas

```cmd
mvnw clean
```

**Qué hace:** Elimina el directorio `target` y limpia compilaciones anteriores.

**Salida esperada:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2.345 s
```

---

### Paso 2: Compilar y Empaquetar

```cmd
mvnw package
```

**Qué hace:**
- Compila todo el código fuente
- Ejecuta los tests unitarios
- Genera el JAR ejecutable en `target/`

**Salida esperada:**
```
[INFO] --- maven-jar-plugin ---
[INFO] Building jar: ...\target\inventory-management-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

**Tiempo estimado:** 30-60 segundos (primera vez puede ser más por descarga de dependencias)

---

### Paso 3: Compilar SIN Tests (Opcional)

Si quieres compilar más rápido sin ejecutar tests:

```cmd
mvnw package -DskipTests
```

---

## ▶️ Ejecución de la Aplicación

### Método 1: Usando Maven Spring Boot Plugin (Recomendado para Desarrollo)

```cmd
mvnw spring-boot:run
```

**Ventajas:**
- Más rápido para desarrollo
- Hot reload con DevTools
- No necesita compilar JAR primero

**Salida esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.5.6)

2025-10-07 10:00:00 [main] INFO  InventoryManagementApplication - Starting InventoryManagementApplication...
2025-10-07 10:00:02 [main] INFO  InventoryManagementApplication - Started InventoryManagementApplication in 2.345 seconds
2025-10-07 10:00:02 [main] INFO  Netty started on port 8080
```

**¡La aplicación está corriendo!** 🎉

---

### Método 2: Ejecutando el JAR Directamente

Primero compila el proyecto:

```cmd
mvnw clean package
```

Luego ejecuta el JAR:

```cmd
java -jar target\inventory-management-0.0.1-SNAPSHOT.jar
```

**Ventajas:**
- Simula entorno de producción
- Más rápido una vez compilado

---

## ✔️ Verificación de la Instalación

### Paso 1: Verificar que el Servidor Está Corriendo

En tu navegador, abre:

```
http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

Si ves esto, ¡el servidor está funcionando correctamente! ✅

---

### Paso 2: Acceder a Swagger UI

Abre en tu navegador:

```
http://localhost:8080/swagger-ui.html
```

**Qué verás:**
- Interfaz de documentación de Swagger
- Lista de todos los endpoints disponibles
- Opción para probar la API directamente

---

### Paso 3: Probar el Endpoint de Login

#### Desde Swagger UI:

1. Busca el endpoint `POST /api/auth/login`
2. Click en "Try it out"
3. Ingresa en el body:
   ```json
   {
     "username": "admin",
     "password": "12345"
   }
   ```
4. Click "Execute"
5. Deberías recibir un token JWT ✅

#### Desde CMD (con curl):

Si tienes `curl` instalado:

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"12345\"}"
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Paso 4: Verificar la Base de Datos

Después de iniciar la aplicación, deberías ver un archivo:

```
inventory-db.mv.db
```

En la raíz del proyecto. Este es el archivo de la base de datos H2.

---

### Paso 5: Revisar los Logs

Los logs se guardan en:

```
logs\inventory-management.log
```

Abre el archivo para ver todos los logs de la aplicación.

---

## ⏹️ Detener la Aplicación

### Si ejecutaste con `mvnw spring-boot:run`:

Presiona `Ctrl + C` en la terminal donde está corriendo.

**Confirmación:**
```
Terminate batch job (Y/N)? Y
```

Escribe `Y` y presiona Enter.

---

### Si ejecutaste con `java -jar`:

Presiona `Ctrl + C` en la terminal.

---

## 🧪 Ejecución de Tests

### Ejecutar Todos los Tests

```cmd
mvnw test
```

**Qué hace:**
- Ejecuta todos los tests unitarios e integración
- Genera reportes en `target/surefire-reports/`

**Salida esperada:**
```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Ejecutar un Test Específico

```cmd
mvnw test -Dtest=InventoryServiceTest
```

---

### Ver Reporte de Tests

Después de ejecutar los tests, abre:

```
target\surefire-reports\TEST-com.meli.inventorymanagement.service.InventoryServiceTest.xml
```

---

## 🛠️ Troubleshooting

### Error: "JAVA_HOME is not set"

**Solución:**
1. Configura la variable de entorno `JAVA_HOME`
2. Reinicia la terminal
3. Verifica con `echo %JAVA_HOME%`

---

### Error: "Port 8080 is already in use"

**Causa:** Otro proceso está usando el puerto 8080.

**Solución 1 - Cambiar el Puerto:**

Edita `src/main/resources/application.yml`:

```yaml
server:
  port: 8081  # Cambiar a otro puerto
```

**Solución 2 - Liberar el Puerto:**

1. Encuentra el proceso que usa el puerto:
   ```cmd
   netstat -ano | findstr :8080
   ```

2. Anota el PID (última columna)

3. Detén el proceso:
   ```cmd
   taskkill /PID <numero-pid> /F
   ```

---

### Error: "Cannot resolve dependencies"

**Causa:** Problema de conexión a internet o repositorio Maven.

**Solución:**

1. Verifica tu conexión a internet
2. Limpia el repositorio local:
   ```cmd
   mvnw clean
   ```
3. Fuerza actualización:
   ```cmd
   mvnw clean install -U
   ```

---

### Error: Tests Fallan

**Solución:** Compila sin tests por ahora:

```cmd
mvnw package -DskipTests
```

Luego ejecuta la aplicación y reporta qué tests fallaron.

---

### Error: "Failed to load ApplicationContext"

**Causa:** Problema en la configuración de Spring.

**Solución:**

1. Verifica que `application.yml` esté correcto
2. Revisa los logs en `logs/inventory-management.log`
3. Asegúrate de que el JDK sea versión 21

---

### Base de Datos No Se Crea

**Solución:**

1. Verifica permisos de escritura en la carpeta del proyecto
2. Borra el archivo `inventory-db.mv.db` si existe
3. Reinicia la aplicación

---

### Swagger UI No Carga

**Solución:**

1. Verifica que la aplicación esté corriendo
2. Asegúrate de usar: `http://localhost:8080/swagger-ui.html` (no `/swagger-ui/`)
3. Limpia caché del navegador

---

## 📚 Comandos Útiles - Resumen

| Comando | Descripción |
|---------|-------------|
| `mvnw clean` | Limpia compilaciones previas |
| `mvnw compile` | Solo compila el código |
| `mvnw package` | Compila y genera JAR |
| `mvnw package -DskipTests` | Compila sin ejecutar tests |
| `mvnw test` | Ejecuta los tests |
| `mvnw spring-boot:run` | Ejecuta la aplicación |
| `java -jar target\inventory-management-0.0.1-SNAPSHOT.jar` | Ejecuta el JAR |

---

## 🎯 Flujo Típico de Desarrollo

### Primera Vez

```cmd
# 1. Navegar al proyecto
cd C:\Users\fedem\Documents\Github\inventory-management

# 2. Compilar
mvnw clean package

# 3. Ejecutar
mvnw spring-boot:run

# 4. Abrir navegador
start http://localhost:8080/swagger-ui.html
```

### Desarrollo Continuo

```cmd
# Simplemente ejecutar (DevTools recarga automáticamente)
mvnw spring-boot:run
```

---

## 🌐 URLs Importantes

Una vez que la aplicación esté corriendo:

| URL | Descripción |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Documentación Swagger (Interfaz) |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |
| http://localhost:8080/actuator/health | Health Check |
| http://localhost:8080/api/auth/login | Endpoint de Login |

---

## 📖 Próximos Pasos

Ahora que tienes la aplicación corriendo:

1. **Lee la documentación:** [docs/DOCUMENTATION.md](docs/DOCUMENTATION.md)
2. **Prueba la API:** Usa Swagger UI o Postman
3. **Explora el código:** Revisa la estructura en [README.md](README.md)

---

## 💡 Consejos

### Desarrollo Activo

Si estás desarrollando, usa:
```cmd
mvnw spring-boot:run
```

Esto habilita **DevTools** que recarga automáticamente cuando guardas cambios.

---

### Producción / Testing

Si quieres simular producción, usa:
```cmd
mvnw clean package
java -jar target\inventory-management-0.0.1-SNAPSHOT.jar
```

---

### Modo Debug

Para ejecutar en modo debug:
```cmd
mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

Luego conecta tu IDE al puerto 5005.

---

## 🆘 ¿Necesitas Ayuda?

Si tienes problemas:

1. **Revisa los logs:** `logs/inventory-management.log`
2. **Consulta la documentación:** [docs/DOCUMENTATION.md](docs/DOCUMENTATION.md)
3. **Verifica requisitos:** Java 21, JAVA_HOME configurado
4. **Limpia y recompila:**
   ```cmd
   mvnw clean package
   ```

---

## ✅ Checklist de Verificación

Antes de reportar un problema, verifica:

- [ ] Java 21 instalado y verificado
- [ ] JAVA_HOME configurado correctamente
- [ ] Proyecto compilado sin errores
- [ ] Puerto 8080 disponible
- [ ] http://localhost:8080/actuator/health retorna "UP"
- [ ] Swagger UI carga correctamente
- [ ] Endpoint de login funciona

Si todos estos puntos están ✅, ¡tu instalación es exitosa!

---

## 🎉 ¡Listo!

Tu Sistema de Gestión de Inventario está corriendo. 

**Disfruta explorando la aplicación y su arquitectura reactiva!** 🚀

