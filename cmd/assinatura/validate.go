package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var validateFlags struct {
	signatureID string
	target      string
	data        string
	local       bool
}

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Valida uma assinatura digital simulada via assinador.jar",
	Long: `Envia uma requisição de validação de assinatura ao assinador.jar.

Exemplo:
  assinatura validate \
    --signature-id 550e8400-e29b-41d4-a716-446655440000 \
    --target Bundle/abc-001 \
    --data <base64>`,
	RunE: runValidate,
}

func init() {
	f := validateCmd.Flags()
	f.StringVar(&validateFlags.signatureID, "signature-id", "", "UUID da assinatura a validar (obrigatório)")
	f.StringVar(&validateFlags.target, "target", "", "Referência ao recurso assinado — ResourceType/id (obrigatório)")
	f.StringVar(&validateFlags.data, "data", "", "Conteúdo da assinatura em Base64 (obrigatório)")
	f.BoolVar(&validateFlags.local, "local", false, "Força invocação direta (modo local) em vez do servidor HTTP")

	_ = validateCmd.MarkFlagRequired("signature-id")
	_ = validateCmd.MarkFlagRequired("target")
	_ = validateCmd.MarkFlagRequired("data")

	rootCmd.AddCommand(validateCmd)
}

func runValidate(cmd *cobra.Command, args []string) error {
	fmt.Fprintln(os.Stderr, "validate: invocação do assinador.jar ainda não implementada (internal/invoker)")
	fmt.Fprintf(cmd.OutOrStdout(), "validate --signature-id %s --target %s --data %s (local=%v)\n",
		validateFlags.signatureID, validateFlags.target, validateFlags.data, validateFlags.local)

	return nil
}
