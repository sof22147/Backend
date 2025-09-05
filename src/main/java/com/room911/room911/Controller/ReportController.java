package com.room911.room911.Controller;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Entity.Registro;
import com.room911.room911.Service.EmployeeService;
import com.room911.room911.Service.RegistroService;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

// 🔽 NUEVOS IMPORTS
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RegistroService registroService;

    // 🔽 NUEVO: Acepta "yyyy-MM-dd'T'HH:mm" y opcionalmente ":ss"
    private static final DateTimeFormatter LOCAL_NO_TZ_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .toFormatter();

    // =========================
    // 1) Reporte de Empleados (PDF bonito con tabla)
    // =========================
    @GetMapping(value = "/employees/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateEmployeesPdf() {
        List<Employee> employees = employeeService.obtenerTodos();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 54); // márgenes
            PdfWriter writer = PdfWriter.getInstance(document, bos);

            String today = LocalDate.now().toString();
            writer.setPageEvent(new HeaderFooter("Room 911 · Reporte de Empleados", today));

            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(17,24,39));
            Paragraph title = new Paragraph("Reporte de Empleados", titleFont);
            title.setSpacingAfter(8f);
            document.add(title);

            // Meta
            Font metaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph meta = new Paragraph("Generado: " + today + "   ·   Total: " + employees.size(), metaFont);
            meta.setSpacingAfter(12f);
            document.add(meta);

            // Tabla
            PdfPTable table = new PdfPTable(new float[]{10, 18, 18, 18, 18, 18});
            table.setWidthPercentage(100);

            table.addCell(th("ID"));
            table.addCell(th("Cédula"));
            table.addCell(th("Nombre"));
            table.addCell(th("Apellido"));
            table.addCell(th("Cargo"));
            table.addCell(th("Departamento"));

            BaseColor zebra = new BaseColor(249, 250, 251); // #f9fafb
            IntStream.range(0, employees.size()).forEach(i -> {
                Employee e = employees.get(i);
                PdfPCell[] row = new PdfPCell[]{
                        td(e.getId() == null ? "" : String.valueOf(e.getId())),
                        td(safe(e.getIdentificacion())),
                        td(safe(e.getNombre())),
                        td(safe(e.getApellido())),
                        td(safe(e.getCargo())),
                        td(safe(e.getDepartamento()))
                };
                if (i % 2 == 0) for (PdfPCell c : row) c.setBackgroundColor(zebra);
                for (PdfPCell c : row) table.addCell(c);
            });

            document.add(table);
            document.close();

            byte[] bytes = bos.toByteArray();
            return buildPdfResponse(bytes, "reporte_empleados.pdf");

        } catch (Exception e) {
            return errorResponse("Error al generar el PDF: " + e.getMessage());
        }
    }

    // =======================================
    // 2) Reporte de Registros (Shifts/Turnos) — tabla bonita + FILTRO POR RANGO
    //    /api/reports/shifts/pdf?from=2025-09-01T08:00[:00]&to=2025-09-01T18:00[:00]
    // =======================================
    @GetMapping(value = "/shifts/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateShiftsPdf(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to",   required = false) String to
    ) {
        List<Registro> registros = registroService.obtenerTodos();

        // Parseo y filtrado opcional del rango (SIN zona)
        LocalDateTime fromDt = null, toDt = null;
        try {
            if (from != null && !from.isBlank()) fromDt = LocalDateTime.parse(from, LOCAL_NO_TZ_FORMATTER);
            if (to   != null && !to.isBlank())   toDt   = LocalDateTime.parse(to,   LOCAL_NO_TZ_FORMATTER);
        } catch (DateTimeParseException ex) {
            return errorResponse("Formato de fecha inválido. Usa YYYY-MM-DDTHH:mm o YYYY-MM-DDTHH:mm:ss");
        }

        if (fromDt != null || toDt != null) {
            final LocalDateTime f = fromDt;
            final LocalDateTime t = toDt;
            registros = registros.stream()
                    .filter(r -> {
                        LocalDateTime fh = r.getFechaHora();
                        if (fh == null) return false;
                        boolean okFrom = (f == null) || !fh.isBefore(f);
                        boolean okTo   = (t == null) || !fh.isAfter(t);
                        return okFrom && okTo;
                    })
                    .collect(Collectors.toList());
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, bos);

            String today = LocalDate.now().toString();
            writer.setPageEvent(new HeaderFooter("Room 911 · Reporte de Registros de Turno", today));

            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(17,24,39));
            Paragraph title = new Paragraph("Reporte de Registros de Turno", titleFont);
            title.setSpacingAfter(8f);
            document.add(title);

            // Meta
            Font metaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph meta = new Paragraph("Generado: " + today + "   ·   Total: " + registros.size(), metaFont);
            meta.setSpacingAfter(6f);
            document.add(meta);

            // Si hay filtro, lo mostramos
            if (fromDt != null || toDt != null) {
                String rango = String.format("Filtro — Desde: %s   ·   Hasta: %s",
                        fromDt != null ? fromDt.toString() : "—",
                        toDt   != null ? toDt.toString()   : "—");
                Paragraph pFiltro = new Paragraph(rango, metaFont);
                pFiltro.setSpacingAfter(12f);
                document.add(pFiltro);
            } else {
                Paragraph spacer = new Paragraph(" ", metaFont);
                spacer.setSpacingAfter(8f);
                document.add(spacer);
            }

            // Tabla
            PdfPTable table = new PdfPTable(new float[]{14, 14, 22, 22, 14, 24});
            table.setWidthPercentage(100);

            table.addCell(th("ID Reg."));
            table.addCell(th("ID Emp."));
            table.addCell(th("Nombre"));
            table.addCell(th("Apellido"));
            table.addCell(th("Tipo"));
            table.addCell(th("Fecha/Hora"));

            BaseColor zebra = new BaseColor(249, 250, 251);
            for (int i = 0; i < registros.size(); i++) {
                Registro reg = registros.get(i);
                String nombre    = reg.getEmployee() != null ? safe(reg.getEmployee().getNombre())   : "N/A";
                String apellido  = reg.getEmployee() != null ? safe(reg.getEmployee().getApellido()) : "N/A";
                String empId     = (reg.getEmployee() != null && reg.getEmployee().getId() != null)
                        ? reg.getEmployee().getId().toString() : "0";

                PdfPCell[] row = new PdfPCell[]{
                        td(reg.getId() == null ? "" : reg.getId().toString()),
                        td(empId),
                        td(nombre),
                        td(apellido),
                        td(safe(reg.getTipo())),
                        td(reg.getFechaHora() != null ? reg.getFechaHora().toString() : "")
                };
                if (i % 2 == 0) for (PdfPCell c : row) c.setBackgroundColor(zebra);
                for (PdfPCell c : row) table.addCell(c);
            }

            document.add(table);
            document.close();

            byte[] bytes = bos.toByteArray();
            String filename = (fromDt != null || toDt != null)
                    ? "reporte_registros_turno_rango.pdf"
                    : "reporte_registros_turno.pdf";
            return buildPdfResponse(bytes, filename);

        } catch (Exception e) {
            return errorResponse("Error al generar el PDF: " + e.getMessage());
        }
    }

    // ===================================================
    // 3) Historial de Turnos por Empleado (por employeeId)
    // ===================================================
    @GetMapping(value = "/employee/{employeeId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateEmployeeHistoryPdf(@PathVariable Long employeeId) {
        Optional<Employee> employeeOptional = employeeService.obtenerPorId(employeeId);
        if (employeeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Employee employee = employeeOptional.get();
        List<Registro> registros = registroService.obtenerRegistrosPorEmpleadoId(employeeId);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, bos);
            document.open();

            document.add(new Paragraph(String.format(
                    "Historial de Turnos para: %s %s (ID: %d)",
                    safe(employee.getNombre()),
                    safe(employee.getApellido()),
                    employee.getId()
            )));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------"));
            document.add(new Paragraph("ID Registro | Tipo de Registro | Fecha y Hora"));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------"));

            if (registros.isEmpty()) {
                document.add(new Paragraph("Sin registros aún"));
            } else {
                for (Registro reg : registros) {
                    document.add(new Paragraph(String.format("%d | %s | %s",
                            reg.getId(),
                            safe(reg.getTipo()),
                            reg.getFechaHora() != null ? reg.getFechaHora().toString() : ""
                    )));
                }
            }
            document.close();

            byte[] bytes = bos.toByteArray();
            String fileName = String.format("historial_turnos_%d.pdf", employeeId);
            return buildPdfResponse(bytes, fileName);

        } catch (DocumentException e) {
            return errorResponse("Error al generar el PDF: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse("Ha ocurrido un error inesperado al generar el PDF.");
        }
    }

    // =========================
    // Helpers (reusados)
    // =========================

    private ResponseEntity<byte[]> buildPdfResponse(byte[] bytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private ResponseEntity<byte[]> errorResponse(String message) {
        return ResponseEntity
                .status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body(message.getBytes(StandardCharsets.UTF_8));
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    // ======= Helpers para tablas bonitas y header/footer =======

    private PdfPCell th(String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Paragraph(text, f));
        cell.setBackgroundColor(new BaseColor(17, 24, 39)); // #111827
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(8f);
        cell.setBorderWidth(0);
        return cell;
    }

    private PdfPCell td(String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Paragraph(text == null ? "" : text, f));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderWidth(0);
        return cell;
    }

    // Cabecera y pie (fecha y “Página N”)
    static class HeaderFooter extends PdfPageEventHelper {
        private final Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        private final Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        private final String leftHeader;
        private final String rightHeader;

        HeaderFooter(String leftHeader, String rightHeader) {
            this.leftHeader = leftHeader;
            this.rightHeader = rightHeader;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle page = document.getPageSize();
            PdfContentByte cb = writer.getDirectContent();
            // Header
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Paragraph(leftHeader, headerFont),
                    document.left(), page.getTop() - 20, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Paragraph(rightHeader, headerFont),
                    document.right(), page.getTop() - 20, 0);
            // Footer (Página N)
            String text = String.format("Página %d", writer.getPageNumber());
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Paragraph(text, footerFont),
                    (page.getLeft() + page.getRight()) / 2, page.getBottom() + 20, 0);
        }
    }
}
