/** Página de inicio de sesión (HU5). Al entrar, cada rol va a su propia página. */
import { iniciarSesion } from "../api/usuarios.js";
import { guardarSesion, obtenerSesion, paginaDeInicio } from "../auth.js";
import { icono, ponerCargando, escaparHtml } from "../ui.js";

// Si ya hay una sesión abierta, no tiene sentido volver a pedir el login
const sesionActual = obtenerSesion();
if (sesionActual) location.replace(paginaDeInicio(sesionActual.rol));

// Íconos (se ponen por JS para no repetir los SVG en el HTML)
document.getElementById("logo-portada").innerHTML = icono("logo");
document.querySelectorAll("[data-icono]").forEach((caja) => caja.insertAdjacentHTML("afterbegin", icono(caja.dataset.icono)));
document.getElementById("beneficios").innerHTML = [
    ["tienda", "Explora los restaurantes y sus menús"],
    ["bolsa", "Haz tu pedido en pocos clics"],
    ["mensaje", "Recibe tu PIN por SMS cuando esté listo"],
].map(([nombre, texto]) => `<li><span>${icono(nombre)}</span>${texto}</li>`).join("");

const formulario = document.getElementById("formulario-login");
const correo = document.getElementById("correo");
const clave = document.getElementById("clave");
const alerta = document.getElementById("alerta");
const botonEntrar = document.getElementById("btn-entrar");
const botonVerClave = document.getElementById("ver-clave");

// Mostrar / ocultar contraseña
botonVerClave.innerHTML = icono("ojo");
botonVerClave.addEventListener("click", () => {
    const visible = clave.type === "text";
    clave.type = visible ? "password" : "text";
    botonVerClave.innerHTML = icono(visible ? "ojo" : "ojoCerrado");
    botonVerClave.setAttribute("aria-label", visible ? "Mostrar contraseña" : "Ocultar contraseña");
});

// Mensaje cuando el token venció y http.js nos mandó de vuelta aquí
if (new URLSearchParams(location.search).get("sesion") === "expirada") {
    mostrarAlerta("Tu sesión terminó. Vuelve a iniciar sesión.", "info");
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    ocultarAlerta();

    if (!correo.value.trim() || !clave.value) {
        mostrarAlerta("Escribe tu correo y tu contraseña.");
        (correo.value.trim() ? clave : correo).focus();
        return;
    }

    ponerCargando(botonEntrar, true);
    try {
        const respuesta = await iniciarSesion(correo.value.trim(), clave.value);
        guardarSesion(respuesta.token);
        location.href = paginaDeInicio(respuesta.rol);
    } catch (error) {
        mostrarAlerta(error.message);
        clave.select();
    } finally {
        ponerCargando(botonEntrar, false);
    }
});

function mostrarAlerta(mensaje, tipo = "error") {
    alerta.className = `login__alerta ${tipo === "info" ? "login__alerta--info" : ""}`;
    alerta.innerHTML = `${icono(tipo === "info" ? "info" : "alerta")}<span>${escaparHtml(mensaje)}</span>`;
    alerta.hidden = false;
}

function ocultarAlerta() {
    alerta.hidden = true;
}
