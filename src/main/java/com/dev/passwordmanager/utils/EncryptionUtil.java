package com.dev.passwordmanager.utils;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

import java.security.SecureRandom;

@Component
public class EncryptionUtil {
    // En producción, esta clave debería estar en un lugar seguro como un vault
    private static final String ENCRYPTION_KEY = "01234567890123456789012345678901"; // 32 bytes para AES-256
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public String encrypt(String data) {
        try {
            // genear IV aleatorio
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // crear clave secreta
            SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");

            // cypher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // encriptar
            byte[] encryptedData = cipher.doFinal(data.getBytes());

            // combinar IV y datos encriptados
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error en la encriptación: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            // Decodificar base64
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // Extraer IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // Extraer datos encriptados
            byte[] encrypted = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Crear clave secreta
            SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");

            // Inicializar cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Desencriptar
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new RuntimeException("Error en la desencriptación: " + e.getMessage(), e);
        }
    }
}
