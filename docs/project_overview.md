# Sagas — Overview Técnico e de Produto

> Documento de referência para leitura externa (recrutador, entrevistador, ou outro agente que
> precise entender o tamanho real do projeto). Escrito em setembro de 2026, sobre a versão **1.15.1**.
> Números levantados direto do repositório, não estimados.

---

## 1. O que é

**Sagas** é um RPG solo por texto para Android, onde o jogador escreve os turnos e uma IA responde
construindo a história — personagens, capítulos, atos, lore e um retrospecto emocional no final.
Não é um chatbot com skin de jogo: é um motor narrativo com estado persistente, progressão
estruturada e uma identidade visual que muda inteira conforme o gênero escolhido.

- **Plataforma**: Android nativo (minSdk 27, targetSdk 36), 100% Kotlin + Jetpack Compose.
- **Aplicativo**: `com.ilustris.sagai`, versionamento automatizado via `version.properties`.
- **Idiomas**: Inglês e Português-BR (~950 strings localizadas cada).
- **Modelo de negócio**: freemium com assinatura ("Signature") via Google Play Billing.

---

## 2. Escala do projeto (números reais)

| Métrica | Valor |
|---|---|
| Arquivos Kotlin | **830** |
| Linhas de Kotlin | **~121.000** (74k features · 25k UI · 18,5k core) |
| Commits | **430**, de maio/2025 a agosto/2026 (~15 meses) |
| Autoria | 399 commits do desenvolvedor principal — projeto **solo**, com agentes de IA como ferramenta de workflow |
| Funções `@Composable` | **816** |
| ViewModels | **76** |
| UseCases | **48** |
| Entidades Room / DAOs | **14 / 14** |
| Versão do banco | **v29**, com **28 migrations** escritas à mão |
| Destinos de navegação | **24** (Navigation 3 type-safe) |
| Blueprints de prompt de IA | **~57**, versionados em Firebase Remote Config |
| Código dedicado a prompts | ~3.350 linhas |
| Gêneros narrativos | **9**, com **6 linguagens visuais** distintas |
| Releases documentados | 15+ versões com release notes bilíngues |

---

## 3. Stack

**Core**: Kotlin 2.2.20 · Coroutines/Flow · Jetpack Compose (BOM 2026.05) · Material 3 ·
Navigation 3 · Hilt/KSP · Room + Paging 3 · WorkManager (`@HiltWorker`) · DataStore · kotlinx.serialization + Gson

**IA & mídia**: Firebase AI Logic (Gemini/Gemma) · Google Generative AI SDK · ML Kit GenAI Prompt
(on-device) · ML Kit Face Detection e Subject Segmentation · Imagen para geração de imagem ·
Coil · Lottie · **AGSL runtime shaders**

**Infra**: Firebase Remote Config, Analytics, Crashlytics (com símbolos nativos), Installations ·
Google Play Billing 8 · Play Age Signals · OkHttp · Timber · R8/ProGuard com regras próprias ·
assinatura de release automatizada

**Qualidade**: JUnit + MockK (16 suítes focadas em regras de domínio: continuidade narrativa,
prompts, roteamento de IA local, notificações) · tela interna de Design System Preview ·
tela de Audit Log de IA dentro do app

---

## 4. Arquitetura de IA — o núcleo do projeto

Esta é a parte com maior densidade técnica, e a que mais evoluiu ao longo do desenvolvimento.

### 4.1 Prompts como configuração, não como código
Todos os ~57 prompts vivem no **Firebase Remote Config** como *blueprints* estruturados
(`role` · `directives` · `rules` · `template`). O `PromptService` busca o blueprint, separa a parte
estática da dinâmica (arquitetura **"Split & Merge"**: instruções estáticas vão para o
`system_instruction` da API, dados variáveis para o template) e injeta variáveis por reflexão a
partir de data classes.

**Consequência prática**: ajustar o comportamento narrativo da IA — tom, regras, estrutura de saída —
é uma mudança de configuração publicada em segundos, sem build, sem review da Play Store.

### 4.2 Roteamento multi-tier de modelos
Em vez de IDs de modelo hardcoded, o sistema usa **seleção por requisito** (`LOW` / `MEDIUM` / `HIGH`),
cada um resolvido por uma flag de Remote Config:

- **LOW (Gemma 3 1B)** — classificação rápida: tom emocional, correção de digitação, reações de NPC.
- **MEDIUM (Gemma 3 12B)** — o Analista: sumarização de cena, wiki, notas comportamentais.
- **HIGH (Gemma 3 27B)** — o Arquiteto: geração narrativa, capítulos, conclusões, direção de arte.

Isso resolveu um problema real de produção — **estouro de quota por soma de tokens** em chamadas
encadeadas. Ao distribuir as etapas entre modelos diferentes, cada uma consome quota separada; e
por **destilação de contexto**, o 12B condensa o histórico bruto em um `SceneSummary` técnico que é
a *única* coisa que chega ao 27B, preservando a janela de contexto do modelo mais caro para
criatividade em vez de rastreamento de histórico.

### 4.3 Pipeline de imagem em três pilares
Geração de arte de saga e de personagem passa por um pipeline encadeado de agentes:

```
Imagem de referência → [DIRECTOR] → [ARTIST] → [REVIEWER] → prompt final → geração
```

- **Director** — extrai 15 parâmetros de cinematografia da referência (ângulo, lente, enquadramento,
  iluminação, profundidade de campo, atmosfera), em três níveis de criticidade.
- **Artist** — traduz jargão técnico em linguagem visual descritiva
  (`f/1.4 shallow DOF` → *"fundo suavemente desfocado, sujeito em foco nítido"*) e monta o prompt em
  três blocos: estilo, cinematografia, personagem.
- **Reviewer** — valida e corrige antes de gastar uma geração, com **score 0–100** para
  cinematografia e para aderência ao estilo do gênero, e veredito `READY` / `NEEDS_REVIEW` /
  `CRITICAL_ISSUES`.

Complementado pelo `ArtworkConceptService` (geração guiada) e por segmentação/detecção de rosto
on-device para tratamento das referências.

### 4.4 Continuidade e anti-alucinação
O problema clássico de narrativa longa com LLM — o modelo ressuscitar um personagem morto, esquecer
uma mudança de local, retconar o último turno — é atacado em várias camadas:

- **Protocolo anti-alucinação** explícito nos prompts de reply e de sumarização (morte é definitiva,
  partida é real, destruição é permanente, a última mensagem é verdade absoluta).
- **Posicionamento estratégico**: a última mensagem do jogador é reinjetada no *fim* do prompt, onde
  o modelo dá mais peso.
- **`SceneSummary` estruturado** (`charactersPresent`, `currentLocation`, `worldStateChanges`) gerado
  por um modelo dedicado e usado como estado factual entre turnos.
- **Camada de continuidade em código** (`ContinuityContextBuilder`, `ContinuityRollup`,
  `ContinuitySummary`) — coberta por testes unitários.

### 4.5 Confiabilidade da camada de IA
- **Streaming** de geração com máquina de estados própria (`StreamingState`), serializada para
  evitar corrida entre chamadas.
- **Retry com política por prioridade** e backoff que respeita o `retryDelay` devolvido pela API em
  rate limit — inclusive distinguindo *rate limit* de *prompt grande demais*, que antes eram
  reportados como o mesmo erro.
- **Fallback de tier**: se um modelo especializado falha, a chamada cai para o tier estável.
- **`ModelOutageException` / `GuardrailsException`** e uma taxonomia de `SafeGuard`
  (`BLOCKED`, `AGE_RESTRICTED`, `EXPLICIT_CONTENT`…) mapeada para mensagens de UI localizadas, em vez
  de erro cru.
- **`AIAuditRecorder`** — toda geração é persistida em Room (`AIAuditLog`) com prompt, resposta e
  metadados, e há uma tela de auditoria dentro do app para investigar comportamento em campo.
- **Validação de saída estruturada** — schemas JSON tipados gerados e validados a partir das data
  classes de destino.

### 4.6 IA on-device
`LocalAiEligibility` / `LocalAiExecutor` / `LocalAiSidebackRouting` avaliam se um prompt cabe no
modelo local do ML Kit GenAI (tier compatível, sem imagens de referência, dentro do teto de
caracteres) e desviam a chamada para execução no dispositivo — reduzindo latência, custo e
dependência de rede para as tarefas leves.

---

## 5. Sistemas de produto construídos

| Sistema | O que é |
|---|---|
| **Chat narrativo** | Turnos com tags de expressão `<action>` / `<think>` / `<narrator>`, menções `@`/`/`, edição e exclusão de mensagens, sugestões de ação geradas por IA, mensagens expressivas animadas |
| **Progressão narrativa** | Máquina de estados (`NarrativeCheck` / `NarrativeCoordinator` / `NarrativeActionExecutor`) que decide, a partir de limiares configuráveis, quando criar evento, capítulo, ato, intro de ato ou o final da saga |
| **Milestones cinematográficos** | Overlays de conquista com tratamento visual por gênero, navegação dedicada e texto gerado dinamicamente ligado à história |
| **Wiki / Lore** | Extração automática de entradas de lore a partir dos eventos, com um segundo passe de *merge* que consolida entradas redundantes |
| **Personagens** | Geração de perfil, retrato com pipeline de imagem, relações entre personagens, apelidos emergentes, sistema de conhecimento (o que cada personagem "sabe"), e relatório final de jornada |
| **Saga Brain** | Grafo navegável da história como constelação — 8 tipos de nó (saga, ato, capítulo, evento, personagem, relação, wiki) e 7 tipos de aresta, renderizado em Canvas com glow e presença |
| **Review / Saga Wrapped** | Retrospecto no estilo Spotify Wrapped: playstyle, assinatura emocional, elenco, arco, conclusão — com renderização temática por gênero (quadrinho, jornal, terminal CRT, colagem de papel rasgado) |
| **Perfil emocional** | Extração de tom emocional por mensagem, notas comportamentais e uma conclusão empática ao fim da saga |
| **Epilogue chat** | Conversa efêmera pós-final com os personagens, que nunca é persistida nem avança o enredo |
| **Compartilhamento** | Cards de imagem geradas para playstyle, emoções, história, relações e personagem, cada um com tagline própria escrita por IA |
| **Notificações inteligentes** | `WorkManager` + `@HiltWorker` gera, em background, uma mensagem de um personagem aleatório baseada no estado real da saga; migrado de `AlarmManager`+coroutines por perda de processo |
| **FAQ com "Ask AI"** | FAQ servida por Remote Config com fallback para geração por IA na "voz do Sagas" |
| **Resiliência** | `DatabaseBackupService` com lock, e uma **`SOSActivity`** dedicada — tela de recuperação que captura corrupção de banco/falha de sistema e oferece restauração em vez de crash loop |
| **Premium** | Assinatura Signature via Play Billing 8, com verificação de idade via Play Age Signals |
| **Ferramentas internas** | Design System Preview, Audit Log de IA, Lore Debug, contador de playtime |

---

## 6. Identidade visual por gênero

Nove gêneros (Fantasy, Cyberpunk, Horror, Heroes, Crime, Shinobi, Space Opera, Cowboy, Punk Rock)
mapeados para **seis linguagens visuais** (`DEFAULT`, `TERMINAL`, `BOOK`, `CRIME`, `COLLAGE`, `COMIC`)
através de uma abstração (`GenreSurfaceStyle` + `GenreStorySurface`) em que adicionar um estilo novo
a um gênero é **uma linha** e nada mais no app precisa saber disso.

Cada gênero carrega forma de balão de chat própria (10 shapes customizados em Compose),
paleta, tipografia, som e efeitos. Há **shaders AGSL** escritos à mão (tela CRT, filtros seletivos
de cor, bordas rotativas, loader estrelado), gradientes mesh, texto manuscrito animado e um mascote
com estados emocionais.

---

## 7. Processo de engenharia

O que diferencia o projeto de um app pessoal comum é o **processo em volta do código**:

- **Workflows agênticos versionados** em `.agent/workflows/` — agentes especializados para auditoria
  de produto (`product_agent`), estratégia de marketing (`marketing_strategy_agent`), planejamento
  técnico de features com avaliação real de *reaproveitável vs. novo* (`feature_planning_agent`),
  além de fluxos de release, release notes, extração de strings hardcoded e commit semântico.
- **Disciplina documental**: `/docs` tem regra explícita — tudo fora de `archive/` precisa estar
  *correntemente verdadeiro*; doc desatualizado é arquivado com uma linha explicando o que o
  substituiu, nunca deixado apodrecendo. Documentação escrita para ser lida por humanos **e** por
  agentes.
- **Roadmap honesto**: features aparecem com status `Proposto` / `Parked` / **`Cancelled`** e o
  motivo real — ex.: a arquitetura de *tool calling* agêntico foi implementada e **revertida** porque
  a latência comprometeu a experiência. Decisão de produto documentada, não escondida.
- **Release engineering**: versionamento derivado de `version.properties` (versionCode calculado),
  R8 com regras próprias, símbolos nativos para deobfuscação no Play Console, scripts de build e de
  deobfuscação de stacktrace, release notes bilíngues por versão.
- **Localização como passo de pipeline**: o workflow de entrega escaneia o diff em busca de strings
  hardcoded antes de abrir o PR.

---

## 8. Bullets prontos para CV

Versão curta, em português:

- Desenvolvi sozinho o **Sagas**, RPG narrativo por texto para Android (Kotlin/Compose, ~121k linhas,
  830 arquivos, 15 releases em produção), com motor de IA generativa próprio.
- Projetei uma **arquitetura multi-tier de LLMs** (Gemma 1B/12B/27B com roteamento por requisito),
  eliminando estouros de quota por soma de tokens via destilação de contexto entre modelos.
- Construí um sistema de **prompts como configuração**: ~57 blueprints versionados em Firebase Remote
  Config, permitindo alterar comportamento de IA em produção sem novo build.
- Implementei um **pipeline de geração de imagem em três agentes** (Director → Artist → Reviewer) com
  extração de 15 parâmetros cinematográficos de referências e validação por score antes da geração.
- Resolvi problemas de **continuidade narrativa em LLM** (alucinação de personagens mortos, retcon de
  eventos) com protocolo anti-alucinação, sumarização de estado estruturada e camada de continuidade
  testada.
- Camada de IA resiliente: streaming, retry com backoff sensível a rate limit, fallback entre
  modelos, taxonomia de guardrails e auditoria persistida de todas as gerações.
- **IA on-device** via ML Kit GenAI com roteamento por elegibilidade, reduzindo latência e custo em
  tarefas leves.
- Persistência com Room em **29 versões de schema e 28 migrations** sem perda de dados de usuário,
  incluindo backup e uma tela de recuperação para corrupção de banco.
- Sistema de design multi-gênero: 9 gêneros, 6 linguagens visuais, shaders AGSL próprios, 816
  composables, totalmente localizado em EN/pt-BR.

English version:

- Solo-built **Sagas**, an AI-driven narrative RPG for Android (Kotlin/Jetpack Compose, ~121k LOC,
  830 files, 15 production releases), including its entire generative-AI engine.
- Designed a **multi-tier LLM architecture** (Gemma 1B/12B/27B, requirement-based routing) that
  eliminated token-quota exhaustion in chained calls through cross-model context distillation.
- Built a **prompts-as-configuration** system: ~57 structured blueprints served from Firebase Remote
  Config, enabling AI behavior changes in production with no app release.
- Implemented a **three-agent image-generation pipeline** (Director → Artist → Reviewer) extracting 15
  cinematography parameters from reference images and scoring prompts before spending a generation.
- Solved long-form **LLM narrative continuity** failures (dead-character hallucination, event
  retconning) via an anti-hallucination protocol, structured scene-state summarization and a
  unit-tested continuity layer.
- Hardened the AI layer: streaming generation, rate-limit-aware retry/backoff, model-tier fallback,
  a guardrail taxonomy mapped to localized UI, and persisted audit logs for every generation.
- Shipped **on-device inference** through ML Kit GenAI with an eligibility-based routing layer.
- Maintained **29 Room schema versions across 28 hand-written migrations** with zero user data loss,
  plus database backup and a dedicated corruption-recovery flow.
- Multi-genre design system: 9 genres, 6 visual languages, hand-written AGSL shaders, 816 composables,
  fully localized (EN/pt-BR).

---

## 9. Como navegar o repositório

| Onde | O que |
|---|---|
| `app/src/main/java/com/ilustris/sagai/core/ai/` | Toda a camada de IA: clientes, engine, prompts, serviços, execução local |
| `.../core/` | Banco, analytics, áudio, notificações, navegação, tema, serviços (billing, remote config, idade) |
| `.../features/` | 22 features de produto (saga, chat, brain, wiki, characters, emotional, review, share…) |
| `.../ui/` | Design system, tema, componentes de gênero, animações |
| `docs/` | Arquitetura, features enviadas, backlog, roadmap, release notes, marketing |
| `.agent/workflows/` | Agentes e workflows de engenharia/produto usados no dia a dia |
