package br.ufg.runner.assinador.cli;

/**
 * Erro lançado quando os argumentos de linha de comando violam as regras
 * declaradas em {@link Args.Builder}.
 */
public class ArgsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ArgsException(final String message) {
        super(message);
    }
}
