package com.dev.passwordmanager.service;

import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.model.VerificationCode;
import com.dev.passwordmanager.repository.UserRepository;
import com.dev.passwordmanager.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public void sendVerificationCode(User user) {
        if (user == null || user.getEmail() == null) {
            log.error("Usuario o email nulo al intentar enviar código de verificación");
            throw new IllegalArgumentException("Usuario y email son requeridos");
        }

        log.info("Generando código de verificación para: {}", user.getEmail());

        try {
            String code = generateVerificationCode();

            // Guardar el código
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setEmail(user.getEmail());
            verificationCode.setExpiryDate(LocalDateTime.now().plusMinutes(10));
            verificationCodeRepository.save(verificationCode);

            // Enviar email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verifica tu cuenta");
            message.setText("Tu código de verificación es: " + code +
                    "\nEste código expirará en 10 minutos.");

            mailSender.send(message);
            log.info("Código de verificación enviado exitosamente a: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error al enviar código de verificación a: " + user.getEmail(), e);
            throw new RuntimeException("Error al enviar código de verificación");
        }
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        log.info("Verificando código para email: {}", email);

        return verificationCodeRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .map(verificationCode -> {
                    if (verificationCode.isExpired()) {
                        log.info("Código expirado para email: {}", email);
                        return false;
                    }

                    try {
                        // Marcar código como usado
                        verificationCode.setUsed(true);
                        verificationCodeRepository.save(verificationCode);

                        // Activar usuario
                        User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                        user.setEnabled(true);
                        userRepository.save(user);

                        log.info("Verificación exitosa para email: {}", email);
                        return true;
                    } catch (Exception e) {
                        log.error("Error al procesar verificación para email: " + email, e);
                        throw new RuntimeException("Error al procesar la verificación", e);
                    }
                })
                .orElseGet(() -> {
                    log.info("Código inválido o ya usado para email: {}", email);
                    return false;
                });
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}