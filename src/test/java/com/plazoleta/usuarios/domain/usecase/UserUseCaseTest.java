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
class UserUseCaseTest {

    @Mock UserPersistencePort persistencePort;
    @Mock PasswordEncoderPort encoderPort;
    @InjectMocks UserUseCase useCase;

    private User usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioValido = new User(null, "Ana", "Pérez", "123456789", "+573005698325",
                LocalDate.now().minusYears(30), "ana@correo.com", "miClave123", null);
    }

    @Test
    void crearPropietario_ok_asignaRolYEncriptaClave() {
        when(encoderPort.encriptar("miClave123")).thenReturn("HASH");
        when(persistencePort.guardar(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User resultado = useCase.crearPropietario(usuarioValido);

        assertEquals(Role.PROPIETARIO, resultado.getRol());
        assertEquals("HASH", resultado.getClave());
        verify(persistencePort).guardar(usuarioValido);
    }

    @Test
    void crearPropietario_menorDeEdad_lanzaExcepcion() {
        usuarioValido.setFechaNacimiento(LocalDate.now().minusYears(17));
        DomainException ex = assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
        assertEquals("El usuario debe ser mayor de edad", ex.getMessage());
        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearPropietario_correoInvalido_lanzaExcepcion() {
        usuarioValido.setCorreo("correo-malo");
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_celularConLetras_lanzaExcepcion() {
        usuarioValido.setCelular("30056abc");
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_celularMasDe13Caracteres_lanzaExcepcion() {
        usuarioValido.setCelular("+5730056983251"); // 14 caracteres
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_documentoNoNumerico_lanzaExcepcion() {
        usuarioValido.setDocumento("12A45");
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_campoObligatorioVacio_lanzaExcepcion() {
        usuarioValido.setNombre(" ");
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_correoRepetido_lanzaExcepcion() {
        when(persistencePort.existePorCorreo("ana@correo.com")).thenReturn(true);
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }

    @Test
    void crearPropietario_documentoRepetido_lanzaExcepcion() {
        when(persistencePort.existePorDocumento("123456789")).thenReturn(true);
        assertThrows(DomainException.class, () -> useCase.crearPropietario(usuarioValido));
    }
}
