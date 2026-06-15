# Sistema Runner — Design

Este documento descreve a arquitetura do Sistema Runner usando o
[modelo C4](https://c4model.com/). Os diagramas são renderizados pelo GitHub
usando [Mermaid](https://mermaid.js.org/).

> Diagramas fonte em PlantUML (.puml) e versões SVG renderizadas serão
> publicados em [`docs/diagramas/`](diagramas/) conforme a Semana 9 do
> [planejamento semanal](../../planejamento-semanal.md).

## 1. Diagrama de Contexto (C4 Nível 1)

Mostra os atores, sistemas externos e o Sistema Runner como uma caixa única.

```mermaid
flowchart TB
    user(["Usuário<br/>(integrador HubSaúde)"])
    runner["Sistema Runner<br/>(CLI multiplataforma)"]
    device[["Dispositivo de<br/>Assinatura Digital<br/>(token / smart card)"]]
    sim[["Simulador do HubSaúde<br/>(aplicação Web externa)"]]

    user -- "comandos<br/>de assinatura" --> runner
    runner -- "PKCS#11" --> device
    runner -- "HTTP" --> sim
```

**Atores e sistemas externos:**

| Elemento | Tipo | Descrição |
|----------|------|-----------|
| Usuário | Ator | Pessoa que interage com o sistema via linha de comandos |
| Dispositivo de Assinatura Digital | Sistema Externo | Hardware criptográfico (token USB, smart card) que armazena certificados e executa operações de assinatura |
| Simulador do HubSaúde | Sistema Externo | Aplicação Web gerida pelo CLI e que responde a requisições de terceiros |

## 2. Diagrama de Contêineres (C4 Nível 2)

Decompõe o Sistema Runner em suas três aplicações.

```mermaid
flowchart LR
    user(["Usuário"])

    subgraph runner ["Sistema Runner"]
        assinatura["assinatura<br/>(Go CLI)"]
        simulador["simulador<br/>(Go CLI)"]
        assinador["assinador.jar<br/>(Java 21)"]
    end

    device[["Dispositivo<br/>Criptográfico"]]
    sim[["Simulador do<br/>HubSaúde"]]

    user -- "CLI: sign/validate" --> assinatura
    user -- "CLI: start/stop/status" --> simulador
    assinatura -- "subprocess / HTTP" --> assinador
    assinador -- "PKCS#11" --> device
    simulador -- "HTTP (lifecycle)" --> sim
```

**Comunicação entre contêineres:**

| Origem | Destino | Protocolo | Descrição |
|--------|---------|-----------|-----------|
| Usuário | assinatura | CLI | Comandos de assinatura (criar, validar) |
| Usuário | simulador | CLI | Comandos de gerenciamento do simulador |
| assinatura | assinador.jar | subprocess OU HTTP | Invocação direta ou requisição HTTP, conforme modo |
| assinador.jar | Dispositivo Criptográfico | PKCS#11 | Interface padrão para tokens e smart cards |
| simulador | Simulador do HubSaúde | HTTP | Invoca e monitora o ciclo de vida do simulador |

## 3. Modos de invocação do assinador.jar

A interação `assinatura → assinador.jar` ocorre em um de dois modos,
escolhidos pelo usuário via flags (`--local` ou padrão servidor):

```mermaid
flowchart TB
    subgraph local ["Modo Local (cold start)"]
        direction LR
        cli1["assinatura<br/>sign --local"] -- "java -jar<br/>(subprocess)" --> jar1["assinador.jar"]
        jar1 -- "stdout/stderr<br/>+ exit code" --> cli1
    end

    subgraph server ["Modo Servidor (warm start)"]
        direction LR
        cli2["assinatura<br/>sign"] -- "POST /sign" --> jar2["assinador.jar<br/>(HTTP daemon)"]
        jar2 -- "JSON response" --> cli2
    end
```

- **Local** (US-01.3): cada execução faz cold-start da JVM. Adequado para
  scripts esporádicos.
- **Servidor** (US-02.4, US-01.5..9): JAR permanece em execução; chamadas
  subsequentes são warm-start (latência baixa).
