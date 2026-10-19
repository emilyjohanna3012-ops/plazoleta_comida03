package com.plazoleta.usuarios.domain.spi;

import com.plazoleta.usuarios.domain.model.User;

/** Puerto de SALIDA: lo que la aplicación necesita para guardar usuarios. */
public interface UserPersistencePort {
    User guardar(User user);
    boolean existePorCorreo(String correo);
    boolean existePorDocumento(String documento);
}
