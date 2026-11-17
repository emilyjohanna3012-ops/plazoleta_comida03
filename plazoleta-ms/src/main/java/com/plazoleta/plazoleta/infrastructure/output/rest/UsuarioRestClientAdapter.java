package com.plazoleta.plazoleta.infrastructure.output.rest;

import com.plazoleta.plazoleta.domain.spi.UsuarioConsultaPort;
import com.plazoleta.plazoleta.infrastructure.output.rest.dto.UsuarioRespuestaDto;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/** Adaptador de SALIDA: consulta al microservicio de Usuarios por HTTP (GET /api/v1/usuarios/{id}). */
public class UsuarioRestClientAdapter implements UsuarioConsultaPort {

    private final RestClient restClient;

    public UsuarioRestClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<String> obtenerRol(Long idUsuario) {
        try {
            UsuarioRespuestaDto usuario = restClient.get()
                    .uri("/api/v1/usuarios/{id}", idUsuario)
                    .retrieve()
                    .body(UsuarioRespuestaDto.class);
            return Optional.ofNullable(usuario).map(UsuarioRespuestaDto::rol);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo consultar el microservicio de Usuarios. Verifica que esté encendido.", e);
        }
    }
}
