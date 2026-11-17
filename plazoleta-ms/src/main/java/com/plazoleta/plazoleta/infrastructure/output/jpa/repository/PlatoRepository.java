
package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatoRepository
        extends JpaRepository<PlatoEntity, Long> {

    // HU10: Buscar platos disponibles de un restaurante
    List<PlatoEntity> findByIdRestauranteAndActivoTrue(
            Long idRestaurante
    );
}
