package invoker

import (
	"testing"
)

func TestLocalInvokerJavaCmdDefault(t *testing.T) {
	l := &LocalInvoker{}
	if l.javaCmd() != "java" {
		t.Errorf("expected 'java', got %q", l.javaCmd())
	}
}

func TestLocalInvokerJavaCmdCustom(t *testing.T) {
	l := &LocalInvoker{JavaPath: "/usr/lib/jvm/java-21/bin/java"}
	want := "/usr/lib/jvm/java-21/bin/java"
	if l.javaCmd() != want {
		t.Errorf("expected %q, got %q", want, l.javaCmd())
	}
}

func TestSignParamsFields(t *testing.T) {
	p := SignParams{
		Type:      "1.2.3",
		When:      "2026-04-08T12:00:00Z",
		Who:       "Practitioner/123",
		Target:    "Bundle/abc",
		SigFormat: "application/jose",
	}
	if p.Type != "1.2.3" {
		t.Errorf("unexpected Type: %s", p.Type)
	}
}

func TestValidateParamsFields(t *testing.T) {
	p := ValidateParams{
		SignatureID: "abc-123",
		Target:      "Bundle/abc",
		Data:        "base64data",
	}
	if p.SignatureID != "abc-123" {
		t.Errorf("unexpected SignatureID: %s", p.SignatureID)
	}
}
