package com.plazoleta.trazabilidad.infrastructure.output.mongo.adapter;

import com.plazoleta.trazabilidad.domain.model.Traza;
import com.plazoleta.trazabilidad.domain.spi.TrazaPersistencePort;
import com.plazoleta.trazabilidad.infrastructure.output.mongo.document.TrazaDocument;
import com.plazoleta.trazabilidad.infrastructure.output.mongo.repository.TrazaMongoRepository;

import java.util.List;

/** Adaptador de SALIDA: implementa el puerto de trazas usando MongoDB. */
public class TrazaMongoAdapter implements TrazaPersistencePort {

    private final TrazaMongoRepository repository;

    public TrazaMongoAdapter(TrazaMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Traza guardar(Traza traza) {
        return toDomain(repository.save(toDocument(traza)));
    }

    @Override
    public List<Traza> listarPorPedido(Long idPedido) {
        return repository.findByIdPedidoOrderByFechaAsc(idPedido).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Traza> listarPorRestaurante(Long idRestaurante) {
        return repository.findByIdRestauranteOrderByFechaAsc(idRestaurante).stream().map(this::toDomain).toList();
    }

    protected TrazaDocument toDocument(Traza t) {
        TrazaDocument d = new TrazaDocument();
        d.setId(t.id());
        d.setIdPedido(t.idPedido());
        d.setIdCliente(t.idCliente());
        d.setIdRestaurante(t.idRestaurante());
        d.setEstadoAnterior(t.estadoAnterior());
        d.setEstadoNuevo(t.estadoNuevo());
        d.setIdEmpleado(t.idEmpleado());
        d.setFecha(t.fecha());
        return d;
    }

    protected Traza toDomain(TrazaDocument d) {
        return new Traza(d.getId(), d.getIdPedido(), d.getIdCliente(), d.getIdRestaurante(),
                d.getEstadoAnterior(), d.getEstadoNuevo(), d.getIdEmpleado(), d.getFecha());
    }
}
