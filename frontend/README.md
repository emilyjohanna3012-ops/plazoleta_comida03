# plazoleta-frontend (Plazoleta de comidas)

Frontend web del reto Plazoleta de comidas, hecho con **HTML, CSS y JavaScript puro** (sin frameworks).
Consume las APIs REST de `usuarios-ms` (8081) y `plazoleta-ms` (8082). trazabilidad-ms y mensajeria-ms
no se llaman desde el navegador: plazoleta-ms habla con ellos (diagrama de arquitectura del reto).

## Cómo correrlo

1. Encender los microservicios (mismo `JWT_SECRET` en todos).
2. Abrir esta carpeta en VS Code y, con la extensión **Live Server**, clic derecho en `index.html` → *Open with Live Server*.
3. Se abre en `http://127.0.0.1:5500` (o `localhost:5500`). plazoleta-ms solo acepta esos dos orígenes (CORS).

> No abrir los `.html` con doble clic (`file://`): los módulos de JavaScript (`type="module"`) no funcionan así.

## Estructura

```
index.html            → login (HU5), redirige según el rol            [base]
registro.html         → registro de cliente (HU8)                       Emily
cliente.html          → restaurantes, menú, pedido, cancelar, trazabilidad Emily
admin.html            → propietarios y restaurantes (HU1, HU2)           Melissa
propietario.html      → platos, empleados, eficiencia (HU3, 4, 6, 7, 18) Melissa
empleado.html         → pedidos del restaurante (HU12–HU15)              Carlos

css/base.css          → colores, tipografía y reset (variables en :root)
css/componentes.css   → encabezado, botones, formularios, tarjetas, insignias, pestañas, avisos, modal
css/login.css         → solo el login
css/<pagina>.css      → estilos propios de cada página (cada uno crea el suyo)

js/config.js          → URLs de los servicios, página de cada rol y menú superior
js/auth.js            → guardar/leer el token, protegerPagina(rol)
js/layout.js          → pinta encabezado y pie en las páginas privadas
js/ui.js              → íconos, avisos, modales, botón "cargando", formato de precios y fechas
js/api/http.js        → apiFetch(): token, JSON y errores {"mensaje"} del backend
js/api/<servicio>.js  → una función por endpoint real
js/paginas/<pagina>.js→ el código de cada página
```

## Reglas para trabajar sin pisarnos

- **Cada quien toca solo sus archivos:** su `.html`, su `js/paginas/<pagina>.js` y su `css/<pagina>.css`.
- Las funciones de API van en archivos separados por tema (`js/api/restaurantes.js`, `js/api/platos.js`…).
  Si dos personas necesitan el mismo archivo, se avisa antes para no generar conflictos.
- Los archivos de la base (`base.css`, `componentes.css`, `http.js`, `auth.js`, `ui.js`, `layout.js`, `config.js`)
  solo se cambian con un PR propio y avisando al grupo.
- **No inventar endpoints:** usar solo los que están en Swagger (`http://localhost:8082/swagger-ui.html`).
- Todo texto que venga del backend se mete en el HTML con `escaparHtml()` (evita inyección de HTML).
- La seguridad real está en el backend: ocultar un botón en el frontend no protege nada.

## Cómo hacer una página nueva (resumen)

```js
import { protegerPagina } from "../auth.js";
import { montarLayout } from "../layout.js";
import { apiFetch } from "../api/http.js";
import { mostrarAviso } from "../ui.js";

const sesion = protegerPagina("CLIENTE");      // si no es CLIENTE, lo saca de la página
if (sesion) {
    montarLayout(sesion);                       // encabezado y pie
    try {
        const pagina = await apiFetch("plazoleta", "/api/v1/restaurantes?pagina=0&tamanio=10");
        // pintar pagina.contenido ...
    } catch (error) {
        mostrarAviso("error", error.message);   // el mensaje ya viene listo del backend
    }
}
```

## Flujo de trabajo con Git
- Nunca trabajar directo en `main` ni en `develop`.
- Una rama por módulo (`feature/front-cliente`, `feature/front-propietario`…) y Pull Request hacia `develop`.
