package com.plazoleta.trazabilidad.infrastructure.output.mongo.repository;

import com.plazoleta.trazabilidad.infrastructure.output.mongo.document.TrazaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrazaMongoRepository extends MongoRepository<TrazaDocument, String> {

    List<TrazaDocument> findByIdPedidoOrderByFechaAsc(Long idPedido);

    List<TrazaDocument> findByIdRestauranteOrderByFechaAsc(Long idRestaurante);
}
