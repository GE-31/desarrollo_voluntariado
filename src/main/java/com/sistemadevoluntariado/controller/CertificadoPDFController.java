package com.sistemadevoluntariado.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistemadevoluntariado.entity.Certificado;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.CertificadoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/certificados/pdf")
public class CertificadoPDFController {

    @Autowired
    private CertificadoService certificadoService;

    private static final java.awt.Color COLOR_AZUL_OSCURO = new java.awt.Color(30, 41, 59);
    private static final java.awt.Color COLOR_INDIGO = new java.awt.Color(79, 70, 229);
    private static final java.awt.Color COLOR_GRIS = new java.awt.Color(100, 116, 139);
    private static final java.awt.Color COLOR_GRIS_CLARO = new java.awt.Color(203, 213, 225);
    private static final java.awt.Color COLOR_DORADO = new java.awt.Color(180, 142, 58);
    private static final java.awt.Color COLOR_VERDE = new java.awt.Color(22, 163, 74);
    private static final java.awt.Color COLOR_ROJO = new java.awt.Color(220, 38, 38);

    @GetMapping
    public void generarPdf(@RequestParam int id,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session) throws Exception {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Certificado cert = certificadoService.obtenerCertificadoPorId(id);
        if (cert == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificado no encontrado");
            return;
        }

        byte[] pdfBytes = generarPDF(cert, request);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=Certificado_" + cert.getCodigoCertificado() + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private byte[] generarPDF(Certificado cert, HttpServletRequest request) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 50, 50, 40, 40);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();
            PdfContentByte canvas = writer.getDirectContent();

            dibujarBordesDecorativos(canvas, doc);
            agregarLogo(doc);
            doc.add(espaciado(8));

            Font fontTitulo = new Font(Font.HELVETICA, 28, Font.BOLD, COLOR_AZUL_OSCURO);
            Paragraph titulo = new Paragraph("CERTIFICADO DE VOLUNTARIADO", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Font fontSubtitulo = new Font(Font.HELVETICA, 13, Font.NORMAL, COLOR_INDIGO);
            Paragraph subtitulo = new Paragraph("Sistema de Voluntariado Universitario", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitulo);

            doc.add(espaciado(6));
            dibujarLineaDecorativa(canvas, doc);
            doc.add(espaciado(14));

            Font fontTexto = new Font(Font.HELVETICA, 12, Font.NORMAL, COLOR_GRIS);
            Paragraph certifica = new Paragraph("Se certifica que", fontTexto);
            certifica.setAlignment(Element.ALIGN_CENTER);
            doc.add(certifica);
            doc.add(espaciado(8));

            Font fontNombre = new Font(Font.HELVETICA, 24, Font.BOLD, COLOR_AZUL_OSCURO);
            Paragraph nombre = new Paragraph(cert.getNombreVoluntario(), fontNombre);
            nombre.setAlignment(Element.ALIGN_CENTER);
            doc.add(nombre);

            Font fontDni = new Font(Font.HELVETICA, 11, Font.NORMAL, COLOR_GRIS);
            Paragraph dni = new Paragraph("DNI: " + cert.getDniVoluntario(), fontDni);
            dni.setAlignment(Element.ALIGN_CENTER);
            doc.add(dni);
            doc.add(espaciado(10));

            Paragraph participacion = new Paragraph(
                    "Ha participado satisfactoriamente en la actividad de voluntariado", fontTexto);
            participacion.setAlignment(Element.ALIGN_CENTER);
            doc.add(participacion);
            doc.add(espaciado(6));

            Font fontActividad = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_INDIGO);
            Paragraph actividad = new Paragraph("\"" + cert.getNombreActividad() + "\"", fontActividad);
            actividad.setAlignment(Element.ALIGN_CENTER);
            doc.add(actividad);
            doc.add(espaciado(10));

            if (cert.getObservaciones() != null && !cert.getObservaciones().trim().isEmpty()) {
                Font fontObs = new Font(Font.HELVETICA, 10, Font.ITALIC, COLOR_GRIS);
                Paragraph obs = new Paragraph(cert.getObservaciones(), fontObs);
                obs.setAlignment(Element.ALIGN_CENTER);
                doc.add(obs);
                doc.add(espaciado(4));
            }

            if ("ANULADO".equals(cert.getEstado())) {
                doc.add(espaciado(4));
                Font fontAnulado = new Font(Font.HELVETICA, 36, Font.BOLD, COLOR_ROJO);
                Paragraph anulado = new Paragraph("— CERTIFICADO ANULADO —", fontAnulado);
                anulado.setAlignment(Element.ALIGN_CENTER);
                doc.add(anulado);
                doc.add(espaciado(4));
            }

            doc.add(espaciado(10));
            agregarSeccionFirmaYQR(doc, cert, request);
            agregarPiePagina(doc, cert);

            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void dibujarBordesDecorativos(PdfContentByte canvas, Document doc) {
        float w = doc.getPageSize().getWidth();
        float h = doc.getPageSize().getHeight();

        canvas.setColorStroke(COLOR_DORADO);
        canvas.setLineWidth(3f);
        canvas.rectangle(15, 15, w - 30, h - 30);
        canvas.stroke();
        canvas.setLineWidth(1f);
        canvas.rectangle(22, 22, w - 44, h - 44);
        canvas.stroke();

        float cornerSize = 30;
        canvas.setLineWidth(2.5f);
        canvas.setColorStroke(COLOR_INDIGO);
        // Esquinas
        float[][] corners = {{28, h - 28}, {w - 28, h - 28}, {28, 28}, {w - 28, 28}};
        int[][] dirs = {{0, -1, 1, 0}, {0, -1, -1, 0}, {0, 1, 1, 0}, {0, 1, -1, 0}};
        for (int i = 0; i < 4; i++) {
            canvas.moveTo(corners[i][0], corners[i][1]);
            canvas.lineTo(corners[i][0], corners[i][1] + dirs[i][1] * cornerSize);
            canvas.stroke();
            canvas.moveTo(corners[i][0], corners[i][1]);
            canvas.lineTo(corners[i][0] + dirs[i][2] * cornerSize, corners[i][1]);
            canvas.stroke();
        }
    }

    private void dibujarLineaDecorativa(PdfContentByte canvas, Document doc) {
        float w = doc.getPageSize().getWidth();
        float centerX = w / 2;
        float y = doc.getPageSize().getHeight() - 190;
        float lineLength = 120;

        canvas.setColorStroke(COLOR_DORADO);
        canvas.setLineWidth(1.5f);
        canvas.moveTo(centerX - lineLength - 15, y);
        canvas.lineTo(centerX - 15, y);
        canvas.stroke();

        canvas.setColorFill(COLOR_DORADO);
        canvas.moveTo(centerX, y + 4);
        canvas.lineTo(centerX + 6, y);
        canvas.lineTo(centerX, y - 4);
        canvas.lineTo(centerX - 6, y);
        canvas.closePath();
        canvas.fill();

        canvas.moveTo(centerX + 15, y);
        canvas.lineTo(centerX + lineLength + 15, y);
        canvas.stroke();
    }

    private void agregarLogo(Document doc) {
        try {
            ClassPathResource resource = new ClassPathResource("static/img/logo.png");
            if (resource.exists()) {
                InputStream is = resource.getInputStream();
                byte[] logoBytes = is.readAllBytes();
                is.close();
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            }
        } catch (Exception e) {
            System.out.println("Logo no encontrado, continuando sin logo");
        }
    }

    private void agregarBadgeHoras(Document doc, int horas) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(25);
        Font fontHoras = new Font(Font.HELVETICA, 18, Font.BOLD, java.awt.Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(horas + " HORAS", fontHoras));
        cell.setBackgroundColor(COLOR_INDIGO);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(10);
        cell.setPaddingBottom(10);
        cell.setBorderWidth(0);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarSeccionFirmaYQR(Document doc, Certificado cert, HttpServletRequest request) throws Exception {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(90);
        table.setWidths(new float[]{35, 30, 35});

        Font fontFirmaLinea = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_GRIS_CLARO);
        Font fontFirmaNombre = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_AZUL_OSCURO);
        Font fontFirmaCargo = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_GRIS);

        // Firma izquierda
        PdfPCell cellFirma = new PdfPCell();
        cellFirma.setBorderWidth(0);
        cellFirma.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellFirma.addElement(crearFirma("Director(a) de Voluntariado", "Universidad",
                fontFirmaLinea, fontFirmaNombre, fontFirmaCargo));
        table.addCell(cellFirma);

        // QR centro
        PdfPCell cellQR = new PdfPCell();
        cellQR.setBorderWidth(0);
        cellQR.setHorizontalAlignment(Element.ALIGN_CENTER);
        String url = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath()
                + "/certificados?action=verificar&codigo=" + cert.getCodigoCertificado();
        Image qrImage = generarCodigoQR(url, 100, 100);
        if (qrImage != null) {
            qrImage.setAlignment(Element.ALIGN_CENTER);
            cellQR.addElement(qrImage);
        }
        Font fontQR = new Font(Font.HELVETICA, 7, Font.NORMAL, COLOR_GRIS);
        Paragraph qrLabel = new Paragraph("Escanea para verificar", fontQR);
        qrLabel.setAlignment(Element.ALIGN_CENTER);
        cellQR.addElement(qrLabel);
        table.addCell(cellQR);

        // Firma derecha
        PdfPCell cellFirma2 = new PdfPCell();
        cellFirma2.setBorderWidth(0);
        cellFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellFirma2.addElement(crearFirma("Coordinador(a) de Actividad", "Responsable de la Actividad",
                fontFirmaLinea, fontFirmaNombre, fontFirmaCargo));
        table.addCell(cellFirma2);

        doc.add(table);
    }

    private Paragraph crearFirma(String cargo, String institucion, Font linea, Font nombre, Font cargoFont) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Paragraph("\n\n\n", linea));
        Paragraph l = new Paragraph("_________________________", linea);
        l.setAlignment(Element.ALIGN_CENTER);
        p.add(l);
        Paragraph n = new Paragraph(cargo, nombre);
        n.setAlignment(Element.ALIGN_CENTER);
        p.add(n);
        Paragraph c = new Paragraph(institucion, cargoFont);
        c.setAlignment(Element.ALIGN_CENTER);
        p.add(c);
        return p;
    }

    private void agregarPiePagina(Document doc, Certificado cert) throws Exception {
        doc.add(espaciado(8));

        PdfPTable lineaTable = new PdfPTable(1);
        lineaTable.setWidthPercentage(80);
        PdfPCell lineaCell = new PdfPCell();
        lineaCell.setBorderWidth(0);
        lineaCell.setBorderWidthTop(0.5f);
        lineaCell.setBorderColorTop(COLOR_GRIS_CLARO);
        lineaCell.setFixedHeight(5);
        lineaTable.addCell(lineaCell);
        doc.add(lineaTable);
        doc.add(espaciado(4));

        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(80);
        footerTable.setWidths(new float[]{33, 34, 33});

        Font fontFooter = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_GRIS);
        Font fontFooterBold = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_AZUL_OSCURO);

        // Código
        PdfPCell cellCodigo = new PdfPCell();
        cellCodigo.setBorderWidth(0);
        cellCodigo.addElement(new Paragraph("Código de Verificación:", fontFooter));
        cellCodigo.addElement(new Paragraph(cert.getCodigoCertificado(), fontFooterBold));
        footerTable.addCell(cellCodigo);

        // Fecha
        PdfPCell cellFecha = new PdfPCell();
        cellFecha.setBorderWidth(0);
        cellFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph pFechaLabel = new Paragraph("Fecha de Emisión:", fontFooter);
        pFechaLabel.setAlignment(Element.ALIGN_CENTER);
        cellFecha.addElement(pFechaLabel);
        String fechaFormateada = cert.getFechaEmision();
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            Date fdate = sdfIn.parse(cert.getFechaEmision());
            fechaFormateada = sdfOut.format(fdate);
        } catch (Exception ignored) {}
        Paragraph pFecha = new Paragraph(fechaFormateada, fontFooterBold);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        cellFecha.addElement(pFecha);
        footerTable.addCell(cellFecha);

        // Estado
        PdfPCell cellEstado = new PdfPCell();
        cellEstado.setBorderWidth(0);
        Paragraph pEstadoLabel = new Paragraph("Estado:", fontFooter);
        pEstadoLabel.setAlignment(Element.ALIGN_RIGHT);
        cellEstado.addElement(pEstadoLabel);
        Font fontEstado = new Font(Font.HELVETICA, 9, Font.BOLD,
                "EMITIDO".equals(cert.getEstado()) ? COLOR_VERDE : COLOR_ROJO);
        Paragraph pEstado = new Paragraph(cert.getEstado(), fontEstado);
        pEstado.setAlignment(Element.ALIGN_RIGHT);
        cellEstado.addElement(pEstado);
        footerTable.addCell(cellEstado);

        doc.add(footerTable);
    }

    private Image generarCodigoQR(String texto, int ancho, int alto) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
            BufferedImage qrBuffered = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            ImageIO.write(qrBuffered, "PNG", qrBaos);
            return Image.getInstance(qrBaos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Paragraph espaciado(float puntos) {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(puntos);
        p.add(new Chunk(" "));
        return p;
    }
}
