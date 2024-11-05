package com.dev.passwordmanager.repository;

import com.dev.passwordmanager.model.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByUserId(Long userId);
    List<PasswordEntry> findByUserIdAndCategoryId(Long userId, Long categoryId);
    List<PasswordEntry> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
}