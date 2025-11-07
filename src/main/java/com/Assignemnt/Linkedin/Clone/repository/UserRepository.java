package com.Assignemnt.Linkedin.Clone.repository;

import com.Assignemnt.Linkedin.Clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
