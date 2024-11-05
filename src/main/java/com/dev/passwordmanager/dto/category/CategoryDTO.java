package com.dev.passwordmanager.dto.category;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;
}