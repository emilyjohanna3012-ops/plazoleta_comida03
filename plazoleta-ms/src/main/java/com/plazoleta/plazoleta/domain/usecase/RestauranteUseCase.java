package com.plazoleta.plazoleta.domain.usecase;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioConsultaPort;

import java.util.regex.Pattern;

/** Caso de uso: aquí viven las reglas de las HU 2 y 9. */
public class RestauranteUseCase implements RestauranteServicePort {

    private static final String ROL_PROPIETARIO = "PROPIETARIO";
    private static final int TAMANIO_MAXIMO_PAGINA = 50;
    private static final Pattern SOLO_NUMEROS = Pattern.compile("^\\d+$");
    private static final Pattern TELEFONO = Pattern.compile("^\\+?\\d+$"); // opcional + y solo números

    private final RestaurantePersistencePort restaurantePersistencePort;
    private final UsuarioConsultaPort usuarioConsultaPort;

    public RestauranteUseCase(RestaurantePersistencePort restaurantePersistencePort,
                              UsuarioConsultaPort usuarioConsultaPort) {
        this.restaurantePersistencePort = restaurantePersistencePort;
        this.usuarioConsultaPort = usuarioConsultaPort;
    }

    @Override
    public Restaurante crearRestaurante(Restaurante restaurante) {
        validarCampos(restaurante);

        if (restaurantePersistencePort.existePorNit(restaurante.getNit())) {
            throw new DomainException("Ya existe un restaurante con ese NIT");
        }

        String rol = usuarioConsultaPort.obtenerRol(restaurante.getIdPropietario())
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario propietario no existe"));
        if (!ROL_PROPIETARIO.equals(rol)) {
            throw new DomainException("El usuario indicado no tiene rol de propietario");
        }

        return restaurantePersistencePort.guardar(restaurante);
    }

    @Override
    public Pagina<Restaurante> listarRestaurantes(int pagina, int tamanio) {
        if (pagina < 0) throw new DomainException("La página no puede ser negativa");
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO_PAGINA) {
            throw new DomainException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO_PAGINA);
        }
        return restaurantePersistencePort.listarOrdenadosPorNombre(pagina, tamanio);
    }

    @Override
    public Restaurante obtenerRestaurante(Long idRestaurante) {
        return restaurantePersistencePort.buscarPorId(idRestaurante)
                .orElseThrow(() -> new RecursoNoEncontradoException("El restaurante no existe"));
    }

    private void validarCampos(Restaurante r) {
        if (esVacio(r.getNombre())) throw new DomainException("El nombre es obligatorio");
        if (SOLO_NUMEROS.matcher(r.getNombre().trim()).matches()) {
            throw new DomainException("El nombre del restaurante no puede contener solo números");
        }
        if (esVacio(r.getNit())) throw new DomainException("El NIT es obligatorio");
        if (!SOLO_NUMEROS.matcher(r.getNit()).matches()) {
            throw new DomainException("El NIT debe ser únicamente numérico");
        }
        if (esVacio(r.getDireccion())) throw new DomainException("La dirección es obligatoria");
        if (esVacio(r.getTelefono())) throw new DomainException("El teléfono es obligatorio");
        if (r.getTelefono().length() > 13 || !TELEFONO.matcher(r.getTelefono()).matches()) {
            throw new DomainException("El teléfono debe tener máximo 13 caracteres y solo puede incluir números y el símbolo + al inicio");
        }
        if (esVacio(r.getUrlLogo())) throw new DomainException("La URL del logo es obligatoria");
        if (r.getIdPropietario() == null) throw new DomainException("El id del propietario es obligatorio");
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
