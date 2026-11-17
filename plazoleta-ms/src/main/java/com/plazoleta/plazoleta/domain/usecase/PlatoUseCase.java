
package com.plazoleta.plazoleta.domain.usecase;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.exception.AccesoDenegadoException;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestauranteConsultaPort;

import java.util.List;

/** Caso de uso: reglas de las HU3, HU4, HU7 y HU10. */
public class PlatoUseCase implements PlatoServicePort {

    private final PlatoPersistencePort platoPersistencePort;
    private final RestauranteConsultaPort restauranteConsultaPort;

    public PlatoUseCase(PlatoPersistencePort platoPersistencePort, RestauranteConsultaPort restauranteConsultaPort) {
        this.platoPersistencePort = platoPersistencePort;
        this.restauranteConsultaPort = restauranteConsultaPort;
    }

    @Override
    public Plato crearPlato(Plato plato, Long idPropietario) {
        validarCamposObligatorios(plato);

        verificarQueEsPropietario(plato.getIdRestaurante(), idPropietario,
                "Solo el propietario del restaurante puede crear platos en él");

        plato.setActivo(true);
        return platoPersistencePort.guardar(plato);
    }

    @Override
    public Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario) {
        validarPrecio(precio);
        validarDescripcion(descripcion);

        Plato plato = platoPersistencePort.buscarPorId(idPlato)
                .orElseThrow(() -> new RecursoNoEncontradoException("El plato no existe"));

        verificarQueEsPropietario(plato.getIdRestaurante(), idPropietario,
                "Solo el propietario del restaurante puede modificar sus platos");

        plato.setPrecio(precio);
        plato.setDescripcion(descripcion);
        return platoPersistencePort.guardar(plato);
    }

    @Override
    public Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario) {
        if (activo == null) {
            throw new DomainException("El estado del plato (activo) es obligatorio");
        }

        Plato plato = platoPersistencePort.buscarPorId(idPlato)
                .orElseThrow(() -> new RecursoNoEncontradoException("El plato no existe"));

        verificarQueEsPropietario(plato.getIdRestaurante(), idPropietario,
                "Solo el propietario del restaurante puede habilitar o deshabilitar sus platos");

        plato.setActivo(activo);
        return platoPersistencePort.guardar(plato);
    }

    // HU10: Consultar los platos activos de un restaurante
    @Override
    public List<Plato> listarPlatosActivosPorRestaurante(Long idRestaurante) {
        if (idRestaurante == null || idRestaurante <= 0) {
            throw new DomainException("El restaurante es obligatorio");
        }

        return platoPersistencePort.listarPlatosActivosPorRestaurante(idRestaurante);
    }

    private void verificarQueEsPropietario(Long idRestaurante, Long idPropietario, String mensajeSiNoLoEs) {
        Long idDuenioDelRestaurante = restauranteConsultaPort.obtenerIdPropietario(idRestaurante)
                .orElseThrow(() -> new RecursoNoEncontradoException("El restaurante no existe"));

        if (!idDuenioDelRestaurante.equals(idPropietario)) {
            throw new AccesoDenegadoException(mensajeSiNoLoEs);
        }
    }

    private void validarCamposObligatorios(Plato p) {
        if (esVacio(p.getNombre())) {
            throw new DomainException("El nombre es obligatorio");
        }
        validarPrecio(p.getPrecio());
        validarDescripcion(p.getDescripcion());
        if (esVacio(p.getUrlImagen())) {
            throw new DomainException("La URL de la imagen es obligatoria");
        }
        if (esVacio(p.getCategoria())) {
            throw new DomainException("La categoría es obligatoria");
        }
        if (p.getIdRestaurante() == null) {
            throw new DomainException("El restaurante es obligatorio");
        }
    }

    private void validarPrecio(Integer precio) {
        if (precio == null || precio <= 0) {
            throw new DomainException("El precio debe ser un número entero mayor a 0");
        }
    }

    private void validarDescripcion(String descripcion) {
        if (esVacio(descripcion)) {
            throw new DomainException("La descripción es obligatoria");
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
