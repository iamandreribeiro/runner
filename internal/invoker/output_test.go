package invoker

import (
	"strings"
	"testing"
)

func TestParseSignOutputValid(t *testing.T) {
	stdout := "signatureId=abc-123\nwhen=2026-04-08T12:00:00Z\nsigFormat=application/jose\ndata=ZmFrZQ==\n"
	o, err := ParseSignOutput(stdout)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if o.SignatureID != "abc-123" {
		t.Errorf("SignatureID = %q, want abc-123", o.SignatureID)
	}
	if o.Data != "ZmFrZQ==" {
		t.Errorf("Data = %q, want ZmFrZQ==", o.Data)
	}
}

func TestParseSignOutputMissingField(t *testing.T) {
	_, err := ParseSignOutput("when=2026-04-08T12:00:00Z\n")
	if err == nil {
		t.Fatal("expected error for missing signatureId")
	}
}

func TestParseSignOutputInvalidLine(t *testing.T) {
	_, err := ParseSignOutput("linha-sem-igual\n")
	if err == nil {
		t.Fatal("expected error for line without '='")
	}
}

func TestParseValidateOutputTrue(t *testing.T) {
	o, err := ParseValidateOutput("valid=true\nreason=ok\n")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !o.Valid {
		t.Error("expected Valid=true")
	}
	if o.Reason != "ok" {
		t.Errorf("Reason = %q, want ok", o.Reason)
	}
}

func TestParseValidateOutputFalse(t *testing.T) {
	o, err := ParseValidateOutput("valid=false\nreason=assinatura inválida\n")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if o.Valid {
		t.Error("expected Valid=false")
	}
}

func TestParseValidateOutputMissingValid(t *testing.T) {
	_, err := ParseValidateOutput("reason=ok\n")
	if err == nil {
		t.Fatal("expected error for missing 'valid' field")
	}
}

func TestFormatSignContainsID(t *testing.T) {
	o := &SignOutput{SignatureID: "id-42", When: "now", SigFormat: "application/jose", Data: "abc"}
	s := FormatSign(o)
	if !strings.Contains(s, "id-42") {
		t.Errorf("FormatSign output does not contain signature ID: %q", s)
	}
}

func TestFormatValidateValid(t *testing.T) {
	s := FormatValidate(&ValidateOutput{Valid: true, Reason: "ok"})
	if !strings.Contains(s, "válida") {
		t.Errorf("FormatValidate output missing 'válida': %q", s)
	}
}

func TestFormatValidateInvalid(t *testing.T) {
	s := FormatValidate(&ValidateOutput{Valid: false, Reason: "divergente"})
	if !strings.Contains(s, "inválida") {
		t.Errorf("FormatValidate output missing 'inválida': %q", s)
	}
}

func TestFormatErrorShowsExitCode(t *testing.T) {
	r := &Result{ExitCode: 2, Stderr: "Parâmetro inválido"}
	s := FormatError(r)
	if !strings.Contains(s, "código 2") {
		t.Errorf("FormatError missing exit code: %q", s)
	}
	if !strings.Contains(s, "Parâmetro inválido") {
		t.Errorf("FormatError missing stderr message: %q", s)
	}
}

func TestTruncateShort(t *testing.T) {
	if truncate("abc", 10) != "abc" {
		t.Error("short string should not be truncated")
	}
}

func TestTruncateLong(t *testing.T) {
	result := truncate("abcdefgh", 4)
	if !strings.HasPrefix(result, "abcd") {
		t.Errorf("unexpected truncation: %q", result)
	}
	if !strings.HasSuffix(result, "…") {
		t.Errorf("missing ellipsis: %q", result)
	}
}
