package br.ufg.runner.assinador.service;

import br.ufg.runner.assinador.model.SignRequest;
import br.ufg.runner.assinador.model.SignResponse;
import br.ufg.runner.assinador.model.ValidateRequest;
import br.ufg.runner.assinador.model.ValidateResponse;
import br.ufg.runner.assinador.validation.SignRequestValidator;
import br.ufg.runner.assinador.validation.ValidateRequestValidator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Implementação simulada de {@link SignatureService}.
 *
 * <p>Não executa operações criptográficas reais: os parâmetros são
 * validados rigorosamente e, quando válidos, retorna uma resposta
 * pré-construída determinística. O foco está no fluxo e no contrato,
 * não na criptografia.</p>
 */
public final class FakeSignatureService implements SignatureService {

    /** Prefixo usado como conteúdo da assinatura simulada antes da codificação Base64. */
    static final String FAKE_SIGNATURE_PAYLOAD_PREFIX = "fake-signature:";

    @Override
    public SignResponse sign(final SignRequest request) {
        SignRequestValidator.validate(request);

        final String signatureId = UUID.randomUUID().toString();
        final String payload = FAKE_SIGNATURE_PAYLOAD_PREFIX
                + request.targetReference() + ":" + signatureId;
        final String data = Base64.getEncoder()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        return new SignResponse(signatureId, request.when(), request.sigFormat(), data);
    }

    @Override
    public ValidateResponse validate(final ValidateRequest request) {
        ValidateRequestValidator.validate(request);

        final String decoded = new String(
                Base64.getDecoder().decode(request.data()), StandardCharsets.UTF_8);

        if (decoded.startsWith(FAKE_SIGNATURE_PAYLOAD_PREFIX)
                && decoded.contains(request.targetReference())) {
            return new ValidateResponse(true, "ok");
        }

        return new ValidateResponse(false,
                "assinatura não corresponde ao recurso '" + request.targetReference() + "'");
    }
}
