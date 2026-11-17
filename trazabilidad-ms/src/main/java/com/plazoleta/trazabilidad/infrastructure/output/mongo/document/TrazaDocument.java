package com.plazoleta.trazabilidad.infrastructure.output.mongo.document;

import com.plazoleta.trazabilidad.domain.model.EstadoPedido;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Colección "trazas" en MongoDB: un documento por cada cambio de estado de un pedido. */
@Document(collection = "trazas")
public class TrazaDocument {
    @Id
    private String id;
    private Long idPedido;
    private Long idCliente;
    private Long idRestaurante;
    private EstadoPedido estadoAnterior;
    private EstadoPedido estadoNuevo;
    private Long idEmpleado;
    private LocalDateTime fecha;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public Long getIdRestaurante() { return idRestaurante; }
    public void setIdRestaurante(Long idRestaurante) { this.idRestaurante = idRestaurante; }
    public EstadoPedido getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoPedido estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public EstadoPedido getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoPedido estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
