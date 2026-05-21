package br.ufg.runner.assinador.model;

import java.time.Instant;

/**
 * Resultado da criação de assinatura digital simulada.
 *
 * @param signatureId  identificador único da assinatura gerada
 * @param when         instante registrado na assinatura
 * @param sigFormat    mime-type do formato da assinatura
 * @param data         conteúdo da assinatura em Base64
 */
public record SignResponse(
        String signatureId,
        Instant when,
        String sigFormat,
        String data) {
}
