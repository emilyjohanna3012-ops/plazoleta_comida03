package com.plazoleta.trazabilidad.domain.usecase;

import com.plazoleta.trazabilidad.domain.api.TrazaServicePort;
import com.plazoleta.trazabilidad.domain.exception.AccesoDenegadoException;
import com.plazoleta.trazabilidad.domain.exception.DomainException;
import com.plazoleta.trazabilidad.domain.model.EstadoPedido;
import com.plazoleta.trazabilidad.domain.model.Eficiencia;
import com.plazoleta.trazabilidad.domain.model.Eficiencia.DuracionPedido;
import com.plazoleta.trazabilidad.domain.model.Eficiencia.RankingEmpleado;
import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.domain.model.TrazaConDuracion;
import com.plazoleta.trazabilidad.domain.spi.TrazaPersistencePort;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caso de uso: aquí viven las reglas de las HU17 y HU18. */
public class TrazaUseCase implements TrazaServicePort {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final TrazaPersistencePort trazaPersistencePort;

    public TrazaUseCase(TrazaPersistencePort trazaPersistencePort) {
        this.trazaPersistencePort = trazaPersistencePort;
    }

    // ---------------- HU17 ----------------

    @Override
    public Traza registrar(Traza traza) {
        if (traza.idPedido() == null) throw new DomainException("El id del pedido es obligatorio");
        if (traza.idCliente() == null) throw new DomainException("El id del cliente es obligatorio");
        if (traza.idRestaurante() == null) throw new DomainException("El id del restaurante es obligatorio");
        if (traza.estadoNuevo() == null) throw new DomainException("El estado nuevo es obligatorio");
        if (traza.fecha() == null) throw new DomainException("La fecha del cambio es obligatoria");
        return trazaPersistencePort.guardar(traza);
    }

    @Override
    public List<TrazaConDuracion> consultarPorPedido(Long idPedido, Long idUsuario, String rol) {
        List<Traza> trazas = trazaPersistencePort.listarPorPedido(idPedido);
        if (trazas.isEmpty()) return List.of();

        // HU17 criterio 2: cada cliente solo puede ver el historial de sus propios pedidos
        if (ROL_CLIENTE.equals(rol) && !trazas.get(0).idCliente().equals(idUsuario)) {
            throw new AccesoDenegadoException("Solo puedes consultar la trazabilidad de tus propios pedidos");
        }

        List<TrazaConDuracion> historial = new ArrayList<>();
        for (int i = 0; i < trazas.size(); i++) {
            Traza actual = trazas.get(i);
            Long duracionAnterior = i == 0 ? null : segundosEntre(trazas.get(i - 1), actual);
            historial.add(new TrazaConDuracion(actual.estadoAnterior(), actual.estadoNuevo(), actual.fecha(),
                    actual.idEmpleado(), duracionAnterior));
        }
        return historial;
    }

    // ---------------- HU18 ----------------

    @Override
    public Eficiencia calcularEficiencia(Long idRestaurante) {
        // 1. Agrupar las trazas del restaurante por pedido (LinkedHashMap conserva el orden por fecha)
        Map<Long, List<Traza>> trazasPorPedido = new LinkedHashMap<>();
        for (Traza t : trazaPersistencePort.listarPorRestaurante(idRestaurante)) {
            trazasPorPedido.computeIfAbsent(t.idPedido(), k -> new ArrayList<>()).add(t);
        }

        // 2. Duración de cada pedido ENTREGADO: desde su primera traza (PENDIENTE) hasta la de ENTREGADO.
        //    Los pedidos cancelados o aún en proceso no cuentan: todavía no tienen un tiempo total real.
        List<PedidoMedido> medidos = new ArrayList<>();
        for (Map.Entry<Long, List<Traza>> entrada : trazasPorPedido.entrySet()) {
            List<Traza> trazas = entrada.getValue();
            Traza entrega = buscar(trazas, EstadoPedido.ENTREGADO);
            if (entrega == null) continue;
            Traza inicio = trazas.get(0);
            Traza asignacion = buscar(trazas, EstadoPedido.EN_PREPARACION);
            Long idEmpleado = asignacion != null ? asignacion.idEmpleado() : entrega.idEmpleado();
            medidos.add(new PedidoMedido(entrada.getKey(), idEmpleado, inicio, entrega, segundosEntre(inicio, entrega)));
        }

        if (medidos.isEmpty()) return new Eficiencia(idRestaurante, 0L, List.of(), List.of());

        // 3. Promedio del restaurante y qué pedidos lo superan (los "más demorados al promedio" del reto)
        long promedio = Math.round(medidos.stream().mapToLong(PedidoMedido::segundos).average().orElse(0));
        List<DuracionPedido> pedidos = medidos.stream()
                .sorted(Comparator.comparingLong(PedidoMedido::segundos).reversed()) // el más demorado primero
                .map(m -> new DuracionPedido(m.idPedido(), m.idEmpleado(), m.inicio().fecha(), m.fin().fecha(),
                        m.segundos(), m.segundos() > promedio))
                .toList();

        // 4. Ranking de empleados: tiempo medio de sus pedidos, de menor a mayor
        Map<Long, List<Long>> tiemposPorEmpleado = new LinkedHashMap<>();
        for (PedidoMedido m : medidos) {
            if (m.idEmpleado() != null) {
                tiemposPorEmpleado.computeIfAbsent(m.idEmpleado(), k -> new ArrayList<>()).add(m.segundos());
            }
        }
        List<Map.Entry<Long, List<Long>>> empleados = tiemposPorEmpleado.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry<Long, List<Long>> e) -> promedioDe(e.getValue()))
                        .thenComparing(e -> e.getKey()))
                .toList();
        List<RankingEmpleado> ranking = new ArrayList<>();
        for (int i = 0; i < empleados.size(); i++) {
            Map.Entry<Long, List<Long>> e = empleados.get(i);
            ranking.add(new RankingEmpleado(i + 1, e.getKey(), e.getValue().size(), Math.round(promedioDe(e.getValue()))));
        }

        return new Eficiencia(idRestaurante, promedio, pedidos, ranking);
    }

    /** Datos intermedios de un pedido entregado (solo se usa dentro de este cálculo). */
    private record PedidoMedido(Long idPedido, Long idEmpleado, Traza inicio, Traza fin, long segundos) { }

    private Traza buscar(List<Traza> trazas, EstadoPedido estado) {
        return trazas.stream().filter(t -> t.estadoNuevo() == estado).findFirst().orElse(null);
    }

    private double promedioDe(List<Long> valores) {
        return valores.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    // ---------------- Apoyo ----------------

    /** Segundos que pasaron entre dos cambios de estado = tiempo que el pedido estuvo en el estado intermedio. */
    private long segundosEntre(Traza desde, Traza hasta) {
        return Duration.between(desde.fecha(), hasta.fecha()).getSeconds();
    }
}
