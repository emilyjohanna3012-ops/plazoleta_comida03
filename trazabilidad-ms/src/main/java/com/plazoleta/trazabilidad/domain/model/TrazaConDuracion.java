package com.plazoleta.trazabilidad.domain.model;

import java.time.LocalDateTime;

/**
 * Un registro del historial tal como lo ve el cliente (HU17).
 * duracionEstadoAnteriorSegundos = cuánto tiempo estuvo el pedido en estadoAnterior (null en el primer registro).
 */
public record TrazaConDuracion(
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        LocalDateTime fecha,
        Long idEmpleado,
        Long duracionEstadoAnteriorSegundos) {
}
