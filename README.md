# usuarios-ms (Plazoleta de comidas)

Microservicio de usuarios — arquitectura hexagonal. HU 1: Crear propietario.

## Cómo correrlo en VS Code
1. Instala: JDK 17+, Maven y MySQL. En VS Code: extensiones "Extension Pack for Java" y "Spring Boot Extension Pack".
2. Abre la carpeta `usuarios-ms` en VS Code.
3. En `src/main/resources/application.yml` cambia `password` por la de tu MySQL (la base se crea sola).
4. Ejecuta `UsuariosApplication` (botón Run) o en terminal: `mvn spring-boot:run`
5. Swagger (documentación OpenAPI): http://localhost:8081/swagger-ui.html
6. Pruebas: `mvn test`

## Probar la HU 1
POST http://localhost:8081/api/v1/usuarios/propietario
{
  "nombre": "Ana", "apellido": "Pérez", "documento": "123456789",
  "celular": "+573005698325", "fechaNacimiento": "1990-05-10",
  "correo": "ana@correo.com", "clave": "miClave123"
}
