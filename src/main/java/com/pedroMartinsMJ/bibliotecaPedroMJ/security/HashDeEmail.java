package com.pedroMartinsMJ.bibliotecaPedroMJ.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

/**
 * Índice pesquisável do e-mail (blind index).
 *
 * <p>O e-mail é cifrado com AES-GCM e IV aleatório, então o mesmo valor produz
 * um cifrado diferente a cada gravação: a coluna cifrada não pode ser usada em
 * {@code WHERE} nem em constraint de unicidade. Este componente deriva, por
 * HMAC-SHA-256, um índice determinístico que devolve busca e unicidade.</p>
 *
 * <p>A chave do HMAC é derivada da chave de criptografia por HKDF-style expand
 * (RFC 5869, {@code expand} apenas) com um info fixo, de modo que a chave de
 * confidencialidade e a de índice não são a mesma sequência de bytes.</p>
 */
@Component
public class HashDeEmail {

    private static final String ALGORITMO = "HmacSHA256";
    private static final String INFO_DERIVACAO = "biblioteca-pedromj-email-index-v1";
    private static final int TAMANHO_INDEX_BYTES = 16;

    private final SecretKeySpec chaveHmac;

    public HashDeEmail(@Value("${app.encryption.key:}") String keyBase64) {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalArgumentException("APP_ENCRYPTION_KEY não pode estar vazia");
        }
        byte[] chaveMestre = Base64.getDecoder().decode(keyBase64);
        this.chaveHmac = new SecretKeySpec(expandir(chaveMestre), ALGORITMO);
    }

    /**
     * Calcula o índice do e-mail, normalizando antes (trim, caixa baixa e
     * domínio em Unicode NFC) para que variações de escrita colidam.
     */
    public String calcular(String email) {
        if (email == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(chaveHmac);
            byte[] digest = mac.doFinal(normalizar(email).getBytes(StandardCharsets.UTF_8));
            byte[] indice = Arrays.copyOf(digest, TAMANHO_INDEX_BYTES);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(indice);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao calcular índice de e-mail", e);
        }
    }

    private static byte[] expandir(byte[] chaveMestre) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(chaveMestre, ALGORITMO));
            return mac.doFinal(INFO_DERIVACAO.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao derivar chave do índice de e-mail", e);
        }
    }

    private static String normalizar(String email) {
        String semEspacos = email.trim();
        int arroba = semEspacos.lastIndexOf('@');
        if (arroba < 0) {
            return normalizarDomínio(semEspacos);
        }
        String local = semEspacos.substring(0, arroba).toLowerCase(Locale.ROOT);
        return local + "@" + normalizarDomínio(semEspacos.substring(arroba + 1));
    }

    private static String normalizarDomínio(String dominio) {
        return Normalizer.normalize(dominio, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    }
}
