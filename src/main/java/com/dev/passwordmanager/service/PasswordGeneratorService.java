package com.dev.passwordmanager.service;

import com.dev.passwordmanager.dto.password.GeneratedPasswordDTO;
import com.dev.passwordmanager.dto.password.PasswordGeneratorDTO;
import com.dev.passwordmanager.dto.password.PasswordStrength;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public GeneratedPasswordDTO generatedPassword(PasswordGeneratorDTO config) {
        // Construir el conjunto de caracteres basado en la configuración
        StringBuilder charPool = new StringBuilder();
        List<String> mandatoryChars = new ArrayList<>();

        if (config.isIncludeUppercase()) {
            charPool.append(UPPERCASE_CHARS);
            mandatoryChars.add(String.valueOf(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length()))));
        }
        if (config.isIncludeLowercase()) {
            charPool.append(LOWERCASE_CHARS);
            mandatoryChars.add(String.valueOf(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length()))));
        }
        if (config.isIncludeNumbers()) {
            charPool.append(NUMBER_CHARS);
            mandatoryChars.add(String.valueOf(NUMBER_CHARS.charAt(random.nextInt(NUMBER_CHARS.length()))));
        }
        if (config.isIncludeSpecialChars()) {
            charPool.append(SPECIAL_CHARS);
            mandatoryChars.add(String.valueOf(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()))));
        }
        // Generar la contraseña
        char[] password = new char[config.getLength()];

        // Primero, incluir los caracteres obligatorios
        for (int i = 0; i < mandatoryChars.size(); i++) {
            password[i] = mandatoryChars.get(i).charAt(0);
        }

        // Llenar el resto con caracteres aleatorios
        for (int i = mandatoryChars.size(); i < config.getLength(); i++) {
            password[i] = charPool.charAt(random.nextInt(charPool.length()));
        }

        // Mezclar la contraseña
        for (int i = password.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        String generatedPassword = new String(password);

        // Crear respuesta
        GeneratedPasswordDTO response = new GeneratedPasswordDTO();
        response.setPassword(generatedPassword);
        response.setStrength(calculatePasswordStrength(generatedPassword));
        response.setScore(calculatePasswordScore(generatedPassword));

        return response;
    }

    private PasswordStrength calculatePasswordStrength(String password) {
        int score = calculatePasswordScore(password);

        if (score >= 90) return PasswordStrength.VERY_STRONG;
        if (score >= 70) return PasswordStrength.STRONG;
        if (score >= 50) return PasswordStrength.MEDIUM;
        if (score >= 30) return PasswordStrength.WEAK;
        return PasswordStrength.VERY_WEAK;
    }

    private int calculatePasswordScore(String password) {
        int score = 0;

        // Longitud
        score += Math.min(password.length() * 4, 40);

        // Tipos de caracteres
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*\\d.*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score += 15;

        // Complejidad adicional
        if (password.matches(".*[A-Z].*[A-Z].*")) score += 5;
        if (password.matches(".*\\d.*\\d.*")) score += 5;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score += 5;

        return Math.min(score, 100);
    }
}
