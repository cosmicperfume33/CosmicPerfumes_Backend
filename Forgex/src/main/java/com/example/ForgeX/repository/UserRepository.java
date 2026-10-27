package com.example.ForgeX.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ForgeX.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUserName(String username);

    Page<User> findByUserNameContainingIgnoreCase(String username, PageRequest pageRequest);

    User findByUserNameContainingIgnoreCase(String username);
}