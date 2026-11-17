/**
 * admin.html — Administración (rol ADMINISTRADOR). Responsable: Melissa.
 * La base ya está lista: protege la página por rol y pinta el encabezado y el pie.
 * Las llamadas a la API van en js/api/<archivo>.js (una función por endpoint REAL del backend).
 */
import { protegerPagina } from "../auth.js";
import { montarLayout } from "../layout.js";
import { icono } from "../ui.js";

const sesion = protegerPagina("ADMINISTRADOR");
if (sesion) {
    montarLayout(sesion);
    document.querySelectorAll("[data-icono]").forEach((el) => { el.innerHTML = icono(el.dataset.icono); });
    // Melissa: aquí empieza el código de esta página.
}
