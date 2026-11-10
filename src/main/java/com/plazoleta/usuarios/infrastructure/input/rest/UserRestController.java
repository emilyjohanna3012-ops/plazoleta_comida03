package com.plazoleta.usuarios.infrastructure.input.rest;

import com.plazoleta.usuarios.domain.api.UserServicePort;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.TokenPort;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.EmpleadoRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.LoginRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.LoginResponse;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios")
public class UserRestController {

    private final UserServicePort userServicePort;
    private final TokenPort tokenPort;

    public UserRestController(UserServicePort userServicePort, TokenPort tokenPort) {
        this.userServicePort = userServicePort;
        this.tokenPort = tokenPort;
    }

    // HU1 - REGISTRAR PROPIETARIO (SOLO ADMINISTRADOR)
    @Operation(summary = "HU1 - Crear un usuario con rol PROPIETARIO")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/propietario")
    public ResponseEntity<UserResponse> crearPropietario(@RequestBody UserRequest request) {
        User creado = userServicePort.crearPropietario(toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // HU6 - REGISTRAR EMPLEADO (SOLO PROPIETARIO, Y SOLO PARA SU RESTAURANTE)
    @Operation(summary = "HU6 - Crear un usuario con rol EMPLEADO en un restaurante del propietario")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/empleado")
    public ResponseEntity<UserResponse> crearEmpleado(@RequestBody EmpleadoRequest request,
                                                      @Parameter(hidden = true) Principal propietario) {
        User empleado = new User();
        empleado.setNombre(request.nombre());
        empleado.setApellido(request.apellido());
        empleado.setDocumento(request.documento());
        empleado.setCelular(request.celular());
        empleado.setFechaNacimiento(request.fechaNacimiento());
        empleado.setCorreo(request.correo());
        empleado.setClave(request.clave());
        empleado.setIdRestaurante(request.idRestaurante());

        // El correo del propietario sale del token (lo deja ahí JwtAuthenticationFilter)
        User creado = userServicePort.crearEmpleado(empleado, propietario.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // HU8 - REGISTRAR CLIENTE
    @Operation(summary = "HU8 - Crear un usuario con rol CLIENTE")
    @PostMapping("/cliente")
    public ResponseEntity<UserResponse> crearCliente(@RequestBody UserRequest request) {
        User creado = userServicePort.crearCliente(toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // HU5 - INICIAR SESION CON JWT
    @Operation(summary = "HU5 - Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@RequestBody LoginRequest request) {

        User usuario = userServicePort.iniciarSesion(
                request.correo(),
                request.clave()
        );

        String token = tokenPort.generarToken(usuario);

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        "Bearer",
                        usuario.getRol().name()
                )
        );
    }

    // CONSULTAR USUARIO POR ID (lo usa el microservicio Plazoleta para validar el rol en la HU2)
    @Operation(summary = "Consultar un usuario por id")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userServicePort.obtenerUsuario(id)));
    }

    private User toUser(UserRequest request) {
        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setDocumento(request.documento());
        user.setCelular(request.celular());
        user.setFechaNacimiento(request.fechaNacimiento());
        user.setCorreo(request.correo());
        user.setClave(request.clave());
        return user;
    }

    /** Nunca devolvemos la clave. */
    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getDocumento(),
                u.getCelular(),
                u.getFechaNacimiento(),
                u.getCorreo(),
                u.getRol().name(),
                u.getIdRestaurante()
        );
    }
}
