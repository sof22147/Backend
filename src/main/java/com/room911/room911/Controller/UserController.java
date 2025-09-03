package com.room911.room911.Controller;

import com.room911.room911.Entity.User;
import com.room911.room911.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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


@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody User user) {
    try {
        User newUser = userService.register(user);
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
