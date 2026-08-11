# Vozes por Gênero — Voice Design (ElevenLabs)

Prompts prontos pra colar em **Voices → My Voices → Add a new voice → Voice Design** (Realistic
Voices) do ElevenLabs — um feminino e um masculino por gênero do app. Gera 3 opções por prompt,
escolha a melhor e salve com um nome tipo `Sagas — Fantasy — F`.

**Isso é infraestrutura de gênero, não de livro.** Roda uma vez por gênero/sexo (custa só o crédito
do preview) e a voz salva fica reutilizável em todo audiolivro daquele gênero — não precisa desenhar
de novo a cada `voice_bible.md` novo. Quando um livro específico pedir algo mais fino (ex: "Rayffa é
mais contida que a média do gênero"), isso continua sendo ajuste de sliders (Estabilidade/
Velocidade) no `voice_bible.md` do livro, não uma voz nova.

**Fonte do tom**: cada essência abaixo vem do `${genero}_conversation_blueprint` real do Remote
Config do app — o mesmo registro de voz que já rege como NPCs e o mundo falam dentro do jogo.
Parafraseado aqui pro contexto de narração de audiolivro, não copiado literal.

**Regra do amadorismo** (ver `audiobook_agent.md`, regra 9): todo prompt abaixo já carrega uma pista
de imperfeição natural — voz real gravando em casa, não locução de estúdio. Isso é intencional em
todos os 9 gêneros, mesmo os mais "grandiosos" na ficção (Heroes, Space Opera): a entrega pode ser
dramática no conteúdo, mas nunca soar produzida demais.

---

## Fantasy · *Magic & Myth*

**Essência**: um mundo onde poder tem custo real — nunca é dado, é conquistado, roubado ou imposto.
Brutalidade e beleza são a mesma matéria. A voz reflete o que essa pessoa já pagou pra chegar aqui,
não performance.

- **Feminino**: Mulher entre 30-45 anos, voz grave e cansada, controlada — fala como quem sobreviveu
  a algo real e não precisa mais provar nada. Ritmo pausado, silêncios reais entre frases carregadas,
  leve aspereza natural. Sem polimento de estúdio — soa como alguém contando uma história tarde da
  noite, não uma dubladora profissional.
- **Masculino**: Homem entre 40-55, voz grave e rouca, autoridade conquistada pelo custo, não pela
  performance. Fala devagar, segura o silêncio antes de frases pesadas. Leve imperfeição natural —
  parece alguém real narrando em casa, não um narrador de trailer épico.

## Cyberpunk · *Neon & Tech*

**Essência**: uma cidade que nunca foi feita pra quem vive nela. Dois registros coexistem — o duro/
sarcástico de sobrevivência e o contemplativo/melancólico por baixo. A tristeza nunca é anunciada,
só se acumula.

- **Feminino**: Mulher de 25-35, voz rouca e seca, cansada mas afiada — fala como quem viu a cidade
  engolir gente e seguiu em frente mesmo assim. Tom levemente irônico, humor sombrio por baixo, a
  frase às vezes corta no meio. Entrega crua, não produzida — como alguém gravando em casa tarde da
  noite, não uma voz de propaganda.
- **Masculino**: Homem de 30-40, voz rouca, ritmo cortado — frases curtas, sem explicar demais. Uma
  melancolia contida por trás do sarcasmo. Levemente imperfeito, sem polimento — soa real, não uma
  narração produzida.

## Horror · *Shadows & Fear*

**Essência**: peso psicológico e isolamento sufocante. O silêncio estica até doer. Nunca grita — a
calma controlada é mais perturbadora que qualquer grito.

- **Feminino**: Mulher de 30-45, voz baixa e contida, quase sussurrada — fala devagar, deixando o
  silêncio esticar até ficar desconfortável. Tensão contida, nunca gritada. Levemente rouca e
  imperfeita, sem clichê de narração de terror de estúdio — soa como alguém real contando algo
  perturbador baixinho no escuro.
- **Masculino**: Homem de 35-50, voz grave e controlada, quase sem inflexão — o tipo de calma mais
  assustadora que o grito. Pausas longas, ritmo claustrofóbico. Entrega crua, não produzida — como um
  sussurro real, não narração de estúdio.

## Heroes · *Artifacts & Powers*

**Essência**: poder que nunca foi justo — mudou quem tocou e deixou sombra proporcional à luz. O
registro do "Veterano" (esparso, preciso, já não se surpreende com nada) é a base mais estável pra
narrar um livro inteiro.

- **Feminino**: Mulher de 35-45, voz firme e econômica, quase sem emoção na superfície — fala como
  quem já viu demais pra se surpreender com qualquer coisa, inclusive consigo mesma. Frases curtas,
  tom medido. Entrega natural e levemente crua, não de locução de dublagem de herói — soa real.
- **Masculino**: Homem de 40-55, voz firme e seca, econômica — precisão de quem parou de se
  impressionar há muito tempo. Ritmo comedido, quase monótono de propósito. Levemente imperfeito, sem
  grandiosidade de trailer de herói — soa como alguém real narrando.

## Crime · *Blood & Noir*

**Essência**: drama de elite contado em retrospecto — sempre sobrevivendo o suficiente pra ser
perguntado depois. Fascínio mórbido pela crueldade calculada, nunca julgamento moral explícito.
(Já validado no piloto de "A Morte de Bianca".)

- **Feminino**: Mulher de 28-40, voz confiante e afiada, levemente sedutora — narra como quem está
  fascinada pela elegância do que está descrevendo. Ágil na maior parte, desacelera só nos raros
  momentos de colapso emocional real. Entrega natural, levemente crua — como alguém real gravando um
  podcast caseiro de true crime, não uma locução de documentário produzido.
- **Masculino**: Homem de 30-42, voz confiante e cortante, ritmo ágil — soa fascinado pelo poder
  calculado que descreve. Frases que cortam como as falas dos personagens. Entrega imperfeita e
  natural, sem polimento de narração profissional.

## Shinobi · *Blades & Honor*

**Essência**: minimalista até o osso. O que não é dito pesa mais que o que é dito. Silêncio é arma,
não vazio.

- **Feminino**: Mulher de 30-45, voz baixa e extremamente contida — fala pouco, cada palavra pesada,
  como se dizer mais fosse revelar demais. Pausas longas antes e depois de frases curtas. Entrega
  crua, quase sussurrada, sem nenhuma teatralidade de dublagem de anime — soa real e minimalista.
- **Masculino**: Homem de 35-50, voz grave e mínima — silêncio é parte da fala. Frases curtas e
  definitivas, nunca explicando além do necessário. Ritmo de respiração antes do golpe: quieto,
  quieto, depois preciso. Entrega natural, imperfeita, sem grandiosidade.

## Space Opera · *Stars & War*

**Essência**: a galáxia não é palco de impérios, é acúmulo de tudo que aconteceu sem ninguém prestar
atenção. Melancolia silenciosa de encontros passageiros — nunca dramatizada, só acumulada.

- **Feminino**: Mulher de 30-45, voz calma e levemente distante, com uma melancolia que nunca é
  anunciada — fala como quem já se despediu de muita gente e lugares e aprendeu a carregar isso
  baixinho. Ritmo suave. Entrega natural e levemente imperfeita — não é narração grandiosa de cinema,
  é alguém real refletindo.
- **Masculino**: Homem de 35-50, voz grave e serena, ritmo lento — carrega uma tristeza discreta por
  baixo da calma, nunca explicada diretamente. Entrega crua, sem grandiloquência de trailer de ficção
  científica — soa como alguém real narrando à noite.

## Cowboy · *Guns & Sand*

**Essência**: a dureza nunca é o ponto — o que está embaixo dela é. Sentem tudo, mostram quase nada.
Silêncio é uma forma de diálogo que essas pessoas dominaram por necessidade.

- **Feminino**: Mulher de 35-50, voz seca e firme, econômica nas palavras — sente tudo mas mostra
  pouco, por hábito e necessidade. Ritmo pausado, silêncios que dizem mais que a fala. Entrega
  natural e rústica, sem sotaque de faroeste caricato — soa real, cansada e verdadeira.
- **Masculino**: Homem de 40-55, voz grave e áspera, fala pouco e devagar — o peso está no que não é
  dito. Ritmo lento, pausas longas. Entrega crua e imperfeita, sem dramatização de filme de cowboy —
  soa como alguém real contando uma história ao redor do fogo.

## Punk Rock · *Anarchy & Riots*

**Essência**: gente jovem e inacabada, péssima em lidar com sentimento — um término tem peso de chefe
de fim de jogo, um show ruim parece morte pública. A voz que narra também é seu próprio personagem,
com atitude própria.

- **Feminino**: Mulher de 20-28, voz crua e um pouco impaciente, cheia de atitude — fala rápido
  quando animada, trava quando o assunto é sentimento de verdade. Energia de quem finge que não liga,
  mas liga demais. Entrega bem natural e imperfeita, tipo alguém gravando um áudio pros amigos no
  quarto, não uma narração produzida.
- **Masculino**: Homem de 20-28, voz meio rouca e irregular, atitude na entrega mas sem polimento
  nenhum — acelera empolgado, trava quando fica sério de verdade. Levemente caótico, real, como se
  estivesse gravando de improviso, não performando um papel.

---

## Onde salvar a voz depois de desenhada

Guarde o `voice_id` retornado (visível na URL/detalhes da voz salva) junto com o nome dela num
registro simples, por exemplo uma tabela aqui mesmo:

| Gênero | Sexo | Nome salvo no ElevenLabs | voice_id |
|---|---|---|---|
| Fantasy | F | Sagas — Fantasy — F | _(preencher após gerar)_ |
| Fantasy | M | Sagas — Fantasy — M | _(preencher após gerar)_ |
| ... | | | |

Depois que a tabela estiver preenchida, o `voice_bible.md` de cada livro novo daquele gênero só
referencia o nome salvo em vez de garimpar voz genérica da biblioteca — mais rápido e mais fiel ao
gênero real do app.
