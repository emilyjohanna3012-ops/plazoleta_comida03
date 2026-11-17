package com.plazoleta.plazoleta.domain.usecase;

import com.plazoleta.plazoleta.domain.exception.AccesoDenegadoException;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestauranteConsultaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseTest {

    private static final Long ID_PROPIETARIO = 10L;

    @Mock PlatoPersistencePort platoPersistencePort;
    @Mock RestauranteConsultaPort restauranteConsultaPort;
    @InjectMocks PlatoUseCase useCase;

    private Plato platoValido;

    @BeforeEach
    void setUp() {
        platoValido = new Plato(null, "Hamburguesa", 15000, "Con queso y tocineta",
                "http://imagenes.com/hamburguesa.png", "Platos fuertes", 1L, null);
    }

    @Test
    void crearPlato_ok_quedaActivoYSeGuarda() {
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(platoPersistencePort.guardar(any(Plato.class))).thenAnswer(i -> i.getArgument(0));

        Plato resultado = useCase.crearPlato(platoValido, ID_PROPIETARIO);

        assertTrue(resultado.getActivo());
        verify(platoPersistencePort).guardar(platoValido);
    }

    @Test
    void crearPlato_precioCero_lanzaExcepcion() {
        platoValido.setPrecio(0);
        DomainException ex = assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
        assertEquals("El precio debe ser un número entero mayor a 0", ex.getMessage());
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void crearPlato_precioNegativo_lanzaExcepcion() {
        platoValido.setPrecio(-500);
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void crearPlato_precioNulo_lanzaExcepcion() {
        platoValido.setPrecio(null);
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_nombreVacio_lanzaExcepcion() {
        platoValido.setNombre("  ");
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_descripcionVacia_lanzaExcepcion() {
        platoValido.setDescripcion(null);
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_urlImagenVacia_lanzaExcepcion() {
        platoValido.setUrlImagen("");
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_categoriaVacia_lanzaExcepcion() {
        platoValido.setCategoria(" ");
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_sinRestaurante_lanzaExcepcion() {
        platoValido.setIdRestaurante(null);
        assertThrows(DomainException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
    }

    @Test
    void crearPlato_restauranteInexistente_lanzaNoEncontrado() {
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void crearPlato_usuarioNoEsElPropietario_lanzaAccesoDenegado() {
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(99L));
        assertThrows(AccesoDenegadoException.class, () -> useCase.crearPlato(platoValido, ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    // ---------------- HU4: modificar plato ----------------

    private Plato platoExistente() {
        return new Plato(5L, "Hamburguesa", 15000, "Descripción vieja",
                "http://imagenes.com/hamburguesa.png", "Platos fuertes", 1L, true);
    }

    @Test
    void modificarPlato_ok_cambiaSoloPrecioYDescripcion() {
        Plato existente = platoExistente();
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.of(existente));
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(platoPersistencePort.guardar(any(Plato.class))).thenAnswer(i -> i.getArgument(0));

        Plato resultado = useCase.modificarPlato(5L, 20000, "Descripción nueva", ID_PROPIETARIO);

        assertEquals(20000, resultado.getPrecio());
        assertEquals("Descripción nueva", resultado.getDescripcion());
        // lo demás no cambia
        assertEquals("Hamburguesa", resultado.getNombre());
        assertEquals("Platos fuertes", resultado.getCategoria());
        assertEquals(1L, resultado.getIdRestaurante());
        assertTrue(resultado.getActivo());
        verify(platoPersistencePort).guardar(existente);
    }

    @Test
    void modificarPlato_platoInexistente_lanzaNoEncontrado() {
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.modificarPlato(5L, 20000, "Nueva", ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void modificarPlato_usuarioNoEsElPropietario_lanzaAccesoDenegado() {
        Plato existente = platoExistente();
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.of(existente));
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(99L));

        assertThrows(AccesoDenegadoException.class,
                () -> useCase.modificarPlato(5L, 20000, "Nueva", ID_PROPIETARIO));
        assertEquals(15000, existente.getPrecio()); // no se tocó el plato
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void modificarPlato_precioCero_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.modificarPlato(5L, 0, "Nueva", ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void modificarPlato_precioNulo_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.modificarPlato(5L, null, "Nueva", ID_PROPIETARIO));
    }

    @Test
    void modificarPlato_descripcionVacia_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.modificarPlato(5L, 20000, " ", ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    // ---------------- HU7: habilitar / deshabilitar plato ----------------

    @Test
    void cambiarEstadoPlato_deshabilitar_ok() {
        Plato existente = platoExistente(); // viene activo
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.of(existente));
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(platoPersistencePort.guardar(any(Plato.class))).thenAnswer(i -> i.getArgument(0));

        Plato resultado = useCase.cambiarEstadoPlato(5L, false, ID_PROPIETARIO);

        assertFalse(resultado.getActivo());
        assertEquals(15000, resultado.getPrecio()); // lo demás no cambia
        verify(platoPersistencePort).guardar(existente);
    }

    @Test
    void cambiarEstadoPlato_habilitar_ok() {
        Plato existente = platoExistente();
        existente.setActivo(false);
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.of(existente));
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(platoPersistencePort.guardar(any(Plato.class))).thenAnswer(i -> i.getArgument(0));

        Plato resultado = useCase.cambiarEstadoPlato(5L, true, ID_PROPIETARIO);

        assertTrue(resultado.getActivo());
    }

    @Test
    void cambiarEstadoPlato_platoInexistente_lanzaNoEncontrado() {
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.cambiarEstadoPlato(5L, false, ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void cambiarEstadoPlato_usuarioNoEsElPropietario_lanzaAccesoDenegado() {
        Plato existente = platoExistente();
        when(platoPersistencePort.buscarPorId(5L)).thenReturn(Optional.of(existente));
        when(restauranteConsultaPort.obtenerIdPropietario(1L)).thenReturn(Optional.of(99L));

        assertThrows(AccesoDenegadoException.class,
                () -> useCase.cambiarEstadoPlato(5L, false, ID_PROPIETARIO));
        assertTrue(existente.getActivo()); // no se tocó el plato
        verify(platoPersistencePort, never()).guardar(any());
    }

    @Test
    void cambiarEstadoPlato_estadoNulo_lanzaExcepcion() {
        assertThrows(DomainException.class, () -> useCase.cambiarEstadoPlato(5L, null, ID_PROPIETARIO));
        verify(platoPersistencePort, never()).guardar(any());
    }
}
