---
description: Monta e mantém o Canvas de Marketing do Sagas — cronograma com o quê, por quê, alvo, plataforma e recursos do app necessários
---

# Agente de Estratégia de Marketing

## Objetivo

Montar o **Canvas de Marketing** do Sagas: um plano vivo que conecta o produto real a um cronograma
de conteúdo/campanha, respondendo pra cada peça **o quê**, **por quê**, **pra quem (alvo)**, **onde
(plataforma)** e **o que precisamos capturar/gerar do app** para executar.

## Antes de gerar — desconsidere o material antigo

`docs/marketing/marketing_plan.md`, `docs/marketing/content_calendar.md` e os workflows
`marketing_monday_agent.md` / `marketing_wednesday_agent.md` / `marketing_friday_agent.md` são de uma
fase anterior do projeto (lançamento inicial, agentes fixos por dia da semana) e **não devem ser
usados como fonte de verdade**. Não reaproveite temas, calendário ou cadência de lá. Se algo daquele
material ainda for genuinamente relevante, ele deve ser reintroduzido pelo Canvas novo, não herdado
automaticamente.

## Fontes de verdade reais

- `docs/features/*` e `docs/current_objective_milestone_simplified.md` — o que já foi entregue e
  como funciona de fato hoje.
- `docs/feature_ideas/*` — o que está no radar (não anuncie como pronto).
- Saída do `product_agent.md` — lista de "Oportunidade de Premium" com a narrativa de venda de 1
  frase, para qualquer item do cronograma que envolva o Signature.
- Princípio compartilhado com o Produto: a experiência free de hoje é o baseline vendável; premium é
  enriquecimento (imagem, áudio), nunca é enquadrado como "o que falta pra jogar de verdade".

## Estrutura do Canvas

### 1. Contexto estratégico (topo do documento)

- **Objetivo do ciclo**: o que estamos tentando mover agora (ex: aquisição, ativação, converter pra
  Signature) — baseado no que há de novo/relevante no produto no momento em que o agente roda.
- **Público-alvo**: quem joga Sagas hoje, segmentado por interesse (ex: fãs de RPG solo/journaling,
  por gênero preferido — fantasia, cyberpunk, horror etc).
- **Posicionamento**: a frase-chave que resume por que Sagas é diferente (parceiro de escrita, não
  jogo com script).

### 2. Cronograma (tabela)

Cada linha é uma peça de conteúdo ou campanha:

| Data/Semana | O quê | Por quê | Alvo | Plataforma | Recursos do app necessários |
|---|---|---|---|---|---|
| ... | formato + tema | objetivo/etapa do funil (awareness, ativação, conversão) | público específico dessa peça | Instagram/TikTok/LinkedIn/etc | o que precisa ser gravado, printado ou gerado no app — específico, não genérico |

A cadência (quantas peças por semana, em quais dias) não é fixa como antes — define de acordo com o
que há pra comunicar no ciclo atual, não um slot vazio pra preencher.

### 3. Dependências / bloqueios

Se o "recurso do app" de um item ainda não existe (feature em `feature_ideas`, não implementada),
marque o item como bloqueado até a feature sair, em vez de propor conteúdo sobre algo que não
funciona ainda.

## Como trabalhar

1. Levantar o que há de novo ou relevante no produto (features recentes, o que está em
   `feature_ideas` pro próximo ciclo).
2. Definir 1-2 objetivos do ciclo atual.
3. Preencher o cronograma, sendo específico no "recurso do app" (ex: "tela do trophy shelf com pelo
   menos 3 sagas finalizadas", não "print do app").
4. Quando um item envolver Signature, puxar a narrativa de venda validada pelo `product_agent` em vez
   de inventar um novo enquadramento.
5. Nunca reintroduzir a cadência ou os temas do calendário antigo por padrão — cada ciclo é montado a
   partir do estado atual do produto.

## Onde salvar

Persistir em `docs/marketing/marketing_canvas.md`. Esse arquivo passa a ser a fonte de verdade de
marketing a partir de agora — `marketing_plan.md` e `content_calendar.md` ficam como histórico, não
como referência ativa.
