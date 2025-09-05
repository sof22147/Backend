package com.room911.room911.Controller;

import com.room911.room911.Entity.User;
import com.room911.room911.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// ⬇️ AÑADIDOS
import org.springframework.security.crypto.password.PasswordEncoder;
// ⬆️ AÑADIDOS

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ⬇️ AÑADIDO: PasswordEncoder para hashear contraseñas
    @Autowired
    private PasswordEncoder passwordEncoder;
    // ⬆️ AÑADIDO

    // ====== LOGIN (GET) — DEJAMOS TU MÉTODO TAL CUAL ======
    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String correo, @RequestParam String contrasena) {
        return userService.login(correo, contrasena)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("correo", user.getCorreo());
                    response.put("rol", user.getRol());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Credenciales inválidas");
                    return ResponseEntity.status(401).body(errorResponse);
                });
    }

    // ====== LOGIN (POST) — AÑADIDO, SIN QUITAR EL GET ======
    @PostMapping("/login")
    public ResponseEntity<?> loginPost(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contraseña"); // acepta "contraseña" con ñ
        if (contrasena == null) {
            // por si lo envían como "contrasena" (sin ñ)
            contrasena = body.get("contrasena");
        }

        if (correo == null || contrasena == null || correo.isBlank() || contrasena.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos: correo y contraseña"));
        }

        return userService.login(correo, contrasena)
                .map(user -> ResponseEntity.ok(Map.of(
                        "correo", user.getCorreo(),
                        "rol", user.getRol()
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of(
                        "error", "Credenciales inválidas"
                )));
    }

    // ====== REGISTER (POST) — DEJAMOS TU MÉTODO Y HASHEAMOS ======
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // 🔒 Encriptar la contraseña ANTES de enviarla al servicio/repositorio
            if (user.getContraseña() != null && !user.getContraseña().isBlank()) {
                user.setContraseña(passwordEncoder.encode(user.getContraseña()));
            }
            User newUser = userService.register(user);

            // Nunca devolvemos la contraseña gracias a @JsonProperty(WRITE_ONLY)
            return ResponseEntity.status(201).body(Map.of(
                    "correo", newUser.getCorreo(),
                    "rol", newUser.getRol()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // ====== TEST DB — DEJAMOS TU MÉTODO TAL CUAL ======
    @GetMapping("/test-db-connection")
    public ResponseEntity<?> testDbConnection() {
        try {
            long userCount = userService.getUserCount();
            return ResponseEntity.ok("Conexión a la base de datos exitosa. Número de usuarios: " + userCount);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}
