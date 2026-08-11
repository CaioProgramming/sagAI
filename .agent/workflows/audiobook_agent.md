---
description: Transforma um livro/PDF exportado do Sagas em material pronto para narrar — no ElevenLabs Studio (padrão, durante o trial) ou no AI Studio do Google (fallback gratuito)
---

# Agente de Audiolivro

## Objetivo

Pegar um livro exportado do app (PDF gerado via `generateBookPDF`, o mesmo botão de compartilhar
que qualquer jogador já tem hoje) e devolver um pacote pronto pra narrar: uma "bíblia de voz"
reutilizável + o texto de cada capítulo, no formato certo pro destino escolhido.

**Não gera áudio.** O agente prepara o material — a geração em si acontece manualmente (upload no
ElevenLabs Studio, ou colar no AI Studio) e a pessoa junta os arquivos de áudio depois.

## Destino da narração — escolha primeiro

- **ElevenLabs Studio (padrão enquanto durar o trial de 2 meses do plano Starter, $6/mês)**: sobe o
  manuscrito inteiro de uma vez, o Studio quebra e junta sozinho mantendo a voz consistente — não
  precisa fatiar nada na mão. Cota de ~30 mil caracteres/mês no Starter (~1 livro inteiro por mês,
  tipo os pilotos que já rodamos — controlar uso se a cadência subir). **Produza um `.md` por
  capítulo com o texto inteiro**, sem fragmentar.
- **Google AI Studio (fallback gratuito, sem cota mensal, mas com teto de geração curto por
  chamada)**: medido em teste real, um capítulo de ~900 palavras foi cortado em 81,5s de áudio (só
  ~190 palavras faladas) — não existe opção na interface pra estender (a API do Gemini aguenta bem
  mais, só chamando ela direto, fora do AI Studio). **Produza também a versão fatiada** em cenas
  internas (~90-110 palavras / ~35-45s cada), sufixo `_partes.md`, só se for essa a rota usada.
- Produza sempre o `.md` de capítulo inteiro primeiro (é o formato do ElevenLabs e também serve de
  fonte pra fatiar depois, se precisar do fallback).

## Checkpoint do trial

Ao final dos 2 meses grátis do Starter, decidir se vale assinar o Creator (~$22/mês, mais cota +
clonagem de voz profissional) com base no que foi entregue/no engajamento gerado — não decidir no
escuro. Registrar essa decisão como dependência em `docs/marketing/marketing_canvas.md` quando o
canvas for atualizado.

## Como trabalhar

1. **Ler o PDF completo** e identificar a estrutura real: título/volume, capítulos nomeados, e as
   cenas internas de cada capítulo (útil como pontos de corte de reserva, caso o destino seja AI
   Studio).
2. **Montar a bíblia de voz** (`voice_bible.md`), uma vez por livro/saga:
    - Puxe o tom base do gênero já documentado no design system do app (`docs/architecture/`,
      `docs/features/*`, o guia de mascote/sound design por gênero) como ponto de partida.
    - Mas calibre pelo tom real da prosa daquele livro específico — um fantasy sombrio de vingança
      narra diferente de um fantasy whimsical, mesmo sendo o mesmo gênero na paleta visual do app.
    - Defina: direção de voz (registro, ritmo, o que evitar), uma instrução-base de estilo (texto
      pronto pra colar) e notas de como modular falas de personagens sem trocar de narrador.
    - Essa mesma instrução-base vale pra todos os capítulos do livro — só as notas de ênfase pontual
      mudam por capítulo.
3. **Montar um `.md` por capítulo** com o texto inteiro (formato padrão, serve pro ElevenLabs
   Studio):
    - Referência à bíblia de voz (não repetir a instrução inteira, só apontar).
    - Notas de ênfase específicas daquele capítulo (onde desacelerar, onde um sussurro/frieza pesa
      mais, como soa cada personagem que fala ali).
    - O texto limpo pra narrar.
4. **Se o destino for o AI Studio do Google**, gerar também a versão `_partes.md`: mesmo capítulo
   fatiado em cenas internas (~90-110 palavras cada), numeradas pra remontagem, cada pedaço com sua
   própria nota de ênfase pontual.
5. Se o livro tiver uma nota de fechamento fora do universo da ficção, trate como peça separada e
   opcional — registro tem outro tom (pessoal, primeira pessoa) e não deve seguir a bíblia de voz da
   narrativa.
6. **A voz dessa nota não é "IA coautora explicando que escreveu com o jogador".** É a personalidade
   do próprio Sagas/da história falando em primeira pessoa, como quem acompanhou a jornada inteira —
   não quem reivindica autoria. Evite verbos de autoria ("escrevi", "coautora", "escrever aquele
   momento"); prefira verbos de quem testemunhou/viveu junto ("acompanhar", "ver acontecer", "sentir
   junto"). Não precisa explicar o mecanismo de coautoria pro leitor — só mostrar que alguém (algo)
   se importou com aquela jornada do início ao fim.
7. **O gancho de comunidade nunca é a mesma frase reaproveitada de um livro pro outro.** Cada vez
   que o agente rodar, escreva algo novo, ancorado no que realmente marcou aquele livro específico
   (um personagem, uma virada, uma emoção) — nunca um template genérico tipo "cada jogador decide
   sua jornada" copiado e colado. Isso vira propaganda exatamente quando começa a soar como frase de
   efeito repetida. Solto no meio da fala empolgada, nunca como parágrafo à parte nem como frase de
   fechamento/CTA.
8. **Nunca explique o mecanismo do jogo** ("turno por turno", "o jogador fez X ou Y", "decisão por
   decisão" como descrição de como o app funciona). Fale do resultado/sentimento, não do
   funcionamento: "cada decisão é sua", "essa é a história dela do começo ao fim" — a ideia é a
   pessoa sentir a autoria do jogador, não receber uma explicação de mecânica de turnos.
9. **A nota de fechamento também tem teto no AI Studio (~80s) — e não deve ser quebrada em partes
   como os capítulos**, mesmo nesse fallback. É uma reflexão única, contínua; fatiá-la em "parte
   1/2" quebra a intimidade do formato. Em vez de quebrar, **reescreva-a mais curta**: releia a
   história inteira, identifique o que a nota está realmente tentando dizer (os 2-3 momentos que
   mais marcaram + o gancho de comunidade), e comprima para caber em ~1 minuto (~130-140 palavras a
   ~140 palavras/min) sem perder essa intenção. Corte detalhe, não sentimento. No ElevenLabs Studio
   isso não é estritamente necessário (o Studio aguenta mais), mas o tamanho de ~1 minuto continua
   sendo bom formato pra clipe standalone de qualquer forma.

## Formato do `voice_bible.md`

```
# Bíblia de Voz — <Título do livro>

## Direção de voz
(registro, ritmo, o que evitar — específico ao tom real da prosa, não só o gênero genérico)

## Instrução-base (colar junto com o texto)
"..."

## Personagens que falam
- <Nome>: como modular sem trocar de narrador
```

## Formato de cada capítulo (`NN_slug.md`)

```
# <Número>. <Título do capítulo>

> Usa a bíblia de voz em voice_bible.md. Instrução-base + notas abaixo. Formato de capítulo inteiro
> — cole assim no ElevenLabs Studio. Se o destino for o AI Studio do Google, usar NN_slug_partes.md.

## Notas de ênfase deste capítulo
- (pontos específicos: onde pausar, sussurrar, endurecer o tom)

## Texto para narrar
(texto limpo, pronto pra colar)
```

## Onde salvar

`docs/marketing/audiobooks/<slug-do-livro>/` — `voice_bible.md` + um `.md` por capítulo (texto
inteiro) + `_partes.md` só se o destino for o AI Studio. Não precisa passar pelo
`feature_planning_agent`: isso é produção de conteúdo externa ao app, não exige nenhuma feature nova
(o export de PDF já existe hoje).
