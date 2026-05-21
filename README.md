# Sistema Runner

Trabalho prático da disciplina **Implementação e Integração de Software** (INF0466)
Bacharelado em Engenharia de Software — UFG · 2026/1

## Sobre o projeto

O Sistema Runner facilita a execução de aplicações Java via linha de comandos,
sem que o usuário precise conhecer detalhes de configuração do ambiente Java.
O projeto é de interesse real da Secretaria de Estado de Saúde de Goiás (SES-GO)
e da UFG, no contexto de uma plataforma de interoperabilidade de dados em saúde
(HubSaúde / FHIR).

## Status

Legenda: ✅ pronto · 🟡 em andamento · ⬜ pendente

| US | Descrição | Status |
|---|---|---|
| US-01.1 | CLI base `assinatura` (Cobra, `version`) | ✅ |
| US-01.2 | Subcomandos `sign` e `validate` no CLI | ✅ |
| US-01.3 | Invocação direta do JAR (modo local) | ✅ |
| US-01.4 | Formatação legível das respostas e erros | ✅ |
| US-01.5 | `start` em modo servidor (HTTP) | ⬜ |
| US-01.6 | Modo servidor por padrão, fallback `--local` | ⬜ |
| US-01.7 | Detectar instância ativa e reusar | ⬜ |
| US-01.8 | `stop` do servidor | ⬜ |
| US-01.9 | Timeout de inatividade | ⬜ |
| US-02.1 | `FakeSignatureService` + modelos FHIR | ✅ |
| US-02.2 | Validação de parâmetros de criação | ✅ |
| US-02.3 | Validação + simulação de `validate` | ✅ |
| US-02.4 | Servidor HTTP embarcado | ⬜ |
| US-02.5 | Integração PKCS#11 (token/smart card) | ⬜ |
| US-03.1 | CLI simulador: `start` | ⬜ |
| US-03.2 | Verificar portas disponíveis | ⬜ |
| US-03.3 | CLI simulador: `stop` / `status` | ⬜ |
| US-03.4 | Download do `simulador.jar` via GitHub Releases | ⬜ |
| US-04.1 | Detecção e provisionamento automático de JDK 21 | ✅ |
| US-05.1 | Binários multiplataforma (Linux/Windows/macOS amd64) | ✅ |
| US-05.2 | Assinatura Cosign + checksums SHA256 | ✅ |

Cronograma semanal completo em [planejamento-semanal.md](../planejamento-semanal.md)
(localizado no diretório-pai do repositório).

## Componentes

| Componente | Caminho | Linguagem | Descrição |
|---|---|---|---|
| `assinatura` | [apps/assinatura/](apps/assinatura/) | Go | CLI multiplataforma — interface principal do usuário |
| `assinador` | [apps/assinador/](apps/assinador/) | Java 21 | Valida parâmetros e simula operações de assinatura digital (FHIR) |
| `simulador` | [apps/simulador/](apps/simulador/) | Go | CLI para gerenciar o ciclo de vida do Simulador do HubSaúde |

## Estrutura do repositório

```
runner/
├── apps/
│   ├── assinatura/        # CLI principal (Go)
│   ├── simulador/         # CLI do simulador (Go)
│   └── assinador/         # assinador.jar (Java/Maven)
├── internal/              # Pacotes Go compartilhados
│   ├── invoker/           # Invocação do JAR (subprocess/HTTP)
│   └── jdk/               # Detecção/provisionamento de JDK
├── docs/                  # Documentação do projeto
├── .github/workflows/     # Pipelines CI/CD
├── go.mod                 # Módulo Go único na raiz
├── README.md
├── CLAUDE.md
└── LICENSE
```

## Documentação

- [Especificação do sistema](docs/especificacao.md) — visão geral, escopo, user stories, requisitos
- [Design (C4 Model)](docs/design.md) — diagramas de contexto e contêineres
- [Plano de implementação](docs/planejamento.md) — estratégia de testes, padrões de qualidade (ISO 25010)
- [Plano revisitado v2](docs/plano-revisitado-v2.md) — sprints e matriz de rastreabilidade
- [Guia para Claude Code](CLAUDE.md) — comandos e convenções para o agente

## Pré-requisitos

- **Go 1.25+** — para compilar `assinatura` e `simulador`
- **JDK 21+** — para o `assinador`. Provisionado automaticamente pelo CLI quando ausente
- **Git** — controle de versão

## Como executar

A partir da raiz do repositório:

```bash
# Lint + testes Go
go vet ./...
go test ./...

# Compilar os CLIs Go
go build -o assinatura ./apps/assinatura
go build -o simulador  ./apps/simulador

# Compilar o assinador.jar (Java)
cd apps/assinador
./mvnw test
./mvnw package          # gera target/assinador-*.jar
cd ../..

# Executar (exemplo)
./assinatura sign \
  --type 1.2.840.10065.1.12.1.1 \
  --when 2026-04-08T12:00:00Z \
  --who Practitioner/123 \
  --target Bundle/abc-001 \
  --sig-format application/jose \
  --jar apps/assinador/target/assinador-0.1.0-SNAPSHOT.jar
```

### Testes de integração end-to-end

```bash
$env:JAR_PATH = "$PWD/apps/assinador/target/assinador-0.1.0-SNAPSHOT.jar"  # PowerShell
go test -tags integration ./internal/invoker/...
```

## Releases

Os binários são publicados via [GitHub Releases](https://github.com/kyriosdata/runner/releases),
assinados com [Cosign](https://github.com/sigstore/cosign) (keyless OIDC) e
acompanhados de checksums SHA256.

```bash
cosign verify-blob \
  --certificate assinatura-vX.Y.Z-linux-amd64.pem \
  --signature   assinatura-vX.Y.Z-linux-amd64.sig \
  assinatura-vX.Y.Z-linux-amd64
```

## Licença

Apache 2.0 — veja [LICENSE](LICENSE).
