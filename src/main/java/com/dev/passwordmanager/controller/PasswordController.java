package com.dev.passwordmanager.controller;

import com.dev.passwordmanager.dto.password.PasswordEntryDTO;
import com.dev.passwordmanager.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173")
@Tag(name = "Contraseñas", description = "API para gestión de contraseñas")
@SecurityRequirement(name = "Bearer Authentication")
public class PasswordController {

    private final PasswordService passwordService;

    @Operation(
            summary = "Crear contraseña",
            description = "Crea una nueva entrada de contraseña para el usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<PasswordEntryDTO.Response> createPassword(
            @Valid @RequestBody PasswordEntryDTO passwordDTO,
            Authentication authentication) {
        return ResponseEntity.ok(passwordService.createPassword(passwordDTO, authentication.getName()));
    }

    @Operation(
            summary = "Obtener contraseñas",
            description = "Obtiene todas las contraseñas del usuario con filtros opcionales"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contraseñas obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<PasswordEntryDTO.Response>> getAllPasswords(
            Authentication authentication,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(passwordService.getUserPasswords(
                authentication.getName(),
                categoryId,
                search
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntryDTO.Response> getPassword(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(passwordService.getPassword(id, authentication.getName()));
    }

    @Operation(
            summary = "Obtener contraseña desencriptada",
            description = "Obtiene la contraseña desencriptada de una entrada específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Contraseña no encontrada")
    })
    @GetMapping("/{id}/decrypt")
    public ResponseEntity<String> getDecryptedPassword(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(passwordService.getDecryptedPassword(id, authentication.getName()));
    }

    @Operation(
            summary = "Actualizar contraseña",
            description = "Actualiza una entrada de contraseña existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Contraseña no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntryDTO.Response> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordEntryDTO passwordDTO,
            Authentication authentication) {
        return ResponseEntity.ok(passwordService.updatePassword(id, passwordDTO, authentication.getName()));
    }

    @Operation(
            summary = "Eliminar contraseña",
            description = "Elimina una entrada de contraseña existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Contraseña no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassword(
            @PathVariable Long id,
            Authentication authentication) {
        passwordService.deletePassword(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}