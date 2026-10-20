package com.plazoleta.usuarios.infrastructure.input.rest.dto;

public record LoginResponse(
        String token,
        String tipo,
        String rol
) {
}