package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PedidoRepository extends JpaRepository<PedidoEntity,Long> {}
