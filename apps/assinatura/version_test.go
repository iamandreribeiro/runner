package main_test

import (
	"os/exec"
	"strings"
	"testing"
)

// TestVersionSubcommand verifica que o subcomando version exibe a versão correta.
func TestVersionSubcommand(t *testing.T) {
	cmd := exec.Command("go", "run", ".", "version")
	cmd.Dir = "."

	out, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("falha ao executar 'go run . version': %v\n%s", err, out)
	}

	output := strings.TrimSpace(string(out))
	if !strings.Contains(output, "dev") {
		t.Errorf("esperava saída contendo 'dev', obteve: %q", output)
	}
}
