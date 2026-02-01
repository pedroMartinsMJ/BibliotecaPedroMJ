package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.CPFValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CPFValidator
 *
 * Para rodar os testes:
 * - No IntelliJ: Clique com botão direito na classe > Run 'CPFValidatorTest'
 * - No terminal: mvn test
 */
class CPFValidatorTest {

    private CPFValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CPFValidator();
        validator.initialize(null); // Inicializa o validador
    }

    @Test
    @DisplayName("Deve aceitar CPF válido com formatação")
    void deveAceitarCpfValidoComFormatacao() {
        // CPFs válidos para teste
        assertTrue(validator.isValid("123.456.789-09", null));
        assertTrue(validator.isValid("111.444.777-35", null));
        assertTrue(validator.isValid("529.982.247-25", null));
    }

    @Test
    @DisplayName("Deve aceitar CPF válido sem formatação")
    void deveAceitarCpfValidoSemFormatacao() {
        assertTrue(validator.isValid("12345678909", null));
        assertTrue(validator.isValid("11144477735", null));
        assertTrue(validator.isValid("52998224725", null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com todos os dígitos iguais")
    void deveRejeitarCpfComDigitosIguais() {
        assertFalse(validator.isValid("111.111.111-11", null));
        assertFalse(validator.isValid("000.000.000-00", null));
        assertFalse(validator.isValid("999.999.999-99", null));
        assertFalse(validator.isValid("11111111111", null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígitos verificadores incorretos")
    void deveRejeitarCpfComDigitosVerificadoresIncorretos() {
        assertFalse(validator.isValid("123.456.789-00", null));
        assertFalse(validator.isValid("111.444.777-00", null));
        assertFalse(validator.isValid("529.982.247-00", null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com número incorreto de dígitos")
    void deveRejeitarCpfComNumeroIncorretoDeDigitos() {
        assertFalse(validator.isValid("123.456.789", null));  // Faltam dígitos
        assertFalse(validator.isValid("123", null));
        assertFalse(validator.isValid("123.456.789-099", null)); // Dígito extra
    }

    @Test
    @DisplayName("Deve aceitar CPF null (use @NotBlank para obrigar preenchimento)")
    void deveAceitarCpfNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF vazio")
    void deveRejeitarCpfVazio() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com letras")
    void deveRejeitarCpfComLetras() {
        assertFalse(validator.isValid("ABC.DEF.GHI-JK", null));
        assertFalse(validator.isValid("12A.456.789-09", null));
    }

    @Test
    @DisplayName("Deve aceitar CPF com espaços (remove automaticamente)")
    void deveAceitarCpfComEspacos() {
        assertTrue(validator.isValid("123 456 789 09", null));
        assertTrue(validator.isValid(" 123.456.789-09 ", null));
    }

    @Test
    @DisplayName("Testa múltiplos CPFs válidos conhecidos")
    void testaMultiplosCpfsValidos() {
        // Lista de CPFs válidos para testes
        String[] cpfsValidos = {
                "12345678909",
                "11144477735",
                "52998224725",
                "12312312312",
                "98765432100"
        };

        for (String cpf : cpfsValidos) {
            assertTrue(validator.isValid(cpf, null),
                    "CPF " + cpf + " deveria ser válido");
        }
    }

    @Test
    @DisplayName("Testa múltiplos CPFs inválidos conhecidos")
    void testaMultiplosCpfsInvalidos() {
        String[] cpfsInvalidos = {
                "00000000000",
                "11111111111",
                "22222222222",
                "12345678900",  // Dígito verificador errado
                "123456789",    // Falta dígitos
                "1234567890123" // Dígitos extras
        };

        for (String cpf : cpfsInvalidos) {
            assertFalse(validator.isValid(cpf, null),
                    "CPF " + cpf + " deveria ser inválido");
        }
    }
}
