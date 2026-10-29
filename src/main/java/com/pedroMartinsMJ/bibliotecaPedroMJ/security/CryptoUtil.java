package com.pedroMartinsMJ.bibliotecaPedroMJ.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário de criptografia simétrica (AES-GCM) para dados sensíveis.
 *
 * <p>Usa uma chave secreta configurada via variável de ambiente
 * {@code APP_ENCRYPTION_KEY} (32 bytes em Base64). A mesma chave deve ser
 * usada em todos os ambientes onde a aplicação roda, pois dados criptografados
 * só podem ser descriptografados com a chave original.</p>
 *
 * <p>O algoritmo AES-GCM fornece confidencialidade e integridade dos dados.</p>
 */
public final class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE_BITS = 256;
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_SIZE_BITS = 128;

    private final SecretKey secretKey;

    public CryptoUtil(String keyBase64) {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalArgumentException("APP_ENCRYPTION_KEY não pode estar vazia");
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != KEY_SIZE_BITS / 8) {
            throw new IllegalArgumentException(
                    "APP_ENCRYPTION_KEY deve ter " + (KEY_SIZE_BITS / 8) + " bytes (256 bits)");
        }
        this.secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Criptografa um texto puro e retorna o resultado em Base64.
     *
     * @param plainText texto em aberto
     * @return texto criptografado em Base64 (IV + ciphertext + tag)
     * @throws RuntimeException se falhar a criptografia
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_SIZE_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combina IV + ciphertext em um único array Base64
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado sensível", e);
        }
    }

    /**
     * Descriptografa um texto em Base64 produzido por {@link #encrypt(String)}.
     *
     * @param cipherTextBase64 texto criptografado em Base64
     * @return texto original em aberto
     * @throws RuntimeException se falhar a descriptografia
     */
    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherTextBase64);

            byte[] iv = new byte[IV_SIZE_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado sensível", e);
        }
    }

    /**
     * Gera uma nova chave Base64 de 256 bits para uso como APP_ENCRYPTION_KEY.
     * Útil para configurar pela primeira vez.
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE_BITS);
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    private CryptoUtil() {
        throw new UnsupportedOperationException("Use CryptoUtil(String keyBase64)");
    }
}
