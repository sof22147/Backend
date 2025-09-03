package com.room911.room911.Controller;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Entity.Registro;
import com.room911.room911.Service.EmployeeService;
import com.room911.room911.Service.RegistroService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RegistroService registroService;

    // =========================
    // 1) Reporte de Empleados
    // =========================
    @GetMapping(value = "/employees/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateEmployeesPdf() {
        List<Employee> employees = employeeService.obtenerTodos();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, bos);
            document.open();

            document.add(new Paragraph("Reporte de Empleados"));
            document.add(new Paragraph("--------------------"));
            document.add(new Paragraph("ID | Identificacion | Nombre | Apellido | Cargo | Departamento | Fecha Ingreso | Fecha Salida"));
            document.add(new Paragraph("------------------------------------------------------------------------------------------------"));

            for (Employee emp : employees) {
                document.add(new Paragraph(String.format("%d | %s | %s | %s | %s | %s | %s | %s",
                        emp.getId(),
                        safe(emp.getIdentificacion()),
                        safe(emp.getNombre()),
                        safe(emp.getApellido()),
                        safe(emp.getCargo()),
                        safe(emp.getDepartamento()),
                        emp.getFechaIngreso() != null ? emp.getFechaIngreso().toString() : "",
                        emp.getFechaSalida() != null ? emp.getFechaSalida().toString() : ""
                )));
            }
            document.close();

            byte[] bytes = bos.toByteArray();
            return buildPdfResponse(bytes, "reporte_empleados.pdf");

        } catch (DocumentException e) {
            return errorResponse("Error al generar el PDF: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse("Ha ocurrido un error inesperado al generar el PDF.");
        }
    }

    // =======================================
    // 2) Reporte de Registros (Shifts/Turnos)
    // =======================================
    @GetMapping(value = "/shifts/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateShiftsPdf() {
        List<Registro> registros = registroService.obtenerTodos();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, bos);
            document.open();

            document.add(new Paragraph("Reporte de Registros de Turno"));
            document.add(new Paragraph("-----------------------------"));
            document.add(new Paragraph("ID Registro | ID Empleado | Nombre Empleado | Apellido Empleado | Tipo de Registro | Fecha y Hora"));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------"));

            for (Registro reg : registros) {
                String employeeName = (reg.getEmployee() != null) ? safe(reg.getEmployee().getNombre()) : "N/A";
                String employeeApellido = (reg.getEmployee() != null) ? safe(reg.getEmployee().getApellido()) : "N/A";
                Long employeeId = (reg.getEmployee() != null) ? reg.getEmployee().getId() : 0L;

                document.add(new Paragraph(String.format("%d | %d | %s | %s | %s | %s",
                        reg.getId(),
                        employeeId,
                        employeeName,
                        employeeApellido,
                        safe(reg.getTipo()),
                        reg.getFechaHora() != null ? reg.getFechaHora().toString() : ""
                )));
            }
            document.close();

            byte[] bytes = bos.toByteArray();
            return buildPdfResponse(bytes, "reporte_registros_turno.pdf");

        } catch (DocumentException e) {
            return errorResponse("Error al generar el PDF: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse("Ha ocurrido un error inesperado al generar el PDF.");
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
    // Helpers
    // =========================

    private ResponseEntity<byte[]> buildPdfResponse(byte[] bytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename)
                        .build()
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
}
