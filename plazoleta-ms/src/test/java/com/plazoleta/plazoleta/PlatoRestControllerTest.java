package com.plazoleta.plazoleta;

import com.jayway.jsonpath.JsonPath;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba de integración de las HU3, HU4 y HU7: controller + caso de uso + base H2 de pruebas. */
@SpringBootTest
@AutoConfigureMockMvc
class PlatoRestControllerTest {

    @Autowired MockMvc mvc;
    @Autowired RestauranteRepository restauranteRepository;

    private static final String PROPIETARIO_1 = TokenDePrueba.bearer(1, "PROPIETARIO");
    private static final String PROPIETARIO_2 = TokenDePrueba.bearer(2, "PROPIETARIO");

    /** Restaurante real (en H2) cuyo dueño es el propietario 1. */
    private Long idRestaurante;

    @BeforeEach
    void crearRestauranteDelPropietario1() {
        RestauranteEntity r = new RestauranteEntity();
        r.setNombre("Restaurante de prueba");
        r.setNit(String.valueOf(System.nanoTime()));
        r.setDireccion("Calle 1 # 2-3");
        r.setTelefono("+573001234567");
        r.setUrlLogo("http://imagenes.com/logo.png");
        r.setIdPropietario(1L);
        idRestaurante = restauranteRepository.save(r).getId();
    }

    private String body(String precio, long idRestaurante) {
        return """
            {"nombre":"Hamburguesa","precio":%s,"descripcion":"Con queso",
             "urlImagen":"http://imagenes.com/h.png","categoria":"Fuertes","idRestaurante":%d}
            """.formatted(precio, idRestaurante);
    }

    /** Crea un plato en el restaurante del propietario 1 y devuelve su id. */
    private Integer crearPlato() throws Exception {
        String respuesta = mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.id");
    }

    // ---------------- HU3: crear plato ----------------

    @Test
    void crearPlato_propietarioDelRestaurante_devuelve201YActivo() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crearPlato_otroPropietario_devuelve403() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_2)
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void crearPlato_precioCero_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content(body("0", idRestaurante)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void crearPlato_restauranteInexistente_devuelve404() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content(body("15000", 999999)))
                .andExpect(status().isNotFound());
    }

    // ---------------- HU4: modificar plato ----------------

    @Test
    void modificarPlato_propietario_devuelve200ConNuevoPrecio() throws Exception {
        Integer id = crearPlato();

        mvc.perform(put("/api/v1/platos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"precio\":20000,\"descripcion\":\"Nueva descripcion\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(20000))
                .andExpect(jsonPath("$.nombre").value("Hamburguesa"));
    }

    @Test
    void modificarPlato_otroPropietario_devuelve403() throws Exception {
        Integer id = crearPlato();

        mvc.perform(put("/api/v1/platos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_2)
                        .content("{\"precio\":20000,\"descripcion\":\"Nueva descripcion\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarPlato_platoInexistente_devuelve404() throws Exception {
        mvc.perform(put("/api/v1/platos/999999").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"precio\":20000,\"descripcion\":\"Nueva descripcion\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void modificarPlato_precioInvalido_devuelve400() throws Exception {
        Integer id = crearPlato();

        mvc.perform(put("/api/v1/platos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"precio\":0,\"descripcion\":\"Nueva descripcion\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- HU7: habilitar / deshabilitar plato ----------------

    @Test
    void cambiarEstado_propietario_deshabilitaYHabilita() throws Exception {
        Integer id = crearPlato();

        mvc.perform(patch("/api/v1/platos/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(patch("/api/v1/platos/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void cambiarEstado_otroPropietario_devuelve403() throws Exception {
        Integer id = crearPlato();

        mvc.perform(patch("/api/v1/platos/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_2)
                        .content("{\"activo\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarEstado_platoInexistente_devuelve404() throws Exception {
        mvc.perform(patch("/api/v1/platos/999999/estado").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{\"activo\":false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstado_sinEstado_devuelve400() throws Exception {
        Integer id = crearPlato();

        mvc.perform(patch("/api/v1/platos/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", PROPIETARIO_1)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- HU5: seguridad ----------------

    @Test
    void crearPlato_sinToken_devuelve401() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPlato_tokenInvalido_devuelve401() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer esto-no-es-un-token")
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPlato_conRolCliente_devuelve403() throws Exception {
        mvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TokenDePrueba.bearer(1, "CLIENTE"))
                        .content(body("15000", idRestaurante)))
                .andExpect(status().isForbidden());
    }
}
