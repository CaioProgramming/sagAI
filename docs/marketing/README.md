# Marketing — Sagas

Fonte de verdade: [`marketing_canvas.md`](marketing_canvas.md), gerado e mantido por
`.agent/workflows/marketing_strategy_agent.md`.

## Como funciona hoje

1. **`marketing_strategy_agent`** lê o estado real do produto (`docs/features/`,
   `docs/feature_ideas/`, achados do `product_agent`) e monta o canvas: contexto estratégico +
   cronograma (o quê / por quê / alvo / plataforma / recursos do app necessários).
2. Ideias de conteúdo que dependem de uma feature ainda não validada tecnicamente passam pelo
   `feature_planning_agent` antes de entrar no cronograma — ver `docs/feature_planning/roadmap.md`.
3. Peças publicadas ficam registradas em:
    - [`social_posts/`](social_posts/) — carrosséis/roteiros de Instagram e TikTok.
    - [`linkedin_posts/`](linkedin_posts/) — posts de "Dev Diary" no LinkedIn, gerados pelo workflow
      `.agent/workflows/create_linkedin_post.md`.

## Princípio fixo

A experiência free é o que se vende primeiro. Premium (Saga Signature) é sempre enquadrado como
upgrade/enriquecimento, nunca como algo que falta pra "jogar de verdade" — ver o princípio
compartilhado em `marketing_strategy_agent.md` e `product_agent.md`.

## Sistema anterior

O calendário fixo (segunda/quarta/sexta) e os agentes diários que o executavam foram desativados —
ver `docs/archive/marketing_v1/` para histórico. Não é fonte de verdade; não reaproveitar.
