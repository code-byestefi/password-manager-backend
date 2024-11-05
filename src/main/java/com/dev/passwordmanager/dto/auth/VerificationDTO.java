package com.dev.passwordmanager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class VerificationDTO {
    @Data
    public static class CodeVerificationRequest {
        @Email
        private String email;

        @NotBlank
        @Size(min = 6, max = 6)
        private String code;
    }

    @Data
    public static class ResendCodeRequest {
        @Email
        private String email;
    }

    @Data
    @Builder
    public static class VerificationResponse {
        private String message;
        private boolean verified;
    }
}
