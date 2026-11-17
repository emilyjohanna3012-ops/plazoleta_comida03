package com.plazoleta.plazoleta.infrastructure.input.rest.dto;
import java.util.List;
public record CrearPedidoRequest(Long idRestaurante, List<PlatoPedidoRequest> platos) {
 public record PlatoPedidoRequest(Long idPlato,Integer cantidad) {}
}
