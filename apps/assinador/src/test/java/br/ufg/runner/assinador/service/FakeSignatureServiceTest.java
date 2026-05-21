package br.ufg.runner.assinador.service;

import br.ufg.runner.assinador.model.SignRequest;
import br.ufg.runner.assinador.model.SignResponse;
import br.ufg.runner.assinador.model.ValidateRequest;
import br.ufg.runner.assinador.model.ValidateResponse;
import br.ufg.runner.assinador.validation.ValidationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeSignatureServiceTest {

    private final FakeSignatureService service = new FakeSignatureService();

    private static SignRequest validRequest() {
        return new SignRequest(
                "1.2.840.10065.1.12.1.1",
                Instant.parse("2026-04-08T12:00:00Z"),
                "Practitioner/123",
                "Bundle/abc-001",
                "application/jose");
    }

    @Test
    void signRetornaRespostaComCamposPreenchidos() {
        SignResponse resp = service.sign(validRequest());

        assertNotNull(resp.signatureId());
        assertEquals(validRequest().when(), resp.when());
        assertEquals(validRequest().sigFormat(), resp.sigFormat());
        assertNotNull(resp.data());
    }

    @Test
    void signGeraIdentificadoresDistintosEntreChamadas() {
        SignResponse a = service.sign(validRequest());
        SignResponse b = service.sign(validRequest());
        assertNotEquals(a.signatureId(), b.signatureId());
    }

    @Test
    void signProduzPayloadBase64DecodificavelComTargetReference() {
        SignResponse resp = service.sign(validRequest());
        String decoded = new String(
                Base64.getDecoder().decode(resp.data()), StandardCharsets.UTF_8);
        assertTrue(decoded.startsWith(FakeSignatureService.FAKE_SIGNATURE_PAYLOAD_PREFIX));
        assertTrue(decoded.contains("Bundle/abc-001"));
        assertTrue(decoded.contains(resp.signatureId()));
    }

    @Test
    void signPropagaValidacaoQuandoRequestInvalida() {
        SignRequest invalid = new SignRequest(
                "invalido", validRequest().when(), validRequest().who(),
                validRequest().targetReference(), validRequest().sigFormat());
        assertThrows(ValidationException.class, () -> service.sign(invalid));
    }

    @Test
    void validateRetornaValidoQuandoAssinaturaCorrespondeAoRecurso() {
        SignResponse signed = service.sign(validRequest());
        ValidateRequest vr = new ValidateRequest(
                signed.signatureId(), "Bundle/abc-001", signed.data());
        ValidateResponse resp = service.validate(vr);
        assertTrue(resp.valid());
        assertEquals("ok", resp.reason());
    }

    @Test
    void validateRetornaInvalidoQuandoTargetNaoConfere() {
        SignResponse signed = service.sign(validRequest());
        ValidateRequest vr = new ValidateRequest(
                signed.signatureId(), "Bundle/outro-recurso", signed.data());
        ValidateResponse resp = service.validate(vr);
        assertFalse(resp.valid());
        assertTrue(resp.reason().contains("Bundle/outro-recurso"));
    }

    @Test
    void validateRetornaInvalidoQuandoDadosForjados() {
        String fakeData = Base64.getEncoder()
                .encodeToString("dados-forjados".getBytes(StandardCharsets.UTF_8));
        ValidateRequest vr = new ValidateRequest(
                "550e8400-e29b-41d4-a716-446655440000", "Bundle/abc-001", fakeData);
        ValidateResponse resp = service.validate(vr);
        assertFalse(resp.valid());
    }

    @Test
    void validatePropagaValidacaoQuandoRequestInvalida() {
        assertThrows(ValidationException.class,
                () -> service.validate(null));
    }
}
