package com.plazoleta.plazoleta.infrastructure.input.rest;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PaginaResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResumenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de ENTRADA: recibe HTTP y llama al caso de uso. */
@RestController
@RequestMapping("/api/v1/restaurantes")
@Tag(name = "Restaurantes")
public class RestauranteRestController {

    private final RestauranteServicePort restauranteServicePort;

    public RestauranteRestController(RestauranteServicePort restauranteServicePort) {
        this.restauranteServicePort = restauranteServicePort;
    }

    // Solo el ADMINISTRADOR (ver SecurityConfig).
    @Operation(summary = "HU2 - Crear un restaurante (administrador)")
    @PostMapping
    public ResponseEntity<RestauranteResponse> crearRestaurante(@RequestBody RestauranteRequest request) {
        Restaurante restaurante = new Restaurante();
        restaurante.setNombre(request.nombre());
        restaurante.setNit(request.nit());
        restaurante.setDireccion(request.direccion());
        restaurante.setTelefono(request.telefono());
        restaurante.setUrlLogo(request.urlLogo());
        restaurante.setIdPropietario(request.idPropietario());

        Restaurante creado = restauranteServicePort.crearRestaurante(restaurante);

        RestauranteResponse response = new RestauranteResponse(creado.getId(), creado.getNombre(), creado.getNit(),
                creado.getDireccion(), creado.getTelefono(), creado.getUrlLogo(), creado.getIdPropietario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "HU9 - Listar restaurantes en orden alfabético, paginados (solo nombre y logo)")
    @GetMapping
    public ResponseEntity<PaginaResponse<RestauranteResumenResponse>> listarRestaurantes(
            @Parameter(description = "Número de página, empieza en 0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Cantidad de restaurantes por página (1 a 50)") @RequestParam(defaultValue = "10") int tamanio) {

        Pagina<Restaurante> resultado = restauranteServicePort.listarRestaurantes(pagina, tamanio);

        List<RestauranteResumenResponse> contenido = resultado.contenido().stream()
               .map(r -> new RestauranteResumenResponse(r.getId(), r.getNombre(), r.getUrlLogo()))
                .toList();
        return ResponseEntity.ok(new PaginaResponse<>(contenido, resultado.pagina(), resultado.tamanio(),
                resultado.totalElementos(), resultado.totalPaginas()));
    }

    @Operation(summary = "Consultar un restaurante por id (lo usa usuarios-ms al crear empleados, HU6)")
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponse> obtenerRestaurante(@PathVariable Long id) {
        Restaurante r = restauranteServicePort.obtenerRestaurante(id);
        return ResponseEntity.ok(new RestauranteResponse(r.getId(), r.getNombre(), r.getNit(),
                r.getDireccion(), r.getTelefono(), r.getUrlLogo(), r.getIdPropietario()));
    }
}
