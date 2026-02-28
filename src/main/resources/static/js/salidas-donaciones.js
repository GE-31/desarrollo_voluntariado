/* ===================================================================
   SALIDAS DE DONACIONES - JavaScript
   Buscador AJAX autocompletado, modal, paginación, filtros, CRUD
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
const PAGINA_TAMANO_S = 8;

// ═══════════════════════════════════════════════════════
// AUTOCOMPLETADO: Donación origen con búsqueda AJAX
// ═══════════════════════════════════════════════════════

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

    // Teclas de navegación
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
            const isDinero = d.tipoDonacion === "DINERO";
            const saldoText = isDinero
                ? `S/ ${Number(d.saldoDisponible).toFixed(2)} disponible`
                : `${Number(d.saldoDisponible).toFixed(0)} unidades disponibles`;
            const tipoClass = isDinero ? "dinero" : "especie";
            const tipoLabel = isDinero ? "DINERO" : "EN ESPECIE";

            html += `
                <div class="autocomplete-item" data-donacion='${JSON.stringify(d).replace(/'/g, "&#39;")}'>
                    <div class="ac-item-header">
                        <span class="ac-item-id">#${d.id}</span>
                        <span class="ac-item-tipo tag ${tipoClass}">${tipoLabel}</span>
                    </div>
                    <div class="ac-item-body">
                        <div class="ac-item-donante"><i class="fa-solid fa-user"></i> ${d.donante}</div>
                        <div class="ac-item-saldo"><i class="fa-solid fa-wallet"></i> ${saldoText}</div>
                    </div>
                    <div class="ac-item-footer">
                        <span class="ac-item-origen"><i class="fa-solid fa-bullhorn"></i> ${d.actividadOrigen}</span>
                        <span class="ac-item-original">Original: ${isDinero ? 'S/ ' + Number(d.cantidadOriginal).toFixed(2) : d.cantidadOriginal + ' uds'}</span>
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

    const isDinero = donacion.tipoDonacion === "DINERO";
    const saldoText = isDinero
        ? `S/ ${Number(donacion.saldoDisponible).toFixed(2)}`
        : `${Number(donacion.saldoDisponible).toFixed(0)} uds`;
    donacionSelText.textContent = `#${donacion.id} — ${donacion.donante} — ${saldoText}`;
    donacionSelBadge.style.display = "flex";

    buscarDonacionInput.value = `#${donacion.id} — ${donacion.donante}`;
    buscarDonacionInput.classList.add("has-selection");
    btnLimpiar.style.display = "flex";

    cerrarDropdown();
    actualizarInfoDonacion(donacion);
}

function actualizarInfoDonacion(donacion) {
    const infoBox = document.getElementById("infoDonacion");
    const seccionEspecie = document.getElementById("seccionEspecieSalida");
    const labelCantidad = document.getElementById("labelCantidadSalida");
    const isDinero = donacion.tipoDonacion === "DINERO";

    document.getElementById("infoDonTipo").textContent = donacion.tipoDonacion;
    document.getElementById("infoDonMonto").textContent = isDinero
        ? `S/ ${Number(donacion.saldoDisponible).toFixed(2)} disponible (original: S/ ${Number(donacion.cantidadOriginal).toFixed(2)})`
        : `${Number(donacion.saldoDisponible).toFixed(0)} unidades disponibles (original: ${donacion.cantidadOriginal})`;
    document.getElementById("infoDonDonante").textContent = donacion.donante;
    document.getElementById("infoDonActividad").textContent = donacion.actividadOrigen;
    infoBox.style.display = "block";

    if (!isDinero) {
        tipoSalidaHidden.value = "ESPECIE";
        labelCantidad.textContent = "Cantidad a distribuir *";
        seccionEspecie.style.display = "block";
    } else {
        tipoSalidaHidden.value = "DINERO";
        labelCantidad.textContent = "Monto a asignar (S/) *";
        seccionEspecie.style.display = "none";
    }

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
    document.getElementById("seccionEspecieSalida").style.display = "none";
    document.getElementById("labelCantidadSalida").textContent = "Monto a asignar (S/) *";
    tipoSalidaHidden.value = "DINERO";
    document.getElementById("cantidadSalida").removeAttribute("max");
    buscarDonacionInput.focus();
}

function cerrarDropdown() {
    donacionResultados.innerHTML = "";
    donacionResultados.classList.remove("open");
}

// ═══════════════════════════════════════════════════════
// MODAL: Abrir / Cerrar
// ═══════════════════════════════════════════════════════

function abrirModalSalida() {
    formSalida.reset();
    idSalidaInput.value = "";
    idDonacionHidden.value = "";
    accionSalidaInput.value = "registrar";
    document.getElementById("tituloModalSalida").innerHTML =
        '<i class="fa-solid fa-arrow-right-from-bracket"></i> Registrar Salida';
    btnGuardarSalida.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Registrar Salida';
    document.getElementById("infoDonacion").style.display = "none";
    document.getElementById("seccionEspecieSalida").style.display = "none";
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

// ═══════════════════════════════════════════════════════
// CARGAR DATOS: Actividades destino
// ═══════════════════════════════════════════════════════

async function cargarActividadesDestino() {
    try {
        const resp = await fetch("salidas-donaciones?accion=actividades");
        const actividades = await resp.json();

        actividadDestinoSelect.innerHTML = '<option value="">Seleccione campaña destino</option>';
        actividades.forEach(a => {
            const opt = document.createElement("option");
            opt.value = a.idActividad;
            opt.textContent = a.nombre;
            actividadDestinoSelect.appendChild(opt);
        });
    } catch (err) {
        console.error("Error al cargar actividades:", err);
    }
}

// ═══════════════════════════════════════════════════════
// EDITAR SALIDA
// ═══════════════════════════════════════════════════════

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

        // Obtener saldo disponible de la donación origen
        let saldoData = null;
        try {
            const saldoResp = await fetch(`salidas-donaciones?accion=saldo_donacion&id=${data.idDonacion}`);
            saldoData = await saldoResp.json();
        } catch (e) {
            console.warn("No se pudo obtener saldo:", e);
        }

        // Configurar donación seleccionada (no editable en modo edición)
        const donData = {
            id: data.idDonacion,
            tipoDonacion: saldoData ? saldoData.tipoDonacion : (data.tipoSalida === "ESPECIE" ? "OBJETO" : "DINERO"),
            cantidadOriginal: saldoData ? saldoData.cantidadOriginal : data.donacionCantidad,
            saldoDisponible: saldoData ? (Number(saldoData.saldoDisponible) + data.cantidad) : data.donacionCantidad,
            donante: data.donanteNombre || "ANÓNIMO",
            actividadOrigen: data.actividadNombre || "Origen",
            descripcion: data.donacionDescripcion || ""
        };

        seleccionarDonacion(donData);
        buscarDonacionInput.disabled = true;
        btnLimpiar.style.display = "none";

        actividadDestinoSelect.value = data.idActividad;
        document.getElementById("cantidadSalida").value = data.cantidad;
        document.getElementById("descripcionSalida").value = data.descripcion || "";

        if (data.tipoSalida === "ESPECIE") {
            tipoSalidaHidden.value = "ESPECIE";
            document.getElementById("seccionEspecieSalida").style.display = "block";
            document.getElementById("labelCantidadSalida").textContent = "Cantidad a distribuir *";
            if (data.cantidadItem) {
                document.getElementById("cantidadItemSalida").value = data.cantidadItem;
            }
            if (data.idItem) {
                document.getElementById("idItemSalida").value = data.idItem;
            }
        }

        modalSalida.style.display = "flex";
    } catch (err) {
        console.error("Error al obtener salida:", err);
        Notify.error("Error al cargar la información de la salida.");
    }
}

// ═══════════════════════════════════════════════════════
// ANULAR SALIDA
// ═══════════════════════════════════════════════════════

async function anularSalida(id) {
    const motivo = prompt("Motivo de anulación:", "Anulación manual");
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
        Notify.error("Error de conexión al anular la salida.");
    }
}

// ═══════════════════════════════════════════════════════
// CAMBIAR ESTADO
// ═══════════════════════════════════════════════════════

async function cambiarEstadoSalida(id, estado) {
    const ok = await Notify.confirm(`¿Cambiar estado a "${estado}"?`, '', { variant: 'info', okText: 'Sí, confirmar' });
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
        Notify.error("Error de conexión al cambiar el estado.");
    }
}

// ═══════════════════════════════════════════════════════
// FILTROS Y BÚSQUEDA (tabla principal)
// ═══════════════════════════════════════════════════════

function filtrarSalidas() {
    const texto = (buscarSalidasInput?.value || "").toLowerCase().trim();
    const filtroTipo = document.getElementById("filtroTipo")?.value || "";
    const filtroEstado = document.getElementById("filtroEstado")?.value || "";
    const filas = document.querySelectorAll("#tbodySalidas .salida-row");

    filas.forEach(fila => {
        const contenido = fila.textContent.toLowerCase();
        const tipo = fila.dataset.tipo || "";
        const estado = fila.dataset.estado || "";

        let visible = true;
        if (texto && !contenido.includes(texto)) visible = false;
        if (filtroTipo && tipo !== filtroTipo) visible = false;
        if (filtroEstado && estado !== filtroEstado) visible = false;

        fila.style.display = visible ? "" : "none";
    });

    paginacionSalidas();
}

if (buscarSalidasInput) {
    buscarSalidasInput.addEventListener("input", filtrarSalidas);
}

// ═══════════════════════════════════════════════════════
// PAGINACIÓN CLIENT-SIDE
// ═══════════════════════════════════════════════════════

let paginaActualS = 1;

function paginacionSalidas() {
    const filas = Array.from(document.querySelectorAll("#tbodySalidas .salida-row"))
        .filter(f => f.style.display !== "none");
    const totalPaginas = Math.ceil(filas.length / PAGINA_TAMANO_S) || 1;
    const paginacionDiv = document.getElementById("salidasPaginacion");
    const textoEl = document.getElementById("textoPaginacionSalidas");

    if (paginaActualS > totalPaginas) paginaActualS = totalPaginas;

    filas.forEach((fila, i) => {
        const pagina = Math.floor(i / PAGINA_TAMANO_S) + 1;
        fila.style.display = pagina === paginaActualS ? "" : "none";
    });

    if (filas.length > PAGINA_TAMANO_S) {
        paginacionDiv.style.display = "flex";
        textoEl.textContent = `Página ${paginaActualS} de ${totalPaginas}`;
        document.getElementById("btnPaginaAnteriorS").disabled = paginaActualS <= 1;
        document.getElementById("btnPaginaSiguienteS").disabled = paginaActualS >= totalPaginas;
    } else {
        paginacionDiv.style.display = "none";
    }
}

document.getElementById("btnPaginaAnteriorS")?.addEventListener("click", () => {
    if (paginaActualS > 1) { paginaActualS--; paginacionSalidas(); }
});
document.getElementById("btnPaginaSiguienteS")?.addEventListener("click", () => {
    paginaActualS++;
    paginacionSalidas();
});

// ═══════════════════════════════════════════════════════
// VALIDACIÓN ANTES DE ENVIAR
// ═══════════════════════════════════════════════════════

if (formSalida) {
    formSalida.addEventListener("submit", function (e) {
        const donacionId = idDonacionHidden.value;
        const actividad = actividadDestinoSelect.value;
        const cantidad = parseFloat(document.getElementById("cantidadSalida").value || 0);

        if (!donacionId) {
            e.preventDefault();
            Notify.warning("Debe buscar y seleccionar una donación origen.");
            buscarDonacionInput.focus();
            return false;
        }

        if (!actividad) {
            e.preventDefault();
            Notify.warning("Debe seleccionar una campaña/actividad destino.");
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
                const isDinero = donacionSeleccionadaData.tipoDonacion === "DINERO";
                const msg = isDinero
                    ? `El monto S/ ${cantidad.toFixed(2)} excede el saldo disponible de S/ ${saldo.toFixed(2)}.`
                    : `La cantidad ${cantidad} excede el saldo disponible de ${saldo} unidades.`;
                Notify.warning(msg);
                return false;
            }
        }

        buscarDonacionInput.disabled = false;
    });
}

// ═══════════════════════════════════════════════════════
// INICIALIZACIÓN
// ═══════════════════════════════════════════════════════

document.addEventListener("DOMContentLoaded", function () {
    inicializarAutocompletado();
    paginacionSalidas();
});
