package com.plazoleta.trazabilidad.domain.usecase;

import com.plazoleta.trazabilidad.domain.exception.AccesoDenegadoException;
import com.plazoleta.trazabilidad.domain.exception.DomainException;
import com.plazoleta.trazabilidad.domain.model.Eficiencia;
import com.plazoleta.trazabilidad.domain.model.EstadoPedido;
import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.domain.model.TrazaConDuracion;
import com.plazoleta.trazabilidad.domain.spi.TrazaPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrazaUseCaseTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 10, 12, 0, 0);

    private TrazaPersistencePort persistencia;
    private TrazaUseCase useCase;

    @BeforeEach
    void setUp() {
        persistencia = mock(TrazaPersistencePort.class);
        useCase = new TrazaUseCase(persistencia);
    }

    static Traza traza(Long idPedido, Long idCliente, EstadoPedido anterior, EstadoPedido nuevo,
                       Long idEmpleado, LocalDateTime fecha) {
        return new Traza(null, idPedido, idCliente, 1L, anterior, nuevo, idEmpleado, fecha);
    }

    // ---------- registrar ----------

    @Test
    void registrarGuardaLaTrazaValida() {
        Traza t = traza(10L, 5L, null, EstadoPedido.PENDIENTE, null, T0);
        when(persistencia.guardar(t)).thenReturn(t);

        assertEquals(t, useCase.registrar(t));
        verify(persistencia).guardar(t);
    }

    @Test
    void registrarSinIdPedidoFalla() {
        Traza t = traza(null, 5L, null, EstadoPedido.PENDIENTE, null, T0);
        DomainException ex = assertThrows(DomainException.class, () -> useCase.registrar(t));
        assertEquals("El id del pedido es obligatorio", ex.getMessage());
        verify(persistencia, never()).guardar(any());
    }

    @Test
    void registrarSinIdClienteFalla() {
        Traza t = traza(10L, null, null, EstadoPedido.PENDIENTE, null, T0);
        assertThrows(DomainException.class, () -> useCase.registrar(t));
    }

    @Test
    void registrarSinIdRestauranteFalla() {
        Traza t = new Traza(null, 10L, 5L, null, null, EstadoPedido.PENDIENTE, null, T0);
        assertThrows(DomainException.class, () -> useCase.registrar(t));
    }

    @Test
    void registrarSinEstadoNuevoFalla() {
        Traza t = traza(10L, 5L, null, null, null, T0);
        assertThrows(DomainException.class, () -> useCase.registrar(t));
    }

    @Test
    void registrarSinFechaFalla() {
        Traza t = traza(10L, 5L, null, EstadoPedido.PENDIENTE, null, null);
        assertThrows(DomainException.class, () -> useCase.registrar(t));
    }

    // ---------- consultarPorPedido ----------

    @Test
    void consultarCalculaLaDuracionDeCadaEstado() {
        when(persistencia.listarPorPedido(10L)).thenReturn(List.of(
                traza(10L, 5L, null, EstadoPedido.PENDIENTE, null, T0),
                traza(10L, 5L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 7L, T0.plusMinutes(2)),
                traza(10L, 5L, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO, 7L, T0.plusMinutes(17)),
                traza(10L, 5L, EstadoPedido.LISTO, EstadoPedido.ENTREGADO, 7L, T0.plusMinutes(20))));

        List<TrazaConDuracion> historial = useCase.consultarPorPedido(10L, 5L, "CLIENTE");

        assertEquals(4, historial.size());
        assertNull(historial.get(0).duracionEstadoAnteriorSegundos());
        assertEquals(120L, historial.get(1).duracionEstadoAnteriorSegundos());   // 2 min en PENDIENTE
        assertEquals(900L, historial.get(2).duracionEstadoAnteriorSegundos());   // 15 min EN_PREPARACION
        assertEquals(180L, historial.get(3).duracionEstadoAnteriorSegundos());   // 3 min en LISTO
        assertEquals(EstadoPedido.ENTREGADO, historial.get(3).estadoNuevo());
        assertEquals(7L, historial.get(3).idEmpleado());
    }

    @Test
    void consultarPedidoSinTrazasDevuelveListaVacia() {
        when(persistencia.listarPorPedido(99L)).thenReturn(List.of());
        assertTrue(useCase.consultarPorPedido(99L, 5L, "CLIENTE").isEmpty());
    }

    @Test
    void clienteNoPuedeVerPedidoAjeno() {
        when(persistencia.listarPorPedido(10L)).thenReturn(List.of(
                traza(10L, 5L, null, EstadoPedido.PENDIENTE, null, T0)));

        AccesoDenegadoException ex = assertThrows(AccesoDenegadoException.class,
                () -> useCase.consultarPorPedido(10L, 6L, "CLIENTE"));
        assertEquals("Solo puedes consultar la trazabilidad de tus propios pedidos", ex.getMessage());
    }

    // ---------- HU18: calcularEficiencia ----------

    private static Traza t(Long idPedido, EstadoPedido anterior, EstadoPedido nuevo, Long idEmpleado, int minuto) {
        return new Traza(null, idPedido, 5L, 1L, anterior, nuevo, idEmpleado, T0.plusMinutes(minuto));
    }

    /** Pedido 1: empleado 7, 20 min. Pedido 2: empleado 8, 10 min. Pedido 3: empleado 7, 30 min. Pedido 4: cancelado. */
    private List<Traza> trazasDelRestaurante() {
        return List.of(
                t(1L, null, EstadoPedido.PENDIENTE, null, 0),
                t(2L, null, EstadoPedido.PENDIENTE, null, 0),
                t(4L, null, EstadoPedido.PENDIENTE, null, 0),
                t(1L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 7L, 1),
                t(2L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 8L, 1),
                t(4L, EstadoPedido.PENDIENTE, EstadoPedido.CANCELADO, null, 2),
                t(2L, EstadoPedido.LISTO, EstadoPedido.ENTREGADO, 8L, 10),
                t(1L, EstadoPedido.LISTO, EstadoPedido.ENTREGADO, 7L, 20),
                t(3L, null, EstadoPedido.PENDIENTE, null, 20),
                t(3L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 7L, 21),
                t(3L, EstadoPedido.LISTO, EstadoPedido.ENTREGADO, 7L, 50));
    }

    @Test
    void eficienciaCalculaPromedioYMarcaLosPedidosMasDemorados() {
        when(persistencia.listarPorRestaurante(1L)).thenReturn(trazasDelRestaurante());

        Eficiencia e = useCase.calcularEficiencia(1L);

        assertEquals(1L, e.idRestaurante());
        assertEquals(1200L, e.promedioSegundos());              // (1200 + 600 + 1800) / 3
        assertEquals(3, e.pedidos().size());                    // el cancelado no cuenta
        assertEquals(3L, e.pedidos().get(0).idPedido());        // el más demorado primero
        assertEquals(1800L, e.pedidos().get(0).duracionSegundos());
        assertTrue(e.pedidos().get(0).superaPromedio());
        assertFalse(e.pedidos().get(1).superaPromedio());       // igual al promedio no lo "supera"
        assertEquals(T0.plusMinutes(20), e.pedidos().get(0).inicio());
        assertEquals(T0.plusMinutes(50), e.pedidos().get(0).fin());
    }

    @Test
    void eficienciaOrdenaElRankingDelEmpleadoMasRapidoAlMasLento() {
        when(persistencia.listarPorRestaurante(1L)).thenReturn(trazasDelRestaurante());

        List<Eficiencia.RankingEmpleado> ranking = useCase.calcularEficiencia(1L).rankingEmpleados();

        assertEquals(2, ranking.size());
        assertEquals(1, ranking.get(0).posicion());
        assertEquals(8L, ranking.get(0).idEmpleado());
        assertEquals(1, ranking.get(0).pedidosAtendidos());
        assertEquals(600L, ranking.get(0).promedioSegundos());
        assertEquals(2, ranking.get(1).posicion());
        assertEquals(7L, ranking.get(1).idEmpleado());
        assertEquals(2, ranking.get(1).pedidosAtendidos());
        assertEquals(1500L, ranking.get(1).promedioSegundos()); // (1200 + 1800) / 2
    }

    @Test
    void eficienciaSinPedidosEntregadosDevuelveCeroYListasVacias() {
        when(persistencia.listarPorRestaurante(2L)).thenReturn(List.of(t(9L, null, EstadoPedido.PENDIENTE, null, 0)));

        Eficiencia e = useCase.calcularEficiencia(2L);

        assertEquals(0L, e.promedioSegundos());
        assertTrue(e.pedidos().isEmpty());
        assertTrue(e.rankingEmpleados().isEmpty());
    }
}
