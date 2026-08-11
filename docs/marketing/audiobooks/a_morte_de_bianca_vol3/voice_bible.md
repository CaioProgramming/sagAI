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
- Evite: qualquer traço de narração "de fantasia" (grave, arcaica, lenta o tempo todo). Isso soa mais
  como um podcast de true crime chique do que como conto sombrio.

## Instrução-base (colar no AI Studio junto com o texto de cada capítulo)

> "Narre este trecho de drama de elite/crime contemporâneo com voz afiada, ágil e ligeiramente
> sedutora — como quem descreve algo chocante com fascínio, não com peso grave. Acelere nas trocas
> de diálogo cortantes; desacelere só nos raros momentos de colapso emocional real. A frieza
> calculada dos Moretti deve soar elegante, nunca cansada."

## Configuração no ElevenLabs (Text to Speech / Estúdio — sem campo de instrução)

O ElevenLabs não tem campo de instrução separado — só um box de texto + configurações ao lado. A
instrução-base acima vira ajuste de interface, e aqui é o oposto do Vol. I de fantasia:

- **Voz**: escolha uma com descrição tipo "confident", "sharp", "sleek" — nada "warm"/"cozy"/"easygoing"
  (a voz padrão sugerida no ElevenLabs costuma ser desse tipo confortável, não serve aqui).
- **Estabilidade**: pro lado de **"Mais variável"** (ou meio-termo) — esse livro precisa de mais
  nuance emocional entre a frieza calculada e as raras rachaduras reais; estabilidade alta demais
  deixa tudo monótono, o oposto do "fascínio" que a direção de voz pede.
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
