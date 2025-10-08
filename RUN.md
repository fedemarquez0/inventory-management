# 🚀 Guía de Ejecución - Sistema de Gestión de Inventario

Esta guía te llevará paso a paso para ejecutar el proyecto en tu entorno local en **Windows, Linux o macOS**.

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
- OpenJDK (Recomendado): https://adoptium.net/

**Instalación:**
- **Windows**: Ejecuta el instalador .exe o .msi
- **Linux**: `sudo apt install openjdk-21-jdk` (Ubuntu/Debian) o `sudo yum install java-21-openjdk` (RHEL/CentOS)
- **macOS**: `brew install openjdk@21` o descarga desde el sitio web

### 2. Maven 3.6+

**Nota:** El proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`), por lo que **NO es estrictamente necesario** instalar Maven globalmente. El wrapper descargará automáticamente la versión correcta.

**Si prefieres instalar Maven:**
- Descargar: https://maven.apache.org/download.cgi
- **macOS**: `brew install maven`
- **Linux**: `sudo apt install maven` (Ubuntu/Debian)
- **Windows**: Descarga y configura manualmente

### 3. Git (Opcional)

Solo necesario si vas a clonar el repositorio.

**Descargar:** https://git-scm.com/downloads

---

## ✅ Verificación de Requisitos

### Paso 1: Verificar Java

Abre una terminal y ejecuta:

**Windows (CMD/PowerShell):**
```cmd
java -version
```

**Linux/macOS (Terminal):**
```bash
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

**Windows (CMD):**
```cmd
echo %JAVA_HOME%
```

**Windows (PowerShell):**
```powershell
echo $env:JAVA_HOME
```

**Linux/macOS:**
```bash
echo $JAVA_HOME
```

**Salida esperada:**
- **Windows**: `C:\Program Files\Java\jdk-21`
- **Linux**: `/usr/lib/jvm/java-21-openjdk` o similar
- **macOS**: `/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home` o `/opt/homebrew/opt/openjdk@21`

**Si no está configurado:**

**Windows:**
1. Busca "Variables de entorno" en el menú de Windows
2. Click en "Variables de entorno..."
3. En "Variables del sistema", click "Nueva..."
4. Nombre: `JAVA_HOME`
5. Valor: Ruta a tu JDK (ej: `C:\Program Files\Java\jdk-21`)
6. Click "Aceptar"
7. **Reinicia la terminal**

**Linux/macOS:**
Agrega al archivo `~/.bashrc`, `~/.zshrc` o `~/.bash_profile`:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk  # Ajusta la ruta según tu instalación
export PATH=$JAVA_HOME/bin:$PATH
```
Luego ejecuta: `source ~/.bashrc` (o el archivo que editaste)

---

### Paso 3: Verificar Maven Wrapper

Navega a la carpeta del proyecto y ejecuta:

**Windows:**
```cmd
mvnw -version
```

**Linux/macOS:**
```bash
./mvnw -version
```

**Salida esperada:**
```
Apache Maven 3.9.x
Maven home: ~/.m2/wrapper/...
Java version: 21.0.x
```

Si funciona, ¡estás listo! No necesitas instalar Maven.

---

## 💿 Instalación y Configuración

### Opción A: Clonar desde Git

Si tienes el repositorio en Git:

**Windows:**
```cmd
git clone <repository-url>
cd inventory-management
```

**Linux/macOS:**
```bash
git clone <repository-url>
cd inventory-management
```

### Opción B: Descomprimir ZIP

Si tienes un archivo ZIP del proyecto:

1. Descomprime el archivo en una carpeta de tu elección
2. Abre una terminal y navega a esa carpeta:

**Windows:**
```cmd
cd C:\Users\tuusuario\Documents\Github\inventory-management
```

**Linux/macOS:**
```bash
cd ~/Documents/inventory-management
```

---

## 🔨 Compilación del Proyecto

### Paso 1: Limpiar Compilaciones Previas

**Windows:**
```cmd
mvnw clean
```

**Linux/macOS:**
```bash
./mvnw clean
```

**Qué hace:** Elimina el directorio `target` y limpia compilaciones anteriores.

**Salida esperada:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2.345 s
```

---

### Paso 2: Compilar y Empaquetar

**Windows:**
```cmd
mvnw package
```

**Linux/macOS:**
```bash
./mvnw package
```

**Qué hace:**
- Compila todo el código fuente
- Ejecuta los tests unitarios
- Genera el JAR ejecutable en `target/`

**Salida esperada:**
```
[INFO] --- maven-jar-plugin ---
[INFO] Building jar: .../target/inventory-management-0.0.1-SNAPSHOT.jar
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

**Windows:**
```cmd
mvnw package -DskipTests
```

**Linux/macOS:**
```bash
./mvnw package -DskipTests
```

---

## ▶️ Ejecución de la Aplicación

### Método 1: Usando Maven Spring Boot Plugin (Recomendado para Desarrollo)

**Windows:**
```cmd
mvnw spring-boot:run
```

**Linux/macOS:**
```bash
./mvnw spring-boot:run
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

**Windows:**
```cmd
mvnw clean package
```

**Linux/macOS:**
```bash
./mvnw clean package
```

Luego ejecuta el JAR:

**Windows:**
```cmd
java -jar target\inventory-management-0.0.1-SNAPSHOT.jar
```

**Linux/macOS:**
```bash
java -jar target/inventory-management-0.0.1-SNAPSHOT.jar
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

#### Desde Terminal (con curl):

**Windows (CMD):**
```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"12345\"}"
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"admin","password":"12345"}'
```

**Linux/macOS:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"12345"}'
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

### Paso 4: Probar con Postman (Recomendado)

El proyecto incluye colecciones de Postman listas para usar en `docs/postman/`:

1. **Importar Colecciones:**
   - Abre Postman
   - Click en "Import"
   - Selecciona los archivos de `docs/postman/collections/`:
     - `Auth.postman_collection.json`
     - `Inventory.postman_collection.json`
     - `Documentation.postman_collection.json`

2. **Importar Environment:**
   - Importa el archivo `docs/postman/environments/dev.postman_environment.json`
   - Selecciona el environment "dev" en Postman

3. **Ejecutar Requests:**
   - Todas las colecciones están preconfiguradas
   - Comienza con la colección "Auth" para obtener un token
   - El token se guardará automáticamente en las variables de entorno

Ver más detalles en la [Documentación Técnica](docs/DOCUMENTATION.md#pruebas-con-postman).

---

### Paso 5: Verificar la Base de Datos

Después de iniciar la aplicación, deberías ver un archivo:

```
inventory-db.mv.db
```

En la raíz del proyecto. Este es el archivo de la base de datos H2.

---

### Paso 6: Revisar los Logs

Los logs se guardan en:

**Windows:**
```
logs\inventory-management.log
```

**Linux/macOS:**
```
logs/inventory-management.log
```

Abre el archivo para ver todos los logs de la aplicación.

---

## ⏹️ Detener la Aplicación

### Si ejecutaste con `mvnw spring-boot:run` o `java -jar`:

Presiona `Ctrl + C` en la terminal donde está corriendo.

**Windows - Confirmación:**
```
Terminate batch job (Y/N)? Y
```

Escribe `Y` y presiona Enter.

**Linux/macOS:**
La aplicación se detendrá inmediatamente.

---

## 🧪 Ejecución de Tests

### Ejecutar Todos los Tests

**Windows:**
```cmd
mvnw test
```

**Linux/macOS:**
```bash
./mvnw test
```

**Salida esperada:**
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Ejecutar Tests con Reporte de Cobertura

**Windows:**
```cmd
mvnw clean verify
```

**Linux/macOS:**
```bash
./mvnw clean verify
```

Los reportes se generan en: `target/site/jacoco/index.html`

---

## 🔧 Troubleshooting

### Error: "JAVA_HOME is not set"

**Solución:** Configura la variable de entorno JAVA_HOME (ver Paso 2 de Verificación)

---

### Error: "Port 8080 is already in use"

**Solución:** 

**Opción 1 - Cambiar el puerto:**
Edita `src/main/resources/application.yml` y cambia:
```yaml
server:
  port: 8081  # O cualquier otro puerto disponible
```

**Opción 2 - Detener el proceso que usa el puerto:**

**Windows:**
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Linux/macOS:**
```bash
lsof -i :8080
kill -9 <PID>
```

---

### Error: "mvnw: Permission denied" (Linux/macOS)

**Solución:**
```bash
chmod +x mvnw
./mvnw -version
```

---

### Error: "Cannot find or load main class"

**Solución:**
1. Limpia y recompila:
   ```bash
   ./mvnw clean package
   ```
2. Verifica que JAVA_HOME apunte al JDK (no JRE)

---

### Base de Datos Corrupta

**Solución:**
Elimina el archivo de base de datos y reinicia:

**Windows:**
```cmd
del inventory-db.mv.db
mvnw spring-boot:run
```

**Linux/macOS:**
```bash
rm inventory-db.mv.db
./mvnw spring-boot:run
```

---

## 📚 Documentación Adicional

- **[README.md](README.md)**: Descripción general del proyecto
- **[docs/DOCUMENTATION.md](docs/DOCUMENTATION.md)**: Documentación técnica completa de la API
- **[docs/postman/](docs/postman/)**: Colecciones de Postman para pruebas
- **[docs/diagrams/](docs/diagrams/)**: Diagramas de arquitectura y flujos

---

## 🎉 ¡Listo!

Si has llegado hasta aquí y todo funciona, ¡felicitaciones! 🎊

La aplicación está corriendo en: **http://localhost:8080**

**Próximos pasos:**
1. Explora la API con Swagger UI
2. Prueba los endpoints con Postman
3. Revisa la documentación técnica
4. Experimenta con diferentes usuarios y permisos

---