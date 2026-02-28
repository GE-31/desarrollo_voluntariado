/* ═══════════════════════════════════════════════════════════════
   SALIDAS DE INVENTARIO - JS (Carrito de Salida)
   ═══════════════════════════════════════════════════════════════ */

const BASE = window.location.pathname.substring(0, window.location.pathname.indexOf('/salidas-inventario'));
const URL_BASE = BASE + '/salidas-inventario';

// ── Estado global ──
let salidas = [];
let itemsDisponibles = [];
let carrito = [];
let paginaActual = 1;
const PAGINA_TAMANO = 10;

// ═══════════════════════════════════════════════════════════
// INICIALIZACIÓN
// ═══════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
    cargarSalidas();
    configurarFiltros();
});

function configurarFiltros() {
    const inputBuscar = document.getElementById('buscarSalidasInv');
    const selectEstado = document.getElementById('filtroEstadoInv');
    if (inputBuscar) inputBuscar.addEventListener('input', filtrarSalidas);
    if (selectEstado) selectEstado.addEventListener('change', filtrarSalidas);
}

// ═══════════════════════════════════════════════════════════
// LISTAR SALIDAS
// ═══════════════════════════════════════════════════════════

function cargarSalidas() {
    fetch(URL_BASE + '?accion=listar')
        .then(r => r.json())
        .then(data => {
            salidas = data || [];
            actualizarResumen();
            renderizarTabla();
        })
        .catch(err => {
            console.error('Error al cargar salidas:', err);
            salidas = [];
            renderizarTabla();
        });
}

function actualizarResumen() {
    const total = salidas.length;
    const confirmadas = salidas.filter(s => s.estado === 'CONFIRMADO').length;
    const anuladas = salidas.filter(s => s.estado === 'ANULADO').length;
    const totalItems = salidas.reduce((sum, s) => sum + (s.totalItems || 0), 0);

    document.getElementById('sumTotal').textContent = total;
    document.getElementById('sumItems').textContent = totalItems;
    document.getElementById('sumConfirmadas').textContent = confirmadas;
    document.getElementById('sumAnuladas').textContent = anuladas;
}

function filtrarSalidas() {
    paginaActual = 1;
    renderizarTabla();
}

function obtenerSalidasFiltradas() {
    const buscar = (document.getElementById('buscarSalidasInv')?.value || '').toLowerCase().trim();
    const estado = document.getElementById('filtroEstadoInv')?.value || '';

    return salidas.filter(s => {
        if (estado && s.estado !== estado) return false;
        if (buscar) {
            const texto = [s.motivo, s.actividadNombre, s.usuarioRegistro, s.observacion]
                .filter(Boolean).join(' ').toLowerCase();
            if (!texto.includes(buscar)) return false;
        }
        return true;
    });
}

function renderizarTabla() {
    const tbody = document.getElementById('tbodySalidasInv');
    const filtradas = obtenerSalidasFiltradas();
    const totalPaginas = Math.ceil(filtradas.length / PAGINA_TAMANO);
    const inicio = (paginaActual - 1) * PAGINA_TAMANO;
    const paginadas = filtradas.slice(inicio, inicio + PAGINA_TAMANO);

    if (paginadas.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="no-data">
                    <i class="fa-solid fa-inbox" style="font-size:2rem; margin-bottom:8px; display:block; color:#cbd5e1;"></i>
                    No hay salidas de inventario registradas
                </td>
            </tr>`;
        document.getElementById('salidasInvPaginacion').style.display = 'none';
        return;
    }

    tbody.innerHTML = paginadas.map(s => {
        const estadoClass = s.estado === 'CONFIRMADO' ? 'success' : (s.estado === 'ANULADO' ? 'error' : 'warning');
        const fecha = s.registradoEn ? formatearFecha(s.registradoEn) : '-';
        return `
            <tr class="salida-inv-row" data-estado="${s.estado}">
                <td><span class="salida-id">#${s.idSalidaInv}</span></td>
                <td><span class="motivo-text">${escHTML(s.motivo)}</span></td>
                <td>
                    <div class="campana-destino">
                        <i class="fa-solid fa-bullhorn"></i>
                        <span>${escHTML(s.actividadNombre || 'Sin actividad')}</span>
                    </div>
                </td>
                <td>
                    <span class="items-badge">${s.totalItems} item${s.totalItems !== 1 ? 's' : ''}</span>
                </td>
                <td>${escHTML(s.usuarioRegistro)}</td>
                <td>${fecha}</td>
                <td><span class="badge ${estadoClass}">${s.estado}</span></td>
                <td class="acciones-cell">
                    <button class="btn-icon view" onclick="verDetalle(${s.idSalidaInv})" title="Ver detalle">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${s.estado === 'CONFIRMADO' ? `
                    <button class="btn-icon cancel" onclick="anularSalida(${s.idSalidaInv})" title="Anular salida">
                        <i class="fas fa-ban"></i>
                    </button>` : ''}
                </td>
            </tr>`;
    }).join('');

    // Paginación
    const pag = document.getElementById('salidasInvPaginacion');
    if (totalPaginas > 1) {
        pag.style.display = 'flex';
        document.getElementById('textoPaginacionSI').textContent = `Página ${paginaActual} de ${totalPaginas}`;
        document.getElementById('btnPaginaAnteriorSI').disabled = paginaActual <= 1;
        document.getElementById('btnPaginaSiguienteSI').disabled = paginaActual >= totalPaginas;
        document.getElementById('btnPaginaAnteriorSI').onclick = () => { paginaActual--; renderizarTabla(); };
        document.getElementById('btnPaginaSiguienteSI').onclick = () => { paginaActual++; renderizarTabla(); };
    } else {
        pag.style.display = 'none';
    }
}

// ═══════════════════════════════════════════════════════════
// MODAL: REGISTRAR SALIDA (CARRITO)
// ═══════════════════════════════════════════════════════════

function abrirModalSalida() {
    carrito = [];
    document.getElementById('salidaMotivo').value = '';
    document.getElementById('salidaObservacion').value = '';
    document.getElementById('carritoCantidad').value = '';
    document.getElementById('carritoStockDisponible').value = '-';
    ocultarErrorCarrito();
    renderizarCarrito();

    // Cargar actividades
    cargarActividades();
    // Cargar items disponibles
    cargarItemsDisponibles();

    document.getElementById('modalSalidaInv').classList.add('active');
}

function cerrarModalSalida() {
    document.getElementById('modalSalidaInv').classList.remove('active');
    carrito = [];
}

function cargarActividades() {
    fetch(URL_BASE + '?accion=actividades')
        .then(r => r.json())
        .then(data => {
            const select = document.getElementById('salidaActividad');
            select.innerHTML = '<option value="0">Sin actividad asociada</option>';
            (data || []).forEach(a => {
                select.innerHTML += `<option value="${a.idActividad}">${escHTML(a.nombre)}</option>`;
            });
        })
        .catch(err => console.error('Error al cargar actividades:', err));
}

function cargarItemsDisponibles() {
    fetch(URL_BASE + '?accion=items_disponibles')
        .then(r => r.json())
        .then(data => {
            itemsDisponibles = data || [];
            actualizarSelectItems();
        })
        .catch(err => console.error('Error al cargar items:', err));
}

function actualizarSelectItems() {
    const select = document.getElementById('carritoItemSelect');
    select.innerHTML = '<option value="">Seleccione un item</option>';

    // Excluir items ya en el carrito
    const idsEnCarrito = carrito.map(c => c.idItem);

    itemsDisponibles
        .filter(item => !idsEnCarrito.includes(item.idItem))
        .forEach(item => {
            const stockText = item.stockActual % 1 === 0 ? item.stockActual.toFixed(0) : item.stockActual.toFixed(2);
            select.innerHTML += `<option value="${item.idItem}" 
                data-stock="${item.stockActual}" 
                data-nombre="${escHTML(item.nombre)}"
                data-categoria="${escHTML(item.categoria || '')}"
                data-unidad="${escHTML(item.unidadMedida || 'unidad')}">
                ${escHTML(item.nombre)} (${item.categoria || 'Sin cat.'}) — Stock: ${stockText} ${item.unidadMedida || ''}
            </option>`;
        });

    // Evento cambio
    select.onchange = () => {
        const opt = select.selectedOptions[0];
        if (opt && opt.value) {
            const stock = parseFloat(opt.dataset.stock || 0);
            document.getElementById('carritoStockDisponible').value = formatNum(stock) + ' ' + (opt.dataset.unidad || '');
            document.getElementById('carritoCantidad').max = stock;
            document.getElementById('carritoCantidad').value = '';
            document.getElementById('carritoCantidad').focus();
        } else {
            document.getElementById('carritoStockDisponible').value = '-';
            document.getElementById('carritoCantidad').value = '';
        }
    };
}

// ═══════════════════════════════════════════════════════════
// CARRITO: AGREGAR / ELIMINAR / RENDERIZAR
// ═══════════════════════════════════════════════════════════

function agregarAlCarrito() {
    const select = document.getElementById('carritoItemSelect');
    const cantidadInput = document.getElementById('carritoCantidad');
    const opt = select.selectedOptions[0];

    if (!opt || !opt.value) {
        mostrarErrorCarrito('Selecciona un item del inventario.');
        return;
    }

    const idItem = parseInt(opt.value);
    const cantidad = parseFloat(cantidadInput.value);
    const stock = parseFloat(opt.dataset.stock || 0);
    const nombre = opt.dataset.nombre;
    const categoria = opt.dataset.categoria;
    const unidad = opt.dataset.unidad;

    if (isNaN(cantidad) || cantidad <= 0) {
        mostrarErrorCarrito('La cantidad debe ser mayor a 0.');
        return;
    }

    if (cantidad > stock) {
        mostrarErrorCarrito(`La cantidad (${formatNum(cantidad)}) excede el stock disponible (${formatNum(stock)} ${unidad}).`);
        return;
    }

    // Verificar si ya está en el carrito
    if (carrito.some(c => c.idItem === idItem)) {
        mostrarErrorCarrito('Este item ya está en el carrito.');
        return;
    }

    carrito.push({
        idItem: idItem,
        nombre: nombre,
        categoria: categoria,
        unidad: unidad,
        stockActual: stock,
        cantidad: cantidad,
        stockResultante: stock - cantidad
    });

    ocultarErrorCarrito();
    cantidadInput.value = '';
    document.getElementById('carritoStockDisponible').value = '-';
    renderizarCarrito();
    actualizarSelectItems();
}

function eliminarDelCarrito(idItem) {
    carrito = carrito.filter(c => c.idItem !== idItem);
    renderizarCarrito();
    actualizarSelectItems();
}

function renderizarCarrito() {
    const tbody = document.getElementById('tbodyCarrito');
    const contador = document.getElementById('carritoContador');
    const btnConfirmar = document.getElementById('btnConfirmarSalida');

    if (carrito.length === 0) {
        tbody.innerHTML = `
            <tr id="carritoVacio">
                <td colspan="7" class="no-data carrito-vacio">
                    <i class="fa-solid fa-cart-shopping" style="font-size:2rem; margin-bottom:8px; display:block; color:#cbd5e1;"></i>
                    El carrito está vacío. Agrega items para registrar la salida.
                </td>
            </tr>`;
        contador.textContent = '0 items';
        btnConfirmar.disabled = true;
        return;
    }

    tbody.innerHTML = carrito.map((item, idx) => {
        const stockResultClass = item.stockResultante <= 0 ? 'stock-critico' : (item.stockResultante < 5 ? 'stock-bajo' : 'stock-ok');
        return `
            <tr class="carrito-row">
                <td><strong>${escHTML(item.nombre)}</strong></td>
                <td>${escHTML(item.categoria || '-')}</td>
                <td>${escHTML(item.unidad)}</td>
                <td><span class="stock-actual-badge">${formatNum(item.stockActual)}</span></td>
                <td>
                    <input type="number" class="carrito-cantidad-input" value="${formatNum(item.cantidad)}" 
                           min="0.01" max="${item.stockActual}" step="0.01"
                           onchange="actualizarCantidadCarrito(${item.idItem}, this.value)">
                </td>
                <td><span class="stock-resultante-badge ${stockResultClass}">${formatNum(item.stockResultante)}</span></td>
                <td>
                    <button type="button" class="btn-icon cancel" onclick="eliminarDelCarrito(${item.idItem})" title="Quitar del carrito">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </td>
            </tr>`;
    }).join('');

    contador.textContent = `${carrito.length} item${carrito.length !== 1 ? 's' : ''}`;
    btnConfirmar.disabled = false;
}

function actualizarCantidadCarrito(idItem, nuevoValor) {
    const item = carrito.find(c => c.idItem === idItem);
    if (!item) return;

    const cantidad = parseFloat(nuevoValor);
    if (isNaN(cantidad) || cantidad <= 0) {
        mostrarErrorCarrito('La cantidad debe ser mayor a 0.');
        renderizarCarrito();
        return;
    }
    if (cantidad > item.stockActual) {
        mostrarErrorCarrito(`La cantidad (${formatNum(cantidad)}) excede el stock disponible (${formatNum(item.stockActual)} ${item.unidad}).`);
        renderizarCarrito();
        return;
    }

    ocultarErrorCarrito();
    item.cantidad = cantidad;
    item.stockResultante = item.stockActual - cantidad;
    renderizarCarrito();
}

// ═══════════════════════════════════════════════════════════
// CONFIRMAR SALIDA
// ═══════════════════════════════════════════════════════════

function confirmarSalida() {
    const motivo = document.getElementById('salidaMotivo').value.trim();
    const observacion = document.getElementById('salidaObservacion').value.trim();
    const idActividad = parseInt(document.getElementById('salidaActividad').value) || 0;

    if (!motivo) {
        mostrarErrorCarrito('El motivo es obligatorio.');
        document.getElementById('salidaMotivo').focus();
        return;
    }

    if (carrito.length === 0) {
        mostrarErrorCarrito('Agrega al menos un item al carrito.');
        return;
    }

    // Validar cantidades
    for (const item of carrito) {
        if (item.cantidad <= 0 || item.cantidad > item.stockActual) {
            mostrarErrorCarrito(`Cantidad inválida para "${item.nombre}".`);
            return;
        }
    }

    const btnConfirmar = document.getElementById('btnConfirmarSalida');
    btnConfirmar.disabled = true;
    btnConfirmar.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Procesando...';

    const payload = {
        motivo: motivo,
        observacion: observacion,
        idActividad: idActividad,
        items: carrito.map(c => ({
            idItem: c.idItem,
            cantidad: c.cantidad
        }))
    };

    fetch(URL_BASE + '?accion=registrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(r => r.json())
    .then(data => {
        if (data.ok) {
            cerrarModalSalida();
            cargarSalidas();
            mostrarToast('✓ ' + data.message, 'success');
        } else {
            mostrarErrorCarrito(data.message || 'Error al registrar la salida.');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        mostrarErrorCarrito('Error de conexión al registrar la salida.');
    })
    .finally(() => {
        btnConfirmar.disabled = false;
        btnConfirmar.innerHTML = '<i class="fa-solid fa-check-circle"></i> Confirmar Salida';
    });
}

// ═══════════════════════════════════════════════════════════
// VER DETALLE
// ═══════════════════════════════════════════════════════════

function verDetalle(idSalidaInv) {
    document.getElementById('detalleIdSalida').textContent = idSalidaInv;

    // Cargar cabecera y detalle en paralelo
    Promise.all([
        fetch(URL_BASE + '?accion=obtener&id=' + idSalidaInv).then(r => r.json()),
        fetch(URL_BASE + '?accion=detalle&id=' + idSalidaInv).then(r => r.json())
    ]).then(([cabecera, detalles]) => {
        // Renderizar info de cabecera
        const estadoClass = cabecera.estado === 'CONFIRMADO' ? 'success' : (cabecera.estado === 'ANULADO' ? 'error' : 'warning');
        document.getElementById('detalleInfo').innerHTML = `
            <div class="detalle-grid">
                <div class="detalle-field">
                    <span class="detalle-label">Motivo</span>
                    <span class="detalle-value">${escHTML(cabecera.motivo)}</span>
                </div>
                <div class="detalle-field">
                    <span class="detalle-label">Actividad / Campaña</span>
                    <span class="detalle-value">${escHTML(cabecera.actividadNombre || 'Sin actividad')}</span>
                </div>
                <div class="detalle-field">
                    <span class="detalle-label">Registrado por</span>
                    <span class="detalle-value">${escHTML(cabecera.usuarioRegistro)}</span>
                </div>
                <div class="detalle-field">
                    <span class="detalle-label">Fecha</span>
                    <span class="detalle-value">${cabecera.registradoEn ? formatearFecha(cabecera.registradoEn) : '-'}</span>
                </div>
                <div class="detalle-field">
                    <span class="detalle-label">Estado</span>
                    <span class="badge ${estadoClass}">${cabecera.estado}</span>
                </div>
                ${cabecera.observacion ? `
                <div class="detalle-field full-width">
                    <span class="detalle-label">Observación</span>
                    <span class="detalle-value">${escHTML(cabecera.observacion)}</span>
                </div>` : ''}
                ${cabecera.motivoAnulacion ? `
                <div class="detalle-field full-width">
                    <span class="detalle-label">Motivo de anulación</span>
                    <span class="detalle-value text-danger">${escHTML(cabecera.motivoAnulacion)}</span>
                </div>` : ''}
            </div>`;

        // Renderizar tabla de detalle
        const tbodyDetalle = document.getElementById('tbodyDetalle');
        if (detalles && detalles.length > 0) {
            tbodyDetalle.innerHTML = detalles.map(d => `
                <tr>
                    <td><strong>${escHTML(d.itemNombre)}</strong></td>
                    <td>${escHTML(d.itemCategoria || '-')}</td>
                    <td>${escHTML(d.itemUnidad || '-')}</td>
                    <td>${formatNum(d.cantidad)}</td>
                    <td>${formatNum(d.stockAntes)}</td>
                    <td>${formatNum(d.stockDespues)}</td>
                </tr>`).join('');
        } else {
            tbodyDetalle.innerHTML = '<tr><td colspan="6" class="no-data">Sin detalles</td></tr>';
        }

        document.getElementById('modalDetalleSalida').classList.add('active');
    }).catch(err => {
        console.error('Error al cargar detalle:', err);
        mostrarToast('Error al cargar el detalle de la salida.', 'error');
    });
}

function cerrarModalDetalle() {
    document.getElementById('modalDetalleSalida').classList.remove('active');
}

// ═══════════════════════════════════════════════════════════
// ANULAR SALIDA
// ═══════════════════════════════════════════════════════════

function anularSalida(idSalidaInv) {
    const motivo = prompt('¿Motivo de la anulación?', 'Anulación manual');
    if (motivo === null) return;

    fetch(URL_BASE + '?accion=anular', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `idSalidaInv=${idSalidaInv}&motivo=${encodeURIComponent(motivo)}`
    })
    .then(r => r.json())
    .then(data => {
        if (data.ok) {
            cargarSalidas();
            mostrarToast('✓ ' + data.message, 'success');
        } else {
            mostrarToast(data.message || 'Error al anular la salida.', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        mostrarToast('Error de conexión.', 'error');
    });
}

// ═══════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════

function mostrarErrorCarrito(msg) {
    const el = document.getElementById('carritoError');
    el.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> ' + msg;
    el.style.display = 'block';
    setTimeout(() => { el.style.display = 'none'; }, 5000);
}

function ocultarErrorCarrito() {
    document.getElementById('carritoError').style.display = 'none';
}

function escHTML(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatNum(n) {
    if (n == null) return '0';
    return n % 1 === 0 ? n.toFixed(0) : n.toFixed(2);
}

function formatearFecha(str) {
    if (!str) return '-';
    try {
        const d = new Date(str);
        if (isNaN(d.getTime())) return str;
        return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' })
             + ' ' + d.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        return str;
    }
}

function mostrarToast(mensaje, tipo) {
    if (typeof Notify !== 'undefined') {
        // Limpiar emojis de checkmark del mensaje
        const cleanMsg = mensaje.replace(/^[✓✔] /, '');
        Notify[tipo === 'error' ? 'error' : tipo === 'warning' ? 'warning' : 'success'](cleanMsg);
        return;
    }
    const existing = document.querySelector('.toast-notification');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${tipo || 'info'}`;
    toast.innerHTML = mensaje;
    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add('show'), 50);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ── Cerrar modales con ESC ──
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        cerrarModalSalida();
        cerrarModalDetalle();
    }
});
