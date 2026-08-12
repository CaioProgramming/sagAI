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

- **ElevenLabs Studio (padrão — Starter anual, ~$60/ano cobrados de uma vez, efetivo ~$5/mês)**:
  sobe o manuscrito inteiro de uma vez, o Studio quebra e junta sozinho mantendo a voz consistente —
  não precisa fatiar nada na mão. Cota de ~30 mil caracteres/mês no Starter (~1 livro inteiro por
  mês, tipo os pilotos que já rodamos — controlar uso se a cadência subir). **Produza um `.md` por
  capítulo com o texto inteiro**, sem fragmentar.
- **Google AI Studio (fallback gratuito, sem cota mensal, mas com teto de geração curto por
  chamada)**: medido em teste real, um capítulo de ~900 palavras foi cortado em 81,5s de áudio (só
  ~190 palavras faladas) — não existe opção na interface pra estender (a API do Gemini aguenta bem
  mais, só chamando ela direto, fora do AI Studio). **Produza também a versão fatiada** em cenas
  internas (~90-110 palavras / ~35-45s cada), sufixo `_partes.md`, só se for essa a rota usada.
- Produza sempre o `.md` de capítulo inteiro primeiro (é o formato do ElevenLabs e também serve de
  fonte pra fatiar depois, se precisar do fallback).

**Importante sobre a interface do ElevenLabs**: nem "Text to Speech" nem "Estúdio" têm um campo de
instrução separado tipo o do AI Studio — é só um box de texto pra colar a narração, com voz/modelo/
sliders (Estabilidade, Velocidade, Similaridade) ao lado. **NUNCA cole a "Instrução-base" (ou
qualquer texto de direção/estilo) nesse box — o motor vai narrar essas frases literalmente, como se
fossem parte da história.** A instrução-base do `voice_bible.md` não vai colada em lugar nenhum da
interface — ela só existe pra orientar a seção **"Configuração no ElevenLabs"** do próprio
`voice_bible.md`: qual tipo de voz escolher (pelo preview, não só pelo nome) e pra que lado ajustar
os sliders. O único texto que entra no box é o conteúdo de `_texto_puro.txt` (ver seção "Formato do
texto puro" abaixo) — nada além disso, nunca.

**Voz: prefira uma voz pronta da biblioteca a desenhar uma nova no Voice Design.** Testamos Voice
Design (`docs/marketing/audiobooks/genre_voice_design.md`) e o resultado saiu dramático/atuado
demais — a expressividade fica meio que "de fábrica" na voz gerada, e nenhum slider desfaz isso por
completo. Uma voz pronta da biblioteca, ouvida por preview até encontrar uma que combine com a
`Essência` do gênero, mais o texto puro sem nenhuma instrução colada, tende a soar mais como "alguém
comum lendo o livro" — que é exatamente o efeito casual que queremos, não uma performance. Trate o
Voice Design como plano B, só se nenhuma voz da biblioteca servir.

## Checkpoint do investimento

**Não é trial grátis** — "2 meses grátis" é o desconto do plano anual (paga 10 meses, leva 12): o
Starter anual sai em ~$60/ano (~$5/mês efetivo) cobrados de uma vez, contra $6/mês sem compromisso
no plano mensal. Já é um investimento real, não uma avaliação sem risco.

Dado isso, vale reavaliar em ~2 meses de uso (não porque o trial "acabou", mas porque é um prazo
razoável pra já ter dado pra ver resultado) se vale seguir no Starter, subir pro Creator (~$22/mês,
mais cota + clonagem de voz profissional), ou voltar pro fallback gratuito do AI Studio. Basear a
decisão no que foi entregue/no engajamento gerado, não decidir no escuro. Registrar essa decisão
como dependência em `docs/marketing/marketing_canvas.md` quando o canvas for atualizado.

## Como trabalhar

1. **Ler o PDF completo** e identificar a estrutura real: título/volume, capítulos nomeados, e as
   cenas internas de cada capítulo (útil como pontos de corte de reserva, caso o destino seja AI
   Studio).
2. **Montar a bíblia de voz** (`voice_bible.md`), uma vez por livro/saga:
    - **Primeiro, cheque `docs/marketing/audiobooks/genre_voice_design.md`.** Esse arquivo já tem,
      pra cada um dos 9 gêneros do app, um prompt de Voice Design feminino e um masculino (em inglês
      — o Voice Design do ElevenLabs entende melhor descrição de estilo em inglês, mesmo pra uma voz
      que vai narrar em português; por isso cada prompt já declara "Speaks Brazilian Portuguese
      (pt-BR)" na primeira frase) a partir do `${genero}_conversation_blueprint` real do Remote
      Config — se o gênero do livro já tiver voz salva na tabela ali, use o `voice_id`/nome salvo em
      vez de garimpar voz genérica da biblioteca do ElevenLabs. Se ainda não tiver, gere com os
      prompts do arquivo (uma vez só, a voz fica reutilizável em todo livro daquele gênero).
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
4b. **Gerar também `NN_slug_texto_puro.txt`** (um por capítulo, e um pra nota de fechamento se
   houver): só a prosa, sem título, sem nota de ênfase, sem citação de bloco — nada que precise ser
   removido antes de colar. É o formato padrão pro ElevenLabs (Text to Speech ou Studio, que não têm
   campo de instrução separado); o `.md` anotado continua existindo como documento de preparo/
   referência, não como o que é colado.
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
9. **O objetivo é soar como alguém amador gravando em casa, não como um estúdio profissional.**
   Hoje em dia tem bastante desconfiança com conteúdo gerado por IA — uma voz perfeita demais,
   produzida demais, é exatamente o que ativa esse radar. Na escolha de voz no ElevenLabs, prefira
   rótulos "conversational" a "narration"/"broadcast" quando existir a opção, mesmo nos capítulos de
   ficção, e não busque estabilidade no máximo — uma leitura com alguma variação natural soa mais
   humana que uma perfeitamente uniforme. Isso vale ainda mais pra nota de fechamento (regra 10):
   ela deve soar como alguém realmente gravando um áudio pros amigos/comunidade — auto-interrupções,
   reação no calor do momento, frase que não fecha perfeitinha, sem gancho de call-to-action
   roteirizado ("preparem-se, vem coisa incrível por aí"). Real vale mais que polido.
9c. **O pilar mais importante de todos: a narradora acompanha a história em tempo real, não recita
   fatos que já conhece de cor.** Descoberto testando a voz de Fantasy: um narrador perfeitamente
   equilibrado o tempo todo soa artificial/perdido, não "cansado e contido" como pretendido. Ela não
   é uma testemunha calma relatando o passado — é alguém lendo agora, sentindo cada decisão das
   personagens junto com quem ouve, se surpreendendo de verdade com as reviravoltas na hora em que
   elas acontecem. "Contido" não é sinônimo de neutro: ela segura a emoção porque se importa, não
   porque não sente nada. Deixe deslizes acontecerem exatamente nos momentos de virada — um leve
   tremor, uma pausa que quebra o ritmo quando algo surpreende, uma frase que acelera de emoção. Isso
   vale pra todo gênero, mas exige atenção redobrada nos mais minimalistas/frios (Horror, Shinobi,
   Cowboy, Heroes), que erram mais fácil pro lado robótico/distante. Na prática: Estabilidade no
   ElevenLabs não deve ir pro extremo "Mais estável" mesmo em vozes contidas — puxe pro meio-termo,
   com oscilação real o bastante pra soar como alguém vivendo a história, não recitando.
10. **A nota de fechamento também tem teto no AI Studio (~80s) — e não deve ser quebrada em partes
   como os capítulos**, mesmo nesse fallback. É uma reflexão única, contínua; fatiá-la em "parte
   1/2" quebra a intimidade do formato. Em vez de quebrar, **reescreva-a mais curta**: releia a
   história inteira, identifique o que a nota está realmente tentando dizer (os 2-3 momentos que
   mais marcaram + o gancho de comunidade), e comprima para caber em ~1 minuto (~130-140 palavras a
   ~140 palavras/min) sem perder essa intenção. Corte detalhe, não sentimento. No ElevenLabs Studio
   isso não é estritamente necessário (o Studio aguenta mais), mas o tamanho de ~1 minuto continua
   sendo bom formato pra clipe standalone de qualquer forma.
11. **Pra postar em Reels/TikTok/Shorts como série, corte cada capítulo em ~3-4 episódios de
   ~1:30-2min (não ~1min/6 episódios — testamos e era granularidade demais, arriscava virar spam se
   replicado em todo capítulo/livro) cortados em pontos de tensão real, não em duração fixa.** Isso é
   diferente do `_partes.md` (regra 4, que existe só por causa do teto técnico do AI Studio): aqui o
   corte é estratégia de conteúdo, pra virar série que prende sem sobrecarregar o feed — vale mesmo
   no ElevenLabs Studio, que não tem teto nenhum. Deixe os pontos de tensão reais decidirem o número
   exato por capítulo (um mais denso pode pedir 4, um mais curto pode caber em 2) em vez de forçar
   uma contagem fixa. **Não reescreva a prosa** — o texto de cada episódio é um recorte exato do
   capítulo original. O trabalho é só escolher onde cortar:
   - Parar bem antes de uma personagem nova/misteriosa falar (a prosa geralmente já entrega esse
     gancho de graça, só precisa parar na hora certa).
   - Terminar logo depois de uma fala/linha de maior impacto emocional (acusação, revelação, grito
     de guerra) — nunca no meio de uma ideia, nunca num trecho neutro.
   - O episódio final do capítulo pode fechar com resolução real — não precisa forçar gancho
     artificial no encerramento de um arco.
   Produza também um `_serie_notas.md` junto com os arquivos: uma tabela com cada corte + por que é
   gancho, e sugestão de legenda de post por episódio (curta, sem spoiler do próximo, nunca
   explicando mecânica do app — regra 8 vale aqui também). Salvo em `series/` dentro da pasta do
   livro. Comece só pelo primeiro capítulo de um livro pra validar o formato antes de replicar pros
   demais.
11b. **Identidade visual do vídeo: reaproveita a capa real do livro, não inventa nome novo.** Nada de
   introdução falada no início do episódio 1 (throat-clearing custa os segundos mais importantes de
   retenção) — o contexto entra por texto na tela, não por narração:
   - **Playlist/coleção** nos apps de vídeo = nome da saga (ex: "As Damas Celestiais"), agrupando
     todo vídeo daquele universo.
   - **Overlay do vídeo** (texto na tela) = a mesma estrutura da capa do livro já gerada no app —
     label pequeno "VOL <número>" em cima do título em destaque, mesma paleta/tipografia do gênero.
     Reaproveitar a capa real (em vez de um apelido inventado pro social) reforça visualmente "isso é
     do Sagas" sem precisar narrar isso em nenhum momento.
   Documente essa definição (nome da playlist + texto do overlay) no `_serie_notas.md` do livro.

## Formato do `voice_bible.md`

```
# Bíblia de Voz — <Título do livro>

## Direção de voz
(registro, ritmo, o que evitar — específico ao tom real da prosa, não só o gênero genérico)

## Instrução-base (colar no AI Studio junto com o texto)
"..."

## Configuração no ElevenLabs (sem campo de instrução)
- Voz: use a voz do gênero já desenhada em `docs/marketing/audiobooks/genre_voice_design.md`
  (Voice Design) se existir; senão, que tipo escolher no preview da biblioteca (grave/ágil/etc, o
  oposto do que evitar)
- Estabilidade: pra qual lado
- Velocidade: pra qual lado
- Modelo: v2 basta ou vale testar v3 com tags inline?

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

## Formato do texto puro (`NN_slug_texto_puro.txt`)

Só a prosa, nada mais — sem título, sem seção, sem nota de ênfase, sem marcação de bloco. A pessoa
seleciona tudo e cola direto no box do ElevenLabs. Nenhuma linha deveria precisar ser apagada antes
de colar.

## Onde salvar

`docs/marketing/audiobooks/<slug-do-livro>/` — `voice_bible.md` + um `.md` por capítulo (texto
inteiro, anotado) + `_texto_puro.txt` por capítulo (pronto pra colar) + `_partes.md` só se o destino
for o AI Studio + `series/` com os cortes de episódio (regra 11) e seu `_serie_notas.md`. Não precisa
passar pelo `feature_planning_agent`: isso é produção de conteúdo externa ao app, não exige nenhuma
feature nova (o export de PDF já existe hoje).
