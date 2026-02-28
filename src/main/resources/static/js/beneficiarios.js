/* ═══════════════════════════════════════════════════
   beneficiarios.js — Gestión de Beneficiarios (AJAX)
   ═══════════════════════════════════════════════════ */

/* ── Variables globales ── */
let todosBeneficiarios = [];
let paginaActualBen = 1;
const POR_PAGINA_BEN = 6;
let modoEdicion = false;

/* ── Inicialización ── */
document.addEventListener('DOMContentLoaded', () => {
    cargarBeneficiarios();
});

/* =====================================================================
   CARGAR BENEFICIARIOS
   ===================================================================== */
function cargarBeneficiarios() {
    fetch('beneficiarios?action=listar')
        .then(r => r.json())
        .then(data => {
            todosBeneficiarios = data;
            paginaActualBen = 1;
            renderTabla();
        })
        .catch(err => {
            console.error('Error al cargar beneficiarios:', err);
            document.getElementById('beneficiarios-tbody').innerHTML =
                '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#d32f2f;">Error al cargar beneficiarios</td></tr>';
        });
}

/* =====================================================================
   RENDERIZAR TABLA
   ===================================================================== */
function renderTabla() {
    const tbody = document.getElementById('beneficiarios-tbody');
    if (!tbody) return;

    const total = todosBeneficiarios.length;
    const totalPaginas = Math.ceil(total / POR_PAGINA_BEN) || 1;

    if (paginaActualBen > totalPaginas) paginaActualBen = totalPaginas;
    if (paginaActualBen < 1) paginaActualBen = 1;

    const inicio = (paginaActualBen - 1) * POR_PAGINA_BEN;
    const fin = Math.min(inicio + POR_PAGINA_BEN, total);
    const pagina = todosBeneficiarios.slice(inicio, fin);

    /* Sin resultados */
    if (total === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#999;">No hay beneficiarios registrados</td></tr>';
        document.getElementById('paginacionBeneficiarios').style.display = 'none';
        return;
    }

    /* Generar filas */
    tbody.innerHTML = pagina.map(b => {
        const esActivo = (b.estado || '').toUpperCase() === 'ACTIVO';
        const estadoCls = esActivo ? 'activo' : 'inactivo';
        const estadoTxt = esActivo ? 'ACTIVO' : 'INACTIVO';
        const tipoCls = (b.tipoBeneficiario || 'individual').toLowerCase();

        const obs = b.observaciones
            ? (b.observaciones.length > 40 ? esc(b.observaciones.substring(0, 40)) + '...' : esc(b.observaciones))
            : '';

        return `<tr class="beneficiario-row" data-id="${b.idBeneficiario}">
            <td>
                <div class="beneficiario-nombre">
                    <strong>${esc(b.nombres)} ${esc(b.apellidos)}</strong>
                    <small>${obs}</small>
                </div>
            </td>
            <td><span class="badge-dni">${esc(b.dni || 'N/A')}</span></td>
            <td>${esc(b.telefono || '—')}</td>
            <td>${esc(b.distrito || '—')}</td>
            <td><span class="badge-tipo ${tipoCls}">${esc(b.tipoBeneficiario || '—')}</span></td>
            <td><span class="badge-necesidad">${esc(b.necesidadPrincipal || '—')}</span></td>
            <td><span class="estado-badge ${estadoCls}">${estadoTxt}</span></td>
            <td class="acciones-cell">
                <button class="btn-icon ver" onclick="verDetalle(${b.idBeneficiario})" title="Ver detalle">👁</button>
                <button class="btn-icon edit" onclick="abrirModalEditar(${b.idBeneficiario})" title="Editar">✎</button>
                ${esActivo
                ? `<button class="btn-icon disable" onclick="cambiarEstado(${b.idBeneficiario},'INACTIVO')" title="Desactivar">⊘</button>`
                : `<button class="btn-icon enable"  onclick="cambiarEstado(${b.idBeneficiario},'ACTIVO')"   title="Activar">✓</button>`
            }
            </td>
        </tr>`;
    }).join('');

    /* ── Paginación ── */
    const pag = document.getElementById('paginacionBeneficiarios');
    pag.style.display = '';

    const infoEl = document.getElementById('paginacionInfo');
    if (infoEl) {
        infoEl.innerHTML = `Mostrando <strong>${inicio + 1}-${fin}</strong> de <strong>${total}</strong> beneficiarios`;
    }

    const btnPrev = document.getElementById('btnPrevBen');
    const btnNext = document.getElementById('btnNextBen');
    if (btnPrev) btnPrev.disabled = paginaActualBen <= 1;
    if (btnNext) btnNext.disabled = paginaActualBen >= totalPaginas;

    const pagesEl = document.getElementById('paginacionPages');
    if (pagesEl) {
        pagesEl.innerHTML = '';
        const rango = [];
        for (let i = 1; i <= totalPaginas; i++) {
            if (i === 1 || i === totalPaginas || (i >= paginaActualBen - 1 && i <= paginaActualBen + 1)) {
                rango.push(i);
            }
        }
        let ultimo = 0;
        rango.forEach(i => {
            if (ultimo && i - ultimo > 1) {
                const sep = document.createElement('span');
                sep.textContent = '···';
                sep.style.cssText = 'display:flex;align-items:center;padding:0 4px;color:#9ca3af;font-size:0.8rem;';
                pagesEl.appendChild(sep);
            }
            const btn = document.createElement('button');
            btn.textContent = i;
            btn.className = 'pag-btn' + (i === paginaActualBen ? ' active' : '');
            btn.onclick = () => { paginaActualBen = i; renderTabla(); };
            pagesEl.appendChild(btn);
            ultimo = i;
        });
    }
}

function cambiarPaginaBen(dir) {
    paginaActualBen += dir;
    renderTabla();
}

/* =====================================================================
   UTILIDADES
   ===================================================================== */
function esc(txt) {
    const d = document.createElement('div');
    d.textContent = txt || '';
    return d.innerHTML;
}

/* =====================================================================
   MODAL — ABRIR / CERRAR
   ===================================================================== */
function abrirModalCrear() {
    modoEdicion = false;
    document.getElementById('modalTitulo').textContent = 'Nuevo Beneficiario';
    document.getElementById('btnGuardarTexto').textContent = 'Registrar Beneficiario';
    document.getElementById('formBeneficiario').reset();
    document.getElementById('beneficiarioId').value = '';
    document.getElementById('modalBeneficiario').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function abrirModalEditar(id) {
    modoEdicion = true;
    document.getElementById('modalTitulo').textContent = 'Editar Beneficiario';
    document.getElementById('btnGuardarTexto').textContent = 'Guardar Cambios';

    fetch(`beneficiarios?action=obtener&id=${id}`)
        .then(r => r.json())
        .then(b => {
            if (b.error) { mostrarNotificacion(b.error, 'error'); return; }

            document.getElementById('beneficiarioId').value = b.idBeneficiario;
            document.getElementById('dni').value = b.dni || '';
            document.getElementById('nombres').value = b.nombres || '';
            document.getElementById('apellidos').value = b.apellidos || '';
            document.getElementById('fechaNacimiento').value = b.fechaNacimiento || '';
            document.getElementById('telefono').value = b.telefono || '';
            document.getElementById('direccion').value = b.direccion || '';
            document.getElementById('distrito').value = b.distrito || '';
            document.getElementById('tipoBeneficiario').value = b.tipoBeneficiario || 'INDIVIDUAL';
            document.getElementById('necesidadPrincipal').value = b.necesidadPrincipal || 'OTRO';
            document.getElementById('observaciones').value = b.observaciones || '';

            document.getElementById('modalBeneficiario').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar el beneficiario', 'error');
        });
}

function cerrarModal() {
    document.getElementById('modalBeneficiario').style.display = 'none';
    document.body.style.overflow = 'auto';
}

function cerrarModalDetalle() {
    document.getElementById('modalDetalle').style.display = 'none';
    document.body.style.overflow = 'auto';
}

/* =====================================================================
   VER DETALLE
   ===================================================================== */
function verDetalle(id) {
    fetch(`beneficiarios?action=obtener&id=${id}`)
        .then(r => r.json())
        .then(b => {
            document.getElementById('detalleSubtitulo').textContent =
                (b.nombres || '') + ' ' + (b.apellidos || '');

            const body = document.getElementById('detalleBody');
            body.innerHTML = `
                <div class="detalle-grid">
                    <div class="detalle-item">
                        <span class="detalle-label">Nombres</span>
                        <span class="detalle-valor">${esc(b.nombres || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Apellidos</span>
                        <span class="detalle-valor">${esc(b.apellidos || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">DNI</span>
                        <span class="detalle-valor">${esc(b.dni || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Fecha de Nacimiento</span>
                        <span class="detalle-valor">${esc(b.fechaNacimiento || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Teléfono</span>
                        <span class="detalle-valor">${esc(b.telefono || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Distrito</span>
                        <span class="detalle-valor">${esc(b.distrito || '—')}</span>
                    </div>
                    <div class="detalle-item full-width">
                        <span class="detalle-label">Dirección</span>
                        <span class="detalle-valor">${esc(b.direccion || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Tipo</span>
                        <span class="detalle-valor">${esc(b.tipoBeneficiario || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Necesidad Principal</span>
                        <span class="detalle-valor">${esc(b.necesidadPrincipal || '—')}</span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Estado</span>
                        <span class="detalle-valor">
                            <span class="estado-badge ${(b.estado || '').toUpperCase() === 'ACTIVO' ? 'activo' : 'inactivo'}">${esc(b.estado || 'INACTIVO')}</span>
                        </span>
                    </div>
                    <div class="detalle-item">
                        <span class="detalle-label">Registrado</span>
                        <span class="detalle-valor">${b.creadoEn ? (Array.isArray(b.creadoEn) ? b.creadoEn.slice(0, 3).join('-') : esc(b.creadoEn)) : '—'}</span>
                    </div>
                    <div class="detalle-item full-width">
                        <span class="detalle-label">Observaciones</span>
                        <span class="detalle-valor">${esc(b.observaciones || 'Sin observaciones')}</span>
                    </div>
                </div>
            `;

            document.getElementById('modalDetalle').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar el detalle', 'error');
        });
}

/* =====================================================================
   GUARDAR BENEFICIARIO (CREAR / EDITAR)
   ===================================================================== */
function guardarBeneficiario(event) {
    event.preventDefault();

    const id = document.getElementById('beneficiarioId').value;
    const nombres = document.getElementById('nombres').value.trim();
    const apellidos = document.getElementById('apellidos').value.trim();
    const dni = document.getElementById('dni').value.trim();
    const fechaNacimiento = document.getElementById('fechaNacimiento').value;
    const telefono = document.getElementById('telefono').value.trim();
    const direccion = document.getElementById('direccion').value.trim();
    const distrito = document.getElementById('distrito').value.trim();
    const tipoBeneficiario = document.getElementById('tipoBeneficiario').value;
    const necesidadPrincipal = document.getElementById('necesidadPrincipal').value;
    const observaciones = document.getElementById('observaciones').value.trim();

    if (!nombres || !apellidos || !dni) {
        mostrarNotificacion('Completa los campos obligatorios', 'error');
        return;
    }

    const params = new URLSearchParams();
    params.append('action', id ? 'editar' : 'crear');
    if (id) params.append('id', id);
    params.append('nombres', nombres);
    params.append('apellidos', apellidos);
    params.append('dni', dni);
    params.append('fechaNacimiento', fechaNacimiento);
    params.append('telefono', telefono);
    params.append('direccion', direccion);
    params.append('distrito', distrito);
    params.append('tipoBeneficiario', tipoBeneficiario);
    params.append('necesidadPrincipal', necesidadPrincipal);
    params.append('observaciones', observaciones);

    fetch('beneficiarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.json())
        .then(result => {
            if (result.success) {
                cerrarModal();
                cargarBeneficiarios();
                mostrarNotificacion(result.message, 'success');
            } else {
                mostrarNotificacion(result.message || 'Error desconocido', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al guardar el beneficiario', 'error');
        });
}

/* =====================================================================
   CAMBIAR ESTADO
   ===================================================================== */
function cambiarEstado(id, nuevoEstado) {
    const msg = nuevoEstado === 'ACTIVO' ? '¿Activar este beneficiario?' : '¿Desactivar este beneficiario?';
    Notify.confirm(msg, '', { variant: 'warning', okText: 'Sí, continuar' }).then(ok => {
        if (!ok) return;

        const params = new URLSearchParams();
        params.append('action', 'cambiarEstado');
        params.append('id', id);
        params.append('estado', nuevoEstado);

        fetch('beneficiarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
            .then(r => r.json())
            .then(result => {
                if (result.success) {
                    cargarBeneficiarios();
                    mostrarNotificacion(result.message, 'success');
                } else {
                    mostrarNotificacion(result.message || 'Error', 'error');
                }
            })
            .catch(err => {
                console.error(err);
                mostrarNotificacion('Error al cambiar el estado', 'error');
            });
    });
}

/* =====================================================================
   NOTIFICACIONES TOAST
   ===================================================================== */
function mostrarNotificacion(mensaje, tipo) {
    if (typeof Notify !== 'undefined') {
        Notify[tipo === 'warning' ? 'warning' : tipo === 'error' ? 'error' : 'success'](mensaje);
        return;
    }
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = mensaje;
    toast.className = 'toast ' + tipo + ' show';
    setTimeout(() => { toast.classList.remove('show'); }, 3500);
}

/* =====================================================================
   EVENTOS GLOBALES
   ===================================================================== */
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
        cerrarModal();
        cerrarModalDetalle();
    }
});

document.addEventListener('click', e => {
    if (e.target === document.getElementById('modalBeneficiario')) cerrarModal();
    if (e.target === document.getElementById('modalDetalle')) cerrarModalDetalle();
});
