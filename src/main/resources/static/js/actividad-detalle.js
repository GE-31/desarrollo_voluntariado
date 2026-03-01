// ══════════════════════════════════════════════════
//  ACTIVIDAD DETALLE — JS
// ══════════════════════════════════════════════════

const ID_ACT = document.getElementById('idActividad').value;
const BASE = 'actividad-detalle';
let CUPO_MAX = parseInt(document.getElementById('cupoMaximo').value) || 0;
let INSCRITOS = parseInt(document.getElementById('inscritos').value) || 0;

// ── TABS ───────────────────────────────────────
function cambiarTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.getElementById('tab-' + tab).classList.add('active');
    document.querySelector(`.tab-btn[onclick="cambiarTab('${tab}')"]`).classList.add('active');

    if (tab === 'recursos') cargarRecursos();
    if (tab === 'participantes') cargarParticipantes();
    if (tab === 'beneficiarios') cargarBeneficiarios();
    if (tab === 'localidades') cargarLocalidades();
}

// ══════════════════════════════════════
//  RECURSOS
// ══════════════════════════════════════
function cargarRecursos() {
    fetch(`${BASE}?action=recursos&id=${ID_ACT}`)
        .then(r => r.json())
        .then(lista => {
            const tbody = document.getElementById('tbody-recursos');
            if (!lista.length) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-msg">No hay recursos asignados aún</td></tr>';
                return;
            }
            tbody.innerHTML = lista.map(ar => {
                const pct = ar.porcentaje || 0;
                const prioClass = ar.prioridad === 'ALTA' ? 'prio-alta' : ar.prioridad === 'BAJA' ? 'prio-baja' : 'prio-media';
                return `<tr>
                    <td><strong>${ar.nombreRecurso || 'Recurso #' + ar.idRecurso}</strong><br><small>${ar.unidadMedida || ''}</small></td>
                    <td>${ar.tipoRecurso || '—'}</td>
                    <td>${ar.cantidadRequerida}</td>
                    <td>
                        <input type="number" value="${ar.cantidadConseguida}" min="0" step="0.01" class="input-inline"
                               onchange="actualizarConseguida(${ar.idActividadRecurso}, this.value)">
                    </td>
                    <td>
                        <div class="progress-bar"><div class="progress-fill" style="width:${pct}%"></div></div>
                        <small>${pct}%</small>
                    </td>
                    <td><span class="badge-prio ${prioClass}">${ar.prioridad || 'MEDIA'}</span></td>
                    <td><button class="btn-icon delete" onclick="eliminarRecurso(${ar.idActividadRecurso})" title="Quitar"><i class="fas fa-trash"></i></button></td>
                </tr>`;
            }).join('');
        }).catch(() => {
            document.getElementById('tbody-recursos').innerHTML = '<tr><td colspan="7" class="empty-msg">Error al cargar recursos</td></tr>';
        });
}

function abrirModalRecurso() {
    document.getElementById('formRecurso').reset();
    document.getElementById('modalRecurso').style.display = 'flex';
}
function cerrarModalRecurso() { document.getElementById('modalRecurso').style.display = 'none'; }

function guardarRecurso(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    params.append('action', 'agregarRecurso');
    params.append('idActividad', ID_ACT);
    params.append('idRecurso', document.getElementById('selRecurso').value);
    params.append('cantidadRequerida', document.getElementById('cantidadRequerida').value);
    params.append('prioridad', document.getElementById('prioridadRecurso').value);
    params.append('observacion', document.getElementById('obsRecurso').value);

    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cerrarModalRecurso(); cargarRecursos(); mostrarToast(res.message, 'success'); }
            else mostrarToast(res.message, 'error');
        }).catch(() => mostrarToast('Error al agregar recurso', 'error'));
}

function actualizarConseguida(idAR, valor) {
    const params = new URLSearchParams();
    params.append('action', 'actualizarConseguida');
    params.append('idActividadRecurso', idAR);
    params.append('cantidadConseguida', valor);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) cargarRecursos();
            else mostrarToast(res.message, 'error');
        });
}

function eliminarRecurso(idAR) {
    if (!confirm('¿Eliminar este recurso de la actividad?')) return;
    const params = new URLSearchParams();
    params.append('action', 'eliminarRecurso');
    params.append('idActividadRecurso', idAR);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cargarRecursos(); mostrarToast('Recurso eliminado', 'success'); }
            else mostrarToast(res.message, 'error');
        });
}

// ── Crear recurso en catálogo ──
function abrirModalNuevoRecurso() { document.getElementById('modalNuevoRecurso').style.display = 'flex'; }

function crearRecurso(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    params.append('action', 'crearRecurso');
    params.append('nombre', document.getElementById('nrNombre').value);
    params.append('unidadMedida', document.getElementById('nrUnidad').value);
    params.append('tipoRecurso', document.getElementById('nrTipo').value);
    params.append('descripcion', document.getElementById('nrDesc').value);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) {
                document.getElementById('modalNuevoRecurso').style.display = 'none';
                // Agregar al select
                const opt = document.createElement('option');
                opt.value = res.idRecurso;
                opt.textContent = document.getElementById('nrNombre').value + ' (' + (document.getElementById('nrUnidad').value || 'Unidad') + ')';
                document.getElementById('selRecurso').appendChild(opt);
                document.getElementById('selRecurso').value = res.idRecurso;
                mostrarToast(res.message, 'success');
            } else mostrarToast(res.message, 'error');
        }).catch(() => mostrarToast('Error al crear recurso', 'error'));
}

// ══════════════════════════════════════
//  PARTICIPANTES
// ══════════════════════════════════════
function cargarParticipantes() {
    fetch(`${BASE}?action=participantes&id=${ID_ACT}`)
        .then(r => r.json())
        .then(lista => {
            const tbody = document.getElementById('tbody-participantes');
            INSCRITOS = lista.length;
            if (!lista.length) {
                tbody.innerHTML = '<tr><td colspan="4" class="empty-msg">No hay voluntarios asignados aún</td></tr>';
                return;
            }
            tbody.innerHTML = lista.map(p => `<tr>
                <td><strong>${p.nombreVoluntario}</strong></td>
                <td>${p.dniVoluntario || '—'}</td>
                <td>${p.carreraVoluntario || '—'}</td>
                <td><button class="btn-icon delete" onclick="eliminarParticipante(${p.idParticipacion})" title="Quitar"><i class="fas fa-trash"></i></button></td>
            </tr>`).join('');
        }).catch(() => {
            document.getElementById('tbody-participantes').innerHTML = '<tr><td colspan="4" class="empty-msg">Error al cargar participantes</td></tr>';
        });
}

let _listaVoluntarios = [];
let _voluntarioSeleccionado = null;

function abrirModalParticipante() {
    if (CUPO_MAX > 0 && INSCRITOS >= CUPO_MAX) {
        mostrarToast('No hay cupos disponibles (' + INSCRITOS + '/' + CUPO_MAX + ')', 'warning');
        return;
    }
    _voluntarioSeleccionado = null;
    document.getElementById('selVoluntario').value = '';
    const input = document.getElementById('buscarVoluntarioInput');
    input.value = '';
    document.getElementById('clearVoluntarioBtn').style.display = 'none';
    document.getElementById('voluntarioDropdown').innerHTML = '';
    document.getElementById('voluntarioDropdown').classList.remove('open');
    const btnAsignar = document.getElementById('btnAsignarVol');
    btnAsignar.disabled = true; btnAsignar.style.opacity = '0.5';

    fetch('voluntarios?action=listar')
        .then(r => r.json()).then(lista => {
            const activos = lista.filter(v => v.estado === 'ACTIVO');
            // Deduplicar por DNI (queda el de menor id = más antiguo)
            const vistos = new Set();
            _listaVoluntarios = activos.filter(v => {
                const clave = v.dni || ('id-' + v.idVoluntario);
                if (vistos.has(clave)) return false;
                vistos.add(clave);
                return true;
            });
            renderVoluntarioDropdown(_listaVoluntarios);
        });
    document.getElementById('modalParticipante').style.display = 'flex';
    setTimeout(() => input.focus(), 200);
}

function cerrarModalParticipante() {
    document.getElementById('modalParticipante').style.display = 'none';
    document.getElementById('voluntarioDropdown').classList.remove('open');
}

function renderVoluntarioDropdown(lista) {
    const dropdown = document.getElementById('voluntarioDropdown');
    if (!lista || lista.length === 0) {
        dropdown.innerHTML = '<div class="search-select-empty">No se encontraron voluntarios</div>';
        dropdown.classList.add('open');
        return;
    }
    dropdown.innerHTML = lista.map(v => `
        <div class="search-select-option" data-id="${v.idVoluntario}" data-nombre="${v.nombres} ${v.apellidos}" data-dni="${v.dni || ''}">
            <div class="search-opt-name">${v.nombres} ${v.apellidos}</div>
            <div class="search-opt-dni">DNI: ${v.dni || 'Sin DNI'}</div>
        </div>
    `).join('');
    dropdown.querySelectorAll('.search-select-option').forEach(opt => {
        opt.addEventListener('click', function () {
            seleccionarVoluntario(this.dataset.id, this.dataset.nombre, this.dataset.dni);
        });
    });
    dropdown.classList.add('open');
}

function seleccionarVoluntario(id, nombre, dni) {
    _voluntarioSeleccionado = id;
    document.getElementById('selVoluntario').value = id;
    const input = document.getElementById('buscarVoluntarioInput');
    input.value = nombre + (dni ? ' — ' + dni : '');
    document.getElementById('clearVoluntarioBtn').style.display = 'flex';
    document.getElementById('voluntarioDropdown').classList.remove('open');
    const btnAsignar = document.getElementById('btnAsignarVol');
    btnAsignar.disabled = false; btnAsignar.style.opacity = '1';
}

function limpiarSeleccionVoluntario() {
    _voluntarioSeleccionado = null;
    document.getElementById('selVoluntario').value = '';
    const input = document.getElementById('buscarVoluntarioInput');
    input.value = '';
    document.getElementById('clearVoluntarioBtn').style.display = 'none';
    const btnAsignar = document.getElementById('btnAsignarVol');
    btnAsignar.disabled = true; btnAsignar.style.opacity = '0.5';
    renderVoluntarioDropdown(_listaVoluntarios);
    input.focus();
}

document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('buscarVoluntarioInput');
    if (input) {
        input.addEventListener('input', function () {
            _voluntarioSeleccionado = null;
            document.getElementById('selVoluntario').value = '';
            const btnAsignar = document.getElementById('btnAsignarVol');
            btnAsignar.disabled = true; btnAsignar.style.opacity = '0.5';
            const q = this.value.trim().toLowerCase();
            document.getElementById('clearVoluntarioBtn').style.display = q ? 'flex' : 'none';
            const filtrados = _listaVoluntarios.filter(v => {
                const texto = (v.nombres + ' ' + v.apellidos + ' ' + (v.dni || '')).toLowerCase();
                return texto.includes(q);
            });
            renderVoluntarioDropdown(filtrados);
        });
        input.addEventListener('focus', function () {
            if (!_voluntarioSeleccionado && _listaVoluntarios.length > 0) {
                const q = this.value.trim().toLowerCase();
                const filtrados = q ? _listaVoluntarios.filter(v => {
                    const texto = (v.nombres + ' ' + v.apellidos + ' ' + (v.dni || '')).toLowerCase();
                    return texto.includes(q);
                }) : _listaVoluntarios;
                renderVoluntarioDropdown(filtrados);
            }
        });
    }
    const clearBtn = document.getElementById('clearVoluntarioBtn');
    if (clearBtn) clearBtn.addEventListener('click', limpiarSeleccionVoluntario);

    // Cerrar dropdown al hacer clic fuera
    document.addEventListener('click', function (e) {
        const container = document.getElementById('voluntarioSearchContainer');
        if (container && !container.contains(e.target)) {
            document.getElementById('voluntarioDropdown').classList.remove('open');
        }
    });
});

function guardarParticipante(e) {
    e.preventDefault();
    const idVol = document.getElementById('selVoluntario').value;
    if (!idVol) {
        mostrarToast('Seleccione un voluntario de la lista', 'warning');
        return;
    }
    const params = new URLSearchParams();
    params.append('action', 'agregarParticipante');
    params.append('idActividad', ID_ACT);
    params.append('idVoluntario', idVol);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cerrarModalParticipante(); cargarParticipantes(); mostrarToast(res.message, 'success'); }
            else mostrarToast(res.message, 'error');
        }).catch(() => mostrarToast('Error al asignar voluntario', 'error'));
}

function eliminarParticipante(id) {
    if (!confirm('¿Quitar a este voluntario de la actividad?')) return;
    const params = new URLSearchParams();
    params.append('action', 'eliminarParticipante');
    params.append('idParticipacion', id);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cargarParticipantes(); mostrarToast('Participante removido', 'success'); }
            else mostrarToast(res.message, 'error');
        });
}

// ══════════════════════════════════════
//  BENEFICIARIOS
// ══════════════════════════════════════
function cargarBeneficiarios() {
    fetch(`${BASE}?action=beneficiarios&id=${ID_ACT}`)
        .then(r => r.json())
        .then(lista => {
            const tbody = document.getElementById('tbody-beneficiarios');
            if (!lista.length) {
                tbody.innerHTML = '<tr><td colspan="10" class="empty-msg">No hay beneficiarios vinculados aún</td></tr>';
                return;
            }
            tbody.innerHTML = lista.map(ab => `<tr>
                <td>${ab.organizacion || '—'}</td>
                <td>${ab.direccion || '—'}</td>
                <td>${ab.distrito || '—'}</td>
                <td>${ab.necesidadPrincipal || '—'}</td>
                <td>${ab.observaciones || '—'}</td>
                <td>${ab.nombreResponsable || '—'}</td>
                <td>${ab.apellidosResponsable || '—'}</td>
                <td>${ab.dni || '—'}</td>
                <td>${ab.telefono || '—'}</td>
                <td><button class="btn-icon delete" onclick="eliminarBeneficiario(${ab.idActividadBeneficiario})" title="Desvincular"><i class="fas fa-trash"></i></button></td>
            </tr>`).join('');
        }).catch(() => {
            document.getElementById('tbody-beneficiarios').innerHTML = '<tr><td colspan="10" class="empty-msg">Error al cargar beneficiarios</td></tr>';
        });
}

let _listaBeneficiarios = [];
let _beneficiarioSeleccionado = null;

function abrirModalBeneficiario() {
    _beneficiarioSeleccionado = null;
    document.getElementById('selBeneficiario').value = '';
    document.getElementById('buscarBeneficiarioInput').value = '';
    document.getElementById('clearBeneficiarioBtn').style.display = 'none';
    document.getElementById('beneficiarioDropdown').innerHTML = '';
    document.getElementById('beneficiarioDropdown').classList.remove('open');
    document.getElementById('obsBeneficiario').value = '';
    const btnVincular = document.getElementById('btnVincularBen');
    btnVincular.disabled = true; btnVincular.style.opacity = '0.5';

    cargarListaBeneficiarios();
    document.getElementById('modalBeneficiario').style.display = 'flex';
    setTimeout(() => document.getElementById('buscarBeneficiarioInput').focus(), 200);
}

function cargarListaBeneficiarios() {
    fetch('beneficiarios?action=listar')
        .then(r => r.json()).then(lista => {
            _listaBeneficiarios = (Array.isArray(lista) ? lista : []).filter(b => b.estado === 'ACTIVO');
            renderBeneficiarioDropdown(_listaBeneficiarios);
        });
}

function renderBeneficiarioDropdown(lista) {
    const dropdown = document.getElementById('beneficiarioDropdown');
    if (!lista || lista.length === 0) {
        dropdown.innerHTML = '<div class="search-select-empty">No se encontraron beneficiarios</div>';
        dropdown.classList.add('open');
        return;
    }
    dropdown.innerHTML = lista.map(b => `
        <div class="search-select-option" data-id="${b.idBeneficiario}" data-nombre="${b.nombreResponsable} ${b.apellidosResponsable}" data-dni="${b.dni || ''}">
            <div class="search-opt-name">${b.organizacion || '—'} (${b.nombreResponsable || ''} ${b.apellidosResponsable || ''})</div>
            <div class="search-opt-dni">DNI: ${b.dni || 'Sin DNI'} — ${b.distrito || ''}</div>
        </div>
    `).join('');
    dropdown.querySelectorAll('.search-select-option').forEach(opt => {
        opt.addEventListener('click', function() {
            seleccionarBeneficiario(this.dataset.id, this.dataset.nombre, this.dataset.dni);
        });
    });
    dropdown.classList.add('open');
}

function seleccionarBeneficiario(id, nombre, dni) {
    _beneficiarioSeleccionado = id;
    document.getElementById('selBeneficiario').value = id;
    document.getElementById('buscarBeneficiarioInput').value = nombre + (dni ? ' — ' + dni : '');
    document.getElementById('clearBeneficiarioBtn').style.display = 'flex';
    document.getElementById('beneficiarioDropdown').classList.remove('open');
    const btnVincular = document.getElementById('btnVincularBen');
    btnVincular.disabled = false; btnVincular.style.opacity = '1';
}

function limpiarSeleccionBeneficiario() {
    _beneficiarioSeleccionado = null;
    document.getElementById('selBeneficiario').value = '';
    document.getElementById('buscarBeneficiarioInput').value = '';
    document.getElementById('clearBeneficiarioBtn').style.display = 'none';
    const btnVincular = document.getElementById('btnVincularBen');
    btnVincular.disabled = true; btnVincular.style.opacity = '0.5';
    renderBeneficiarioDropdown(_listaBeneficiarios);
    document.getElementById('buscarBeneficiarioInput').focus();
}

function cerrarModalBeneficiario() {
    document.getElementById('modalBeneficiario').style.display = 'none';
    document.getElementById('beneficiarioDropdown').classList.remove('open');
}

// — Búsqueda de beneficiarios —
document.addEventListener('DOMContentLoaded', function() {
    const inputB = document.getElementById('buscarBeneficiarioInput');
    if (inputB) {
        inputB.addEventListener('input', function() {
            _beneficiarioSeleccionado = null;
            document.getElementById('selBeneficiario').value = '';
            const btnV = document.getElementById('btnVincularBen');
            btnV.disabled = true; btnV.style.opacity = '0.5';
            const q = this.value.trim().toLowerCase();
            document.getElementById('clearBeneficiarioBtn').style.display = q ? 'flex' : 'none';
            const filtrados = _listaBeneficiarios.filter(b => {
                const texto = (
                    (b.organizacion || '') + ' ' +
                    (b.direccion || '') + ' ' +
                    (b.distrito || '') + ' ' +
                    (b.necesidadPrincipal || '') + ' ' +
                    (b.observaciones || '') + ' ' +
                    (b.nombreResponsable || '') + ' ' +
                    (b.apellidosResponsable || '') + ' ' +
                    (b.dni || '') + ' ' +
                    (b.telefono || '')
                ).toLowerCase();
                return texto.includes(q);
            });
            renderBeneficiarioDropdown(filtrados);
        });
        inputB.addEventListener('focus', function() {
            if (!_beneficiarioSeleccionado && _listaBeneficiarios.length > 0) {
                const q = this.value.trim().toLowerCase();
                const filtrados = q ? _listaBeneficiarios.filter(b => {
                    const texto = (
                        (b.organizacion || '') + ' ' +
                        (b.direccion || '') + ' ' +
                        (b.distrito || '') + ' ' +
                        (b.necesidadPrincipal || '') + ' ' +
                        (b.observaciones || '') + ' ' +
                        (b.nombreResponsable || '') + ' ' +
                        (b.apellidosResponsable || '') + ' ' +
                        (b.dni || '') + ' ' +
                        (b.telefono || '')
                    ).toLowerCase();
                    return texto.includes(q);
                }) : _listaBeneficiarios;
                renderBeneficiarioDropdown(filtrados);
            }
        });
    }
    const clearBtnB = document.getElementById('clearBeneficiarioBtn');
    if (clearBtnB) clearBtnB.addEventListener('click', limpiarSeleccionBeneficiario);

    document.addEventListener('click', function(e) {
        const cB = document.getElementById('beneficiarioSearchContainer');
        if (cB && !cB.contains(e.target)) {
            document.getElementById('beneficiarioDropdown').classList.remove('open');
        }
    });
});

// — Modal Nuevo Beneficiario —
function abrirModalNuevoBeneficiario() {
    document.getElementById('formNuevoBeneficiario').reset();
    document.getElementById('modalNuevoBeneficiario').style.display = 'flex';
    setTimeout(() => document.getElementById('nbDni').focus(), 200);
}

// Buscar DNI en API y llenar nombres/apellidos
async function buscarDniBeneficiario() {
    const dniVal = document.getElementById('nbDni').value.trim();
    if (!dniVal || dniVal.length !== 8) {
        mostrarToast('Ingresa un DNI válido de 8 dígitos', 'warning');
        return;
    }
    const btn = document.querySelector('#modalNuevoBeneficiario .btn-search-dni');
    if (btn) { btn.disabled = true; btn.textContent = 'Buscando...'; }

    try {
        const datos = await buscarDNIEnAPI(dniVal);
        if (datos) {
            // Nombres
            if (datos.nombres) {
                document.getElementById('nbNombres').value = datos.nombres;
            }
            // Apellidos
            let apellido = '';
            if ((datos.apellido_paterno || datos.apellidoPaterno) || (datos.apellido_materno || datos.apellidoMaterno)) {
                const a1 = datos.apellido_paterno || datos.apellidoPaterno || '';
                const a2 = datos.apellido_materno || datos.apellidoMaterno || '';
                apellido = (a1 + ' ' + a2).trim();
            } else if (datos.apellidos) {
                apellido = datos.apellidos;
            }
            if (apellido) {
                document.getElementById('nbApellidos').value = apellido;
            }
            mostrarToast('DNI encontrado', 'success');
        }
    } catch (err) {
        console.error('Error buscando DNI:', err);
        mostrarToast('Error al buscar DNI', 'error');
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = '🔍 Buscar'; }
    }
}

function cerrarModalNuevoBeneficiario() {
    document.getElementById('modalNuevoBeneficiario').style.display = 'none';
}

async function guardarNuevoBeneficiario(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    params.append('action', 'crear');
    params.append('organizacion', document.getElementById('nbOrganizacion').value.trim());
    params.append('direccion', document.getElementById('nbDireccion').value.trim());
    params.append('distrito', document.getElementById('nbDistrito').value.trim());
    params.append('necesidadPrincipal', document.getElementById('nbNecesidad').value.trim());
    params.append('observaciones', document.getElementById('nbObservaciones').value.trim());
    params.append('nombreResponsable', document.getElementById('nbNombreResponsable').value.trim());
    params.append('apellidosResponsable', document.getElementById('nbApellidosResponsable').value.trim());
    params.append('dni', document.getElementById('nbDni').value.trim());
    params.append('telefono', document.getElementById('nbTelefono').value.trim());

    try {
        const response = await fetch('beneficiarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });
        const data = await response.json();
        if (data.success) {
            mostrarToast('Beneficiario registrado correctamente', 'success');
            cerrarModalNuevoBeneficiario();
            // Recargar lista y auto-seleccionar el nuevo
            const lista = await fetch('beneficiarios?action=listar').then(r => r.json());
            _listaBeneficiarios = (Array.isArray(lista) ? lista : []).filter(b => b.estado === 'ACTIVO');
            if (data.idBeneficiario) {
                const nuevo = _listaBeneficiarios.find(b => b.idBeneficiario == data.idBeneficiario);
                if (nuevo) {
                    seleccionarBeneficiario(nuevo.idBeneficiario, nuevo.nombres + ' ' + nuevo.apellidos, nuevo.dni);
                }
            }
        } else {
            mostrarToast(data.message || 'Error al registrar beneficiario', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarToast('Error de conexión', 'error');
    }
}

function guardarBeneficiario(e) {
    e.preventDefault();
    const idBen = document.getElementById('selBeneficiario').value;
    if (!idBen) {
        mostrarToast('Seleccione un beneficiario de la lista', 'warning');
        return;
    }
    const params = new URLSearchParams();
    params.append('action', 'agregarBeneficiario');
    params.append('idActividad', ID_ACT);
    params.append('idBeneficiario', idBen);
    params.append('observacion', document.getElementById('obsBeneficiario').value);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cerrarModalBeneficiario(); cargarBeneficiarios(); mostrarToast(res.message, 'success'); }
            else mostrarToast(res.message, 'error');
        }).catch(() => mostrarToast('Error al vincular beneficiario', 'error'));
}

function eliminarBeneficiario(id) {
    if (!confirm('¿Desvincular a este beneficiario de la actividad?')) return;
    const params = new URLSearchParams();
    params.append('action', 'eliminarBeneficiario');
    params.append('idActividadBeneficiario', id);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cargarBeneficiarios(); mostrarToast('Beneficiario desvinculado', 'success'); }
            else mostrarToast(res.message, 'error');
        });
}

// ══════════════════════════════════════
//  LOCALIDADES
// ══════════════════════════════════════
function cargarLocalidades() {
    fetch(`${BASE}?action=catalogoLugares`)
        .then(r => r.json())
        .then(lista => {
            const container = document.getElementById('lista-localidades');
            if (!lista.length) {
                container.innerHTML = '<div class="empty-msg">No hay localidades registradas. Registre una nueva.</div>';
                return;
            }
            container.innerHTML = lista.map(l => `
                <div class="localidad-card">
                    <div class="localidad-icon"><i class="fas fa-map-marker-alt"></i></div>
                    <div class="localidad-body">
                        <strong>${l.distrito || ''}</strong>
                        <span>${l.provincia || ''}, ${l.departamento || ''}</span>
                        ${l.direccionReferencia ? '<small>' + l.direccionReferencia + '</small>' : ''}
                    </div>
                </div>
            `).join('');
        }).catch(() => {
            document.getElementById('lista-localidades').innerHTML = '<div class="empty-msg">Error al cargar localidades</div>';
        });
}

function abrirModalLugar() {
    document.getElementById('formLugar').reset();
    document.getElementById('modalLugar').style.display = 'flex';
}
function cerrarModalLugar() { document.getElementById('modalLugar').style.display = 'none'; }

function guardarLugar(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    params.append('action', 'crearLugar');
    params.append('departamento', document.getElementById('lugarDepartamento').value);
    params.append('provincia', document.getElementById('lugarProvincia').value);
    params.append('distrito', document.getElementById('lugarDistrito').value);
    params.append('direccionReferencia', document.getElementById('lugarDireccion').value);
    fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() })
        .then(r => r.json()).then(res => {
            if (res.success) { cerrarModalLugar(); cargarLocalidades(); mostrarToast(res.message, 'success'); }
            else mostrarToast(res.message, 'error');
        }).catch(() => mostrarToast('Error al registrar localidad', 'error'));
}

// ══════════════════════════════════════
//  TOAST  (delegado al sistema global Notify)
// ══════════════════════════════════════
function mostrarToast(msg, tipo) {
    if (typeof Notify !== 'undefined') {
        if (tipo === 'success') Notify.success(msg);
        else if (tipo === 'error') Notify.error(msg);
        else if (tipo === 'warning') Notify.warning(msg);
        else Notify.info(msg);
    }
}
// Alias para dni-api.js que usa mostrarNotificacion
function mostrarNotificacion(msg, tipo) { mostrarToast(msg, tipo); }

// ── INIT ──
document.addEventListener('DOMContentLoaded', () => {
    cargarRecursos();
});

// ── Cerrar modales con Escape ──
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay').forEach(m => m.style.display = 'none');
    }
});
document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) e.target.style.display = 'none';
});
