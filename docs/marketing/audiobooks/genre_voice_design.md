# Vozes por Gênero — Voice Design (ElevenLabs)

Prompts prontos pra colar em **Voices → My Voices → Add a new voice → Voice Design** (Realistic
Voices) do ElevenLabs — um feminino e um masculino por gênero do app. Gera 3 opções por prompt,
escolha a melhor e salve com um nome tipo `Sagas — Fantasy — F`.

**Prompt em inglês, de propósito.** A documentação do ElevenLabs recomenda escrever o prompt do
Voice Design em inglês mesmo quando a voz vai narrar em outro idioma — o modelo entende descrição de
estilo melhor em inglês, e pra não haver "drift" de idioma cada prompt já declara explícito na
primeira frase que a voz fala português do Brasil. A `Essência` de cada gênero abaixo fica em
português só como contexto pra você — não precisa colar isso, só o bloco `Prompt`.

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

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 30s-45s, low and weary
  voice, controlled — sounds like someone who survived something real and no longer needs to prove
  it. Slow pacing, real silences between weighted sentences, slight natural roughness. No studio
  polish — sounds like someone telling a story late at night, not a professional voice actress.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his 40s-55s, deep and raspy
  voice, authority earned through cost, not performance. Speaks slowly, holds silence before heavy
  sentences. Slight natural imperfection — sounds like a real person narrating at home, not an epic
  trailer narrator.*

## Cyberpunk · *Neon & Tech*

**Essência**: uma cidade que nunca foi feita pra quem vive nela. Dois registros coexistem — o duro/
sarcástico de sobrevivência e o contemplativo/melancólico por baixo. A tristeza nunca é anunciada,
só se acumula.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 20s-30s, raspy and dry
  voice, tired but sharp — sounds like someone who watched the city swallow people and kept going
  anyway. Slightly ironic tone, dark humor underneath, sentences sometimes cut off mid-thought. Raw,
  unproduced delivery — like someone recording at home late at night, not an advertisement voice.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his 30s-40s, raspy voice,
  clipped rhythm — short sentences, doesn't over-explain. Contained melancholy underneath the
  sarcasm. Slightly imperfect, no polish — sounds real, not a produced narration.*

## Horror · *Shadows & Fear*

**Essência**: peso psicológico e isolamento sufocante. O silêncio estica até doer. Nunca grita — a
calma controlada é mais perturbadora que qualquer grito.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 30s-45s, low and
  restrained voice, almost whispered — speaks slowly, letting silence stretch until it becomes
  uncomfortable. Contained tension, never shouted. Slightly raspy and imperfect, no cliché
  horror-studio narration — sounds like a real person telling something disturbing quietly in the
  dark.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his 30s-50s, deep and
  controlled voice, almost no inflection — the kind of calm that's more frightening than a scream.
  Long pauses, claustrophobic pacing. Raw, unproduced delivery — like a real whisper, not studio
  narration.*

## Heroes · *Artifacts & Powers*

**Essência**: poder que nunca foi justo — mudou quem tocou e deixou sombra proporcional à luz. O
registro do "Veterano" (esparso, preciso, já não se surpreende com nada) é a base mais estável pra
narrar um livro inteiro.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her mid-30s to mid-40s, firm
  and economical voice, almost no emotion on the surface — sounds like someone who has seen too much
  to be surprised by anything anymore, including herself. Short sentences, measured tone. Natural
  and slightly raw delivery, not superhero-dubbing narration — sounds real.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his 40s-55s, firm and dry
  voice, economical — the precision of someone who stopped being impressed long ago. Restrained
  pacing, almost deliberately monotone. Slightly imperfect, no hero-trailer grandiosity — sounds
  like a real person narrating.*

## Crime · *Blood & Noir*

**Essência**: drama de elite contado em retrospecto — sempre sobrevivendo o suficiente pra ser
perguntado depois. Fascínio mórbido pela crueldade calculada, nunca julgamento moral explícito.
(Já validado no piloto de "A Morte de Bianca".)

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her late 20s to late 30s,
  confident and sharp voice, slightly seductive — narrates like someone fascinated by the elegance
  of what she's describing. Agile pacing most of the time, slowing only in rare moments of real
  emotional collapse. Natural, slightly raw delivery — like someone recording a homemade true-crime
  podcast, not a produced documentary voiceover.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his early 30s to early 40s,
  confident and cutting voice, agile rhythm — sounds fascinated by the calculated power he's
  describing. Sentences that cut like the characters' own lines. Imperfect and natural delivery, no
  professional-narration polish.*

## Shinobi · *Blades & Honor*

**Essência**: minimalista até o osso. O que não é dito pesa mais que o que é dito. Silêncio é arma,
não vazio.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 30s-45s, low and
  extremely restrained voice — speaks little, every word weighted, as if saying more would reveal
  too much. Long pauses before and after short sentences. Raw, almost whispered delivery, no
  anime-dub theatrics — sounds real and minimalist.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his mid-30s to 50s, deep and
  minimal voice — silence is part of the speech. Short, definitive sentences, never over-explaining.
  Breath-before-the-strike rhythm: quiet, quiet, then precise. Natural, imperfect delivery, no
  grandiosity.*

## Space Opera · *Stars & War*

**Essência**: a galáxia não é palco de impérios, é acúmulo de tudo que aconteceu sem ninguém prestar
atenção. Melancolia silenciosa de encontros passageiros — nunca dramatizada, só acumulada.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 30s-45s, calm and
  slightly distant voice, carrying a melancholy that's never announced — sounds like someone who has
  said goodbye to many people and places and learned to carry it quietly. Soft pacing. Natural and
  slightly imperfect delivery — not grand cinematic space-opera narration, more like a real person
  reflecting.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his mid-30s to 50s, deep and
  serene voice, slow pacing — carries a quiet sadness underneath the calm, never explained directly.
  Raw delivery, no sci-fi trailer grandiloquence — sounds like a real person narrating at night.*

## Cowboy · *Guns & Sand*

**Essência**: a dureza nunca é o ponto — o que está embaixo dela é. Sentem tudo, mostram quase nada.
Silêncio é uma forma de diálogo que essas pessoas dominaram por necessidade.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her 30s-50s, dry and firm
  voice, economical with words — feels everything but shows little, out of habit and necessity.
  Slow pacing, silences that say more than the words. Natural, rustic delivery, no cartoonish
  western accent — sounds real, tired and true.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his 40s-55s, deep and rough
  voice, speaks little and slowly — the weight is in what's left unsaid. Slow pacing, long pauses.
  Raw and imperfect delivery, no cowboy-movie dramatization — sounds like a real person telling a
  story around a campfire.*

## Punk Rock · *Anarchy & Riots*

**Essência**: gente jovem e inacabada, péssima em lidar com sentimento — um término tem peso de chefe
de fim de jogo, um show ruim parece morte pública. A voz que narra também é seu próprio personagem,
com atitude própria.

- **Feminino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Woman in her early-to-mid 20s, raw
  and slightly impatient voice, full of attitude — talks fast when excited, freezes up when real
  feelings come up. Energy of someone pretending not to care, but caring too much. Very natural and
  imperfect delivery, like someone recording a voice message for friends in their bedroom, not a
  produced narration.*
- **Masculino** — Prompt: *Speaks Brazilian Portuguese (pt-BR). Man in his early-to-mid 20s, slightly
  raspy and uneven voice, attitude in the delivery but zero polish — speeds up when excited, freezes
  when things get real. Slightly chaotic, real, like he's recording off-the-cuff, not performing a
  role.*

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
