package com.room911.room911.Controller;

import com.room911.room911.Dto.LoginRequest;
import com.room911.room911.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class loginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Received login request for correo: " + loginRequest.getCorreo());
        return userService.authenticateUser(loginRequest);
    }
}
