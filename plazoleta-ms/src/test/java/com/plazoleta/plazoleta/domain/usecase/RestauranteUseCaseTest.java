package com.plazoleta.plazoleta.domain.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioConsultaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestauranteUseCaseTest {

    @Mock RestaurantePersistencePort restaurantePersistencePort;
    @Mock UsuarioConsultaPort usuarioConsultaPort;
    @InjectMocks RestauranteUseCase useCase;

    private Restaurante restauranteValido;

    @BeforeEach
    void setUp() {
        restauranteValido = new Restaurante(null, "Comidas El Sabor", "900123456", "Calle 10 # 20-30",
                "+573005698325", "http://imagenes.com/logo.png", 1L);
    }

    // ---------------- HU2: crear restaurante ----------------

    @Test
    void crearRestaurante_ok_seGuarda() {
        when(usuarioConsultaPort.obtenerRol(1L)).thenReturn(Optional.of("PROPIETARIO"));
        when(restaurantePersistencePort.guardar(any(Restaurante.class))).thenAnswer(i -> i.getArgument(0));

        Restaurante resultado = useCase.crearRestaurante(restauranteValido);

        assertEquals("Comidas El Sabor", resultado.getNombre());
        verify(restaurantePersistencePort).guardar(restauranteValido);
    }

    @Test
    void crearRestaurante_nombreConNumerosYLetras_esValido() {
        restauranteValido.setNombre("Restaurante 123");
        when(usuarioConsultaPort.obtenerRol(1L)).thenReturn(Optional.of("PROPIETARIO"));
        when(restaurantePersistencePort.guardar(any(Restaurante.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_nombreSoloNumeros_lanzaExcepcion() {
        restauranteValido.setNombre("12345");
        DomainException ex = assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
        assertEquals("El nombre del restaurante no puede contener solo números", ex.getMessage());
        verify(restaurantePersistencePort, never()).guardar(any());
    }

    @Test
    void crearRestaurante_nombreVacio_lanzaExcepcion() {
        restauranteValido.setNombre(" ");
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_nitConLetras_lanzaExcepcion() {
        restauranteValido.setNit("900A23");
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_telefonoMasDe13Caracteres_lanzaExcepcion() {
        restauranteValido.setTelefono("+5730056983251"); // 14 caracteres
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_telefonoConLetras_lanzaExcepcion() {
        restauranteValido.setTelefono("300abc");
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_direccionVacia_lanzaExcepcion() {
        restauranteValido.setDireccion("");
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_urlLogoVacia_lanzaExcepcion() {
        restauranteValido.setUrlLogo(null);
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
    }

    @Test
    void crearRestaurante_sinPropietario_lanzaExcepcion() {
        restauranteValido.setIdPropietario(null);
        assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
        verify(usuarioConsultaPort, never()).obtenerRol(any());
    }

    @Test
    void crearRestaurante_nitRepetido_lanzaExcepcion() {
        when(restaurantePersistencePort.existePorNit("900123456")).thenReturn(true);
        DomainException ex = assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
        assertEquals("Ya existe un restaurante con ese NIT", ex.getMessage());
        verify(restaurantePersistencePort, never()).guardar(any());
    }

    @Test
    void crearRestaurante_propietarioInexistente_lanzaNoEncontrado() {
        when(usuarioConsultaPort.obtenerRol(1L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.crearRestaurante(restauranteValido));
        verify(restaurantePersistencePort, never()).guardar(any());
    }

    @Test
    void crearRestaurante_usuarioNoEsPropietario_lanzaExcepcion() {
        when(usuarioConsultaPort.obtenerRol(1L)).thenReturn(Optional.of("CLIENTE"));
        DomainException ex = assertThrows(DomainException.class, () -> useCase.crearRestaurante(restauranteValido));
        assertEquals("El usuario indicado no tiene rol de propietario", ex.getMessage());
        verify(restaurantePersistencePort, never()).guardar(any());
    }

    // ---------------- HU9: listar restaurantes ----------------

    @Test
    void listarRestaurantes_ok_devuelveLaPagina() {
        Pagina<Restaurante> pagina = new Pagina<>(List.of(restauranteValido), 0, 10, 1, 1);
        when(restaurantePersistencePort.listarOrdenadosPorNombre(0, 10)).thenReturn(pagina);

        Pagina<Restaurante> resultado = useCase.listarRestaurantes(0, 10);

        assertSame(pagina, resultado);
    }

    @Test
    void listarRestaurantes_paginaNegativa_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.listarRestaurantes(-1, 10));
        verify(restaurantePersistencePort, never()).listarOrdenadosPorNombre(anyInt(), anyInt());
    }

    @Test
    void listarRestaurantes_tamanioCero_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.listarRestaurantes(0, 0));
    }

    @Test
    void listarRestaurantes_tamanioMayorAlMaximo_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.listarRestaurantes(0, 51));
    }

    // ---------------- Consulta por id (la usa usuarios-ms en la HU6) ----------------

    @Test
    void obtenerRestaurante_existente_loDevuelve() {
        restauranteValido.setId(10L);
        when(restaurantePersistencePort.buscarPorId(10L)).thenReturn(Optional.of(restauranteValido));

        assertEquals(1L, useCase.obtenerRestaurante(10L).getIdPropietario());
    }

    @Test
    void obtenerRestaurante_inexistente_lanzaNoEncontrado() {
        when(restaurantePersistencePort.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtenerRestaurante(99L));
    }
}
