package com.plazoleta.plazoleta.domain.spi;

import java.util.Optional;

/** Puerto de SALIDA: consulta al microservicio de Usuarios. */
public interface UsuarioConsultaPort {

    /** Devuelve el rol del usuario (por ejemplo "PROPIETARIO"), o vacío si el usuario no existe. */
    Optional<String> obtenerRol(Long idUsuario);
}
