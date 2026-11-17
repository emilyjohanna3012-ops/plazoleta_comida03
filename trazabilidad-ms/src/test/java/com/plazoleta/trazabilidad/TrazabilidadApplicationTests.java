package com.plazoleta.trazabilidad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Comprueba que el microservicio arranca (no necesita MongoDB encendido: solo se conecta al guardar o consultar). */
@SpringBootTest
class TrazabilidadApplicationTests {

    @Test
    void contextLoads() {
    }
}
