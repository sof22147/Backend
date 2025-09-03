package com.room911.room911.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String identificacion;
    private String cargo;
    private String departamento;
    @Column(nullable = true)
    private LocalDate fechaIngreso;
    @Column(nullable = true)
    private LocalDate fechaSalida;
}
