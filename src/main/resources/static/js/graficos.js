
function initDashboardCharts(datos) {
    const actLabels = datos.actLabels || [];
    const actData = datos.actData || [];
    const horasLabels = datos.horasLabels || [];
    const horasData = datos.horasData || [];

    const donutColors = ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ef4444'];

    // ── Helper: crear gráfico de línea ──
    function crearLineChart(canvasId) {
        var el = document.getElementById(canvasId);
        if (!el) return;
        var ctx = el.getContext('2d');
        var grad = ctx.createLinearGradient(0, 0, 0, 250);
        grad.addColorStop(0, 'rgba(59,130,246,0.20)');
        grad.addColorStop(1, 'rgba(59,130,246,0.01)');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: actLabels,
                datasets: [{
                    label: 'Actividades',
                    data: actData,
                    borderColor: '#3b82f6',
                    backgroundColor: grad,
                    borderWidth: 2.5,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#3b82f6',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1e293b',
                        titleFont: { family: 'Inter', size: 12, weight: '600' },
                        bodyFont: { family: 'Inter', size: 11 },
                        padding: 10, cornerRadius: 8, displayColors: false,
                        callbacks: {
                            label: function (c) { return c.parsed.y + ' actividades'; }
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { family: 'Inter', size: 11 }, color: '#94a3b8' } },
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.04)', drawBorder: false }, ticks: { font: { family: 'Inter', size: 11 }, color: '#94a3b8', stepSize: 1, precision: 0 } }
                }
            }
        });
    }

    // 1. Gráfico línea izquierdo
    crearLineChart('chartActividadesMes');

    // ============================
    // 3. Gráfico DONUT – Horas Voluntarias + Leyenda
    // ============================
    var ctxDonut = document.getElementById('chartHorasVoluntarias');
    if (ctxDonut) {
        var donutLabelsArr = horasLabels.length > 0 ? horasLabels : ['Total'];
        var donutDataArr = horasData.length > 0 ? horasData : [1];
        var donutColorsArr = horasData.length > 0
            ? donutColors.slice(0, horasData.length)
            : ['#10b981'];

        new Chart(ctxDonut.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: donutLabelsArr,
                datasets: [{
                    data: donutDataArr,
                    backgroundColor: donutColorsArr,
                    borderColor: '#fff',
                    borderWidth: 3,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '68%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        enabled: horasData.length > 0,
                        backgroundColor: '#1e293b',
                        titleFont: { family: 'Inter', size: 11, weight: '600' },
                        bodyFont: { family: 'Inter', size: 10 },
                        padding: 8, cornerRadius: 8,
                        callbacks: {
                            label: function (c) {
                                var total = c.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                                var pct = total > 0 ? Math.round(c.parsed / total * 100) : 0;
                                return c.label + ': ' + c.parsed + 'h (' + pct + '%)';
                            }
                        }
                    }
                }
            }
        });

        // ── Generar leyenda dinámica ──
        var legendContainer = document.getElementById('horasLegend');
        if (legendContainer && horasLabels.length > 0) {
            legendContainer.innerHTML = '';
            var totalHoras = horasData.reduce(function (a, b) { return a + b; }, 0);
            for (var i = 0; i < horasLabels.length; i++) {
                var pct = totalHoras > 0 ? Math.round(horasData[i] / totalHoras * 100) : 0;
                var item = document.createElement('div');
                item.className = 'horas-legend-item';
                item.innerHTML =
                    '<span class="horas-legend-dot" style="background:' + donutColorsArr[i] + '"></span>' +
                    '<span>' + horasLabels[i] + ' <span class="horas-legend-value">(' + horasData[i] + 'h)</span></span>';
                legendContainer.appendChild(item);
            }
        }
    }
}