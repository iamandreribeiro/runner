package br.ufg.runner.assinador;

import br.ufg.runner.assinador.model.SignRequest;
import br.ufg.runner.assinador.model.SignResponse;
import br.ufg.runner.assinador.model.ValidateRequest;
import br.ufg.runner.assinador.model.ValidateResponse;
import br.ufg.runner.assinador.service.FakeSignatureService;
import br.ufg.runner.assinador.service.SignatureService;
import br.ufg.runner.assinador.validation.ValidationException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Ponto de entrada do assinador.jar.
 *
 * <p>Subcomandos suportados:</p>
 * <ul>
 *   <li>{@code sign} — cria uma assinatura digital simulada</li>
 *   <li>{@code validate} — valida uma assinatura digital simulada</li>
 * </ul>
 *
 * <h3>Códigos de saída</h3>
 * <ul>
 *   <li>0 — sucesso</li>
 *   <li>1 — erro de uso (parâmetros ausentes ou comando desconhecido)</li>
 *   <li>2 — erro de validação de parâmetros</li>
 *   <li>3 — erro interno inesperado</li>
 * </ul>
 */
public final class App {

    static final int EXIT_OK = 0;
    static final int EXIT_USAGE = 1;
    static final int EXIT_VALIDATION = 2;
    static final int EXIT_INTERNAL = 3;

    static final String USAGE_MESSAGE = String.join(System.lineSeparator(),
            "Uso: assinador <comando> [opções]",
            "",
            "Comandos:",
            "  sign       Cria uma assinatura digital simulada",
            "  validate   Valida uma assinatura digital simulada",
            "",
            "Opções do sign:",
            "  --type <oid>              OID do tipo de assinatura",
            "  --when <instant>          Instante da assinatura (ISO-8601)",
            "  --who <reference>         Referência ao signatário (ResourceType/id)",
            "  --target <reference>      Referência ao recurso assinado (ResourceType/id)",
            "  --sig-format <mime>       Formato da assinatura (ex.: application/jose)",
            "",
            "Opções do validate:",
            "  --signature-id <uuid>     ID da assinatura a validar",
            "  --target <reference>      Referência ao recurso assinado",
            "  --data <base64>           Conteúdo da assinatura em Base64");

    private final SignatureService service;

    App(final SignatureService service) {
        this.service = service;
    }

    /**
     * Ponto de entrada principal.
     *
     * @param args argumentos da linha de comandos
     */
    public static void main(final String[] args) {
        final App app = new App(new FakeSignatureService());
        final int exitCode = app.run(args);
        if (exitCode != EXIT_OK) {
            System.exit(exitCode);
        }
    }

    /**
     * Executa o comando e retorna o código de saída (sem chamar System.exit).
     */
    int run(final String[] args) {
        if (args.length == 0) {
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
        }

        final String command = args[0];
        final String[] rest = Arrays.copyOfRange(args, 1, args.length);

        return switch (command) {
            case "sign" -> handleSign(rest);
            case "validate" -> handleValidate(rest);
            default -> {
                System.err.println("Comando desconhecido: " + command);
                System.out.println(USAGE_MESSAGE);
                yield EXIT_USAGE;
            }
        };
    }

    private int handleSign(final String[] args) {
        final Map<String, String> opts = parseOptions(args);

        final String type = opts.get("--type");
        final String whenStr = opts.get("--when");
        final String who = opts.get("--who");
        final String target = opts.get("--target");
        final String sigFormat = opts.get("--sig-format");

        if (type == null || whenStr == null || who == null
                || target == null || sigFormat == null) {
            System.err.println("Erro: todos os parâmetros do sign são obrigatórios.");
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
        }

        final Instant when;
        try {
            when = Instant.parse(whenStr);
        } catch (DateTimeParseException e) {
            System.err.println("Erro: --when deve estar no formato ISO-8601 (ex.: 2026-04-08T12:00:00Z)");
            return EXIT_VALIDATION;
        }

        try {
            final SignRequest request = new SignRequest(type, when, who, target, sigFormat);
            final SignResponse response = service.sign(request);
            System.out.println("signatureId=" + response.signatureId());
            System.out.println("when=" + response.when());
            System.out.println("sigFormat=" + response.sigFormat());
            System.out.println("data=" + response.data());
            return EXIT_OK;
        } catch (ValidationException e) {
            System.err.println("Erro de validação: " + e.getMessage());
            return EXIT_VALIDATION;
        } catch (Exception e) {
            System.err.println("Erro interno: " + e.getMessage());
            return EXIT_INTERNAL;
        }
    }

    private int handleValidate(final String[] args) {
        final Map<String, String> opts = parseOptions(args);

        final String signatureId = opts.get("--signature-id");
        final String target = opts.get("--target");
        final String data = opts.get("--data");

        if (signatureId == null || target == null || data == null) {
            System.err.println("Erro: todos os parâmetros do validate são obrigatórios.");
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
        }

        try {
            final ValidateRequest request = new ValidateRequest(signatureId, target, data);
            final ValidateResponse response = service.validate(request);
            System.out.println("valid=" + response.valid());
            System.out.println("reason=" + response.reason());
            return EXIT_OK;
        } catch (ValidationException e) {
            System.err.println("Erro de validação: " + e.getMessage());
            return EXIT_VALIDATION;
        } catch (Exception e) {
            System.err.println("Erro interno: " + e.getMessage());
            return EXIT_INTERNAL;
        }
    }

    static Map<String, String> parseOptions(final String[] args) {
        final Map<String, String> opts = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i].startsWith("--")) {
                opts.put(args[i], args[i + 1]);
            }
        }
        return opts;
    }
}
