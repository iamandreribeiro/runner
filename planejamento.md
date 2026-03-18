# Plano de Implementação — Sistema Runner
**Disciplina:** Implementação e Integração de Software (INF0466)  
**Semestre:** 2026/1 · Turma B · Prof. Fabio Nogueira de Lucena  
**Data de elaboração:** 18/03/2026  
**Encerramento:** 17/06/2026

---

## 1. Visão Geral do Projeto

O **Sistema Runner** tem como objetivo facilitar a execução de aplicações Java via linha de comandos, sem que o usuário precise conhecer detalhes de configuração do ambiente Java. O sistema é composto por três componentes principais:

| Componente | Tecnologia | Descrição |
|---|---|---|
| `assinatura` | CLI multiplataforma | Interface de linha de comandos para o usuário final |
| `assinador.jar` | Java | Valida parâmetros e simula operações de assinatura digital FHIR |
| `simulador` | CLI multiplataforma | Gerencia o ciclo de vida do Simulador do HubSaúde |

O sistema é de interesse real da **SES-GO** e da **UFG** no contexto de interoperabilidade de dados em saúde.

---

## 2. Requisitos a Implementar

| ID | História de Usuário | Prioridade |
|---|---|---|
| US-01 | Invocar Assinador via CLI (modos direto e HTTP) | Alta |
| US-02 | Simular assinatura digital com validação de parâmetros FHIR | Alta |
| US-03 | Gerenciar ciclo de vida do Simulador do HubSaúde | Média |
| US-04 | Provisionar JDK automaticamente | Alta |
| US-05 | Disponibilizar binários multiplataforma (Win/Linux/macOS) | Alta |

> **Meta mínima (nota 6,0):** software funcionando cobrindo US-01, US-02 e US-04.  
> **Meta completa (nota 10,0):** todos os requisitos + qualidade ISO/IEC 25010:2023.

---

## 3. Plano de Garantia de Qualidade

### 3.1 Estratégia de Testes

| Nível | Ferramenta | Cobertura-alvo |
|---|---|---|
| Testes de unidade | JUnit 5 (Java) + biblioteca nativa da linguagem do CLI | ≥ 80% das classes do `assinador.jar` |
| Testes de integração | Scripts de integração + testes e2e CLI → JAR | Todos os fluxos das US |
| Testes de aceitação | Critérios de aceitação das user stories (BDD / Cucumber) | 100% dos critérios definidos |
| Testes de erro | Parametrização de entradas inválidas | Todos os casos de erro documentados |

### 3.2 Inspeção de Código

- **Revisão por pares** antes de todo merge na branch `main` (pull request obrigatório).
- **Linting** automatizado no pipeline CI (ex: Checkstyle para Java, equivalente para CLI).
- **Inspeção periódica** a cada iteração, registrada no repositório como issue ou comentário de PR.

### 3.3 Requisitos de Qualidade (ISO/IEC 25010:2023)

Para além do software "funcionando", serão observadas as seguintes características:

| Característica | Como será atendida |
|---|---|
| **Adequação funcional** | Critérios de aceitação cobrindo 100% das US |
| **Eficiência de desempenho** | Modo servidor (warm start) para reduzir latência; medição documentada |
| **Compatibilidade** | Build e teste nas 3 plataformas (Win/Linux/macOS) via GitHub Actions |
| **Usabilidade** | Mensagens de erro claras e úteis; documentação de uso (manual) |
| **Confiabilidade** | Tratamento de exceções em todos os pontos críticos; testes de cenários de falha |
| **Segurança** | Assinatura de artefatos com Cosign/Sigstore; checksums SHA256 |
| **Manutenibilidade** | Código limpo (Clean Code), padrões definidos abaixo, refatoração contínua |
| **Portabilidade** | Binários compilados para amd64 nos 3 SOs via CI/CD |

---

## 4. Plano de Gerência de Configuração

### 4.1 Estrutura de Branches

```
main              ← produção / releases estáveis
├── develop       ← integração contínua das features
│   ├── feat/us01-cli-assinatura
│   ├── feat/us02-assinador-jar
│   ├── feat/us03-simulador-cli
│   ├── feat/us04-provisionar-jdk
│   └── feat/us05-binarios-multiplataforma
└── fix/*         ← correção de defeitos
```

**Regras:**
- Nenhum commit direto em `main` — apenas via PR aprovado.
- Branch de feature: ramificar de `develop`, rebase antes do PR, squash ao fazer merge.
- Toda aula: executar `git pull --rebase` para manter repositório atualizado.

### 4.2 Convenções de Commit (Conventional Commits)

```
feat(us01): adiciona modo servidor HTTP no CLI assinatura
fix(us02): corrige validação de parâmetro CPF no assinador.jar
test(us02): adiciona casos de teste para parâmetros inválidos
docs: atualiza manual de usuário com exemplos de uso
chore(ci): configura pipeline de build multiplataforma
```

### 4.3 Versionamento

- Seguir **SemVer** (`MAJOR.MINOR.PATCH`).
- Releases publicadas via **GitHub Releases** com artefatos assinados por Cosign.
- Arquivos obrigatórios por artefato: `<artefato>`, `<artefato>.sig`, `<artefato>.pem`.

### 4.4 Build e Entrega

- **Build automatizado** via GitHub Actions a cada push em `develop`.
- **Release pipeline** disparado por tag `vX.Y.Z` em `main`:
  1. Compilar e testar nas 3 plataformas (matrix build).
  2. Gerar binários: `.exe` (Windows), `.AppImage` (Linux), `.dmg` (macOS).
  3. Assinar com Cosign via OIDC.
  4. Publicar na GitHub Release com checksums SHA256.

---

## 5. Padrões de Codificação

### 5.1 Geral (todas as linguagens)

- Nomes em **inglês** para código; comentários e documentação em **português**.
- Funções/métodos com responsabilidade única (máx. ~30 linhas de corpo).
- Sem números mágicos — usar constantes nomeadas.
- Todo `catch` deve tratar ou relançar com contexto; nunca silenciar exceções.
- Logging estruturado nos pontos de entrada/saída de operações relevantes.

### 5.2 Java (`assinador.jar`)

- Estilo: **Google Java Style Guide** (verificado por Checkstyle).
- Imutabilidade preferida; `final` onde possível.
- Javadoc obrigatório em classes públicas e métodos de interface.
- Exceções: tipos específicos, nunca `Exception` genérico capturado em silêncio.

### 5.3 CLI (`assinatura` / `simulador`)

- A definir conforme linguagem escolhida (Go ou Rust recomendados para binários nativos).
- Tratamento de sinais de SO (SIGINT, SIGTERM) para encerramento gracioso.
- Saída estruturada: `stdout` para resultados, `stderr` para erros e logs.
- Código de saída: `0` para sucesso, `>0` para erro (documentados no manual).

---

## 6. Cronograma de Iterações

O desenvolvimento é organizado em **iterações curtas**, alinhadas ao cronograma da disciplina. Cada iteração termina com algo funcionando e registrado no repositório.

---

### Iteração 0 — Planejamento e Ambiente (18/03/2026)
**Objetivos:** Concluir este plano e preparar o ambiente de desenvolvimento.

- [ ] Configurar repositório: branches `main` e `develop`, `.gitignore`, licença.
- [ ] Definir e documentar padrões de codificação (este documento).
- [ ] Configurar pipeline CI inicial (lint + build no push).
- [ ] Escolher linguagem e toolchain do CLI (`assinatura` / `simulador`).
- [ ] Criar estrutura de diretórios dos projetos:
  ```
  /assinatura/   ← CLI principal
  /assinador/    ← projeto Java
  /simulador/    ← CLI do simulador
  /docs/         ← documentação
  /diagramas/    ← artefatos C4 PlantUML
  ```
- [ ] Registrar decisões arquiteturais (ADRs) no repositório.
- [ ] Revisar e aprimorar a especificação: adicionar requisitos de qualidade (ISO 25010), refinar critérios de aceitação e criar Definition of Done (DoD) para cada US.

**Artefatos:** `planejamento.md` atualizado, estrutura de projeto criada, CI básico funcionando.

---

### Iteração 1 — Arquitetura Detalhada e Esqueleto do Projeto (01/04/2026)
**Objetivos:** Detalhar o design e criar a estrutura mínima compilável.

- [ ] Elaborar diagrama de componentes (nível C4 Componentes) para `assinador.jar`.
- [ ] Definir interfaces entre `assinatura` (CLI) e `assinador.jar` (contrato de comunicação HTTP e CLI).
- [ ] Criar projeto Java (`assinador`) com estrutura básica: `main`, testes, build (Maven ou Gradle).
- [ ] Criar projeto CLI (`assinatura`) com estrutura básica e parsing de argumentos (ex: Cobra/Go ou Clap/Rust).
- [ ] Implementar **esqueleto** de US-04: detecção da presença do JDK (sem download ainda).
- [ ] Criar primeiros testes de unidade (mínimo: validação de 1 parâmetro do assinador).

**Artefatos:** Design detalhado documentado, projetos compilando, pelo menos 1 teste verde.

---

### Iteração 2 — Núcleo do Assinador (22/04/2026)
**Objetivos:** Implementar US-02 — validação de parâmetros e simulação de assinatura.

- [ ] Implementar validação completa de parâmetros no `assinador.jar` (US-02):
  - Validação conforme especificações FHIR.
  - Retornar mensagens de erro claras para cada violação.
- [ ] Implementar simulação de criação de assinatura (resposta pré-construída).
- [ ] Implementar simulação de validação de assinatura (resultado pré-determinado).
- [ ] Cobrir com testes de unidade: casos válidos, casos inválidos, casos de borda.
- [ ] Implementar invocação direta do `assinador.jar` pelo CLI (`assinatura`) — **US-01 modo CLI**.
- [ ] Integrar: testar fluxo completo CLI → JAR via linha de comandos.

**Artefatos:** `assinador.jar` funcional (simulação), CLI invocando JAR diretamente, testes passando.

---

### Iteração 3 — Integração HTTP e Simulador (06/05/2026 e 13/05/2026)
**Objetivos:** Completar US-01 (modo HTTP), US-03 (simulador) e US-04 (download do JDK).

- [ ] Implementar modo servidor HTTP no `assinador.jar` (endpoints para criar e validar assinatura).
- [ ] Implementar invocação via HTTP no CLI `assinatura` (US-01 modo servidor).
- [ ] Implementar CLI `simulador` (US-03):
  - Iniciar, parar e monitorar o `simulador.jar`.
  - Verificar portas disponíveis antes de iniciar.
  - Download automático da versão mais recente via GitHub Releases (sem re-download se já atualizado).
- [ ] Completar US-04: download automático do JDK quando ausente, nas 3 plataformas.
- [ ] Testes de integração: fluxo completo HTTP, gerenciamento do simulador.
- [ ] Revisão e refatoração do código existente (identificar e pagar débitos técnicos).

**Artefatos:** Todos os modos de US-01 funcionando, US-03 e US-04 implementados, testes de integração.

---

### Iteração 4 — Build Multiplataforma, CI/CD e Qualidade (03/06/2026)
**Objetivos:** Implementar US-05 e elevar a qualidade geral para além do 6,0.

- [ ] Configurar **matrix build** no GitHub Actions (Windows, Linux, macOS).
- [ ] Gerar binários nativos para as 3 plataformas.
- [ ] Configurar assinatura de artefatos com **Cosign/Sigstore** no pipeline.
- [ ] Publicar primeira release completa no GitHub Releases.
- [ ] Completar documentação:
  - Manual de usuário para `assinatura` e `simulador`.
  - Documentação técnica da integração.
  - Guia de instalação.
  - Exemplos de uso.
- [ ] Revisar cobertura de testes: atingir ≥ 80% no `assinador.jar`.
- [ ] Executar inspeção final de código (Clean Code, mensagens de erro, logging).
- [ ] Verificar atributos ISO 25010: desempenho, portabilidade, usabilidade, segurança.

**Artefatos:** Release publicada com binários assinados, documentação completa, testes abrangentes.

---

### Fechamento — Revisão Final (17/06/2026)
**Atenção:** Apenas o que estiver no repositório nesta data será considerado para avaliação.

- [ ] Garantir que todos os artefatos finais estão na branch `main`.
- [ ] Verificar que o pipeline CI está verde.
- [ ] Confirmar que a release está publicada no GitHub Releases.
- [ ] Revisar o repositório como um avaliador externo: README claro, documentação acessível.

---

## 7. Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|
| Complexidade do build multiplataforma | Alta | Alto | Iniciar configuração do CI nas primeiras iterações (It. 0) |
| Ambiguidade nos parâmetros FHIR | Média | Alto | Clarificar com o professor até 01/04; registrar decisões como ADR |
| Integração CLI ↔ JAR mais complexa que o esperado | Média | Médio | Prototipagem simples na It. 1 antes de comprometer com design definitivo |
| Download do JDK em plataformas restritas | Baixa | Médio | Testar via CI nas 3 plataformas desde a It. 1 |
| Tempo insuficiente para qualidade além do 6,0 | Média | Médio | Priorizar entregáveis funcionais; investir em qualidade iterativamente |

---

## 8. Checklist de Definition of Done (DoD)

Para cada user story ser considerada concluída:

- [ ] Todos os critérios de aceitação verificados.
- [ ] Testes unitários escritos e passando.
- [ ] Testes de integração relevantes passando.
- [ ] Código revisado por pelo menos um par (PR aprovado).
- [ ] Sem warnings de lint pendentes.
- [ ] Documentação atualizada (manual ou doc técnica, conforme o caso).
- [ ] Comportamento de erro documentado e testado.
- [ ] Artefato registrado no repositório (commit na branch correspondente).

---

## 9. Referências

- Especificação do Sistema Runner: `especificacao.md` (repositório)
- Design (C4 Model): `design.md` (repositório)
- Orientações de planejamento: `docs/planejamento.md` (repositório)
- Ementa da disciplina: Plano de Ensino INF0466 / 2026-1
- McConnell, S. *Code Complete*, 2ª ed. — guia de referência para construção
- Martin, R. C. *Clean Code* — padrões de codificação
- ISO/IEC 25010:2023 — modelo de qualidade de produto de software
- C4 Model: https://c4model.com/
- Sigstore / Cosign: https://docs.sigstore.dev/
