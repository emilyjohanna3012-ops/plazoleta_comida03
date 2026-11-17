
/**
 * http.js — la ÚNICA función que habla con el backend.
 * Pone el token, convierte la respuesta a JSON
 * y transforma los errores del backend en ApiError.
 */

import { API } from "../config.js";
import { obtenerSesion, cerrarSesion } from "../auth.js";

export class ApiError extends Error {
    constructor(estado, mensaje) {
        super(mensaje);
        this.name = "ApiError";
        this.estado = estado;
    }
}

// ==========================================
// MICROSERVICIOS
// ==========================================

const NOMBRES_SERVICIO = {
    usuarios: "usuarios-ms (8081)",
    plazoleta: "plazoleta-ms (8082)",
    trazabilidad: "trazabilidad-ms (8083)"
};

/**
 * Función para comunicarse con el backend.
 *
 * @param {"usuarios"|"plazoleta"|"trazabilidad"} servicio
 * @param {string} ruta
 * @param {{metodo?: string, cuerpo?: object, conToken?: boolean}} opciones
 */

export async function apiFetch(
    servicio,
    ruta,
    { metodo = "GET", cuerpo, conToken = true } = {}
) {
    const encabezados = {
        Accept: "application/json"
    };

    if (cuerpo !== undefined) {
        encabezados["Content-Type"] = "application/json";
    }

    // ==========================================
    // AGREGAR TOKEN
    // ==========================================

    if (conToken) {
        const sesion = obtenerSesion();

        if (!sesion) {
            redirigirAlLogin();

            throw new ApiError(
                401,
                "Tu sesión terminó. Inicia sesión de nuevo."
            );
        }

        encabezados.Authorization = `Bearer ${sesion.token}`;
    }

    // ==========================================
    // REALIZAR PETICIÓN
    // ==========================================

    let respuesta;

    try {
        respuesta = await fetch(API[servicio] + ruta, {
            method: metodo,
            headers: encabezados,
            body:
                cuerpo !== undefined
                    ? JSON.stringify(cuerpo)
                    : undefined
        });

    } catch {
        throw new ApiError(
            0,
            `No se pudo conectar con ${NOMBRES_SERVICIO[servicio]}. ¿Está encendido?`
        );
    }

    // ==========================================
    // LEER RESPUESTA
    // ==========================================

    const texto = await respuesta.text();

    let datos = null;

    if (texto) {
        try {
            datos = JSON.parse(texto);
        } catch {
            datos = null;
        }
    }

    // ==========================================
    // MANEJAR ERRORES
    // ==========================================

    if (!respuesta.ok) {

        if (respuesta.status === 401 && conToken) {
            redirigirAlLogin();
        }

        const mensaje =
            datos?.mensaje ||
            mensajePorEstado(respuesta.status);

        throw new ApiError(
            respuesta.status,
            mensaje
        );
    }

    return datos;
}

// ==========================================
// MENSAJES DE ERROR
// ==========================================

function mensajePorEstado(estado) {

    if (estado === 401) {
        return "Debes iniciar sesión.";
    }

    if (estado === 403) {
        return "Tu rol no tiene permiso para esta operación.";
    }

    if (estado === 404) {
        return "No se encontró lo que buscabas.";
    }

    if (estado === 409) {
        return "Alguien más modificó este registro. Actualiza e intenta de nuevo.";
    }

    if (estado >= 500) {
        return "El servidor tuvo un problema. Intenta de nuevo en un momento.";
    }

    return "La petición no es válida.";
}

// ==========================================
// REDIRIGIR AL LOGIN
// ==========================================

function redirigirAlLogin() {

    cerrarSesion();

    if (
        !location.pathname.endsWith("/index.html") &&
        !location.pathname.endsWith("/")
    ) {
        location.href = "index.html?sesion=expirada";
    }
}
