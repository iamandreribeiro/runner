package br.ufg.runner.assinador;

import br.ufg.runner.assinador.service.FakeSignatureService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private final App app = new App(new FakeSignatureService());

    @Test
    void semArgumentosRetornaUsage() {
        assertEquals(App.EXIT_USAGE, app.run(new String[]{}));
    }

    @Test
    void comandoDesconhecidoRetornaUsage() {
        assertEquals(App.EXIT_USAGE, app.run(new String[]{"desconhecido"}));
    }

    @Test
    void signSemParametrosRetornaUsage() {
        assertEquals(App.EXIT_USAGE, app.run(new String[]{"sign"}));
    }

    @Test
    void signParametrosIncompletosRetornaUsage() {
        assertEquals(App.EXIT_USAGE, app.run(new String[]{"sign", "--type", "1.2.3"}));
    }

    @Test
    void signComWhenInvalidoRetornaValidation() {
        int code = app.run(new String[]{
                "sign",
                "--type", "1.2.840.10065.1.12.1.1",
                "--when", "nao-eh-data",
                "--who", "Practitioner/123",
                "--target", "Bundle/abc-001",
                "--sig-format", "application/jose"
        });
        assertEquals(App.EXIT_VALIDATION, code);
    }

    @Test
    void signComParametrosValidosRetornaSucesso() {
        PrintStream original = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            int code = app.run(new String[]{
                    "sign",
                    "--type", "1.2.840.10065.1.12.1.1",
                    "--when", "2026-04-08T12:00:00Z",
                    "--who", "Practitioner/123",
                    "--target", "Bundle/abc-001",
                    "--sig-format", "application/jose"
            });
            assertEquals(App.EXIT_OK, code);
            String output = capture.toString();
            assertTrue(output.contains("signatureId="));
            assertTrue(output.contains("data="));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void signComTypeInvalidoRetornaValidation() {
        int code = app.run(new String[]{
                "sign",
                "--type", "invalido",
                "--when", "2026-04-08T12:00:00Z",
                "--who", "Practitioner/123",
                "--target", "Bundle/abc-001",
                "--sig-format", "application/jose"
        });
        assertEquals(App.EXIT_VALIDATION, code);
    }

    @Test
    void validateSemParametrosRetornaUsage() {
        assertEquals(App.EXIT_USAGE, app.run(new String[]{"validate"}));
    }

    @Test
    void validateComParametrosValidosRetornaSucesso() {
        // First sign to get valid data
        PrintStream original = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            app.run(new String[]{
                    "sign",
                    "--type", "1.2.840.10065.1.12.1.1",
                    "--when", "2026-04-08T12:00:00Z",
                    "--who", "Practitioner/123",
                    "--target", "Bundle/abc-001",
                    "--sig-format", "application/jose"
            });
        } finally {
            System.setOut(original);
        }

        String output = capture.toString();
        String signatureId = extractValue(output, "signatureId=");
        String data = extractValue(output, "data=");

        capture.reset();
        System.setOut(new PrintStream(capture));
        try {
            int code = app.run(new String[]{
                    "validate",
                    "--signature-id", signatureId,
                    "--target", "Bundle/abc-001",
                    "--data", data
            });
            assertEquals(App.EXIT_OK, code);
            String validateOutput = capture.toString();
            assertTrue(validateOutput.contains("valid=true"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void validateComDadosIncorretosRetornaNaoValida() {
        PrintStream original = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            int code = app.run(new String[]{
                    "validate",
                    "--signature-id", "550e8400-e29b-41d4-a716-446655440000",
                    "--target", "Bundle/abc-001",
                    "--data", java.util.Base64.getEncoder()
                            .encodeToString("dados-errados".getBytes())
            });
            assertEquals(App.EXIT_OK, code);
            assertTrue(capture.toString().contains("valid=false"));
        } finally {
            System.setOut(original);
        }
    }

    private static String extractValue(String output, String prefix) {
        for (String line : output.split("\\R")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        throw new IllegalStateException("Prefix not found in output: " + prefix);
    }
}
