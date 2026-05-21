package main

import (
	"fmt"
	"os"
	"time"

	"github.com/kyriosdata/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var signFlags struct {
	signType  string
	when      string
	who       string
	target    string
	sigFormat string
	local     bool
}

var signCmd = &cobra.Command{
	Use:   "sign",
	Short: "Cria uma assinatura digital simulada via assinador.jar",
	Long: `Envia uma requisição de criação de assinatura ao assinador.jar.

Os parâmetros seguem o modelo FHIR Provenance.signature. Todos são
obrigatórios. O instante (--when) deve estar no formato ISO-8601.

Exemplo:
  assinatura sign \
    --type 1.2.840.10065.1.12.1.1 \
    --when 2026-04-08T12:00:00Z \
    --who Practitioner/123 \
    --target Bundle/abc-001 \
    --sig-format application/jose`,
	RunE: runSign,
}

func init() {
	f := signCmd.Flags()
	f.StringVar(&signFlags.signType, "type", "", "OID do tipo de assinatura (obrigatório)")
	f.StringVar(&signFlags.when, "when", "", "Instante ISO-8601 da assinatura (obrigatório)")
	f.StringVar(&signFlags.who, "who", "", "Referência ao signatário — ResourceType/id (obrigatório)")
	f.StringVar(&signFlags.target, "target", "", "Referência ao recurso assinado — ResourceType/id (obrigatório)")
	f.StringVar(&signFlags.sigFormat, "sig-format", "", "Mime-type do formato de assinatura (obrigatório)")
	f.BoolVar(&signFlags.local, "local", false, "Força invocação direta (modo local) em vez do servidor HTTP")

	_ = signCmd.MarkFlagRequired("type")
	_ = signCmd.MarkFlagRequired("when")
	_ = signCmd.MarkFlagRequired("who")
	_ = signCmd.MarkFlagRequired("target")
	_ = signCmd.MarkFlagRequired("sig-format")

	rootCmd.AddCommand(signCmd)
}

func runSign(cmd *cobra.Command, args []string) error {
	if _, err := time.Parse(time.RFC3339, signFlags.when); err != nil {
		return fmt.Errorf("--when deve estar no formato ISO-8601 (ex.: 2026-04-08T12:00:00Z): %w", err)
	}

	jar, err := resolveJar(jarPath)
	if err != nil {
		return err
	}

	inv, err := newLocalInvoker(jar)
	if err != nil {
		return err
	}

	params := invoker.SignParams{
		Type:      signFlags.signType,
		When:      signFlags.when,
		Who:       signFlags.who,
		Target:    signFlags.target,
		SigFormat: signFlags.sigFormat,
	}

	result, err := inv.Sign(params)
	if err != nil {
		return fmt.Errorf("falha ao invocar o assinador.jar: %w", err)
	}

	if result.ExitCode != 0 {
		fmt.Fprint(os.Stderr, invoker.FormatError(result))
		os.Exit(result.ExitCode)
	}

	out, err := invoker.ParseSignOutput(result.Stdout)
	if err != nil {
		return fmt.Errorf("resposta inesperada do assinador.jar: %w", err)
	}

	fmt.Fprint(cmd.OutOrStdout(), invoker.FormatSign(out))
	return nil
}
