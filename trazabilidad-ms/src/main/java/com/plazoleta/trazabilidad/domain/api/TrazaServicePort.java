package com.plazoleta.trazabilidad.domain.api;

import com.plazoleta.trazabilidad.domain.model.Eficiencia;
import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.domain.model.TrazaConDuracion;

import java.util.List;

/** Puerto de ENTRADA: operaciones de trazabilidad (HU17, HU18). */
public interface TrazaServicePort {

    /** HU17 criterio 1: guarda un cambio de estado (lo llama plazoleta-ms en cada cambio). */
    Traza registrar(Traza traza);

    /** HU17 criterio 2: historial de un pedido con la duración de cada estado; un cliente solo ve los suyos. */
    List<TrazaConDuracion> consultarPorPedido(Long idPedido, Long idUsuario, String rol);

    /** HU18: duración de cada pedido entregado y ranking de empleados del restaurante. */
    Eficiencia calcularEficiencia(Long idRestaurante);
}
