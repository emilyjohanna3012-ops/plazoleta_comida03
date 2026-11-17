package com.plazoleta.plazoleta.infrastructure.output.jpa.entity;
import jakarta.persistence.*;
@Entity
@Table(name="pedido_platos")
public class PedidoPlatoEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_pedido",nullable=false) private PedidoEntity pedido;
 @Column(name="id_plato",nullable=false) private Long idPlato;
 @Column(nullable=false) private Integer cantidad;
 @Column(name="precio_unitario",nullable=false) private Integer precioUnitario;
 public void setPedido(PedidoEntity v){pedido=v;}
 public Long getIdPlato(){return idPlato;} public void setIdPlato(Long v){idPlato=v;}
 public Integer getCantidad(){return cantidad;} public void setCantidad(Integer v){cantidad=v;}
 public Integer getPrecioUnitario(){return precioUnitario;} public void setPrecioUnitario(Integer v){precioUnitario=v;}
}
