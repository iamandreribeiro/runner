// Package invoker define o contrato e as implementações para invocação
// do assinador.jar — tanto via subprocess (modo local) quanto via HTTP
// (modo servidor).
package invoker

// Result representa a resposta bruta retornada pelo assinador.jar,
// independentemente do modo de invocação.
type Result struct {
	// ExitCode é o código de saída do processo (modo local) ou o
	// status HTTP mapeado para código equivalente (modo servidor).
	ExitCode int

	// Stdout contém a saída-padrão do processo ou o corpo da resposta HTTP.
	Stdout string

	// Stderr contém a saída de erro do processo (vazio no modo servidor).
	Stderr string
}

// Invoker é a interface que abstrai o mecanismo de comunicação com o
// assinador.jar. Permite que o CLI alterne entre modo local e servidor
// de forma transparente.
type Invoker interface {
	// Sign envia os parâmetros de criação de assinatura e retorna o resultado bruto.
	Sign(params SignParams) (*Result, error)

	// Validate envia os parâmetros de validação e retorna o resultado bruto.
	Validate(params ValidateParams) (*Result, error)
}

// SignParams agrupa os parâmetros que o CLI repassa ao assinador.jar
// para a operação de criação de assinatura.
type SignParams struct {
	Type      string
	When      string // ISO-8601
	Who       string
	Target    string
	SigFormat string
}

// ValidateParams agrupa os parâmetros para a operação de validação.
type ValidateParams struct {
	SignatureID string
	Target      string
	Data        string // Base64
}
