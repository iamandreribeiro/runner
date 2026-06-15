package cli

import (
	"errors"
	"fmt"
	"testing"
)

func TestExitErrorImplementsError(t *testing.T) {
	var _ error = (*ExitError)(nil)
}

func TestNewExitErrorFormatsMessage(t *testing.T) {
	e := NewExitError(2, "código %d: %s", 42, "boom")
	if e.Code != 2 {
		t.Errorf("Code = %d, want 2", e.Code)
	}
	if e.Message != "código 42: boom" {
		t.Errorf("Message = %q, want 'código 42: boom'", e.Message)
	}
}

func TestExitErrorWorksWithErrorsAs(t *testing.T) {
	wrapped := fmt.Errorf("contexto: %w", NewExitError(3, "interno"))
	var target *ExitError
	if !errors.As(wrapped, &target) {
		t.Fatal("errors.As should unwrap to *ExitError")
	}
	if target.Code != 3 {
		t.Errorf("Code = %d, want 3", target.Code)
	}
}

func TestExitErrorEmptyMessage(t *testing.T) {
	e := NewExitError(1, "")
	if e.Error() != "" {
		t.Errorf("Error() = %q, want empty", e.Error())
	}
}
