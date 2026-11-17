
/**
 * config.js — dónde están los microservicios.
 * Si cambian un puerto en el backend, solo hay que cambiarlo aquí.
 */
export const API = {
    usuarios: "http://localhost:8081",
    plazoleta: "http://localhost:8082",
    trazabilidad: "http://localhost:8083",
};

/** Página de inicio de cada rol. */
export const ROLES = {
    ADMINISTRADOR: { nombre: "Administrador", pagina: "admin.html" },
    PROPIETARIO: { nombre: "Propietario", pagina: "propietario.html" },
    EMPLEADO: { nombre: "Empleado", pagina: "empleado.html" },
    CLIENTE: { nombre: "Cliente", pagina: "cliente.html" },
};

/** Enlaces del menú superior según el rol. */
export const MENU = {
    ADMINISTRADOR: [
        { texto: "Restaurantes", href: "admin.html", icono: "tienda" }
    ],
    PROPIETARIO: [
        { texto: "Mi restaurante", href: "propietario.html", icono: "tienda" }
    ],
    EMPLEADO: [
        { texto: "Pedidos", href: "empleado.html", icono: "cocina" }
    ],
    CLIENTE: [
        { texto: "Restaurantes", href: "cliente.html", icono: "tienda" }
    ],
};
