
/**
 * Página del cliente
 *
 * HU9: Listar restaurantes
 * HU10: Consultar menú
 * HU11: Carrito y realizar pedidos
 * HU16: Consultar mis pedidos
 * HU17: Consultar historial de estados del pedido
 */

import { protegerPagina } from "../auth.js";
import { montarLayout } from "../layout.js";
import { listarRestaurantes } from "../api/restaurantes.js";
import { listarPlatosPorRestaurante } from "../api/platos.js";
import {
    crearPedido,
    consultarMisPedidos,
    consultarHistorialPedido
} from "../api/pedidos.js";

const sesion = protegerPagina("CLIENTE");

if (sesion) {
    montarLayout(sesion);

    // RESTAURANTES
    const seccionRestaurantes =
        document.getElementById("seccionRestaurantes");
    const listaRestaurantes =
        document.getElementById("listaRestaurantes");
    const estadoRestaurantes =
        document.getElementById("estadoRestaurantes");
    const anterior = document.getElementById("anterior");
    const siguiente = document.getElementById("siguiente");
    const numeroPagina = document.getElementById("numeroPagina");

    // MENÚ
    const seccionMenu =
        document.getElementById("seccionMenu");
    const nombreRestaurante =
        document.getElementById("nombreRestaurante");
    const estadoMenu =
        document.getElementById("estadoMenu");
    const listaPlatos =
        document.getElementById("listaPlatos");
    const volverRestaurantes =
        document.getElementById("volverRestaurantes");

    // CARRITO
    const seccionCarrito =
        document.getElementById("seccionCarrito");
    const nombreRestauranteCarrito =
        document.getElementById("nombreRestauranteCarrito");
    const estadoCarrito =
        document.getElementById("estadoCarrito");
    const listaCarrito =
        document.getElementById("listaCarrito");
    const totalCarrito =
        document.getElementById("totalCarrito");
    const vaciarCarrito =
        document.getElementById("vaciarCarrito");
    const realizarPedido =
        document.getElementById("realizarPedido");
    const estadoPedido =
        document.getElementById("estadoPedido");

    let paginaActual = 0;
    let carrito = [];
    let restauranteCarrito = null;
    let enviandoPedido = false;

    function formatoPrecio(valor) {
        return Number(valor).toLocaleString("es-CO", {
            style: "currency",
            currency: "COP",
            maximumFractionDigits: 0
        });
    }

    function mostrarMensaje(mensaje, color = "") {
        estadoPedido.textContent = mensaje;
        estadoPedido.style.color = color;
    }

    // ==========================================
    // HU9 - LISTAR RESTAURANTES
    // ==========================================

    async function cargarRestaurantes() {
        estadoRestaurantes.textContent =
            "Cargando restaurantes...";

        listaRestaurantes.replaceChildren();

        anterior.disabled = true;
        siguiente.disabled = true;

        try {
            const respuesta = await listarRestaurantes(
                paginaActual,
                10
            );

            const restaurantes = Array.isArray(respuesta)
                ? respuesta
                : respuesta.contenido ??
                  respuesta.content ??
                  [];

            if (!Array.isArray(restaurantes)) {
                throw new Error(
                    "No se pudo interpretar la lista de restaurantes."
                );
            }

            estadoRestaurantes.textContent =
                restaurantes.length === 0
                    ? "No hay restaurantes disponibles."
                    : "";

            restaurantes.forEach((restaurante) => {
                const tarjeta = document.createElement("article");
                tarjeta.className = "tarjeta";
                tarjeta.style.padding = "20px";

                if (restaurante.urlLogo) {
                    const imagen = document.createElement("img");
                    imagen.src = restaurante.urlLogo;
                    imagen.alt = restaurante.nombre;
                    imagen.style.width = "100%";
                    imagen.style.height = "150px";
                    imagen.style.objectFit = "contain";
                    imagen.onerror = () => imagen.remove();

                    tarjeta.appendChild(imagen);
                }

                const titulo = document.createElement("h2");
                titulo.textContent =
                    restaurante.nombre || "Restaurante";

                const boton = document.createElement("button");
                boton.type = "button";
                boton.className = "btn";
                boton.textContent = "Ver menú";

                boton.disabled = restaurante.id == null;

                boton.addEventListener("click", () => {
                    cargarMenu(
                        restaurante.id,
                        restaurante.nombre
                    );
                });

                tarjeta.appendChild(titulo);
                tarjeta.appendChild(boton);

                listaRestaurantes.appendChild(tarjeta);
            });

            const totalPaginas =
                respuesta.totalPaginas ??
                respuesta.totalPages ??
                (restaurantes.length > 0 ? 1 : 0);

            numeroPagina.textContent = totalPaginas
                ? `Página ${paginaActual + 1} de ${totalPaginas}`
                : "Sin páginas";

            anterior.disabled = paginaActual === 0;

            siguiente.disabled =
                paginaActual + 1 >= totalPaginas;

        } catch (error) {
            estadoRestaurantes.textContent =
                error.message ||
                "Error al cargar los restaurantes.";

            numeroPagina.textContent = "";
        }
    }

    // ==========================================
    // HU10 - CONSULTAR MENÚ
    // ==========================================

    async function cargarMenu(idRestaurante, nombre) {
        nombreRestaurante.textContent =
            `Restaurante: ${nombre}`;

        estadoMenu.textContent = "Cargando platos...";
        listaPlatos.replaceChildren();

        seccionMenu.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

        try {
            const respuesta =
                await listarPlatosPorRestaurante(idRestaurante);

            const platos = Array.isArray(respuesta)
                ? respuesta
                : respuesta.contenido ??
                  respuesta.content ??
                  [];

            if (!Array.isArray(platos)) {
                throw new Error(
                    "No se pudo interpretar el menú."
                );
            }

            estadoMenu.textContent =
                platos.length === 0
                    ? "Este restaurante no tiene platos disponibles."
                    : "";

            platos.forEach((plato) => {
                const tarjeta = document.createElement("article");
                tarjeta.className = "tarjeta";
                tarjeta.style.padding = "20px";

                if (plato.urlImagen) {
                    const imagen = document.createElement("img");
                    imagen.src = plato.urlImagen;
                    imagen.alt = plato.nombre;
                    imagen.style.width = "100%";
                    imagen.style.height = "170px";
                    imagen.style.objectFit = "cover";
                    imagen.style.borderRadius = "10px";
                    imagen.onerror = () => imagen.remove();

                    tarjeta.appendChild(imagen);
                }

                const titulo = document.createElement("h2");
                titulo.textContent = plato.nombre;

                const descripcion = document.createElement("p");
                descripcion.textContent =
                    plato.descripcion || "";

                const categoria = document.createElement("p");
                categoria.textContent =
                    `Categoría: ${plato.categoria || "General"}`;

                const precio = document.createElement("p");
                const precioNumerico = Number(plato.precio);

                const precioValido =
                    plato.precio != null &&
                    Number.isFinite(precioNumerico) &&
                    precioNumerico >= 0;

                precio.textContent = precioValido
                    ? `Precio: ${formatoPrecio(precioNumerico)}`
                    : "Precio no disponible";

                precio.style.fontWeight = "bold";

                const boton = document.createElement("button");
                boton.type = "button";
                boton.className = "btn";
                boton.textContent = "Agregar al carrito";

                boton.disabled =
                    plato.id == null || !precioValido;

                boton.addEventListener("click", () => {
                    agregarAlCarrito(
                        plato,
                        idRestaurante,
                        nombre
                    );
                });

                tarjeta.appendChild(titulo);
                tarjeta.appendChild(descripcion);
                tarjeta.appendChild(categoria);
                tarjeta.appendChild(precio);
                tarjeta.appendChild(boton);

                listaPlatos.appendChild(tarjeta);
            });

        } catch (error) {
            estadoMenu.textContent =
                error.message || "Error al cargar el menú.";
        }
    }

    // ==========================================
    // HU11 - AGREGAR AL CARRITO
    // ==========================================

    function agregarAlCarrito(plato, idRestaurante, nombre) {
        if (enviandoPedido) return;

        if (
            restauranteCarrito &&
            String(restauranteCarrito.id) !==
                String(idRestaurante)
        ) {
            const cambiar = confirm(
                "Ya tienes productos de otro restaurante. " +
                "¿Quieres vaciar el carrito para agregar este plato?"
            );

            if (!cambiar) return;

            carrito = [];
            restauranteCarrito = null;
        }

        if (!restauranteCarrito) {
            restauranteCarrito = {
                id: idRestaurante,
                nombre: nombre
            };
        }

        const existente = carrito.find(
            item => String(item.id) === String(plato.id)
        );

        if (existente) {
            existente.cantidad += 1;
        } else {
            carrito.push({
                id: plato.id,
                nombre: plato.nombre,
                precio: Number(plato.precio),
                cantidad: 1
            });
        }

        mostrarMensaje("");
        actualizarCarrito();

        seccionCarrito.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    // ==========================================
    // HU11 - ACTUALIZAR CARRITO
    // ==========================================

    function actualizarCarrito() {
        listaCarrito.replaceChildren();

        const tieneProductos = carrito.length > 0;

        estadoCarrito.textContent = tieneProductos
            ? ""
            : "Tu carrito está vacío.";

        nombreRestauranteCarrito.textContent =
            tieneProductos && restauranteCarrito
                ? `Restaurante: ${restauranteCarrito.nombre}`
                : "";

        let total = 0;

        carrito.forEach((item) => {
            const subtotal = item.precio * item.cantidad;
            total += subtotal;

            const tarjeta = document.createElement("div");
            tarjeta.className = "tarjeta";
            tarjeta.style.padding = "15px";
            tarjeta.style.marginBottom = "10px";

            const titulo = document.createElement("h3");
            titulo.textContent = item.nombre;

            const detalle = document.createElement("p");
            detalle.textContent =
                `${formatoPrecio(item.precio)} x ${item.cantidad}` +
                ` = ${formatoPrecio(subtotal)}`;

            const controles = document.createElement("div");
            controles.style.display = "flex";
            controles.style.gap = "10px";
            controles.style.alignItems = "center";
            controles.style.flexWrap = "wrap";

            const menos = document.createElement("button");
            menos.type = "button";
            menos.className = "btn";
            menos.textContent = "−";
            menos.disabled = enviandoPedido;

            menos.addEventListener("click", () => {
                if (enviandoPedido) return;

                item.cantidad--;

                if (item.cantidad <= 0) {
                    carrito = carrito.filter(
                        producto => producto !== item
                    );
                }

                if (carrito.length === 0) {
                    restauranteCarrito = null;
                }

                actualizarCarrito();
            });

            const cantidad = document.createElement("span");
            cantidad.textContent =
                `Cantidad: ${item.cantidad}`;

            const mas = document.createElement("button");
            mas.type = "button";
            mas.className = "btn";
            mas.textContent = "+";
            mas.disabled = enviandoPedido;

            mas.addEventListener("click", () => {
                if (enviandoPedido) return;

                item.cantidad++;
                actualizarCarrito();
            });

            const eliminar = document.createElement("button");
            eliminar.type = "button";
            eliminar.className = "btn";
            eliminar.textContent = "Eliminar";
            eliminar.disabled = enviandoPedido;

            eliminar.addEventListener("click", () => {
                if (enviandoPedido) return;

                carrito = carrito.filter(
                    producto => producto !== item
                );

                if (carrito.length === 0) {
                    restauranteCarrito = null;
                }

                actualizarCarrito();
            });

            controles.appendChild(menos);
            controles.appendChild(cantidad);
            controles.appendChild(mas);
            controles.appendChild(eliminar);

            tarjeta.appendChild(titulo);
            tarjeta.appendChild(detalle);
            tarjeta.appendChild(controles);

            listaCarrito.appendChild(tarjeta);
        });

        totalCarrito.textContent = formatoPrecio(total);

        realizarPedido.disabled =
            carrito.length === 0 || enviandoPedido;

        vaciarCarrito.disabled =
            carrito.length === 0 || enviandoPedido;
    }

    // ==========================================
    // HU11 - VACIAR CARRITO
    // ==========================================

    vaciarCarrito.addEventListener("click", () => {
        if (enviandoPedido) return;

        carrito = [];
        restauranteCarrito = null;

        mostrarMensaje("");
        actualizarCarrito();
    });

    // ==========================================
    // HU11 - REALIZAR PEDIDO REAL
    // ==========================================

    realizarPedido.addEventListener("click", async () => {
        if (enviandoPedido) return;

        if (carrito.length === 0 || !restauranteCarrito) {
            mostrarMensaje("Tu carrito está vacío.", "red");
            return;
        }

        const confirmar = confirm(
            `¿Deseas confirmar tu pedido en ` +
            `${restauranteCarrito.nombre}?`
        );

        if (!confirmar) return;

        enviandoPedido = true;
        actualizarCarrito();

        mostrarMensaje("Enviando tu pedido...");

        try {
            const pedido = await crearPedido(
                restauranteCarrito.id,
                carrito
            );

            const numeroPedido = pedido?.id != null
                ? ` Número de pedido: ${pedido.id}.`
                : "";

            carrito = [];
            restauranteCarrito = null;

            mostrarMensaje(
                `¡Pedido realizado correctamente!${numeroPedido}`,
                "green"
            );

            await cargarMisPedidos();

        } catch (error) {
            mostrarMensaje(
                error.message ||
                "No se pudo realizar el pedido.",
                "red"
            );

            console.error("Error al crear pedido:", error);

        } finally {
            enviandoPedido = false;
            actualizarCarrito();
        }
    });

    // ==========================================
    // HU16 - CONSULTAR MIS PEDIDOS
    // ==========================================

    const seccionMisPedidos = document.createElement("section");
    seccionMisPedidos.id = "seccionMisPedidos";
    seccionMisPedidos.className = "tarjeta";
    seccionMisPedidos.style.padding = "20px";
    seccionMisPedidos.style.marginTop = "25px";

    const tituloMisPedidos = document.createElement("h2");
    tituloMisPedidos.textContent = "Mis pedidos";

    const botonActualizarPedidos = document.createElement("button");
    botonActualizarPedidos.type = "button";
    botonActualizarPedidos.className = "btn";
    botonActualizarPedidos.textContent = "Actualizar pedidos";
    botonActualizarPedidos.style.marginBottom = "15px";

    const estadoMisPedidos = document.createElement("p");

    const listaMisPedidos = document.createElement("div");
    listaMisPedidos.id = "listaMisPedidos";

    seccionMisPedidos.appendChild(tituloMisPedidos);
    seccionMisPedidos.appendChild(botonActualizarPedidos);
    seccionMisPedidos.appendChild(estadoMisPedidos);
    seccionMisPedidos.appendChild(listaMisPedidos);

    seccionCarrito.insertAdjacentElement(
        "afterend",
        seccionMisPedidos
    );

    // ==========================================
    // HU17 - MOSTRAR HISTORIAL DE UN PEDIDO
    // ==========================================

    async function mostrarHistorialPedido(idPedido, contenedor, boton) {
        if (!contenedor.hidden) {
            contenedor.hidden = true;
            boton.textContent = "Ver historial";
            return;
        }

        contenedor.hidden = false;
        contenedor.replaceChildren();
        boton.textContent = "Ocultar historial";

        const mensaje = document.createElement("p");
        mensaje.textContent = "Cargando historial...";
        contenedor.appendChild(mensaje);

        boton.disabled = true;

        try {
            const respuesta = await consultarHistorialPedido(idPedido);

            const historial = Array.isArray(respuesta)
                ? respuesta
                : respuesta?.contenido ??
                  respuesta?.content ??
                  [];

            if (!Array.isArray(historial)) {
                throw new Error(
                    "No se pudo interpretar el historial del pedido."
                );
            }

            contenedor.replaceChildren();

            if (historial.length === 0) {
                const vacio = document.createElement("p");
                vacio.textContent =
                    "Este pedido todavía no tiene historial registrado.";
                contenedor.appendChild(vacio);
                return;
            }

            const tituloHistorial = document.createElement("h4");
            tituloHistorial.textContent = "Historial de estados";
            tituloHistorial.style.marginTop = "15px";
            contenedor.appendChild(tituloHistorial);

            historial.forEach((registro, indice) => {
                const elemento = document.createElement("div");
                elemento.style.padding = "12px";
                elemento.style.marginBottom = "10px";
                elemento.style.borderLeft = "4px solid #4caf50";
                elemento.style.backgroundColor = "rgba(128,128,128,0.08)";
                elemento.style.borderRadius = "6px";

                const estado = document.createElement("p");
                estado.style.fontWeight = "bold";
                estado.textContent =
                    `${indice + 1}. ${registro.estadoNuevo || "Sin estado"}`;

                const fecha = document.createElement("p");

                if (registro.fecha) {
                    const fechaRegistro = new Date(registro.fecha);

                    fecha.textContent = Number.isNaN(
                        fechaRegistro.getTime()
                    )
                        ? `Fecha: ${registro.fecha}`
                        : `Fecha: ${fechaRegistro.toLocaleString("es-CO")}`;
                } else {
                    fecha.textContent = "Fecha no disponible";
                }

                elemento.appendChild(estado);
                elemento.appendChild(fecha);

                if (registro.estadoAnterior) {
                    const anterior = document.createElement("p");
                    anterior.textContent =
                        `Estado anterior: ${registro.estadoAnterior}`;
                    elemento.appendChild(anterior);
                }

                if (
                    registro.duracionEstadoAnteriorSegundos != null
                ) {
                    const duracion = document.createElement("p");

                    const segundos = Number(
                        registro.duracionEstadoAnteriorSegundos
                    );

                    const minutos = Math.floor(segundos / 60);
                    const segundosRestantes = segundos % 60;

                    duracion.textContent =
                        `Duración del estado anterior: ` +
                        `${minutos} min ${segundosRestantes} s`;

                    elemento.appendChild(duracion);
                }

                contenedor.appendChild(elemento);
            });

        } catch (error) {
            contenedor.replaceChildren();

            const mensajeError = document.createElement("p");
            mensajeError.style.color = "red";
            mensajeError.textContent =
                error.message ||
                "No se pudo consultar el historial.";

            contenedor.appendChild(mensajeError);

            console.error(
                "Error al consultar historial:",
                error
            );

        } finally {
            boton.disabled = false;
        }
    }

    // ==========================================
    // HU16 - CARGAR MIS PEDIDOS
    // HU17 - BOTÓN VER HISTORIAL
    // ==========================================

    async function cargarMisPedidos() {
        estadoMisPedidos.textContent = "Cargando tus pedidos...";
        listaMisPedidos.replaceChildren();
        botonActualizarPedidos.disabled = true;

        try {
            const respuesta = await consultarMisPedidos();

            const pedidos = Array.isArray(respuesta)
                ? respuesta
                : respuesta?.contenido ??
                  respuesta?.content ??
                  [];

            if (!Array.isArray(pedidos)) {
                throw new Error(
                    "No se pudo interpretar la lista de pedidos."
                );
            }

            if (pedidos.length === 0) {
                estadoMisPedidos.textContent =
                    "Todavía no has realizado pedidos.";
                return;
            }

            estadoMisPedidos.textContent = "";

            pedidos.forEach((pedido) => {
                const tarjeta = document.createElement("article");
                tarjeta.className = "tarjeta";
                tarjeta.style.padding = "15px";
                tarjeta.style.marginBottom = "15px";

                // NÚMERO DE PEDIDO
                const titulo = document.createElement("h3");
                titulo.textContent = `Pedido #${pedido.id}`;

                // RESTAURANTE
                const restaurante = document.createElement("p");
                restaurante.textContent =
                    `Restaurante ID: ${pedido.idRestaurante}`;

                // ESTADO ACTUAL
                const estado = document.createElement("p");
                estado.textContent =
                    `Estado: ${pedido.estado || "PENDIENTE"}`;
                estado.style.fontWeight = "bold";

                // FECHA DEL PEDIDO
                const fecha = document.createElement("p");

                if (pedido.fechaCreacion) {
                    const fechaPedido = new Date(
                        pedido.fechaCreacion
                    );

                    fecha.textContent = Number.isNaN(
                        fechaPedido.getTime()
                    )
                        ? `Fecha: ${pedido.fechaCreacion}`
                        : `Fecha: ${fechaPedido.toLocaleString("es-CO")}`;
                } else {
                    fecha.textContent = "Fecha no disponible";
                }

                // TOTAL
                const total = document.createElement("p");
                total.textContent =
                    `Total: ${formatoPrecio(pedido.total ?? 0)}`;
                total.style.fontWeight = "bold";

                // PLATOS
                const tituloPlatos = document.createElement("h4");
                tituloPlatos.textContent = "Platos del pedido:";

                const listaPlatosPedido = document.createElement("ul");

                const detalles = Array.isArray(pedido.platos)
                    ? pedido.platos
                    : [];

                detalles.forEach((plato) => {
                    const elemento = document.createElement("li");

                    elemento.textContent =
                        `Plato ID: ${plato.idPlato} | ` +
                        `Cantidad: ${plato.cantidad} | ` +
                        `Precio: ${formatoPrecio(plato.precioUnitario)}`;

                    listaPlatosPedido.appendChild(elemento);
                });

                // ==========================================
                // HU17 - BOTÓN VER HISTORIAL
                // ==========================================

                const botonHistorial = document.createElement("button");
                botonHistorial.type = "button";
                botonHistorial.className = "btn";
                botonHistorial.textContent = "Ver historial";
                botonHistorial.style.marginTop = "15px";
                botonHistorial.style.marginBottom = "10px";

                // CONTENEDOR DEL HISTORIAL
                const contenedorHistorial = document.createElement("div");
                contenedorHistorial.hidden = true;
                contenedorHistorial.style.marginTop = "10px";

                botonHistorial.disabled = pedido.id == null;

                // MOSTRAR U OCULTAR HISTORIAL
                botonHistorial.addEventListener("click", () => {
                    mostrarHistorialPedido(
                        pedido.id,
                        contenedorHistorial,
                        botonHistorial
                    );
                });

                // ==========================================
                // AGREGAR ELEMENTOS A LA TARJETA
                // ==========================================

                tarjeta.appendChild(titulo);
                tarjeta.appendChild(restaurante);
                tarjeta.appendChild(estado);
                tarjeta.appendChild(fecha);
                tarjeta.appendChild(total);
                tarjeta.appendChild(tituloPlatos);
                tarjeta.appendChild(listaPlatosPedido);

                // HU17
                tarjeta.appendChild(botonHistorial);
                tarjeta.appendChild(contenedorHistorial);

                listaMisPedidos.appendChild(tarjeta);
            });

        } catch (error) {
            estadoMisPedidos.textContent =
                error.message ||
                "No se pudieron consultar tus pedidos.";

            console.error(
                "Error al consultar pedidos:",
                error
            );

        } finally {
            botonActualizarPedidos.disabled = false;
        }
    }

    // ==========================================
    // HU16 - ACTUALIZAR PEDIDOS
    // ==========================================

    botonActualizarPedidos.addEventListener(
        "click",
        cargarMisPedidos
    );

    // ==========================================
    // HU9 - PAGINACIÓN DE RESTAURANTES
    // ==========================================

    anterior.addEventListener("click", () => {
        if (paginaActual > 0) {
            paginaActual--;
            cargarRestaurantes();
        }
    });

    siguiente.addEventListener("click", () => {
        paginaActual++;
        cargarRestaurantes();
    });

    // ==========================================
    // HU10 - VOLVER A RESTAURANTES
    // ==========================================

    volverRestaurantes.addEventListener("click", () => {
        seccionRestaurantes.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    });

    // ==========================================
    // INICIAR PÁGINA
    // ==========================================

    actualizarCarrito();
    cargarRestaurantes();
    cargarMisPedidos();

}





