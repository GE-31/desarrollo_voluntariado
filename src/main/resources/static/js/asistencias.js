// ============================================
// VARIABLES GLOBALES
// ============================================
let actividadSeleccionadaId = null;
let actividadSeleccionadaData = {};
let participantesList = [];
let asistenciasList = [];
let dataMerged = [];
let paginaActual = 1;
const registrosPorPagina = 10;

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', function () {
    filtrarActividades();
});

// ============================================
// VISTA 1: ACTIVIDADES
// ============================================
function filtrarActividades() {
    const filtro = document.getElementById('filtroEstadoActividad').value;
    const cards = document.querySelectorAll('.actividad-card');
    let visibles = 0;

    cards.forEach(card => {
        const estado = card.dataset.estado || '';
        const mostrar = !filtro || estado === filtro;
        card.style.display = mostrar ? '' : 'none';
        if (mostrar) visibles++;
    });

    document.getElementById('sinActividadesMsg').style.display = visibles === 0 ? '' : 'none';
}

function seleccionarActividad(idActividad, cardEl) {
    actividadSeleccionadaId = idActividad;

    // Guardar datos de la actividad desde data attributes
    actividadSeleccionadaData = {
        id: idActividad,
        nombre: cardEl.dataset.nombre || '',
        ubicacion: cardEl.dataset.ubicacion || '',
        fechaInicio: cardEl.dataset.fechaInicio || '',
        fechaFin: cardEl.dataset.fechaFin || '',
        cupo: cardEl.dataset.cupo || '0',
        inscritos: cardEl.dataset.inscritos || '0',
        estado: cardEl.dataset.estado || ''
    };

    // Configurar la vista de participantes
    document.getElementById('tituloActividad').textContent = actividadSeleccionadaData.nombre;

    let subtitulo = '';
    if (actividadSeleccionadaData.ubicacion) subtitulo += '📍 ' + actividadSeleccionadaData.ubicacion;
    if (actividadSeleccionadaData.fechaInicio) {
        if (subtitulo) subtitulo += '  |  ';
        subtitulo += '📅 ' + formatearFecha(actividadSeleccionadaData.fechaInicio);
        if (actividadSeleccionadaData.fechaFin) {
            subtitulo += ' — ' + formatearFecha(actividadSeleccionadaData.fechaFin);
        }
    }
    document.getElementById('subtituloActividad').textContent = subtitulo || 'Gestiona la asistencia de los participantes';

    // Fecha por defecto: hoy
    const hoy = new Date().toISOString().split('T')[0];
    document.getElementById('fechaAsistencia').value = hoy;

    // Restringir rango de fechas al de la actividad
    const fechaInput = document.getElementById('fechaAsistencia');
    if (actividadSeleccionadaData.fechaInicio) fechaInput.setAttribute('min', actividadSeleccionadaData.fechaInicio);
    if (actividadSeleccionadaData.fechaFin) fechaInput.setAttribute('max', actividadSeleccionadaData.fechaFin);

    // Cambiar vistas
    document.getElementById('vistaActividades').style.display = 'none';
    document.getElementById('vistaParticipantes').style.display = '';

    // Cargar participantes con asistencia
    cargarParticipantesConAsistencia();
}

function volverActividades() {
    actividadSeleccionadaId = null;
    actividadSeleccionadaData = {};
    participantesList = [];
    asistenciasList = [];
    dataMerged = [];

    document.getElementById('vistaParticipantes').style.display = 'none';
    document.getElementById('vistaActividades').style.display = '';
}

// ============================================
// VISTA 2: PARTICIPANTES CON ASISTENCIA
// ============================================
function cargarParticipantesConAsistencia() {
    if (!actividadSeleccionadaId) return;

    const tbody = document.getElementById('participantes-tbody');
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#999;">Cargando...</td></tr>';

    // Hacer dos solicitudes en paralelo: participantes y asistencias de la actividad
    Promise.all([
        fetch(`asistencias?action=participantes&id_actividad=${actividadSeleccionadaId}`).then(r => r.json()),
        fetch(`asistencias?action=porActividad&id_actividad=${actividadSeleccionadaId}`).then(r => r.json())
    ])
    .then(([participantes, asistencias]) => {
        participantesList = participantes || [];
        asistenciasList = asistencias || [];
        renderizarParticipantes();
    })
    .catch(error => {
        console.error('Error al cargar datos:', error);
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#dc2626;">Error al cargar los participantes</td></tr>';
    });
}

function renderizarParticipantes() {
    const fecha = document.getElementById('fechaAsistencia').value;
    const tbody = document.getElementById('participantes-tbody');
    tbody.innerHTML = '';

    if (participantesList.length === 0) {
        tbody.innerHTML = '<tr id="sinParticipantesRow"><td colspan="8" style="text-align:center; padding:2rem; color:#999;">No hay participantes registrados en esta actividad</td></tr>';
        actualizarStats([]);
        actualizarPaginacion();
        return;
    }

    // Merge: para cada participante, buscar si tiene asistencia en la fecha seleccionada
    dataMerged = participantesList.map(p => {
        const asistencia = asistenciasList.find(a =>
            a.idVoluntario === p.idVoluntario && a.fecha === fecha
        );

        return {
            idVoluntario: p.idVoluntario,
            nombreVoluntario: p.nombreVoluntario || '',
            dniVoluntario: p.dniVoluntario || '',
            carrera: p.carreraVoluntario || '',
            // Datos de asistencia (si existe)
            tieneAsistencia: !!asistencia,
            idAsistencia: asistencia ? asistencia.idAsistencia : null,
            estado: asistencia ? asistencia.estado : null,
            horaEntrada: asistencia ? asistencia.horaEntrada : null,
            horaSalida: asistencia ? asistencia.horaSalida : null,
            horasTotales: asistencia ? asistencia.horasTotales : null,
            observaciones: asistencia ? asistencia.observaciones : null
        };
    });

    actualizarStats(dataMerged);

    // Paginación
    const total = dataMerged.length;
    const totalPaginas = Math.ceil(total / registrosPorPagina) || 1;
    if (paginaActual > totalPaginas) paginaActual = totalPaginas;

    const inicio = (paginaActual - 1) * registrosPorPagina;
    const fin = Math.min(inicio + registrosPorPagina, total);
    const pagina = dataMerged.slice(inicio, fin);

    pagina.forEach(d => {
        const fila = document.createElement('tr');
        fila.className = 'participante-row';

        let estadoHTML;
        if (d.tieneAsistencia) {
            estadoHTML = `<span class="estado-badge ${d.estado.toLowerCase()}">${d.estado}</span>`;
        } else {
            estadoHTML = `<span class="estado-badge pendiente">PENDIENTE</span>`;
        }

        let accionesHTML;
        if (d.tieneAsistencia) {
            accionesHTML = `
                <button class="btn-icon edit" onclick="abrirModalEditar(${d.idAsistencia})" title="Editar">&#9998;</button>
                <button class="btn-icon delete" onclick="eliminarAsistencia(${d.idAsistencia})" title="Eliminar">&#128465;</button>
            `;
        } else {
            accionesHTML = `
                <button class="btn-registrar-asis" onclick="abrirModalRegistrar(${d.idVoluntario}, '${d.nombreVoluntario}')">
                    Registrar
                </button>
            `;
        }

        fila.innerHTML = `
            <td><strong>${d.nombreVoluntario}</strong></td>
            <td><span class="badge-dni">${d.dniVoluntario || '-'}</span></td>
            <td>${d.carrera || '-'}</td>
            <td>${estadoHTML}</td>
            <td>${d.horaEntrada || '-'}</td>
            <td>${d.horaSalida || '-'}</td>
            <td>${d.horasTotales != null ? d.horasTotales : '-'}</td>
            <td class="acciones-cell">${accionesHTML}</td>
        `;
        tbody.appendChild(fila);
    });

    actualizarPaginacion();
}

function actualizarStats(merged) {
    const total = merged.length;
    const asistio = merged.filter(d => d.estado === 'ASISTIO').length;
    const tardanza = merged.filter(d => d.estado === 'TARDANZA').length;
    const falta = merged.filter(d => d.estado === 'FALTA').length;

    document.getElementById('statTotal').textContent = total;
    document.getElementById('statAsistio').textContent = asistio;
    document.getElementById('statTardanza').textContent = tardanza;
    document.getElementById('statFalta').textContent = falta;
}

// ============================================
// PAGINACIÓN
// ============================================
function actualizarPaginacion() {
    const total = dataMerged.length;
    const totalPaginas = Math.ceil(total / registrosPorPagina) || 1;
    const inicio = (paginaActual - 1) * registrosPorPagina;
    const fin = Math.min(inicio + registrosPorPagina, total);

    const paginacionEl = document.getElementById('paginacionParticipantes');
    if (total <= registrosPorPagina) {
        if (paginacionEl) paginacionEl.style.display = 'none';
        return;
    }
    if (paginacionEl) paginacionEl.style.display = 'flex';

    const info = document.getElementById('paginacionInfo');
    if (info) {
        info.textContent = total > 0
            ? `Mostrando ${inicio + 1}-${fin} de ${total}`
            : 'Mostrando 0-0 de 0';
    }

    document.getElementById('btnPrevPart').disabled = paginaActual <= 1;
    document.getElementById('btnNextPart').disabled = paginaActual >= totalPaginas;

    const pagesContainer = document.getElementById('paginacionPages');
    if (pagesContainer) {
        pagesContainer.innerHTML = '';
        for (let i = 1; i <= totalPaginas; i++) {
            const btn = document.createElement('button');
            btn.textContent = i;
            btn.style.cssText = 'padding:0.4rem 0.7rem; border:1px solid #e5e7eb; background:' +
                (i === paginaActual ? '#667eea' : '#fff') + '; color:' +
                (i === paginaActual ? '#fff' : '#4b5563') +
                '; border-radius:6px; font-size:0.82rem; cursor:pointer;';
            btn.onclick = () => { paginaActual = i; renderizarParticipantes(); };
            pagesContainer.appendChild(btn);
        }
    }
}

function cambiarPaginaPart(dir) {
    paginaActual += dir;
    renderizarParticipantes();
}

// ============================================
// MODAL: REGISTRAR ASISTENCIA
// ============================================
function abrirModalRegistrar(idVoluntario, nombreVoluntario) {
    document.getElementById('modalTitulo').textContent = 'Registrar Asistencia';
    document.getElementById('modalSubtitulo').textContent = nombreVoluntario + ' — ' + actividadSeleccionadaData.nombre;
    document.getElementById('formAsistencia').reset();
    document.getElementById('asistenciaId').value = '';
    document.getElementById('modoEdicion').value = 'false';
    document.getElementById('modalIdVoluntario').value = idVoluntario;
    document.getElementById('modalIdActividad').value = actividadSeleccionadaId;
    document.getElementById('modalFecha').value = document.getElementById('fechaAsistencia').value;

    toggleHoras();

    document.getElementById('modalAsistencia').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// ============================================
// MODAL: EDITAR ASISTENCIA
// ============================================
function abrirModalEditar(idAsistencia) {
    fetch(`asistencias?action=obtener&id=${idAsistencia}`)
        .then(response => response.text())
        .then(text => {
            if (!text || text.trim() === '') throw new Error('Respuesta vacía');
            return JSON.parse(text);
        })
        .then(asistencia => {
            document.getElementById('modalTitulo').textContent = 'Editar Asistencia';
            document.getElementById('modalSubtitulo').textContent =
                (asistencia.nombreVoluntario || '') + ' — ' + (actividadSeleccionadaData.nombre || '');
            document.getElementById('asistenciaId').value = asistencia.idAsistencia;
            document.getElementById('modoEdicion').value = 'true';
            document.getElementById('modalIdVoluntario').value = asistencia.idVoluntario;
            document.getElementById('modalIdActividad').value = asistencia.idActividad;
            document.getElementById('modalFecha').value = asistencia.fecha;

            document.getElementById('estado').value = asistencia.estado;
            document.getElementById('hora_entrada').value = asistencia.horaEntrada || '';
            document.getElementById('hora_salida').value = asistencia.horaSalida || '';
            document.getElementById('observaciones').value = asistencia.observaciones || '';

            toggleHoras();

            document.getElementById('modalAsistencia').style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al cargar los datos de la asistencia.', 'error');
        });
}

function cerrarModal() {
    document.body.style.overflow = 'auto';
    document.getElementById('modalAsistencia').style.display = 'none';
}

// Mostrar/ocultar horas según estado
function toggleHoras() {
    const estado = document.getElementById('estado').value;
    const grupoEntrada = document.getElementById('grupoHoraEntrada');
    const grupoSalida = document.getElementById('grupoHoraSalida');

    if (estado === 'FALTA') {
        grupoEntrada.style.opacity = '0.5';
        grupoSalida.style.opacity = '0.5';
        document.getElementById('hora_entrada').value = '';
        document.getElementById('hora_salida').value = '';
    } else {
        grupoEntrada.style.opacity = '1';
        grupoSalida.style.opacity = '1';
    }
}

// ============================================
// GUARDAR ASISTENCIA
// ============================================
function guardarAsistencia(event) {
    event.preventDefault();

    const esEdicion = document.getElementById('modoEdicion').value === 'true';
    const params = new URLSearchParams();

    if (esEdicion) {
        params.append('action', 'actualizar');
        params.append('id', document.getElementById('asistenciaId').value);
        params.append('hora_entrada', document.getElementById('hora_entrada').value);
        params.append('hora_salida', document.getElementById('hora_salida').value);
        params.append('estado', document.getElementById('estado').value);
        params.append('observaciones', document.getElementById('observaciones').value);
    } else {
        params.append('action', 'registrar');
        params.append('id_voluntario', document.getElementById('modalIdVoluntario').value);
        params.append('id_actividad', document.getElementById('modalIdActividad').value);
        params.append('fecha', document.getElementById('modalFecha').value);
        params.append('hora_entrada', document.getElementById('hora_entrada').value);
        params.append('hora_salida', document.getElementById('hora_salida').value);
        params.append('estado', document.getElementById('estado').value);
        params.append('observaciones', document.getElementById('observaciones').value);
    }

    fetch('asistencias', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(response => response.text())
    .then(text => {
        if (!text || text.trim() === '') throw new Error('Respuesta vacía');
        const data = JSON.parse(text);

        if (data.success) {
            cerrarModal();
            mostrarNotificacion(data.message || 'Asistencia guardada correctamente', 'success');
            // Recargar participantes sin recargar la página
            setTimeout(() => cargarParticipantesConAsistencia(), 800);
        } else {
            mostrarNotificacion(data.message || 'Error al guardar la asistencia', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarNotificacion('Error al guardar la asistencia: ' + error.message, 'error');
    });
}

// ============================================
// ELIMINAR ASISTENCIA
// ============================================
function eliminarAsistencia(idAsistencia) {
    mostrarConfirm('¿Eliminar esta asistencia?', 'Esta acción no se puede deshacer.', () => {
        const params = new URLSearchParams();
        params.append('action', 'eliminar');
        params.append('id', idAsistencia);

        fetch('asistencias', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                mostrarNotificacion('Asistencia eliminada correctamente', 'success');
                setTimeout(() => cargarParticipantesConAsistencia(), 800);
            } else {
                mostrarNotificacion(data.message || 'Error al eliminar', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al eliminar la asistencia', 'error');
        });
    });
}

// ============================================
// UTILIDADES
// ============================================
function formatearFecha(fechaStr) {
    if (!fechaStr || fechaStr === 'null') return '—';
    const partes = fechaStr.split('-');
    if (partes.length !== 3) return fechaStr;
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

// ============================================
// NOTIFICACIONES Y CONFIRMACIÓN
// ============================================
function mostrarNotificacion(mensaje, tipo) {
    if (typeof Notify !== 'undefined') {
        const fn = tipo === 'error' ? 'error' : tipo === 'warning' ? 'warning' : tipo === 'info' ? 'info' : 'success';
        Notify[fn](mensaje);
        return;
    }
    alert((tipo === 'error' ? 'Error: ' : '') + mensaje);
}

function mostrarConfirm(titulo, mensaje, onConfirm) {
    if (typeof Notify !== 'undefined') {
        Notify.confirm(titulo, mensaje, { variant: 'danger' })
            .then(ok => { if (ok) onConfirm(); });
        return;
    }
    if (confirm(titulo + '\n' + mensaje)) onConfirm();
}
