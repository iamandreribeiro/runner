package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

// version é injetada em build time via -ldflags "-X main.version=<tag>".
// Deve ser var (não const) para que o linker consiga sobrescrever o valor.
var version = "dev"

var rootCmd = &cobra.Command{
	Use:   "assinatura",
	Short: "CLI do Sistema Runner — invoca o assinador.jar via linha de comandos",
}

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão do CLI",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("assinatura " + version)
	},
}

func init() {
	rootCmd.AddCommand(versionCmd)
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		os.Exit(1)
	}
}
