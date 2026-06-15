package main

import (
	"errors"
	"fmt"
	"os"

	"github.com/kyriosdata/runner/internal/cli"
	"github.com/spf13/cobra"
)

// version é injetada em build time via -ldflags "-X main.version=<tag>".
// Deve ser var (não const) para que o linker consiga sobrescrever o valor.
var version = "dev"

// jarPath pode ser sobrescrito pela flag global --jar.
var jarPath string

var rootCmd = &cobra.Command{
	Use:           "assinatura",
	Short:         "CLI do Sistema Runner — invoca o assinador.jar via linha de comando",
	SilenceErrors: true,
	SilenceUsage:  true,
}

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão do CLI",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("assinatura " + version)
	},
}

func init() {
	rootCmd.PersistentFlags().StringVar(&jarPath, "jar", "",
		"Caminho para o assinador.jar (padrão: ~/.hubsaude/assinador.jar)")
	rootCmd.AddCommand(versionCmd)
}

func main() {
	err := rootCmd.Execute()
	if err == nil {
		return
	}
	var exitErr *cli.ExitError
	if errors.As(err, &exitErr) {
		fmt.Fprint(os.Stderr, exitErr.Message)
		os.Exit(exitErr.Code)
	}
	fmt.Fprintln(os.Stderr, "Erro:", err)
	os.Exit(1)
}
