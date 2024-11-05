package com.dev.passwordmanager.dto.auth;

import com.dev.passwordmanager.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    @Data
    @Schema(description = "Solicitud de registro de usuario")
    public static class RegisterRequest {
        @Schema(description = "Nombre del usuario", example = "John Doe")
        @NotBlank(message = "El nombre es obligatorio")
        private String name;

        @Schema(description = "Email del usuario", example = "john@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        private String email;

        @Schema(description = "Contraseña del usuario", example = "password123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        private String password;
    }

    @Data
    public static class LoginRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;

    }

    // respuesta
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        @Schema(description = "Token JWT para autenticación")
        private String token;
        @Schema(description = "Email del usuario autenticado")
        private String email;
        @Schema(description = "Nombre del usuario autenticado")
        private String name;
        private User user;
        private boolean requiresVerification;

        public AuthResponse(String token, String email, String name, boolean requiresVerification) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.requiresVerification = requiresVerification;
        }
    }

    @Data
    @Builder
    public static class RegisterResponse {
        private String token;
        private UserDTO user;
        private boolean requiresVerification;
    }
}
