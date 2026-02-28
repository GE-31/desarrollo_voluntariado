/**
 * session-manager.js
 * 
 * Manejo de sesión del lado del cliente.
 * - Detecta actividad del usuario (clic, mouse, teclado, scroll, navegación).
 * - Renueva la sesión JWT cada vez que hay actividad (debounced a 1 min).
 * - Muestra advertencia cuando quedan 2 minutos de inactividad.
 * - Redirige a login cuando la sesión expira por inactividad.
 */
(function () {
    'use strict';

    const SESSION_TIMEOUT_MS = 20 * 60 * 1000;     // 20 minutos
    const WARNING_BEFORE_MS = 2 * 60 * 1000;        // Advertir 2 min antes
    const RENEW_DEBOUNCE_MS = 60 * 1000;             // Renovar máximo cada 1 minuto
    const CHECK_INTERVAL_MS = 30 * 1000;             // Verificar cada 30 segundos

    let lastActivity = Date.now();
    let lastRenew = 0;
    let warningShown = false;
    let sessionExpired = false;
    let warningModal = null;

    /**
     * Obtiene el contextPath del sistema.
     */
    function getContextPath() {
        const path = window.location.pathname;
        const parts = path.split('/');
        if (parts.length > 1 && parts[1]) {
            return '/' + parts[1];
        }
        return '';
    }

    /**
     * Registra actividad del usuario y renueva la sesión si corresponde.
     */
    function registrarActividad() {
        if (sessionExpired) return;

        lastActivity = Date.now();

        // Ocultar advertencia si estaba visible
        if (warningShown) {
            ocultarAdvertencia();
        }

        // Renovar sesión con debounce (máximo cada 1 minuto)
        const ahora = Date.now();
        if (ahora - lastRenew >= RENEW_DEBOUNCE_MS) {
            lastRenew = ahora;
            renovarSesion();
        }
    }

    /**
     * Llama al endpoint de renovación de sesión.
     */
    function renovarSesion() {
        const contextPath = getContextPath();

        fetch(contextPath + '/api/session/renew', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(function (response) {
            if (!response.ok) {
                // Sesión expirada en el servidor
                manejarExpiracion();
            }
        })
        .catch(function (error) {
            console.warn('Error al renovar sesión:', error);
        });
    }

    /**
     * Verifica periódicamente si la sesión debe expirar por inactividad.
     */
    function verificarInactividad() {
        if (sessionExpired) return;

        const tiempoInactivo = Date.now() - lastActivity;

        // Si ha pasado el tiempo total → sesión expirada
        if (tiempoInactivo >= SESSION_TIMEOUT_MS) {
            manejarExpiracion();
            return;
        }

        // Si quedan menos de 2 minutos → mostrar advertencia
        if (tiempoInactivo >= (SESSION_TIMEOUT_MS - WARNING_BEFORE_MS) && !warningShown) {
            const minutosRestantes = Math.ceil((SESSION_TIMEOUT_MS - tiempoInactivo) / 60000);
            mostrarAdvertencia(minutosRestantes);
        }
    }

    /**
     * Maneja la expiración de la sesión.
     */
    function manejarExpiracion() {
        sessionExpired = true;
        const contextPath = getContextPath();

        // Mostrar mensaje y redirigir
        if (warningModal) {
            warningModal.remove();
        }

        const modal = document.createElement('div');
        modal.id = 'session-expired-modal';
        modal.innerHTML = 
            '<div style="position:fixed;top:0;left:0;width:100%;height:100%;' +
            'background:rgba(0,0,0,0.6);display:flex;align-items:center;' +
            'justify-content:center;z-index:99999;">' +
            '<div style="background:#fff;border-radius:12px;padding:32px;' +
            'max-width:400px;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,0.2);">' +
            '<div style="font-size:48px;margin-bottom:16px;">⏰</div>' +
            '<h3 style="margin:0 0 8px;color:#1a1a2e;font-size:20px;">' +
            'Sesión expirada</h3>' +
            '<p style="color:#666;margin:0 0 24px;font-size:14px;">' +
            'Tu sesión ha expirado por inactividad.<br>Serás redirigido al inicio de sesión.</p>' +
            '<button onclick="window.location.href=\'' + contextPath + '/login\'" ' +
            'style="background:#4361ee;color:#fff;border:none;padding:10px 32px;' +
            'border-radius:8px;font-size:14px;cursor:pointer;font-weight:500;">' +
            'Ir a Iniciar Sesión</button></div></div>';

        document.body.appendChild(modal);

        // Redirigir automáticamente después de 3 segundos
        setTimeout(function () {
            window.location.href = contextPath + '/login';
        }, 3000);
    }

    /**
     * Muestra la advertencia de que la sesión está por expirar.
     */
    function mostrarAdvertencia(minutosRestantes) {
        warningShown = true;

        warningModal = document.createElement('div');
        warningModal.id = 'session-warning-modal';
        warningModal.innerHTML = 
            '<div style="position:fixed;top:20px;right:20px;z-index:99998;' +
            'background:#fff3cd;border:1px solid #ffc107;border-radius:10px;' +
            'padding:16px 20px;max-width:320px;box-shadow:0 4px 12px rgba(0,0,0,0.15);' +
            'display:flex;align-items:center;gap:12px;">' +
            '<span style="font-size:24px;">⚠️</span>' +
            '<div><strong style="color:#856404;font-size:14px;">Sesión por expirar</strong>' +
            '<p style="margin:4px 0 0;color:#856404;font-size:13px;">' +
            'Tu sesión expirará en ' + minutosRestantes + ' minuto(s) por inactividad. ' +
            'Realiza cualquier acción para mantenerla activa.</p></div></div>';

        document.body.appendChild(warningModal);
    }

    /**
     * Oculta la advertencia de sesión.
     */
    function ocultarAdvertencia() {
        warningShown = false;
        if (warningModal) {
            warningModal.remove();
            warningModal = null;
        }
    }

    // =================== INICIALIZACIÓN ===================

    // Eventos de actividad del usuario
    var eventos = ['click', 'mousemove', 'keydown', 'keypress', 'scroll', 'touchstart'];
    eventos.forEach(function (evento) {
        document.addEventListener(evento, registrarActividad, { passive: true });
    });

    // Verificar inactividad periódicamente
    setInterval(verificarInactividad, CHECK_INTERVAL_MS);

    // Renovar sesión al cargar la página (confirma que el token es válido)
    renovarSesion();

})();
