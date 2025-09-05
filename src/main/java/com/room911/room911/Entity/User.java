package com.room911.room911.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Jackson
import com.fasterxml.jackson.annotation.JsonProperty;

// OpenAPI/Swagger
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correo;

    // ⬇⬇⬇ EL CAMBIO CLAVE
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)     // no sale en respuestas
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)         // Swagger: solo como entrada
    private String contraseña;
    // ⬆⬆⬆

    private String rol;

    // Getters y setters
    public Long getId() { return id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
