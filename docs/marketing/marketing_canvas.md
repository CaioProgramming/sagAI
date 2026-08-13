# Canvas de Marketing — Sagas

> Gerado pelo `marketing_strategy_agent.md`. É a fonte de verdade ativa de marketing — o sistema
> anterior (calendário fixo + agentes diários) está arquivado em `docs/archive/marketing_v1/`.

## Contexto estratégico

**Objetivo do ciclo atual**: a campanha começa **do zero** (sem awareness prévia a reaproveitar), o
que muda a ordem do conteúdo — o primeiro contato precisa ser o gancho mais simples e universal do
app, não a camada mais rica (tags, menções). O sistema de milestones (navegação + overlay
progressivo) já cobriu o gap de cold opens dentro do produto; agora o trabalho é replicar esse
"aha moment" pra fora, pra quem nunca abriu o app.

**Hierarquia de gancho usada neste ciclo** (ver `marketing_strategy_agent.md`):
1. **Camada 0** — escrever qualquer coisa em texto livre e ver a IA reagir. Zero curva de
   aprendizado. É o que abre a campanha.
2. **Camada 1** — tags `<action>/<think>/<narrator>`, menções `@`/`/`. Conteúdo de aprofundamento
   pra quem já instalou, não de primeiro contato.
3. **Camada 2** — milestones, capítulos, atos (progressão/retenção).
4. **Camada 3** — Saga Wrapped, identidade/compartilhamento.

**Público-alvo**: jogadores de RPG solo / ficção interativa por texto, segmentado por gênero
preferido (fantasia, cyberpunk, horror, heroes).

**Posicionamento**: "Sagas não é um jogo com roteiro, é um parceiro de escrita que reage a cada
palavra sua."

## Cronograma

| Data/Semana | O quê | Por quê | Alvo | Plataforma | Recursos do app necessários |
|---|---|---|---|---|---|
| Semana 1 (abertura) | Vídeo (Reels) — usuário escreve **uma frase simples** ("Eu abro a porta") sem tags nem preparo, e a IA reage de forma vívida e inesperada | Camada 0: gancho universal, zero explicação necessária, mostra a promessa central do app pra quem nunca viu | Público frio, nunca ouviu falar do app | TikTok/Reels | Gravação de tela real: campo de texto vazio → uma frase curta digitada → resposta da IA aparecendo (sem mencionar tags, menções ou UI avançada) |
| Semana 1 | Carrossel "Não é um jogo com roteiro" | Reforça a Camada 0 com um segundo ângulo (conceito, não mecânica) pro mesmo público frio | Mesmo público frio da peça de abertura | Instagram (Carrossel) | Screenshots de 2-3 respostas curtas da IA a mensagens simples, em gêneros diferentes por slide |
| Semana 2 | Vídeo/Carrossel sobre as tags `<action>/<think>/<narrator>` e menções `@`/`/` | Camada 1: aprofundar pra quem já instalou/já mandou a primeira mensagem — mostrar que dá pra ir além do texto puro | Quem já abriu o app e mandou ≥1 mensagem (não público frio) | Instagram (Carrossel) | Gravação com a última mensagem animada (levitação, estrelinhas tocáveis, caixa do narrador) |
| Semana 2-3 | Carrossel "Anatomia de um Milestone" | Camada 2: reforçar retenção mostrando o "feel" de progressão (fill animado + haptic + botão continuar), com texto de milestone já gerado dinamicamente pela IA ligado à história | Usuários ativos que já viram 1-2 milestones | Instagram (Carrossel) | Prints das 3 fases do fill (200dp carregando → encolhe → botão "Continuar"), em gêneros diferentes por slide |
| Semana 3 | Vídeo "Termine sua saga, o Wrapped é seu" | Camada 3: Wrapped é **free de propósito** — o ângulo é terminar a história e compartilhar com amigos (motor de virality orgânica), nunca conversão para pagamento | Usuários com saga longa parada sem terminar | Reels + Stories | Gravação completa de um Wrapped de teste (Intro → Vibe → Playstyle → Squad → Journey → Conclusion) com CTA de compartilhar, não de assinar |
| Futuro (sem data) | Carrossel "Wrapped temático" (terminal cyberpunk, página de livro fantasy, etc.) | Ideia de Signature — mas depende de viabilidade técnica de renderizar vídeo com animações + música do tema | Usuários que já terminaram ≥1 saga | Instagram | **BLOQUEADO** — status "Proposto 💡" em `docs/feature_planning/roadmap.md` (#23), aguardando spike técnico. Só volta pro cronograma quando o time decidir seguir |

## Dependências / bloqueios

- **Wrapped temático (Signature)**: planejado pelo `feature_planning_agent.md` em
  `docs/feature_planning/wrapped_themed_render/task.md` (roadmap #23). Nenhuma capacidade de vídeo
  existe hoje no projeto (o compartilhamento atual é só imagem via `features/share/*`) —
  recomendação é um spike técnico antes de qualquer estimativa. Marketing não entra em cronograma
  até o status sair de "Proposto".

## Achados do `product_agent` usados aqui

- **Melhoria de experiência**: som de milestone pendente (TODO já documentado no código).
- **Oportunidade de Premium**: Wrapped temático por gênero (terminal/cyberpunk, livro/fantasy) — já
  virou item de backlog (#23, "Proposto 💡") via `feature_planning_agent.md`; expressões/skins extras
  do mascote "Saga" (ainda em spec); estilo de retrato selecionável no Signature (hoje a seleção de
  referência é aleatória via Remote Config).
