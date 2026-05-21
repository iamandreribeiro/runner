package br.ufg.runner.assinador.model;

/**
 * Parâmetros para validação de assinatura digital simulada.
 *
 * @param signatureId      identificador da assinatura a validar
 * @param targetReference  referência ao recurso assinado
 * @param data             conteúdo da assinatura em Base64
 */
public record ValidateRequest(
        String signatureId,
        String targetReference,
        String data) {
}
