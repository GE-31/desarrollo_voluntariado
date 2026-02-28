// ══════════════════════════════════════════════════
//  ACTIVIDADES — JS
// ══════════════════════════════════════════════════

let modoEdicion = false;
let paginaActualAct = 1;
const registrosPorPaginaAct = 6;
const buscarActividadesInput = document.getElementById('buscarActividades');

// ── ABRIR MODAL CREAR ──────────────────────────
function abrirModalCrear() {
    modoEdicion = false;
    document.getElementById('modalTitulo').textContent = 'Nueva Actividad';
    document.getElementById('btnGuardarTexto').textContent = 'Crear Actividad';
    document.getElementById('formActividad').reset();
    document.getElementById('actividadId').value = '';
    document.getElementById('modalActividad').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// ── ABRIR MODAL EDITAR ─────────────────────────
function abrirModalEditar(id) {
    modoEdicion = true;
    document.getElementById('modalTitulo').textContent = 'Editar Actividad';
    document.getElementById('btnGuardarTexto').textContent = 'Guardar Cambios';

    fetch(`actividades?action=obtener&id=${id}`)
        .then(r => r.text())
        .then(text => {
            if (!text || text.trim() === '') throw new Error('Respuesta vacía');
            return JSON.parse(text);
        })
        .then(a => {
            document.getElementById('actividadId').value = a.idActividad;
            document.getElementById('nombre').value = a.nombre;
            document.getElementById('descripcion').value = a.descripcion || '';
            document.getElementById('fechaInicio').value = a.fechaInicio || '';
            document.getElementById('fechaFin').value = a.fechaFin || '';
            document.getElementById('ubicacion').value = a.ubicacion;
            document.getElementById('cupoMaximo').value = a.cupoMaximo;

            document.getElementById('modalActividad').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar la actividad', 'error');
        });
}

// ── CERRAR MODAL ───────────────────────────────
function cerrarModal() {
    document.body.style.overflow = 'auto';
    document.getElementById('modalActividad').style.display = 'none';
}

// ── GUARDAR (CREAR / EDITAR) ───────────────────
function guardarActividad(event) {
    event.preventDefault();

    const id = document.getElementById('actividadId').value;
    const nombre = document.getElementById('nombre').value;
    const descripcion = document.getElementById('descripcion').value;
    const fechaInicio = document.getElementById('fechaInicio').value;
    const fechaFin = document.getElementById('fechaFin').value;
    const ubicacion = document.getElementById('ubicacion').value;
    const cupoMaximo = document.getElementById('cupoMaximo').value;

    const params = new URLSearchParams();
    params.append('action', id ? 'editar' : 'crear');
    if (id) params.append('id', id);
    params.append('nombre', nombre);
    params.append('descripcion', descripcion);
    params.append('fechaInicio', fechaInicio);
    params.append('fechaFin', fechaFin);
    params.append('ubicacion', ubicacion);
    params.append('cupoMaximo', cupoMaximo);

    fetch('actividades', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.text())
        .then(text => {
            if (!text || !text.trim()) throw new Error('Respuesta vacía');
            return JSON.parse(text);
        })
        .then(result => {
            if (result.success) {
                cerrarModal();
                cargarActividades();
                mostrarNotificacion(result.message, 'success');
            } else {
                mostrarNotificacion(result.message, 'error');
            }
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al guardar la actividad', 'error');
        });
}

// ── CAMBIAR ESTADO ─────────────────────────────
function cambiarEstado(id, nuevoEstado) {
    const config = {
        FINALIZADO: {
            emoji: '✅',
            titulo: 'Finalizar actividad',
            texto: '¿Estás seguro de marcar esta actividad como finalizada?',
            clase: 'finalizar',
            boton: 'Sí, finalizar'
        },
        CANCELADO: {
            emoji: '⛔',
            titulo: 'Cancelar actividad',
            texto: '¿Deseas cancelar esta actividad?',
            clase: 'cancelar',
            boton: 'Sí, cancelar'
        },
        ACTIVO: {
            emoji: '🔄',
            titulo: 'Reactivar actividad',
            texto: '¿Deseas reactivar esta actividad?',
            clase: 'reactivar',
            boton: 'Sí, reactivar'
        }
    };
    const cfg = config[nuevoEstado];
    if (!cfg) return;

    mostrarConfirm(cfg, () => {
        const params = new URLSearchParams();
        params.append('action', 'cambiarEstado');
        params.append('id', id);
        params.append('estado', nuevoEstado);

        fetch('actividades', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
            .then(r => r.text())
            .then(text => {
                if (!text.trim()) throw new Error('Respuesta vacía');
                return JSON.parse(text);
            })
            .then(result => {
                if (result.success) {
                    cargarActividades();
                    const tc = {
                        FINALIZADO: { titulo: '¡Actividad finalizada!', msg: 'La actividad fue marcada como finalizada correctamente.' },
                        CANCELADO: { titulo: 'Actividad cancelada', msg: 'La actividad fue cancelada.' },
                        ACTIVO: { titulo: '¡Actividad reactivada!', msg: 'La actividad está activa nuevamente.' }
                    }[nuevoEstado] || { titulo: '¡Listo!', msg: result.message };
                    mostrarNotificacion(tc.titulo, tc.msg, 'success');
                } else {
                    mostrarNotificacion('Error', result.message, 'error');
                }
            })
            .catch(err => {
                console.error(err);
                mostrarNotificacion('Error', 'No se pudo cambiar el estado', 'error');
            });
    });
}

// ── VER DETALLE DE ACTIVIDAD ───────────────
function verDetalles(id) {
    window.location.href = 'actividad-detalle?id=' + id;
}

// ── CARGAR ACTIVIDADES (AJAX) ──────────────────
function cargarActividades() {
    fetch('actividades?action=listar')
        .then(r => r.json())
        .then(actividades => {
            const tbody = document.getElementById('actividades-tbody');
            tbody.innerHTML = '';

            if (actividades && actividades.length > 0) {
                actividades.forEach(a => {
                    const estadoClass = a.estado === 'ACTIVO' ? 'activo'
                        : a.estado === 'FINALIZADO' ? 'finalizado' : 'cancelado';

                    let botonesAcciones = '';
                    if (a.estado === 'ACTIVO') {
                        botonesAcciones = `
                            <button class="btn-icon info"      onclick="verDetalles(${a.idActividad})" title="Ver detalle">👁</button>
                            <button class="btn-icon edit"     onclick="abrirModalEditar(${a.idActividad})" title="Editar">✎</button>
                            <button class="btn-icon finalizar" onclick="cambiarEstado(${a.idActividad},'FINALIZADO')" title="Finalizar">✓</button>
                            <button class="btn-icon disable"  onclick="cambiarEstado(${a.idActividad},'CANCELADO')" title="Cancelar">⊘</button>`;
                    } else if (a.estado === 'CANCELADO') {
                        botonesAcciones = `
                            <button class="btn-icon edit"   onclick="abrirModalEditar(${a.idActividad})" title="Editar">✎</button>
                            <button class="btn-icon enable" onclick="cambiarEstado(${a.idActividad},'ACTIVO')" title="Reactivar">↻</button>`;
                    } else if (a.estado === 'FINALIZADO') {
                        botonesAcciones = `
                            <button class="btn-icon info"   onclick="verDetalles(${a.idActividad})" title="Ver detalles">👁</button>`;
                    }

                    const pct = a.cupoMaximo > 0 ? Math.round(a.inscritos * 100 / a.cupoMaximo) : 0;

                    // Determinar clase del chip según porcentaje de ocupación
                    let chipClass = 'chip-ok';
                    let chipTagText = 'Disponible';
                    if (a.cupoMaximo > 0 && a.inscritos >= a.cupoMaximo) {
                        chipClass = 'chip-lleno';
                        chipTagText = 'Lleno';
                    } else if (a.cupoMaximo > 0 && a.inscritos >= (a.cupoMaximo * 0.7)) {
                        chipClass = 'chip-casi';
                        chipTagText = 'Casi lleno';
                    }

                    const fila = document.createElement('tr');
                    fila.className = 'actividad-row';
                    fila.dataset.id = a.idActividad;
                    fila.innerHTML = `
                        <td>
                            <div class="actividad-nombre">
                                <strong>${a.nombre}</strong>
                                <small>${a.descripcion || ''}</small>
                            </div>
                        </td>
                        <td><span class="badge-fecha">${a.fechaInicio}</span></td>
                        <td><span class="badge-fecha">${a.fechaFin || '—'}</span></td>
                        <td>${a.ubicacion}</td>
                        <td>
                            <div class="cupo-chip ${chipClass}">
                                <div class="chip-progress" style="--pct:${pct}"></div>
                                <div class="chip-content">
                                    <span class="chip-icon">&#128101;</span>
                                    <span class="chip-numbers">
                                        <strong>${a.inscritos}</strong>
                                        <span class="chip-sep">/</span>
                                        <span>${a.cupoMaximo}</span>
                                    </span>
                                    <span class="chip-tag">${chipTagText}</span>
                                </div>
                            </div>
                        </td>
                        <td><span class="estado-badge ${estadoClass}">${a.estado}</span></td>
                        <td class="acciones-cell">
                            ${botonesAcciones}
                        </td>`;
                    tbody.appendChild(fila);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:2rem;color:#999;">No hay actividades registradas</td></tr>';
            }

            paginaActualAct = 1;
            aplicarFiltroActividades();
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar actividades', 'error');
        });
}

function aplicarFiltroActividades() {
    const q = (buscarActividadesInput?.value || '').trim().toLowerCase();
    const filas = Array.from(document.querySelectorAll('#actividades-tbody tr.actividad-row'));
    filas.forEach((fila) => {
        const texto = (fila.textContent || '').toLowerCase();
        const match = !q || texto.includes(q);
        fila.classList.toggle('filtro-oculto', !match);
    });
    paginaActualAct = 1;
    aplicarPaginacionAct();
}

// ══════════════════════════════════════
//  PAGINACIÓN
// ══════════════════════════════════════
function aplicarPaginacionAct() {
    const tbody = document.getElementById('actividades-tbody');
    if (!tbody) return;

    const filas = Array.from(tbody.querySelectorAll('tr.actividad-row'));
    const filasFiltradas = filas.filter(f => !f.classList.contains('filtro-oculto'));
    const total = filasFiltradas.length;
    const totalPaginas = Math.ceil(total / registrosPorPaginaAct) || 1;

    const sinDatos = document.getElementById('sinActividadesRow');
    if (sinDatos) sinDatos.style.display = 'none';

    let filaNoResultados = document.getElementById('sinResultadosActividadesRow');
    if (!filaNoResultados) {
        filaNoResultados = document.createElement('tr');
        filaNoResultados.id = 'sinResultadosActividadesRow';
        filaNoResultados.innerHTML = '<td colspan="7" style="text-align:center; padding:2rem; color:#999;">No hay actividades que coincidan con la busqueda</td>';
        tbody.appendChild(filaNoResultados);
    }
    filaNoResultados.style.display = total === 0 ? '' : 'none';

    const paginacionEl = document.getElementById('paginacionActividades');
    if (paginacionEl) paginacionEl.style.display = total > 0 ? '' : 'none';
    if (total === 0) {
        filas.forEach(f => { f.style.display = 'none'; });
        return;
    }

    if (paginaActualAct > totalPaginas) paginaActualAct = totalPaginas;
    if (paginaActualAct < 1) paginaActualAct = 1;

    const inicio = (paginaActualAct - 1) * registrosPorPaginaAct;
    const fin = inicio + registrosPorPaginaAct;

    filas.forEach((f) => { f.style.display = 'none'; });
    filasFiltradas.forEach((f, i) => { f.style.display = (i >= inicio && i < fin) ? '' : 'none'; });

    // Info
    const infoEl = document.getElementById('paginacionInfo');
    if (infoEl) {
        infoEl.innerHTML = `Mostrando <strong>${inicio + 1}-${Math.min(fin, total)}</strong> de <strong>${total}</strong> actividades`;
    }

    // Botones
    const btnPrev = document.getElementById('btnPrevAct');
    const btnNext = document.getElementById('btnNextAct');
    if (btnPrev) btnPrev.disabled = paginaActualAct <= 1;
    if (btnNext) btnNext.disabled = paginaActualAct >= totalPaginas;

    // Números
    const pages = document.getElementById('paginacionPages');
    if (pages) {
        pages.innerHTML = '';
        for (let i = 1; i <= totalPaginas; i++) {
            const btn = document.createElement('button');
            btn.textContent = i;
            btn.style.cssText = 'width:36px;height:36px;border:1px solid #e5e7eb;border-radius:8px;font-size:0.85rem;font-weight:500;cursor:pointer;display:flex;align-items:center;justify-content:center;';
            if (i === paginaActualAct) {
                btn.style.background = '#10b981';
                btn.style.color = '#fff';
                btn.style.borderColor = '#10b981';
            } else {
                btn.style.background = '#fff';
                btn.style.color = '#4b5563';
            }
            btn.onclick = () => { paginaActualAct = i; aplicarPaginacionAct(); };
            pages.appendChild(btn);
        }
    }
}

function cambiarPaginaAct(delta) {
    paginaActualAct += delta;
    aplicarPaginacionAct();
}

// ── Inicializar paginación al cargar ───────────
document.addEventListener('DOMContentLoaded', () => {
    if (buscarActividadesInput) {
        buscarActividadesInput.addEventListener('input', aplicarFiltroActividades);
    }
    cargarActividades();
});

// ══════════════════════════════════════
//  NOTIFICACIÓN TOAST
// ══════════════════════════════════════
let _toastTimer = null;

function mostrarNotificacion(titulo, mensaje, tipo) {
    // Soporte legado: mostrarNotificacion(msg, tipo)
    if (mensaje === 'success' || mensaje === 'error' || mensaje === 'warning' || mensaje === 'info') {
        tipo = mensaje;
        mensaje = titulo;
        titulo = tipo === 'success' ? '¡Éxito!' : tipo === 'error' ? 'Error' : 'Aviso';
    }
    if (typeof Notify !== 'undefined') {
        const fn = tipo === 'error' ? 'error' : tipo === 'warning' ? 'warning' : tipo === 'info' ? 'info' : 'success';
        Notify[fn](mensaje, { title: titulo });
        return;
    }
    const iconos = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.innerHTML = `
        <div class="toast-body">
            <div class="toast-icon">${iconos[tipo] || 'ℹ'}</div>
            <div class="toast-content">
                <div class="toast-title">${titulo}</div>
                <div class="toast-message">${mensaje}</div>
            </div>
            <button class="toast-close" onclick="this.closest('#toast').classList.remove('show')">&#x2715;</button>
        </div>
        <div class="toast-progress"></div>
    `;
    toast.className = 'toast ' + (tipo || 'info');
    void toast.offsetWidth; // reflow para reiniciar animación
    toast.classList.add('show');

    if (_toastTimer) clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => { toast.classList.remove('show'); }, 4000);
}

// ══════════════════════════════════════
//  MODAL CONFIRMACIÓN PERSONALIZADO
// ══════════════════════════════════════
function mostrarConfirm({ emoji, titulo, texto, clase, boton }, onAceptar) {
    if (typeof Notify !== 'undefined') {
        const variant = clase && clase.includes('danger') ? 'danger' : 'warning';
        Notify.confirm(titulo || '¿Estás seguro?', texto || '', { variant })
            .then(ok => { if (ok) onAceptar(); });
        return;
    }
    let overlay = document.getElementById('confirmOverlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'confirmOverlay';
        overlay.className = 'confirm-overlay';
        document.body.appendChild(overlay);
    }
    overlay.innerHTML = `
        <div class="confirm-card">
            <span class="confirm-emoji">${emoji}</span>
            <div class="confirm-title">${titulo}</div>
            <div class="confirm-text">${texto}</div>
            <div class="confirm-btns">
                <button class="confirm-btn-cancel" id="confirmCancelar">Cancelar</button>
                <button class="confirm-btn-ok ${clase}" id="confirmAceptar">${boton}</button>
            </div>
        </div>
    `;
    requestAnimationFrame(() => overlay.classList.add('show'));
    const cerrar = () => {
        overlay.classList.remove('show');
        setTimeout(() => { if (overlay.parentNode) overlay.remove(); }, 300);
    };
    document.getElementById('confirmCancelar').onclick = cerrar;
    overlay.onclick = (e) => { if (e.target === overlay) cerrar(); };
    document.getElementById('confirmAceptar').onclick = () => { cerrar(); onAceptar(); };
}

// ── Cerrar modal con Escape ────────────────────
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') cerrarModal();
});

// ── Cerrar modal clickeando fuera ──────────────
document.addEventListener('click', (e) => {
    const modal = document.getElementById('modalActividad');
    if (e.target === modal) cerrarModal();
});
