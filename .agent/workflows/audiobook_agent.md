---
description: Transforma um livro/PDF exportado do Sagas em peças de narração prontas para gerar áudio no AI Studio, mantendo naturalidade e consistência de voz entre as partes
---

# Agente de Audiolivro

## Objetivo

Pegar um livro exportado do app (PDF gerado via `generateBookPDF`, o mesmo botão de compartilhar
que qualquer jogador já tem hoje) e devolver um pacote pronto pra narrar no Google AI Studio: uma
"bíblia de voz" reutilizável + um `.md` por capítulo, numerados pra remontagem depois de gerar cada
pedaço separadamente.

**Não gera áudio.** O agente prepara o material — a geração em si acontece manualmente no AI
Studio, colando cada `.md` e juntando os arquivos de áudio depois num editor.

## Por que quebrar por capítulo, não por frase/parágrafo

Modelos de TTS ajustam ritmo e ênfase com base no contexto da passagem — pedaço curto demais some
com a "respiração" natural do texto e a costura entre os áudios fica robótica. Em teoria a unidade
ideal é o **capítulo já existente no livro** (geralmente 800-900 palavras / 5-6 min de áudio), mas
**na prática o player de "Generate Speech" do AI Studio tem um teto de geração bem mais curto que
isso** — medido em teste real: um capítulo de ~900 palavras foi cortado em 81,5s de áudio (só ~190
palavras faladas). Não existe opção na interface pra estender esse teto (é limite da própria
plataforma, não do modelo — a API do Gemini aguenta bem mais, mas só chamando ela direto, fora do
AI Studio).

**Comece direto pela quebra em cenas internas** (parágrafos/blocos de diálogo dentro do capítulo,
mirando ~90-110 palavras / ~35-45s cada — margem confortável abaixo do teto observado), em vez de
tentar o capítulo inteiro primeiro e descobrir que corta. Ajuste o tamanho do pedaço pelo resultado
real do teste de cada usuário, já que esse teto pode variar.

## Como trabalhar

1. **Ler o PDF completo** e identificar a estrutura real: título/volume, capítulos nomeados, e as
   cenas internas de cada capítulo (útil como pontos de corte de reserva).
2. **Montar a bíblia de voz** (`voice_bible.md`), uma vez por livro/saga:
    - Puxe o tom base do gênero já documentado no design system do app (`docs/architecture/`,
      `docs/features/*`, o guia de mascote/sound design por gênero) como ponto de partida.
    - Mas calibre pelo tom real da prosa daquele livro específico — um fantasy sombrio de vingança
      narra diferente de um fantasy whimsical, mesmo sendo o mesmo gênero na paleta visual do app.
    - Defina: direção de voz (registro, ritmo, o que evitar), uma instrução-base de estilo (texto
      pronto pra colar no campo de instrução do AI Studio) e notas de como modular falas de
      personagens sem trocar de narrador.
    - Essa mesma instrução-base vale pra todos os capítulos do livro — só as notas de ênfase pontual
      mudam por capítulo.
3. **Montar um `.md` por capítulo**, numerado (`01_..md`, `02_..md`...) com:
    - Referência à bíblia de voz (não repetir a instrução inteira, só apontar).
    - Notas de ênfase específicas daquele capítulo (onde desacelerar, onde um sussurro/frieza pesa
      mais, como soa cada personagem que fala ali).
    - O texto limpo pra narrar, exatamente como vai ser colado no AI Studio.
4. **Nomear os arquivos pela ordem de remontagem** — é o único jeito de garantir que a pessoa não
   junte os áudios fora de ordem depois de gerar cada um separadamente.
5. Se o livro tiver uma nota de fechamento fora do universo da ficção (ex: comentário do "coautor"
   IA sobre o processo de escrever), trate como peça separada e opcional — registro tem outro tom
   (pessoal, primeira pessoa) e não deve seguir a bíblia de voz da narrativa.
6. Essa nota de fechamento é o gancho de comunidade — mas o gancho é a nota soar genuína, não
   propaganda. Não escreva "isso nasceu no Sagas, onde cada jogador decide sua jornada" como frase
   de efeito/CTA. Escreva como uma reação de espanto de quem realmente escreveu aquilo com o
   jogador, tipo "dá pra acreditar que tudo isso aconteceu no Sagas? Cada decisão muda tudo, é por
   isso que eu amo esse universo" — solta no meio da fala empolgada dela, não como parágrafo à parte
   nem como frase de fechamento. Se a menção destacar mais do universo/da mecânica do que da emoção
   genuína de quem escreveu, reescreva.

## Formato do `voice_bible.md`

```
# Bíblia de Voz — <Título do livro>

## Direção de voz
(registro, ritmo, o que evitar — específico ao tom real da prosa, não só o gênero genérico)

## Instrução-base (colar no AI Studio junto com o texto)
"..."

## Personagens que falam
- <Nome>: como modular sem trocar de narrador
```

## Formato de cada capítulo (`NN_slug.md`)

```
# <Número>. <Título do capítulo>

> Usa a bíblia de voz em voice_bible.md. Instrução-base + notas abaixo.

## Notas de ênfase deste capítulo
- (pontos específicos: onde pausar, sussurrar, endurecer o tom)

## Texto para narrar
(texto limpo, pronto pra colar)
```

## Onde salvar

`docs/marketing/audiobooks/<slug-do-livro>/` — `voice_bible.md` + os capítulos numerados. Não
precisa passar pelo `feature_planning_agent`: isso é produção de conteúdo externa ao app, não exige
nenhuma feature nova (o export de PDF já existe hoje).
