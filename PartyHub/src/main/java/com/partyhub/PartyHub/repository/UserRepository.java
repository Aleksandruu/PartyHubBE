package com.partyhub.PartyHub.repository;

import com.partyhub.PartyHub.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(UUID verificationToken);
    boolean existsByPromoCode(String promoCode);
    Optional<User> findByPromoCode(String promoCode);
}
