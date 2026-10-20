
package com.plazoleta.usuarios.infrastructure.input.rest;

import com.plazoleta.usuarios.domain.api.UserServicePort;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.TokenPort;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.LoginRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.LoginResponse;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setDocumento(request.documento());
        user.setCelular(request.celular());
        user.setFechaNacimiento(request.fechaNacimiento());
        user.setCorreo(request.correo());
        user.setClave(request.clave());

        User creado = userServicePort.crearPropietario(user);

        UserResponse response = new UserResponse(
                creado.getId(),
                creado.getNombre(),
                creado.getApellido(),
                creado.getDocumento(),
                creado.getCelular(),
                creado.getFechaNacimiento(),
                creado.getCorreo(),
                creado.getRol().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HU8 - REGISTRAR CLIENTE
    @Operation(summary = "HU8 - Crear un usuario con rol CLIENTE")
    @PostMapping("/cliente")
    public ResponseEntity<UserResponse> crearCliente(@RequestBody UserRequest request) {

        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setDocumento(request.documento());
        user.setCelular(request.celular());
        user.setFechaNacimiento(request.fechaNacimiento());
        user.setCorreo(request.correo());
        user.setClave(request.clave());

        User creado = userServicePort.crearCliente(user);

        UserResponse response = new UserResponse(
                creado.getId(),
                creado.getNombre(),
                creado.getApellido(),
                creado.getDocumento(),
                creado.getCelular(),
                creado.getFechaNacimiento(),
                creado.getCorreo(),
                creado.getRol().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
}
