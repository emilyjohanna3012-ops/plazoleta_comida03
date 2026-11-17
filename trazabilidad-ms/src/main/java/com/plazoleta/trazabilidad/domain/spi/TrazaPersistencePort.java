package com.plazoleta.trazabilidad.domain.spi;

import com.plazoleta.trazabilidad.domain.model.Traza;

import java.util.List;

/** Puerto de SALIDA: lo que la aplicación necesita para guardar y consultar trazas. */
public interface TrazaPersistencePort {

    Traza guardar(Traza traza);

    /** Trazas de un pedido, de la más antigua a la más reciente. */
    List<Traza> listarPorPedido(Long idPedido);

    /** HU18: todas las trazas de un restaurante, de la más antigua a la más reciente. */
    List<Traza> listarPorRestaurante(Long idRestaurante);
}
