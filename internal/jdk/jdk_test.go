package jdk

import (
	"os"
	"path/filepath"
	"runtime"
	"testing"
)

func TestParseMajorJDK21(t *testing.T) {
	inputs := []struct {
		output string
		want   int
	}{
		{`openjdk version "21.0.3" 2024-04-16`, 21},
		{`java version "21" 2024-01-01`, 21},
		{`openjdk version "17.0.9"`, 17},
		{`java version "1.8.0_351"`, 1},
		{"unrecognized output", 0},
	}
	for _, tc := range inputs {
		got := parseMajor(tc.output)
		if got != tc.want {
			t.Errorf("parseMajor(%q) = %d, want %d", tc.output, got, tc.want)
		}
	}
}

func TestJavaExecutableWindows(t *testing.T) {
	if runtime.GOOS != "windows" {
		t.Skip("windows-only test")
	}
	exe := javaExecutable("/some/bin")
	if filepath.Base(exe) != "java.exe" {
		t.Errorf("expected java.exe on Windows, got %s", exe)
	}
}

func TestJavaExecutableUnix(t *testing.T) {
	if runtime.GOOS == "windows" {
		t.Skip("unix-only test")
	}
	exe := javaExecutable("/some/bin")
	if filepath.Base(exe) != "java" {
		t.Errorf("expected java on Unix, got %s", exe)
	}
}

func TestCacheDirUnderHome(t *testing.T) {
	dir, err := CacheDir()
	if err != nil {
		t.Fatalf("CacheDir() error: %v", err)
	}
	home, _ := os.UserHomeDir()
	if dir == "" || dir == home {
		t.Errorf("CacheDir() returned unexpected path: %q", dir)
	}
	if filepath.Base(dir) != "jdk" {
		t.Errorf("CacheDir() base should be 'jdk', got %q", filepath.Base(dir))
	}
}

func TestAdoptiumPlatformLinux(t *testing.T) {
	if runtime.GOOS != "linux" {
		t.Skip("linux-only test")
	}
	os_, _ := adoptiumPlatform()
	if os_ != "linux" {
		t.Errorf("expected linux, got %s", os_)
	}
}

func TestAdoptiumPlatformWindows(t *testing.T) {
	if runtime.GOOS != "windows" {
		t.Skip("windows-only test")
	}
	os_, _ := adoptiumPlatform()
	if os_ != "windows" {
		t.Errorf("expected windows, got %s", os_)
	}
}

func TestDetectFromEnvEmptyJAVAHOME(t *testing.T) {
	orig := os.Getenv("JAVA_HOME")
	os.Unsetenv("JAVA_HOME")
	defer os.Setenv("JAVA_HOME", orig)

	if detectFromEnv() != "" {
		t.Error("expected empty result when JAVA_HOME is unset")
	}
}

func TestDetectFromCacheEmptyDir(t *testing.T) {
	tmp := t.TempDir()
	origHome := os.Getenv("HOME")
	if runtime.GOOS == "windows" {
		origHome = os.Getenv("USERPROFILE")
		os.Setenv("USERPROFILE", tmp)
		defer os.Setenv("USERPROFILE", origHome)
	} else {
		os.Setenv("HOME", tmp)
		defer os.Setenv("HOME", origHome)
	}

	if detectFromCache() != "" {
		t.Error("expected empty result for empty cache dir")
	}
}
