package br.ufg.runner.assinador.model;

/**
 * Resultado da validação de uma assinatura digital simulada.
 *
 * @param valid   indica se a assinatura é considerada válida
 * @param reason  descrição textual do motivo (ou "ok" quando válida)
 */
public record ValidateResponse(boolean valid, String reason) {
}
