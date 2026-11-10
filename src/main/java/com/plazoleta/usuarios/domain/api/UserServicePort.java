package com.plazoleta.usuarios.domain.api;

import com.plazoleta.usuarios.domain.model.User;

/** Puerto de ENTRADA: operaciones que ofrece la aplicación. */
public interface UserServicePort {

    User crearPropietario(User user);

    User crearCliente(User user);

    /** HU6: un propietario crea la cuenta de un empleado de SU restaurante (empleado.idRestaurante). */
    User crearEmpleado(User empleado, String correoPropietario);

    User iniciarSesion(String correo, String clave);

    /** Consultar un usuario por id (lo usa el microservicio Plazoleta en la HU2). */
    User obtenerUsuario(Long id);
}