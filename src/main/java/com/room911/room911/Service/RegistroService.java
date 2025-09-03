package com.room911.room911.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Import RegistroRequest
import org.springframework.stereotype.Service;

import com.room911.room911.Dto.RegistroRequest;
import com.room911.room911.Entity.Employee;
import com.room911.room911.Entity.Registro;
import com.room911.room911.Repository.EmployeeRepository; // Import List
import com.room911.room911.Repository.RegistroRepository;

@Service
public class RegistroService {

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public Registro createRegistro(RegistroRequest registroRequest) {
        Optional<Employee> employeeOptional = employeeRepository.findById(registroRequest.getEmployeeId());
        if (employeeOptional.isEmpty()) {
            throw new IllegalArgumentException("Employee with ID " + registroRequest.getEmployeeId() + " not found.");
        }
        
        Registro registro = new Registro();
        registro.setEmployee(employeeOptional.get());
        registro.setTipo(registroRequest.getTipo());
        registro.setFechaHora(registroRequest.getFechaHora());

        return registroRepository.save(registro);
    }

    public List<Registro> obtenerTodos() {
        return registroRepository.findAll();
    }

    public List<Registro> obtenerRegistrosPorEmpleadoId(Long employeeId) { // New method
        return registroRepository.findByEmployeeId(employeeId);
    }
}
