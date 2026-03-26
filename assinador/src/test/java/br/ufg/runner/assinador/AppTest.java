package br.ufg.runner.assinador;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do ponto de entrada do assinador.
 */
class AppTest {

    @Test
    void semArgumentosDeveExibirMensagemDeUso() {
        final String output = captureStdout(() -> App.main(new String[]{}));
        assertEquals(App.USAGE_MESSAGE, output.trim());
    }

    @Test
    void comArgumentoDeveExibirComandoRecebido() {
        final String output = captureStdout(() -> App.main(new String[]{"assinar"}));
        assertTrue(output.contains("assinar"));
    }

    /**
     * Captura a saída do stdout durante a execução de um bloco.
     */
    private String captureStdout(final Runnable action) {
        final PrintStream original = System.out;
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
