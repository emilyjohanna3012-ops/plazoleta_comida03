
package com.plazoleta.plazoleta.infrastructure.input.rest;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoEstadoRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoModificacionRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoResponse;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de ENTRADA: recibe HTTP y llama al caso de uso. */
@RestController
@RequestMapping("/api/v1/platos")
@Tag(name = "Platos")
public class PlatoRestController {

    private final PlatoServicePort platoServicePort;

    public PlatoRestController(PlatoServicePort platoServicePort) {
        this.platoServicePort = platoServicePort;
    }

    @Operation(summary = "HU3 - Crear un plato (solo el propietario del restaurante)")
    @PostMapping
    public ResponseEntity<PlatoResponse> crearPlato(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoRequest request) {

        Plato plato = new Plato();
        plato.setNombre(request.nombre());
        plato.setPrecio(request.precio());
        plato.setDescripcion(request.descripcion());
        plato.setUrlImagen(request.urlImagen());
        plato.setCategoria(request.categoria());
        plato.setIdRestaurante(request.idRestaurante());

        Plato creado = platoServicePort.crearPlato(plato, usuario.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    @Operation(summary = "HU4 - Modificar el precio y la descripción de un plato (solo el propietario del restaurante)")
    @PutMapping("/{id}")
    public ResponseEntity<PlatoResponse> modificarPlato(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoModificacionRequest request) {

        Plato modificado = platoServicePort.modificarPlato(
                id, request.precio(), request.descripcion(), usuario.id());

        return ResponseEntity.ok(toResponse(modificado));
    }

    @Operation(summary = "HU7 - Habilitar o deshabilitar un plato (solo el propietario del restaurante)")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PlatoResponse> cambiarEstadoPlato(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoEstadoRequest request) {

        Plato actualizado = platoServicePort.cambiarEstadoPlato(
                id, request.activo(), usuario.id());

        return ResponseEntity.ok(toResponse(actualizado));
    }

    // HU10: Consultar el menú disponible de un restaurante
    @Operation(summary = "HU10 - Listar platos activos de un restaurante")
    @GetMapping("/restaurante/{idRestaurante}")
    public ResponseEntity<List<PlatoResponse>> listarPlatosPorRestaurante(
            @PathVariable Long idRestaurante) {

        List<PlatoResponse> platos = platoServicePort
                .listarPlatosActivosPorRestaurante(idRestaurante)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(platos);
    }

    private PlatoResponse toResponse(Plato p) {
        return new PlatoResponse(
                p.getId(),
                p.getNombre(),
                p.getPrecio(),
                p.getDescripcion(),
                p.getUrlImagen(),
                p.getCategoria(),
                p.getIdRestaurante(),
                p.getActivo()
        );
    }
}
