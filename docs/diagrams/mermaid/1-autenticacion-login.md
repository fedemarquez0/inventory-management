# Diagrama de Secuencia: Autenticación de Usuario (Login)

Este diagrama muestra el flujo de autenticación de un usuario en el sistema.

```mermaid
sequenceDiagram
    participant Cliente as Cliente
    participant AuthController as AuthController
    participant AuthService as AuthService
    participant UserRepository as UserRepository
    participant PasswordEncoder as PasswordEncoder
    participant JwtUtil as JwtUtil

    Cliente->>AuthController: POST /api/auth/login<br/>{username, password}
    
    AuthController->>AuthService: authenticate(request)
    
    AuthService->>AuthService: Validar username y password no vacíos
    
    alt Campos vacíos
        AuthService-->>Cliente: 400 - "Username/Password cannot be empty"
    end
    
    AuthService->>UserRepository: Buscar usuario por username
    
    alt Usuario no existe
        UserRepository-->>AuthService: Usuario no encontrado
        AuthService-->>Cliente: 401 - "Invalid username or password"
    end
    
    UserRepository-->>AuthService: Usuario encontrado
    
    AuthService->>AuthService: Verificar si usuario está activo
    
    alt Usuario inactivo
        AuthService-->>Cliente: 403 - "User account is inactive"
    end
    
    AuthService->>PasswordEncoder: Comparar password ingresado con hash guardado
    
    alt Password incorrecto
        PasswordEncoder-->>AuthService: No coincide
        AuthService-->>Cliente: 401 - "Invalid username or password"
    end
    
    PasswordEncoder-->>AuthService: Password correcto
    
    AuthService->>JwtUtil: Generar token JWT para el usuario
    JwtUtil-->>AuthService: Token generado (expira en 24hs)
    
    AuthService->>AuthService: Crear respuesta con token
    AuthService-->>AuthController: AuthResponse {token, type, username}
    
    AuthController-->>Cliente: 200 OK - Token de acceso

```

## Descripción del Flujo

1. **Cliente envía credenciales**: POST a `/api/auth/login` con username y password
2. **Validación básica**: Se verifica que los campos no estén vacíos
3. **Búsqueda de usuario**: Se busca en la base de datos por username
4. **Verificación de estado**: Se confirma que el usuario esté activo
5. **Validación de password**: Se compara el password con bcrypt hash
6. **Generación de token**: Se crea un JWT válido por 24 horas
7. **Respuesta exitosa**: Se devuelve el token al cliente

## Request/Response

**Request:**
```json
{
  "username": "store1",
  "password": "password123"
}
```

**Response exitosa (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "store1"
}
```

## Errores Posibles

| Código | Descripción |
|--------|-------------|
| 400 | Username o password vacío |
| 401 | Credenciales incorrectas |
| 403 | Cuenta de usuario inactiva |
| 500 | Error al generar token |
