package com.dev.passwordmanager.controller;

import com.dev.passwordmanager.dto.auth.AuthDTO;
import com.dev.passwordmanager.dto.auth.VerificationDTO;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.UserRepository;
import com.dev.passwordmanager.service.AuthService;
import com.dev.passwordmanager.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173")
@Tag(name = "Autenticación", description = "API de autenticación y registro")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema y retorna un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso",
                    content = @Content(schema = @Schema(implementation = AuthDTO.AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        try {
            log.info("Recibida solicitud de registro para: {}", request.getEmail());
            AuthDTO.AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en el registro", e);
            return ResponseEntity
                    .badRequest()
                    .body(new ApiError(e.getMessage()));
        }
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthDTO.AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        try {
            AuthDTO.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en el login", e);
            return ResponseEntity
                    .badRequest()
                    .body(new ApiError(e.getMessage()));
        }
    }

/*    @PostMapping("/verify-email")
    public ResponseEntity<VerificationDTO.VerificationResponse> verifyCode(
            @Valid @RequestBody VerificationDTO.CodeVerificationRequest request) {
        boolean verified = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(VerificationDTO.VerificationResponse.builder()
                .verified(verified)
                .message(verified ? "Email verificado exitosamente" : "Código inválido o expirado")
                .build());
    }*/

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerificationDTO.CodeVerificationRequest request) {
        try {
            boolean verified = emailVerificationService.verifyCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(new VerificationResponse(
                    verified,
                    verified ? "Email verificado exitosamente" : "Código inválido o expirado"
            ));
        } catch (Exception e) {
            log.error("Error en la verificación", e);
            return ResponseEntity
                    .badRequest()
                    .body(new ApiError(e.getMessage()));
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendVerificationCode(@Valid @RequestBody VerificationDTO.ResendCodeRequest request) {
        try {
            User user = authService.findUserByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            emailVerificationService.sendVerificationCode(user);
            return ResponseEntity.ok(new MessageResponse("Código de verificación reenviado"));
        } catch (Exception e) {
            log.error("Error al reenviar código", e);
            return ResponseEntity
                    .badRequest()
                    .body(new ApiError(e.getMessage()));
        }
    }

    @Data
    @AllArgsConstructor
    static class ApiError {
        private String message;
    }

    @Data
    @AllArgsConstructor
    static class MessageResponse {
        private String message;
    }

    @Data
    @AllArgsConstructor
    static class VerificationResponse {
        private boolean verified;
        private String message;
    }



}
