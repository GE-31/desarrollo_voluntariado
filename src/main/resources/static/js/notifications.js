/* ══════════════════════════════════════════════════════════════
   Notify.js — Sistema global de notificaciones
   Uso:
     Notify.success('Guardado correctamente');
     Notify.error('No se pudo conectar');
     Notify.warning('Verificar datos');
     Notify.info('Procesando...');
     Notify.confirm('¿Eliminar?', 'Esta acción no se puede deshacer.')
           .then(ok => { if (ok) ... });
   ══════════════════════════════════════════════════════════════ */

const Notify = (() => {
    /* ── CONFIG ─────────────────────────────────────────────── */
    const ICONS = {
        success: '<i class="fa-solid fa-check"></i>',
        error:   '<i class="fa-solid fa-xmark"></i>',
        warning: '<i class="fa-solid fa-triangle-exclamation"></i>',
        info:    '<i class="fa-solid fa-circle-info"></i>'
    };
    const TITLES = {
        success: 'Éxito',
        error:   'Error',
        warning: 'Atención',
        info:    'Información'
    };
    const CONFIRM_ICONS = {
        warning: '<i class="fa-solid fa-triangle-exclamation"></i>',
        danger:  '<i class="fa-solid fa-trash-can"></i>',
        info:    '<i class="fa-solid fa-circle-question"></i>',
        success: '<i class="fa-solid fa-check-circle"></i>'
    };

    const DEFAULT_DURATION = 4000;

    /* ── Ensure container ──────────────────────────────────── */
    function getContainer() {
        let c = document.getElementById('toast-container-global');
        if (!c) {
            c = document.createElement('div');
            c.id = 'toast-container-global';
            c.className = 'toast-container';
            document.body.appendChild(c);
        }
        return c;
    }

    /* ── TOAST ─────────────────────────────────────────────── */
    function toast(type, message, opts = {}) {
        const duration = opts.duration ?? DEFAULT_DURATION;
        const title = opts.title ?? TITLES[type] ?? '';

        const el = document.createElement('div');
        el.className = `toast-notify toast-${type}`;
        el.style.position = 'relative';
        el.innerHTML = `
            <div class="toast-icon">${ICONS[type] || ''}</div>
            <div class="toast-body">
                <div class="toast-title">${title}</div>
                <div class="toast-message">${escapeHtml(message)}</div>
            </div>
            <button class="toast-close" aria-label="Cerrar">&times;</button>
            <div class="toast-progress" style="animation-duration:${duration}ms"></div>
        `;

        const container = getContainer();
        container.appendChild(el);

        // Animate in
        requestAnimationFrame(() => {
            requestAnimationFrame(() => el.classList.add('show'));
        });

        // Close handler
        const close = () => {
            el.classList.remove('show');
            el.classList.add('hide');
            el.addEventListener('transitionend', () => el.remove(), { once: true });
            // Fallback
            setTimeout(() => { if (el.parentNode) el.remove(); }, 400);
        };

        el.querySelector('.toast-close').addEventListener('click', close);

        // Auto close
        if (duration > 0) {
            setTimeout(close, duration);
        }

        return el;
    }

    /* ── CONFIRM DIALOG ────────────────────────────────────── */
    function confirm(message, subtitle, opts = {}) {
        // Support: confirm('msg'), confirm('msg','sub'), confirm('msg','sub',{opts})
        if (typeof subtitle === 'object' && subtitle !== null) {
            opts = subtitle;
            subtitle = '';
        }
        subtitle = subtitle || '';

        const variant = opts.variant || 'warning';   // warning | danger | info | success
        const okText  = opts.okText  || 'Confirmar';
        const cancelText = opts.cancelText || 'Cancelar';

        return new Promise(resolve => {
            const overlay = document.createElement('div');
            overlay.className = 'confirm-overlay';
            overlay.innerHTML = `
                <div class="confirm-dialog confirm-${variant}">
                    <div class="confirm-icon">${CONFIRM_ICONS[variant] || CONFIRM_ICONS.warning}</div>
                    <div class="confirm-title">${escapeHtml(message)}</div>
                    <div class="confirm-text">${escapeHtml(subtitle)}</div>
                    <div class="confirm-actions">
                        <button class="confirm-btn confirm-btn-cancel">${escapeHtml(cancelText)}</button>
                        <button class="confirm-btn confirm-btn-ok">${escapeHtml(okText)}</button>
                    </div>
                </div>
            `;

            document.body.appendChild(overlay);
            requestAnimationFrame(() => {
                requestAnimationFrame(() => overlay.classList.add('show'));
            });

            const dismiss = (result) => {
                overlay.classList.remove('show');
                overlay.addEventListener('transitionend', () => overlay.remove(), { once: true });
                setTimeout(() => { if (overlay.parentNode) overlay.remove(); }, 350);
                resolve(result);
            };

            overlay.querySelector('.confirm-btn-ok').addEventListener('click', () => dismiss(true));
            overlay.querySelector('.confirm-btn-cancel').addEventListener('click', () => dismiss(false));

            // Click on backdrop closes
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) dismiss(false);
            });

            // ESC key closes
            const onKey = (e) => {
                if (e.key === 'Escape') { document.removeEventListener('keydown', onKey); dismiss(false); }
            };
            document.addEventListener('keydown', onKey);

            // Focus OK button
            setTimeout(() => overlay.querySelector('.confirm-btn-ok')?.focus(), 100);
        });
    }

    /* ── Helpers ────────────────────────────────────────────── */
    function escapeHtml(text) {
        const d = document.createElement('div');
        d.textContent = text || '';
        return d.innerHTML;
    }

    /* ── Public API ─────────────────────────────────────────── */
    return {
        success: (msg, opts) => toast('success', msg, opts),
        error:   (msg, opts) => toast('error',   msg, opts),
        warning: (msg, opts) => toast('warning', msg, opts),
        info:    (msg, opts) => toast('info',     msg, opts),
        confirm
    };
})();
