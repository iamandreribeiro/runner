package br.ufg.runner.assinador.validation;

/**
 * Erro lançado quando parâmetros de entrada violam as regras de validação.
 *
 * <p>A mensagem deve indicar claramente qual parâmetro é inválido e o motivo,
 * para que o usuário possa corrigir a requisição sem ambiguidade.</p>
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Nome do parâmetro que causou a falha de validação. */
    private final String parameter;

    /**
     * @param parameter nome do parâmetro inválido
     * @param message   descrição do motivo da rejeição
     */
    public ValidationException(final String parameter, final String message) {
        super("Parâmetro inválido '" + parameter + "': " + message);
        this.parameter = parameter;
    }

    public String parameter() {
        return parameter;
    }
}
