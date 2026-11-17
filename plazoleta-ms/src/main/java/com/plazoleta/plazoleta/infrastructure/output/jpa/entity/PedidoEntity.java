package com.plazoleta.plazoleta.infrastructure.output.jpa.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name="pedidos")
public class PedidoEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="id_cliente",nullable=false) private Long idCliente;
 @Column(name="id_restaurante",nullable=false) private Long idRestaurante;
 @Column(name="id_empleado_asignado") private Long idEmpleadoAsignado;
 @Column(nullable=false) private String estado="PENDIENTE";
 @Column(name="fecha_creacion",nullable=false) private LocalDateTime fechaCreacion=LocalDateTime.now();
 @Column(nullable=false) private Long total;
 @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
 private List<PedidoPlatoEntity> platos=new ArrayList<>();
 public Long getId(){return id;} public Long getIdCliente(){return idCliente;} public void setIdCliente(Long v){idCliente=v;}
 public Long getIdRestaurante(){return idRestaurante;} public void setIdRestaurante(Long v){idRestaurante=v;}
 public Long getIdEmpleadoAsignado(){return idEmpleadoAsignado;}
 public String getEstado(){return estado;} public LocalDateTime getFechaCreacion(){return fechaCreacion;}
 public Long getTotal(){return total;} public void setTotal(Long v){total=v;}
 public List<PedidoPlatoEntity> getPlatos(){return platos;}
 public void agregarPlato(PedidoPlatoEntity plato){plato.setPedido(this);platos.add(plato);}
}
