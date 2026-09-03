package io.hatefulbug.marketplaceapi.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hatefulbug.marketplaceapi.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

}
