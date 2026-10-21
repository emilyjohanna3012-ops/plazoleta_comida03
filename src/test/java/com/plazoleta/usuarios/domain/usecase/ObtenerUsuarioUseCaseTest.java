package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerUsuarioUseCaseTest {

    @Mock UserPersistencePort persistencePort;
    @Mock PasswordEncoderPort encoderPort;
    @InjectMocks UserUseCase useCase;

    @Test
    void obtenerUsuario_existente_loDevuelveConSuRol() {
        User propietario = new User(7L, "Ana", "Pérez", "123456789", "+573005698325",
                LocalDate.now().minusYears(30), "ana@correo.com", "HASH", Role.PROPIETARIO);
        when(persistencePort.buscarPorId(7L)).thenReturn(Optional.of(propietario));

        User resultado = useCase.obtenerUsuario(7L);

        assertEquals(Role.PROPIETARIO, resultado.getRol());
    }

    @Test
    void obtenerUsuario_inexistente_lanzaNoEncontrado() {
        when(persistencePort.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtenerUsuario(99L));
    }
}
