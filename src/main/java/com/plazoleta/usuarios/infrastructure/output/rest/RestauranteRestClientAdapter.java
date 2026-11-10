package com.plazoleta.usuarios.infrastructure.output.rest;

import com.plazoleta.usuarios.domain.spi.RestauranteConsultaPort;
import com.plazoleta.usuarios.infrastructure.output.rest.dto.RestauranteRespuestaDto;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/** Adaptador de SALIDA: consulta un restaurante en Plazoleta por HTTP (GET /api/v1/restaurantes/{id}). */
public class RestauranteRestClientAdapter implements RestauranteConsultaPort {

    private final RestClient restClient;

    public RestauranteRestClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<Long> obtenerIdPropietario(Long idRestaurante) {
        try {
            RestauranteRespuestaDto restaurante = restClient.get()
                    .uri("/api/v1/restaurantes/{id}", idRestaurante)
                    .retrieve()
                    .body(RestauranteRespuestaDto.class);
            return Optional.ofNullable(restaurante).map(RestauranteRespuestaDto::idPropietario);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo consultar el microservicio Plazoleta. Verifica que esté encendido.", e);
        }
    }
}
