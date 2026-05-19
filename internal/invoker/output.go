package invoker

import (
	"fmt"
	"strings"
)

// SignOutput holds the parsed fields from a successful "sign" response.
type SignOutput struct {
	SignatureID string
	When        string
	SigFormat   string
	Data        string
}

// ValidateOutput holds the parsed fields from a successful "validate" response.
type ValidateOutput struct {
	Valid  bool
	Reason string
}

// ParseSignOutput parses the key=value stdout of a JAR sign response.
func ParseSignOutput(stdout string) (*SignOutput, error) {
	kv, err := parseKeyValue(stdout)
	if err != nil {
		return nil, err
	}
	out := &SignOutput{
		SignatureID: kv["signatureId"],
		When:        kv["when"],
		SigFormat:   kv["sigFormat"],
		Data:        kv["data"],
	}
	if out.SignatureID == "" || out.Data == "" {
		return nil, fmt.Errorf("resposta incompleta do assinador.jar: %q", stdout)
	}
	return out, nil
}

// ParseValidateOutput parses the key=value stdout of a JAR validate response.
func ParseValidateOutput(stdout string) (*ValidateOutput, error) {
	kv, err := parseKeyValue(stdout)
	if err != nil {
		return nil, err
	}
	raw, ok := kv["valid"]
	if !ok {
		return nil, fmt.Errorf("resposta incompleta do assinador.jar: campo 'valid' ausente")
	}
	return &ValidateOutput{
		Valid:  raw == "true",
		Reason: kv["reason"],
	}, nil
}

// FormatSign formats a SignOutput for human-readable display.
func FormatSign(o *SignOutput) string {
	var sb strings.Builder
	sb.WriteString("Assinatura criada com sucesso\n")
	sb.WriteString("  ID:        " + o.SignatureID + "\n")
	sb.WriteString("  Instante:  " + o.When + "\n")
	sb.WriteString("  Formato:   " + o.SigFormat + "\n")
	sb.WriteString("  Dados:     " + truncate(o.Data, 64) + "\n")
	return sb.String()
}

// FormatValidate formats a ValidateOutput for human-readable display.
func FormatValidate(o *ValidateOutput) string {
	if o.Valid {
		return "Assinatura válida\n  Motivo: " + o.Reason + "\n"
	}
	return "Assinatura inválida\n  Motivo: " + o.Reason + "\n"
}

// FormatError formats a JAR error (stderr + exit code) for the user.
func FormatError(result *Result) string {
	msg := strings.TrimSpace(result.Stderr)
	if msg == "" {
		msg = strings.TrimSpace(result.Stdout)
	}
	return fmt.Sprintf("Erro do assinador.jar (código %d): %s\n", result.ExitCode, msg)
}

func parseKeyValue(s string) (map[string]string, error) {
	m := make(map[string]string)
	for _, line := range strings.Split(strings.TrimSpace(s), "\n") {
		line = strings.TrimRight(line, "\r")
		if line == "" {
			continue
		}
		idx := strings.Index(line, "=")
		if idx < 0 {
			return nil, fmt.Errorf("linha sem '=' na saída do assinador.jar: %q", line)
		}
		m[line[:idx]] = line[idx+1:]
	}
	return m, nil
}

func truncate(s string, max int) string {
	if len(s) <= max {
		return s
	}
	return s[:max] + "…"
}
