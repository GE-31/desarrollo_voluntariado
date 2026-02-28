/* ===================================================================
   SALIDAS DE DONACIONES - JavaScript
   Buscador AJAX autocompletado, modal, paginaciÃ³n, filtros, CRUD
=================================================================== */

const modalSalida = document.getElementById("modalSalida");
const formSalida = document.getElementById("formSalida");
const btnGuardarSalida = document.getElementById("btnGuardarSalida");
const accionSalidaInput = document.getElementById("accionSalida");
const idSalidaInput = document.getElementById("idSalida");
const tipoSalidaHidden = document.getElementById("tipoSalidaHidden");
const idDonacionHidden = document.getElementById("idDonacion");
const actividadDestinoSelect = document.getElementById("actividadDestino");
const buscarSalidasInput = document.getElementById("buscarSalidas");
const PAGINA_TAMANO_S = 5;

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// AUTOCOMPLETADO: Donacion origen con busqueda AJAX
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

const buscarDonacionInput = document.getElementById("buscarDonacion");
const donacionResultados = document.getElementById("donacionResultados");
const btnLimpiar = document.getElementById("btnLimpiarDonacion");
const donacionSelBadge = document.getElementById("donacionSeleccionada");
const donacionSelText = document.getElementById("donacionSelectedText");

let searchTimeout = null;
let donacionSeleccionadaData = null;

function inicializarAutocompletado() {
    if (!buscarDonacionInput) return;

    buscarDonacionInput.addEventListener("input", function () {
        const query = this.value.trim();
        clearTimeout(searchTimeout);

        if (query.length < 2) {
            cerrarDropdown();
            return;
        }

        searchTimeout = setTimeout(() => buscarDonacionesAjax(query), 300);
    });

    // Cerrar dropdown al hacer click fuera
    document.addEventListener("click", function (e) {
        const wrapper = document.getElementById("donacionAutocompleteWrapper");
        if (wrapper && !wrapper.contains(e.target)) {
            cerrarDropdown();
        }
    });

    // Teclas de navegaciÃ³n
    buscarDonacionInput.addEventListener("keydown", function (e) {
        const items = donacionResultados.querySelectorAll(".autocomplete-item");
        const activeItem = donacionResultados.querySelector(".autocomplete-item.active");
        let idx = Array.from(items).indexOf(activeItem);

        if (e.key === "ArrowDown") {
            e.preventDefault();
            if (idx < items.length - 1) idx++;
            else idx = 0;
            items.forEach(i => i.classList.remove("active"));
            if (items[idx]) { items[idx].classList.add("active"); items[idx].scrollIntoView({ block: "nearest" }); }
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            if (idx > 0) idx--;
            else idx = items.length - 1;
            items.forEach(i => i.classList.remove("active"));
            if (items[idx]) { items[idx].classList.add("active"); items[idx].scrollIntoView({ block: "nearest" }); }
        } else if (e.key === "Enter") {
            e.preventDefault();
            if (activeItem) activeItem.click();
        } else if (e.key === "Escape") {
            cerrarDropdown();
        }
    });

    if (btnLimpiar) {
        btnLimpiar.addEventListener("click", limpiarDonacionSeleccionada);
    }
}

async function buscarDonacionesAjax(query) {
    try {
        donacionResultados.innerHTML = '<div class="autocomplete-loading"><i class="fa-solid fa-spinner fa-spin"></i> Buscando...</div>';
        donacionResultados.classList.add("open");

        const resp = await fetch(`salidas-donaciones?accion=buscar_donaciones&query=${encodeURIComponent(query)}`);
        const data = await resp.json();

        if (!data.results || data.results.length === 0) {
            donacionResultados.innerHTML = '<div class="autocomplete-empty"><i class="fa-solid fa-inbox"></i> No se encontraron donaciones</div>';
            return;
        }

        let html = "";
        data.results.forEach(d => {
            const donante = normalizarTextoVisual(d.donante || "");
            const actividadOrigen = normalizarTextoVisual(d.actividadOrigen || "");
            const saldoText = `S/ ${Number(d.saldoDisponible).toFixed(2)} disponible`;
            html += `
                <div class="autocomplete-item" data-donacion='${JSON.stringify(d).replace(/'/g, "&#39;")}'>
                    <div class="ac-item-header">
                        <span class="ac-item-id">#${d.id}</span>
                        <span class="ac-item-tipo tag dinero">DINERO</span>
                    </div>
                    <div class="ac-item-body">
                        <div class="ac-item-donante"><i class="fa-solid fa-user"></i> ${donante}</div>
                        <div class="ac-item-saldo"><i class="fa-solid fa-wallet"></i> ${saldoText}</div>
                    </div>
                    <div class="ac-item-footer">
                        <span class="ac-item-origen"><i class="fa-solid fa-bullhorn"></i> ${actividadOrigen}</span>
                        <span class="ac-item-original">Original: S/ ${Number(d.cantidadOriginal).toFixed(2)}</span>
                    </div>
                </div>`;
        });

        donacionResultados.innerHTML = html;

        donacionResultados.querySelectorAll(".autocomplete-item").forEach(item => {
            item.addEventListener("click", function () {
                const donData = JSON.parse(this.dataset.donacion);
                seleccionarDonacion(donData);
            });
        });

    } catch (err) {
        console.error("Error al buscar donaciones:", err);
        donacionResultados.innerHTML = '<div class="autocomplete-empty"><i class="fa-solid fa-triangle-exclamation"></i> Error al buscar</div>';
    }
}

function seleccionarDonacion(donacion) {
    donacionSeleccionadaData = donacion;
    idDonacionHidden.value = donacion.id;

    const donante = normalizarTextoVisual(donacion.donante || "");
    const saldoText = `S/ ${Number(donacion.saldoDisponible).toFixed(2)}`;
    donacionSelText.textContent = `#${donacion.id} - ${donante} - ${saldoText}`;
    donacionSelBadge.style.display = "flex";

    buscarDonacionInput.value = `#${donacion.id} - ${donante}`;
    buscarDonacionInput.classList.add("has-selection");
    btnLimpiar.style.display = "flex";

    cerrarDropdown();
    actualizarInfoDonacion(donacion);
}

function actualizarInfoDonacion(donacion) {
    const infoBox = document.getElementById("infoDonacion");
    const labelCantidad = document.getElementById("labelCantidadSalida");

    document.getElementById("infoDonMonto").textContent =
        `S/ ${Number(donacion.saldoDisponible).toFixed(2)} disponible (original: S/ ${Number(donacion.cantidadOriginal).toFixed(2)})`;
    document.getElementById("infoDonDonante").textContent = normalizarTextoVisual(donacion.donante || "");
    document.getElementById("infoDonActividad").textContent = normalizarTextoVisual(donacion.actividadOrigen || "");
    infoBox.style.display = "block";

    tipoSalidaHidden.value = "DINERO";
    labelCantidad.textContent = "Monto a asignar (S/) *";

    const cantidadInput = document.getElementById("cantidadSalida");
    cantidadInput.max = donacion.saldoDisponible;
}

function limpiarDonacionSeleccionada() {
    donacionSeleccionadaData = null;
    idDonacionHidden.value = "";
    buscarDonacionInput.value = "";
    buscarDonacionInput.classList.remove("has-selection");
    btnLimpiar.style.display = "none";
    donacionSelBadge.style.display = "none";
    document.getElementById("infoDonacion").style.display = "none";
    document.getElementById("labelCantidadSalida").textContent = "Monto a asignar (S/) *";
    tipoSalidaHidden.value = "DINERO";
    document.getElementById("cantidadSalida").removeAttribute("max");
    buscarDonacionInput.focus();
}

function cerrarDropdown() {
    donacionResultados.innerHTML = "";
    donacionResultados.classList.remove("open");
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// MODAL: Abrir / Cerrar
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

function abrirModalSalida() {
    formSalida.reset();
    idSalidaInput.value = "";
    idDonacionHidden.value = "";
    accionSalidaInput.value = "registrar";
    document.getElementById("tituloModalSalida").innerHTML =
        '<i class="fa-solid fa-arrow-right-from-bracket"></i> Registrar Salida';
    btnGuardarSalida.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Registrar Salida';
    document.getElementById("infoDonacion").style.display = "none";
    document.getElementById("labelCantidadSalida").textContent = "Monto a asignar (S/) *";
    tipoSalidaHidden.value = "DINERO";

    limpiarDonacionSeleccionada();
    buscarDonacionInput.disabled = false;

    cargarActividadesDestino();
    modalSalida.style.display = "flex";
    setTimeout(() => buscarDonacionInput.focus(), 200);
}

function cerrarModalSalida() {
    modalSalida.style.display = "none";
    cerrarDropdown();
}

if (modalSalida) {
    modalSalida.addEventListener("click", function (e) {
        if (e.target === modalSalida) cerrarModalSalida();
    });
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// CARGAR DATOS: Actividades destino
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

async function cargarActividadesDestino() {
    try {
        const resp = await fetch("salidas-donaciones?accion=actividades");
        const actividades = await resp.json();

        actividadDestinoSelect.innerHTML = '<option value="">Seleccione campana destino</option>';
        actividades.forEach(a => {
            const opt = document.createElement("option");
            opt.value = a.idActividad;
            opt.textContent = normalizarTextoVisual(a.nombre || "");
            actividadDestinoSelect.appendChild(opt);
        });
    } catch (err) {
        console.error("Error al cargar actividades:", err);
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// EDITAR SALIDA
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

async function editarSalida(id) {
    try {
        const resp = await fetch(`salidas-donaciones?accion=obtener&id=${id}`);
        const data = await resp.json();

        if (data.ok === false) {
            Notify.error(data.message || "Salida no encontrada");
            return;
        }

        if (data.estado && data.estado.toUpperCase() !== "PENDIENTE") {
            Notify.warning("Solo se pueden editar salidas con estado PENDIENTE.");
            return;
        }

        formSalida.reset();
        accionSalidaInput.value = "editar";
        idSalidaInput.value = data.idSalida;
        document.getElementById("tituloModalSalida").innerHTML =
            '<i class="fa-solid fa-pen-to-square"></i> Editar Salida';
        btnGuardarSalida.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar Cambios';

        await cargarActividadesDestino();

        // Obtener saldo disponible de la donacion origen
        let saldoData = null;
        try {
            const saldoResp = await fetch(`salidas-donaciones?accion=saldo_donacion&id=${data.idDonacion}`);
            saldoData = await saldoResp.json();
        } catch (e) {
            console.warn("No se pudo obtener saldo:", e);
        }

        // Configurar donacion seleccionada (no editable en modo edicion)
        const donData = {
            id: data.idDonacion,
            tipoDonacion: "DINERO",
            cantidadOriginal: saldoData ? saldoData.cantidadOriginal : data.donacionCantidad,
            saldoDisponible: saldoData ? (Number(saldoData.saldoDisponible) + data.cantidad) : data.donacionCantidad,
            donante: data.donanteNombre || "ANONIMO",
            actividadOrigen: data.actividadNombre || "Origen",
            descripcion: data.donacionDescripcion || ""
        };

        seleccionarDonacion(donData);
        buscarDonacionInput.disabled = true;
        btnLimpiar.style.display = "none";

        actividadDestinoSelect.value = data.idActividad;
        document.getElementById("cantidadSalida").value = data.cantidad;
        document.getElementById("descripcionSalida").value = data.descripcion || "";

        modalSalida.style.display = "flex";
    } catch (err) {
        console.error("Error al obtener salida:", err);
        Notify.error("Error al cargar la informacion de la salida.");
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ANULAR SALIDA
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

async function anularSalida(id) {
    const motivo = prompt("Motivo de anulacion:", "Anulacion manual");
    if (motivo === null) return;

    try {
        const resp = await fetch("salidas-donaciones?accion=anular", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `idSalida=${id}&motivo=${encodeURIComponent(motivo)}`
        });
        const data = await resp.json();
        if (data.ok) {
            Notify.success("Salida anulada correctamente");
            setTimeout(() => location.reload(), 1200);
        } else {
            Notify.error(data.message || "No se pudo anular la salida");
        }
    } catch (err) {
        console.error("Error al anular salida:", err);
        Notify.error("Error de conexion al anular la salida.");
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// CAMBIAR ESTADO
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

async function cambiarEstadoSalida(id, estado) {
    const ok = await Notify.confirm(`Cambiar estado a "${estado}"?`, "", { variant: "info", okText: "Si, confirmar" });
    if (!ok) return;

    try {
        const resp = await fetch("salidas-donaciones?accion=cambiar_estado", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `idSalida=${id}&estado=${encodeURIComponent(estado)}`
        });
        const data = await resp.json();
        if (data.ok) {
            Notify.success("Estado actualizado correctamente");
            setTimeout(() => location.reload(), 1200);
        } else {
            Notify.error(data.message || "No se pudo cambiar el estado");
        }
    } catch (err) {
        console.error("Error al cambiar estado:", err);
        Notify.error("Error de conexion al cambiar el estado.");
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// FILTROS Y BÃšSQUEDA (tabla principal)
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

function filtrarSalidas() {
    const texto = (buscarSalidasInput?.value || "").toLowerCase().trim();
    const filtroEstado = document.getElementById("filtroEstado")?.value || "";
    const filas = document.querySelectorAll("#tbodySalidas .salida-row");

    filas.forEach(fila => {
        const contenido = fila.textContent.toLowerCase();
        const estado = fila.dataset.estado || "";

        let visible = true;
        if (texto && !contenido.includes(texto)) visible = false;
        if (filtroEstado && estado !== filtroEstado) visible = false;

        fila.dataset.filtroVisible = visible ? "1" : "0";
    });

    paginaActualS = 1;
    paginacionSalidas();
}

if (buscarSalidasInput) {
    buscarSalidasInput.addEventListener("input", filtrarSalidas);
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// PAGINACIÃ“N CLIENT-SIDE
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

let paginaActualS = 1;

function paginacionSalidas() {
    const todasLasFilas = Array.from(document.querySelectorAll("#tbodySalidas .salida-row"));
    const filasVisibles = todasLasFilas.filter(f => (f.dataset.filtroVisible || "1") === "1");
    const totalPaginas = Math.ceil(filasVisibles.length / PAGINA_TAMANO_S) || 1;
    const paginacionDiv = document.getElementById("salidasPaginacion");
    const textoEl = document.getElementById("textoPaginacionSalidas");

    if (paginaActualS > totalPaginas) paginaActualS = totalPaginas;

    // Primero ocultar todo; luego mostrar solo lo que corresponda a la pagina actual.
    todasLasFilas.forEach(fila => {
        fila.style.display = "none";
    });

    filasVisibles.forEach((fila, i) => {
        const pagina = Math.floor(i / PAGINA_TAMANO_S) + 1;
        fila.style.display = pagina === paginaActualS ? "" : "none";
    });

    if (filasVisibles.length > PAGINA_TAMANO_S) {
        paginacionDiv.style.display = "flex";
        textoEl.textContent = `Pagina ${paginaActualS} de ${totalPaginas}`;
        document.getElementById("btnPaginaAnteriorS").disabled = paginaActualS <= 1;
        document.getElementById("btnPaginaSiguienteS").disabled = paginaActualS >= totalPaginas;
    } else {
        paginacionDiv.style.display = "none";
    }
}

function normalizarTextoVisual(texto) {
    if (!texto) return texto;
    let limpio = texto;
    limpio = limpio
        .replace(/AN.{0,3}NIMO/gi, "ANONIMO")
        .replace(/campa.{0,3}a/gi, "campana")
        .replace(/Ã¡/g, "a")
        .replace(/Ã©/g, "e")
        .replace(/Ã­/g, "i")
        .replace(/Ã³/g, "o")
        .replace(/Ãº/g, "u")
        .replace(/Ã±/g, "n")
        .replace(/Â/g, "")
        .replace(/�/g, "");
    return limpio;
}

function corregirTextosRotos() {
    // No tocar celdas completas ni contenedores de acciones; eso elimina botones/iconos.
    const selectoresSeguros = [
        "#tbodySalidas .origin-donante",
        "#tbodySalidas .origin-monto",
        "#tbodySalidas .campana-destino span",
        "#tbodySalidas .desc-text",
        "#tbodySalidas td:nth-child(5)", // Registrado por
        "#tbodySalidas td:nth-child(6)", // Fecha
        "#actividadDestino option"
    ];

    document.querySelectorAll(selectoresSeguros.join(",")).forEach(el => {
        el.textContent = normalizarTextoVisual(el.textContent);
    });
}

document.getElementById("btnPaginaAnteriorS")?.addEventListener("click", () => {
    if (paginaActualS > 1) { paginaActualS--; paginacionSalidas(); }
});
document.getElementById("btnPaginaSiguienteS")?.addEventListener("click", () => {
    paginaActualS++;
    paginacionSalidas();
});

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// VALIDACIÃ“N ANTES DE ENVIAR
// VALIDACION ANTES DE ENVIAR

if (formSalida) {
    formSalida.addEventListener("submit", function (e) {
        const donacionId = idDonacionHidden.value;
        const actividad = actividadDestinoSelect.value;
        const cantidad = parseFloat(document.getElementById("cantidadSalida").value || 0);

        if (!donacionId) {
            e.preventDefault();
            Notify.warning("Debe buscar y seleccionar una donacion origen.");
            buscarDonacionInput.focus();
            return false;
        }

        if (!actividad) {
            e.preventDefault();
            Notify.warning("Debe seleccionar una campana/actividad destino.");
            return false;
        }

        if (cantidad <= 0) {
            e.preventDefault();
            Notify.warning("La cantidad debe ser mayor a 0.");
            return false;
        }
        // Validar que el monto no exceda el saldo disponible
        if (donacionSeleccionadaData && donacionSeleccionadaData.saldoDisponible) {
            const saldo = parseFloat(donacionSeleccionadaData.saldoDisponible);
            if (cantidad > saldo) {
                e.preventDefault();
                const msg = `El monto S/ ${cantidad.toFixed(2)} excede el saldo disponible de S/ ${saldo.toFixed(2)}.`;
                Notify.warning(msg);
                return false;
            }
        }


        buscarDonacionInput.disabled = false;
    });
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// INICIALIZACION
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

document.addEventListener("DOMContentLoaded", function () {
    inicializarAutocompletado();
    filtrarSalidas();
    corregirTextosRotos();
});

