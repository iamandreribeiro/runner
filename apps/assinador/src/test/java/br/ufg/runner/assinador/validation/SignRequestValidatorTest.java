package br.ufg.runner.assinador.validation;

import br.ufg.runner.assinador.model.SignRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignRequestValidatorTest {

    private static SignRequest valid() {
        return new SignRequest(
                "1.2.840.10065.1.12.1.1",
                Instant.parse("2026-04-08T12:00:00Z"),
                "Practitioner/123",
                "Bundle/abc-001",
                "application/jose");
    }

    @Test
    void aceitaRequisicaoValida() {
        assertDoesNotThrow(() -> SignRequestValidator.validate(valid()));
    }

    @Test
    void rejeitaRequisicaoNula() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> SignRequestValidator.validate(null));
        assertEquals("request", ex.parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abc", "1..2", "1.2.", ".1.2"})
    void rejeitaTypeInvalido(final String type) {
        SignRequest r = new SignRequest(
                type, valid().when(), valid().who(),
                valid().targetReference(), valid().sigFormat());
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> SignRequestValidator.validate(r));
        assertEquals("type", ex.parameter());
    }

    @Test
    void rejeitaTypeNulo() {
        SignRequest r = new SignRequest(
                null, valid().when(), valid().who(),
                valid().targetReference(), valid().sigFormat());
        assertEquals("type",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @Test
    void rejeitaWhenNulo() {
        SignRequest r = new SignRequest(
                valid().type(), null, valid().who(),
                valid().targetReference(), valid().sigFormat());
        assertEquals("when",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @Test
    void rejeitaWhenNoFuturo() {
        SignRequest r = new SignRequest(
                valid().type(),
                Instant.now().plusSeconds(3600),
                valid().who(),
                valid().targetReference(),
                valid().sigFormat());
        assertEquals("when",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "sem-barra", "practitioner/123", "Bundle/", "/abc", "Bundle/abc!"})
    void rejeitaWhoInvalido(final String who) {
        SignRequest r = new SignRequest(
                valid().type(), valid().when(), who,
                valid().targetReference(), valid().sigFormat());
        assertEquals("who",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bundle", "bundle/abc", "Bundle/ ", "Bundle/abc#1"})
    void rejeitaTargetReferenceInvalido(final String target) {
        SignRequest r = new SignRequest(
                valid().type(), valid().when(), valid().who(),
                target, valid().sigFormat());
        assertEquals("targetReference",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "text/plain", "application/json", "application/xml"})
    void rejeitaSigFormatInvalido(final String sigFormat) {
        SignRequest r = new SignRequest(
                valid().type(), valid().when(), valid().who(),
                valid().targetReference(), sigFormat);
        assertEquals("sigFormat",
                assertThrows(ValidationException.class,
                        () -> SignRequestValidator.validate(r)).parameter());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/jose",
            "application/jose+json",
            "application/pkcs7-signature",
            "application/x-pkcs7-signature"})
    void aceitaSigFormatSuportado(final String sigFormat) {
        SignRequest r = new SignRequest(
                valid().type(), valid().when(), valid().who(),
                valid().targetReference(), sigFormat);
        assertDoesNotThrow(() -> SignRequestValidator.validate(r));
    }

    @Test
    void mensagemDeErroIdentificaParametroEMotivo() {
        SignRequest r = new SignRequest(
                "nao-eh-oid", valid().when(), valid().who(),
                valid().targetReference(), valid().sigFormat());
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> SignRequestValidator.validate(r));
        assertTrue(ex.getMessage().contains("type"));
        assertTrue(ex.getMessage().contains("nao-eh-oid"));
    }
}
