package com.room911.room911.Repository;

import com.room911.room911.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCorreo(String correo);
}
