/* ===================================================================
   voluntarios.js — Gestión de Voluntarios (AJAX completo)
   =================================================================== */

/* ── Variables globales ── */
let todosVoluntarios = [];
let paginaActualVol = 1;
const POR_PAGINA_VOL = 5;
let modoEdicion = false;

/* ── Inicialización ── */
document.addEventListener('DOMContentLoaded', () => {
    cargarVoluntarios();
    document.getElementById('cargo')?.addEventListener('change', actualizarInfoCargo);
});

/* =====================================================================
   CARGAR VOLUNTARIOS
   ===================================================================== */
function cargarVoluntarios() {
    fetch('voluntarios?action=listar')
        .then(r => r.json())
        .then(data => {
            todosVoluntarios = data;
            paginaActualVol = 1;
            renderTabla();
        })
        .catch(err => {
            console.error('Error al cargar voluntarios:', err);
            document.getElementById('voluntarios-tbody').innerHTML =
                '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#d32f2f;">Error al cargar voluntarios</td></tr>';
        });
}

/* =====================================================================
   RENDERIZAR TABLA
   ===================================================================== */
function renderTabla() {
    const tbody = document.getElementById('voluntarios-tbody');
    if (!tbody) return;

    const total = todosVoluntarios.length;
    const totalPaginas = Math.ceil(total / POR_PAGINA_VOL) || 1;

    if (paginaActualVol > totalPaginas) paginaActualVol = totalPaginas;
    if (paginaActualVol < 1) paginaActualVol = 1;

    const inicio = (paginaActualVol - 1) * POR_PAGINA_VOL;
    const fin    = Math.min(inicio + POR_PAGINA_VOL, total);
    const pagina = todosVoluntarios.slice(inicio, fin);

    /* Sin resultados */
    if (total === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#999;">No hay voluntarios registrados</td></tr>';
        document.getElementById('paginacionVoluntarios').style.display = 'none';
        return;
    }

    /* Generar filas */
    tbody.innerHTML = pagina.map(v => {
        const esActivo  = (v.estado || '').toUpperCase() === 'ACTIVO';
        const estadoCls = esActivo ? 'activo' : 'inactivo';
        const estadoTxt = esActivo ? 'ACTIVO' : 'INACTIVO';

        return `<tr class="voluntario-row" data-id="${v.idVoluntario}">
            <td><strong>${esc(v.nombres)} ${esc(v.apellidos)}</strong></td>
            <td><span class="badge-dni">${esc(v.dni || 'N/A')}</span></td>
            <td>${esc(v.correo || '-')}</td>
            <td>${esc(v.telefono || '-')}</td>
            <td>${esc(v.carrera || '-')}</td>
            <td>${esc(v.cargo || '-')}</td>
            <td><span class="estado-badge ${estadoCls}">${estadoTxt}</span></td>
            <td class="acciones-cell">
                <button class="btn-icon edit" onclick="abrirModalEditar(${v.idVoluntario})" title="Editar">✎</button>
                ${esActivo
                    ? `<button class="btn-icon disable" onclick="cambiarEstado(${v.idVoluntario},'INACTIVO')" title="Deshabilitar">⊘</button>`
                    : `<button class="btn-icon enable"  onclick="cambiarEstado(${v.idVoluntario},'ACTIVO')"   title="Habilitar">✓</button>`
                }
            </td>
        </tr>`;
    }).join('');

    /* ── Paginación ── */
    const pag = document.getElementById('paginacionVoluntarios');
    pag.style.display = '';

    const infoEl = document.getElementById('paginacionInfo');
    if (infoEl) {
        infoEl.innerHTML = `Mostrando <strong>${inicio + 1}-${fin}</strong> de <strong>${total}</strong> voluntarios`;
    }

    const btnPrev = document.getElementById('btnPrevVol');
    const btnNext = document.getElementById('btnNextVol');
    if (btnPrev) { btnPrev.disabled = paginaActualVol <= 1;           btnPrev.style.opacity = btnPrev.disabled ? '0.35' : '1'; }
    if (btnNext) { btnNext.disabled = paginaActualVol >= totalPaginas; btnNext.style.opacity = btnNext.disabled ? '0.35' : '1'; }

    const pagesEl = document.getElementById('paginacionPages');
    if (pagesEl) {
        pagesEl.innerHTML = '';
        for (let i = 1; i <= totalPaginas; i++) {
            const btn = document.createElement('button');
            btn.textContent = i;
            btn.style.cssText = 'width:36px; height:36px; border:1px solid #e5e7eb; border-radius:8px; font-size:0.85rem; font-weight:500; cursor:pointer; display:flex; align-items:center; justify-content:center;';
            if (i === paginaActualVol) {
                btn.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
                btn.style.color = '#fff';
                btn.style.borderColor = 'transparent';
                btn.style.boxShadow = '0 3px 10px rgba(102,126,234,0.4)';
            } else {
                btn.style.background = '#fff';
                btn.style.color = '#4b5563';
            }
            btn.onclick = () => { paginaActualVol = i; renderTabla(); };
            pagesEl.appendChild(btn);
        }
    }
}

function cambiarPaginaVol(dir) {
    paginaActualVol += dir;
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
/* Cargos con acceso al sistema */
const CARGOS_CON_ACCESO = ['Coordinador', 'Responsable', 'Líder', 'Administrador'];

function actualizarInfoCargo() {
    const cargo = document.getElementById('cargo').value;
    const info  = document.getElementById('cargoInfo');
    if (info) {
        info.style.display = CARGOS_CON_ACCESO.includes(cargo) ? 'block' : 'none';
    }
}

function abrirModalCrear() {
    modoEdicion = false;
    document.getElementById('modalTitulo').textContent = 'Crear Voluntario';
    document.getElementById('formVoluntario').reset();
    document.getElementById('voluntarioId').value = '';
    actualizarInfoCargo();
    document.getElementById('modalVoluntario').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function abrirModalEditar(id) {
    modoEdicion = true;
    document.getElementById('modalTitulo').textContent = 'Editar Voluntario';

    fetch(`voluntarios?action=obtener&id=${id}`)
        .then(r => r.json())
        .then(v => {
            if (v.error) {
                mostrarNotificacion(v.error, 'error');
                return;
            }
            document.getElementById('voluntarioId').value = v.idVoluntario;
            document.getElementById('dni').value       = v.dni || '';
            document.getElementById('nombres').value   = v.nombres || '';
            document.getElementById('apellidos').value = v.apellidos || '';
            document.getElementById('correo').value    = v.correo || '';
            document.getElementById('telefono').value  = v.telefono || '';
            document.getElementById('carrera').value   = v.carrera || '';
            document.getElementById('cargo').value     = v.cargo || '';
            actualizarInfoCargo();

            document.getElementById('modalVoluntario').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar voluntario', 'error');
        });
}

function cerrarModal() {
    document.getElementById('modalVoluntario').style.display = 'none';
    document.body.style.overflow = 'auto';
}

/* =====================================================================
   GUARDAR VOLUNTARIO
   ===================================================================== */
function validarFormularioVoluntario() {
    const dni = document.getElementById('dni').value.trim();
    const telefono = document.getElementById('telefono').value.trim();
    const nombres = document.getElementById('nombres').value.trim();
    const apellidos = document.getElementById('apellidos').value.trim();
    const carrera = document.getElementById('carrera').value.trim();

    const regexSoloLetras = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;
    const regexDNI = /^\d{8}$/;
    const regexTelefono = /^\d{9}$/;

    if (!regexDNI.test(dni)) {
        mostrarNotificacion('El DNI debe tener exactamente 8 dígitos numéricos.', 'error');
        return false;
    }

    if (!regexTelefono.test(telefono)) {
        mostrarNotificacion('El teléfono debe tener exactamente 9 dígitos numéricos.', 'error');
        return false;
    }

    if (!regexSoloLetras.test(nombres)) {
        mostrarNotificacion('El campo de nombres solo debe contener letras.', 'error');
        return false;
    }

    if (!regexSoloLetras.test(apellidos)) {
        mostrarNotificacion('El campo de apellidos solo debe contener letras.', 'error');
        return false;
    }

    if (!regexSoloLetras.test(carrera)) {
        mostrarNotificacion('El campo de carrera solo debe contener letras.', 'error');
        return false;
    }

    return true;
}

function guardarVoluntario(event) {
    event.preventDefault();

    if (!validarFormularioVoluntario()) {
        return;
    }

    const id       = document.getElementById('voluntarioId').value;
    const nombres  = document.getElementById('nombres').value.trim();
    const apellidos = document.getElementById('apellidos').value.trim();
    const dni      = document.getElementById('dni').value.trim();
    const correo   = document.getElementById('correo').value.trim();
    const telefono = document.getElementById('telefono').value.trim();
    const carrera  = document.getElementById('carrera').value.trim();
    const cargo    = document.getElementById('cargo').value;

    if (!nombres || !apellidos || !dni) {
        mostrarNotificacion('Completa los campos obligatorios', 'warning');
        return;
    }

    const params = new URLSearchParams();
    params.append('action', id ? 'editar' : 'crear');
    if (id) params.append('idVoluntario', id);
    params.append('nombres', nombres);
    params.append('apellidos', apellidos);
    params.append('dni', dni);
    params.append('correo', correo);
    params.append('telefono', telefono);
    params.append('carrera', carrera);
    params.append('cargo', cargo);

    fetch('voluntarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.json())
        .then(result => {
            if (result.success) {
                cerrarModal();
                cargarVoluntarios();
                mostrarNotificacion(result.message, 'success');
            } else {
                mostrarNotificacion(result.message || 'Error desconocido', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al guardar voluntario', 'error');
        });
}

/* =====================================================================
   CAMBIAR ESTADO
   ===================================================================== */
function cambiarEstado(id, nuevoEstado) {
    const msg = nuevoEstado === 'ACTIVO' ? '¿Habilitar este voluntario?' : '¿Deshabilitar este voluntario?';
    Notify.confirm(msg, '', { variant: 'warning', okText: 'Sí, continuar' }).then(ok => {
        if (!ok) return;

    const params = new URLSearchParams();
    params.append('action', 'cambiarEstado');
    params.append('id', id);
    params.append('estado', nuevoEstado);

    fetch('voluntarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.json())
        .then(result => {
            if (result.success) {
                cargarVoluntarios();
                mostrarNotificacion(result.message, 'success');
            } else {
                mostrarNotificacion(result.message || 'Error', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cambiar estado', 'error');
        });
    });
}

/* =====================================================================
   NOTIFICACIONES
   ===================================================================== */
function mostrarNotificacion(mensaje, tipo) {
    if (typeof Notify !== 'undefined') {
        Notify[tipo === 'warning' ? 'warning' : tipo === 'error' ? 'error' : 'success'](mensaje);
        return;
    }
    const notif = document.createElement('div');
    notif.className = `notificacion ${tipo}`;
    notif.textContent = mensaje;
    document.body.appendChild(notif);

    setTimeout(() => notif.classList.add('show'), 10);
    setTimeout(() => {
        notif.classList.remove('show');
        setTimeout(() => document.body.removeChild(notif), 300);
    }, 3000);
}

/* =====================================================================
   EVENTOS GLOBALES
   ===================================================================== */
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') cerrarModal();
});

window.onclick = function(e) {
    if (e.target === document.getElementById('modalVoluntario')) cerrarModal();
};

function buscarVoluntarios() {
    const nombres = document.getElementById('filtroNombres')?.value || '';
    const apellidos = document.getElementById('filtroApellidos')?.value || '';
    const dni = document.getElementById('filtroDni')?.value || '';
    const correo = document.getElementById('filtroCorreo')?.value || '';
    const telefono = document.getElementById('filtroTelefono')?.value || '';
    const carrera = document.getElementById('filtroCarrera')?.value || '';
    const cargo = document.getElementById('filtroCargo')?.value || '';

    const params = new URLSearchParams({
        nombres, apellidos, dni, correo, telefono, carrera, cargo
    });

    fetch(`voluntarios?action=buscar&${params.toString()}`)
        .then(r => r.json())
        .then(data => {
            todosVoluntarios = data;
            paginaActualVol = 1;
            renderTabla();
        })
        .catch(err => {
            console.error('Error al buscar voluntarios:', err);
            document.getElementById('voluntarios-tbody').innerHTML =
                '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#d32f2f;">Error al buscar voluntarios</td></tr>';
        });
}

/* =====================================================================
   FILTROS DE BÚSQUEDA
   ===================================================================== */
function filtrarVoluntarios() {
    const filtroGeneral = document.getElementById('filtroGeneral').value.toLowerCase();
    const filtroEstado = document.getElementById('filtroEstado').value;

    const voluntariosFiltrados = todosVoluntarios.filter(v => {
        const cumpleFiltroGeneral = filtroGeneral === '' || 
            (v.nombres && v.nombres.toLowerCase().includes(filtroGeneral)) ||
            (v.apellidos && v.apellidos.toLowerCase().includes(filtroGeneral)) ||
            (v.dni && v.dni.toLowerCase().includes(filtroGeneral)) ||
            (v.correo && v.correo.toLowerCase().includes(filtroGeneral)) ||
            (v.telefono && v.telefono.toLowerCase().includes(filtroGeneral)) ||
            (v.carrera && v.carrera.toLowerCase().includes(filtroGeneral)) ||
            (v.cargo && v.cargo.toLowerCase().includes(filtroGeneral));

        const cumpleFiltroEstado = filtroEstado === '' || (v.estado && v.estado.toUpperCase() === filtroEstado);

        return cumpleFiltroGeneral && cumpleFiltroEstado;
    });

    renderTablaFiltrada(voluntariosFiltrados);
}

function renderTablaFiltrada(voluntarios) {
    const tbody = document.getElementById('voluntarios-tbody');
    if (!tbody) return;

    if (voluntarios.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#999;">No se encontraron resultados</td></tr>';
        return;
    }

    tbody.innerHTML = voluntarios.map(v => {
        const esActivo  = (v.estado || '').toUpperCase() === 'ACTIVO';
        const estadoCls = esActivo ? 'activo' : 'inactivo';
        const estadoTxt = esActivo ? 'ACTIVO' : 'INACTIVO';

        return `<tr class="voluntario-row" data-id="${v.idVoluntario}">
            <td><strong>${esc(v.nombres)} ${esc(v.apellidos)}</strong></td>
            <td><span class="badge-dni">${esc(v.dni || 'N/A')}</span></td>
            <td>${esc(v.correo || '-')}</td>
            <td>${esc(v.telefono || '-')}</td>
            <td>${esc(v.carrera || '-')}</td>
            <td>${esc(v.cargo || '-')}</td>
            <td><span class="estado-badge ${estadoCls}">${estadoTxt}</span></td>
            <td class="acciones-cell">
                <button class="btn-icon edit" onclick="abrirModalEditar(${v.idVoluntario})" title="Editar">✎</button>
                ${esActivo
                    ? `<button class="btn-icon disable" onclick="cambiarEstado(${v.idVoluntario},'INACTIVO')" title="Deshabilitar">⊘</button>`
                    : `<button class="btn-icon enable"  onclick="cambiarEstado(${v.idVoluntario},'ACTIVO')"   title="Habilitar">✓</button>`
                }
            </td>
        </tr>`;
    }).join('');
}

function limpiarFiltros() {
    document.getElementById('filtroGeneral').value = '';
    document.getElementById('filtroEstado').value = '';
    filtrarVoluntarios();
}
