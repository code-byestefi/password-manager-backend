package com.dev.passwordmanager.service;

import com.dev.passwordmanager.dto.password.PasswordEntryDTO;
import com.dev.passwordmanager.exception.ResourceNotFoundException;
import com.dev.passwordmanager.model.Category;
import com.dev.passwordmanager.model.PasswordEntry;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.CategoryRepository;
import com.dev.passwordmanager.repository.PasswordEntryRepository;
import com.dev.passwordmanager.repository.UserRepository;
import com.dev.passwordmanager.utils.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public PasswordEntryDTO.Response createPassword(PasswordEntryDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PasswordEntry passwordEntry = new PasswordEntry();
        passwordEntry.setName(dto.getName());
        passwordEntry.setUsername(dto.getUsername());
        passwordEntry.setPassword(encryptionUtil.encrypt(dto.getPassword())); // Usando la nueva utilidad
        passwordEntry.setWebsite(dto.getWebsiteUrl());
        passwordEntry.setNotes(dto.getNotes());
        passwordEntry.setUser(user);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }
            passwordEntry.setCategory(category);
        }

        passwordEntry = passwordEntryRepository.save(passwordEntry);
        return convertToResponse(passwordEntry);
    }

    public List<PasswordEntryDTO.Response> getUserPasswords(String userEmail, Long categoryId, String search) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<PasswordEntry> passwords;
        if (categoryId != null) {
            passwords = passwordEntryRepository.findByUserIdAndCategoryId(user.getId(), categoryId);
        } else if (search != null && !search.trim().isEmpty()) {
            passwords = passwordEntryRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), search);
        } else {
            passwords = passwordEntryRepository.findByUserId(user.getId());
        }

        return passwords.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public PasswordEntryDTO.Response getPassword(Long id, String userEmail) {
        PasswordEntry passwordEntry = getPasswordEntryByIdAndEmail(id, userEmail);
        return convertToResponse(passwordEntry);
    }

    public String getDecryptedPassword(Long id, String userEmail) {
        PasswordEntry passwordEntry = getPasswordEntryByIdAndEmail(id, userEmail);
        return encryptionUtil.decrypt(passwordEntry.getPassword());
    }

    @Transactional
    public PasswordEntryDTO.Response updatePassword(Long id, PasswordEntryDTO dto, String userEmail) {
        PasswordEntry passwordEntry = getPasswordEntryByIdAndEmail(id, userEmail);

        passwordEntry.setName(dto.getName());
        passwordEntry.setUsername(dto.getUsername());
        if (dto.getPassword() != null) {
            passwordEntry.setPassword(encryptionUtil.encrypt(dto.getPassword()));
        }
        passwordEntry.setWebsite(dto.getWebsiteUrl());
        passwordEntry.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            if (!category.getUser().getId().equals(passwordEntry.getUser().getId())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }
            passwordEntry.setCategory(category);
        } else {
            passwordEntry.setCategory(null);
        }

        passwordEntry = passwordEntryRepository.save(passwordEntry);
        return convertToResponse(passwordEntry);
    }

    @Transactional
    public void deletePassword(Long id, String userEmail) {
        PasswordEntry passwordEntry = getPasswordEntryByIdAndEmail(id, userEmail);
        passwordEntryRepository.delete(passwordEntry);
    }

    private PasswordEntry getPasswordEntryByIdAndEmail(Long id, String userEmail) {
        PasswordEntry passwordEntry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contraseña no encontrada"));

        if (!passwordEntry.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("No autorizado para acceder a esta contraseña");
        }

        return passwordEntry;
    }

    private PasswordEntryDTO.Response convertToResponse(PasswordEntry passwordEntry) {
        PasswordEntryDTO.Response response = new PasswordEntryDTO.Response();
        response.setId(passwordEntry.getId());
        response.setName(passwordEntry.getName());
        response.setUsername(passwordEntry.getUsername());
        response.setWebsiteUrl(passwordEntry.getWebsite());
        response.setNotes(passwordEntry.getNotes());

        if (passwordEntry.getCategory() != null) {
            response.setCategoryId(passwordEntry.getCategory().getId());
            response.setCategoryName(passwordEntry.getCategory().getName());
        }

        return response;
    }


}
