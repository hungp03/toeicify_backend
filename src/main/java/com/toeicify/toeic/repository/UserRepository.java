package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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
    @Query(
            value = """
        SELECT * 
        FROM users u
        WHERE 
            unaccent(lower(u.full_name)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
            OR unaccent(lower(u.username)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
            OR unaccent(lower(u.email)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
        """,
            countQuery = """
        SELECT count(*) 
        FROM users u
        WHERE 
            unaccent(lower(u.full_name)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
            OR unaccent(lower(u.username)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
            OR unaccent(lower(u.email)) LIKE unaccent(lower(CONCAT('%', :searchTerm, '%')))
        """,
            nativeQuery = true
    )
    Page<User> findByUsernameOrEmail(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.registrationDate > :start")
    long countByRegistrationDateAfter(@Param("start") Instant start);

    @Query("SELECT COUNT(u) FROM User u WHERE u.registrationDate BETWEEN :start AND :end")
    long countByRegistrationDateBetween(@Param("start") Instant start, @Param("end") Instant end);

    List<User> findTop1ByOrderByRegistrationDateDesc();

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.userId = :userId AND u.isActive = true")
    boolean isUserActive(@Param("userId") Long userId);

    @Query(value = """
        SELECT DATE_TRUNC('month', u.registration_date) as month, COUNT(u.user_id) as user_count
        FROM users u
        WHERE u.registration_date >= :start AND u.registration_date < :end
        GROUP BY DATE_TRUNC('month', u.registration_date)
        ORDER BY month DESC
    """, nativeQuery = true)

    List<Object[]> countUsersByMonth(@Param("start") Instant start, @Param("end") Instant end);
        @Query(value = """
        SELECT DATE_TRUNC('day', u.registration_date) as day, COUNT(u.user_id) as user_count
        FROM users u
        WHERE u.registration_date >= :start AND u.registration_date < :end
        GROUP BY DATE_TRUNC('day', u.registration_date)
        ORDER BY day DESC
    """, nativeQuery = true)
        List<Object[]> countUsersByDay(@Param("start") Instant start, @Param("end") Instant end);
}
