# Password Manager | Gestor de Contraseñas Seguro

Una aplicación web moderna y segura para gestionar contraseñas, desarrollada con Spring Boot y React. Este gestor de contraseñas permite a los usuarios almacenar y organizar sus credenciales de forma segura, con características clave como:

✨ **Características Destacadas**:
- Encriptación robusta de contraseñas
- Sistema de categorización flexible
- Autenticación segura con JWT y verificación por email
- Interfaz intuitiva y responsive
- Búsqueda y filtrado instantáneo
- Gestión de categorías personalizada

🛠️ **Stack Tecnológico**:
- Backend: Spring Boot, Spring Security, JPA/Hibernate
- Frontend: React, TypeScript, TailwindCSS
- Base de Datos: PostgreSQL
- Herramientas: Docker, JWT, React Query

🔐 **Seguridad**:
Implementa las mejores prácticas de seguridad, incluyendo autenticación de dos pasos vía email, encriptación de datos sensibles y tokens JWT para sesiones seguras.


# Documentación API Password Manager

## Autenticación y Usuarios

### Registro y Verificación
- `POST /api/auth/register`
  - Registra un nuevo usuario
  - Body: `{ "name": string, "email": string, "password": string }`
  - Retorna: Token JWT y datos del usuario

- `POST /api/auth/verify`
  - Verifica el código enviado por email
  - Body: `{ "email": string, "code": string }`
  - Retorna: Estado de verificación

- `POST /api/auth/resend-code`
  - Reenvía el código de verificación
  - Body: `{ "email": string }`
  - Retorna: Confirmación de envío

### Login
- `POST /api/auth/login`
  - Autentica al usuario
  - Body: `{ "email": string, "password": string }`
  - Retorna: Token JWT y datos del usuario

## Gestión de Contraseñas

### Operaciones CRUD
- `GET /api/passwords`
  - Obtiene todas las contraseñas del usuario
  - Headers: Authorization Bearer Token
  - Retorna: Lista de contraseñas encriptadas

- `POST /api/passwords`
  - Crea una nueva contraseña
  - Headers: Authorization Bearer Token
  - Body: 
    ```json
    {
      "name": string,
      "username": string,
      "password": string,
      "websiteUrl": string (opcional),
      "notes": string (opcional),
      "categoryId": number (opcional)
    }
    ```
  - Retorna: Contraseña creada

- `PUT /api/passwords/{id}`
  - Actualiza una contraseña existente
  - Headers: Authorization Bearer Token
  - Body: Igual que POST pero campos opcionales
  - Retorna: Contraseña actualizada

- `DELETE /api/passwords/{id}`
  - Elimina una contraseña
  - Headers: Authorization Bearer Token
  - Retorna: 204 No Content

### Operaciones Especiales
- `GET /api/passwords/{id}/decrypt`
  - Obtiene la contraseña desencriptada
  - Headers: Authorization Bearer Token
  - Retorna: Contraseña en texto plano
 
## Generador de Contraseñas

### Generación de Contraseñas Seguras
- `POST /api/passwords/generator`
  - Genera una contraseña segura
  - Headers: Authorization Bearer Token
  - Body (opcional):
    ```json
    {
      "length": number,
      "includeUppercase": boolean,
      "includeLowercase": boolean,
      "includeNumbers": boolean,
      "includeSpecialChars": boolean
    }
    ```
  - Retorna:
    ```json
    {
      "password": string,
      "strength": string
    }
    ```

## Gestión de Categorías

### Operaciones CRUD
- `GET /api/categories`
  - Obtiene todas las categorías del usuario
  - Headers: Authorization Bearer Token
  - Retorna: Lista de categorías

- `POST /api/categories`
  - Crea una nueva categoría
  - Headers: Authorization Bearer Token
  - Body: `{ "name": string }`
  - Retorna: Categoría creada

- `PUT /api/categories/{id}`
  - Actualiza una categoría
  - Headers: Authorization Bearer Token
  - Body: `{ "name": string }`
  - Retorna: Categoría actualizada

- `DELETE /api/categories/{id}`
  - Elimina una categoría
  - Headers: Authorization Bearer Token
  - Retorna: 204 No Content

### Gestión de Imagen de Perfil
- `POST /api/profile/image`
  - Actualiza la imagen de perfil
  - Headers: Authorization Bearer Token
  - Content-Type: multipart/form-data
  - Body: FormData con campo "image"
  - Retorna: ID de la foto y mensaje de confirmación

- `GET /api/profile/image/{id}`
  - Obtiene una imagen específica
  - Headers: Authorization Bearer Token
  - Retorna: Imagen en formato JPEG

- `GET /api/profile/image`
  - Obtiene la imagen de perfil actual
  - Headers: Authorization Bearer Token
  - Retorna: Imagen en formato JPEG

- `DELETE /api/profile/image`
  - Elimina la imagen de perfil
  - Headers: Authorization Bearer Token
  - Retorna: Mensaje de confirmación

## Detalles de Implementación

### Seguridad
- Todas las rutas (excepto register/login) requieren autenticación JWT
- Las contraseñas se almacenan encriptadas en la base de datos
- Verificación por email obligatoria para nuevos usuarios
- Validación de pertenencia de recursos al usuario actual

### Paginación y Filtrado
- Los endpoints GET soportan parámetros de query para:
  - Búsqueda: `?search=término`
  - Filtrado por categoría: `?categoryId=1`
  - Paginación: `?page=0&size=10`
  - Ordenamiento: `?sort=name,desc`

### Validaciones
- Campos requeridos en registro:
  - Email válido y único
  - Contraseña mínimo 8 caracteres
  - Nombre no vacío

- Campos requeridos en contraseñas:
  - Nombre no vacío
  - Usuario no vacío
  - Contraseña no vacía

### Respuestas de Error
```json
{
  "message": "Descripción del error",
  "timestamp": "2024-11-05T12:00:00Z",
  "status": 400,
  "path": "/api/passwords"
}
```

### Códigos de Estado
- 200: Éxito
- 201: Recurso creado
- 400: Error de validación
- 401: No autenticado
- 403: No autorizado
- 404: Recurso no encontrado
- 409: Conflicto (ej: email duplicado)
