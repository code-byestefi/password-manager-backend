package com.dev.passwordmanager.dto.password;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordEntryDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String websiteUrl;
    private String notes;
    private Long categoryId;

    public PasswordEntryDTO() {

    }

    // DTO para respuestas (sin mostrar la contraseña)
    @Data
    public static class Response {
        private Long id;
        private String name;
        private String username;
        private String websiteUrl;
        private String notes;
        private Long categoryId;
        private String categoryName;
    }
}
