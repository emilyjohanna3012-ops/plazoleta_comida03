package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;

import java.util.Optional;

/** Puerto de SALIDA: lo que la aplicación necesita para guardar y consultar restaurantes. */
public interface RestaurantePersistencePort {
    Restaurante guardar(Restaurante restaurante);

    boolean existePorNit(String nit);

    Pagina<Restaurante> listarOrdenadosPorNombre(int pagina, int tamanio);

    Optional<Restaurante> buscarPorId(Long idRestaurante);
}
