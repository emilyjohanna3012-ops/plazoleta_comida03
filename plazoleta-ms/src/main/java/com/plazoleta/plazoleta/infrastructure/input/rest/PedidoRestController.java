package com.plazoleta.plazoleta.infrastructure.input.rest;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.CrearPedidoRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoResponse;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.*;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.*;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoRestController {
 private final PedidoRepository pedidos;
 private final PlatoRepository platos;
 private final RestauranteRepository restaurantes;
 public PedidoRestController(PedidoRepository pedidos,PlatoRepository platos,RestauranteRepository restaurantes){
  this.pedidos=pedidos;this.platos=platos;this.restaurantes=restaurantes;
 }
 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public PedidoResponse crear(@RequestBody CrearPedidoRequest request,@AuthenticationPrincipal UsuarioAutenticado usuario){
  if(usuario==null || usuario.id()==null) throw new DomainException("Debes iniciar sesión como cliente");
  if(request==null || request.idRestaurante()==null || request.platos()==null || request.platos().isEmpty())
   throw new DomainException("Debes seleccionar un restaurante y al menos un plato");
  if(!restaurantes.existsById(request.idRestaurante())) throw new DomainException("El restaurante no existe");
  PedidoEntity pedido=new PedidoEntity();pedido.setIdCliente(usuario.id());pedido.setIdRestaurante(request.idRestaurante());
  long total=0;
  Set<Long> vistos=new HashSet<>();
  for(CrearPedidoRequest.PlatoPedidoRequest item:request.platos()){
   if(item==null || item.idPlato()==null || item.cantidad()==null || item.cantidad()<=0)
    throw new DomainException("Cada plato debe tener un ID y una cantidad positiva");
   if(!vistos.add(item.idPlato())) throw new DomainException("No repitas platos en el pedido");
   PlatoEntity plato=platos.findById(item.idPlato()).orElseThrow(()->new DomainException("El plato no existe"));
   if(!request.idRestaurante().equals(plato.getIdRestaurante())) throw new DomainException("Todos los platos deben pertenecer al mismo restaurante");
   if(!Boolean.TRUE.equals(plato.getActivo())) throw new DomainException("Uno de los platos no está disponible");
   if(plato.getPrecio()==null || plato.getPrecio()<0) throw new DomainException("El precio del plato no es válido");
   try {total=Math.addExact(total,Math.multiplyExact((long)plato.getPrecio(),(long)item.cantidad()));}
   catch(ArithmeticException ex){throw new DomainException("El total del pedido es demasiado grande");}
   PedidoPlatoEntity detalle=new PedidoPlatoEntity();detalle.setIdPlato(plato.getId());detalle.setCantidad(item.cantidad());detalle.setPrecioUnitario(plato.getPrecio());pedido.agregarPlato(detalle);
  }
  pedido.setTotal(total);
  PedidoEntity guardado=pedidos.saveAndFlush(pedido);
  List<PedidoResponse.PlatoResponsePedido> detalles=guardado.getPlatos().stream()
   .map(p->new PedidoResponse.PlatoResponsePedido(p.getIdPlato(),p.getCantidad(),p.getPrecioUnitario())).toList();
  return new PedidoResponse(guardado.getId(),guardado.getIdCliente(),guardado.getIdRestaurante(),
   guardado.getIdEmpleadoAsignado(),guardado.getEstado(),guardado.getFechaCreacion(),guardado.getTotal(),detalles);
 }
}
