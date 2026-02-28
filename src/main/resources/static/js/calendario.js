// ======================================================
// CALENDARIO - Sistema de Voluntariado
// ======================================================

let calendar;

document.addEventListener("DOMContentLoaded", function () {
    const calendarEl = document.getElementById("calendar");

    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        locale: "es",
        selectable: true,
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        buttonText: {
            today: 'Hoy',
            month: 'Mes',
            week: 'Semana',
            day: 'Día'
        },
        height: 'auto',
        dayMaxEvents: 3,

        // Click en un día → abrir modal
        dateClick: function(info) {
            abrirModal(info.dateStr);
        },

        // Click en un evento → ver detalle
        eventClick: function(info) {
            mostrarToast('📌 ' + info.event.title, 'info');
        },

        // Cargar eventos del servidor
        events: contextPath + '/calendario?accion=listar'
    });

    calendar.render();

    // Selector de color
    document.querySelectorAll('.color-dot').forEach(dot => {
        dot.addEventListener('click', function() {
            document.querySelectorAll('.color-dot').forEach(d => d.classList.remove('active'));
            this.classList.add('active');
            document.getElementById('colorEvento').value = this.dataset.color;
        });
    });

    // Submit del formulario
    document.getElementById('formEvento').addEventListener('submit', function(e) {
        e.preventDefault();
        guardarEvento();
    });
});

// ── MODAL ───────────────────────────────────────────────

function abrirModal(fecha) {
    document.getElementById("modalEvento").style.display = "flex";
    document.getElementById("titulo").value = '';
    document.getElementById("descripcion").value = '';
    document.getElementById("fechaInicio").value = fecha;
    document.getElementById("fechaFin").value = fecha;
    document.getElementById("colorEvento").value = '#f97316';
    // Mostrar la fecha formateada
    const partes = fecha.split('-');
    const fechaObj = new Date(partes[0], partes[1] - 1, partes[2]);
    const opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    const fechaFormateada = fechaObj.toLocaleDateString('es-PE', opciones);
    document.getElementById("fechaTexto").textContent = '📅 ' + fechaFormateada.charAt(0).toUpperCase() + fechaFormateada.slice(1);
    // Reset color selector
    document.querySelectorAll('.color-dot').forEach(d => d.classList.remove('active'));
    document.querySelector('.color-dot[data-color="#f97316"]').classList.add('active');
    // Focus en título
    setTimeout(() => document.getElementById("titulo").focus(), 100);
}

function cerrarModal() {
    document.getElementById("modalEvento").style.display = "none";
}

// ── GUARDAR EVENTO ──────────────────────────────────────

function guardarEvento() {
    const titulo = document.getElementById("titulo").value.trim();
    if (!titulo) {
        mostrarToast('El título es obligatorio', 'error');
        return;
    }

    const data = {
        titulo: titulo,
        descripcion: document.getElementById("descripcion").value.trim(),
        fechaInicio: document.getElementById("fechaInicio").value,
        fechaFin: document.getElementById("fechaFin").value || document.getElementById("fechaInicio").value,
        color: document.getElementById("colorEvento").value
    };

    const btnSave = document.querySelector('.btn-save');
    btnSave.disabled = true;
    btnSave.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Guardando...';

    fetch(contextPath + "/calendario?accion=guardar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => res.text())
    .then(r => {
        if (r === "ok") {
            cerrarModal();
            mostrarToast('✅ Evento guardado correctamente', 'success');
            // Actualizar badge de notificaciones
            const badge = document.getElementById('notifBadge');
            if (badge) {
                let count = parseInt(badge.textContent) || 0;
                badge.textContent = count + 1;
                badge.style.display = 'block';
            }
            // Refrescar calendario sin recargar página
            calendar.refetchEvents();
        } else {
            mostrarToast('Error al guardar el evento', 'error');
        }
    })
    .catch(() => {
        mostrarToast('Error de conexión', 'error');
    })
    .finally(() => {
        btnSave.disabled = false;
        btnSave.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar evento';
    });
}

// ── TOAST ────────────────────────────────────────────────

function mostrarToast(mensaje, tipo) {
    const toast = document.getElementById('toastCalendario');
    toast.className = 'toast-calendario ' + tipo + ' show';
    const iconos = {
        success: '<i class="fa-solid fa-circle-check"></i>',
        error: '<i class="fa-solid fa-circle-xmark"></i>',
        info: '<i class="fa-solid fa-circle-info"></i>'
    };
    toast.innerHTML = (iconos[tipo] || '') + ' ' + mensaje;
    setTimeout(() => toast.classList.remove('show'), 3000);
}

