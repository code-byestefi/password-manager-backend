package com.dev.passwordmanager.dto.password;

import lombok.Data;

@Data
public class GeneratedPasswordDTO {

    private String password;
    private PasswordStrength strength;
    private int score; // 0-100
}
