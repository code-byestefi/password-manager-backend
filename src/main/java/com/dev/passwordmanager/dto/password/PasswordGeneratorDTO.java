package com.dev.passwordmanager.dto.password;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PasswordGeneratorDTO {
    @Min(value = 8, message = "La longitud mínima es 8")
    @Max(value = 100, message = "La longitud máxima es 100")
    private int length = 12;

    private boolean includeUppercase = true;
    private boolean includeLowercase = true;
    private boolean includeNumbers = true;
    private boolean includeSpecialChars = true;
}
