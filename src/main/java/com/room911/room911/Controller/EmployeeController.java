package com.room911.room911.Controller;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.util.List;
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
            return ResponseEntity.status(201).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/todos")
    public List<Employee> obtenerTodos() {
        return employeeService.obtenerTodos();
    }

    @GetMapping("/{id}") // New GET endpoint by ID
    public ResponseEntity<Employee> obtenerEmpleadoPorId(@PathVariable Long id) {
        Optional<Employee> empleado = employeeService.obtenerPorId(id);
        return empleado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}") // New PUT endpoint to update employee
    public ResponseEntity<?> actualizarEmpleado(@PathVariable Long id, @RequestBody Employee empleado) {
        try {
            Employee empleadoActualizado = employeeService.actualizarEmpleado(id, empleado);
            return ResponseEntity.ok(empleadoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Por favor, selecciona un archivo CSV para subir.", HttpStatus.BAD_REQUEST);
        }
        try {
            employeeService.saveEmployeesFromCsv(file);
            return new ResponseEntity<>("Empleados importados exitosamente desde el CSV.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar el archivo CSV: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
