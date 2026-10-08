package com.wallet.auth_service.repository;

import com.wallet.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.failedLoginAttempts = u.failedLoginAttempts + 1 where u.email = :email")
    int incrementFailedAttempts(@Param("email") String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update User u
               set u.lockedUntil = :until, u.failedLoginAttempts = 0
             where u.email = :email and u.failedLoginAttempts >= :max
            """)
    int lockIfThresholdReached(@Param("email") String email, @Param("max") int max, @Param("until") Instant until);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.failedLoginAttempts = 0, u.lockedUntil = null where u.email = :email")
    int resetFailedAttempts(@Param("email") String email);
}
