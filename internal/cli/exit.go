// Package cli provides shared CLI utilities for the assinatura and simulador
// commands — currently a single error type that propagates a process-exit code
// through cobra's RunE chain instead of calling os.Exit deep inside handlers.
package cli

import "fmt"

// ExitError carries an exit code that main.go should use when the command
// finishes. Returning *ExitError from a cobra RunE keeps defers in scope and
// makes handlers testable (no global os.Exit side effect).
type ExitError struct {
	// Code is the exit status that main() will pass to os.Exit.
	Code int

	// Message is shown to the user on stderr before exit. May be empty,
	// in which case nothing extra is printed.
	Message string
}

// NewExitError builds an ExitError. Use code 0 for "exit cleanly with this
// message" (rare) and non-zero for failures.
func NewExitError(code int, format string, args ...any) *ExitError {
	return &ExitError{Code: code, Message: fmt.Sprintf(format, args...)}
}

// Error implements the error interface.
func (e *ExitError) Error() string {
	return e.Message
}
