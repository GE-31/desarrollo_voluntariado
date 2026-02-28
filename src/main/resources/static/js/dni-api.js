// =========================================
// SERVICIO DE BÚSQUEDA DE DNI
// =========================================

// Configura aquí tu URL de API y TOKEN
const DNI_API_BASE = "https://dniruc.apisperu.com/api/v1/dni";
const DNI_API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImtmZW50eWdlbjAyNDVAZ21haWwuY29tIn0.JaGeW7g-XS7gcGqBFj7_mnNTgXwFg-THOxxgkLsfJaU";

/**
 * Busca información del DNI en la API
 * @param {string} dni - Número de DNI a buscar
 */
async function buscarDNIEnAPI(dni) {
    try {
        // Validar que el DNI no esté vacío
        if (!dni || dni.trim() === "") {
            mostrarNotificacion("Por favor ingresa un DNI", "error");
            return null;
        }

        // Mostrar indicador de carga
        const btnBuscar = document.querySelector(".btn-search-dni");
        if (btnBuscar) {
            btnBuscar.disabled = true;
            btnBuscar.textContent = "Buscando...";
        }

        // Construir URL con el DNI y token
        const url = `${DNI_API_BASE}/${dni}?token=${DNI_API_TOKEN}`;
        console.log("Buscando DNI en:", url);

        // Llamar a la API
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        const data = await response.json();
        console.log("Respuesta de API:", data);

        // Restaurar botón
        if (btnBuscar) {
            btnBuscar.disabled = false;
            btnBuscar.textContent = "🔍 Buscar";
        }

        // Verificar si encontró datos (no mostramos notificación aquí para evitar duplicados)
        if (data && (data.nombres || data.success)) {
            return data;
        } else {
            mostrarNotificacion("No se encontraron datos para este DNI", "warning");
            return null;
        }

    } catch (error) {
        console.error("Error al buscar DNI:", error);

        // Restaurar botón
        const btnBuscar = document.querySelector(".btn-search-dni");
        if (btnBuscar) {
            btnBuscar.disabled = false;
            btnBuscar.textContent = "🔍 Buscar";
        }

        mostrarNotificacion("Error al conectar con la API: " + error.message, "error");
        return null;
    }
}

/**
 * Llena los campos del formulario con datos del DNI
 */
async function buscarYLlenarDatos() {
    const dni = document.getElementById("dni")?.value;

    if (!dni) {
        mostrarNotificacion("Ingresa un DNI primero", "warning");
        return;
    }

    const datos = await buscarDNIEnAPI(dni);

    if (datos) {
        console.log("Datos completos de la API:", datos);
        console.log("Claves disponibles:", Object.keys(datos));

        // Llenar NOMBRES
        let nombreCompleto = "";
        if (datos.nombres) {
            nombreCompleto = datos.nombres;
            document.getElementById("nombres").value = nombreCompleto;
            console.log("✓ Nombres llenado:", nombreCompleto);
        }

        // Llenar APELLIDOS - Intentar múltiples estrategias
        let apellidoCompleto = "";

        // Estrategia 1: Buscar campos específicos de apellido (snake_case y camelCase)
        if ((datos.apellido_paterno && datos.apellido_materno) || (datos.apellidoPaterno && datos.apellidoMaterno)) {
            const a1 = datos.apellido_paterno || datos.apellidoPaterno;
            const a2 = datos.apellido_materno || datos.apellidoMaterno;
            apellidoCompleto = (a1 + " " + a2).trim();
            console.log("✓ Estrategia 1: Apellidos (paterno + materno):", apellidoCompleto);
        } else if (datos.apellido_paterno || datos.apellidoPaterno) {
            apellidoCompleto = datos.apellido_paterno || datos.apellidoPaterno;
            console.log("✓ Estrategia 1: Apellidos (solo paterno):", apellidoCompleto);
        } else if (datos.apellido_materno || datos.apellidoMaterno) {
            apellidoCompleto = datos.apellido_materno || datos.apellidoMaterno;
            console.log("✓ Estrategia 1: Apellidos (solo materno):", apellidoCompleto);
        } else if (datos.apellidos) {
            apellidoCompleto = datos.apellidos;
            console.log("✓ Estrategia 1: Apellidos (campo apellidos):", apellidoCompleto);
        } else if (datos.apellido) {
            apellidoCompleto = datos.apellido;
            console.log("✓ Estrategia 1: Apellidos (campo apellido):", apellidoCompleto);
        }

        // Estrategia 2: Si no encontró apellido pero tiene nombres, intentar extraer
        if (!apellidoCompleto && nombreCompleto) {
            var partesNombre = nombreCompleto.trim().split(/\s+/);
            if (partesNombre.length > 1) {
                apellidoCompleto = partesNombre.slice(1).join(" ");
                console.log("✓ Estrategia 2: Apellidos extraído del nombre:", apellidoCompleto);
            }
        }

        // Estrategia 3: Buscar en cualquier campo que contenga "apellido" y combinar si hay varios
        if (!apellidoCompleto) {
            var apValues = [];
            for (var key in datos) {
                if (key.toLowerCase().indexOf("apellido") !== -1 && datos[key]) {
                    apValues.push(datos[key]);
                }
            }
            if (apValues.length > 0) {
                apellidoCompleto = apValues.join(" ");
                console.log("✓ Estrategia 3: Apellidos combinados (campos encontrados):", apValues);
            }
        }

        if (apellidoCompleto) {
            document.getElementById("apellidos").value = apellidoCompleto;
            console.log("✓ FINAL: Apellidos llenado:", apellidoCompleto);
        } else {
            console.warn("⚠ No se encontró apellido en los datos");
        }

        // Llenar CORREO
        if (datos.correo) {
            document.getElementById("correo").value = datos.correo;
            console.log("✓ Correo llenado:", datos.correo);
        }

        // Llenar TELÉFONO
        if (datos.telefono) {
            document.getElementById("telefono").value = datos.telefono;
            console.log("✓ Teléfono llenado:", datos.telefono);
        }

        // Notificación única y simple
        mostrarNotificacion("DNI ENCONTRADO", "success");
    }
}
