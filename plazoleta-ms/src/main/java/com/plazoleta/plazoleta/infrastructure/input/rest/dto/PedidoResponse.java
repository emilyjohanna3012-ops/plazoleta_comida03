package com.plazoleta.plazoleta.infrastructure.input.rest.dto;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoResponse(Long id,Long idCliente,Long idRestaurante,Long idEmpleadoAsignado,
 String estado,LocalDateTime fechaCreacion,Long total,List<PlatoResponsePedido> platos) {
 public record PlatoResponsePedido(Long idPlato,Integer cantidad,Integer precioUnitario) {}
}
