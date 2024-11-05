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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {

    @Mock
    private PasswordEntryRepository passwordEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EncryptionUtil encryptionUtil;


    @InjectMocks
    private PasswordService passwordService;

    private User testUser;
    private Category testCategory;
    private PasswordEntry testPasswordEntry;
    private PasswordEntryDTO testPasswordDTO;

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setName("Test User");

        // Configurar categoría de prueba
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setUser(testUser);

        // Configurar entrada de contraseña de prueba
        testPasswordEntry = new PasswordEntry();
        testPasswordEntry.setId(1L);
        testPasswordEntry.setName("Test Password");
        testPasswordEntry.setUsername("testuser");
        testPasswordEntry.setPassword("encryptedPassword");
        testPasswordEntry.setUser(testUser);
        testPasswordEntry.setCategory(testCategory);

        // Configurar DTO de prueba
        testPasswordDTO = new PasswordEntryDTO();
        testPasswordDTO.setName("Test Password");
        testPasswordDTO.setUsername("testuser");
        testPasswordDTO.setPassword("password123");
        testPasswordDTO.setCategoryId(testCategory.getId());
    }

    @Test
    void createPassword_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encryptedPassword");
        when(passwordEntryRepository.save(any(PasswordEntry.class))).thenReturn(testPasswordEntry);

        // When
        PasswordEntryDTO.Response response = passwordService.createPassword(testPasswordDTO, "test@test.com");

        // Then
        assertNotNull(response);
        assertEquals(testPasswordDTO.getName(), response.getName());
        assertEquals(testPasswordDTO.getUsername(), response.getUsername());
        assertEquals(testCategory.getId(), response.getCategoryId());
    }



    @Test
    void createPassword_UserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () ->
                passwordService.createPassword(testPasswordDTO, "nonexistent@test.com")
        );
        verify(passwordEntryRepository, never()).save(any());
    }

    @Test
    void getUserPasswords_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEntryRepository.findByUserId(anyLong()))
                .thenReturn(Arrays.asList(testPasswordEntry));

        // When
        List<PasswordEntryDTO.Response> responses = passwordService.getUserPasswords(
                "test@test.com", null, null);

        // Then
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(testPasswordEntry.getName(), responses.get(0).getName());
    }

    @Test
    void getUserPasswords_WithCategoryFilter() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEntryRepository.findByUserIdAndCategoryId(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(testPasswordEntry));

        // When
        List<PasswordEntryDTO.Response> responses = passwordService.getUserPasswords(
                "test@test.com", testCategory.getId(), null);

        // Then
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(testCategory.getId(), responses.get(0).getCategoryId());
        assertEquals(testPasswordEntry.getName(), responses.get(0).getName());
    }


    @Test
    void getUserPasswords_WithSearch() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEntryRepository.findByUserIdAndNameContainingIgnoreCase(anyLong(), anyString()))
                .thenReturn(Arrays.asList(testPasswordEntry));

        // When
        List<PasswordEntryDTO.Response> responses = passwordService.getUserPasswords(
                "test@test.com", null, "Test");

        // Then
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getName().contains("Test"));
    }

    @Test
    void deletePassword_Success() {
        // Given
        when(passwordEntryRepository.findById(anyLong())).thenReturn(Optional.of(testPasswordEntry));

        // When
        passwordService.deletePassword(1L, "test@test.com");

        // Then
        verify(passwordEntryRepository).delete(testPasswordEntry);
    }

    @Test
    void deletePassword_NotFound() {
        // Given
        when(passwordEntryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () ->
                passwordService.deletePassword(1L, "test@test.com")
        );
        verify(passwordEntryRepository, never()).delete(any());
    }


}
