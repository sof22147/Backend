package com.room911.room911.Dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RegistroRequest {
    private Long employeeId;
    private String tipo;
    private LocalDateTime fechaHora;
}
