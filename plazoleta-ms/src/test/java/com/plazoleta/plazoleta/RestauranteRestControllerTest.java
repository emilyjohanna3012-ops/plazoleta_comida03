package com.plazoleta.plazoleta;

import com.jayway.jsonpath.JsonPath;
import com.plazoleta.plazoleta.domain.spi.UsuarioConsultaPort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de las HU2 y HU9: controller + caso de uso + base H2 de pruebas.
 * El microservicio de Usuarios se simula con @MockBean (en las pruebas no está encendido).
 */
@SpringBootTest
@AutoConfigureMockMvc
class RestauranteRestControllerTest {

    @Autowired MockMvc mvc;
    @Autowired RestauranteRepository restauranteRepository;
    @MockBean UsuarioConsultaPort usuarioConsultaPort;

    private static final String ADMIN = TokenDePrueba.bearer(100, "ADMINISTRADOR");
    private static final String CLIENTE = TokenDePrueba.bearer(200, "CLIENTE");

    private String nitUnico() {
        return String.valueOf(System.nanoTime());
    }

    private String body(String nombre, String nit, long idPropietario) {
        return """
            {"nombre":"%s","nit":"%s","direccion":"Calle 10 # 20-30","telefono":"+573005698325",
             "urlLogo":"http://imagenes.com/logo.png","idPropietario":%d}
            """.formatted(nombre, nit, idPropietario);
    }

    private void guardarRestaurante(String nombre) {
        RestauranteEntity r = new RestauranteEntity();
        r.setNombre(nombre);
        r.setNit(nitUnico());
        r.setDireccion("Calle 1 # 2-3");
        r.setTelefono("+573001234567");
        r.setUrlLogo("http://imagenes.com/logo.png");
        r.setIdPropietario(1L);
        restauranteRepository.save(r);
    }

    // ---------------- HU2 ----------------

    @Test
    void crearRestaurante_propietarioValido_devuelve201() throws Exception {
        when(usuarioConsultaPort.obtenerRol(1L)).thenReturn(Optional.of("PROPIETARIO"));

        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON).header("Authorization", ADMIN)
                        .content(body("Comidas El Sabor", nitUnico(), 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.idPropietario").value(1));
    }

    @Test
    void crearRestaurante_usuarioNoPropietario_devuelve400() throws Exception {
        when(usuarioConsultaPort.obtenerRol(2L)).thenReturn(Optional.of("CLIENTE"));

        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON).header("Authorization", ADMIN)
                        .content(body("Comidas El Sabor", nitUnico(), 2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearRestaurante_propietarioInexistente_devuelve404() throws Exception {
        when(usuarioConsultaPort.obtenerRol(3L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON).header("Authorization", ADMIN)
                        .content(body("Comidas El Sabor", nitUnico(), 3)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearRestaurante_nombreSoloNumeros_devuelve400() throws Exception {
        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON).header("Authorization", ADMIN)
                        .content(body("12345", nitUnico(), 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ---------------- HU9 ----------------

    @Test
    void listarRestaurantes_ordenAlfabeticoYSoloNombreYLogo() throws Exception {
        guardarRestaurante("Zona Pizza");
        guardarRestaurante("Arepas Don Juan");
        guardarRestaurante("Burger Town");

        String json = mvc.perform(get("/api/v1/restaurantes").header("Authorization", CLIENTE).param("pagina", "0").param("tamanio", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].nombre").exists())
                .andExpect(jsonPath("$.contenido[0].urlLogo").exists())
                .andExpect(jsonPath("$.contenido[0].nit").doesNotExist())
                .andExpect(jsonPath("$.contenido[0].direccion").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<String> nombres = JsonPath.read(json, "$.contenido[*].nombre");
        List<String> ordenados = new ArrayList<>(nombres);
        ordenados.sort(String::compareTo);
        assertEquals(ordenados, nombres);
    }

    @Test
    void listarRestaurantes_respetaElTamanioDePagina() throws Exception {
        guardarRestaurante("Arepas Don Juan");
        guardarRestaurante("Burger Town");

        mvc.perform(get("/api/v1/restaurantes").header("Authorization", CLIENTE).param("pagina", "0").param("tamanio", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(2));
    }

    @Test
    void listarRestaurantes_tamanioInvalido_devuelve400() throws Exception {
        mvc.perform(get("/api/v1/restaurantes").header("Authorization", CLIENTE).param("pagina", "0").param("tamanio", "0"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- HU5: seguridad ----------------

    @Test
    void crearRestaurante_sinToken_devuelve401() throws Exception {
        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Comidas El Sabor", nitUnico(), 1)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearRestaurante_conRolPropietario_devuelve403() throws Exception {
        mvc.perform(post("/api/v1/restaurantes").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TokenDePrueba.bearer(1, "PROPIETARIO"))
                        .content(body("Comidas El Sabor", nitUnico(), 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarRestaurantes_sinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/v1/restaurantes"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------- Consulta por id (la usa usuarios-ms en la HU6) ----------------

    @Test
    void obtenerRestaurante_conToken_devuelve200ConSuPropietario() throws Exception {
        RestauranteEntity r = new RestauranteEntity();
        r.setNombre("Restaurante Consulta");
        r.setNit(nitUnico());
        r.setDireccion("Calle 1 # 2-3");
        r.setTelefono("+573001234567");
        r.setUrlLogo("http://imagenes.com/logo.png");
        r.setIdPropietario(7L);
        Long id = restauranteRepository.save(r).getId();

        mvc.perform(get("/api/v1/restaurantes/" + id).header("Authorization", TokenDePrueba.bearer(7, "PROPIETARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPropietario").value(7));
    }

    @Test
    void obtenerRestaurante_inexistente_devuelve404() throws Exception {
        mvc.perform(get("/api/v1/restaurantes/999999").header("Authorization", TokenDePrueba.bearer(7, "PROPIETARIO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerRestaurante_sinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/v1/restaurantes/1"))
                .andExpect(status().isUnauthorized());
    }
}
