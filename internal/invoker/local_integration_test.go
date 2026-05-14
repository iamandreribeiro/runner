//go:build integration

package invoker_test

import (
	"os"
	"strings"
	"testing"

	"github.com/kyriosdata/runner/internal/invoker"
	"github.com/kyriosdata/runner/internal/jdk"
)

// requireJar returns the JAR path from the JAR_PATH env var or skips the test.
func requireJar(t *testing.T) string {
	t.Helper()
	p := os.Getenv("JAR_PATH")
	if p == "" {
		t.Skip("skipping integration test: set JAR_PATH env var to assinador.jar path")
	}
	if _, err := os.Stat(p); err != nil {
		t.Skipf("JAR_PATH=%q not found: %v", p, err)
	}
	return p
}

func buildLocalInvoker(t *testing.T) *invoker.LocalInvoker {
	t.Helper()
	jarPath := requireJar(t)
	javaPath, err := jdk.Resolve()
	if err != nil {
		t.Fatalf("jdk.Resolve() failed: %v", err)
	}
	return &invoker.LocalInvoker{JavaPath: javaPath, JarPath: jarPath}
}

func TestLocalInvokerSignRoundTrip(t *testing.T) {
	inv := buildLocalInvoker(t)

	result, err := inv.Sign(invoker.SignParams{
		Type:      "1.2.840.10065.1.12.1.1",
		When:      "2026-04-08T12:00:00Z",
		Who:       "Practitioner/123",
		Target:    "Bundle/abc-001",
		SigFormat: "application/jose",
	})
	if err != nil {
		t.Fatalf("Sign() error: %v", err)
	}
	if result.ExitCode != 0 {
		t.Fatalf("Sign() exit %d — stderr: %s", result.ExitCode, result.Stderr)
	}

	out, err := invoker.ParseSignOutput(result.Stdout)
	if err != nil {
		t.Fatalf("ParseSignOutput: %v", err)
	}
	if out.SignatureID == "" {
		t.Error("expected non-empty signatureId")
	}
	if out.Data == "" {
		t.Error("expected non-empty data")
	}

	// Now validate the signature we just got
	vResult, err := inv.Validate(invoker.ValidateParams{
		SignatureID: out.SignatureID,
		Target:      "Bundle/abc-001",
		Data:        out.Data,
	})
	if err != nil {
		t.Fatalf("Validate() error: %v", err)
	}
	if vResult.ExitCode != 0 {
		t.Fatalf("Validate() exit %d — stderr: %s", vResult.ExitCode, vResult.Stderr)
	}

	vOut, err := invoker.ParseValidateOutput(vResult.Stdout)
	if err != nil {
		t.Fatalf("ParseValidateOutput: %v", err)
	}
	if !vOut.Valid {
		t.Errorf("expected valid=true, got reason: %s", vOut.Reason)
	}
}

func TestLocalInvokerSignInvalidParams(t *testing.T) {
	inv := buildLocalInvoker(t)

	result, err := inv.Sign(invoker.SignParams{
		Type:      "invalido",
		When:      "2026-04-08T12:00:00Z",
		Who:       "Practitioner/123",
		Target:    "Bundle/abc-001",
		SigFormat: "application/jose",
	})
	if err != nil {
		t.Fatalf("Sign() unexpected error: %v", err)
	}
	if result.ExitCode == 0 {
		t.Fatal("expected non-zero exit for invalid type")
	}
	combined := result.Stderr + result.Stdout
	if !strings.Contains(combined, "type") {
		t.Errorf("error message should mention 'type', got: %q", combined)
	}
}

func TestLocalInvokerValidateWrongTarget(t *testing.T) {
	inv := buildLocalInvoker(t)

	// Sign first
	sResult, _ := inv.Sign(invoker.SignParams{
		Type:      "1.2.840.10065.1.12.1.1",
		When:      "2026-04-08T12:00:00Z",
		Who:       "Practitioner/123",
		Target:    "Bundle/abc-001",
		SigFormat: "application/jose",
	})
	sOut, _ := invoker.ParseSignOutput(sResult.Stdout)

	// Validate with wrong target
	vResult, err := inv.Validate(invoker.ValidateParams{
		SignatureID: sOut.SignatureID,
		Target:      "Bundle/outro",
		Data:        sOut.Data,
	})
	if err != nil {
		t.Fatalf("Validate() error: %v", err)
	}
	if vResult.ExitCode != 0 {
		t.Fatalf("Validate() exit %d, expected 0 even for invalid result", vResult.ExitCode)
	}
	vOut, _ := invoker.ParseValidateOutput(vResult.Stdout)
	if vOut.Valid {
		t.Error("expected valid=false for wrong target")
	}
}
