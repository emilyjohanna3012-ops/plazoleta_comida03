/**
 * ui.js — utilidades de interfaz que comparten todas las páginas:
 * íconos SVG, avisos (toast), modales, botones "cargando" y formato de precios y fechas.
 */

/* ------------------------------------------------------------------ */
/* Íconos: trazos de 24x24 hechos para este proyecto (sin librerías). */
/* ------------------------------------------------------------------ */
const TRAZOS = {
    logo: '<path d="M3 12h18a9 9 0 0 1-18 0Z"/><path d="M8 8c0-1.5 1-1.5 1-3M12 8c0-1.5 1-1.5 1-3M16 8c0-1.5 1-1.5 1-3"/>',
    cocina: '<path d="M6 13.9A4 4 0 0 1 7.5 6a5 5 0 0 1 9 0A4 4 0 0 1 18 13.9V20H6Z"/><path d="M6 17h12"/>',
    tienda: '<path d="M4 10v10h16V10"/><path d="M2.5 10 5 4h14l2.5 6Z"/><path d="M2.5 10a3.2 3.2 0 0 0 6.3 0 3.2 3.2 0 0 0 6.4 0 3.2 3.2 0 0 0 6.3 0"/><path d="M10 20v-5h4v5"/>',
    salir: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
    actualizar: '<path d="M21 12a9 9 0 0 1-15.5 6.2L3 16"/><path d="M3 21v-5h5"/><path d="M3 12a9 9 0 0 1 15.5-6.2L21 8"/><path d="M21 3v5h-5"/>',
    check: '<path d="M20 6 9 17l-5-5"/>',
    checkCirculo: '<circle cx="12" cy="12" r="9"/><path d="m8 12.5 2.7 2.7L16 10"/>',
    cerrar: '<path d="M18 6 6 18M6 6l12 12"/>',
    alerta: '<circle cx="12" cy="12" r="9"/><path d="M12 8v4.5M12 16h.01"/>',
    info: '<circle cx="12" cy="12" r="9"/><path d="M12 16v-4.5M12 8h.01"/>',
    reloj: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    usuario: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    correo: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/>',
    candado: '<rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
    ojo: '<path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>',
    ojoCerrado: '<path d="M10.6 5.1A10 10 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-2.2 3.1M6.6 6.6C3.7 8.4 2 12 2 12s3.6 7 10 7a9.6 9.6 0 0 0 5.4-1.6"/><path d="M14.1 14.2a3 3 0 0 1-4.2-4.3"/><path d="m3 3 18 18"/>',
    bolsa: '<path d="M5 8h14l-1 12H6Z"/><path d="M9 8V6a3 3 0 0 1 6 0v2"/>',
    llama: '<path d="M12 22a7 7 0 0 0 7-7c0-4-3.5-6.5-4.5-10-2 2-2.5 4-2.5 5.5C10.5 9 9.5 8 9.5 6 7 8 5 11 5 15a7 7 0 0 0 7 7Z"/><path d="M12 22a3 3 0 0 0 3-3c0-2-1.5-3-3-5-1.5 2-3 3-3 5a3 3 0 0 0 3 3Z"/>',
    campana: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.9 1.9 0 0 0 3.4 0"/>',
    llave: '<circle cx="7.5" cy="15.5" r="4.5"/><path d="m10.7 12.3 9.3-9.3M17 6l3 3M14 9l2 2"/>',
    izquierda: '<path d="m15 18-6-6 6-6"/>',
    derecha: '<path d="m9 18 6-6-6-6"/>',
    plato: '<path d="M4 3v7a3 3 0 0 0 6 0V3M7 3v18"/><path d="M17 21V3c-2.5 0-4 2.5-4 6s1.5 4 4 4"/>',
    mensaje: '<path d="M21 12a8 8 0 0 1-11.8 7L3 21l2-5.6A8 8 0 1 1 21 12Z"/><path d="M8 12h.01M12 12h.01M16 12h.01"/>',
    entregado: '<path d="M3 7h11v9H3z"/><path d="M14 10h4l3 3v3h-7"/><circle cx="7" cy="17.5" r="1.8"/><circle cx="17" cy="17.5" r="1.8"/>',
    prohibido: '<circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/>',
};

/** Devuelve el SVG de un ícono como texto, para usar dentro de innerHTML. */
export function icono(nombre, clase = "") {
    const trazo = TRAZOS[nombre] ?? TRAZOS.info;
    return `<svg class="${clase}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
        stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${trazo}</svg>`;
}

/* ------------------------------------------------------------------ */
/* Seguridad: cualquier texto que venga del backend se escapa antes   */
/* de meterlo en innerHTML (evita inyectar HTML con un nombre de plato). */
/* ------------------------------------------------------------------ */
export function escaparHtml(valor) {
    return String(valor ?? "")
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

/* ------------------------------------------------------------------ */
/* Avisos flotantes                                                    */
/* ------------------------------------------------------------------ */
const ICONO_AVISO = { exito: "checkCirculo", error: "alerta", info: "info" };

/** mostrarAviso("exito" | "error" | "info", "texto") */
export function mostrarAviso(tipo, mensaje, duracionMs = 4500) {
    let zona = document.querySelector(".avisos");
    if (!zona) {
        zona = document.createElement("div");
        zona.className = "avisos";
        zona.setAttribute("role", "status");
        zona.setAttribute("aria-live", "polite");
        document.body.append(zona);
    }
    const aviso = document.createElement("div");
    aviso.className = `aviso aviso--${tipo}`;
    aviso.innerHTML = `${icono(ICONO_AVISO[tipo] ?? "info")}
        <p class="aviso__texto">${escaparHtml(mensaje)}</p>
        <button class="aviso__cerrar" type="button" aria-label="Cerrar aviso">${icono("cerrar")}</button>`;
    zona.append(aviso);

    const quitar = () => {
        if (!aviso.isConnected) return;
        aviso.classList.add("saliendo");
        aviso.addEventListener("animationend", () => aviso.remove(), { once: true });
    };
    aviso.querySelector(".aviso__cerrar").addEventListener("click", quitar);
    setTimeout(quitar, duracionMs);
}

/* ------------------------------------------------------------------ */
/* Modal                                                               */
/* ------------------------------------------------------------------ */
/**
 * Abre un modal y devuelve funciones para manejarlo.
 * @param {{icono?: string, titulo: string, cuerpo: string|Node, acciones: Array<{texto:string, clase?:string, alHacerClic:Function}>}} opciones
 * El cuerpo puede ser texto o un nodo (por ejemplo un formulario).
 */
export function abrirModal({ icono: nombreIcono = "info", titulo, cuerpo, acciones = [] }) {
    const fondo = document.createElement("div");
    fondo.className = "modal-fondo";
    fondo.innerHTML = `
        <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
            <div class="modal__icono">${icono(nombreIcono)}</div>
            <h2 class="modal__titulo" id="modal-titulo">${escaparHtml(titulo)}</h2>
            <div class="modal__cuerpo"></div>
            <div class="modal__acciones"></div>
        </div>`;
    const zonaCuerpo = fondo.querySelector(".modal__cuerpo");
    if (typeof cuerpo === "string") zonaCuerpo.textContent = cuerpo; else if (cuerpo) zonaCuerpo.append(cuerpo);

    const focoAnterior = document.activeElement;
    const cerrar = () => {
        document.removeEventListener("keydown", alPresionarTecla);
        fondo.classList.add("saliendo");
        fondo.addEventListener("animationend", () => fondo.remove(), { once: true });
        if (focoAnterior instanceof HTMLElement) focoAnterior.focus();
    };
    const alPresionarTecla = (e) => { if (e.key === "Escape") cerrar(); };

    const zonaAcciones = fondo.querySelector(".modal__acciones");
    const botones = acciones.map(({ texto, clase = "", alHacerClic }) => {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.className = `btn ${clase}`;
        boton.innerHTML = `<span>${escaparHtml(texto)}</span>`;
        boton.addEventListener("click", () => alHacerClic?.({ cerrar, boton }));
        zonaAcciones.append(boton);
        return boton;
    });

    fondo.addEventListener("mousedown", (e) => { if (e.target === fondo) cerrar(); });
    document.addEventListener("keydown", alPresionarTecla);
    document.body.append(fondo);
    (fondo.querySelector("input") ?? botones.at(-1))?.focus();
    return { cerrar, fondo, botones };
}

/** Modal de confirmación. Devuelve una promesa: true si el usuario confirmó. */
export function confirmar({ icono: nombreIcono, titulo, mensaje, textoConfirmar = "Confirmar", clase = "btn--primario" }) {
    return new Promise((resolver) => {
        let respondio = false;
        const { fondo } = abrirModal({
            icono: nombreIcono, titulo, cuerpo: mensaje,
            acciones: [
                { texto: "Cancelar", clase: "btn--fantasma", alHacerClic: ({ cerrar }) => { respondio = true; resolver(false); cerrar(); } },
                { texto: textoConfirmar, clase, alHacerClic: ({ cerrar }) => { respondio = true; resolver(true); cerrar(); } },
            ],
        });
        // Si lo cierran con Escape o clic afuera, cuenta como "no"
        new MutationObserver((_, obs) => {
            if (!fondo.isConnected) { obs.disconnect(); if (!respondio) resolver(false); }
        }).observe(document.body, { childList: true });
    });
}

/* ------------------------------------------------------------------ */
/* Botones                                                             */
/* ------------------------------------------------------------------ */
/** Pone o quita el estado "cargando" (spinner) de un botón y lo deshabilita mientras tanto. */
export function ponerCargando(boton, cargando) {
    if (!boton) return;
    boton.classList.toggle("cargando", cargando);
    boton.disabled = cargando;
}

/* ------------------------------------------------------------------ */
/* Formatos                                                            */
/* ------------------------------------------------------------------ */
const formatoPesos = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });

/** 25000 → "$ 25.000" */
export function formatearPrecio(valor) {
    return formatoPesos.format(valor ?? 0);
}

/** "2026-11-12T13:05:00" → "12 nov, 1:05 p. m." */
export function formatearFecha(iso) {
    if (!iso) return "";
    return new Date(iso).toLocaleString("es-CO", { day: "numeric", month: "short", hour: "numeric", minute: "2-digit" });
}

/** "2026-11-12T13:05:00" → "1:05 p. m." (si no es de hoy: "10 nov, 1:05 p. m.") */
export function formatearHora(iso) {
    if (!iso) return "";
    const fecha = new Date(iso);
    const hora = fecha.toLocaleTimeString("es-CO", { hour: "numeric", minute: "2-digit" })
        .replace(/\s/g, "\u00a0");   // espacios que no se parten: "10:14 a. m." nunca queda en dos líneas
    if (fecha.toDateString() === new Date().toDateString()) return hora;
    return `${fecha.getDate()} ${fecha.toLocaleString("es-CO", { month: "short" }).replace(".", "")}, ${hora}`;
}

/** "2026-11-12T13:05:00" → "hace 5 min" */
export function tiempoTranscurrido(iso) {
    if (!iso) return "";
    const segundos = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 1000));
    if (segundos < 60) return "hace un momento";
    const minutos = Math.round(segundos / 60);
    if (minutos < 60) return `hace ${minutos} min`;
    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `hace ${horas} h ${minutos % 60 ? (minutos % 60) + " min" : ""}`.trim();
    const dias = Math.floor(horas / 24);
    return `hace ${dias} ${dias === 1 ? "día" : "días"}`;
}
