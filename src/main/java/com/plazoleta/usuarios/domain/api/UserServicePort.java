package com.plazoleta.usuarios.domain.api;

import com.plazoleta.usuarios.domain.model.User;

/** Puerto de ENTRADA: operaciones que ofrece la aplicación. */
public interface UserServicePort {

    User crearPropietario(User user);

    User crearCliente(User user);

    User iniciarSesion(String correo, String clave);
}