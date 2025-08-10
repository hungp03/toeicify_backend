package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT 1", nativeQuery = true)
    Integer ping();
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
        @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    Page<User> findByUsernameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);
}
