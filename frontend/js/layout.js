/**
 * layout.js — dibuja el encabezado (marca, menú según el rol, usuario y botón salir)
 * y el pie de página. Así todas las páginas privadas se ven iguales sin copiar HTML.
 *
 * Uso en cada página privada:
 *   const sesion = protegerPagina("EMPLEADO");
 *   montarLayout(sesion);
 */
import { MENU, ROLES } from "./config.js";
import { cerrarSesion } from "./auth.js";
import { icono, escaparHtml } from "./ui.js";

export function montarLayout(sesion) {
    const encabezado = document.getElementById("encabezado");
    const pie = document.getElementById("pie");
    const paginaActual = location.pathname.split("/").pop() || "index.html";

    if (encabezado) {
        const enlaces = (MENU[sesion.rol] ?? []).map((enlace) => `
            <a href="${enlace.href}" class="${enlace.href === paginaActual ? "activo" : ""}"
               ${enlace.href === paginaActual ? 'aria-current="page"' : ""} title="${escaparHtml(enlace.texto)}">
                ${icono(enlace.icono)}<span>${escaparHtml(enlace.texto)}</span>
            </a>`).join("");

        encabezado.className = "encabezado";
        encabezado.innerHTML = `
            <div class="contenedor">
                <a class="marca" href="${ROLES[sesion.rol]?.pagina ?? "index.html"}">
                    <span class="marca__logo">${icono("logo")}</span>
                    <span class="marca__texto">
                        <span class="marca__nombre">Plazoleta</span>
                        <span class="marca__sub">de comidas</span>
                    </span>
                </a>
                <nav class="navegacion" aria-label="Menú principal">${enlaces}</nav>
                <div class="usuario-menu">
                    <div class="usuario-menu__datos">
                        <span class="usuario-menu__correo">${escaparHtml(sesion.correo)}</span>
                        <span class="usuario-menu__rol">${escaparHtml(ROLES[sesion.rol]?.nombre ?? sesion.rol)}</span>
                    </div>
                    <span class="usuario-menu__avatar" aria-hidden="true">${escaparHtml((sesion.correo ?? "?").charAt(0))}</span>
                    <button class="btn btn--fantasma btn--icono" type="button" id="btn-salir" title="Cerrar sesión" aria-label="Cerrar sesión">
                        ${icono("salir")}
                    </button>
                </div>
            </div>`;

        encabezado.querySelector("#btn-salir").addEventListener("click", () => {
            cerrarSesion();
            location.href = "index.html";
        });
    }

    if (pie) {
        pie.className = "pie";
        pie.innerHTML = `
            <div class="contenedor">
                <span>Plazoleta de comidas · Reto de microservicios</span>
                <span>Carlos · Emily · Melissa</span>
            </div>`;
    }
}
