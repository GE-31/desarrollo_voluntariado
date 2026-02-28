document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.querySelector('.sidebar');
    const toggleBtn = document.createElement('button');
    toggleBtn.innerHTML = '☰';
    toggleBtn.style.cssText = 'position:absolute;top:10px;right:-40px;background:#4f4fc1;color:#fff;border:none;padding:10px;border-radius:5px;cursor:pointer;';
    sidebar.appendChild(toggleBtn);

    toggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('collapsed');
    });

    // ─── Detectar página activa por URL y marcar nav-item ───
    const currentPath = window.location.pathname;
    // Extraer la ruta sin el context path (ej: /sistemadevoluntariado/donaciones → /donaciones)
    const segments = currentPath.split('/');
    // Buscar la última parte significativa de la ruta
    let rutaActual = '/' + segments[segments.length - 1];
    // Para rutas con contexto como /sistemadevoluntariado/salidas-donaciones
    if (segments.length >= 2) {
        rutaActual = '/' + segments[segments.length - 1];
    }

    // Marcar el nav-item activo
    document.querySelectorAll('.panel .nav-item').forEach(link => {
        const href = link.getAttribute('href') || '';
        // Extraer la última parte del href
        const hrefSegments = href.split('/');
        const hrefRuta = '/' + hrefSegments[hrefSegments.length - 1];

        if (hrefRuta === rutaActual && rutaActual !== '/') {
            link.classList.add('active');
        }
    });

    // También marcar Dashboard si estamos en /dashboard
    if (rutaActual === '/dashboard') {
        const dashLink = document.querySelector('.dashboard-link');
        if (dashLink) dashLink.classList.add('active');
    }

    // ─── Auto-abrir el grupo que contiene el nav-item activo ───
    const activeItem = document.querySelector('.panel .nav-item.active');
    if (activeItem) {
        const panel = activeItem.closest('.panel');
        const accordion = panel ? panel.previousElementSibling : null;
        if (accordion && accordion.classList.contains('accordion')) {
            accordion.classList.add('active');
            panel.style.maxHeight = panel.scrollHeight + 'px';
            panel.classList.add('open');
        }
    }
});

// ─── Accordion click: abrir/cerrar grupos ───
document.querySelectorAll(".accordion").forEach(btn => {
  btn.addEventListener("click", () => {
    // Cerrar otros paneles (comportamiento tipo accordion)
    document.querySelectorAll(".accordion").forEach(otherBtn => {
      if (otherBtn !== btn && otherBtn.classList.contains("active")) {
        otherBtn.classList.remove("active");
        otherBtn.nextElementSibling.style.maxHeight = null;
        otherBtn.nextElementSibling.classList.remove("open");
      }
    });

    btn.classList.toggle("active");
    let panel = btn.nextElementSibling;
    if (panel.style.maxHeight) {
      panel.style.maxHeight = null;
      panel.classList.remove("open");
    } else {
      panel.style.maxHeight = panel.scrollHeight + "px";
      panel.classList.add("open");
    }
  });
});
