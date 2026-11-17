package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestauranteConsultaPort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * Adaptador de SALIDA: implementa los puertos de restaurantes usando JPA/MySQL.
 * También responde a los platos quién es el dueño de un restaurante (RestauranteConsultaPort).
 */
public class RestauranteJpaAdapter implements RestaurantePersistencePort, RestauranteConsultaPort {

    private final RestauranteRepository repository;

    public RestauranteJpaAdapter(RestauranteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Restaurante guardar(Restaurante restaurante) {
        return toDomain(repository.save(toEntity(restaurante)));
    }

    @Override
    public boolean existePorNit(String nit) {
        return repository.existsByNit(nit);
    }

    @Override
    public Pagina<Restaurante> listarOrdenadosPorNombre(int pagina, int tamanio) {
        Page<RestauranteEntity> page = repository.findAll(
                PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending()));
        return new Pagina<>(page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public Optional<Restaurante> buscarPorId(Long idRestaurante) {
        return repository.findById(idRestaurante).map(this::toDomain);
    }

    @Override
    public Optional<Long> obtenerIdPropietario(Long idRestaurante) {
        return repository.findById(idRestaurante).map(RestauranteEntity::getIdPropietario);
    }

    private RestauranteEntity toEntity(Restaurante r) {
        RestauranteEntity e = new RestauranteEntity();
        e.setId(r.getId());
        e.setNombre(r.getNombre());
        e.setNit(r.getNit());
        e.setDireccion(r.getDireccion());
        e.setTelefono(r.getTelefono());
        e.setUrlLogo(r.getUrlLogo());
        e.setIdPropietario(r.getIdPropietario());
        return e;
    }

    private Restaurante toDomain(RestauranteEntity e) {
        return new Restaurante(e.getId(), e.getNombre(), e.getNit(), e.getDireccion(), e.getTelefono(),
                e.getUrlLogo(), e.getIdPropietario());
    }
}
