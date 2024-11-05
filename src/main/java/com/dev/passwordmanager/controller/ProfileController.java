package com.dev.passwordmanager.controller;

import com.dev.passwordmanager.dto.profile.ChangePasswordRequest;
import com.dev.passwordmanager.dto.profile.ProfileResponse;
import com.dev.passwordmanager.dto.profile.UpdateRequest;
import com.dev.passwordmanager.model.Photo;
import com.dev.passwordmanager.service.ImageService;
import com.dev.passwordmanager.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Perfil de Usuario", description = "API para gestión del perfil de usuario")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {

    private final ProfileService profileService;
    private final ImageService imageService;

    @Operation(
            summary = "Obtener perfil",
            description = "Obtiene los datos del perfil del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @Operation(
            summary = "Actualizar perfil",
            description = "Actualiza los datos del perfil del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(request, authentication.getName()));
    }

    @Operation(
            summary = "Cambiar contraseña",
            description = "Cambia la contraseña del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        profileService.changePassword(request, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Actualizar imagen de perfil",
            description = "Actualiza la imagen de perfil del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("image") MultipartFile file,
            Authentication authentication) throws IOException, SQLException {
        Photo photo = imageService.savePhoto(file, authentication.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Imagen actualizada correctamente",
                "photoId", photo.getId()
        ));
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) throws SQLException {
        byte[] imageData = imageService.getImageData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageData);
    }

    @GetMapping("/image")
    public ResponseEntity<?> getProfileImage(Authentication authentication) throws SQLException {
        byte[] imageData = imageService.getUserPhoto(authentication.getName());
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageData);
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteProfileImage(Authentication authentication) {
        imageService.deletePhoto(authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Imagen eliminada correctamente"));
    }

}
