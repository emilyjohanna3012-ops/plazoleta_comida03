package com.plazoleta.plazoleta.infrastructure.output.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Cuando Plazoleta llama a otro microservicio, le reenvía el mismo token con el que llegó la petición.
 * Así usuarios-ms sabe quién está preguntando.
 */
public class PropagarTokenInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            String autorizacion = atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (autorizacion != null) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, autorizacion);
            }
        }
        return execution.execute(request, body);
    }
}
