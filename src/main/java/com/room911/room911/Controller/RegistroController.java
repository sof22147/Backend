package com.room911.room911.Controller;

import com.room911.room911.Entity.Registro;
import com.room911.room911.Dto.RegistroRequest; // Import RegistroRequest
import com.room911.room911.Service.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Import List

@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    @Autowired
    private RegistroService registroService;

    @PostMapping
    public ResponseEntity<?> createRegistro(@RequestBody RegistroRequest registroRequest) {
        try {
            Registro nuevoRegistro = registroService.createRegistro(registroRequest);
            return ResponseEntity.status(201).body(nuevoRegistro);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al registrar turno: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Registro> obtenerTodos() {
        return registroService.obtenerTodos();
    }

    @GetMapping("/empleado/{employeeId}") // New GET endpoint for employee history
    public ResponseEntity<List<Registro>> obtenerRegistrosPorEmpleadoId(@PathVariable Long employeeId) {
        List<Registro> registros = registroService.obtenerRegistrosPorEmpleadoId(employeeId);
        if (registros.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(registros);
    }
}
