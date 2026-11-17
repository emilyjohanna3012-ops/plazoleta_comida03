package com.plazoleta.usuarios.infrastructure.output.jpa.repository;

import com.plazoleta.usuarios.infrastructure.output.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByCorreo(String correo);

    boolean existsByDocumento(String documento);

    /*permite buscar en MySQL un usuario utilizando su correo electrónico. */
    Optional<UserEntity> findByCorreo(String correo);

}
