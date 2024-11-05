package com.dev.passwordmanager.service;

import com.dev.passwordmanager.dto.profile.ChangePasswordRequest;
import com.dev.passwordmanager.dto.profile.ProfileResponse;
import com.dev.passwordmanager.dto.profile.UpdateRequest;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/profiles/";

    public ProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);
        return convertToProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(UpdateRequest request, String currentEmail) {
        User user = getUserByEmail(currentEmail);

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user = userRepository.save(user);
        return convertToProfileResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public ProfileResponse updateProfileImage(MultipartFile image, String email) {
        User user = getUserByEmail(email);
        try {
            // Crear directorio si no existe
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Generar nombre único para el archivo
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            // Guardar archivo
            Files.copy(image.getInputStream(), filePath);

            // Eliminar imagen anterior si existe
            if (user.getProfileImage() != null) {
                try {
                    Files.deleteIfExists(Paths.get(user.getProfileImage()));
                } catch (IOException e) {
                    // Log error pero continuar
                }
            }
            // Actualizar referencia en el usuario
            user.setProfileImage(filePath.toString());
            user = userRepository.save(user);

            return convertToProfileResponse(user);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen del perfil", e);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    private ProfileResponse convertToProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfileImage(user.getProfileImage());
        return response;
    }
}
