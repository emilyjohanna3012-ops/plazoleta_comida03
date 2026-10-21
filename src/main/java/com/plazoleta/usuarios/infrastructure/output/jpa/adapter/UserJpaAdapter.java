package com.plazoleta.usuarios.infrastructure.output.jpa.adapter;

import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import com.plazoleta.usuarios.infrastructure.output.jpa.entity.UserEntity;
import com.plazoleta.usuarios.infrastructure.output.jpa.repository.UserRepository;
import java.util.Optional;


/** Adaptador de SALIDA: implementa el puerto usando JPA/MySQL. */
public class UserJpaAdapter implements UserPersistencePort {

    private final UserRepository repository;

    public UserJpaAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User guardar(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    @Override
public Optional<User> buscarPorCorreo(String correo) {
    return repository.findByCorreo(correo).map(this::toDomain);
}

    @Override
    public boolean existePorDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }

    @Override
    public Optional<User> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setNombre(u.getNombre());
        e.setApellido(u.getApellido());
        e.setDocumento(u.getDocumento());
        e.setCelular(u.getCelular());
        e.setFechaNacimiento(u.getFechaNacimiento());
        e.setCorreo(u.getCorreo());
        e.setClave(u.getClave());
        e.setRol(u.getRol());
        return e;
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getNombre(), e.getApellido(), e.getDocumento(), e.getCelular(),
                e.getFechaNacimiento(), e.getCorreo(), e.getClave(), e.getRol());
    }
}
