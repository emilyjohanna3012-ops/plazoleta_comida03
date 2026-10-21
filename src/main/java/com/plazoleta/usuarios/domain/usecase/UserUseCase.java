package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.api.UserServicePort;
import com.plazoleta.usuarios.domain.exception.DomainException;
import com.plazoleta.usuarios.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/** Caso de uso: aquí viven las reglas de la HU 1. */
public class UserUseCase implements UserServicePort {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern CELULAR = Pattern.compile("^\\+?\\d+$"); // opcional + y solo números
    private static final Pattern SOLO_NUMEROS = Pattern.compile("^\\d+$");

    private final UserPersistencePort persistencePort;
    private final PasswordEncoderPort passwordEncoderPort;

    public UserUseCase(UserPersistencePort persistencePort, PasswordEncoderPort passwordEncoderPort) {
        this.persistencePort = persistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public User crearPropietario(User user) {
        validarCamposObligatorios(user);
        validarCorreo(user.getCorreo());
        validarCelular(user.getCelular());
        validarDocumento(user.getDocumento());
        validarMayorDeEdad(user.getFechaNacimiento());

        if (persistencePort.existePorCorreo(user.getCorreo())) {
            throw new DomainException("Ya existe un usuario con ese correo");
        }
        if (persistencePort.existePorDocumento(user.getDocumento())) {
            throw new DomainException("Ya existe un usuario con ese documento de identidad");
        }

        user.setRol(Role.PROPIETARIO);
        user.setClave(passwordEncoderPort.encriptar(user.getClave()));
        return persistencePort.guardar(user);
    }
        @Override
    public User crearCliente(User user) {
        validarCamposObligatorios(user);
        validarCorreo(user.getCorreo());
        validarCelular(user.getCelular());
        validarDocumento(user.getDocumento());

        if (persistencePort.existePorCorreo(user.getCorreo())) {
            throw new DomainException("Ya existe un usuario con ese correo");
        }

        if (persistencePort.existePorDocumento(user.getDocumento())) {
            throw new DomainException("Ya existe un usuario con ese documento de identidad");
        }

        user.setRol(Role.CLIENTE);
        user.setClave(passwordEncoderPort.encriptar(user.getClave()));

        return persistencePort.guardar(user);
    }

    @Override
public User iniciarSesion(String correo, String clave) {

    // Comprobar que escribieron correo y contraseña
    if (esVacio(correo) || esVacio(clave)) {
        throw new DomainException("Correo o contraseña incorrectos");
    }

    // Buscar al usuario en la base de datos
    User usuario = persistencePort.buscarPorCorreo(correo)
            .orElseThrow(() ->
                    new DomainException("Correo o contraseña incorrectos"));

    // Verificar la contraseña utilizando BCrypt
    if (!passwordEncoderPort.verificar(clave, usuario.getClave())) {
        throw new DomainException("Correo o contraseña incorrectos");
    }

    // Devolver el usuario autenticado
    return usuario;
}

    @Override
    public User obtenerUsuario(Long id) {
        return persistencePort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }

    private void validarCamposObligatorios(User u) {
        if (esVacio(u.getNombre())) throw new DomainException("El nombre es obligatorio");
        if (esVacio(u.getApellido())) throw new DomainException("El apellido es obligatorio");
        if (esVacio(u.getDocumento())) throw new DomainException("El documento de identidad es obligatorio");
        if (esVacio(u.getCelular())) throw new DomainException("El celular es obligatorio");
        if (u.getFechaNacimiento() == null) throw new DomainException("La fecha de nacimiento es obligatoria");
        if (esVacio(u.getCorreo())) throw new DomainException("El correo es obligatorio");
        if (esVacio(u.getClave())) throw new DomainException("La clave es obligatoria");
    }

    private void validarCorreo(String correo) {
        if (!EMAIL.matcher(correo).matches()) throw new DomainException("El correo no tiene una estructura válida");
    }

    private void validarCelular(String celular) {
        if (celular.length() > 13 || !CELULAR.matcher(celular).matches()) {
            throw new DomainException("El celular debe tener máximo 13 caracteres y solo puede incluir números y el símbolo + al inicio");
        }
    }

    private void validarDocumento(String documento) {
        if (!SOLO_NUMEROS.matcher(documento).matches()) {
            throw new DomainException("El documento de identidad debe ser únicamente numérico");
        }
    }

    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            throw new DomainException("El usuario debe ser mayor de edad");
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }


}
