package com.plazoleta.trazabilidad.domain.model;

import java.time.LocalDateTime;

/**
 * Un cambio de estado de un pedido (HU17, criterio 1: "se debe crear un registro de cada cambio de estado").
 * estadoAnterior es null cuando el pedido se acaba de crear; idEmpleado es null si el cambio lo hizo el cliente.
 */
public record Traza(
        String id,
        Long idPedido,
        Long idCliente,
        Long idRestaurante,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        Long idEmpleado,
        LocalDateTime fecha) {
}
