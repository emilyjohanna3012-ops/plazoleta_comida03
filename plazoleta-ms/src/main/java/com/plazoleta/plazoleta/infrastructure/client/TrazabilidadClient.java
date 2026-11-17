
package com.plazoleta.plazoleta.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Component
public class TrazabilidadClient {

    private final RestClient restClient;

    public TrazabilidadClient(
            @Value("${trazabilidad.url:http://localhost:8083}") String url) {

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .build();
    }

    public void registrarPedido(
            Long idPedido,
            Long idCliente,
            Long idRestaurante,
            LocalDateTime fecha,
            String token) {

        registrarCambioEstado(
                idPedido,
                idCliente,
                idRestaurante,
                null,
                "PENDIENTE",
                null,
                fecha,
                token
        );
    }

    public void registrarCambioEstado(
            Long idPedido,
            Long idCliente,
            Long idRestaurante,
            String estadoAnterior,
            String estadoNuevo,
            Long idEmpleado,
            LocalDateTime fecha,
            String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "No se recibió un token válido para registrar la trazabilidad"
            );
        }

        var datos = new RegistroTraza(
                idPedido,
                idCliente,
                idRestaurante,
                estadoAnterior,
                estadoNuevo,
                idEmpleado,
                fecha
        );

        restClient.post()
                .uri("/api/v1/trazas")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    private record RegistroTraza(
            Long idPedido,
            Long idCliente,
            Long idRestaurante,
            String estadoAnterior,
            String estadoNuevo,
            Long idEmpleado,
            LocalDateTime fecha) {
    }
}
