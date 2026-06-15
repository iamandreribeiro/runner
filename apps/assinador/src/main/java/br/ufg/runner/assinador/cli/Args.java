package br.ufg.runner.assinador.cli;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parser de argumentos de linha de comando do assinador.jar.
 *
 * <p>Suporta dois tipos de opções:</p>
 * <ul>
 *   <li><strong>Opções com valor:</strong> {@code --flag valor} ou {@code --flag=valor}</li>
 *   <li><strong>Opções booleanas:</strong> {@code --flag} (presença = true, ausência = false)</li>
 * </ul>
 *
 * <p>O parser rejeita:</p>
 * <ul>
 *   <li>Opções desconhecidas (não declaradas nos {@code Spec})</li>
 *   <li>Opções com valor declaradas sem valor (faltando o argumento seguinte)</li>
 *   <li>Opções repetidas</li>
 *   <li>Argumentos posicionais (qualquer token sem {@code --} prefixo gera erro)</li>
 * </ul>
 *
 * <p>Uso típico:</p>
 * <pre>
 *   Args args = Args.parser()
 *       .value("--type")
 *       .value("--port")
 *       .flag("--server")
 *       .parse(rawArgs);
 *   String type = args.required("--type");
 *   boolean server = args.flag("--server");
 * </pre>
 */
public final class Args {

    private final Map<String, String> values;
    private final Set<String> flags;

    private Args(final Map<String, String> values, final Set<String> flags) {
        this.values = values;
        this.flags = flags;
    }

    /**
     * @return novo {@link Builder} para configurar opções suportadas.
     */
    public static Builder parser() {
        return new Builder();
    }

    /**
     * Retorna o valor da opção, ou {@code null} se ausente.
     */
    public String get(final String name) {
        return values.get(name);
    }

    /**
     * Retorna o valor da opção ou lança erro se ausente.
     */
    public String required(final String name) {
        final String v = values.get(name);
        if (v == null) {
            throw new ArgsException("opção obrigatória ausente: " + name);
        }
        return v;
    }

    /**
     * Verifica se uma flag booleana foi fornecida.
     */
    public boolean flag(final String name) {
        return flags.contains(name);
    }

    /**
     * Builder fluido para declarar opções.
     */
    public static final class Builder {
        private final Set<String> valueOpts = new LinkedHashSet<>();
        private final Set<String> flagOpts = new LinkedHashSet<>();

        public Builder value(final String name) {
            requireOptionName(name);
            valueOpts.add(name);
            return this;
        }

        public Builder flag(final String name) {
            requireOptionName(name);
            flagOpts.add(name);
            return this;
        }

        /**
         * Faz o parsing dos argumentos brutos.
         *
         * @throws ArgsException quando há violação das regras
         */
        public Args parse(final String[] args) {
            final Map<String, String> values = new LinkedHashMap<>();
            final Set<String> flags = new LinkedHashSet<>();

            int i = 0;
            while (i < args.length) {
                final String token = args[i];

                if (!token.startsWith("--")) {
                    throw new ArgsException(
                            "argumento posicional não suportado: '" + token + "'");
                }

                final String name;
                final String inlineValue;
                final int eq = token.indexOf('=');
                if (eq >= 0) {
                    name = token.substring(0, eq);
                    inlineValue = token.substring(eq + 1);
                } else {
                    name = token;
                    inlineValue = null;
                }

                if (valueOpts.contains(name)) {
                    if (values.containsKey(name)) {
                        throw new ArgsException("opção repetida: " + name);
                    }
                    final String v;
                    if (inlineValue != null) {
                        v = inlineValue;
                        i++;
                    } else {
                        if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                            throw new ArgsException(
                                    "opção '" + name + "' requer um valor");
                        }
                        v = args[i + 1];
                        i += 2;
                    }
                    values.put(name, v);
                } else if (flagOpts.contains(name)) {
                    if (inlineValue != null) {
                        throw new ArgsException(
                                "flag booleana '" + name + "' não aceita valor");
                    }
                    if (!flags.add(name)) {
                        throw new ArgsException("flag repetida: " + name);
                    }
                    i++;
                } else {
                    throw new ArgsException("opção desconhecida: " + name);
                }
            }

            return new Args(
                    Collections.unmodifiableMap(values),
                    Collections.unmodifiableSet(flags));
        }

        private static void requireOptionName(final String name) {
            if (name == null || !name.startsWith("--") || name.length() < 3) {
                throw new IllegalArgumentException(
                        "nome de opção inválido: '" + name + "' (deve começar com '--')");
            }
        }
    }
}
