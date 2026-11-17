
package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.DomainException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearClienteUseCaseTest {

    @Mock
    private UserPersistencePort persistencePort;

    @Mock
    private PasswordEncoderPort encoderPort;

    @InjectMocks
    private UserUseCase useCase;

    private User cliente;

    @BeforeEach
    void setUp() {
        cliente = new User(
            null,
            "Maria",
            "Gomez",
            "123456789",
            "3001234567",
            LocalDate.of(2005, 5, 10),
            "maria@correo.com",
            "clave123",
            null
        );
    }

    @Test
    void crearCliente_correctamente() {
        when(encoderPort.encriptar("clave123"))
            .thenReturn("CLAVE_ENCRIPTADA");

        when(persistencePort.guardar(any(User.class)))
            .thenAnswer(i -> i.getArgument(0));

        User resultado = useCase.crearCliente(cliente);

        assertEquals(Role.CLIENTE, resultado.getRol());
        assertEquals("CLAVE_ENCRIPTADA", resultado.getClave());

        verify(persistencePort).guardar(cliente);
    }

    @Test
    void crearCliente_correoInvalido() {
        cliente.setCorreo("correoIncorrecto");

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearCliente_correoDuplicado() {
        when(persistencePort.existePorCorreo("maria@correo.com"))
            .thenReturn(true);

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearCliente_documentoDuplicado() {
        when(persistencePort.existePorDocumento("123456789"))
            .thenReturn(true);

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearCliente_nombreVacio() {
        cliente.setNombre("");

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearCliente_celularInvalido() {
        cliente.setCelular("300ABC123");

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearCliente_documentoInvalido() {
        cliente.setDocumento("123ABC");

        assertThrows(
            DomainException.class,
            () -> useCase.crearCliente(cliente)
        );

        verify(persistencePort, never()).guardar(any());
    }
}
