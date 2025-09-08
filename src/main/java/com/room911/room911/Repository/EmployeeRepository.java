package com.room911.room911.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.room911.room911.Entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByIdentificacion(String identificacion);

}
