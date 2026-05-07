package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(User.Role role);
    java.util.List<User> findByRoleOrderByCreatedAtDesc(User.Role role);
    long countByRoleAndCreatedAtBetween(User.Role role, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
