package com.sistemadevoluntariado.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.entity.Asistencia;
import com.sistemadevoluntariado.entity.Beneficiario;
import com.sistemadevoluntariado.entity.Donacion;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.service.ReportesService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    private static final Logger logger = Logger.getLogger(ReportesController.class.getName());

    @Autowired
    private ReportesService reportesService;

    // ── Vista principal ──
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("page", "reportes");
        return "views/reportes/reportes";
    }

    // ── Resumen general del sistema (JSON) ──
    @GetMapping(params = "accion=resumen")
    @ResponseBody
    public Map<String, Object> resumenGeneral() {
        return reportesService.obtenerResumenGeneral();
    }

    // ── Datos por módulo (JSON) ──
    @GetMapping(params = "accion=voluntarios")
    @ResponseBody
    public List<Voluntario> listarVoluntarios() {
        return reportesService.listarVoluntarios();
    }

    @GetMapping(params = "accion=actividades")
    @ResponseBody
    public List<Actividad> listarActividades() {
        return reportesService.listarActividades();
    }

    @GetMapping(params = "accion=beneficiarios")
    @ResponseBody
    public List<Beneficiario> listarBeneficiarios() {
        return reportesService.listarBeneficiarios();
    }

    @GetMapping(params = "accion=asistencias")
    @ResponseBody
    public List<Asistencia> listarAsistencias() {
        return reportesService.listarAsistencias();
    }

    @GetMapping(params = "accion=donaciones_lista")
    @ResponseBody
    public List<Donacion> listarDonaciones() {
        return reportesService.listarDonaciones();
    }

    @GetMapping(params = "accion=inventario")
    @ResponseBody
    public List<InventarioItem> listarInventario() {
        return reportesService.listarInventario();
    }

    @GetMapping(params = "accion=tesoreria")
    @ResponseBody
    public List<MovimientoFinanciero> listarTesoreria() {
        return reportesService.listarTesoreria();
    }

    // ── Legado: listar donaciones filtradas ──
    @GetMapping(params = "accion=listar")
    @ResponseBody
    public List<Donacion> listar(@RequestParam(required = false) String fechaInicio,
                                  @RequestParam(required = false) String fechaFin,
                                  @RequestParam(required = false) String tipoDonacion,
                                  @RequestParam(required = false) String tipoDonante,
                                  @RequestParam(required = false) String incluirAnuladas) {
        boolean inclAnuladas = "1".equals(incluirAnuladas) || "true".equalsIgnoreCase(incluirAnuladas);
        return reportesService.filtrarDonaciones(fechaInicio, fechaFin, tipoDonacion, tipoDonante, inclAnuladas);
    }

    // ══════════════════════════════════════════════════════════
    //  EXPORTAR REPORTE GENERAL COMPLETO A EXCEL
    // ══════════════════════════════════════════════════════════

    @GetMapping(params = "accion=exportar_general")
    public void exportarGeneral(HttpServletResponse response) throws IOException {
        String filename = "Reporte_General_Voluntariado_" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (Workbook wb = new XSSFWorkbook()) {

            // ── Estilos ──
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle subtitleStyle = wb.createCellStyle();
            Font subtitleFont = wb.createFont();
            subtitleFont.setItalic(true);
            subtitleFont.setFontHeightInPoints((short) 9);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);

            // ── Hoja 1: RESUMEN GENERAL ──
            crearHojaResumen(wb, titleStyle, subtitleStyle, headerStyle, moneyStyle);

            // ── Hoja 2: VOLUNTARIOS ──
            crearHojaVoluntarios(wb, titleStyle, subtitleStyle, headerStyle);

            // ── Hoja 3: ACTIVIDADES ──
            crearHojaActividades(wb, titleStyle, subtitleStyle, headerStyle);

            // ── Hoja 4: ASISTENCIAS ──
            crearHojaAsistencias(wb, titleStyle, subtitleStyle, headerStyle);

            // ── Hoja 5: BENEFICIARIOS ──
            crearHojaBeneficiarios(wb, titleStyle, subtitleStyle, headerStyle);

            // ── Hoja 6: DONACIONES ──
            crearHojaDonaciones(wb, titleStyle, subtitleStyle, headerStyle, moneyStyle);

            // ── Hoja 7: INVENTARIO ──
            crearHojaInventario(wb, titleStyle, subtitleStyle, headerStyle);

            // ── Hoja 8: TESORERÍA ──
            crearHojaTesoreria(wb, titleStyle, subtitleStyle, headerStyle, moneyStyle);

            wb.write(response.getOutputStream());
            logger.info("Reporte general exportado exitosamente");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar reporte general Excel", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando reporte");
        }
    }

    // ── Legado: exportar solo donaciones ──
    @GetMapping(params = "accion=donaciones")
    public void exportarExcelDonaciones(@RequestParam(required = false) String fechaInicio,
                               @RequestParam(required = false) String fechaFin,
                               @RequestParam(required = false) String tipoDonacion,
                               @RequestParam(required = false) String tipoDonante,
                               @RequestParam(required = false) String incluirAnuladas,
                               HttpServletResponse response) throws IOException {
        boolean inclAnuladas = "1".equals(incluirAnuladas) || "true".equalsIgnoreCase(incluirAnuladas);
        List<Donacion> donaciones = reportesService.filtrarDonaciones(
                fechaInicio, fechaFin, tipoDonacion, tipoDonante, inclAnuladas);

        String filename = "donaciones_" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            Sheet sheet = wb.createSheet("Donaciones");
            String[] headers = {"Fecha", "Tipo", "Monto (S/)", "Donante", "Nombre / Razón Social",
                                "Correo", "Teléfono", "Actividad", "Estado"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Donacion d : donaciones) {
                Row row = sheet.createRow(rowIdx++);
                String fecha = d.getRegistradoEn();
                row.createCell(0).setCellValue(fecha != null ? (fecha.length() >= 10 ? fecha.substring(0, 10) : fecha) : "");
                row.createCell(1).setCellValue(d.getIdTipoDonacion() == 1 ? "Dinero" : "En especie");
                Cell montoCell = row.createCell(2);
                montoCell.setCellValue(d.getCantidad() != null ? d.getCantidad().doubleValue() : 0);
                montoCell.setCellStyle(moneyStyle);
                row.createCell(3).setCellValue(safe(d.getTipoDonante()));
                row.createCell(4).setCellValue(safe(d.getNombreDonante()));
                row.createCell(5).setCellValue(safe(d.getCorreoDonante()));
                row.createCell(6).setCellValue(safe(d.getTelefonoDonante()));
                row.createCell(7).setCellValue(safe(d.getActividad()));
                String estado = d.getEstado() != null ? d.getEstado().toUpperCase() : "PENDIENTE";
                if ("ACTIVO".equals(estado)) estado = "CONFIRMADO";
                row.createCell(8).setCellValue(estado);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(response.getOutputStream());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar Excel de donaciones", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando reporte");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  EXPORTACIÓN POR MÓDULO INDIVIDUAL
    // ══════════════════════════════════════════════════════════

    @GetMapping(params = "accion=exportar_voluntarios")
    public void exportarVoluntarios(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Voluntarios_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            crearHojaVoluntarios(wb, estilos[0], estilos[1], estilos[2]);
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping(params = "accion=exportar_actividades")
    public void exportarActividades(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Actividades_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            crearHojaActividades(wb, estilos[0], estilos[1], estilos[2]);
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping(params = "accion=exportar_asistencias")
    public void exportarAsistencias(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Asistencias_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            crearHojaAsistencias(wb, estilos[0], estilos[1], estilos[2]);
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping(params = "accion=exportar_beneficiarios")
    public void exportarBeneficiarios(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Beneficiarios_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            crearHojaBeneficiarios(wb, estilos[0], estilos[1], estilos[2]);
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping(params = "accion=exportar_inventario")
    public void exportarInventario(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Inventario_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            crearHojaInventario(wb, estilos[0], estilos[1], estilos[2]);
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping(params = "accion=exportar_tesoreria")
    public void exportarTesoreria(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "Tesoreria_" + LocalDate.now() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle[] estilos = crearEstilosBase(wb);
            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            crearHojaTesoreria(wb, estilos[0], estilos[1], estilos[2], moneyStyle);
            wb.write(response.getOutputStream());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  MÉTODOS DE CONSTRUCCIÓN DE HOJAS
    // ══════════════════════════════════════════════════════════

    private void crearHojaResumen(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle,
                                   CellStyle headerStyle, CellStyle moneyStyle) {
        Sheet sheet = wb.createSheet("Resumen General");
        Map<String, Object> resumen = reportesService.obtenerResumenGeneral();

        int r = 0;
        Row row = sheet.createRow(r++);
        Cell titleCell = row.createCell(0);
        titleCell.setCellValue("REPORTE GENERAL - SISTEMA DE VOLUNTARIADO UNIVERSITARIO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        row = sheet.createRow(r++);
        Cell subCell = row.createCell(0);
        subCell.setCellValue("Generado el: " + LocalDate.now().toString());
        subCell.setCellStyle(subtitleStyle);

        r++;
        String[][] secciones = {
            {"VOLUNTARIOS", "Total Voluntarios", str(resumen.get("totalVoluntarios")),
                             "Activos", str(resumen.get("voluntariosActivos"))},
            {"BENEFICIARIOS", "Total Beneficiarios", str(resumen.get("totalBeneficiarios")),
                              "Activos", str(resumen.get("beneficiariosActivos"))},
            {"ACTIVIDADES", "Total Actividades", str(resumen.get("totalActividades")),
                            "Activas", str(resumen.get("actividadesActivas")),
                            "Finalizadas", str(resumen.get("actividadesFinalizadas"))},
            {"ASISTENCIAS", "Total Registros", str(resumen.get("totalAsistencias")),
                            "Asistieron", str(resumen.get("asistieron")),
                            "Tardanzas", str(resumen.get("tardanzas")),
                            "Faltas", str(resumen.get("faltas")),
                            "Total Horas Voluntarias", str(resumen.get("totalHorasVoluntarias"))},
            {"DONACIONES", "Total Donaciones", str(resumen.get("totalDonaciones")),
                           "Confirmadas", str(resumen.get("donacionesConfirmadas")),
                           "Total Recaudado (S/)", str(resumen.get("totalDonacionesDinero"))},
            {"INVENTARIO", "Total Items", str(resumen.get("totalItemsInventario")),
                           "Items Activos", str(resumen.get("itemsActivos")),
                           "Items Stock Bajo", str(resumen.get("itemsStockBajo"))},
            {"TESORERÍA", "Total Ingresos (S/)", str(resumen.get("totalIngresos")),
                          "Total Egresos (S/)", str(resumen.get("totalEgresos")),
                          "Saldo Actual (S/)", str(resumen.get("saldoActual"))}
        };

        for (String[] seccion : secciones) {
            row = sheet.createRow(r++);
            Cell sec = row.createCell(0);
            sec.setCellValue(seccion[0]);
            sec.setCellStyle(headerStyle);
            Cell sec2 = row.createCell(1);
            sec2.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 1));

            for (int i = 1; i < seccion.length; i += 2) {
                row = sheet.createRow(r++);
                row.createCell(0).setCellValue(seccion[i]);
                row.createCell(1).setCellValue(seccion[i + 1]);
            }
            r++;
        }

        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 5000);
    }

    private void crearHojaVoluntarios(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Voluntarios");
        List<Voluntario> lista = reportesService.listarVoluntarios();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "LISTADO DE VOLUNTARIOS", 7);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Nombres", "Apellidos", "DNI", "Correo", "Teléfono", "Carrera", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (Voluntario v : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(v.getNombres()));
            row.createCell(2).setCellValue(safe(v.getApellidos()));
            row.createCell(3).setCellValue(safe(v.getDni()));
            row.createCell(4).setCellValue(safe(v.getCorreo()));
            row.createCell(5).setCellValue(safe(v.getTelefono()));
            row.createCell(6).setCellValue(safe(v.getCarrera()));
            row.createCell(7).setCellValue(safe(v.getEstado()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaActividades(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Actividades");
        List<Actividad> lista = reportesService.listarActividades();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "LISTADO DE ACTIVIDADES", 7);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Nombre", "Descripción", "Fecha Inicio", "Fecha Fin", "Ubicación", "Cupo", "Inscritos", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (Actividad a : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(a.getNombre()));
            row.createCell(2).setCellValue(safe(a.getDescripcion()));
            row.createCell(3).setCellValue(a.getFechaInicio() != null ? a.getFechaInicio().toString() : "");
            row.createCell(4).setCellValue(a.getFechaFin() != null ? a.getFechaFin().toString() : "");
            row.createCell(5).setCellValue(safe(a.getUbicacion()));
            row.createCell(6).setCellValue(a.getCupoMaximo());
            row.createCell(7).setCellValue(a.getInscritos());
            row.createCell(8).setCellValue(safe(a.getEstado()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaAsistencias(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Asistencias");
        List<Asistencia> lista = reportesService.listarAsistencias();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "REGISTRO DE ASISTENCIAS", 8);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Voluntario", "DNI", "Actividad", "Fecha", "Hora Entrada", "Hora Salida", "Horas", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (Asistencia a : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(a.getNombreVoluntario()));
            row.createCell(2).setCellValue(safe(a.getDniVoluntario()));
            row.createCell(3).setCellValue(safe(a.getNombreActividad()));
            row.createCell(4).setCellValue(safe(a.getFecha()));
            row.createCell(5).setCellValue(safe(a.getHoraEntrada()));
            row.createCell(6).setCellValue(safe(a.getHoraSalida()));
            row.createCell(7).setCellValue(a.getHorasTotales() != null ? a.getHorasTotales().doubleValue() : 0);
            row.createCell(8).setCellValue(safe(a.getEstado()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaBeneficiarios(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Beneficiarios");
        List<Beneficiario> lista = reportesService.listarBeneficiarios();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "LISTADO DE BENEFICIARIOS", 8);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Nombres", "Apellidos", "DNI", "Teléfono", "Distrito", "Tipo", "Necesidad", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (Beneficiario b : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(b.getNombres()));
            row.createCell(2).setCellValue(safe(b.getApellidos()));
            row.createCell(3).setCellValue(safe(b.getDni()));
            row.createCell(4).setCellValue(safe(b.getTelefono()));
            row.createCell(5).setCellValue(safe(b.getDistrito()));
            row.createCell(6).setCellValue(safe(b.getTipoBeneficiario()));
            row.createCell(7).setCellValue(safe(b.getNecesidadPrincipal()));
            row.createCell(8).setCellValue(safe(b.getEstado()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaDonaciones(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle,
                                      CellStyle headerStyle, CellStyle moneyStyle) {
        Sheet sheet = wb.createSheet("Donaciones");
        List<Donacion> lista = reportesService.listarDonaciones();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "REGISTRO DE DONACIONES", 8);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Fecha", "Tipo", "Monto (S/)", "Donante", "Nombre", "Actividad", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (Donacion d : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            String fecha = d.getRegistradoEn();
            row.createCell(1).setCellValue(fecha != null ? (fecha.length() >= 10 ? fecha.substring(0, 10) : fecha) : "");
            row.createCell(2).setCellValue(d.getIdTipoDonacion() == 1 ? "Dinero" : "En especie");
            Cell c = row.createCell(3);
            c.setCellValue(d.getCantidad() != null ? d.getCantidad().doubleValue() : 0);
            c.setCellStyle(moneyStyle);
            row.createCell(4).setCellValue(safe(d.getTipoDonante()));
            row.createCell(5).setCellValue(safe(d.getDonanteNombre()));
            row.createCell(6).setCellValue(safe(d.getActividad()));
            String estado = d.getEstado() != null ? d.getEstado().toUpperCase() : "PENDIENTE";
            if ("ACTIVO".equals(estado)) estado = "CONFIRMADO";
            row.createCell(7).setCellValue(estado);
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaInventario(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Inventario");
        List<InventarioItem> lista = reportesService.listarInventario();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "INVENTARIO DE ITEMS", 6);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Nombre", "Categoría", "Unidad", "Stock Actual", "Stock Mínimo", "Estado"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (InventarioItem item : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(item.getNombre()));
            row.createCell(2).setCellValue(safe(item.getCategoria()));
            row.createCell(3).setCellValue(safe(item.getUnidadMedida()));
            row.createCell(4).setCellValue(item.getStockActual());
            row.createCell(5).setCellValue(item.getStockMinimo());
            row.createCell(6).setCellValue(safe(item.getEstado()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void crearHojaTesoreria(Workbook wb, CellStyle titleStyle, CellStyle subtitleStyle,
                                     CellStyle headerStyle, CellStyle moneyStyle) {
        Sheet sheet = wb.createSheet("Tesorería");
        List<MovimientoFinanciero> lista = reportesService.listarTesoreria();

        int r = 0;
        crearTitulo(sheet, r++, titleStyle, "MOVIMIENTOS DE TESORERÍA", 6);
        crearSubtitulo(sheet, r++, subtitleStyle, lista.size() + " registros | Generado: " + LocalDate.now());
        r++;

        String[] headers = {"#", "Fecha", "Tipo", "Categoría", "Descripción", "Monto (S/)", "Comprobante"};
        crearCabecera(sheet, r++, headerStyle, headers);

        int n = 1;
        for (MovimientoFinanciero m : lista) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(n++);
            row.createCell(1).setCellValue(safe(m.getFechaMovimiento()));
            row.createCell(2).setCellValue(safe(m.getTipo()));
            row.createCell(3).setCellValue(safe(m.getCategoria()));
            row.createCell(4).setCellValue(safe(m.getDescripcion()));
            Cell c = row.createCell(5);
            c.setCellValue(m.getMonto());
            c.setCellStyle(moneyStyle);
            row.createCell(6).setCellValue(safe(m.getComprobante()));
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════

    private void crearTitulo(Sheet sheet, int rowNum, CellStyle style, String texto, int cols) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(texto);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, cols));
    }

    private void crearSubtitulo(Sheet sheet, int rowNum, CellStyle style, String texto) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(texto);
        cell.setCellStyle(style);
    }

    private void crearCabecera(Sheet sheet, int rowNum, CellStyle style, String[] headers) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private CellStyle[] crearEstilosBase(Workbook wb) {
        CellStyle titleStyle = wb.createCellStyle();
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        CellStyle subtitleStyle = wb.createCellStyle();
        Font subtitleFont = wb.createFont();
        subtitleFont.setItalic(true);
        subtitleFont.setFontHeightInPoints((short) 9);
        subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        subtitleStyle.setFont(subtitleFont);

        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        return new CellStyle[]{titleStyle, subtitleStyle, headerStyle};
    }

    private void setExcelResponse(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }

    private String safe(String v) { return v != null ? v : ""; }
    private String str(Object v) { return v != null ? v.toString() : "0"; }
}
