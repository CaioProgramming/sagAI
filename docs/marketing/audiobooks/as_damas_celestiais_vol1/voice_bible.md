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
- **O pilar mais importante: ela está acompanhando a história em tempo real, não recontando fatos já
  sabidos.** Nada de "testemunha calma relatando o passado" — é alguém lendo agora, sentindo cada
  decisão das personagens junto com quem ouve, se surpreendendo com as reviravoltas na hora em que
  elas acontecem. "Contida" não é sinônimo de impassível: é alguém segurando a emoção porque se
  importa, não porque não sente nada. Se a leitura sair perfeitamente equilibrada o tempo todo, ela
  vira artificial — o oposto do objetivo. Deixe pequenos deslizes humanos acontecerem exatamente nos
  momentos de virada: uma pausa que quebra o ritmo quando algo inesperado acontece, um leve tremor
  na voz num momento pesado, uma frase que sai um pouco apressada porque ela também quer saber o que
  vem a seguir. Isso não é erro de qualidade — é o que a torna crível como pessoa lendo e vivendo a
  história, não como máquina narrando fatos já conhecidos.
- Evite: voz jovem/brilhante, ritmo acelerado, entonação de conto infantil, qualquer sotaque
  caricato de "fantasia medieval".

## Instrução-base (colar no AI Studio junto com o texto de cada capítulo)

> "Narre este trecho de fantasia sombria com voz grave e cansada, ritmo pausado (não acelere), como
> alguém lendo essa história agora, pela primeira vez, sentindo cada decisão das personagens e se
> surpreendendo com as reviravoltas junto com quem ouve — não uma testemunha calma recontando fatos
> já sabidos. Peso emocional contido, mas nunca frieza impecável: de vez em quando essa emoção
> escapa, principalmente nos momentos de virada — um leve tremor, uma pausa que quebra o ritmo
> perfeito, uma frase que sai rápida demais porque você também quer saber o que vem a seguir. Sem
> teatralidade, sem exagero de performance. Um deslize ocasional é bem-vindo, é o que soa humano."

## Configuração no ElevenLabs (Text to Speech / Estúdio — sem campo de instrução)

O ElevenLabs não tem campo de instrução separado — só um box de texto + configurações ao lado. A
instrução-base acima vira ajuste de interface:

- **Voz**: escolha uma com descrição tipo "calm", "deep", "weary", "grave" — evite as rotuladas
  "upbeat"/"bright"/"energetic" (a voz padrão sugerida geralmente é desse tipo, não serve aqui).
  Ouça o preview antes de bater o olho na descrição. Prefira vozes rotuladas "conversational" a
  "narration"/"broadcast" quando existir a opção — o objetivo não é soar audiolivro profissional de
  estúdio, é soar alguém real lendo com sentimento contido.
- **Estabilidade**: **meio-termo, não "Mais estável"** — testamos no lado mais estável e o resultado
  saiu artificial/perdido, sem o "vazamento" emocional de uma leitora envolvida (ver nota acima na
  Direção de voz). Puxe o slider mais pro centro: controle suficiente pra não virar performance
  teatral, mas com oscilação real o bastante pra soar como alguém que sente a história, não uma voz
  robótica lendo com sotaque grave. Se ainda sair frio demais, teste um passo a mais em direção a
  "Mais variável".
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
