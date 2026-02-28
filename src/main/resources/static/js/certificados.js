/**
 * CERTIFICADOS.JS - Sistema de Voluntariado Universitario
 * Manejo de certificados de voluntariado
 */

// El contextPath se define desde el JSP antes de cargar este archivo
// const contextPath = '${pageContext.request.contextPath}';
let buscarCertificadosInput = null;

// ==========================================
// MODAL EMITIR CERTIFICADO
// ==========================================

function abrirModal() {
    document.getElementById('modalCertificado').classList.add('active');
    document.getElementById('formCertificado').reset();
    document.getElementById('tituloModal').textContent = 'Emitir Certificado';
    // Resetear estado
    const infoContainer = document.getElementById('infoHorasContainer');
    if (infoContainer) infoContainer.style.display = 'none';
    const btnSubmit = document.querySelector('#formCertificado button[type="submit"]');
    if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.style.opacity = '0.5'; }
    // Resetear select de voluntarios
    const selVol = document.getElementById('idVoluntario');
    selVol.innerHTML = '<option value="">Primero seleccione una actividad</option>';
}

async function cargarVoluntariosPorActividad() {
    const idActividad = document.getElementById('idActividad').value;
    const select = document.getElementById('idVoluntario');
    const infoContainer = document.getElementById('infoHorasContainer');
    const btnSubmit = document.querySelector('#formCertificado button[type="submit"]');

    if (infoContainer) infoContainer.style.display = 'none';
    if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.style.opacity = '0.5'; }

    if (!idActividad) {
        select.innerHTML = '<option value="">Primero seleccione una actividad</option>';
        return;
    }

    select.innerHTML = '<option value="">Cargando voluntarios...</option>';
    try {
        const response = await fetch(`${contextPath}/certificados?action=voluntariosPorActividad&idActividad=${idActividad}`);
        const voluntarios = await response.json();
        select.innerHTML = '<option value="">Seleccione un voluntario</option>';
        if (voluntarios && voluntarios.length > 0) {
            voluntarios.forEach(v => {
                const option = document.createElement('option');
                option.value = v.idVoluntario;
                option.textContent = `${v.nombres} ${v.apellidos} - DNI: ${v.dni}`;
                option.dataset.horas = v.horas || 0;
                select.appendChild(option);
            });
        } else {
            select.innerHTML = '<option value="">No hay voluntarios con asistencia en esta actividad</option>';
        }
    } catch (error) {
        console.error('Error al cargar voluntarios:', error);
        select.innerHTML = '<option value="">Error al cargar voluntarios</option>';
    }

    // Agregar listener para habilitar botón al seleccionar voluntario
    select.onchange = function() {
        const selectedOption = select.options[select.selectedIndex];
        if (select.value) {
            const infoMsg = document.getElementById('infoHorasMsg');
            if (infoMsg) {
                infoMsg.style.background = '#ecfdf5';
                infoMsg.style.border = '1px solid #6ee7b7';
                infoMsg.style.color = '#065f46';
                infoMsg.innerHTML = '<i class="fa-solid fa-check-circle"></i> Este voluntario tiene asistencia registrada en esta actividad.';
            }
            if (infoContainer) infoContainer.style.display = 'block';
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.style.opacity = '1'; }
        } else {
            if (infoContainer) infoContainer.style.display = 'none';
            if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.style.opacity = '0.5'; }
        }
    };
}

function cerrarModal() {
    document.getElementById('modalCertificado').classList.remove('active');
}

// ==========================================
// MODAL VERIFICAR CERTIFICADO
// ==========================================

function abrirModalVerificar() {
    document.getElementById('modalVerificar').classList.add('active');
    document.getElementById('codigoVerificar').value = '';
    document.getElementById('resultadoVerificacion').style.display = 'none';
}

function cerrarModalVerificar() {
    document.getElementById('modalVerificar').classList.remove('active');
}

// ==========================================
// MODAL VER CERTIFICADO
// ==========================================

function cerrarModalVer() {
    document.getElementById('modalVer').classList.remove('active');
    document.getElementById('certificadoPDFFrame').src = '';
}

// ==========================================
// CRUD CERTIFICADOS
// ==========================================

async function guardarCertificado(event) {
    event.preventDefault();

    const idActividad = document.getElementById('idActividad').value;
    const idVoluntario = document.getElementById('idVoluntario').value;

    if (!idActividad || !idVoluntario) {
        mostrarAlerta('error', 'Debe seleccionar una actividad y un voluntario.');
        return;
    }

    const params = new URLSearchParams();
    params.append('action', 'crear');
    params.append('idVoluntario', idVoluntario);
    params.append('idActividad', idActividad);
    params.append('observaciones', document.getElementById('observaciones').value);

    try {
        const response = await fetch(`${contextPath}/certificados`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        const data = await response.json();

        if (data.success) {
            mostrarAlerta('success', 'Certificado emitido correctamente');
            cerrarModal();
            setTimeout(() => location.reload(), 1000);
        } else {
            mostrarAlerta('error', data.message || 'Error al emitir certificado');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error de conexión');
    }
}

function verCertificado(id) {
    const iframe = document.getElementById('certificadoPDFFrame');
    iframe.src = `${contextPath}/certificados/pdf?id=${id}`;
    document.getElementById('modalVer').classList.add('active');
}

function renderizarCertificado(cert) {
    const container = document.getElementById('certificadoContent');
    
    const estadoClase = cert.estado === 'ANULADO' ? 'style="opacity: 0.5; position: relative;"' : '';
    const marcaAnulado = cert.estado === 'ANULADO' 
        ? '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-30deg); font-size: 4rem; color: #ef4444; font-weight: bold; opacity: 0.3; pointer-events: none;">ANULADO</div>' 
        : '';
    
    container.innerHTML = `
        <div ${estadoClase}>
            ${marcaAnulado}
            <div class="logo-placeholder">
                <i class="fa-solid fa-hands-helping"></i>
            </div>
            
            <h2>Certificado de Voluntariado</h2>
            <p class="subtitulo">Sistema de Voluntariado Universitario</p>
            
            <p class="texto-certificado">
                Se certifica que
            </p>
            
            <p class="nombre-voluntario">${cert.nombreVoluntario}</p>
            <p style="color: #64748b; margin-top: 0;">DNI: ${cert.dniVoluntario}</p>
            
            <p class="texto-certificado">
                Ha participado satisfactoriamente en la actividad de voluntariado<br>
                <span class="actividad-nombre">"${cert.nombreActividad}"</span>
            </p>
            
            ${cert.observaciones ? `<p style="color: #64748b; font-style: italic; margin-top: 1rem;">${cert.observaciones}</p>` : ''}
            
            <div class="codigo-verificacion">
                <p style="color: #94a3b8; font-size: 0.8rem; margin-bottom: 0.5rem;">Código de verificación:</p>
                <span>${cert.codigoCertificado}</span>
            </div>
            
            <p class="fecha-emision">
                Emitido el ${formatearFecha(cert.fechaEmision)}
            </p>
        </div>
    `;
}

async function verificarCertificado() {
    const codigo = document.getElementById('codigoVerificar').value.trim().toUpperCase();
    
    if (!codigo) {
        mostrarAlerta('warning', 'Ingrese un código de certificado');
        return;
    }

    try {
        const response = await fetch(`${contextPath}/certificados?action=verificar&codigo=${encodeURIComponent(codigo)}`);
        const data = await response.json();

        const resultado = document.getElementById('resultadoVerificacion');
        resultado.style.display = 'block';

        if (data.success && data.certificado) {
            const cert = data.certificado;
            
            if (cert.estado === 'EMITIDO') {
                resultado.className = 'resultado-verificacion valido';
                resultado.innerHTML = `
                    <h4><i class="fa-solid fa-check-circle"></i> Certificado Válido</h4>
                    <p><strong>Voluntario:</strong> ${cert.nombreVoluntario}</p>
                    <p><strong>DNI:</strong> ${cert.dniVoluntario}</p>
                    <p><strong>Actividad:</strong> ${cert.nombreActividad}</p>
                    <p><strong>Fecha de emisión:</strong> ${formatearFecha(cert.fechaEmision)}</p>
                `;
            } else {
                resultado.className = 'resultado-verificacion invalido';
                resultado.innerHTML = `
                    <h4><i class="fa-solid fa-ban"></i> Certificado Anulado</h4>
                    <p>Este certificado ha sido anulado y ya no tiene validez.</p>
                `;
            }
        } else {
            resultado.className = 'resultado-verificacion invalido';
            resultado.innerHTML = `
                <h4><i class="fa-solid fa-times-circle"></i> Certificado No Encontrado</h4>
                <p>No existe ningún certificado con el código ingresado.</p>
            `;
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error de conexión');
    }
}

async function anularCertificado(id) {
    const ok = await Notify.confirm('¿Anular este certificado?', 'Esta acción no se puede deshacer.', { variant: 'danger', okText: 'Sí, anular' });
    if (!ok) {
        return;
    }

    const motivo = prompt('Ingrese el motivo de la anulación:');
    if (!motivo) {
        mostrarAlerta('warning', 'Debe ingresar un motivo para anular');
        return;
    }

    try {
        const params = new URLSearchParams();
        params.append('action', 'anular');
        params.append('id', id);
        params.append('motivo', motivo);

        const response = await fetch(`${contextPath}/certificados`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        const data = await response.json();

        if (data.success) {
            mostrarAlerta('success', 'Certificado anulado correctamente');
            setTimeout(() => location.reload(), 1000);
        } else {
            mostrarAlerta('error', data.message || 'Error al anular certificado');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error de conexión');
    }
}

function imprimirCertificado(id) {
    // Abrir PDF en nueva pestaña
    window.open(`${contextPath}/certificados/pdf?id=${id}`, '_blank');
}

// ==========================================
// UTILIDADES
// ==========================================

function formatearFecha(fecha) {
    if (!fecha) return '-';
    const date = new Date(fecha);
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return date.toLocaleDateString('es-ES', options);
}

function mostrarAlerta(tipo, mensaje) {
    if (typeof Notify !== 'undefined') {
        Notify[tipo === 'warning' ? 'warning' : tipo === 'error' ? 'error' : 'success'](mensaje);
        return;
    }
    // Crear elemento de alerta
    const alerta = document.createElement('div');
    alerta.className = `alerta alerta-${tipo}`;
    
    const iconos = {
        success: 'fa-check-circle',
        error: 'fa-times-circle',
        warning: 'fa-exclamation-triangle'
    };
    
    const colores = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b'
    };
    
    alerta.innerHTML = `
        <i class="fa-solid ${iconos[tipo] || iconos.success}"></i>
        <span>${mensaje}</span>
    `;
    
    alerta.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        padding: 1rem 1.5rem;
        border-radius: 10px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 0.75rem;
        z-index: 9999;
        animation: slideIn 0.3s ease;
        border-left: 4px solid ${colores[tipo] || colores.success};
    `;
    
    alerta.querySelector('i').style.color = colores[tipo] || colores.success;
    
    document.body.appendChild(alerta);
    
    // Remover después de 3 segundos
    setTimeout(() => {
        alerta.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => alerta.remove(), 300);
    }, 3000);
}

// Agregar animaciones
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);

// ==========================================
// CERRAR MODALES CON ESCAPE O CLICK FUERA
// ==========================================

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        cerrarModal();
        cerrarModalVerificar();
        cerrarModalVer();
    }
});

document.querySelectorAll('.modal-overlay').forEach(modal => {
    modal.addEventListener('click', function(e) {
        if (e.target === this) {
            cerrarModal();
            cerrarModalVerificar();
            cerrarModalVer();
        }
    });
});

// ==========================================
// INICIALIZACIÓN
// ==========================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('Módulo de Certificados cargado');
    cargarCertificados();
});

// ==========================================
// CARGAR CERTIFICADOS VÍA AJAX
// ==========================================
function cargarCertificados() {
    fetch('certificados?action=listar')
        .then(response => response.json())
        .then(certificados => {
            const tbody = document.getElementById('tbodyCertificados');
            tbody.innerHTML = '';

            if (certificados && certificados.length > 0) {
                certificados.forEach(c => {
                    const fila = document.createElement('tr');
                    fila.className = 'certificado-row';
                    fila.innerHTML = `
                        <td>
                            <span class="codigo-certificado">
                                <i class="fa-solid fa-qrcode"></i>
                                ${c.codigoCertificado}
                            </span>
                        </td>
                        <td>${c.nombreVoluntario || ''}</td>
                        <td>${c.dniVoluntario || ''}</td>
                        <td>${c.nombreActividad || ''}</td>
                        <td>${c.fechaEmision || ''}</td>
                        <td>
                            <span class="estado-badge ${(c.estado || '').toLowerCase()}">
                                ${c.estado || ''}
                            </span>
                        </td>
                        <td class="acciones-cell">
                            <button class="btn-icon view" onclick="verCertificado(${c.idCertificado})" title="Ver Certificado">
                                <i class="fa-solid fa-eye"></i>
                            </button>
                            <button class="btn-icon print" onclick="imprimirCertificado(${c.idCertificado})" title="Imprimir">
                                <i class="fa-solid fa-print"></i>
                            </button>
                            ${c.estado === 'EMITIDO' ? `<button class="btn-icon delete" onclick="anularCertificado(${c.idCertificado})" title="Anular"><i class="fa-solid fa-ban"></i></button>` : ''}
                        </td>
                    `;
                    tbody.appendChild(fila);
                });
            } else {
                tbody.innerHTML = '<tr id="sinCertificadosRow"><td colspan="7" class="no-data">No hay certificados emitidos</td></tr>';
            }
            aplicarFiltroCertificados();
        })
        .catch(error => {
            console.error('Error al cargar certificados:', error);
        });
}

function aplicarFiltroCertificados() {
    const q = (buscarCertificadosInput?.value || '').trim().toLowerCase();
    const tbody = document.getElementById('tbodyCertificados');
    if (!tbody) return;

    const filas = Array.from(tbody.querySelectorAll('tr.certificado-row'));
    filas.forEach((fila) => {
        const texto = (fila.textContent || '').toLowerCase();
        const match = !q || texto.includes(q);
        fila.classList.toggle('filtro-oculto', !match);
    });

    let filaNoResultados = document.getElementById('sinResultadosCertificadosRow');
    if (!filaNoResultados) {
        filaNoResultados = document.createElement('tr');
        filaNoResultados.id = 'sinResultadosCertificadosRow';
        filaNoResultados.innerHTML = '<td colspan="7" class="no-data">No hay certificados que coincidan con la busqueda</td>';
        tbody.appendChild(filaNoResultados);
    }
    const hayCoincidencias = filas.some(f => !f.classList.contains('filtro-oculto'));
    filaNoResultados.style.display = hayCoincidencias ? 'none' : '';

    const sinCertificados = document.getElementById('sinCertificadosRow');
    if (sinCertificados) sinCertificados.style.display = filas.length === 0 && !q ? '' : 'none';
    if (filas.length === 0) filaNoResultados.style.display = q ? '' : 'none';
}

document.addEventListener('DOMContentLoaded', function() {
    buscarCertificadosInput = document.getElementById('buscarCertificados');
    if (buscarCertificadosInput) {
        buscarCertificadosInput.addEventListener('input', aplicarFiltroCertificados);
    }
});
