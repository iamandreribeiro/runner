package br.ufg.runner.assinador;

/**
 * Ponto de entrada do assinador.
 *
 * <p>Recebe argumentos da linha de comandos, valida parâmetros
 * e executa a operação de assinatura digital simulada.</p>
 */
public final class App {

    /** Mensagem exibida quando nenhum argumento é fornecido. */
    static final String USAGE_MESSAGE = "Uso: assinador <comando> [opções]";

    private App() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Ponto de entrada principal.
     *
     * @param args argumentos da linha de comandos
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.out.println(USAGE_MESSAGE);
            return;
        }

        System.out.println("Comando recebido: " + args[0]);
    }
}
