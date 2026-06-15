package br.ufg.runner.assinador;

import br.ufg.runner.assinador.cli.Args;
import br.ufg.runner.assinador.cli.ArgsException;
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

    private int handleSign(final String[] rawArgs) {
        final Args opts;
        try {
            opts = Args.parser()
                    .value("--type")
                    .value("--when")
                    .value("--who")
                    .value("--target")
                    .value("--sig-format")
                    .parse(rawArgs);
        } catch (ArgsException e) {
            System.err.println("Erro de uso: " + e.getMessage());
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
        }

        final String type;
        final Instant when;
        final String who;
        final String target;
        final String sigFormat;
        try {
            type = opts.required("--type");
            who = opts.required("--who");
            target = opts.required("--target");
            sigFormat = opts.required("--sig-format");
            try {
                when = Instant.parse(opts.required("--when"));
            } catch (DateTimeParseException e) {
                System.err.println("Erro: --when deve estar no formato ISO-8601 (ex.: 2026-04-08T12:00:00Z)");
                return EXIT_VALIDATION;
            }
        } catch (ArgsException e) {
            System.err.println("Erro de uso: " + e.getMessage());
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
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

    private int handleValidate(final String[] rawArgs) {
        final Args opts;
        try {
            opts = Args.parser()
                    .value("--signature-id")
                    .value("--target")
                    .value("--data")
                    .parse(rawArgs);
        } catch (ArgsException e) {
            System.err.println("Erro de uso: " + e.getMessage());
            System.out.println(USAGE_MESSAGE);
            return EXIT_USAGE;
        }

        final String signatureId;
        final String target;
        final String data;
        try {
            signatureId = opts.required("--signature-id");
            target = opts.required("--target");
            data = opts.required("--data");
        } catch (ArgsException e) {
            System.err.println("Erro de uso: " + e.getMessage());
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
}
