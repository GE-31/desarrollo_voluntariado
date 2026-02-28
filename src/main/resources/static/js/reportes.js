// ═══════════════════════════════════════════════════════════
//  REPORTES – Sistema de Voluntariado Universitario
//  Dashboard profesional: KPIs + tablas por módulo + export
// ═══════════════════════════════════════════════════════════

// ── Utilidades ──

function getContextPath() {
    // Usa la variable CTX inyectada por Thymeleaf, o detecta por pathname
    if (typeof CTX !== 'undefined' && CTX) {
        let c = CTX.replace(/\/+$/, '');
        return c || '';
    }
    const path = window.location.pathname;
    const idx = path.indexOf('/', 1);
    return idx > 0 ? path.substring(0, idx) : '';
}

function fmt(v) { return v != null ? v : ''; }
function fmtNum(v) { return v != null ? Number(v).toLocaleString('es-PE') : '0'; }
function fmtMoney(v) { return 'S/ ' + (v != null ? Number(v).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'); }
function fmtDate(d) {
    if (!d) return '';
    if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10);
    return d;
}

function estadoBadge(estado) {
    if (!estado) return '<span class="badge badge-neutral">—</span>';
    const s = estado.toUpperCase();
    if (s === 'ACTIVO' || s === 'CONFIRMADO') return `<span class="badge badge-success">${s === 'ACTIVO' ? 'ACTIVO' : 'CONFIRMADO'}</span>`;
    if (s === 'FINALIZADO' || s === 'COMPLETADO') return `<span class="badge badge-info">${s}</span>`;
    if (s === 'PENDIENTE' || s === 'EN_PROGRESO' || s === 'EN PROGRESO') return `<span class="badge badge-warning">${s}</span>`;
    if (s === 'ANULADO' || s === 'INACTIVO' || s === 'RECHAZADO' || s === 'FALTA') return `<span class="badge badge-danger">${s}</span>`;
    if (s === 'TARDANZA') return `<span class="badge badge-warning">${s}</span>`;
    if (s === 'ASISTIO') return `<span class="badge badge-success">ASISTIÓ</span>`;
    return `<span class="badge badge-neutral">${s}</span>`;
}

// ── Estado global ──
let moduloActual = 'voluntarios';
let datosModuloActual = [];

// ── Mapa de endpoints de exportación por módulo ──
const EXPORT_MAP = {
    voluntarios: 'exportar_voluntarios',
    actividades: 'exportar_actividades',
    asistencias: 'exportar_asistencias',
    beneficiarios: 'exportar_beneficiarios',
    donaciones: 'donaciones',  // legacy endpoint
    inventario: 'exportar_inventario',
    tesoreria: 'exportar_tesoreria'
};

// Mapa de accion para fetch JSON por modulo
const FETCH_MAP = {
    voluntarios: 'voluntarios',
    actividades: 'actividades',
    asistencias: 'asistencias',
    beneficiarios: 'beneficiarios',
    donaciones: 'donaciones_lista',
    inventario: 'inventario',
    tesoreria: 'tesoreria'
};

// ══════════════════════════════════════════
//  1. CARGAR RESUMEN / KPIs
// ══════════════════════════════════════════

async function cargarResumen() {
    const ctx = getContextPath();
    try {
        const resp = await fetch(`${ctx}/reportes?accion=resumen`);
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        const r = await resp.json();

        document.getElementById('kpiVoluntarios').textContent = fmtNum(r.totalVoluntarios);
        document.getElementById('kpiVolActivos').textContent = fmtNum(r.voluntariosActivos) + ' activos';

        document.getElementById('kpiBeneficiarios').textContent = fmtNum(r.totalBeneficiarios);
        document.getElementById('kpiBenActivos').textContent = fmtNum(r.beneficiariosActivos) + ' activos';

        document.getElementById('kpiActividades').textContent = fmtNum(r.totalActividades);
        document.getElementById('kpiActActivas').textContent = fmtNum(r.actividadesActivas) + ' activas · ' + fmtNum(r.actividadesFinalizadas) + ' finalizadas';

        document.getElementById('kpiAsistencias').textContent = fmtNum(r.totalAsistencias);
        document.getElementById('kpiHoras').textContent = fmtNum(r.totalHorasVoluntarias) + ' hrs voluntarias';

        document.getElementById('kpiDonaciones').textContent = fmtNum(r.totalDonaciones);
        document.getElementById('kpiRecaudado').textContent = fmtMoney(r.totalDonacionesDinero) + ' recaudado';

        document.getElementById('kpiInventario').textContent = fmtNum(r.totalItemsInventario);
        document.getElementById('kpiStockBajo').textContent = fmtNum(r.itemsStockBajo) + ' stock bajo';

        document.getElementById('kpiSaldo').textContent = fmtMoney(r.saldoActual);
        document.getElementById('kpiIngresos').textContent = 'Ing: ' + fmtMoney(r.totalIngresos) + ' | Egr: ' + fmtMoney(r.totalEgresos);

    } catch (e) {
        console.error('Error cargando resumen:', e);
    }
}

// ══════════════════════════════════════════
//  2. CARGAR DATOS DE MÓDULO EN TABLA
// ══════════════════════════════════════════

async function cargarModulo(modulo) {
    moduloActual = modulo;
    const ctx = getContextPath();
    const accion = FETCH_MAP[modulo] || modulo;
    const content = document.getElementById('modulosContent');
    const footer = document.getElementById('tableFooter');

    // Loading
    content.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><span>Cargando datos...</span></div>';
    footer.style.display = 'none';

    try {
        const resp = await fetch(`${ctx}/reportes?accion=${accion}`);
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        const data = await resp.json();
        datosModuloActual = data;

        if (!data || data.length === 0) {
            content.innerHTML = '<div class="empty-state"><div class="empty-icon">📭</div><div class="empty-text">No hay registros disponibles</div></div>';
            footer.style.display = 'none';
            return;
        }

        renderTabla(modulo, data);
        footer.style.display = 'block';
        document.getElementById('recordCount').textContent = data.length + ' registros';

    } catch (e) {
        console.error('Error cargando módulo', modulo, e);
        content.innerHTML = '<div class="empty-state"><div class="empty-icon">⚠️</div><div class="empty-text">Error al cargar datos</div></div>';
    }
}

// ══════════════════════════════════════════
//  3. RENDER DE TABLAS POR MÓDULO
// ══════════════════════════════════════════

function renderTabla(modulo, data) {
    const container = document.getElementById('modulosContent');
    const renderers = {
        voluntarios: renderVoluntarios,
        actividades: renderActividades,
        asistencias: renderAsistencias,
        beneficiarios: renderBeneficiarios,
        donaciones: renderDonaciones,
        inventario: renderInventario,
        tesoreria: renderTesoreria
    };
    const fn = renderers[modulo];
    if (fn) {
        container.innerHTML = fn(data);
    }
}

function renderVoluntarios(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Nombres</th><th>Apellidos</th><th>DNI</th><th>Correo</th><th>Teléfono</th><th>Carrera</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((v, i) => {
        html += `<tr>
            <td>${i + 1}</td><td>${fmt(v.nombres)}</td><td>${fmt(v.apellidos)}</td>
            <td>${fmt(v.dni)}</td><td>${fmt(v.correo)}</td><td>${fmt(v.telefono)}</td>
            <td>${fmt(v.carrera)}</td><td>${estadoBadge(v.estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderActividades(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Nombre</th><th>Descripción</th><th>Fecha Inicio</th><th>Fecha Fin</th><th>Ubicación</th><th>Cupo</th><th>Inscritos</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((a, i) => {
        html += `<tr>
            <td>${i + 1}</td><td>${fmt(a.nombre)}</td><td>${fmt(a.descripcion)}</td>
            <td>${fmtDate(a.fechaInicio)}</td><td>${fmtDate(a.fechaFin)}</td>
            <td>${fmt(a.ubicacion)}</td><td>${a.cupoMaximo ?? ''}</td><td>${a.inscritos ?? ''}</td>
            <td>${estadoBadge(a.estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderAsistencias(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Voluntario</th><th>DNI</th><th>Actividad</th><th>Fecha</th><th>Entrada</th><th>Salida</th><th>Horas</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((a, i) => {
        html += `<tr>
            <td>${i + 1}</td><td>${fmt(a.nombreVoluntario)}</td><td>${fmt(a.dniVoluntario)}</td>
            <td>${fmt(a.nombreActividad)}</td><td>${fmtDate(a.fecha)}</td>
            <td>${fmt(a.horaEntrada)}</td><td>${fmt(a.horaSalida)}</td>
            <td>${a.horasTotales != null ? Number(a.horasTotales).toFixed(1) : ''}</td>
            <td>${estadoBadge(a.estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderBeneficiarios(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Nombres</th><th>Apellidos</th><th>DNI</th><th>Teléfono</th><th>Distrito</th><th>Tipo</th><th>Necesidad</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((b, i) => {
        html += `<tr>
            <td>${i + 1}</td><td>${fmt(b.nombres)}</td><td>${fmt(b.apellidos)}</td>
            <td>${fmt(b.dni)}</td><td>${fmt(b.telefono)}</td><td>${fmt(b.distrito)}</td>
            <td>${fmt(b.tipoBeneficiario)}</td><td>${fmt(b.necesidadPrincipal)}</td>
            <td>${estadoBadge(b.estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderDonaciones(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Fecha</th><th>Tipo</th><th>Monto</th><th>Donante</th><th>Nombre</th><th>Correo</th><th>Actividad</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((d, i) => {
        const tipo = (d.idTipoDonacion === 1 || (d.tipoDonacion && d.tipoDonacion.toUpperCase().includes('DINERO')))
            ? '<span class="badge badge-success">Dinero</span>'
            : '<span class="badge badge-warning">En especie</span>';
        let estado = (d.estado || 'PENDIENTE').toUpperCase();
        if (estado === 'ACTIVO') estado = 'CONFIRMADO';
        html += `<tr>
            <td>${i + 1}</td><td>${fmtDate(d.registradoEn)}</td><td>${tipo}</td>
            <td>${fmtMoney(d.cantidad)}</td><td>${fmt(d.tipoDonante)}</td>
            <td>${fmt(d.nombreDonante || d.donanteNombre)}</td><td>${fmt(d.correoDonante)}</td>
            <td>${fmt(d.actividad)}</td><td>${estadoBadge(estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderInventario(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Nombre</th><th>Categoría</th><th>Unidad</th><th>Stock Actual</th><th>Stock Mínimo</th><th>Estado</th>
    </tr></thead><tbody>`;
    data.forEach((item, i) => {
        const stockClass = (item.stockActual <= item.stockMinimo) ? 'badge badge-danger' : '';
        html += `<tr>
            <td>${i + 1}</td><td>${fmt(item.nombre)}</td><td>${fmt(item.categoria)}</td>
            <td>${fmt(item.unidadMedida)}</td>
            <td><span class="${stockClass}">${item.stockActual ?? ''}</span></td>
            <td>${item.stockMinimo ?? ''}</td>
            <td>${estadoBadge(item.estado)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

function renderTesoreria(data) {
    let html = `<table class="data-table"><thead><tr>
        <th>#</th><th>Fecha</th><th>Tipo</th><th>Categoría</th><th>Descripción</th><th>Monto</th><th>Comprobante</th>
    </tr></thead><tbody>`;
    data.forEach((m, i) => {
        const tipoClass = (m.tipo && m.tipo.toUpperCase() === 'INGRESO') ? 'badge-success' : 'badge-danger';
        html += `<tr>
            <td>${i + 1}</td><td>${fmtDate(m.fechaMovimiento)}</td>
            <td><span class="badge ${tipoClass}">${fmt(m.tipo)}</span></td>
            <td>${fmt(m.categoria)}</td><td>${fmt(m.descripcion)}</td>
            <td>${fmtMoney(m.monto)}</td><td>${fmt(m.comprobante)}</td>
        </tr>`;
    });
    return html + '</tbody></table>';
}

// ══════════════════════════════════════════
//  4. DESCARGA DE EXCEL
// ══════════════════════════════════════════

function descargarExcel(accion) {
    const ctx = getContextPath();
    const url = `${ctx}/reportes?accion=${accion}`;

    fetch(url)
        .then(resp => {
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            return resp.blob();
        })
        .then(blob => {
            const disp = '';
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = accion + '_' + new Date().toISOString().slice(0, 10) + '.xlsx';
            document.body.appendChild(link);
            link.click();
            link.remove();
        })
        .catch(err => {
            console.error('Error descargando Excel:', err);
            Notify.error('No se pudo descargar el archivo.');
        });
}

// ══════════════════════════════════════════
//  5. BÚSQUEDA EN TABLA (filtro local)
// ══════════════════════════════════════════

function filtrarTabla(texto) {
    const rows = document.querySelectorAll('#modulosContent .data-table tbody tr');
    const term = texto.toLowerCase().trim();
    let visible = 0;
    rows.forEach(row => {
        const match = !term || row.textContent.toLowerCase().includes(term);
        row.style.display = match ? '' : 'none';
        if (match) visible++;
    });
    const countEl = document.getElementById('recordCount');
    if (countEl) {
        countEl.textContent = visible + ' de ' + rows.length + ' registros';
    }
}

// ══════════════════════════════════════════
//  6. INICIALIZACIÓN
// ══════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

    // Cargar KPIs
    cargarResumen();

    // Cargar primer módulo
    cargarModulo('voluntarios');

    // Tabs
    document.getElementById('modulosTabs')?.addEventListener('click', (e) => {
        const tab = e.target.closest('.mod-tab');
        if (!tab) return;
        document.querySelectorAll('.mod-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const modulo = tab.dataset.modulo;
        document.getElementById('searchTable').value = '';
        cargarModulo(modulo);
    });

    // Export general
    document.getElementById('btnExportGeneral')?.addEventListener('click', () => {
        descargarExcel('exportar_general');
    });

    // Export módulo
    document.getElementById('btnExportModulo')?.addEventListener('click', () => {
        const accion = EXPORT_MAP[moduloActual];
        if (accion) descargarExcel(accion);
    });

    // Búsqueda en tabla
    let searchTimeout;
    document.getElementById('searchTable')?.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => filtrarTabla(e.target.value), 250);
    });
});
