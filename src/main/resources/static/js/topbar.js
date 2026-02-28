document.addEventListener('DOMContentLoaded', function() {
    const avatarModal = document.getElementById('avatarModal');
    const openBtn = document.getElementById('openAvatarModal');
    const closeBtn = document.getElementById('closeModal');
    const uploadForm = document.getElementById('uploadForm');
    const fotoInput = document.getElementById('fotoInput');
    const previewImg = document.getElementById('previewImg');
    const previewPlaceholder = document.getElementById('previewPlaceholder');
    const btnGuardar = document.getElementById('btnGuardarFoto');
    const btnSeleccionar = document.getElementById('btnSeleccionarFoto');
    const nombreArchivo = document.getElementById('nombreArchivo');

    // Abrir modal
    openBtn.addEventListener('click', function() {
        avatarModal.classList.add('active');
    });

    // Cerrar modal
    function cerrarModal() {
        avatarModal.classList.remove('active');
        uploadForm.reset();
        previewImg.classList.remove('visible');
        previewPlaceholder.style.display = 'flex';
        btnSeleccionar.classList.remove('has-file');
        nombreArchivo.textContent = 'Seleccionar imagen';
    }

    closeBtn.addEventListener('click', cerrarModal);

    // Cerrar al hacer clic fuera
    avatarModal.addEventListener('click', function(e) {
        if (e.target === avatarModal) cerrarModal();
    });

    // Cerrar con Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && avatarModal.classList.contains('active')) cerrarModal();
    });

    // Botón custom abre el file input
    btnSeleccionar.addEventListener('click', function() {
        fotoInput.click();
    });

    // Vista previa de imagen
    fotoInput.addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                mostrarToastPerfil('El archivo es demasiado grande. Máximo 5 MB.', 'error');
                this.value = '';
                return;
            }

            // Mostrar nombre del archivo
            nombreArchivo.textContent = file.name;
            btnSeleccionar.classList.add('has-file');

            // Preview en el círculo
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                previewImg.classList.add('visible');
                previewPlaceholder.style.display = 'none';
            };
            reader.readAsDataURL(file);
        }
    });

    // Enviar formulario por AJAX
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = new FormData(uploadForm);
        btnGuardar.disabled = true;
        const textoOriginal = btnGuardar.innerHTML;
        btnGuardar.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px;vertical-align:-2px;animation:spin 1s linear infinite;"><circle cx="12" cy="12" r="10" stroke-dasharray="31.4" stroke-dashoffset="10"/></svg> Subiendo...';

        fetch(window.location.pathname.split('/').slice(0, 2).join('/') + '/foto-perfil', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Actualizar avatar en el topbar
                const avatarDiv = document.querySelector('.user-profile .avatar');
                const ctxPath = window.location.pathname.split('/').slice(0, 2).join('/');
                avatarDiv.innerHTML = '<img src="' + data.fotoUrl + '" alt="Foto de perfil" class="avatar-img" onerror="this.onerror=null; this.src=\'' + ctxPath + '/img/perfil.png\';">';
                mostrarToastPerfil('Foto actualizada correctamente', 'success');
            } else {
                mostrarToastPerfil(data.message || 'Error al subir la foto', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarToastPerfil('Error de conexión al subir la foto', 'error');
        })
        .finally(() => {
            btnGuardar.disabled = false;
            btnGuardar.innerHTML = textoOriginal;
            cerrarModal();
        });
    });

    // Toast
    function mostrarToastPerfil(mensaje, tipo) {
        const toast = document.createElement('div');
        toast.className = 'toast-perfil toast-' + tipo;
        toast.style.cssText = 'position:fixed;top:20px;right:20px;padding:12px 24px;border-radius:12px;color:#fff;font-size:0.85rem;z-index:10000;box-shadow:0 8px 24px rgba(0,0,0,0.15);font-family:Inter,sans-serif;font-weight:500;display:flex;align-items:center;gap:8px;animation:toastSlideIn 0.35s ease;';
        toast.style.background = tipo === 'success' ? 'linear-gradient(135deg, #10b981, #059669)' : 'linear-gradient(135deg, #ef4444, #dc2626)';

        const icon = tipo === 'success'
            ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>'
            : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';

        toast.innerHTML = icon + mensaje;
        document.body.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity 0.3s'; }, 2500);
        setTimeout(() => toast.remove(), 3000);
    }

    // ======================================================
    // SISTEMA DE NOTIFICACIONES
    // ======================================================
    const ctxPath = window.location.pathname.split('/').slice(0, 2).join('/');
    const notifBell = document.getElementById('notifBell');
    const notifDropdown = document.getElementById('notifDropdown');
    const notifBadge = document.getElementById('notifBadge');
    const notifList = document.getElementById('notifList');
    const marcarTodasBtn = document.getElementById('marcarTodasBtn');

    if (notifBell) {
        // Toggle dropdown al hacer clic en la campana
        document.getElementById('notifContainer').addEventListener('click', function(e) {
            e.stopPropagation();
            const isActive = notifDropdown.classList.contains('active');
            if (!isActive) {
                cargarNotificaciones();
            }
            notifDropdown.classList.toggle('active');
        });

        // Cerrar dropdown al hacer clic fuera
        document.addEventListener('click', function(e) {
            if (!document.getElementById('notifContainer').contains(e.target)) {
                notifDropdown.classList.remove('active');
            }
        });

        // Marcar todas como leídas
        marcarTodasBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            fetch(ctxPath + '/notificaciones?action=marcarTodas')
                .then(r => r.json())
                .then(() => {
                    cargarNotificaciones();
                    actualizarContador();
                });
        });

        // Cargar contador inicial
        actualizarContador();

        // Actualizar cada 60 segundos
        setInterval(actualizarContador, 60000);
    }

    function actualizarContador() {
        fetch(ctxPath + '/notificaciones?action=contar')
            .then(r => r.json())
            .then(data => {
                if (data.noLeidas > 0) {
                    notifBadge.textContent = data.noLeidas > 99 ? '99+' : data.noLeidas;
                    notifBadge.style.display = 'block';
                } else {
                    notifBadge.style.display = 'none';
                }
            })
            .catch(() => {});
    }

    function cargarNotificaciones() {
        fetch(ctxPath + '/notificaciones?action=listar')
            .then(r => r.json())
            .then(data => {
                if (data.success && data.notificaciones.length > 0) {
                    notifList.innerHTML = data.notificaciones.map(n => {
                        const tiempoAgo = tiempoRelativo(n.fechaCreacion);
                        const claseLeida = n.leida ? '' : 'no-leida';
                        return `
                            <div class="notif-item ${claseLeida}" onclick="marcarNotifLeida(${n.idNotificacion}, this)">
                                <div class="notif-icon" style="background: ${n.color || '#6366f1'}">
                                    <i class="fas ${n.icono || 'fa-bell'}"></i>
                                </div>
                                <div class="notif-text">
                                    <p class="notif-titulo">${n.titulo}</p>
                                    <p class="notif-mensaje">${n.mensaje || ''}</p>
                                    <span class="notif-tiempo">${tiempoAgo}</span>
                                </div>
                            </div>
                        `;
                    }).join('');
                } else {
                    notifList.innerHTML = `
                        <div class="notif-empty">
                            <i class="fas fa-bell-slash"></i>
                            <p>Sin notificaciones</p>
                        </div>
                    `;
                }
                // Actualizar badge
                if (data.noLeidas !== undefined) {
                    if (data.noLeidas > 0) {
                        notifBadge.textContent = data.noLeidas > 99 ? '99+' : data.noLeidas;
                        notifBadge.style.display = 'block';
                    } else {
                        notifBadge.style.display = 'none';
                    }
                }
            })
            .catch(err => console.error('Error cargando notificaciones:', err));
    }

    function tiempoRelativo(fechaStr) {
        if (!fechaStr) return '';
        const fecha = new Date(fechaStr.replace(' ', 'T'));
        const ahora = new Date();
        const diffMs = ahora - fecha;
        const diffMin = Math.floor(diffMs / 60000);
        const diffHoras = Math.floor(diffMin / 60);
        const diffDias = Math.floor(diffHoras / 24);

        if (diffMin < 1) return 'Justo ahora';
        if (diffMin < 60) return `Hace ${diffMin} min`;
        if (diffHoras < 24) return `Hace ${diffHoras}h`;
        if (diffDias === 1) return 'Ayer';
        if (diffDias < 7) return `Hace ${diffDias} días`;
        return fecha.toLocaleDateString('es-PE', { day: '2-digit', month: 'short' });
    }
});

// Función global para marcar como leída
function marcarNotifLeida(id, element) {
    const ctxPath = window.location.pathname.split('/').slice(0, 2).join('/');
    fetch(ctxPath + '/notificaciones?action=marcarLeida&id=' + id)
        .then(r => r.json())
        .then(() => {
            element.classList.remove('no-leida');
            // Actualizar contador
            const badge = document.getElementById('notifBadge');
            let count = parseInt(badge.textContent) || 0;
            if (count > 0) {
                count--;
                if (count > 0) {
                    badge.textContent = count;
                } else {
                    badge.style.display = 'none';
                }
            }
        });
}
