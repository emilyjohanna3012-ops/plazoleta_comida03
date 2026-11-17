package com.plazoleta.trazabilidad.infrastructure.input.rest.dto;

import com.plazoleta.trazabilidad.domain.model.EstadoPedido;

import java.time.LocalDateTime;

/** Cuerpo de POST /api/v1/trazas (lo envía plazoleta-ms; contrato, sección 3). */
public record TrazaRequest(
        Long idPedido,
        Long idCliente,
        Long idRestaurante,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        Long idEmpleado,
        LocalDateTime fecha) {
}
