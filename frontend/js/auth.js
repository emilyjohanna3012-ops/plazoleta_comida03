/**
 * auth.js — sesión del usuario.
 * El token JWT lo genera usuarios-ms al iniciar sesión (HU5). Aquí solo se GUARDA y se LEE:
 * el frontend no valida la firma (eso lo hace el backend en cada petición).
 * Claims del token: sub (correo), rol, id y, para empleados, idRestaurante.
 */
import { ROLES } from "./config.js";

const CLAVE = "plazoleta.token";

export function guardarSesion(token) {
    try { localStorage.setItem(CLAVE, token); } catch { /* almacenamiento bloqueado */ }
}

export function cerrarSesion() {
    try { localStorage.removeItem(CLAVE); } catch { /* nada que borrar */ }
}

/** Devuelve {token, id, rol, correo, idRestaurante, expira} o null si no hay sesión o ya venció. */
export function obtenerSesion() {
    let token = null;
    try { token = localStorage.getItem(CLAVE); } catch { return null; }
    if (!token) return null;

    const datos = leerToken(token);
    if (!datos || (datos.exp && datos.exp * 1000 <= Date.now())) {
        cerrarSesion();
        return null;
    }
    return {
        token,
        id: datos.id ?? null,
        rol: datos.rol,
        correo: datos.sub,
        idRestaurante: datos.idRestaurante ?? null,
        expira: datos.exp ? new Date(datos.exp * 1000) : null,
    };
}

/** Página a la que va cada rol después del login. */
export function paginaDeInicio(rol) {
    return ROLES[rol]?.pagina ?? "index.html";
}

/**
 * Llamar al inicio de cada página privada.
 * Sin sesión → login. Con otro rol → su propia página. Si todo está bien, devuelve la sesión.
 */
export function protegerPagina(...rolesPermitidos) {
    const sesion = obtenerSesion();
    if (!sesion) {
        location.replace("index.html");
        return null;
    }
    if (rolesPermitidos.length && !rolesPermitidos.includes(sesion.rol)) {
        location.replace(paginaDeInicio(sesion.rol));
        return null;
    }
    return sesion;
}

/** Decodifica la parte central (payload) del JWT. */
function leerToken(token) {
    try {
        const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        const json = decodeURIComponent(atob(base64).split("")
            .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0")).join(""));
        return JSON.parse(json);
    } catch {
        return null;
    }
}
