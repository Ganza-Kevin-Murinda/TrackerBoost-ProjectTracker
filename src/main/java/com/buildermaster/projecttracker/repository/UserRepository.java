package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String email);

    boolean existsByUsername(String email);

    @Query("SELECT u FROM User u")
    List<User> findAllUserDetails();
}
