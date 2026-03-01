/* ===================================================================
   usuarios.js — Gestión de Usuarios (AJAX completo)
   =================================================================== */

/* ── Variables globales ── */
let todosUsuarios = [];
let usuariosFiltrados = [];
let paginaActual = 1;
const POR_PAGINA = 5;
let modoEdicion = false;

/* ── Inicialización ── */
document.addEventListener('DOMContentLoaded', () => {
    cargarUsuarios();
    initFiltros();
    initPermisos();
});

/* =====================================================================
   CARGAR USUARIOS
   ===================================================================== */
function cargarUsuarios() {
    fetch('usuarios?action=listar')
        .then(r => r.json())
        .then(data => {
            todosUsuarios = data;
            aplicarFiltros();
        })
        .catch(err => {
            console.error('Error al cargar usuarios:', err);
            document.getElementById('usuarios-tbody').innerHTML =
                '<tr><td colspan="6" class="text-center" style="padding:2rem; color:#d32f2f;">Error al cargar usuarios</td></tr>';
        });
}

/* =====================================================================
   FILTROS
   ===================================================================== */
function initFiltros() {
    document.getElementById('filtroCorreo')?.addEventListener('input', aplicarFiltros);
    document.getElementById('filtroEstado')?.addEventListener('change', aplicarFiltros);
    document.getElementById('filtroFecha')?.addEventListener('change', aplicarFiltros);
}

function aplicarFiltros() {
    const filtro = (document.getElementById('filtroCorreo')?.value || '').toLowerCase().trim();
    const estado = (document.getElementById('filtroEstado')?.value || '').toUpperCase().trim();
    const fecha  = (document.getElementById('filtroFecha')?.value || '').trim();

    usuariosFiltrados = todosUsuarios.filter(u => {
        const coincideFiltro = !filtro || 
            (u.correo || '').toLowerCase().includes(filtro) ||
            (u.username || '').toLowerCase().includes(filtro) ||
            (u.nombreRol || '').toLowerCase().includes(filtro) ||
            (u.estado || '').toLowerCase().includes(filtro);
        const coincideEstado = !estado || (u.estado || '').toUpperCase() === estado;
        const coincideFecha  = !fecha  || (u.creadoEn || '').slice(0, 10) === fecha;
        return coincideFiltro && coincideEstado && coincideFecha;
    });

    paginaActual = 1;
    renderTabla();
}

function limpiarFiltros() {
    const fc = document.getElementById('filtroCorreo');
    const fe = document.getElementById('filtroEstado');
    const ff = document.getElementById('filtroFecha');
    if (fc) fc.value = '';
    if (fe) fe.value = '';
    if (ff) ff.value = '';
    aplicarFiltros();
}

function hayFiltrosActivos() {
    const c = document.getElementById('filtroCorreo')?.value || '';
    const e = document.getElementById('filtroEstado')?.value || '';
    const f = document.getElementById('filtroFecha')?.value || '';
    return c.trim() !== '' || e.trim() !== '' || f.trim() !== '';
}

/* =====================================================================
   RENDERIZAR TABLA
   ===================================================================== */
function renderTabla() {
    const tbody = document.getElementById('usuarios-tbody');
    if (!tbody) return;

    const total = usuariosFiltrados.length;
    const totalPaginas = Math.ceil(total / POR_PAGINA) || 1;

    if (paginaActual > totalPaginas) paginaActual = totalPaginas;
    if (paginaActual < 1) paginaActual = 1;

    const inicio = (paginaActual - 1) * POR_PAGINA;
    const fin    = Math.min(inicio + POR_PAGINA, total);
    const pagina = usuariosFiltrados.slice(inicio, fin);

    /* Sin resultados */
    if (total === 0) {
        const msg = hayFiltrosActivos()
            ? 'No hay usuarios que coincidan con los filtros'
            : 'No hay usuarios registrados';
        tbody.innerHTML = `<tr><td colspan="6" class="text-center" style="padding:2rem; color:#999;">${msg}</td></tr>`;
        document.getElementById('paginacionUsuarios').style.display = 'none';
        return;
    }

    /* Generar filas */
    tbody.innerHTML = pagina.map(u => {
        const slug     = generarSlug(u.nombreRol);
        const esActivo = (u.estado || '').toUpperCase() === 'ACTIVO';
        const estadoCls = esActivo ? 'activo' : 'inactivo';
        const estadoTxt = esActivo ? 'Activo' : 'Inactivo';
        const fecha     = (u.creadoEn || '').slice(0, 10);

        return `<tr class="usuario-row">
            <td><span class="badge-username">${escHtml(u.username)}</span></td>
            <td>${escHtml(u.correo || '-')}</td>
            <td><span class="badge-rol badge-rol-${slug}">${escHtml(u.nombreRol || 'Sin rol')}</span></td>
            <td><span class="estado-badge ${estadoCls}">${estadoTxt}</span></td>
            <td class="fecha">${escHtml(fecha)}</td>
            <td class="acciones-cell">
                <button class="btn-icon edit" onclick="abrirModalEditar(${u.idUsuario})" title="Editar permisos">&#9998;</button>
                ${esActivo
                    ? `<button class="btn-icon disable" onclick="cambiarEstado(${u.idUsuario},'INACTIVO')" title="Desactivar">&#8856;</button>`
                    : `<button class="btn-icon enable"  onclick="cambiarEstado(${u.idUsuario},'ACTIVO')"   title="Activar">&#8635;</button>`
                }
            </td>
        </tr>`;
    }).join('');

    /* ── Paginación ── */
    const pag = document.getElementById('paginacionUsuarios');
    pag.style.display = '';

    document.getElementById('paginacionInfo').innerHTML =
        `Mostrando <span class="highlight">${inicio + 1}-${fin}</span> de <span class="highlight">${total}</span> usuarios`;

    const btnPrev = document.getElementById('btnPrevUsr');
    const btnNext = document.getElementById('btnNextUsr');
    btnPrev.disabled = paginaActual <= 1;
    btnNext.disabled = paginaActual >= totalPaginas;
    btnPrev.style.opacity = btnPrev.disabled ? '0.35' : '1';
    btnNext.style.opacity = btnNext.disabled ? '0.35' : '1';

    const pagesEl = document.getElementById('paginacionPages');
    pagesEl.innerHTML = '';
    for (let i = 1; i <= totalPaginas; i++) {
        const btn = document.createElement('button');
        btn.className = 'pg-num' + (i === paginaActual ? ' active' : '');
        btn.textContent = i;
        btn.onclick = () => { paginaActual = i; renderTabla(); };
        pagesEl.appendChild(btn);
    }
}

function cambiarPagina(dir) {
    paginaActual += dir;
    renderTabla();
}

/* =====================================================================
   UTILIDADES
   ===================================================================== */
function generarSlug(texto) {
    if (!texto || texto === 'Sin rol') return 'sin';
    return texto.toLowerCase()
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-|-$/g, '');
}

function escHtml(txt) {
    const div = document.createElement('div');
    div.textContent = txt || '';
    return div.innerHTML;
}

/* =====================================================================
   MODAL — ABRIR / CERRAR
   ===================================================================== */
function abrirModalCrear() {
    modoEdicion = false;

    document.getElementById('modalTitulo').textContent      = 'Crear Usuario';
    document.getElementById('modalSubtitulo').textContent   = 'Ingresa la información del nuevo usuario';
    document.getElementById('formUsuario').reset();
    document.getElementById('usuarioId').value              = '';
    document.getElementById('camposCreacion').style.display = '';

    document.getElementById('password').required        = true;
    document.getElementById('confirmPassword').required = true;

    /* Desmarcar permisos */
    document.querySelectorAll('.perm, .select-all').forEach(ch => ch.checked = false);
    actualizarContadores();

    /* Cargar voluntarios */
    const selVol = document.getElementById('voluntarioId');
    selVol.innerHTML = '<option value="">Cargando...</option>';
    fetch('usuarios?action=voluntarios')
        .then(r => r.json())
        .then(lista => {
            selVol.innerHTML = '<option value="">Seleccione un voluntario</option>';
            if (lista.length === 0) {
                selVol.innerHTML += '<option value="" disabled>No hay voluntarios disponibles</option>';
            } else {
                lista.forEach(v => {
                    const opt = document.createElement('option');
                    opt.value = v.idVoluntario;
                    opt.textContent = `${v.nombres} ${v.apellidos} - ${v.cargo || ''} (DNI: ${v.dni})`;
                    selVol.appendChild(opt);
                });
            }
        })
        .catch(() => {
            selVol.innerHTML = '<option value="">Error al cargar voluntarios</option>';
        });

    document.getElementById('modalUsuario').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function abrirModalEditar(id) {
    modoEdicion = true;
    document.getElementById('modalTitulo').textContent        = 'Editar Permisos';
    document.getElementById('camposCreacion').style.display    = 'none';
    // Deshabilitar campos de creación en modo edición para evitar errores de focus/validación
    document.getElementById('username').disabled = true;
    document.getElementById('password').disabled = true;
    document.getElementById('confirmPassword').disabled = true;
    document.getElementById('voluntarioId').disabled = true;
    document.getElementById('formUsuario').reset();
    document.getElementById('password').required              = false;
    document.getElementById('confirmPassword').required       = false;

    fetch(`usuarios?action=obtener&id=${id}`)
        .then(r => r.json())
        .then(data => {
            if (data.error) {
                mostrarNotificacion(data.error, 'error');
                return;
            }
            // Llenar el campo oculto después del reset
            setTimeout(() => {
                document.getElementById('usuarioId').value = data.idUsuario;
            }, 0);
            document.getElementById('modalSubtitulo').textContent =
                `Permisos de ${data.username} — ${data.nombreRol || 'Sin rol'}`;

            /* Marcar permisos actuales */
            document.querySelectorAll('.perm').forEach(ch => {
                ch.checked = data.permisos && data.permisos.includes(parseInt(ch.value));
            });
            actualizarContadores();

            document.getElementById('modalUsuario').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al cargar usuario', 'error');
        });
}

function cerrarModal() {
    document.getElementById('modalUsuario').style.display = 'none';
    document.body.style.overflow = 'auto';
    // Habilitar campos de creación al cerrar modal
    document.getElementById('username').disabled = false;
    document.getElementById('password').disabled = false;
    document.getElementById('confirmPassword').disabled = false;
    document.getElementById('voluntarioId').disabled = false;
}

/* =====================================================================
   GUARDAR USUARIO
   ===================================================================== */
function guardarUsuario(event) {
    event.preventDefault();

    const id = document.getElementById('usuarioId').value;
    if (modoEdicion && !id) {
        mostrarNotificacion('No se pudo identificar el usuario a editar. Intente de nuevo.', 'error');
        return;
    }
    const params = new URLSearchParams();

    if (!id) {
        /* ── CREAR ── */
        const voluntarioId  = document.getElementById('voluntarioId').value;
        const username      = document.getElementById('username').value.trim();
        const password      = document.getElementById('password').value;
        const confirmPass   = document.getElementById('confirmPassword').value;

        if (!voluntarioId)                 { mostrarNotificacion('Seleccione un voluntario', 'warning'); return; }
        if (!username)                     { mostrarNotificacion('Ingrese nombre de usuario', 'warning'); return; }
        if (!password || password.length < 6) { mostrarNotificacion('Contraseña mínima de 6 caracteres', 'warning'); return; }
        if (password !== confirmPass)      { mostrarNotificacion('Las contraseñas no coinciden', 'warning'); return; }

        params.append('action', 'crear');
        params.append('voluntarioId', voluntarioId);
        params.append('username', username);
        params.append('password', password);
    } else {
        /* ── EDITAR (solo permisos) ── */
        params.append('action', 'editar');
        params.append('idUsuario', id);
    }

    /* Añadir permisos seleccionados */
    document.querySelectorAll('.perm:checked').forEach(ch => {
        params.append('permisos[]', ch.value);
    });

    fetch('usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.json())
        .then(result => {
            if (result.success) {
                cerrarModal();
                cargarUsuarios();
                mostrarNotificacion(result.message, 'success');
            } else {
                mostrarNotificacion(result.message || 'Error desconocido', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            mostrarNotificacion('Error al guardar', 'error');
        });
}

/* =====================================================================
   CAMBIAR ESTADO
   ===================================================================== */
function cambiarEstado(id, nuevoEstado) {
    const texto = nuevoEstado === 'ACTIVO' ? 'activar' : 'desactivar';
    Notify.confirm(`¿Desea ${texto} este usuario?`, '', { variant: 'warning', okText: 'Sí, continuar' }).then(ok => {
        if (!ok) return;

    const params = new URLSearchParams();
    params.append('action', 'cambiar_estado');
    params.append('id', id);
    params.append('estado', nuevoEstado);

    fetch('usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(r => r.json())
        .then(result => {
            if (result.success) {
                cargarUsuarios();
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
   PERMISOS — Select-all y contadores
   ===================================================================== */
function initPermisos() {
    document.querySelectorAll('.perm').forEach(ch => {
        ch.addEventListener('change', actualizarContadores);
    });

    document.querySelectorAll('.select-all').forEach(sa => {
        sa.addEventListener('change', () => {
            const grupo = sa.dataset.group;
            document.querySelectorAll(`.perm[data-group="${grupo}"]`).forEach(c => c.checked = sa.checked);
            actualizarContadores();
        });
    });
}

function actualizarContadores() {
    const grupos = ['personas', 'actividades', 'agenda', 'donaciones', 'financiera', 'informes'];
    let total = 0;

    grupos.forEach(g => {
        const items   = document.querySelectorAll(`.perm[data-group="${g}"]`);
        const marcados = document.querySelectorAll(`.perm[data-group="${g}"]:checked`);

        const countEl = document.getElementById(`count-${g}`);
        if (countEl) countEl.textContent = `${marcados.length}/${items.length}`;

        const sa = document.querySelector(`.select-all[data-group="${g}"]`);
        if (sa) sa.checked = marcados.length === items.length && items.length > 0;

        total += marcados.length;
    });

    const contEl = document.getElementById('contadorPermisos');
    if (contEl) contEl.textContent = `${total} permisos seleccionados`;
}

/* =====================================================================
   NOTIFICACIONES
   ===================================================================== */
function mostrarNotificacion(mensaje, tipo) {
    if (typeof Notify !== 'undefined') {
        Notify[tipo === 'warning' ? 'warning' : tipo === 'error' ? 'error' : 'success'](mensaje);
        return;
    }
    let notif = document.getElementById('notificacion');
    if (!notif) {
        notif = document.createElement('div');
        notif.id = 'notificacion';
        document.body.appendChild(notif);
    }
    notif.textContent = mensaje;
    notif.className = `notificacion ${tipo} show`;
    setTimeout(() => notif.classList.remove('show'), 3000);
}

/* =====================================================================
   EVENTOS GLOBALES
   ===================================================================== */
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') cerrarModal();
});

document.addEventListener('click', e => {
    if (e.target === document.getElementById('modalUsuario')) cerrarModal();
});
