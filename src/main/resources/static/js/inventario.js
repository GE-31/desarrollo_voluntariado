/* ═══════════════════════════════════════════════════════════
   inventario.js — Categorías + Items Base (dos pestañas)
   Diseño JSP → Thymeleaf
   ═══════════════════════════════════════════════════════════ */

const modalInventario = document.getElementById("modalInventario");
const formInventario  = document.getElementById("formInventario");
const modalMovimiento = document.getElementById("modalMovimiento");
const modalCategoria  = document.getElementById("modalCategoria");
const formCategoria   = document.getElementById("formCategoria");

const PAGINA_TAMANO_INVENTARIO = 5;
const PAGINA_TAMANO_CATEGORIAS = 12;

let paginaActualInventario   = 1;
let totalPaginasInventario   = 1;
let inventarioFiltradoActual = [];
let editingIdInventario      = null;

let paginaActualCategorias   = 1;
let totalPaginasCategorias   = 1;
let categoriasListaActual    = [];
let editingIdCategoria       = null;
let tabActivo = "tabCategorias";

/* ─── Inicialización ─────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", () => {
    cargarCategoriasDinamicas().then(() => {
        configurarFiltros();
        configurarPaginacionInventario();
        configurarPaginacionCategorias();
        cargarStockBajo();
        configurarUnidadMedida();
        filtrarInventario();
        cargarCategorias();
    });
});

/* ═══════════════════ PESTANAS ═══════════════════ */
function cambiarTab(tab) {
    tabActivo = tab;
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelector(`.tab-btn[data-tab="${tab}"]`).classList.add("active");

    document.querySelectorAll(".tab-content").forEach(c => {
        c.style.display = "none";
        c.classList.remove("active");
    });
    const el = document.getElementById(tab);
    if (el) { el.style.display = "block"; el.classList.add("active"); }

    const btnTexto = document.getElementById("btnNuevoTexto");
    if (tab === "tabCategorias") {
        if (btnTexto) btnTexto.textContent = "Nueva Categoria";
    } else {
        if (btnTexto) btnTexto.textContent = "Nuevo Item Base";
    }
}

function accionNuevo() {
    if (tabActivo === "tabCategorias") {
        abrirModalCategoria();
    } else {
        abrirModal();
    }
}

/* ═══════════════════ CATEGORIAS ═══════════════════ */
async function cargarCategorias() {
    try {
        const resp = await fetch("inventario?accion=listar_categorias");
        categoriasListaActual = await resp.json();
        paginaActualCategorias = 1;
        totalPaginasCategorias = Math.max(1, Math.ceil(categoriasListaActual.length / PAGINA_TAMANO_CATEGORIAS));
        renderPaginaCategorias();
    } catch (e) {
        console.error("Error cargando categorias:", e);
    }
}

function renderPaginaCategorias() {
    const grid = document.getElementById("categoriasGrid");
    if (!grid) return;

    const inicio = (paginaActualCategorias - 1) * PAGINA_TAMANO_CATEGORIAS;
    const pagina = categoriasListaActual.slice(inicio, inicio + PAGINA_TAMANO_CATEGORIAS);

    if (pagina.length === 0) {
        grid.innerHTML = '<div class="no-data-cats">No hay categorias registradas. Crea la primera.</div>';
    } else {
        grid.innerHTML = pagina.map(cat => {
            const color = cat.color || "#6366f1";
            const icono = cat.icono || "fa-box";
            return `<div class="cat-card" style="border-left:4px solid ${color}">
                <div class="cat-card-header">
                    <div class="cat-icon" style="background:${color}20; color:${color}">
                        <i class="fa-solid ${icono}"></i>
                    </div>
                    <div class="cat-info">
                        <h3>${cat.nombre}</h3>
                        <p>${cat.descripcion || '<em>Sin descripcion</em>'}</p>
                    </div>
                </div>
                <div class="cat-card-actions">
                    <button class="btn-icon edit" onclick="editarCategoria(${cat.idCategoria})" title="Editar"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon disable" onclick="eliminarCategoria(${cat.idCategoria}, '${cat.nombre.replace(/'/g, "\\'")}')" title="Eliminar"><i class="fa-solid fa-trash"></i></button>
                </div>
            </div>`;
        }).join("");
    }

    const pag = document.getElementById("categoriasPaginacion");
    if (pag) {
        pag.style.display = totalPaginasCategorias > 1 ? "flex" : "none";
        document.getElementById("textoPaginacionCategorias").textContent =
            "Pagina " + paginaActualCategorias + " de " + totalPaginasCategorias;
        document.getElementById("btnCategoriasAnterior").disabled = paginaActualCategorias <= 1;
        document.getElementById("btnCategoriasSiguiente").disabled = paginaActualCategorias >= totalPaginasCategorias;
    }
}

function configurarPaginacionCategorias() {
    const btnAnt = document.getElementById("btnCategoriasAnterior");
    const btnSig = document.getElementById("btnCategoriasSiguiente");
    if (btnAnt) btnAnt.addEventListener("click", () => { if (paginaActualCategorias > 1) { paginaActualCategorias--; renderPaginaCategorias(); } });
    if (btnSig) btnSig.addEventListener("click", () => { if (paginaActualCategorias < totalPaginasCategorias) { paginaActualCategorias++; renderPaginaCategorias(); } });
}

function abrirModalCategoria() {
    editingIdCategoria = null;
    if (formCategoria) formCategoria.reset();
    document.getElementById("catIdCategoria").value = "";
    document.getElementById("tituloCatModal").textContent = "Nueva Categoria";
    document.getElementById("catColor").value = "#6366f1";
    document.getElementById("catIcono").value = "fa-box";
    if (modalCategoria) modalCategoria.style.display = "flex";
}

function cerrarModalCategoria() {
    if (modalCategoria) modalCategoria.style.display = "none";
}

async function editarCategoria(id) {
    const cat = categoriasListaActual.find(c => c.idCategoria === id);
    if (!cat) { Notify.error("Categoría no encontrada"); return; }
    editingIdCategoria = id;
    document.getElementById("tituloCatModal").textContent = "Editar Categoria";
    document.getElementById("catIdCategoria").value = cat.idCategoria;
    document.getElementById("catNombre").value = cat.nombre;
    document.getElementById("catDescripcion").value = cat.descripcion || "";
    document.getElementById("catColor").value = cat.color || "#6366f1";
    document.getElementById("catIcono").value = cat.icono || "fa-box";
    if (modalCategoria) modalCategoria.style.display = "flex";
}

async function guardarCategoria(event) {
    event.preventDefault();
    const params = new URLSearchParams();
    params.append("accion", editingIdCategoria ? "actualizar_categoria" : "registrar_categoria");
    if (editingIdCategoria) params.append("idCategoria", editingIdCategoria);
    params.append("nombre", document.getElementById("catNombre").value.trim());
    params.append("descripcion", document.getElementById("catDescripcion").value.trim());
    params.append("color", document.getElementById("catColor").value);
    params.append("icono", document.getElementById("catIcono").value);
    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.success) {
            cerrarModalCategoria();
            await cargarCategorias();
            await cargarCategoriasDinamicas();
        } else {
            Notify.error(result.message || "No se pudo guardar la categoría");
        }
    } catch (e) {
        console.error("Error guardando categoria:", e);
        Notify.error("Error al guardar la categoría");
    }
}

async function eliminarCategoria(id, nombre) {
    const ok = await Notify.confirm(`¿Eliminar la categoría "${nombre}"?`, 'Los ítems que la usen conservarán su categoría actual.', { variant: 'danger', okText: 'Sí, eliminar' });
    if (!ok) return;
    const params = new URLSearchParams();
    params.append("accion", "eliminar_categoria");
    params.append("idCategoria", id);
    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.success) {
            await cargarCategorias();
            await cargarCategoriasDinamicas();
        } else {
            Notify.error(result.message || "No se pudo eliminar la categoría");
        }
    } catch (e) {
        console.error("Error eliminando categoria:", e);
    }
}

/* ═══════════════════ CATEGORIAS DINAMICAS (selects) ═══════════════════ */
async function cargarCategoriasDinamicas() {
    try {
        const resp = await fetch("inventario?accion=listar_categorias");
        const categorias = await resp.json();
        const filtroCat = document.getElementById("filtroCategoria");
        if (filtroCat) {
            filtroCat.innerHTML = '<option value="">Todas</option>';
            (categorias || []).forEach(cat => {
                const opt = document.createElement("option");
                opt.value = cat.nombre;
                opt.textContent = cat.nombre;
                filtroCat.appendChild(opt);
            });
        }
        const formCat = document.getElementById("categoria");
        if (formCat) {
            formCat.innerHTML = '<option value="">Seleccione</option>';
            (categorias || []).forEach(cat => {
                const opt = document.createElement("option");
                opt.value = cat.nombre;
                opt.textContent = cat.nombre;
                formCat.appendChild(opt);
            });
        }
    } catch (e) {
        console.error("Error cargando categorias dinamicas:", e);
    }
}

/* ═══════════════════ ITEMS BASE ═══════════════════ */
function configurarFiltros() {
    const q = document.getElementById("filtroQ");
    const categoria = document.getElementById("filtroCategoria");
    const estado = document.getElementById("filtroEstado");
    const stockBajo = document.getElementById("filtroStockBajo");
    if (q) q.addEventListener("input", filtrarInventario);
    if (categoria) categoria.addEventListener("change", filtrarInventario);
    if (estado) estado.addEventListener("change", filtrarInventario);
    if (stockBajo) stockBajo.addEventListener("change", filtrarInventario);
}

function abrirModal() {
    editingIdInventario = null;
    formInventario.reset();
    document.getElementById("idItem").value = "";
    document.getElementById("tituloModal").textContent = "Nuevo Item";
    mostrarUnidadOtro(false);
    modalInventario.style.display = "flex";
}

function cerrarModal() {
    modalInventario.style.display = "none";
}

async function guardarItem(event) {
    event.preventDefault();
    const params = new URLSearchParams();
    params.append("accion", editingIdInventario ? "actualizar" : "registrar");
    if (editingIdInventario) params.append("idItem", editingIdInventario);
    params.append("nombre", document.getElementById("nombre").value);
    params.append("categoria", document.getElementById("categoria").value);
    params.append("unidadMedida", resolverUnidadMedida());
    params.append("stockMinimo", document.getElementById("stockMinimo").value);
    params.append("observacion", document.getElementById("observacion").value);
    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.success) {
            cerrarModal();
            await filtrarInventario();
            await cargarStockBajo();
        } else {
            Notify.error(result.message || "No se pudo guardar el ítem");
        }
    } catch (e) {
        console.error("Error guardando item:", e);
        Notify.error("Error al guardar el ítem");
    }
}

async function editarItem(id) {
    try {
        const resp = await fetch("inventario?accion=obtener&id=" + id);
        const item = await resp.json();
        if (!item || item.error) { Notify.error("No se pudo cargar el ítem"); return; }
        editingIdInventario = id;
        document.getElementById("tituloModal").textContent = "Editar Item";
        document.getElementById("idItem").value = item.idItem;
        document.getElementById("nombre").value = item.nombre;
        document.getElementById("categoria").value = item.categoria;
        document.getElementById("stockMinimo").value = item.stockMinimo;
        document.getElementById("observacion").value = item.observacion || "";
        const unidades = ["unidad","kg","litro","caja","paquete","lata","botella"];
        if (unidades.includes(item.unidadMedida)) {
            document.getElementById("unidadMedida").value = item.unidadMedida;
            mostrarUnidadOtro(false);
        } else {
            document.getElementById("unidadMedida").value = "__OTRO__";
            mostrarUnidadOtro(true);
            document.getElementById("unidadMedidaOtro").value = item.unidadMedida;
        }
        modalInventario.style.display = "flex";
    } catch (e) {
        console.error("Error cargando item:", e);
        Notify.error("Error al cargar el ítem");
    }
}

async function cambiarEstado(id, nuevoEstado) {
    const accion = nuevoEstado === "INACTIVO" ? "desactivar" : "activar";
    const ok = await Notify.confirm(`¿${accion.charAt(0).toUpperCase() + accion.slice(1)} este ítem?`, '', { variant: 'warning', okText: 'Sí, continuar' });
    if (!ok) return;
    const params = new URLSearchParams();
    params.append("accion", "cambiar_estado");
    params.append("idItem", id);
    params.append("estado", nuevoEstado);
    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.success) {
            await filtrarInventario();
            await cargarStockBajo();
        } else {
            Notify.error(result.message || "No se pudo cambiar el estado");
        }
    } catch (e) {
        console.error("Error cambiando estado:", e);
    }
}

/* ─── Movimiento ─────────────────────────────────── */
function abrirModalMovimiento(idItem) {
    fetch("inventario?accion=obtener&id=" + idItem)
        .then(r => r.json())
        .then(item => {
            document.getElementById("movIdItem").value = item.idItem;
            document.getElementById("movNombreItem").value = item.nombre;
            document.getElementById("movStockActual").value = item.stockActual;
            document.getElementById("movTipo").value = "";
            document.getElementById("movCantidad").value = "";
            document.getElementById("movMotivo").value = "";
            document.getElementById("movObservacion").value = "";
            modalMovimiento.style.display = "flex";
        })
        .catch(e => { console.error(e); Notify.error("Error al cargar ítem"); });
}

function cerrarModalMovimiento() {
    modalMovimiento.style.display = "none";
}

async function guardarMovimiento(event) {
    event.preventDefault();
    const params = new URLSearchParams();
    params.append("accion", "registrar_movimiento");
    params.append("idItem", document.getElementById("movIdItem").value);
    params.append("tipo", document.getElementById("movTipo").value);
    params.append("cantidad", document.getElementById("movCantidad").value);
    params.append("motivo", document.getElementById("movMotivo").value);
    params.append("observacion", document.getElementById("movObservacion").value);
    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.success) {
            cerrarModalMovimiento();
            await filtrarInventario();
            await cargarStockBajo();
        } else {
            Notify.error(result.message || "No se pudo registrar el movimiento");
        }
    } catch (e) {
        console.error("Error registrando movimiento:", e);
        Notify.error("Error al registrar movimiento");
    }
}

/* ─── Filtro / paginacion items ──────────────────── */
async function filtrarInventario() {
    const q         = (document.getElementById("filtroQ")?.value || "").toLowerCase();
    const categoria = document.getElementById("filtroCategoria")?.value || "";
    const estado    = document.getElementById("filtroEstado")?.value || "";
    const soloStock = document.getElementById("filtroStockBajo")?.checked || false;
    try {
        const resp = await fetch("inventario?accion=listar");
        const items = await resp.json();
        inventarioFiltradoActual = items.filter(item => {
            const matchQ    = !q || item.nombre.toLowerCase().includes(q) || (item.observacion||"").toLowerCase().includes(q);
            const matchCat  = !categoria || item.categoria === categoria;
            const matchEst  = !estado || item.estado === estado;
            const matchStock= !soloStock || item.stockActual <= item.stockMinimo;
            return matchQ && matchCat && matchEst && matchStock;
        });
        paginaActualInventario = 1;
        totalPaginasInventario = Math.max(1, Math.ceil(inventarioFiltradoActual.length / PAGINA_TAMANO_INVENTARIO));
        renderPaginaInventario();
    } catch (e) {
        console.error("Error filtrando inventario:", e);
    }
}

function renderPaginaInventario() {
    const tbody = document.getElementById("tbodyInventario");
    if (!tbody) return;
    const inicio = (paginaActualInventario - 1) * PAGINA_TAMANO_INVENTARIO;
    const pagina = inventarioFiltradoActual.slice(inicio, inicio + PAGINA_TAMANO_INVENTARIO);
    if (pagina.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="no-data">No hay items que coincidan con los filtros</td></tr>';
    } else {
        tbody.innerHTML = pagina.map(item => {
            const stockClass = item.stockActual <= item.stockMinimo ? "stock-bajo" : "stock-ok";
            const estadoTag  = item.estado === "ACTIVO" ? "tag-activo" : "tag-inactivo";
            const btnEstado  = item.estado === "ACTIVO"
                ? `<button class="btn-icon disable" onclick="cambiarEstado(${item.idItem},'INACTIVO')" title="Desactivar">&#8856;</button>`
                : `<button class="btn-icon enable"  onclick="cambiarEstado(${item.idItem},'ACTIVO')"   title="Activar">&#10003;</button>`;
            return `<tr>
                <td>${item.categoria}</td>
                <td><strong>${item.nombre}</strong><div class="item-obs">${item.observacion||""}</div></td>
                <td>${item.unidadMedida}</td>
                <td><span class="${stockClass}">${item.stockActual}</span></td>
                <td>${item.stockMinimo}</td>
                <td><span class="tag ${estadoTag}">${item.estado}</span></td>
                <td>${item.actualizadoEn || item.creadoEn || ""}</td>
                <td class="acciones-cell">
                    <button class="btn-icon edit" onclick="abrirModalMovimiento(${item.idItem})" title="Registrar movimiento"><i class="fa-solid fa-right-left"></i></button>
                    <button class="btn-icon edit" onclick="editarItem(${item.idItem})" title="Editar">&#9998;</button>
                    ${btnEstado}
                </td>
            </tr>`;
        }).join("");
    }
    const pag = document.getElementById("inventarioPaginacion");
    if (pag) {
        pag.style.display = totalPaginasInventario > 1 ? "flex" : "none";
        document.getElementById("textoPaginacionInventario").textContent =
            "Pagina " + paginaActualInventario + " de " + totalPaginasInventario;
        document.getElementById("btnInventarioAnterior").disabled = paginaActualInventario <= 1;
        document.getElementById("btnInventarioSiguiente").disabled = paginaActualInventario >= totalPaginasInventario;
    }
}

function configurarPaginacionInventario() {
    const btnAnt = document.getElementById("btnInventarioAnterior");
    const btnSig = document.getElementById("btnInventarioSiguiente");
    if (btnAnt) btnAnt.addEventListener("click", () => { if (paginaActualInventario > 1) { paginaActualInventario--; renderPaginaInventario(); } });
    if (btnSig) btnSig.addEventListener("click", () => { if (paginaActualInventario < totalPaginasInventario) { paginaActualInventario++; renderPaginaInventario(); } });
}

async function cargarStockBajo() {
    try {
        const resp = await fetch("inventario?accion=stock_bajo");
        const data = await resp.json();
        const el = document.getElementById("stockBajoTotal");
        if (el && data.cantidad !== undefined) el.textContent = data.cantidad;
    } catch (e) { /* silencioso */ }
}

function limpiarFiltros() {
    const q = document.getElementById("filtroQ"); if (q) q.value = "";
    const c = document.getElementById("filtroCategoria"); if (c) c.value = "";
    const e = document.getElementById("filtroEstado"); if (e) e.value = "";
    const s = document.getElementById("filtroStockBajo"); if (s) s.checked = false;
    filtrarInventario();
}

function configurarUnidadMedida() {
    const sel = document.getElementById("unidadMedida");
    if (sel) sel.addEventListener("change", () => mostrarUnidadOtro(sel.value === "__OTRO__"));
}

function mostrarUnidadOtro(show) {
    const inp = document.getElementById("unidadMedidaOtro");
    if (inp) { inp.style.display = show ? "block" : "none"; inp.required = show; if (!show) inp.value = ""; }
}

function resolverUnidadMedida() {
    const sel = document.getElementById("unidadMedida");
    if (sel.value === "__OTRO__") return document.getElementById("unidadMedidaOtro").value.trim();
    return sel.value;
}