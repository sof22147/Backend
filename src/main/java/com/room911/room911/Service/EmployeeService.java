// EmployeeService.java
package com.room911.room911.Service;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // ===== CRUD =====

    public Employee registrarEmpleado(Employee empleado) {
        // Tu entidad ya tiene default: activo = true
        return employeeRepository.save(empleado);
    }

    public List<Employee> obtenerTodos() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> obtenerPorId(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee actualizarEmpleado(Long id, Employee in) {
        Employee db = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado con id " + id + " no existe"));

        // Copia de campos
        db.setIdentificacion(in.getIdentificacion());
        db.setNombre(in.getNombre());
        db.setApellido(in.getApellido());
        db.setCargo(in.getCargo());
        db.setDepartamento(in.getDepartamento());
        db.setFechaIngreso(in.getFechaIngreso());
        db.setFechaSalida(in.getFechaSalida());

        // Importante: tu entidad usa boolean => usa isActivo() para leer
        db.setActivo(in.isActivo());

        return employeeRepository.save(db);
    }

    // 👇 NUEVO: habilitar / deshabilitar (solo cambia el flag 'activo')
    public Employee actualizarEstado(Long id, boolean activo) {
        Employee db = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado con id " + id + " no existe"));
        db.setActivo(activo);
        return employeeRepository.save(db);
    }

    // ===== CARGA MASIVA CSV (SIN LIBRERÍAS EXTERNAS) =====
    /**
     * Columnas esperadas (encabezados; may/min indiferente):
     * identificacion,nombre,apellido,cargo,departamento,fechaIngreso,fechaSalida,activo
     *
     * - fechas: yyyy-MM-dd (ej: 2024-01-10). Vacías => null.
     * - activo: true/false (si falta o vacío => true).
     * - Si existe la misma "identificacion", se ACTUALIZA; si no, se CREA.
     */
    public void saveEmployeesFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Por favor, selecciona un archivo CSV.");
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 1) Leer encabezado y mapear índices
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("El CSV está vacío.");
            }
            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(h -> h.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                idx.put(headers.get(i), i);
            }

            // Validar que exista al menos 'identificacion'
            if (!idx.containsKey("identificacion")) {
                throw new IllegalArgumentException("El CSV debe contener la columna 'identificacion'.");
            }

            // 2) Procesar filas
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> cols = parseCsvLine(line);
                String identificacion = get(cols, idx.get("identificacion"));
                if (isBlank(identificacion)) {
                    // Saltar filas sin identificacion
                    continue;
                }

                String nombre       = get(cols, idx.getOrDefault("nombre", -1));
                String apellido     = get(cols, idx.getOrDefault("apellido", -1));
                String cargo        = get(cols, idx.getOrDefault("cargo", -1));
                String departamento = get(cols, idx.getOrDefault("departamento", -1));
                String fIngresoStr  = get(cols, idx.getOrDefault("fechainingreso", -1));
                String fSalidaStr   = get(cols, idx.getOrDefault("fechasalida", -1));
                String activoStr    = get(cols, idx.getOrDefault("activo", -1));

                LocalDate fIngreso = parseDateOrNull(fIngresoStr);
                LocalDate fSalida  = parseDateOrNull(fSalidaStr);
                Boolean activoCsv  = parseBooleanOrNull(activoStr); // null => usar default

                // Buscar existente por 'identificacion' (sin método custom)
                Employee existente = findByIdentificacionLinear(identificacion);

                if (existente != null) {
                    // Actualizar
                    existente.setNombre(nombre);
                    existente.setApellido(apellido);
                    existente.setCargo(cargo);
                    existente.setDepartamento(departamento);
                    existente.setFechaIngreso(fIngreso);
                    existente.setFechaSalida(fSalida);
                    if (activoCsv != null) existente.setActivo(activoCsv);
                    employeeRepository.save(existente);
                } else {
                    // Crear
                    Employee nuevo = new Employee();
                    nuevo.setIdentificacion(identificacion);
                    nuevo.setNombre(nombre);
                    nuevo.setApellido(apellido);
                    nuevo.setCargo(cargo);
                    nuevo.setDepartamento(departamento);
                    nuevo.setFechaIngreso(fIngreso);
                    nuevo.setFechaSalida(fSalida);
                    nuevo.setActivo(activoCsv != null ? activoCsv : true); // default true
                    employeeRepository.save(nuevo);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el CSV: " + e.getMessage(), e);
        }
    }

    // ===== Helpers =====

    // Búsqueda sin método custom en el repositorio (para que compile sin agregar firmas)
    private Employee findByIdentificacionLinear(String identificacion) {
        return employeeRepository.findAll().stream()
                .filter(e -> identificacion.equals(e.getIdentificacion()))
                .findFirst()
                .orElse(null);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static LocalDate parseDateOrNull(String s) {
        if (isBlank(s)) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private static Boolean parseBooleanOrNull(String s) {
        if (isBlank(s)) return null;
        return Boolean.parseBoolean(s.trim());
    }

    private static String get(List<String> cols, int index) {
        if (index < 0 || index >= cols.size()) return null;
        String v = cols.get(index);
        return v != null ? v.trim() : null;
    }

    /**
     * Parser CSV muy simple que soporta comillas dobles.
     * No usa librerías externas para que compile en cualquier proyecto.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                // Doble comilla dentro de comillas => escapa
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // saltar la segunda comilla
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());

        // Quitar comillas exteriores si las hay
        for (int i = 0; i < out.size(); i++) {
            String v = out.get(i);
            if (v != null && v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
                out.set(i, v.substring(1, v.length() - 1));
            }
        }

        return out;
    }
}
