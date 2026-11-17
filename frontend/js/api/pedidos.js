
/**
 * API DE PEDIDOS
 *
 * HU11: Crear pedidos
 * HU12: Consultar pedidos del restaurante
 * HU13: Asignar pedidos
 * HU14: Marcar pedidos como listos
 * HU15: Entregar pedidos
 * HU16: Consultar mis pedidos
 * HU17: Consultar historial de pedidos
 */

import { apiFetch } from "./http.js";

// HU11 - CREAR PEDIDO

export function crearPedido(datos) {
    return apiFetch(
        "plazoleta",
        "/api/v1/pedidos",
        {
            metodo: "POST",
            cuerpo: datos
        }
    );
}

// HU12 - CONSULTAR PEDIDOS POR ESTADO

export function listarPedidosPorEstado(
    estado,
    pagina = 0,
    tamanio = 10
) {
    const parametros = new URLSearchParams({
        estado,
        pagina,
        tamanio
    });

    return apiFetch(
        "plazoleta",
        `/api/v1/pedidos/empleado?${parametros}`
    );
}

// HU13 - ASIGNARSE UN PEDIDO

export function asignarPedido(idPedido) {
    return apiFetch(
        "plazoleta",
        `/api/v1/pedidos/${encodeURIComponent(idPedido)}/asignar`,
        {
            metodo: "PATCH"
        }
    );
}

// HU14 - MARCAR PEDIDO COMO LISTO

export function marcarPedidoListo(idPedido) {
    return apiFetch(
        "plazoleta",
        `/api/v1/pedidos/${encodeURIComponent(idPedido)}/listo`,
        {
            metodo: "PATCH"
        }
    );
}

// HU15 - ENTREGAR PEDIDO

export function entregarPedido(idPedido, pin) {
    return apiFetch(
        "plazoleta",
        `/api/v1/pedidos/${encodeURIComponent(idPedido)}/entregar`,
        {
            metodo: "PATCH",
            cuerpo: { pin }
        }
    );
}

// HU16 - CONSULTAR MIS PEDIDOS
// Nombre corregido para que funcione con cliente.js

export function consultarMisPedidos() {
    return apiFetch(
        "plazoleta",
        "/api/v1/pedidos"
    );
}

// Alias para conservar compatibilidad
export const listarMisPedidos = consultarMisPedidos;

// HU17 - CONSULTAR HISTORIAL DEL PEDIDO

export function consultarHistorialPedido(idPedido) {
    return apiFetch(
        "trazabilidad",
        `/api/v1/trazas/pedido/${encodeURIComponent(idPedido)}`
    );
}
