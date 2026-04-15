package br.ufg.runner.assinador.validation;

import br.ufg.runner.assinador.model.ValidateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateRequestValidatorTest {

    private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_TARGET = "Bundle/abc-001";
    private static final String VALID_DATA =
            Base64.getEncoder().encodeToString("fake-signature:Bundle/abc-001:id".getBytes());

    private static ValidateRequest valid() {
        return new ValidateRequest(VALID_UUID, VALID_TARGET, VALID_DATA);
    }

    @Test
    void aceitaRequisicaoValida() {
        assertDoesNotThrow(() -> ValidateRequestValidator.validate(valid()));
    }

    @Test
    void rejeitaRequisicaoNula() {
        assertEquals("request",
                assertThrows(ValidationException.class,
                        () -> ValidateRequestValidator.validate(null)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "nao-e-uuid", "550e8400-XXXX-41d4-a716-446655440000", "123"})
    void rejeitaSignatureIdInvalido(final String id) {
        ValidateRequest r = new ValidateRequest(id, VALID_TARGET, VALID_DATA);
        assertEquals("signatureId",
                assertThrows(ValidationException.class,
                        () -> ValidateRequestValidator.validate(r)).parameter());
    }

    @Test
    void rejeitaSignatureIdNulo() {
        ValidateRequest r = new ValidateRequest(null, VALID_TARGET, VALID_DATA);
        assertEquals("signatureId",
                assertThrows(ValidationException.class,
                        () -> ValidateRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "sem-barra", "bundle/abc", "Bundle/"})
    void rejeitaTargetReferenceInvalido(final String target) {
        ValidateRequest r = new ValidateRequest(VALID_UUID, target, VALID_DATA);
        assertEquals("targetReference",
                assertThrows(ValidationException.class,
                        () -> ValidateRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "não-é-base64!!!", "===invalid==="})
    void rejeitaDataInvalido(final String data) {
        ValidateRequest r = new ValidateRequest(VALID_UUID, VALID_TARGET, data);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidateRequestValidator.validate(r));
        // "data" or "data" parameter
        assertEquals("data", ex.parameter());
    }

    @Test
    void rejeitaDataNulo() {
        ValidateRequest r = new ValidateRequest(VALID_UUID, VALID_TARGET, null);
        assertEquals("data",
                assertThrows(ValidationException.class,
                        () -> ValidateRequestValidator.validate(r)).parameter());
    }
}
