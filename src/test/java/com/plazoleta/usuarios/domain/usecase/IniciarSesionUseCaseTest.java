
package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.DomainException;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IniciarSesionUseCaseTest {

    @Mock
    private UserPersistencePort persistencePort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private UserUseCase useCase;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setCorreo("maria@correo.com");
        usuario.setClave("CLAVE_ENCRIPTADA");
    }

    @Test
    void iniciarSesion_correctamente() {
        when(persistencePort.buscarPorCorreo("maria@correo.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoderPort.verificar(
                "clave123", "CLAVE_ENCRIPTADA"))
                .thenReturn(true);

        User resultado = useCase.iniciarSesion(
                "maria@correo.com", "clave123");

        assertEquals(usuario, resultado);
        verify(passwordEncoderPort)
                .verificar("clave123", "CLAVE_ENCRIPTADA");
    }

    @Test
    void iniciarSesion_correoVacio() {
        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion("", "clave123"));

        verifyNoInteractions(persistencePort);
    }

    @Test
    void iniciarSesion_claveVacia() {
        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion(
                        "maria@correo.com", ""));

        verifyNoInteractions(persistencePort);
    }

    @Test
    void iniciarSesion_usuarioNoExiste() {
        when(persistencePort.buscarPorCorreo("maria@correo.com"))
                .thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion(
                        "maria@correo.com", "clave123"));

        verifyNoInteractions(passwordEncoderPort);
    }

    @Test
    void iniciarSesion_claveIncorrecta() {
        when(persistencePort.buscarPorCorreo("maria@correo.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoderPort.verificar(
                "claveIncorrecta", "CLAVE_ENCRIPTADA"))
                .thenReturn(false);

        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion(
                        "maria@correo.com", "claveIncorrecta"));
    }

    @Test
    void iniciarSesion_correoNulo() {
        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion(null, "clave123"));

        verifyNoInteractions(persistencePort);
    }

    @Test
    void iniciarSesion_claveNula() {
        assertThrows(DomainException.class,
                () -> useCase.iniciarSesion(
                        "maria@correo.com", null));

        verifyNoInteractions(persistencePort);
    }
}
