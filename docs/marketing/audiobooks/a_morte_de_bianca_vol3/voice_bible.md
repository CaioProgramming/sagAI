# Bíblia de Voz — A Morte de Bianca, Vol. III

**Gênero**: Crime/drama de elite. Identidade visual rosa-choque, flamingo, script cursivo —
completamente diferente da fantasia sombria (vermelho/dourado, dragão, prosa grave). A prosa aqui
também é outra: contemporânea, cortante, glamourosa por fora e crua por dentro — registro tipo
"Succession"/drama de prestígio, não conto sombrio.

## Direção de voz

- Registro mais jovem, ágil e afiado — o oposto do narrador grave e cansado da fantasia. Frases
  curtas cortam como as falas dos personagens cortam.
- Tem um certo fascínio mórbido pela crueldade da Isabella — a voz narra como quem está impressionado
  com o quão elegante a crueldade calculada pode soar, sem julgar moralmente, só observando de perto.
- Ritmo ágil na maior parte do texto; desacelera só nos raros momentos de colapso emocional real
  (o pranto de Bianca no meio da rua, a ligação com a Cami) — o contraste é o que dá impacto.
- **O pilar mais importante: ela está acompanhando essa história em tempo real, não narrando um caso
  que já conhece de cor.** Sente cada decisão da Bianca como se estivesse descobrindo agora, se
  surpreende de verdade quando a Isabella faz algo ainda mais frio do que esperava, quase perde o
  fôlego junto com a Cami. A frieza calculada é impressionante justamente porque quem narra também
  se choca com ela — se a leitura sair polida e distante demais, perde exatamente o fascínio que faz
  o livro funcionar. Deslizes na hora da virada (uma pausa que corta, uma frase que acelera de
  surpresa) são bem-vindos.
- Evite: qualquer traço de narração "de fantasia" (grave, arcaica, lenta o tempo todo). Isso soa mais
  como um podcast de true crime chique do que como conto sombrio.

## Instrução-base (colar no AI Studio junto com o texto de cada capítulo)

> "Narre este trecho de drama de elite/crime contemporâneo com voz afiada, ágil e ligeiramente
> sedutora — como alguém acompanhando essa história agora, se surpreendendo com cada decisão da
> Bianca e cada frieza calculada da Isabella, não narrando um caso que já sabe de cor. Descreva algo
> chocante com fascínio genuíno, não com distância de reportagem. Acelere nas trocas de diálogo
> cortantes; desacelere só nos raros momentos de colapso emocional real. A frieza calculada dos
> Moretti deve soar elegante, nunca cansada — mas quem narra também se choca com ela, e isso pode
> escapar na entrega."

## Configuração no ElevenLabs (Text to Speech / Estúdio — sem campo de instrução)

O ElevenLabs não tem campo de instrução separado — só um box de texto + configurações ao lado. **A
instrução-base acima nunca vai colada no box de texto** — se colar, o motor narra essas frases
literalmente, como se fossem parte da história. Ela só existe pra orientar as escolhas abaixo. O
único texto que vai no box é `01_texto_puro.txt`, nada mais. Testamos voz desenhada no Voice Design
(`docs/marketing/audiobooks/genre_voice_design.md`) e saiu dramática/atuada demais — prefira uma voz
pronta da biblioteca, aqui é o oposto do Vol. I de fantasia:

- **Voz**: escolha uma com descrição tipo "confident", "sharp", "sleek" — nada "warm"/"cozy"/"easygoing"
  (a voz padrão sugerida no ElevenLabs costuma ser desse tipo confortável, não serve aqui). Prefira
  "conversational" a "narration"/"broadcast" quando existir a opção — mesmo esse livro sendo mais
  produzido/glamouroso na trama, a leitura não deve soar locução de estúdio, e sim alguém real e
  fascinado narrando. **Escolha uma voz que já tenha personalidade própria no preview** — o slider de
  Style abaixo amplifica o que a voz já tem, não cria caráter do zero numa voz genérica.
- **Estabilidade**: pro lado de **"Mais variável"** (ou meio-termo) — esse livro precisa de mais
  nuance emocional entre a frieza calculada e as raras rachaduras reais; estabilidade alta demais
  deixa tudo monótono, o oposto do "fascínio" que a direção de voz pede.
- **Style (Style Exaggeration)**: nível médio-alto — mais que na fantasia, já que esse livro pede uma
  narradora fascinada e presente, não uma leitura neutra. Em 0 fica plano/profissional; acima de ~60%
  costuma distorcer o áudio. É esse slider (não risada/suspiro pontual) que dá a sensação de alguém
  com opinião contando o caso, e não uma voz bonita só lendo.
- **Velocidade**: neutro a levemente **"Mais rápido"** — é o "ágil", oposto do ritmo pausado da
  fantasia.
- **Modelo**: v3 vale mais a pena aqui do que na fantasia — esse livro tem mais variação de emoção
  por cena, e tags inline tipo `[frio]`/`[sussurrando]`/`[quebra a voz]` ajudam a marcar o contraste
  Isabella-fria vs. Cami-descontrolada sem precisar trocar de voz.
- Fixe a mesma voz + os mesmos sliders pros capítulos do livro — consistência sem instrução escrita.

## Personagens que falam

Um narrador só — module o tom, não troque de voz:

- **Isabella**: cirúrgica, nunca em volume alto — quanto mais cruel a frase, mais calma a entrega.
- **Bianca**: alterna entre a máscara controlada e rachaduras reais — marque a diferença no ritmo
  (controlada = ágil/precisa; rachando = pausas, quebra).
- **Cami**: a única voz genuinamente sem controle — embargada, real, sem polimento.
- **Kimmy**: plana e clínica, quase sem afeto — o oposto do desespero da Cami na mesma cena.
- **Milena**: o respiro do livro — mais leve, mais brincalhona, sotaque de quem não é de Santa Maria.

## Notas gerais de consistência

- Use exatamente esta instrução-base em todos os capítulos — só as notas de ênfase pontual mudam.
- Diferente da fantasia: aqui **não** existe um "narrador cansado testemunhando tragédias" — existe
  alguém fascinado narrando um jogo de poder em tempo real. Se a leitura sair grave/lenta demais, não
  bate com o material.
