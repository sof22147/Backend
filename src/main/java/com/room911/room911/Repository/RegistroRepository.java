package com.room911.room911.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room911.room911.Entity.Registro; // Import List

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findByEmployeeId(Long employeeId); // New method to find by employee ID
}
