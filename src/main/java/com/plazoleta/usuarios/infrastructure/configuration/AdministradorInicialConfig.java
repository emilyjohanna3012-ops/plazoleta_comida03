package com.plazoleta.usuarios.infrastructure.configuration;

import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Crea el primer ADMINISTRADOR al arrancar, si no existe.
 * Sin él nadie podría crear propietarios (HU1 exige el rol ADMINISTRADOR).
 * Correo y clave se pueden cambiar con las variables de entorno ADMIN_CORREO y ADMIN_CLAVE.
 */
@Configuration
public class AdministradorInicialConfig {

    private static final String DOCUMENTO_ADMIN = "1000000000";

    @Bean
    public CommandLineRunner crearAdministradorInicial(UserPersistencePort persistencePort,
                                                       PasswordEncoderPort passwordEncoderPort,
                                                       @Value("${admin.correo:admin@plazoleta.com}") String correo,
                                                       @Value("${admin.clave:Admin123*}") String clave) {
        return args -> {
            if (persistencePort.existePorCorreo(correo) || persistencePort.existePorDocumento(DOCUMENTO_ADMIN)) {
                return;
            }
            User administrador = new User(null, "Administrador", "Plazoleta", DOCUMENTO_ADMIN, "+570000000000",
                    LocalDate.of(1990, 1, 1), correo, passwordEncoderPort.encriptar(clave), Role.ADMINISTRADOR);
            persistencePort.guardar(administrador);
        };
    }
}
