package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.AccesoDenegadoException;
import com.plazoleta.usuarios.domain.exception.DomainException;
import com.plazoleta.usuarios.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.RestauranteConsultaPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** HU6: crear cuenta de empleado. */
@ExtendWith(MockitoExtension.class)
class CrearEmpleadoUseCaseTest {

    private static final String CORREO_PROPIETARIO = "duenio@correo.com";
    private static final Long ID_PROPIETARIO = 5L;
    private static final Long ID_RESTAURANTE = 10L;

    @Mock UserPersistencePort persistencePort;
    @Mock PasswordEncoderPort encoderPort;
    @Mock RestauranteConsultaPort restauranteConsultaPort;
    @InjectMocks UserUseCase useCase;

    private User empleado;
    private User propietario;

    @BeforeEach
    void setUp() {
        empleado = new User(null, "Luis", "Rojas", "1122334455", "+573001112233",
                LocalDate.of(1998, 3, 15), "luis@correo.com", "clave123", null);
        empleado.setIdRestaurante(ID_RESTAURANTE);

        propietario = new User(ID_PROPIETARIO, "Ana", "Pérez", "123456789", "+573005698325",
                LocalDate.of(1990, 5, 10), CORREO_PROPIETARIO, "HASH", Role.PROPIETARIO);
    }

    @Test
    void crearEmpleado_ok_asignaRolEmpleadoYEncriptaClave() {
        when(persistencePort.buscarPorCorreo(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restauranteConsultaPort.obtenerIdPropietario(ID_RESTAURANTE)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(encoderPort.encriptar("clave123")).thenReturn("CLAVE_ENCRIPTADA");
        when(persistencePort.guardar(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User resultado = useCase.crearEmpleado(empleado, CORREO_PROPIETARIO);

        assertEquals(Role.EMPLEADO, resultado.getRol());
        assertEquals("CLAVE_ENCRIPTADA", resultado.getClave());
        assertEquals(ID_RESTAURANTE, resultado.getIdRestaurante());
        verify(persistencePort).guardar(empleado);
    }

    @Test
    void crearEmpleado_aunqueEnvienOtroRol_quedaComoEmpleado() {
        empleado.setRol(Role.ADMINISTRADOR);
        when(persistencePort.buscarPorCorreo(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restauranteConsultaPort.obtenerIdPropietario(ID_RESTAURANTE)).thenReturn(Optional.of(ID_PROPIETARIO));
        when(encoderPort.encriptar("clave123")).thenReturn("CLAVE_ENCRIPTADA");
        when(persistencePort.guardar(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(Role.EMPLEADO, useCase.crearEmpleado(empleado, CORREO_PROPIETARIO).getRol());
    }

    @Test
    void crearEmpleado_restauranteDeOtroPropietario_lanzaAccesoDenegado() {
        when(persistencePort.buscarPorCorreo(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restauranteConsultaPort.obtenerIdPropietario(ID_RESTAURANTE)).thenReturn(Optional.of(99L));

        assertThrows(AccesoDenegadoException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearEmpleado_restauranteInexistente_lanzaNoEncontrado() {
        when(persistencePort.buscarPorCorreo(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restauranteConsultaPort.obtenerIdPropietario(ID_RESTAURANTE)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearEmpleado_sinRestaurante_lanzaExcepcion() {
        empleado.setIdRestaurante(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        assertEquals("El restaurante del empleado es obligatorio", ex.getMessage());
        verifyNoInteractions(restauranteConsultaPort);
    }

    @Test
    void crearEmpleado_correoRepetido_lanzaExcepcion() {
        when(persistencePort.existePorCorreo("luis@correo.com")).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearEmpleado_documentoRepetido_lanzaExcepcion() {
        when(persistencePort.existePorDocumento("1122334455")).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        verify(persistencePort, never()).guardar(any());
    }

    @Test
    void crearEmpleado_correoInvalido_lanzaExcepcion() {
        empleado.setCorreo("correo-malo");
        assertThrows(DomainException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
    }

    @Test
    void crearEmpleado_celularInvalido_lanzaExcepcion() {
        empleado.setCelular("300ABC");
        assertThrows(DomainException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
    }

    @Test
    void crearEmpleado_campoObligatorioVacio_lanzaExcepcion() {
        empleado.setNombre(" ");
        assertThrows(DomainException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
    }

    @Test
    void crearEmpleado_propietarioNoIdentificado_lanzaAccesoDenegado() {
        when(persistencePort.buscarPorCorreo(CORREO_PROPIETARIO)).thenReturn(Optional.empty());

        assertThrows(AccesoDenegadoException.class, () -> useCase.crearEmpleado(empleado, CORREO_PROPIETARIO));
        verifyNoInteractions(restauranteConsultaPort);
    }
}
