package br.ufg.runner.assinador.model;

import java.time.Instant;

/**
 * Parâmetros para criação de assinatura digital simulada.
 *
 * <p>Os campos seguem o modelo FHIR Provenance.signature.</p>
 *
 * @param type             código do tipo de assinatura (ex.: "1.2.840.10065.1.12.1.1")
 * @param when             instante em que a assinatura é gerada
 * @param who              referência ao signatário (ex.: "Practitioner/123")
 * @param targetReference  referência ao recurso assinado (ex.: "Bundle/abc")
 * @param sigFormat        mime-type do formato de assinatura (ex.: "application/jose")
 */
public record SignRequest(
        String type,
        Instant when,
        String who,
        String targetReference,
        String sigFormat) {
}
