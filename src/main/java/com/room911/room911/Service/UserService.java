package com.room911.room911.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.room911.room911.Dto.LoginRequest;
import com.room911.room911.Entity.User;
import com.room911.room911.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Instancia local: ya no autowireamos PasswordEncoder
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Usado por loginController
    public Optional<User> autenticarUsuario(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }

        Optional<User> userOptional = userRepository.findByCorreo(correo);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        String hash = user.getContraseña();

        if (hash != null && passwordEncoder.matches(contrasena, hash)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // Usado por UserController
    public Optional<User> login(String correo, String contrasena) {
        return autenticarUsuario(correo, contrasena);
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        if (loginRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solicitud inválida");
        }
        Optional<User> userOptional = autenticarUsuario(loginRequest.getCorreo(), loginRequest.getContrasena());
        if (userOptional.isPresent()) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    public User register(User user) {
        if (user == null || user.getCorreo() == null) {
            throw new IllegalArgumentException("Datos de usuario incompletos");
        }
        if (userRepository.findByCorreo(user.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (user.getContraseña() == null || user.getContraseña().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        user.setContraseña(passwordEncoder.encode(user.getContraseña()));
        return userRepository.save(user);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public Optional<User> findByCorreo(String correo) {
        return userRepository.findByCorreo(correo);
    }
}
