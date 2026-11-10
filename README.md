# usuarios-ms (Plazoleta de comidas)

Microservicio de usuarios: cuentas, inicio de sesión y roles. Arquitectura hexagonal.

## Historias de usuario

| HU | Endpoint | Quién puede usarlo |
|---|---|---|
| HU1 - Crear propietario | `POST /api/v1/usuarios/propietario` | ADMINISTRADOR |
| HU5 - Iniciar sesión | `POST /api/v1/usuarios/login` | Cualquiera |
| HU6 - Crear empleado | `POST /api/v1/usuarios/empleado` | PROPIETARIO, y solo para su propio restaurante |
| HU8 - Crear cliente | `POST /api/v1/usuarios/cliente` | Cualquiera |
| Consultar usuario por id (lo usa Plazoleta) | `GET /api/v1/usuarios/{id}` | Usuario autenticado |

## Requisitos
JDK 17+, Maven y MySQL. Para crear empleados (HU6) también debe estar encendido **plazoleta-ms** (puerto 8082),
porque allá viven los restaurantes.

## Cómo correrlo
Contraseñas y claves por variables de entorno (nada de secretos en el repositorio).
`JWT_SECRET` debe ser **la misma** en usuarios-ms y plazoleta-ms (mínimo 32 caracteres).

```bash
DB_PASSWORD=tu_clave JWT_SECRET=la_clave_del_grupo mvn spring-boot:run
```

- Puerto: **8081** · Base de datos: `plazoleta_usuarios` (se crea sola)
- Swagger: http://localhost:8081/swagger-ui.html (botón **Authorize** para pegar el token)
- Dirección de Plazoleta: `http://localhost:8082` por defecto (variable `PLAZOLETA_URL`)

### Administrador inicial
Al arrancar se crea, si no existe, el administrador **admin@plazoleta.com** con clave **Admin123\***
(se cambian con las variables `ADMIN_CORREO` y `ADMIN_CLAVE`). Con él se crean los propietarios.

## El token (HU5)
`POST /api/v1/usuarios/login` devuelve un JWT con: correo (subject), `rol`, `id` y, para los empleados, `idRestaurante`.
Plazoleta lee esos datos para saber quién hace cada petición.

## Ejemplo HU6
Con el token de un propietario:
```json
{
  "nombre": "Luis", "apellido": "Rojas", "documento": "1122334455", "celular": "+573001112233",
  "fechaNacimiento": "1998-03-15", "correo": "luis@correo.com", "clave": "clave123", "idRestaurante": 1
}
```
Respuestas: `201` creado, `400` datos inválidos o repetidos, `403` el restaurante no es suyo o no es propietario,
`404` el restaurante no existe, `503` Plazoleta apagado.

## Pruebas
```bash
mvn test
```
