package com.plazoleta.usuarios.domain.spi;

import com.plazoleta.usuarios.domain.model.User;
import java.util.Optional;

/** Puerto de SALIDA: operaciones de persistencia de usuarios. */
public interface UserPersistencePort {

    User guardar(User user);

    boolean existePorCorreo(String correo);

    boolean existePorDocumento(String documento);

    /** Esto permitirá buscar un usuario por su correo. */
    Optional<User> buscarPorCorreo(String correo);
}
