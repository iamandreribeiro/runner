package main

import (
	"fmt"
	"os"
	"time"

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

	fmt.Fprintln(os.Stderr, "sign: invocação do assinador.jar ainda não implementada (internal/invoker)")
	fmt.Fprintf(cmd.OutOrStdout(), "sign --type %s --when %s --who %s --target %s --sig-format %s (local=%v)\n",
		signFlags.signType, signFlags.when, signFlags.who, signFlags.target, signFlags.sigFormat, signFlags.local)

	return nil
}
