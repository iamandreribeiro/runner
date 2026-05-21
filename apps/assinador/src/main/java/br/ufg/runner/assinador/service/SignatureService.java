package br.ufg.runner.assinador.service;

import br.ufg.runner.assinador.model.SignRequest;
import br.ufg.runner.assinador.model.SignResponse;
import br.ufg.runner.assinador.model.ValidateRequest;
import br.ufg.runner.assinador.model.ValidateResponse;

/**
 * Contrato do serviço de assinatura digital.
 *
 * <p>Implementações podem ser simuladas (fake) ou reais. No escopo do
 * Sistema Runner, apenas a simulação é prevista.</p>
 */
public interface SignatureService {

    /**
     * Cria uma assinatura digital a partir dos parâmetros fornecidos.
     *
     * @param request parâmetros validados da criação de assinatura
     * @return resposta contendo a assinatura simulada
     */
    SignResponse sign(SignRequest request);

    /**
     * Valida uma assinatura digital previamente gerada.
     *
     * @param request parâmetros validados da operação de validação
     * @return resultado da validação (válida ou não) com motivo
     */
    ValidateResponse validate(ValidateRequest request);
}
