
/**
 * Llamadas al microservicio usuarios-ms.
 * HU5: Inicio de sesión.
 * HU8: Registro de clientes.
 */

import { apiFetch } from "./http.js";

/**
 * HU5: Iniciar sesión.
 * Permite ingresar con correo y contraseña.
 */
export function iniciarSesion(correo, clave) {
    return apiFetch("usuarios", "/api/v1/usuarios/login", {
        metodo: "POST",
        cuerpo: { correo, clave },
        conToken: false,
    });
}

/**
 * HU8: Registrar cliente.
 * Permite crear una cuenta sin iniciar sesión.
 */
export function registrarCliente(datos) {
    return apiFetch("usuarios", "/api/v1/usuarios/cliente", {
        metodo: "POST",
        cuerpo: datos,
        conToken: false,
    });
}
