
package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de SALIDA: implementa el puerto usando JPA/MySQL. */
public class PlatoJpaAdapter implements PlatoPersistencePort {

    private final PlatoRepository repository;

    public PlatoJpaAdapter(PlatoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Plato guardar(Plato plato) {
        return toDomain(repository.save(toEntity(plato)));
    }

    @Override
    public Optional<Plato> buscarPorId(Long idPlato) {
        return repository.findById(idPlato).map(this::toDomain);
    }

    // HU10: Consultar platos activos de un restaurante
    @Override
    public List<Plato> listarPlatosActivosPorRestaurante(Long idRestaurante) {
        return repository.findByIdRestauranteAndActivoTrue(idRestaurante)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PlatoEntity toEntity(Plato p) {
        PlatoEntity e = new PlatoEntity();
        e.setId(p.getId());
        e.setNombre(p.getNombre());
        e.setPrecio(p.getPrecio());
        e.setDescripcion(p.getDescripcion());
        e.setUrlImagen(p.getUrlImagen());
        e.setCategoria(p.getCategoria());
        e.setIdRestaurante(p.getIdRestaurante());
        e.setActivo(p.getActivo());
        return e;
    }

    private Plato toDomain(PlatoEntity e) {
        return new Plato(e.getId(), e.getNombre(), e.getPrecio(), e.getDescripcion(), e.getUrlImagen(),
                e.getCategoria(), e.getIdRestaurante(), e.getActivo());
    }
}
