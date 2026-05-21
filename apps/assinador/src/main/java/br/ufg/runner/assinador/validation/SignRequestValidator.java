package br.ufg.runner.assinador.validation;

import br.ufg.runner.assinador.model.SignRequest;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Valida os parâmetros de uma {@link SignRequest} conforme as regras
 * do caso de uso "Criar Assinatura" (FHIR R4 — HubSaúde).
 *
 * <p>Todas as regras são verificadas antes de qualquer processamento,
 * e a primeira violação encontrada resulta em {@link ValidationException}
 * com mensagem específica sobre o parâmetro e o motivo.</p>
 */
public final class SignRequestValidator {

    /** OID (sem pontuação arbitrária): dígitos separados por pontos. */
    private static final Pattern OID_PATTERN = Pattern.compile("^\\d+(\\.\\d+)+$");

    /** Referência FHIR no formato "ResourceType/id" (id conforme §2.1.1 do FHIR). */
    private static final Pattern REFERENCE_PATTERN =
            Pattern.compile("^[A-Z][A-Za-z]+/[A-Za-z0-9\\-.]{1,64}$");

    /** Mime-types de assinatura aceitos na simulação. */
    private static final Pattern SIG_FORMAT_PATTERN =
            Pattern.compile("^application/(jose|jose\\+json|pkcs7-signature|x-pkcs7-signature)$");

    private SignRequestValidator() {
        // Classe utilitária.
    }

    /**
     * Executa a validação completa. Retorna sem efeito se válida.
     *
     * @param request requisição a validar (pode ser {@code null})
     * @throws ValidationException quando qualquer regra é violada
     */
    public static void validate(final SignRequest request) {
        if (request == null) {
            throw new ValidationException("request", "requisição não pode ser nula");
        }

        validateType(request.type());
        validateWhen(request.when());
        validateReference("who", request.who());
        validateReference("targetReference", request.targetReference());
        validateSigFormat(request.sigFormat());
    }

    private static void validateType(final String type) {
        requireNonBlank("type", type);
        if (!OID_PATTERN.matcher(type).matches()) {
            throw new ValidationException(
                    "type",
                    "deve ser um OID no formato '1.2.3.4' (recebido: '" + type + "')");
        }
    }

    private static void validateWhen(final Instant when) {
        if (when == null) {
            throw new ValidationException("when", "instante da assinatura é obrigatório");
        }
        if (when.isAfter(Instant.now().plusSeconds(60))) {
            throw new ValidationException(
                    "when",
                    "não pode estar no futuro (recebido: " + when + ")");
        }
    }

    private static void validateReference(final String field, final String reference) {
        requireNonBlank(field, reference);
        if (!REFERENCE_PATTERN.matcher(reference).matches()) {
            throw new ValidationException(
                    field,
                    "deve seguir o formato 'ResourceType/id' (recebido: '" + reference + "')");
        }
    }

    private static void validateSigFormat(final String sigFormat) {
        requireNonBlank("sigFormat", sigFormat);
        if (!SIG_FORMAT_PATTERN.matcher(sigFormat).matches()) {
            throw new ValidationException(
                    "sigFormat",
                    "mime-type não suportado (recebido: '" + sigFormat + "')");
        }
    }

    private static void requireNonBlank(final String field, final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "não pode ser vazio");
        }
    }
}
