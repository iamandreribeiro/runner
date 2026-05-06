// Package jdk detects a JDK 21 installation on the machine and, when absent,
// downloads and caches one from the Eclipse Adoptium distribution.
//
// Resolution order:
//  1. JAVA_HOME environment variable (if set and points to JDK 21+)
//  2. "java" on PATH (if version is 21+)
//  3. Cached JDK under ~/.hubsaude/jdk/
//  4. Automatic download from Adoptium into ~/.hubsaude/jdk/
package jdk

import (
	"fmt"
	"os"
	"path/filepath"
)

const requiredMajor = 21

// Resolve returns the absolute path to a java executable that satisfies
// JDK 21+. It downloads and caches a JDK if none is found locally.
func Resolve() (string, error) {
	if p := detectFromEnv(); p != "" {
		return p, nil
	}
	if p := detectFromPath(); p != "" {
		return p, nil
	}
	if p := detectFromCache(); p != "" {
		return p, nil
	}
	return provision()
}

// CacheDir returns the directory where downloaded JDKs are stored.
func CacheDir() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("não foi possível determinar o diretório home: %w", err)
	}
	return filepath.Join(home, ".hubsaude", "jdk"), nil
}
