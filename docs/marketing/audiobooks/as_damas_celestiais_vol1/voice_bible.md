# Bíblia de Voz — As Damas Celestiais, Vol. I: O Pacto do Eremita e o Caminho de Espinhos

**Gênero**: Fantasia. Paleta visual do app pro gênero é vermelho/dourado, épico — mas a prosa deste
livro específico não é whimsical, é **fantasy sombrio de exílio e vingança**. A voz precisa refletir
isso, não o fantasy "nobre e arejado" genérico.

## Direção de voz

- Registro grave a médio-grave, cansado, contido — não teatral, não "narrador de trailer épico".
  Mais crônica de guerra do que conto de fadas.
- Ritmo pausado (~130-140 palavras/min), com espaço real pra silêncio entre frases longas — a prosa
  já é densa, não precisa de pressa pra parecer importante.
- A emoção mora no timbre, não na performance. Evite qualquer entonação "de apresentador". Rayffa é
  uma personagem que reprime sentimento — a leitura reflete isso: contida por fora, pesada por
  dentro.
- Evite: voz jovem/brilhante, ritmo acelerado, entonação de conto infantil, qualquer sotaque
  caricato de "fantasia medieval".

## Instrução-base (colar no AI Studio junto com o texto de cada capítulo)

> "Narre este trecho de fantasia sombria com voz grave e cansada, ritmo pausado (não acelere), como
> alguém que testemunhou tragédias e agora relata os fatos com peso emocional contido — sem
> teatralidade, sem pressa. Deixe pausas reais entre frases carregadas. A emoção aparece no timbre,
> nunca em exagero de performance."

## Configuração no ElevenLabs (Text to Speech / Estúdio — sem campo de instrução)

O ElevenLabs não tem campo de instrução separado — só um box de texto + configurações ao lado. A
instrução-base acima vira ajuste de interface:

- **Voz**: escolha uma com descrição tipo "calm", "deep", "weary", "grave" — evite as rotuladas
  "upbeat"/"bright"/"energetic" (a voz padrão sugerida geralmente é desse tipo, não serve aqui).
  Ouça o preview antes de bater o olho na descrição. Prefira vozes rotuladas "conversational" a
  "narration"/"broadcast" quando existir a opção — o objetivo não é soar audiolivro profissional de
  estúdio, é soar alguém real lendo com sentimento contido.
- **Estabilidade**: pro lado de **"Mais estável"** — leitura mais controlada, menos variação
  aleatória, sem exagero de performance (é o equivalente ao "sem teatralidade" da instrução). Mas
  não no máximo: estabilidade 100% soa robótica/perfeita demais, o que hoje em dia é exatamente o
  que ativa o radar de "isso é IA". Deixe uma pequena variação natural.
- **Velocidade**: pro lado de **"Mais devagar"** — é o "ritmo pausado, não acelere".
- **Modelo**: v2 já deve dar conta só com voz + sliders certos. Se quiser mais controle fino, o v3
  aceita tags de emoção dentro do próprio texto colado (ex: `[cansado]`, `[pausa]`) — opcional, teste
  se o v2 não estiver saindo pesado o suficiente.
- Fixe a mesma voz + os mesmos sliders pros 3 capítulos — é o que garante consistência sem instrução
  escrita.

## Personagens que falam

Um narrador só, sem trocar de voz — module levemente o tom nas falas diretas, sem imitação:

- **Rayffa**: contida mesmo ameaçando; frieza, não raiva gritada. Fala mais baixo, não mais rápido.
- **Lysandra**: firme, quase sem afeto — voz de quem já decidiu, não de quem está convencendo.
- **Isis**: mais lenta, amarga, quase sussurrada — a dor dela pesa na cadência, não no volume.
- **Archie**: o único ponto de contraste — leve arrogância/desdém no tom, um traço mais afiado que o
  resto (ele é o vilão, pode ter um pouco mais de "corte" na voz).
- **Milford**: mais velho e rouco, ritmo ainda mais lento que o narrador — cada frase dele pesa como
  se custasse esforço pra falar.

## Notas gerais de consistência

- Use exatamente esta instrução-base em todos os capítulos do livro — só as notas de ênfase pontual
  (no início de cada `.md` de capítulo) mudam.
- Se o AI Studio permitir fixar uma voz específica entre sessões, escolha uma voz de registro
  grave/melancólico da lista atual e reuse o mesmo ID em todos os capítulos — confirme o nome exato
  na interface, a lista de vozes muda com frequência.
