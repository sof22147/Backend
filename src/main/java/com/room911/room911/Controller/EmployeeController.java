package com.room911.room911.Controller;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleados")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarEmpleado(@RequestBody Employee empleado) {
        try {
            Employee nuevo = employeeService.registrarEmpleado(empleado);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/todos")
    public List<Employee> obtenerTodos() {
        return employeeService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> obtenerEmpleadoPorId(@PathVariable Long id) {
        Optional<Employee> empleado = employeeService.obtenerPorId(id);
        return empleado.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEmpleado(@PathVariable Long id, @RequestBody Employee empleado) {
        try {
            Employee empleadoActualizado = employeeService.actualizarEmpleado(id, empleado);
            return ResponseEntity.ok(empleadoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 👇 NUEVO: habilitar / deshabilitar empleado (solo cambia el campo 'activo')
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoEmpleado(@PathVariable Long id,
                                                      @RequestParam boolean activo) {
        try {
            Employee actualizado = employeeService.actualizarEstado(id, activo);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 👇 IMPORTANTE: especificar que consume multipart/form-data
    @PostMapping(value = "/upload-csv", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Por favor, selecciona un archivo CSV para subir."));
        }
        try {
            employeeService.saveEmployeesFromCsv(file);
            return ResponseEntity.ok(Map.of("message", "Empleados importados exitosamente desde el CSV."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo CSV: " + e.getMessage()));
        }
    }
}
