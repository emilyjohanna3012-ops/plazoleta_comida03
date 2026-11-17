/**
 * empleado.html — Panel de cocina del EMPLEADO. Responsable: Carlos.
 *
 * HU12: ver los pedidos de su restaurante filtrados por estado, paginados (del más antiguo al más nuevo).
 * HU13: asignarse un pedido PENDIENTE → EN_PREPARACION.
 * HU14: marcar LISTO un pedido que él prepara → plazoleta-ms envía el PIN por SMS al cliente.
 * HU15: entregar un pedido LISTO escribiendo el PIN que dice el cliente.
 *
 * Las reglas (quién puede hacer qué) las valida el backend; aquí solo se muestran los botones que tienen sentido.
 */
import { protegerPagina } from "../auth.js";
import { montarLayout } from "../layout.js";
import { listarPedidosPorEstado, asignarPedido, marcarPedidoListo, entregarPedido } from "../api/pedidos.js";
import { obtenerRestaurante } from "../api/restaurantes.js";
import {
    icono, escaparHtml, mostrarAviso, abrirModal, confirmar, ponerCargando,
    formatearPrecio, formatearFecha, formatearHora, tiempoTranscurrido,
} from "../ui.js";

/* ------------------------------------------------------------------ */
/* Configuración                                                       */
/* ------------------------------------------------------------------ */
const ESTADOS = [
    { valor: "PENDIENTE", texto: "Pendientes", insignia: "Pendiente", icono: "campana", vacio: "No hay pedidos esperando. Los nuevos aparecen aquí." },
    { valor: "EN_PREPARACION", texto: "En preparación", insignia: "En preparación", icono: "llama", vacio: "Nadie está preparando pedidos en este momento." },
    { valor: "LISTO", texto: "Listos", insignia: "Listo", icono: "bolsa", vacio: "No hay pedidos esperando a que el cliente los reclame." },
    { valor: "ENTREGADO", texto: "Entregados", insignia: "Entregado", icono: "entregado", vacio: "Todavía no se ha entregado ningún pedido." },
    { valor: "CANCELADO", texto: "Cancelados", insignia: "Cancelado", icono: "prohibido", vacio: "No hay pedidos cancelados." },
];
const TAMANIO_PAGINA = 9;                 // 3 filas de 3 tarjetas en escritorio (el backend permite hasta 50)
const ACTUALIZAR_CADA_MS = 30_000;        // revisa si llegaron pedidos nuevos cada 30 s
const MINUTOS_DEMORA = 15;                // un pendiente con más de 15 min se resalta en rojo

/* ------------------------------------------------------------------ */
/* Estado de la página                                                 */
/* ------------------------------------------------------------------ */
const estado = {
    filtro: "PENDIENTE",
    pagina: 0,
    totalPaginas: 0,
    ultimaActualizacion: null,
    cargando: false,
};

const elementos = {
    pestanas: document.getElementById("pestanas"),
    lista: document.getElementById("lista-pedidos"),
    paginacion: document.getElementById("paginacion"),
    textoPagina: document.getElementById("texto-pagina"),
    anterior: document.getElementById("btn-anterior"),
    siguiente: document.getElementById("btn-siguiente"),
    actualizar: document.getElementById("btn-actualizar"),
    actualizado: document.getElementById("actualizado"),
    nombreRestaurante: document.getElementById("nombre-restaurante"),
    logoRestaurante: document.getElementById("logo-restaurante"),
};

/* ------------------------------------------------------------------ */
/* Inicio                                                              */
/* ------------------------------------------------------------------ */
const sesion = protegerPagina("EMPLEADO");
if (sesion) iniciar();

function iniciar() {
    montarLayout(sesion);
    document.querySelectorAll("[data-icono]").forEach((el) => { el.innerHTML = icono(el.dataset.icono); });
    elementos.logoRestaurante.innerHTML = icono("tienda");

    if (!sesion.idRestaurante) {
        elementos.nombreRestaurante.textContent = "Sin restaurante";
        mostrarMensajeLista("alerta", "Tu usuario no está asociado a ningún restaurante",
            "Pídele al propietario que revise tu cuenta de empleado.");
        elementos.actualizar.disabled = true;
        return;
    }

    dibujarPestanas();
    cargarRestaurante();
    cargarTodo();

    elementos.pestanas.addEventListener("click", alElegirPestana);
    elementos.pestanas.addEventListener("keydown", alMoverConTeclado);
    elementos.lista.addEventListener("click", alHacerClicEnPedido);
    elementos.actualizar.addEventListener("click", () => cargarTodo({ conBoton: true }));
    elementos.anterior.addEventListener("click", () => irAPagina(estado.pagina - 1));
    elementos.siguiente.addEventListener("click", () => irAPagina(estado.pagina + 1));

    // Actualización automática (solo si la pestaña del navegador está visible y no hay un modal abierto)
    setInterval(() => {
        if (document.visibilityState === "visible" && !document.querySelector(".modal-fondo")) {
            cargarTodo({ silencioso: true });
        }
    }, ACTUALIZAR_CADA_MS);
    setInterval(pintarHoraActualizacion, 15_000);
}

/* ------------------------------------------------------------------ */
/* Restaurante del empleado (para el encabezado)                       */
/* ------------------------------------------------------------------ */
async function cargarRestaurante() {
    try {
        const restaurante = await obtenerRestaurante(sesion.idRestaurante);
        elementos.nombreRestaurante.textContent = restaurante.nombre;
        document.title = `Pedidos · ${restaurante.nombre}`;
        if (restaurante.urlLogo) {
            const imagen = new Image();
            imagen.alt = `Logo de ${restaurante.nombre}`;
            imagen.onload = () => elementos.logoRestaurante.replaceChildren(imagen);  // si la URL falla, se queda el ícono
            imagen.src = restaurante.urlLogo;
        }
    } catch {
        elementos.nombreRestaurante.textContent = "Pedidos del restaurante";
    }
}

/* ------------------------------------------------------------------ */
/* Pestañas (HU12: filtro por estado)                                  */
/* ------------------------------------------------------------------ */
function dibujarPestanas() {
    elementos.pestanas.innerHTML = ESTADOS.map((e) => `
        <button class="pestana" type="button" role="tab" id="pestana-${e.valor}" data-estado="${e.valor}"
                aria-selected="${e.valor === estado.filtro}" aria-controls="lista-pedidos"
                tabindex="${e.valor === estado.filtro ? 0 : -1}">
            ${escaparHtml(e.texto)}
            <span class="pestana__conteo" data-conteo="${e.valor}">·</span>
        </button>`).join("");
}

function alElegirPestana(evento) {
    const boton = evento.target.closest(".pestana");
    if (!boton || boton.dataset.estado === estado.filtro) return;
    seleccionarPestana(boton.dataset.estado);
}

function alMoverConTeclado(evento) {
    if (!["ArrowRight", "ArrowLeft"].includes(evento.key)) return;
    const indice = ESTADOS.findIndex((e) => e.valor === estado.filtro);
    const siguiente = (indice + (evento.key === "ArrowRight" ? 1 : -1) + ESTADOS.length) % ESTADOS.length;
    seleccionarPestana(ESTADOS[siguiente].valor);
    document.getElementById(`pestana-${ESTADOS[siguiente].valor}`).focus();
}

function seleccionarPestana(valor) {
    estado.filtro = valor;
    estado.pagina = 0;
    elementos.pestanas.querySelectorAll(".pestana").forEach((p) => {
        const activa = p.dataset.estado === valor;
        p.setAttribute("aria-selected", activa);
        p.tabIndex = activa ? 0 : -1;
    });
    cargarPedidos();
}

/** Cuántos pedidos hay en cada estado: se pide 1 elemento por estado y se usa totalElementos. */
async function cargarConteos() {
    const resultados = await Promise.allSettled(ESTADOS.map((e) => listarPedidosPorEstado(e.valor, 0, 1)));
    resultados.forEach((resultado, i) => {
        const etiqueta = elementos.pestanas.querySelector(`[data-conteo="${ESTADOS[i].valor}"]`);
        if (!etiqueta) return;
        const total = resultado.status === "fulfilled" ? resultado.value.totalElementos : null;
        etiqueta.textContent = total ?? "–";
        // Los pendientes se resaltan para que el empleado los vea aunque esté en otra pestaña
        etiqueta.classList.toggle("pestana__conteo--alerta", ESTADOS[i].valor === "PENDIENTE" && total > 0);
    });
}

/* ------------------------------------------------------------------ */
/* Lista de pedidos                                                    */
/* ------------------------------------------------------------------ */
async function cargarTodo({ conBoton = false, silencioso = false } = {}) {
    if (conBoton) ponerCargando(elementos.actualizar, true);
    await Promise.all([cargarPedidos({ silencioso }), cargarConteos()]);
    if (conBoton) ponerCargando(elementos.actualizar, false);
}

async function cargarPedidos({ silencioso = false } = {}) {
    if (estado.cargando && silencioso) return;
    estado.cargando = true;
    const filtroPedido = estado.filtro;
    if (!silencioso) mostrarEsqueletos();
    elementos.lista.setAttribute("aria-busy", "true");

    try {
        const pagina = await listarPedidosPorEstado(estado.filtro, estado.pagina, TAMANIO_PAGINA);
        if (filtroPedido !== estado.filtro) return;   // el usuario cambió de pestaña mientras cargaba

        // Si la página quedó vacía (por ejemplo, se entregó el último pedido), volver a la anterior
        if (pagina.contenido.length === 0 && estado.pagina > 0) {
            estado.pagina = Math.max(0, pagina.totalPaginas - 1);
            estado.cargando = false;
            return await cargarPedidos({ silencioso });
        }

        estado.totalPaginas = pagina.totalPaginas;
        pintarPedidos(pagina.contenido);
        pintarPaginacion();
        estado.ultimaActualizacion = new Date();
        pintarHoraActualizacion();
    } catch (error) {
        if (!silencioso) {
            mostrarMensajeLista("alerta", "No se pudieron cargar los pedidos", error.message);
            elementos.paginacion.hidden = true;
        }
    } finally {
        estado.cargando = false;
        elementos.lista.setAttribute("aria-busy", "false");
    }
}

function pintarPedidos(pedidos) {
    if (pedidos.length === 0) {
        const info = ESTADOS.find((e) => e.valor === estado.filtro);
        mostrarMensajeLista(info.icono, `Sin pedidos ${info.texto.toLowerCase()}`, info.vacio);
        return;
    }
    elementos.lista.innerHTML = pedidos.map((pedido, i) => tarjetaPedido(pedido, i)).join("");
}

function tarjetaPedido(pedido, posicion) {
    const info = ESTADOS.find((e) => e.valor === pedido.estado);
    const minutos = (Date.now() - new Date(pedido.fechaCreacion).getTime()) / 60000;
    const demorado = pedido.estado === "PENDIENTE" && minutos > MINUTOS_DEMORA;

    const platos = (pedido.platos ?? []).map((p) => `
        <li class="pedido__plato">
            <span class="pedido__cantidad">${escaparHtml(p.cantidad)}×</span>
            <span class="pedido__nombre">${escaparHtml(p.nombre)}</span>
            <span class="pedido__subtotal">${formatearPrecio(p.subtotal)}</span>
        </li>`).join("");

    return `
        <article class="tarjeta pedido pedido--${escaparHtml(pedido.estado)} ${demorado ? "pedido--demorado" : ""}"
                 style="animation-delay:${posicion * 40}ms" aria-label="Pedido ${escaparHtml(pedido.id)}">
            <header class="pedido__cabecera">
                <div>
                    <span class="pedido__numero">Pedido #${escaparHtml(pedido.id)}</span>
                    <span class="pedido__hora" title="${escaparHtml(formatearFecha(pedido.fechaCreacion))}">
                        ${icono("reloj")} ${escaparHtml(tiempoTranscurrido(pedido.fechaCreacion))} · ${escaparHtml(formatearHora(pedido.fechaCreacion))}
                    </span>
                </div>
                <span class="insignia insignia--${escaparHtml(pedido.estado)}">${escaparHtml(info?.insignia ?? pedido.estado)}</span>
            </header>

            <ul class="pedido__platos">${platos}</ul>

            <div class="pedido__datos">
                <span class="pedido__dato">${icono("usuario")} Cliente <strong>#${escaparHtml(pedido.idCliente)}</strong></span>
                ${datoEmpleado(pedido)}
            </div>

            <footer class="pedido__pie">
                <div class="pedido__total"><small>Total</small><strong>${formatearPrecio(pedido.total)}</strong></div>
                ${accionPedido(pedido)}
            </footer>
        </article>`;
}

function datoEmpleado(pedido) {
    if (!pedido.idEmpleadoAsignado) return "";
    const esMio = pedido.idEmpleadoAsignado === sesion.id;
    return `<span class="pedido__dato ${esMio ? "pedido__dato--mio" : ""}">${icono("cocina")}
        Chef <strong>${esMio ? "Tú" : "#" + escaparHtml(pedido.idEmpleadoAsignado)}</strong></span>`;
}

/** El botón que corresponde según el estado (las mismas reglas que valida el backend). */
function accionPedido(pedido) {
    const id = escaparHtml(pedido.id);
    switch (pedido.estado) {
        case "PENDIENTE":
            return `<button class="btn btn--primario" type="button" data-accion="asignar" data-id="${id}">
                        ${icono("cocina")}<span>Asignarme</span></button>`;
        case "EN_PREPARACION":
            if (pedido.idEmpleadoAsignado === sesion.id) {
                return `<button class="btn btn--exito" type="button" data-accion="listo" data-id="${id}">
                            ${icono("campana")}<span>Marcar listo</span></button>`;
            }
            return `<span class="pedido__nota">${icono("llama")}Lo prepara otro chef</span>`;
        case "LISTO":
            return `<button class="btn btn--primario" type="button" data-accion="entregar" data-id="${id}">
                        ${icono("llave")}<span>Entregar</span></button>`;
        case "ENTREGADO":
            return `<span class="pedido__nota">${icono("checkCirculo")}Entregado</span>`;
        default:
            return `<span class="pedido__nota">${icono("prohibido")}Cancelado por el cliente</span>`;
    }
}

function mostrarEsqueletos() {
    elementos.lista.innerHTML = Array.from({ length: 3 }, () => `
        <div class="tarjeta pedido-esqueleto" aria-hidden="true">
            <span class="esqueleto"></span><span class="esqueleto"></span>
            <span class="esqueleto"></span><span class="esqueleto"></span>
        </div>`).join("");
}

function mostrarMensajeLista(nombreIcono, titulo, texto) {
    elementos.lista.innerHTML = `
        <div class="vacio">
            <div class="vacio__icono">${icono(nombreIcono)}</div>
            <h3>${escaparHtml(titulo)}</h3>
            <p>${escaparHtml(texto)}</p>
        </div>`;
    elementos.paginacion.hidden = true;
}

/* ------------------------------------------------------------------ */
/* Paginación                                                          */
/* ------------------------------------------------------------------ */
function pintarPaginacion() {
    const hayVarias = estado.totalPaginas > 1;
    elementos.paginacion.hidden = !hayVarias;
    if (!hayVarias) return;
    elementos.textoPagina.textContent = `Página ${estado.pagina + 1} de ${estado.totalPaginas}`;
    elementos.anterior.disabled = estado.pagina === 0;
    elementos.siguiente.disabled = estado.pagina >= estado.totalPaginas - 1;
}

function irAPagina(numero) {
    if (numero < 0 || numero >= estado.totalPaginas) return;
    estado.pagina = numero;
    cargarPedidos();
    elementos.pestanas.scrollIntoView({ behavior: "smooth", block: "start" });
}

function pintarHoraActualizacion() {
    if (!estado.ultimaActualizacion) return;
    elementos.actualizado.textContent = `Actualizado ${tiempoTranscurrido(estado.ultimaActualizacion.toISOString())}`;
}

/* ------------------------------------------------------------------ */
/* Acciones sobre un pedido (HU13, HU14, HU15)                         */
/* ------------------------------------------------------------------ */
function alHacerClicEnPedido(evento) {
    const boton = evento.target.closest("[data-accion]");
    if (!boton) return;
    const idPedido = Number(boton.dataset.id);
    if (boton.dataset.accion === "asignar") asignar(idPedido, boton);
    if (boton.dataset.accion === "listo") marcarListo(idPedido, boton);
    if (boton.dataset.accion === "entregar") entregar(idPedido);
}

/** HU13 */
async function asignar(idPedido, boton) {
    ponerCargando(boton, true);
    try {
        await asignarPedido(idPedido);
        mostrarAviso("exito", `Te asignaste el pedido #${idPedido}. Ya está en preparación.`);
    } catch (error) {
        // 400/409 típico: otro empleado se lo asignó primero
        mostrarAviso("error", error.message);
    } finally {
        ponerCargando(boton, false);
        cargarTodo({ silencioso: true });
    }
}

/** HU14 */
async function marcarListo(idPedido, boton) {
    const confirmado = await confirmar({
        icono: "campana",
        titulo: `¿El pedido #${idPedido} está listo?`,
        mensaje: "Le enviaremos un SMS al cliente con su PIN de seguridad para que venga a reclamarlo.",
        textoConfirmar: "Sí, está listo",
        clase: "btn--exito",
    });
    if (!confirmado) return;

    ponerCargando(boton, true);
    try {
        await marcarPedidoListo(idPedido);
        mostrarAviso("exito", `Pedido #${idPedido} listo. El cliente recibirá su PIN por SMS.`);
    } catch (error) {
        // 503: mensajeria-ms no respondió → el pedido NO cambia de estado (decisión del backend)
        mostrarAviso("error", error.message, 7000);
    } finally {
        ponerCargando(boton, false);
        cargarTodo({ silencioso: true });
    }
}

/** HU15: modal donde el empleado escribe el PIN que le dice el cliente. */
function entregar(idPedido) {
    const formulario = document.createElement("form");
    formulario.className = "formulario-pin";
    formulario.noValidate = true;
    formulario.innerHTML = `
        <p>Pídele al cliente el PIN de 6 dígitos que le llegó por SMS.</p>
        <div class="campo">
            <label class="solo-lectores" for="pin">PIN de seguridad</label>
            <input class="entrada entrada-pin" id="pin" name="pin" inputmode="numeric" autocomplete="one-time-code"
                   maxlength="6" placeholder="000000" aria-describedby="error-pin">
            <span class="campo__error" id="error-pin" role="alert"></span>
        </div>`;

    const entradaPin = formulario.querySelector("#pin");
    const errorPin = formulario.querySelector("#error-pin");
    entradaPin.addEventListener("input", () => {
        entradaPin.value = entradaPin.value.replace(/\D/g, "").slice(0, 6);  // solo números
        errorPin.textContent = "";
        entradaPin.removeAttribute("aria-invalid");
    });

    const enviar = async (boton, cerrar) => {
        const pin = entradaPin.value;
        if (!/^\d{6}$/.test(pin)) {
            mostrarErrorPin("El PIN tiene 6 números.");
            return;
        }
        ponerCargando(boton, true);
        try {
            await entregarPedido(idPedido, pin);
            cerrar();
            mostrarAviso("exito", `Pedido #${idPedido} entregado. ¡Buen provecho para el cliente!`);
            cargarTodo({ silencioso: true });
        } catch (error) {
            // Lo más común: "El PIN de seguridad no es correcto" (400). El modal sigue abierto para reintentar.
            mostrarErrorPin(error.message);
            entradaPin.select();
        } finally {
            ponerCargando(boton, false);
        }
    };

    const { cerrar, botones } = abrirModal({
        icono: "llave",
        titulo: `Entregar pedido #${idPedido}`,
        cuerpo: formulario,
        acciones: [
            { texto: "Cancelar", clase: "btn--fantasma", alHacerClic: ({ cerrar }) => cerrar() },
            { texto: "Confirmar entrega", clase: "btn--primario", alHacerClic: ({ boton, cerrar }) => enviar(boton, cerrar) },
        ],
    });
    formulario.addEventListener("submit", (e) => { e.preventDefault(); enviar(botones[1], cerrar); });

    function mostrarErrorPin(mensaje) {
        errorPin.textContent = mensaje;
        entradaPin.setAttribute("aria-invalid", "true");
    }
}
