package invoker

import (
	"bytes"
	"fmt"
	"os/exec"
)

// LocalInvoker invoca o assinador.jar como subprocess via
// "java -jar <jarPath> <subcommand> <args...>".
type LocalInvoker struct {
	// JavaPath é o caminho para o executável java.
	// Se vazio, usa "java" (assume que está no PATH).
	JavaPath string

	// JarPath é o caminho para o assinador.jar.
	JarPath string
}

func (l *LocalInvoker) javaCmd() string {
	if l.JavaPath != "" {
		return l.JavaPath
	}
	return "java"
}

func (l *LocalInvoker) run(args ...string) (*Result, error) {
	cmdArgs := append([]string{"-jar", l.JarPath}, args...)
	cmd := exec.Command(l.javaCmd(), cmdArgs...)

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err := cmd.Run()

	exitCode := 0
	if err != nil {
		if exitErr, ok := err.(*exec.ExitError); ok {
			exitCode = exitErr.ExitCode()
		} else {
			return nil, fmt.Errorf("falha ao executar java: %w", err)
		}
	}

	return &Result{
		ExitCode: exitCode,
		Stdout:   stdout.String(),
		Stderr:   stderr.String(),
	}, nil
}

// Sign invoca "java -jar assinador.jar sign --type ... --when ... etc."
func (l *LocalInvoker) Sign(params SignParams) (*Result, error) {
	return l.run("sign",
		"--type", params.Type,
		"--when", params.When,
		"--who", params.Who,
		"--target", params.Target,
		"--sig-format", params.SigFormat,
	)
}

// Validate invoca "java -jar assinador.jar validate --signature-id ... etc."
func (l *LocalInvoker) Validate(params ValidateParams) (*Result, error) {
	return l.run("validate",
		"--signature-id", params.SignatureID,
		"--target", params.Target,
		"--data", params.Data,
	)
}
