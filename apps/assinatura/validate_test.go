package main

import (
	"bytes"
	"testing"
)

func TestValidateCmdRequiresAllFlags(t *testing.T) {
	cmd := rootCmd
	cmd.SetArgs([]string{"validate"})
	buf := new(bytes.Buffer)
	cmd.SetErr(buf)

	err := cmd.Execute()
	if err == nil {
		t.Fatal("expected error when required flags are missing")
	}
}
