package com.dev.passwordmanager.controller;


import com.dev.passwordmanager.dto.password.GeneratedPasswordDTO;
import com.dev.passwordmanager.dto.password.PasswordGeneratorDTO;
import com.dev.passwordmanager.service.PasswordGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passwords/generator")
@CrossOrigin("http://localhost:5173")
@Tag(name = "Generador de Contraseñas", description = "API para generación de contraseñas seguras")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class PasswordGeneratorController {

    private final PasswordGeneratorService passwordGeneratorService;

    @Operation(
            summary = "Generar contraseña",
            description = "Genera una contraseña segura basada en los parámetros proporcionados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña generada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de generación inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<GeneratedPasswordDTO> generatePassword(
            @Valid @RequestBody(required = false) PasswordGeneratorDTO config) {
        if (config == null) {
            config = new PasswordGeneratorDTO(); // Usa configuración por defecto
        }
        return ResponseEntity.ok(passwordGeneratorService.generatedPassword(config));
    }
}
