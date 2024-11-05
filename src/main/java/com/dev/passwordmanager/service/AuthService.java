package com.dev.passwordmanager.service;

import com.dev.passwordmanager.config.security.jwt.JwtService;
import com.dev.passwordmanager.config.security.UserSecurity;
import com.dev.passwordmanager.dto.auth.AuthDTO;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;


    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {

        log.info("Iniciando registro para usuario: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        try {
            // Crear el usuario
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(false);

            // Guardar el usuario
            user = userRepository.save(user);
            log.info("Usuario guardado con ID: {}", user.getId());

            // Generar token JWT
            UserSecurity securityUser = new UserSecurity(user);
            String token = jwtService.generateToken(securityUser);

            // Enviar código de verificación
            try {
                emailVerificationService.sendVerificationCode(user);
                log.info("Código de verificación enviado a: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error al enviar código de verificación", e);
                // No lanzamos la excepción para que el registro continúe
            }

            // Crear y retornar respuesta
            return AuthDTO.AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .name(user.getName())
                    .requiresVerification(true)
                    .build();

        } catch (Exception e) {
            log.error("Error en el registro de usuario", e);
            throw new RuntimeException("Error al registrar usuario: " + e.getMessage());
        }
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        try {
            // Usa AuthenticationManager para validar las credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            UserSecurity securityUser = new UserSecurity(user);
            String token = jwtService.generateToken(securityUser);

            return createAuthResponse(user, token);

        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    private AuthDTO.AuthResponse createAuthResponse(User user, String token) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new AuthDTO.AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                !user.isEnabled()
        );
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}