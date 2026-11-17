package com.plazoleta.trazabilidad.infrastructure.input.rest;

import com.plazoleta.trazabilidad.domain.api.TrazaServicePort;
import com.plazoleta.trazabilidad.domain.model.Eficiencia;
import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.infrastructure.input.rest.dto.HistorialResponse;
import com.plazoleta.trazabilidad.infrastructure.input.rest.dto.TrazaRequest;
import com.plazoleta.trazabilidad.infrastructure.input.rest.dto.TrazaResponse;
import com.plazoleta.trazabilidad.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de ENTRADA: endpoints de trazabilidad. Normalmente los llama plazoleta-ms, no el navegador. */
@RestController
@RequestMapping("/api/v1/trazas")
@Tag(name = "Trazas")
public class TrazaRestController {

    private final TrazaServicePort trazaServicePort;

    public TrazaRestController(TrazaServicePort trazaServicePort) {
        this.trazaServicePort = trazaServicePort;
    }

    @Operation(summary = "HU17 - Registrar un cambio de estado de un pedido (lo llama plazoleta-ms)")
    @PostMapping
    public ResponseEntity<TrazaResponse> registrar(@RequestBody TrazaRequest request) {
        Traza guardada = trazaServicePort.registrar(new Traza(null, request.idPedido(), request.idCliente(),
                request.idRestaurante(), request.estadoAnterior(), request.estadoNuevo(), request.idEmpleado(),
                request.fecha()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TrazaResponse.desde(guardada));
    }

    @Operation(summary = "HU17 - Historial de estados de un pedido con la duración de cada estado (solo el cliente dueño)")
    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<HistorialResponse>> consultarPorPedido(
            @PathVariable Long idPedido,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(trazaServicePort.consultarPorPedido(idPedido, usuario.id(), usuario.rol()).stream()
                .map(HistorialResponse::desde).toList());
    }

    @Operation(summary = "HU18 - Duración de los pedidos entregados y ranking de empleados de un restaurante (PROPIETARIO)")
    @GetMapping("/eficiencia/restaurante/{idRestaurante}")
    public ResponseEntity<Eficiencia> eficiencia(@PathVariable Long idRestaurante) {
        // Que el propietario sea dueño de ESE restaurante lo verifica plazoleta-ms antes de llamar aquí.
        return ResponseEntity.ok(trazaServicePort.calcularEficiencia(idRestaurante));
    }
}
