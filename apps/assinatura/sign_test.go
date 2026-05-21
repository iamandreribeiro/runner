package main

import (
	"bytes"
	"testing"
)

func TestSignCmdRequiresAllFlags(t *testing.T) {
	cmd := rootCmd
	cmd.SetArgs([]string{"sign"})
	buf := new(bytes.Buffer)
	cmd.SetErr(buf)

	err := cmd.Execute()
	if err == nil {
		t.Fatal("expected error when required flags are missing")
	}
}

func TestSignCmdRejectsInvalidWhen(t *testing.T) {
	cmd := rootCmd
	cmd.SetArgs([]string{
		"sign",
		"--type", "1.2.840.10065.1.12.1.1",
		"--when", "not-a-date",
		"--who", "Practitioner/123",
		"--target", "Bundle/abc-001",
		"--sig-format", "application/jose",
	})
	buf := new(bytes.Buffer)
	cmd.SetErr(buf)

	err := cmd.Execute()
	if err == nil {
		t.Fatal("expected error for invalid --when")
	}
}
