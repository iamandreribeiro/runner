package main

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"github.com/kyriosdata/runner/internal/invoker"
	"github.com/kyriosdata/runner/internal/jdk"
)

// resolveJar returns the path to assinador.jar, in priority order:
//  1. --jar flag (explicit)
//  2. ~/.hubsaude/assinador.jar
//  3. ./assinador.jar (working directory)
func resolveJar(flagJarPath string) (string, error) {
	if flagJarPath != "" {
		if _, err := os.Stat(flagJarPath); err == nil {
			return flagJarPath, nil
		}
		return "", fmt.Errorf("assinador.jar não encontrado em: %s", flagJarPath)
	}

	candidates := []string{hubsaudeJarPath(), "assinador.jar"}
	for _, c := range candidates {
		if _, err := os.Stat(c); err == nil {
			return c, nil
		}
	}
	return "", errors.New(
		"assinador.jar não encontrado. Use --jar <caminho> ou copie o JAR para ~/.hubsaude/assinador.jar")
}

// hubsaudeJarPath returns the default JAR location in the user home directory.
func hubsaudeJarPath() string {
	home, err := os.UserHomeDir()
	if err != nil {
		return ""
	}
	return filepath.Join(home, ".hubsaude", "assinador.jar")
}

// newLocalInvoker builds a LocalInvoker, resolving the java executable
// via the jdk package (detects or provisions JDK 21).
func newLocalInvoker(jarPath string) (*invoker.LocalInvoker, error) {
	javaPath, err := jdk.Resolve()
	if err != nil {
		return nil, fmt.Errorf("JDK não disponível: %w", err)
	}
	return &invoker.LocalInvoker{JavaPath: javaPath, JarPath: jarPath}, nil
}
