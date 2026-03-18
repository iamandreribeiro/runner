# Sistema Runner

Trabalho prático da disciplina **Implementação e Integração de Software** (INF0466)  
Bacharelado em Engenharia de Software — UFG · 2026/1

## Sobre o projeto

O Sistema Runner facilita a execução de aplicações Java via linha de comandos, sem que o usuário precise conhecer detalhes de configuração do ambiente Java. O projeto é de interesse real da Secretaria de Estado de Saúde de Goiás (SES-GO) e da UFG, no contexto de uma plataforma de interoperabilidade de dados em saúde.

## Componentes

| Componente | Linguagem | Descrição |
|---|---|---|
| `assinatura` | Go | CLI multiplataforma — interface principal do usuário |
| `assinador` | Java | Valida parâmetros e simula operações de assinatura digital (FHIR) |
| `simulador` | Go | CLI para gerenciar o ciclo de vida do Simulador do HubSaúde |

## Estrutura do repositório

```
assinatura/     CLI principal (Go)
assinador/      Aplicação Java (assinador.jar)
simulador/      CLI do simulador (Go)
docs/           Documentação do projeto
  planejamento.md   Plano de implementação
diagramas/      Diagramas C4 em PlantUML
```

## Documentação

- [Especificação do sistema](https://github.com/kyriosdata/runner/blob/main/especificacao.md)
- [Design (C4 Model)](https://github.com/kyriosdata/runner/blob/main/design.md)
- [Plano de implementação](docs/planejamento.md)

## Pré-requisitos

- Go 1.22+
- JDK 21+ (provisionado automaticamente pelo sistema quando ausente)
- Git

## Como executar (em construção)

```bash
# Clonar o repositório
git clone <url-do-seu-repo>
cd runner

# Compilar o CLI principal
cd assinatura
go build -o assinatura .

# Compilar o assinador
cd ../assinador
./mvnw package

# Executar
./assinatura assinar --ajuda
```

## Desenvolvimento

Consulte o [CONTRIBUTING.md](CONTRIBUTING.md) para convenções de branch, commits e fluxo de trabalho.

## Licença

Apache 2.0 — veja [LICENSE](LICENSE).
