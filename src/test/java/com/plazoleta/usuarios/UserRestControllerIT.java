package com.plazoleta.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerIT {

    @Autowired MockMvc mvc;

    private String body(String correo, String doc, String nacimiento) {
        return """
            {"nombre":"Ana","apellido":"Pérez","documento":"%s","celular":"+573005698325",
             "fechaNacimiento":"%s","correo":"%s","clave":"miClave123"}
            """.formatted(doc, nacimiento, correo);
    }

    @Test
    void crearPropietario_devuelve201_sinClaveEnLaRespuesta() throws Exception {
        mvc.perform(post("/api/v1/usuarios/propietario").contentType(MediaType.APPLICATION_JSON)
                        .content(body("it1@correo.com", "111111", "1990-05-10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PROPIETARIO"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void crearPropietario_menorDeEdad_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/usuarios/propietario").contentType(MediaType.APPLICATION_JSON)
                        .content(body("it2@correo.com", "222222", "2020-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario debe ser mayor de edad"));
    }
}
