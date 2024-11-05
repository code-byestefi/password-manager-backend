package com.dev.passwordmanager.repository;

import com.dev.passwordmanager.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);
    void deleteByEmailAndUsedTrue(String email);
}