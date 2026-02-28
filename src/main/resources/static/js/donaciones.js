const modal = document.getElementById("modalDonacion");
const form = document.getElementById("formDonacion");
const tipoDonacionSelect = document.getElementById("tipoDonacion");
const subtipoDonacionSelect = document.getElementById("subtipoDonacion");
const seccionEspecie = document.getElementById("seccionEspecie");
const labelCantidad = document.getElementById("labelCantidad");
const btnGuardarDonacion = document.getElementById("btnGuardarDonacion");
const accionDonacionInput = document.getElementById("accionDonacion");
const idDonacionInput = document.getElementById("idDonacion");
const motivoEdicionInput = document.getElementById("motivoEdicion");
const idItemSelect = document.getElementById("idItem");
const donacionAnonimaCheck = document.getElementById("donacionAnonima");
const donanteFields = document.getElementById("donanteFields");
const dniDonanteInput = document.getElementById("dniDonante");
const buscarDonacionesInput = document.getElementById("buscarDonaciones");
const buscarDonanteInput = document.getElementById("buscarDonanteInput");
const buscarDonanteResultados = document.getElementById("buscarDonanteResultados");
const donanteSeleccionadoInfo = document.getElementById("donanteSeleccionadoInfo");
const donanteSeleccionadoTexto = document.getElementById("donanteSeleccionadoTexto");
const buscarDonanteSection = document.getElementById("buscarDonanteSection");
const PAGINA_TAMANO = 5;

/* ===================================================================
   SUBTIPOS DE DONACIÓN (opciones dinámicas según tipo)
=================================================================== */
const SUBTIPOS_DINERO = [
    'Efectivo',
    'Deposito bancario',
    'Transferencia',
    'Yape/Plin',
    'Cheque',
    'Otro'
];
let SUBTIPOS_ESPECIE = [];
let subtiposEspecieCargados = false;

async function cargarSubtiposEspecieDesdeCategorias() {
    if (subtiposEspecieCargados) return;
    try {
        const resp = await fetch("inventario?accion=listar_categorias");
        const cats = await resp.json();
        SUBTIPOS_ESPECIE = Array.from(
            new Set(
                (cats || [])
                    .map(c => (c && c.nombre ? String(c.nombre).trim() : ""))
                    .filter(Boolean)
            )
        ).sort((a, b) => a.localeCompare(b, 'es'));
    } catch (e) {
        console.error("Error cargando categorias para subtipo de donacion:", e);
        SUBTIPOS_ESPECIE = [];
    } finally {
        subtiposEspecieCargados = true;
    }
}

function actualizarSubtipos(valorSeleccionado) {
    if (!subtipoDonacionSelect) return;
    const tipo = tipoDonacionSelect ? tipoDonacionSelect.value : '';
    if (tipo === '2' && !subtiposEspecieCargados) {
        subtipoDonacionSelect.innerHTML = '<option value="">Cargando categorias...</option>';
        cargarSubtiposEspecieDesdeCategorias().then(() => actualizarSubtipos(valorSeleccionado));
        return;
    }
    let opciones = [];
    if (tipo === '1') opciones = SUBTIPOS_DINERO;
    else if (tipo === '2') opciones = SUBTIPOS_ESPECIE;

    subtipoDonacionSelect.innerHTML = '<option value="">Seleccione</option>';
    opciones.forEach(op => {
        const option = document.createElement('option');
        option.value = op;
        option.textContent = op;
        if (valorSeleccionado && op === valorSeleccionado) option.selected = true;
        subtipoDonacionSelect.appendChild(option);
    });
}

// --- Lookup (DNI / RUC) using your provided APIs ---
// NOTE: token is exposed client-side because you provided the URL/token. If you prefer the token secret
// I can add a server-side proxy endpoint that calls the external API and avoids CORS / token exposure.
const RUC_API_TEMPLATE = 'https://dniruc.apisperu.com/api/v1/ruc/{ident}?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImtmZW50eWdlbjAyNDVAZ21haWwuY29tIn0.JaGeW7g-XS7gcGqBFj7_mnNTgXwFg-THOxxgkLsfJaU';
const DNI_API_TEMPLATE = 'https://dniruc.apisperu.com/api/v1/dni/{ident}?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImtmZW50eWdlbjAyNDVAZ21haWwuY29tIn0.JaGeW7g-XS7gcGqBFj7_mnNTgXwFg-THOxxgkLsfJaU';

function debounce(fn, delay = 400) {
    let timer = null;
    return function(...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}

/* ===================================================================
   BUSCADOR DE DONANTES REGISTRADOS EN BD
=================================================================== */
let donanteSeleccionadoData = null;

async function buscarDonanteEnBD(termino) {
    if (!termino || termino.trim().length < 2) {
        if (buscarDonanteResultados) buscarDonanteResultados.style.display = 'none';
        return;
    }
    const t = termino.trim();
    try {
        const resp = await fetch(`donaciones?accion=buscarDonante&q=${encodeURIComponent(t)}`);
        const donantes = await resp.json();

        // Si no hay resultados en BD y el texto parece un DNI (8 digitos) o RUC (11 digitos), buscar en API
        const esDNI = /^\d{8}$/.test(t);
        const esRUC = /^\d{11}$/.test(t);
        if ((!donantes || donantes.length === 0) && (esDNI || esRUC)) {
            await buscarDonanteEnAPI(t, esDNI);
            return;
        }

        renderResultadosDonantes(donantes, t);
    } catch (err) {
        console.error('Error buscando donantes:', err);
        if (buscarDonanteResultados) buscarDonanteResultados.style.display = 'none';
    }
}

async function buscarDonanteEnAPI(ident, esDNI) {
    if (!buscarDonanteResultados) return;
    buscarDonanteResultados.innerHTML = '<div class="donante-search-no-results"><i class="fa-solid fa-spinner fa-spin"></i> No encontrado en registros, buscando en API...</div>';
    buscarDonanteResultados.style.display = 'block';
    try {
        const url = esDNI
            ? DNI_API_TEMPLATE.replace('{ident}', encodeURIComponent(ident))
            : RUC_API_TEMPLATE.replace('{ident}', encodeURIComponent(ident));
        const resp = await fetch(url);
        if (!resp.ok) throw new Error('No encontrado en API');
        const data = await resp.json();

        let nombre = '';
        if (esDNI) {
            nombre = data.nombre_completo || data.nombre_completo_inei || data.nombre || (data.nombres ? ([data.nombres, data.apellidoPaterno, data.apellidoMaterno].filter(Boolean).join(' ')) : '');
        } else {
            nombre = data.razon_social || data.nombre_o_razon_social || data.razonSocial || data.nombre || '';
        }

        if (nombre) {
            const donanteAPI = {
                idDonante: null,
                tipo: esDNI ? 'Persona' : 'Empresa',
                nombre: nombre,
                correo: '',
                telefono: '',
                dni: esDNI ? ident : '',
                ruc: esDNI ? '' : ident,
                desdeAPI: true
            };
            let html = `<div class="donante-search-item" style="background:#fffbeb;border-left:3px solid #f59e0b;" onclick='seleccionarDonante(${JSON.stringify(donanteAPI)})'>
                <div class="donante-icon" style="background:#fef3c7;color:#d97706;"><i class="fa-solid fa-globe"></i></div>
                <div class="donante-info">
                    <div class="donante-nombre">${escapeHtml(nombre)}</div>
                    <div class="donante-detalle">${esDNI ? 'DNI' : 'RUC'}: ${escapeHtml(ident)} · <span style="color:#d97706;">Encontrado en API (nuevo donante)</span></div>
                </div>
            </div>`;
            buscarDonanteResultados.innerHTML = html;
        } else {
            buscarDonanteResultados.innerHTML = '<div class="donante-search-no-results"><i class="fa-solid fa-user-slash"></i> No encontrado en registros ni en la API. Ingresa los datos manualmente.</div>';
        }
        buscarDonanteResultados.style.display = 'block';
    } catch (err) {
        console.error('Error buscando donante en API:', err);
        buscarDonanteResultados.innerHTML = '<div class="donante-search-no-results"><i class="fa-solid fa-user-slash"></i> No encontrado en registros ni en la API. Ingresa los datos manualmente.</div>';
        buscarDonanteResultados.style.display = 'block';
    }
}

function renderResultadosDonantes(donantes, termino) {
    if (!buscarDonanteResultados) return;
    if (!donantes || donantes.length === 0) {
        buscarDonanteResultados.innerHTML = '<div class="donante-search-no-results"><i class="fa-solid fa-user-slash"></i> No se encontro donante registrado. Ingresa los datos manualmente o usa el DNI/RUC.</div>';
        buscarDonanteResultados.style.display = 'block';
        return;
    }
    let html = '';
    donantes.forEach(d => {
        const icono = d.tipo === 'Empresa' || d.tipo === 'Grupo' ? 'fa-building' : 'fa-user';
        const ident = d.dni ? `DNI: ${d.dni}` : (d.ruc ? `RUC: ${d.ruc}` : '');
        const contacto = [d.correo, d.telefono].filter(Boolean).join(' | ');
        const detalle = [d.tipo, ident, contacto].filter(Boolean).join(' · ');
        html += `<div class="donante-search-item" onclick='seleccionarDonante(${JSON.stringify(d)})'>
            <div class="donante-icon"><i class="fa-solid ${icono}"></i></div>
            <div class="donante-info">
                <div class="donante-nombre">${escapeHtml(d.nombre)}</div>
                <div class="donante-detalle">${escapeHtml(detalle)}</div>
            </div>
        </div>`;
    });
    buscarDonanteResultados.innerHTML = html;
    buscarDonanteResultados.style.display = 'block';
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function seleccionarDonante(donante) {
    donanteSeleccionadoData = donante;
    // Rellenar campos del formulario
    const tipoMap = { 'Persona': 'PERSONA', 'Empresa': 'EMPRESA', 'Grupo': 'GRUPO' };
    const tipoDonante = document.getElementById('tipoDonante');
    if (tipoDonante) tipoDonante.value = tipoMap[donante.tipo] || 'PERSONA';

    const identEl = document.getElementById('dniDonante');
    const tipoVal = (tipoMap[donante.tipo] || 'PERSONA');
    if (identEl) {
        identEl.value = (tipoVal === 'PERSONA') ? (donante.dni || '') : (donante.ruc || '');
    }

    const nombreEl = document.getElementById('nombreDonante');
    if (nombreEl) nombreEl.value = donante.nombre || '';

    const correoEl = document.getElementById('correoDonante');
    if (correoEl) correoEl.value = donante.correo || '';

    const telefonoEl = document.getElementById('telefonoDonante');
    if (telefonoEl) telefonoEl.value = donante.telefono || '';

    // Ocultar resultados y mostrar chip de seleccion
    if (buscarDonanteResultados) buscarDonanteResultados.style.display = 'none';
    if (buscarDonanteInput) buscarDonanteInput.value = '';
    mostrarDonanteSeleccionado(donante);

    actualizarDonante();
}

function mostrarDonanteSeleccionado(donante) {
    if (donanteSeleccionadoInfo && donanteSeleccionadoTexto) {
        const ident = donante.dni ? `DNI: ${donante.dni}` : (donante.ruc ? `RUC: ${donante.ruc}` : '');
        donanteSeleccionadoTexto.innerHTML = `<i class="fa-solid fa-circle-check"></i> ${escapeHtml(donante.nombre)} ${ident ? '(' + escapeHtml(ident) + ')' : ''} <span style="color:#6b7280;font-weight:400;">— donante registrado</span>`;
        donanteSeleccionadoInfo.style.display = 'flex';
    }
}

function limpiarDonanteSeleccionado() {
    donanteSeleccionadoData = null;
    if (donanteSeleccionadoInfo) donanteSeleccionadoInfo.style.display = 'none';
    // Limpiar campos del donante
    ['dniDonante', 'nombreDonante', 'correoDonante', 'telefonoDonante'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    const tipoDonante = document.getElementById('tipoDonante');
    if (tipoDonante) tipoDonante.value = 'PERSONA';
    actualizarDonante();
}

const debouncedBuscarDonante = debounce(function() {
    if (buscarDonanteInput) {
        buscarDonanteEnBD(buscarDonanteInput.value);
    }
}, 350);

if (buscarDonanteInput) {
    buscarDonanteInput.addEventListener('input', debouncedBuscarDonante);
    // Cerrar dropdown si se hace click fuera
    document.addEventListener('click', function(e) {
        if (buscarDonanteResultados && !buscarDonanteInput.contains(e.target) && !buscarDonanteResultados.contains(e.target)) {
            buscarDonanteResultados.style.display = 'none';
        }
    });
}

async function fetchDonanteByIdent() {
    if (!dniDonanteInput) return;
    const tipo = (tipoDonanteSelect?.value || 'PERSONA').toUpperCase();
    const ident = (dniDonanteInput.value || '').trim();
    const statusEl = document.getElementById('identLookupStatus');
    if (!ident) {
        if (statusEl) statusEl.textContent = '';
        return;
    }
    if (tipo === 'PERSONA' && ident.length !== 8) {
        if (statusEl) statusEl.textContent = '';
        return;
    }
    if ((tipo === 'EMPRESA' || tipo === 'GRUPO') && ident.length !== 11) {
        if (statusEl) statusEl.textContent = '';
        return;
    }

    if (statusEl) statusEl.textContent = 'Buscando en registros...';

    // PASO 1: Buscar primero en la base de datos local
    try {
        const respBD = await fetch(`donaciones?accion=buscarDonante&q=${encodeURIComponent(ident)}`);
        const donantes = await respBD.json();
        // Buscar una coincidencia exacta por DNI, RUC o nombre
        const match = (donantes || []).find(d => {
            if (tipo === 'PERSONA' && d.dni && d.dni === ident) return true;
            if ((tipo === 'EMPRESA' || tipo === 'GRUPO') && d.ruc && d.ruc === ident) return true;
            // Tambien aceptar coincidencia por telefono como fallback
            if (d.telefono && d.telefono === ident) return true;
            return false;
        });
        if (match) {
            // Donante encontrado en BD - autocompletar
            const nombreInput = document.getElementById('nombreDonante');
            if (nombreInput) nombreInput.value = match.nombre || '';
            const correoEl = document.getElementById('correoDonante');
            if (correoEl && match.correo) correoEl.value = match.correo;
            const telefonoEl = document.getElementById('telefonoDonante');
            if (telefonoEl && match.telefono) telefonoEl.value = match.telefono;

            const tipoMap = { 'Persona': 'PERSONA', 'Empresa': 'EMPRESA', 'Grupo': 'GRUPO' };
            if (tipoDonanteSelect && match.tipo) tipoDonanteSelect.value = tipoMap[match.tipo] || tipo;

            donanteSeleccionadoData = match;
            mostrarDonanteSeleccionado(match);
            actualizarDonante();
            if (statusEl) statusEl.innerHTML = '<span style="color:#10b981;"><i class="fa-solid fa-database"></i> Donante encontrado en el sistema</span>';
            setTimeout(() => { if (statusEl) statusEl.textContent = ''; }, 4000);
            return;
        }
    } catch (err) {
        console.error('Error buscando donante en BD:', err);
    }

    // PASO 2: Si no encontrado en BD, consultar API externa
    if (statusEl) statusEl.textContent = 'No encontrado en registros, buscando en API...';
    try {
        const url = tipo === 'PERSONA'
            ? DNI_API_TEMPLATE.replace('{ident}', encodeURIComponent(ident))
            : RUC_API_TEMPLATE.replace('{ident}', encodeURIComponent(ident));
        const resp = await fetch(url);
        if (!resp.ok) throw new Error('No encontrado');
        const data = await resp.json();

        let nombre = '';
        if (tipo === 'PERSONA') {
            nombre = data.nombre_completo || data.nombre_completo_inei || data.nombre || (data.nombres ? ([data.nombres, data.apellidoPaterno, data.apellidoMaterno].filter(Boolean).join(' ')) : '');
        } else {
            nombre = data.razon_social || data.nombre_o_razon_social || data.razonSocial || data.nombre || '';
        }

        if (nombre) {
            const nombreInput = document.getElementById('nombreDonante');
            if (nombreInput) nombreInput.value = nombre;
            if (tipo === 'PERSONA') {
                if (tipoDonanteSelect) tipoDonanteSelect.value = 'PERSONA';
            } else {
                if (tipoDonanteSelect) tipoDonanteSelect.value = 'EMPRESA';
            }
            actualizarDonante();
            if (statusEl) statusEl.innerHTML = '<span style="color:#667eea;"><i class="fa-solid fa-globe"></i> Datos obtenidos de API externa</span>';
        } else {
            if (statusEl) statusEl.textContent = 'No se encontro nombre en la respuesta';
        }
    } catch (err) {
        console.error('Lookup identificador:', err);
        if (statusEl) statusEl.textContent = 'No encontrado en registros ni en API';
    }
    setTimeout(() => {
        const statusEl2 = document.getElementById('identLookupStatus');
        if (statusEl2) statusEl2.textContent = '';
    }, 4000);
}

const debouncedFetchIdent = debounce(fetchDonanteByIdent, 450);

if (dniDonanteInput) {
    dniDonanteInput.addEventListener('input', function () {
        const tipo = (tipoDonanteSelect?.value || 'PERSONA').toUpperCase();
        const v = (this.value || '').trim();
        if ((tipo === 'PERSONA' && v.length === 8) || ((tipo === 'EMPRESA' || tipo === 'GRUPO') && v.length === 11)) {
            debouncedFetchIdent();
        } else {
            const st = document.getElementById('identLookupStatus');
            if (st) st.textContent = '';
        }
    });
    dniDonanteInput.addEventListener('blur', function () {
        fetchDonanteByIdent();
    });
}

// Escuchar cambios para actualizar campos del donante
const tipoDonanteSelect = document.getElementById("tipoDonante");
if (tipoDonanteSelect) tipoDonanteSelect.addEventListener("change", actualizarDonante);
if (donacionAnonimaCheck) donacionAnonimaCheck.addEventListener("change", actualizarDonante);

// Validación antes de enviar (regla: Empresa/Grupo -> RUC + (correo|telefono); Persona -> nombre + (correo|telefono))
if (form) {
    form.addEventListener("submit", function (e) {
        const anonima = donacionAnonimaCheck && donacionAnonimaCheck.checked;
        if (anonima) return; // si es anónima no validamos campos del donante aquí

        const tipoVal = (tipoDonanteSelect ? (tipoDonanteSelect.value || "PERSONA") : "PERSONA").toUpperCase();
        const identVal = (document.getElementById("dniDonante")?.value || "").trim();
        const nombre = (document.getElementById("nombreDonante")?.value || "").trim();
        const correo = (document.getElementById("correoDonante")?.value || "").trim();
        const telefono = (document.getElementById("telefonoDonante")?.value || "").trim();

        if (tipoVal === "PERSONA") {
            if (!identVal) {
                Notify.warning("Para persona: ingresa el DNI del donante.");
                e.preventDefault();
                return false;
            }
            if (!nombre) {
                Notify.warning("Para persona: ingresa el nombre del donante.");
                e.preventDefault();
                return false;
            }
            if (!correo && !telefono) {
                Notify.warning("Para persona: ingresa al menos correo o teléfono del donante.");
                e.preventDefault();
                return false;
            }
        } else {
            // Empresa / Grupo -> identVal contiene RUC en este modo
            if (!identVal) {
                Notify.warning("Para empresa/grupo es obligatorio el RUC.");
                e.preventDefault();
                return false;
            }
            if (!nombre) {
                Notify.warning("Para empresa/grupo: ingresa la razón social del donante.");
                e.preventDefault();
                return false;
            }
            if (!correo && !telefono) {
                Notify.warning("Para empresa/grupo: ingresa al menos correo o teléfono del donante.");
                e.preventDefault();
                return false;
            }
        }

        // Normalizar campos (mantener lógica existente)
        normalizarCamposNuevoItemAntesDeEnviar();
        return true;
    });
}
let paginaActual = 1;
let totalPaginas = 1;
let donacionOriginal = null;
let filasBaseDonaciones = [];
let filasFiltradasDonaciones = [];

function abrirModal() {
    modal.style.display = "flex";
    form.reset();
    donacionOriginal = null;
    donanteSeleccionadoData = null;
    if (accionDonacionInput) accionDonacionInput.value = "registrar";
    if (idDonacionInput) idDonacionInput.value = "";
    if (motivoEdicionInput) motivoEdicionInput.value = "";
    document.getElementById("tituloModal").innerText = "Registrar Donacion";
    if (btnGuardarDonacion) btnGuardarDonacion.textContent = "Registrar donacion";
    // Reset buscador donante
    if (buscarDonanteInput) buscarDonanteInput.value = '';
    if (buscarDonanteResultados) buscarDonanteResultados.style.display = 'none';
    if (donanteSeleccionadoInfo) donanteSeleccionadoInfo.style.display = 'none';
    if (subtipoDonacionSelect) subtipoDonacionSelect.innerHTML = '<option value="">Seleccione primero el tipo</option>';
    desbloquearCamposEdicion();
    const cargas = Promise.all([cargarActividades(), cargarItemsInventario(), cargarSubtiposEspecieDesdeCategorias()]);
    actualizarCamposInventario();
    return cargas;
}

function cerrarModal() {
    modal.style.display = "none";
}

async function cargarActividades() {
    try {
        const resp = await fetch("donaciones?accion=actividades");
        const actividades = await resp.json();
        const select = document.getElementById("actividad");
        select.innerHTML = '<option value="">Seleccione actividad</option>';
        actividades.forEach((act) => {
            const option = document.createElement("option");
            option.value = act.idActividad;
            option.textContent = act.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar actividades:", error);
    }
}

async function cargarItemsInventario() {
    if (!idItemSelect) {
        return;
    }
    try {
        const resp = await fetch("inventario?accion=listar");
        const items = await resp.json();
        // Filtrar solo items ACTIVO
        const activos = (items || []).filter(i => i.estado === 'ACTIVO');
        idItemSelect.innerHTML = '<option value="">Seleccione item del inventario</option>';

        // Agrupar por categoría
        const grupos = {};
        activos.forEach(item => {
            const cat = item.categoria || 'Sin categoría';
            if (!grupos[cat]) grupos[cat] = [];
            grupos[cat].push(item);
        });

        // Crear optgroups
        Object.keys(grupos).sort().forEach(cat => {
            const optgroup = document.createElement("optgroup");
            optgroup.label = cat;
            grupos[cat].forEach(item => {
                const option = document.createElement("option");
                option.value = item.idItem;
                option.textContent = item.nombre + " (" + item.unidadMedida + " - stock: " + item.stockActual + ")";
                optgroup.appendChild(option);
            });
            idItemSelect.appendChild(optgroup);
        });

        // Si no hay items activos, deshabilitar la opción "En especie" y mostrar aviso
        const tipoEspecieOpt = document.querySelector('#tipoDonacion option[value="2"]');
        const noItemsNotice = document.getElementById('noItemsNotice');
        if (activos.length === 0) {
            if (tipoEspecieOpt) {
                tipoEspecieOpt.disabled = true;
                tipoEspecieOpt.title = 'No hay items activos en el inventario. Crea un item desde Inventario.';
            }
            if (noItemsNotice) noItemsNotice.style.display = 'block';
        } else {
            if (tipoEspecieOpt) {
                tipoEspecieOpt.disabled = false;
                tipoEspecieOpt.title = '';
            }
            if (noItemsNotice) noItemsNotice.style.display = 'none';
        }
    } catch (error) {
        console.error("Error al cargar items de inventario:", error);
    }
}

function onTipoDonacionChange() {
    actualizarSubtipos();
    actualizarCamposInventario();
}

function actualizarCamposInventario() {
    const esObjeto = tipoDonacionSelect && tipoDonacionSelect.value === "2";
    if (seccionEspecie) {
        seccionEspecie.style.display = esObjeto ? "block" : "none";
    }
    if (labelCantidad) {
        labelCantidad.textContent = esObjeto ? "Cantidad a ingresar *" : "Monto (S/) *";
    }
    if (btnGuardarDonacion) {
        btnGuardarDonacion.textContent = esObjeto
            ? "Registrar y actualizar inventario"
            : "Registrar donacion";
    }
    actualizarModoItem();
    actualizarDonante();
}

function actualizarModoItem() {
    const esObjeto = tipoDonacionSelect && tipoDonacionSelect.value === "2";
    if (idItemSelect) {
        idItemSelect.required = esObjeto;
        idItemSelect.disabled = false;
        if (!esObjeto) idItemSelect.value = "";
    }
}

function normalizarCamposNuevoItemAntesDeEnviar() {
    // Ya no se crean items desde donaciones — se usan los existentes del inventario
}

async function editarDonacion(idDonacion) {
    try {
        const resp = await fetch(`donaciones?accion=obtener&id=${idDonacion}`);
        const data = await resp.json();
        if (!data || data.ok === false) {
            Notify.error(data?.message || "No se pudo cargar la donación");
            return;
        }

        donacionOriginal = data;
        await abrirModal();
        document.getElementById("tituloModal").innerText = "Editar Donacion";
        if (accionDonacionInput) accionDonacionInput.value = "editar";
        if (idDonacionInput) idDonacionInput.value = data.idDonacion || "";

        document.getElementById("tipoDonacion").value = String(data.idTipoDonacion || "");
        actualizarSubtipos(data.subtipoDonacion || '');
        document.getElementById("cantidad").value = (data.cantidadItem ?? data.cantidad ?? "");
        document.getElementById("descripcion").value = data.descripcion || "";
        document.getElementById("actividad").value = String(data.idActividad || "");
        if (donacionAnonimaCheck) {
            donacionAnonimaCheck.checked = data.donacionAnonima === true;
        }
        document.getElementById("tipoDonante").value = (data.tipoDonante || "PERSONA").toUpperCase();
        const identEl = document.getElementById("dniDonante");
        const tipoDon = (data.tipoDonante || "PERSONA").toUpperCase();
        if (identEl) {
            identEl.value = (tipoDon === 'PERSONA') ? (data.dniDonante || '') : (data.rucDonante || '');
            // name will be ajusted by actualizarDonante()
        }
        document.getElementById("nombreDonante").value = data.nombreDonante || "";
        document.getElementById("correoDonante").value = data.correoDonante || "";
        document.getElementById("telefonoDonante").value = data.telefonoDonante || "";

        if (data.idItem) {
            idItemSelect.value = String(data.idItem);
        }

        actualizarCamposInventario();
        bloquearCamposSegunTipo(data.idTipoDonacion, data.estado);
        if (btnGuardarDonacion) btnGuardarDonacion.textContent = "Guardar cambios";
    } catch (error) {
        console.error("Error cargando donacion para editar:", error);
        Notify.error("No se pudo abrir la donación para editar");
    }
}

function actualizarDonante() {
    const anonima = donacionAnonimaCheck && donacionAnonimaCheck.checked;
    if (donanteFields) {
        donanteFields.style.display = anonima ? "none" : "grid";
    }
    // Ocultar/mostrar buscador de donante
    if (buscarDonanteSection) {
        buscarDonanteSection.style.display = anonima ? "none" : "grid";
    }

    const nombreDonante = document.getElementById("nombreDonante");
    const tipoDonante = document.getElementById("tipoDonante");
    const correo = document.getElementById("correoDonante");
    const telefono = document.getElementById("telefonoDonante");
    const identEl = document.getElementById("dniDonante");
    const labelIdent = document.getElementById("labelIdent");

    // Nombre / Razón social: obligatorio para Persona y Empresa/Grupo cuando no es anónima
    if (nombreDonante) {
        const tipoActual = (tipoDonante ? (tipoDonante.value || "PERSONA") : "PERSONA").toUpperCase();
        const requiereNombre = !anonima && (tipoActual === "PERSONA" || tipoActual === "EMPRESA" || tipoActual === "GRUPO");
        nombreDonante.required = requiereNombre;
        if (anonima) {
            nombreDonante.value = "";
        }
        // actualizar label y placeholder según tipo
        const labelNombre = document.getElementById("labelNombreDonante");
        if (labelNombre) {
            if (tipoActual === "PERSONA") {
                labelNombre.textContent = "Nombre del donante *";
                nombreDonante.placeholder = "Nombres y apellidos";
            } else {
                labelNombre.textContent = "Razón social del donante *";
                nombreDonante.placeholder = "Razón social";
            }
        }
    }

    if (tipoDonante) {
        tipoDonante.required = !anonima;
    }

    // Campo identificador (DNI <-> RUC) — usamos el mismo input `dniDonante` y cambiamos label/name/placeholder
    const tipoVal = (tipoDonante ? (tipoDonante.value || "PERSONA") : "PERSONA").toUpperCase();
    if (identEl && labelIdent) {
        if (anonima) {
            // ocultar
            identEl.style.display = "none";
            identEl.required = false;
            identEl.value = "";
            labelIdent.style.display = "none";
            identEl.name = "";
        } else if (tipoVal === "PERSONA") {
            labelIdent.textContent = "DNI";
            labelIdent.style.display = "block";
            identEl.style.display = "block";
            identEl.placeholder = "Requerido para Persona";
            identEl.required = true;
            identEl.name = "dniDonante";
        } else {
            labelIdent.textContent = "RUC";
            labelIdent.style.display = "block";
            identEl.style.display = "block";
            identEl.placeholder = "Requerido para Empresa/Grupo";
            identEl.required = true;
            identEl.name = "rucDonante";
        }

        // Si ya hay un identificador pero el nombre está vacío, intentar autocompletar (DNI->persona, RUC->empresa)
        const nombreInput = document.getElementById("nombreDonante");
        if (identEl && nombreInput && !nombreInput.value) {
            const valIdent = (identEl.value || "").trim();
            if ((tipoVal === "PERSONA" && valIdent.length === 8) || ((tipoVal === "EMPRESA" || tipoVal === "GRUPO") && valIdent.length === 11)) {
                debouncedFetchIdent();
            }
        }
    }

    // Contactos: no forzamos ambos, validación en submit exige al menos uno (correo o teléfono)
    if (correo) {
        if (anonima) correo.value = "";
    }
    if (telefono) {
        if (anonima) telefono.value = "";
    }
}

async function anularDonacion(idDonacion) {
    const motivo = prompt("Motivo de anulación (opcional):", "Anulación manual");
    if (motivo === null) {
        return;
    }

    const ok = await Notify.confirm("¿Anular esta donación?", "Se anulará la donación y, si fue en especie, se revertirá su stock.", { variant: 'danger', okText: 'Sí, anular' });
    if (!ok) {
        return;
    }

    const params = new URLSearchParams();
    params.append("accion", "anular");
    params.append("idDonacion", idDonacion);
    params.append("motivo", motivo || "Anulacion manual");

    try {
        const resp = await fetch("donaciones", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        if (result.ok) {
            Notify.success("Donación anulada correctamente");
            setTimeout(() => window.location.reload(), 1200);
        } else {
            Notify.error(result.message || "No se pudo anular la donación");
        }
    } catch (error) {
        console.error("Error anulando donacion:", error);
        Notify.error("Error al anular la donación");
    }
}

// Cambiar estado (ej. Aprobar -> ACTIVO)
async function cambiarEstadoDonacion(idDonacion, nuevoEstado) {
    const ok = await Notify.confirm(`¿Cambiar estado de la donación #${idDonacion}?`, `El nuevo estado será: ${nuevoEstado}`, { variant: 'info', okText: 'Sí, cambiar' });
    if (!ok) return;
    try {
        const params = new URLSearchParams();
        params.append('accion', 'cambiar_estado');
        params.append('idDonacion', idDonacion);
        params.append('estado', nuevoEstado);

        const resp = await fetch('donaciones', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });
        const text = await resp.text();
        let obj = {};
        try { obj = JSON.parse(text); } catch(e) { obj = { ok: false, message: text }; }
        if (obj.ok) {
            Notify.success(obj.message || 'Estado actualizado');
            setTimeout(() => location.reload(), 1200);
        } else {
            Notify.error(obj.message || 'No se pudo actualizar el estado');
        }
    } catch (err) {
        console.error(err);
        Notify.error('Error al cambiar el estado de la donación');
    }
}

function bloquearCamposSegunTipo(idTipo, estado) {
    const esDinero = Number(idTipo) === 1;
    const estadoNorm = String(estado || "").toUpperCase();
    const bloquearCantidadEspecie = !esDinero && estadoNorm !== "PENDIENTE";
    if (bloquearCantidadEspecie) {
        const cantidad = document.getElementById("cantidad");
        if (cantidad) {
            cantidad.readOnly = true;
            cantidad.title = "Para donaciones en especie confirmadas/anuladas, la cantidad no se edita desde este flujo.";
        }
    } else {
        const cantidad = document.getElementById("cantidad");
        if (cantidad) {
            cantidad.readOnly = false;
            cantidad.title = "";
        }
    }
    if (!esDinero) {
        if (tipoDonacionSelect) {
            tipoDonacionSelect.dataset.locked = "1";
            tipoDonacionSelect.dataset.original = String(idTipo);
        }
    }
}

function desbloquearCamposEdicion() {
    const cantidad = document.getElementById("cantidad");
    if (cantidad) {
        cantidad.readOnly = false;
        cantidad.title = "";
    }
    if (tipoDonacionSelect) {
        tipoDonacionSelect.dataset.locked = "0";
        tipoDonacionSelect.dataset.original = "";
    }
}

function inicializarPaginacionDonaciones() {
    const tbody = document.getElementById("tbodyDonaciones");
    if (!tbody) {
        return;
    }
    filasBaseDonaciones = Array.from(tbody.querySelectorAll("tr")).filter((tr) => !tr.querySelector(".no-data"));
    filasFiltradasDonaciones = [...filasBaseDonaciones];
    paginaActual = 1;
    renderPaginacionDonaciones();
}

function renderPaginacionDonaciones() {
    const tbody = document.getElementById("tbodyDonaciones");
    const contenedor = document.getElementById("donacionesPaginacion");
    const btnAnterior = document.getElementById("btnPaginaAnterior");
    const btnSiguiente = document.getElementById("btnPaginaSiguiente");
    const texto = document.getElementById("textoPaginacionDonaciones");
    if (!tbody || !contenedor || !btnAnterior || !btnSiguiente || !texto) return;

    const filaNoResultadosExistente = document.getElementById("filaNoResultadosDonaciones");
    if (filaNoResultadosExistente) {
        filaNoResultadosExistente.remove();
    }

    if (filasFiltradasDonaciones.length === 0) {
        filasBaseDonaciones.forEach((fila) => {
            fila.style.display = "none";
        });
        contenedor.style.display = "none";
        tbody.insertAdjacentHTML("beforeend", '<tr id="filaNoResultadosDonaciones"><td colspan="10" class="no-data">No hay donaciones que coincidan con la busqueda</td></tr>');
        return;
    }

    totalPaginas = Math.max(1, Math.ceil(filasFiltradasDonaciones.length / PAGINA_TAMANO));
    if (paginaActual > totalPaginas) {
        paginaActual = totalPaginas;
    }

    const inicio = (paginaActual - 1) * PAGINA_TAMANO;
    const fin = inicio + PAGINA_TAMANO;
    filasBaseDonaciones.forEach((fila) => {
        fila.style.display = "none";
    });
    filasFiltradasDonaciones.forEach((fila, idx) => {
        fila.style.display = idx >= inicio && idx < fin ? "" : "none";
    });

    texto.textContent = `Pagina ${paginaActual} de ${totalPaginas}`;
    contenedor.style.display = totalPaginas > 1 ? "flex" : "none";
    btnAnterior.disabled = paginaActual === 1;
    btnSiguiente.disabled = paginaActual === totalPaginas;
}

function aplicarFiltroDonaciones() {
    const q = (buscarDonacionesInput?.value || "").trim().toLowerCase();
    filasFiltradasDonaciones = filasBaseDonaciones.filter((fila) => {
        if (!q) return true;
        return (fila.textContent || "").toLowerCase().includes(q);
    });
    paginaActual = 1;
    renderPaginacionDonaciones();
}

function configurarEventosPaginacionDonaciones() {
    const btnAnterior = document.getElementById("btnPaginaAnterior");
    const btnSiguiente = document.getElementById("btnPaginaSiguiente");
    if (!btnAnterior || !btnSiguiente) return;

    btnAnterior.addEventListener("click", () => {
        if (paginaActual > 1) {
            paginaActual -= 1;
            renderPaginacionDonaciones();
        }
    });
    btnSiguiente.addEventListener("click", () => {
        if (paginaActual < totalPaginas) {
            paginaActual += 1;
            renderPaginacionDonaciones();
        }
    });
}

if (tipoDonacionSelect) {
    tipoDonacionSelect.addEventListener("change", () => {
        if (tipoDonacionSelect.dataset.locked === "1") {
            tipoDonacionSelect.value = tipoDonacionSelect.dataset.original || "";
            Notify.warning("No se puede cambiar el tipo de donación en modo edición.");
            return;
        }
        actualizarCamposInventario();
    });
}
if (form) {
    form.addEventListener("submit", (e) => {
        // Normalizar (si hubiera campos de nuevo item - mantenemos por compatibilidad)
        normalizarCamposNuevoItemAntesDeEnviar();

        // Validación cliente: donación en especie requiere seleccionar una categoria
        if (tipoDonacionSelect && tipoDonacionSelect.value === "2") {
            if (!idItemSelect || !idItemSelect.value) {
                e.preventDefault();
                Notify.warning('Para donaciones en especie debes seleccionar un ítem del inventario.');
                return;
            }
        }

        // En edición pedir motivo (comportamiento existente)
        if (accionDonacionInput && accionDonacionInput.value === "editar" && motivoEdicionInput) {
            const motivo = prompt("Motivo de edicion (opcional):", "Actualizacion de donacion");
            if (motivo !== null) {
                motivoEdicionInput.value = (motivo || "Actualizacion de donacion").trim();
            } else {
                motivoEdicionInput.value = "Actualizacion de donacion";
            }
        }
    });
}
if (donacionAnonimaCheck) {
    donacionAnonimaCheck.addEventListener("change", actualizarDonante);
}

document.addEventListener("DOMContentLoaded", () => {
    cargarSubtiposEspecieDesdeCategorias();
    actualizarCamposInventario();
    actualizarDonante();
    configurarEventosPaginacionDonaciones();
    inicializarPaginacionDonaciones();
    if (buscarDonacionesInput) {
        buscarDonacionesInput.addEventListener("input", aplicarFiltroDonaciones);
    }
    // Configurar evento "Otro" en unidad del modal rápido
    const qiUnidad = document.getElementById("qi_unidad");
    if (qiUnidad) {
        qiUnidad.addEventListener("change", () => {
            const otro = document.getElementById("qi_unidadOtro");
            if (otro) {
                const mostrar = qiUnidad.value === "__OTRO__";
                otro.style.display = mostrar ? "block" : "none";
                otro.required = mostrar;
                if (!mostrar) otro.value = "";
            }
        });
    }
});

/* ===================================================================
   CREACIÓN RÁPIDA DE ITEM DESDE DONACIONES
=================================================================== */

async function cargarCategoriasParaQuickItem() {
    const sel = document.getElementById("qi_categoria");
    if (!sel) return;
    try {
        const resp = await fetch("inventario?accion=listar_categorias");
        const cats = await resp.json();
        sel.innerHTML = '<option value="">Seleccione</option>';
        (cats || []).forEach(c => {
            const opt = document.createElement("option");
            opt.value = c.nombre;
            opt.textContent = c.nombre;
            sel.appendChild(opt);
        });
    } catch (e) {
        console.error("Error cargando categorías para quick item:", e);
    }
}

function abrirModalNuevoItemRapido() {
    const modal = document.getElementById("modalNuevoItemRapido");
    if (!modal) return;
    const form = document.getElementById("formNuevoItemRapido");
    if (form) form.reset();
    const otro = document.getElementById("qi_unidadOtro");
    if (otro) { otro.style.display = "none"; otro.required = false; }
    cargarCategoriasParaQuickItem();
    modal.style.display = "flex";
}

function cerrarModalNuevoItemRapido() {
    const modal = document.getElementById("modalNuevoItemRapido");
    if (modal) modal.style.display = "none";
}

async function guardarItemRapido(event) {
    event.preventDefault();
    const btn = document.getElementById("btnGuardarItemRapido");
    if (btn) { btn.disabled = true; btn.textContent = "Creando..."; }

    const unidadSel = document.getElementById("qi_unidad").value;
    let unidad = unidadSel;
    if (unidadSel === "__OTRO__") {
        unidad = (document.getElementById("qi_unidadOtro").value || "").trim();
        if (!unidad) { Notify.warning("Especifique la unidad de medida"); if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-check"></i> Crear y seleccionar'; } return; }
    }

    const params = new URLSearchParams();
    params.append("accion", "registrar");
    params.append("nombre", document.getElementById("qi_nombre").value.trim());
    params.append("categoria", document.getElementById("qi_categoria").value);
    params.append("unidadMedida", unidad);
    params.append("stockMinimo", document.getElementById("qi_stockMinimo").value);
    params.append("observacion", (document.getElementById("qi_observacion").value || "").trim());

    try {
        const resp = await fetch("inventario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        });
        const result = await resp.json();
        const ok = result && (result.ok === true || result.success === true);
        if (ok) {
            cerrarModalNuevoItemRapido();
            // Recargar items y seleccionar el nuevo
            await cargarItemsInventario();
            if (result.idItem && idItemSelect) {
                idItemSelect.value = String(result.idItem);
            }
            // Habilitar la opcion especie si estaba deshabilitada
            const tipoEspecieOpt = document.querySelector('#tipoDonacion option[value="2"]');
            if (tipoEspecieOpt) tipoEspecieOpt.disabled = false;
            const noItemsNotice = document.getElementById('noItemsNotice');
            if (noItemsNotice) noItemsNotice.style.display = 'none';
        } else {
            Notify.error(result.message || "No se pudo crear el ítem");
        }
    } catch (e) {
        console.error("Error creando item rápido:", e);
        Notify.error("Error al crear el ítem");
    } finally {
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-check"></i> Crear y seleccionar'; }
    }
}
