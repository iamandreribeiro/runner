package jdk

import (
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
)

var javaVersionRe = regexp.MustCompile(`(?:java|openjdk)\s+version\s+"?(\d+)`)

// detectFromEnv checks JAVA_HOME for a compatible JDK.
func detectFromEnv() string {
	javaHome := os.Getenv("JAVA_HOME")
	if javaHome == "" {
		return ""
	}
	candidate := javaExecutable(filepath.Join(javaHome, "bin"))
	if isCompatible(candidate) {
		return candidate
	}
	return ""
}

// detectFromPath checks whether "java" on PATH satisfies the requirement.
func detectFromPath() string {
	path, err := exec.LookPath("java")
	if err != nil {
		return ""
	}
	if isCompatible(path) {
		return path
	}
	return ""
}

// detectFromCache looks for a previously downloaded JDK under CacheDir.
func detectFromCache() string {
	dir, err := CacheDir()
	if err != nil {
		return ""
	}
	entries, err := os.ReadDir(dir)
	if err != nil {
		return ""
	}
	for _, e := range entries {
		if !e.IsDir() {
			continue
		}
		candidate := javaExecutable(filepath.Join(dir, e.Name(), "bin"))
		if isCompatible(candidate) {
			return candidate
		}
	}
	return ""
}

// isCompatible runs "java -version" and checks the major version.
func isCompatible(javaPath string) bool {
	if javaPath == "" {
		return false
	}
	if _, err := os.Stat(javaPath); err != nil {
		return false
	}
	out, err := exec.Command(javaPath, "-version").CombinedOutput()
	if err != nil {
		return false
	}
	return parseMajor(string(out)) >= requiredMajor
}

// parseMajor extracts the major version number from "java -version" output.
func parseMajor(output string) int {
	m := javaVersionRe.FindStringSubmatch(output)
	if m == nil {
		return 0
	}
	major, err := strconv.Atoi(strings.TrimSpace(m[1]))
	if err != nil {
		return 0
	}
	return major
}

// javaExecutable returns the platform-specific java binary path inside a bin dir.
func javaExecutable(binDir string) string {
	name := "java"
	if isWindows() {
		name = "java.exe"
	}
	return filepath.Join(binDir, name)
}
