package com.plazoleta.trazabilidad;

import com.plazoleta.trazabilidad.domain.model.EstadoPedido;
import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.domain.spi.TrazaPersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los endpoints con seguridad real (JWT) y MongoDB simulado (@MockBean del puerto),
 * así no hace falta tener MongoDB encendido para correr mvn test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrazaRestControllerTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 10, 12, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrazaPersistencePort persistencia;

    private static final String CUERPO = """
            {"idPedido":10,"idCliente":5,"idRestaurante":1,"estadoAnterior":"PENDIENTE",
             "estadoNuevo":"EN_PREPARACION","idEmpleado":7,"fecha":"2026-11-10T12:02:00"}
            """;

    // ---------- POST /api/v1/trazas ----------

    @Test
    void registrarDevuelve201ConElId() throws Exception {
        when(persistencia.guardar(any())).thenAnswer(inv -> {
            Traza t = inv.getArgument(0);
            return new Traza("abc123", t.idPedido(), t.idCliente(), t.idRestaurante(), t.estadoAnterior(),
                    t.estadoNuevo(), t.idEmpleado(), t.fecha());
        });

        mockMvc.perform(post("/api/v1/trazas")
                        .header("Authorization", TokenDePrueba.bearer(7, "EMPLEADO"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc123"))
                .andExpect(jsonPath("$.estadoNuevo").value("EN_PREPARACION"));
    }

    @Test
    void registrarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/trazas").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        verify(persistencia, never()).guardar(any());
    }

    @Test
    void registrarConEstadoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/trazas")
                        .header("Authorization", TokenDePrueba.bearer(5, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPedido\":10,\"idCliente\":5,\"idRestaurante\":1,\"estadoNuevo\":\"VOLANDO\",\"fecha\":\"2026-11-10T12:00:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarSinIdPedidoDevuelve400ConMensaje() throws Exception {
        mockMvc.perform(post("/api/v1/trazas")
                        .header("Authorization", TokenDePrueba.bearer(5, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCliente\":5,\"idRestaurante\":1,\"estadoNuevo\":\"PENDIENTE\",\"fecha\":\"2026-11-10T12:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El id del pedido es obligatorio"));
    }

    // ---------- GET /api/v1/trazas/pedido/{id} ----------

    @Test
    void clienteDuenoVeElHistorialConDuraciones() throws Exception {
        when(persistencia.listarPorPedido(10L)).thenReturn(List.of(
                new Traza("a", 10L, 5L, 1L, null, EstadoPedido.PENDIENTE, null, T0),
                new Traza("b", 10L, 5L, 1L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 7L, T0.plusMinutes(2))));

        mockMvc.perform(get("/api/v1/trazas/pedido/10").header("Authorization", TokenDePrueba.bearer(5, "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estadoNuevo").value("PENDIENTE"))
                .andExpect(jsonPath("$[1].estadoAnterior").value("PENDIENTE"))
                .andExpect(jsonPath("$[1].duracionEstadoAnteriorSegundos").value(120));
    }

    @Test
    void clienteAjenoRecibe403() throws Exception {
        when(persistencia.listarPorPedido(10L)).thenReturn(List.of(
                new Traza("a", 10L, 5L, 1L, null, EstadoPedido.PENDIENTE, null, T0)));

        mockMvc.perform(get("/api/v1/trazas/pedido/10").header("Authorization", TokenDePrueba.bearer(6, "CLIENTE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo puedes consultar la trazabilidad de tus propios pedidos"));
    }

    @Test
    void empleadoNoPuedeConsultarHistorial() throws Exception {
        mockMvc.perform(get("/api/v1/trazas/pedido/10").header("Authorization", TokenDePrueba.bearer(7, "EMPLEADO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/trazas/pedido/10")).andExpect(status().isUnauthorized());
    }

    // ---------- GET /api/v1/trazas/eficiencia/restaurante/{id} (HU18) ----------

    @Test
    void propietarioVeLaEficienciaDelRestaurante() throws Exception {
        when(persistencia.listarPorRestaurante(1L)).thenReturn(List.of(
                new Traza("a", 10L, 5L, 1L, null, EstadoPedido.PENDIENTE, null, T0),
                new Traza("b", 10L, 5L, 1L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, 7L, T0.plusMinutes(1)),
                new Traza("c", 10L, 5L, 1L, EstadoPedido.LISTO, EstadoPedido.ENTREGADO, 7L, T0.plusMinutes(21))));

        mockMvc.perform(get("/api/v1/trazas/eficiencia/restaurante/1")
                        .header("Authorization", TokenDePrueba.bearer(3, "PROPIETARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRestaurante").value(1))
                .andExpect(jsonPath("$.promedioSegundos").value(1260))
                .andExpect(jsonPath("$.pedidos[0].idPedido").value(10))
                .andExpect(jsonPath("$.pedidos[0].superaPromedio").value(false))
                .andExpect(jsonPath("$.rankingEmpleados[0].posicion").value(1))
                .andExpect(jsonPath("$.rankingEmpleados[0].idEmpleado").value(7));
    }

    @Test
    void clienteNoPuedeVerEficiencia() throws Exception {
        mockMvc.perform(get("/api/v1/trazas/eficiencia/restaurante/1")
                        .header("Authorization", TokenDePrueba.bearer(5, "CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void eficienciaConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/trazas/eficiencia/restaurante/abc")
                        .header("Authorization", TokenDePrueba.bearer(3, "PROPIETARIO")))
                .andExpect(status().isBadRequest());
    }
}
