package com.plazoleta.usuarios.infrastructure.input.rest.dto;

public record LoginRequest(
        String correo,
        String clave
) {
}