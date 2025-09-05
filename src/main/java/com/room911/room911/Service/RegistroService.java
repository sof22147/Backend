package com.room911.room911.Service;

import java.time.LocalDateTime; // ⬅️ nuevo si quieres default de fechaHora
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.room911.room911.Dto.RegistroRequest;
import com.room911.room911.Entity.Employee;
import com.room911.room911.Entity.Registro;
import com.room911.room911.Repository.EmployeeRepository;
import com.room911.room911.Repository.RegistroRepository;

@Service
public class RegistroService {

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public Registro createRegistro(RegistroRequest registroRequest) {
        // 1) Buscar empleado
        Optional<Employee> employeeOptional = employeeRepository.findById(registroRequest.getEmployeeId());
        if (employeeOptional.isEmpty()) {
            throw new IllegalArgumentException("Employee with ID " + registroRequest.getEmployeeId() + " not found.");
        }

        Employee employee = employeeOptional.get();

        // 2) BLOQUEAR si no tiene acceso
        //    (recuerda haber añadido el boolean `activo` en Employee)
        if (!employee.isActivo()) {
            // Puedes usar IllegalStateException o una custom
            throw new IllegalStateException("El empleado no tiene acceso para registrar turnos.");
        }

        // 3) Crear el registro
        Registro registro = new Registro();
        registro.setEmployee(employee);
        registro.setTipo(registroRequest.getTipo());

        // Si tu DTO a veces viene sin fechaHora, coloca la actual por defecto:
        if (registroRequest.getFechaHora() == null) {
            registro.setFechaHora(LocalDateTime.now());
        } else {
            registro.setFechaHora(registroRequest.getFechaHora());
        }

        return registroRepository.save(registro);
    }

    public List<Registro> obtenerTodos() {
        return registroRepository.findAll();
    }

    public List<Registro> obtenerRegistrosPorEmpleadoId(Long employeeId) {
        return registroRepository.findByEmployeeId(employeeId);
    }
}
