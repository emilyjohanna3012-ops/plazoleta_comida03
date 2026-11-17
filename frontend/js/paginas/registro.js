
/**
 * HU8 - Registro de clientes.
 * Conexión con usuarios-ms.
 */

const formulario = document.getElementById("formRegistro");
const mensaje = document.getElementById("mensajeRegistro");
const boton = formulario.querySelector('button[type="submit"]');

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const clave = document.getElementById("clave").value;
    const confirmar = document.getElementById("confirmarClave").value;

    if (clave !== confirmar) {
        mensaje.textContent = "Las contraseñas no coinciden.";
        mensaje.style.color = "red";
        return;
    }

    const datos = {
        nombre: document.getElementById("nombre").value.trim(),
        apellido: document.getElementById("apellido").value.trim(),
        documento: document.getElementById("documento").value.trim(),
        celular: document.getElementById("celular").value.trim(),
        fechaNacimiento: document.getElementById("fechaNacimiento").value,
        correo: document.getElementById("correo").value.trim(),
        clave: clave
    };

    mensaje.textContent = "Registrando cliente...";
    mensaje.style.color = "black";
    boton.disabled = true;

    try {
        const respuesta = await fetch(
            "http://localhost:8081/api/v1/usuarios/cliente",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            }
        );

        if (!respuesta.ok) {
            let detalle = "";

            try {
                const error = await respuesta.json();
                detalle = error.message || error.mensaje || "";
            } catch {
                // El servidor no devolvió un error en JSON.
            }

            throw new Error(
                detalle || `No se pudo registrar el cliente (${respuesta.status}).`
            );
        }

        mensaje.textContent =
            "¡Cuenta creada correctamente! Ya puedes iniciar sesión.";
        mensaje.style.color = "green";

        formulario.reset();

    } catch (error) {
        mensaje.textContent =
            error.message || "No se pudo conectar con el servidor.";
        mensaje.style.color = "red";
    } finally {
        boton.disabled = false;
    }
});

