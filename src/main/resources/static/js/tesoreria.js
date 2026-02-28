/* ============================================================
   TESORERÍA.JS — Sistema de Voluntariado
   CRUD, filtros, balance y gráficos
   ============================================================ */

const modal = document.getElementById("modalMovimiento");
const form  = document.getElementById("formMovimiento");
let editingId = null;
let chartMensual = null;
let chartCategoria = null;

// ====================== INICIALIZACIÓN ======================
document.addEventListener("DOMContentLoaded", () => {
    cargarBalance();
    cargarMovimientos();
    cargarGraficos();
    cargarActividades();

    // Fecha por defecto = hoy
    document.getElementById("fechaMovimiento").valueAsDate = new Date();
});

// ====================== BALANCE ======================
async function cargarBalance() {
    try {
        const resp = await fetch("tesoreria?accion=balance");
        const data = await resp.json();

        document.getElementById("totalIngresos").textContent   = "S/ " + parseFloat(data.ingresos).toFixed(2);
        document.getElementById("totalGastos").textContent     = "S/ " + parseFloat(data.gastos).toFixed(2);
        document.getElementById("saldoDisponible").textContent = "S/ " + parseFloat(data.saldo).toFixed(2);

        // Color del saldo
        const saldoEl = document.getElementById("saldoDisponible");
        saldoEl.style.color = data.saldo >= 0 ? "#10b981" : "#ef4444";
    } catch (e) {
        console.error("Error al cargar balance:", e);
    }
}

// ====================== CARGAR ACTIVIDADES ======================
async function cargarActividades() {
    try {
        const resp = await fetch("actividades?action=listar");
        const actividades = await resp.json();

        const select = document.getElementById("idActividad");
        select.innerHTML = '<option value="0">Ninguna</option>';

        actividades.forEach(act => {
            const opt = document.createElement("option");
            opt.value = act.idActividad;
            opt.textContent = act.nombre;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error("Error al cargar actividades:", e);
    }
}

// ====================== MODAL ======================
function abrirModal() {
    modal.style.display = "flex";
    form.reset();
    editingId = null;
    document.getElementById("tituloModal").textContent = "Registrar Movimiento";
    document.getElementById("fechaMovimiento").valueAsDate = new Date();
    cargarActividades();
}

function cerrarModal() {
    modal.style.display = "none";
}

// ====================== GUARDAR / ACTUALIZAR ======================
async function guardarMovimiento(event) {
    event.preventDefault();

    const params = new URLSearchParams();
    params.append("tipo", document.getElementById("tipo").value);
    params.append("monto", document.getElementById("monto").value);
    params.append("descripcion", document.getElementById("descripcion").value);
    params.append("categoria", document.getElementById("categoria").value);
    params.append("comprobante", document.getElementById("comprobante").value);
    params.append("fechaMovimiento", document.getElementById("fechaMovimiento").value);
    params.append("idActividad", document.getElementById("idActividad").value);

    if (editingId) {
        params.append("accion", "actualizar");
        params.append("idMovimiento", editingId);
    } else {
        params.append("accion", "registrar");
    }

    try {
        const resp = await fetch("tesoreria", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });

        const result = await resp.json();
        if (result.success) {
            cerrarModal();
            cargarMovimientos();
            cargarBalance();
            cargarGraficos();
        } else {
            Notify.error(result.message || "Error al guardar el movimiento");
        }
    } catch (e) {
        console.error("Error:", e);
    }
}

// ====================== EDITAR ======================
async function editarMovimiento(id) {
    abrirModal();
    editingId = id;

    await cargarActividades();

    try {
        const resp = await fetch("tesoreria?accion=obtener&id=" + id);
        const m = await resp.json();

        document.getElementById("tipo").value = m.tipo;
        document.getElementById("monto").value = m.monto;
        document.getElementById("descripcion").value = m.descripcion;
        document.getElementById("categoria").value = m.categoria;
        document.getElementById("comprobante").value = m.comprobante || "";
        document.getElementById("fechaMovimiento").value = m.fechaMovimiento;
        document.getElementById("idActividad").value = m.idActividad || 0;

        document.getElementById("tituloModal").textContent = "Editar Movimiento";
    } catch (e) {
        console.error("Error al obtener movimiento:", e);
    }
}

// ====================== ELIMINAR ======================
async function eliminarMovimiento(id) {
    const ok = await Notify.confirm("¿Eliminar este movimiento?", "Esta acción no se puede deshacer.", { variant: 'danger', okText: 'Sí, eliminar' });
    if (!ok) return;

    try {
        const resp = await fetch("tesoreria?accion=eliminar&id=" + id);
        const result = await resp.json();

        if (result.success) {
            cargarMovimientos();
            cargarBalance();
            cargarGraficos();
        }
    } catch (e) {
        console.error("Error al eliminar:", e);
    }
}

// ====================== CARGAR TABLA ======================
async function cargarMovimientos() {
    try {
        const resp = await fetch("tesoreria?accion=listar");
        const data = await resp.json();
        renderTabla(data);
    } catch (e) {
        console.error("Error al cargar movimientos:", e);
    }
}

// ====================== FILTRAR ======================
async function filtrarMovimientos() {
    const tipo      = document.getElementById("filtroTipo").value;
    const categoria = document.getElementById("filtroCategoria").value;
    const fechaIni  = document.getElementById("filtroFechaIni").value;
    const fechaFin  = document.getElementById("filtroFechaFin").value;

    const params = new URLSearchParams();
    params.append("accion", "filtrar");
    if (tipo)      params.append("tipo", tipo);
    if (categoria) params.append("categoria", categoria);
    if (fechaIni)  params.append("fechaIni", fechaIni);
    if (fechaFin)  params.append("fechaFin", fechaFin);

    try {
        const resp = await fetch("tesoreria?" + params.toString());
        const data = await resp.json();
        renderTabla(data);
    } catch (e) {
        console.error("Error al filtrar:", e);
    }
}

function limpiarFiltros() {
    document.getElementById("filtroTipo").value = "";
    document.getElementById("filtroCategoria").value = "";
    document.getElementById("filtroFechaIni").value = "";
    document.getElementById("filtroFechaFin").value = "";
    cargarMovimientos();
}

// ====================== RENDER TABLA ======================
function renderTabla(data) {
    const tbody = document.getElementById("tbodyMovimientos");

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">No hay movimientos registrados</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(m => `
        <tr>
            <td>
                <span class="tag ${m.tipo === 'INGRESO' ? 'tag-ingreso' : 'tag-gasto'}">
                    ${m.tipo}
                </span>
            </td>
            <td><strong>S/ ${parseFloat(m.monto).toFixed(2)}</strong></td>
            <td>${m.descripcion}</td>
            <td>${m.categoria}</td>
            <td>${m.comprobante || '—'}</td>
            <td>${m.fechaMovimiento}</td>
            <td>${m.actividad || '—'}</td>
            <td>${m.usuarioRegistro}</td>
            <td class="acciones-cell">
                <button class="btn-icon edit" onclick="editarMovimiento(${m.idMovimiento})" title="Editar">✎</button>
                <button class="btn-icon delete" onclick="eliminarMovimiento(${m.idMovimiento})" title="Eliminar">🗑</button>
            </td>
        </tr>
    `).join("");
}

// ====================== GRÁFICOS ======================
async function cargarGraficos() {
    await cargarGraficoMensual();
    await cargarGraficoCategoria();
}

async function cargarGraficoMensual() {
    try {
        const resp = await fetch("tesoreria?accion=resumenMensual");
        const data = await resp.json();

        const meses    = data.map(d => d.mes).reverse();
        const ingresos = data.map(d => d.ingresos).reverse();
        const gastos   = data.map(d => d.gastos).reverse();

        if (chartMensual) chartMensual.destroy();

        const ctx = document.getElementById("chartMensual").getContext("2d");
        chartMensual = new Chart(ctx, {
            type: "line",
            data: {
                labels: meses,
                datasets: [
                    {
                        label: "Ingresos",
                        data: ingresos,
                        borderColor: "#10b981",
                        backgroundColor: "rgba(16,185,129,0.1)",
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: "Gastos",
                        data: gastos,
                        borderColor: "#ef4444",
                        backgroundColor: "rgba(239,68,68,0.1)",
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: "bottom" }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    } catch (e) {
        console.error("Error gráfico mensual:", e);
    }
}

async function cargarGraficoCategoria() {
    try {
        const resp = await fetch("tesoreria?accion=resumenCategoria");
        const data = await resp.json();

        const categorias = [...new Set(data.map(d => d.categoria))];
        const colores = [
            "#667eea", "#764ba2", "#10b981", "#f59e0b",
            "#ef4444", "#3b82f6", "#8b5cf6", "#ec4899",
            "#06b6d4", "#84cc16"
        ];

        const totales = categorias.map(cat => {
            const items = data.filter(d => d.categoria === cat);
            return items.reduce((sum, i) => sum + i.total, 0);
        });

        if (chartCategoria) chartCategoria.destroy();

        const ctx = document.getElementById("chartCategoria").getContext("2d");
        chartCategoria = new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: categorias,
                datasets: [{
                    data: totales,
                    backgroundColor: colores.slice(0, categorias.length),
                    borderWidth: 2,
                    borderColor: "#fff"
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: "bottom" }
                }
            }
        });
    } catch (e) {
        console.error("Error gráfico categoría:", e);
    }
}
