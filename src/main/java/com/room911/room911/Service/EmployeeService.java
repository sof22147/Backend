// EmployeeService.java
package com.room911.room911.Service;

import com.room911.room911.Entity.Employee;
import com.room911.room911.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee registrarEmpleado(Employee empleado) {
        // Si no viene el flag desde el front, deja true por defecto
        // (Tu entidad ya tiene default = true)
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
        db.setFechaIngreso(in.getFechaIngreso()); // si manejas nulos, decide tu política
        db.setFechaSalida(in.getFechaSalida());

        // ⬇️ MUY IMPORTANTE: persistir acceso
        db.setActivo(in.isActivo());

        return employeeRepository.save(db);
    }

    // Si la tienes, dejo la firma para CSV:
    public void saveEmployeesFromCsv(MultipartFile file) {
        // tu lógica de parseo…
        // IMPORTANTE: cuando crees Employee desde CSV, si no especificas, que quede activo=true
        // Employee e = new Employee(); e.setActivo(true); …
    }
}
