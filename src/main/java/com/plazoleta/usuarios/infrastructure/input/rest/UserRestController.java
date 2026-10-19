package com.plazoleta.usuarios.infrastructure.input.rest;

import com.plazoleta.usuarios.domain.api.UserServicePort;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserRequest;
import com.plazoleta.usuarios.infrastructure.input.rest.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Adaptador de ENTRADA: recibe HTTP y llama al caso de uso. */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios")
public class UserRestController {

    private final UserServicePort userServicePort;

    public UserRestController(UserServicePort userServicePort) {
        this.userServicePort = userServicePort;
    }

    @Operation(summary = "HU1 - Crear un usuario con rol PROPIETARIO")
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
        UserResponse response = new UserResponse(creado.getId(), creado.getNombre(), creado.getApellido(),
                creado.getDocumento(), creado.getCelular(), creado.getFechaNacimiento(),
                creado.getCorreo(), creado.getRol().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
        @Operation(summary = "Crear un usuario con rol CLIENTE")
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
}
