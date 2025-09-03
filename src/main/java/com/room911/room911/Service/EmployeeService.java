package com.room911.room911.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository empleadoRepository;

    public Employee registrarEmpleado(Employee empleado) {
        if (empleadoRepository.existsByIdentificacion(empleado.getIdentificacion())) {
            throw new IllegalArgumentException("El empleado ya está registrado.");
        }
        return empleadoRepository.save(empleado);
    }

    public List<Employee> obtenerTodos() {
        return empleadoRepository.findAll();
    }

    public Optional<Employee> obtenerPorId(Long id) { // New method to get employee by ID
        return empleadoRepository.findById(id);
    }

    public Employee actualizarEmpleado(Long id, Employee empleadoActualizado) { // New method to update employee
        return empleadoRepository.findById(id)
                .map(empleadoExistente -> {
                    empleadoExistente.setNombre(empleadoActualizado.getNombre());
                    empleadoExistente.setApellido(empleadoActualizado.getApellido());
                    empleadoExistente.setIdentificacion(empleadoActualizado.getIdentificacion());
                    empleadoExistente.setCargo(empleadoActualizado.getCargo());
                    empleadoExistente.setDepartamento(empleadoActualizado.getDepartamento());
                    // No actualizar fechaIngreso/fechaSalida a menos que se especifique
                    return empleadoRepository.save(empleadoExistente);
                })
                .orElseThrow(() -> new IllegalArgumentException("Empleado con ID " + id + " no encontrado."));
    }

    public void saveEmployeesFromCsv(MultipartFile file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            reader.readLine(); // Skip header row
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // Assuming comma as delimiter
                if (data.length >= 5) { // Ensure enough columns for basic employee data
                    Employee employee = new Employee();
                    // Assuming CSV format: identificacion,nombre,apellido,cargo,departamento
                    employee.setIdentificacion(data[0].trim());
                    employee.setNombre(data[1].trim());
                    employee.setApellido(data[2].trim());
                    employee.setCargo(data[3].trim());
                    employee.setDepartamento(data[4].trim());
                    // Add more fields if necessary, handling potential parsing errors (e.g., for dates, numbers)
                    employees.add(employee);
                }
            }
            empleadoRepository.saveAll(employees);
        } catch (Exception e) {
            throw new Exception("Fallo al parsear el archivo CSV: " + e.getMessage());
        }
    }
}
