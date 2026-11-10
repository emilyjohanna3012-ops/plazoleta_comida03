
package com.plazoleta.usuarios;

import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import com.plazoleta.usuarios.infrastructure.security.JwtTokenAdapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "jwt.secret=ClaveTemporalExclusivaParaPruebasJWT2026Segura",
    "jwt.expiration=3600000"
})
@AutoConfigureMockMvc
class UserRestControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtTokenAdapter jwtTokenAdapter;

    @MockBean
    private UserPersistencePort userPersistencePort;

    private String tokenAdministrador() {
        User administrador = new User();
        administrador.setCorreo("admin@correo.com");
        administrador.setRol(Role.ADMINISTRADOR);

        when(userPersistencePort.buscarPorCorreo("admin@correo.com"))
                .thenReturn(Optional.of(administrador));

        return jwtTokenAdapter.generarToken(administrador);
    }

    private String body(String correo, String doc, String nacimiento) {
        return """
            {"nombre":"Ana","apellido":"Pérez","documento":"%s","celular":"+573005698325",
             "fechaNacimiento":"%s","correo":"%s","clave":"miClave123"}
            """.formatted(doc, nacimiento, correo);
    }

    @Test
    void crearPropietario_devuelve201_sinClaveEnLaRespuesta() throws Exception {

        String token = tokenAdministrador();

        when(userPersistencePort.existePorCorreo(anyString()))
                .thenReturn(false);

        when(userPersistencePort.existePorDocumento(anyString()))
                .thenReturn(false);

        when(userPersistencePort.guardar(any(User.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        mvc.perform(post("/api/v1/usuarios/propietario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("it1@correo.com", "111111", "1990-05-10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PROPIETARIO"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void crearPropietario_menorDeEdad_devuelve400() throws Exception {

        String token = tokenAdministrador();

        mvc.perform(post("/api/v1/usuarios/propietario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("it2@correo.com", "222222", "2020-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El usuario debe ser mayor de edad"));
    }

    @Test
    void crearPropietario_sinToken_devuelve403() throws Exception {

        mvc.perform(post("/api/v1/usuarios/propietario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("it3@correo.com", "333333", "1990-05-10")))
                .andExpect(status().isForbidden());
    }
}



 