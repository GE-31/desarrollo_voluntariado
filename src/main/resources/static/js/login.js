document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', (e) => {
        const usuario = document.getElementById('usuario').value.trim();
        const clave = document.getElementById('clave').value.trim();

        if (usuario === '' || clave === '') {
            e.preventDefault();
            Notify.warning('Por favor completa todos los campos');
        }
    });
});
