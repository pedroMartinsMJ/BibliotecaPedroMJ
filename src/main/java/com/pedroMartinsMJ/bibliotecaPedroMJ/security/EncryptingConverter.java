package com.pedroMartinsMJ.bibliotecaPedroMJ.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Conversor JPA que criptografa automaticamente um campo de entidade antes de
 * persistir no banco e descriptografa ao ler.
 *
 * <p>O campo anotado com {@code @Convert(converter = EncryptingConverter.class)}
 * será armazenado criptografado no banco de dados, protegendo dados sensíveis
 * (ex: e-mail) caso o banco seja acessado diretamente.</p>
 *
 * <p>Requer a variável de ambiente {@code APP_ENCRYPTION_KEY}.</p>
 */
@Component
@Converter(autoApply = false)
public class EncryptingConverter implements AttributeConverter<String, String> {

    private final CryptoUtil crypto;

    public EncryptingConverter(@Value("${app.encryption.key:}") String keyBase64) {
        this.crypto = new CryptoUtil(keyBase64);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return crypto.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return crypto.decrypt(dbData);
    }
}
