package br.ufg.runner.assinador.validation;

import br.ufg.runner.assinador.model.ValidateRequest;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Valida os parâmetros de uma {@link ValidateRequest} conforme as regras
 * do caso de uso "Validar Assinatura" (FHIR R4 — HubSaúde).
 */
public final class ValidateRequestValidator {

    private static final Pattern REFERENCE_PATTERN =
            Pattern.compile("^[A-Z][A-Za-z]+/[A-Za-z0-9\\-.]{1,64}$");

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private ValidateRequestValidator() {
    }

    /**
     * Executa a validação completa.
     *
     * @param request requisição a validar
     * @throws ValidationException quando qualquer regra é violada
     */
    public static void validate(final ValidateRequest request) {
        if (request == null) {
            throw new ValidationException("request", "requisição não pode ser nula");
        }

        validateSignatureId(request.signatureId());
        validateTargetReference(request.targetReference());
        validateData(request.data());
    }

    private static void validateSignatureId(final String signatureId) {
        requireNonBlank("signatureId", signatureId);
        if (!UUID_PATTERN.matcher(signatureId).matches()) {
            throw new ValidationException(
                    "signatureId",
                    "deve ser um UUID válido (recebido: '" + signatureId + "')");
        }
    }

    private static void validateTargetReference(final String targetReference) {
        requireNonBlank("targetReference", targetReference);
        if (!REFERENCE_PATTERN.matcher(targetReference).matches()) {
            throw new ValidationException(
                    "targetReference",
                    "deve seguir o formato 'ResourceType/id' (recebido: '" + targetReference + "')");
        }
    }

    private static void validateData(final String data) {
        requireNonBlank("data", data);
        try {
            Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "data",
                    "deve ser uma string Base64 válida");
        }
    }

    private static void requireNonBlank(final String field, final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "não pode ser vazio");
        }
    }
}
