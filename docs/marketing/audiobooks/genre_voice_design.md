# Vozes por Gênero — Voice Design (ElevenLabs)

**Status: plano B, não o caminho padrão.** Testamos essas vozes geradas e o resultado saiu
dramático/atuado demais — a expressividade fica "de fábrica" na voz e nenhum slider desfaz isso por
completo. Isso quebra o objetivo de soar casual ("alguém comum lendo o livro", não uma performance).
**O caminho padrão agora é escolher uma voz pronta da biblioteca do ElevenLabs** (ouça o preview até
achar uma que combine com a `Essência` de cada gênero abaixo) e colar só o texto puro, sem nada
gerado sob medida. Guarde este arquivo pra quando nenhuma voz da biblioteca servir pra um gênero
específico — aí sim vale tentar desenhar uma.

Prompts prontos pra colar em **Voices → My Voices → Add a new voice → Voice Design** (Realistic
Voices) do ElevenLabs — um feminino e um masculino por gênero do app.

**Um prompt por vez.** Cada geração do Voice Design produz 3 variações de **uma única voz** — não dá
pra colar dois prompts juntos e sair com duas vozes distintas na mesma rodada. O fluxo é: cola 1
prompt → gera 3 opções → escolhe a melhor → salva com nome (`Sagas — Fantasy — F`) → limpa o campo →
cola o próximo. São 18 rodadas sequenciais no total (9 gêneros × 2). Vale começar só com **Fantasy e
Crime** (os dois já validados nos pilotos) antes de gerar os outros 7 — confirma que o resultado bate
com o esperado antes de gastar crédito nos 14 restantes.

**Formato do prompt**: segue o formato recomendado pela própria documentação do ElevenLabs:

```
Native <Language>. <Gender>, <Age range>. <Quality level>.
Persona: <2–5 words>. Emotion: <2–3 adjectives>.
<1–2 sentences about timbre, pacing, delivery>
```

- Prompt em inglês, de propósito: o Voice Design entende descrição de estilo melhor em inglês, mesmo
  pra uma voz que vai narrar em outro idioma — por isso cada prompt abre com `Native Portuguese,
  Brazilian Portuguese (pt-BR)` pra travar o idioma/dialeto e evitar drift.
- **Evite a palavra "accent"** no prompt — o ElevenLabs avisa que isso pode disparar troca de sotaque
  regional sem querer; descreva entonação/estilo em vez disso (por isso os prompts abaixo dizem
  "understated"/"theatrical" em vez de "sem sotaque").
- **Evite palavras de efeito** tipo "reverb", "echo", "phone", "tape" — pioram a qualidade do áudio
  gerado.

A `Essência` de cada gênero abaixo fica em português só como contexto pra você — não precisa colar
isso, só o bloco `Prompt`.

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

**Regra da leitora envolvida — o pilar mais importante de todos** (ver `audiobook_agent.md`, regra
9c — descoberta testando a voz de Fantasy, que saiu impassível/artificial demais no primeiro teste):
"contido"/"restrained" não pode virar sinônimo de neutro. A narradora de qualquer gênero está
acompanhando a história **em tempo real**, não recontando fatos que já conhece de cor — sente cada
decisão das personagens junto com quem ouve, se surpreende de verdade com as reviravoltas na hora em
que elas acontecem. Não é uma testemunha calma relatando o passado, é alguém lendo agora e vivendo
junto. Por isso pequenos deslizes humanos nos momentos de virada (um tremor, uma pausa que quebra o
ritmo, uma frase que acelera de surpresa) não são defeito — são a prova de que ela também está
sentindo. Nos gêneros mais minimalistas/frios (Horror, Shinobi, Cowboy, Heroes) isso é ainda mais
fácil de errar pro lado robótico — tome cuidado extra ao testar essas vozes.

---

## Fantasy · *Magic & Myth*

**Essência**: um mundo onde poder tem custo real — nunca é dado, é conquistado, roubado ou imposto.
Brutalidade e beleza são a mesma matéria. A voz reflete o que essa pessoa já pagou pra chegar aqui,
não performance.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 30-45. Ok
  quality. Persona: someone reading this story out loud right now, living it in real time — not a
  narrator who already knows how it ends. Emotion: weary and restrained most of the time, but
  genuinely caught off guard by plot twists — feeling each character's decision as it happens. Low,
  rough timbre with slow pacing and real pauses; natural imperfection exactly at turning points — a
  catch in the voice, a rushed line, a pause that breaks the rhythm when something surprises her —
  never perfectly composed, not a professional voice actress reciting a script she already knows.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 40-55. Ok quality.
  Persona: someone reading this story out loud right now, living it in real time — not a narrator
  who already knows how it ends. Emotion: grave and weary most of the time, but genuinely caught off
  guard by plot twists — feeling each character's decision as it happens. Deep, raspy timbre with
  slow pacing and long pauses; natural imperfection exactly at turning points — a catch in the
  voice, a rushed line — never perfectly composed, not an epic trailer narrator reciting a script he
  already knows.*

## Cyberpunk · *Neon & Tech*

**Essência**: uma cidade que nunca foi feita pra quem vive nela. Dois registros coexistem — o duro/
sarcástico de sobrevivência e o contemplativo/melancólico por baixo. A tristeza nunca é anunciada,
só se acumula.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 25-35. Ok
  quality. Persona: streetwise survivor, dry wit. Emotion: tired, sharp, sardonic. Raspy timbre,
  clipped pacing that cuts off mid-thought, dark humor undertone; raw, homemade delivery, not an
  advertisement voice.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 30-40. Ok quality.
  Persona: street-hardened loner. Emotion: sarcastic, guarded, melancholic. Raspy timbre, clipped
  short sentences, contained sadness beneath the sarcasm; slightly imperfect, homemade delivery.*

## Horror · *Shadows & Fear*

**Essência**: peso psicológico e isolamento sufocante. O silêncio estica até doer. Nunca grita — a
calma controlada é mais perturbadora que qualquer grito.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 30-45. Ok
  quality. Persona: quiet witness to dread. Emotion: restrained, unsettled, controlled. Low,
  near-whispered timbre with slow pacing and long uncomfortable silences; raw, homemade delivery,
  not studio horror narration.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 30-50. Ok quality.
  Persona: calm harbinger of dread. Emotion: controlled, ominous, still. Deep, flat timbre with
  almost no inflection, long claustrophobic pauses; raw, homemade delivery, not studio narration.*

## Heroes · *Artifacts & Powers*

**Essência**: poder que nunca foi justo — mudou quem tocou e deixou sombra proporcional à luz. O
registro do "Veterano" (esparso, preciso, já não se surpreende com nada) é a base mais estável pra
narrar um livro inteiro.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 35-45. Ok
  quality. Persona: unshakeable veteran. Emotion: measured, weary, resolute. Firm, economical timbre
  with short sentences and even pacing; natural, slightly raw delivery, not superhero-dubbing
  narration.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 40-55. Ok quality.
  Persona: seasoned veteran, unimpressed. Emotion: dry, measured, restrained. Firm timbre with
  deliberately restrained, near-monotone pacing; slightly imperfect delivery, no hero-trailer
  grandiosity.*

## Crime · *Blood & Noir*

**Essência**: drama de elite contado em retrospecto — sempre sobrevivendo o suficiente pra ser
perguntado depois. Fascínio mórbido pela crueldade calculada, nunca julgamento moral explícito.
(Já validado no piloto de "A Morte de Bianca".)

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 28-40. Ok
  quality. Persona: someone reading this story out loud right now, hooked and reacting live to it —
  not a host who already knows the whole case. Emotion: confident and sharp most of the time, but
  visibly thrown, delighted, or unsettled when a character makes a shocking decision, as if
  discovering it in the moment. Agile pacing that catches or slows exactly when something genuinely
  surprises her; smooth timbre with natural imperfection at emotional peaks. Homemade delivery, like
  someone recording a true-crime podcast for friends, not a produced documentary voiceover.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 30-42. Ok quality.
  Persona: someone reading this story out loud right now, hooked and reacting live to it — not a
  host who already knows the whole case. Emotion: confident and cutting most of the time, but
  visibly thrown or fascinated when a character makes a shocking decision, as if discovering it in
  the moment. Sharp timbre, agile pacing that catches at turning points; imperfect, natural
  delivery, no professional-narration polish.*

## Shinobi · *Blades & Honor*

**Essência**: minimalista até o osso. O que não é dito pesa mais que o que é dito. Silêncio é arma,
não vazio.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 30-45. Ok
  quality. Persona: restrained, minimalist warrior. Emotion: still, contained, precise. Low timbre,
  extremely sparse delivery with long pauses around short sentences; raw, near-whispered, no
  anime-dub theatrics.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 35-50. Ok quality.
  Persona: silent, disciplined warrior. Emotion: still, precise, restrained. Deep, minimal timbre
  with breath-before-the-strike pacing — quiet, quiet, then precise; natural, imperfect delivery, no
  grandiosity.*

## Space Opera · *Stars & War*

**Essência**: a galáxia não é palco de impérios, é acúmulo de tudo que aconteceu sem ninguém prestar
atenção. Melancolia silenciosa de encontros passageiros — nunca dramatizada, só acumulada.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 30-45. Ok
  quality. Persona: wandering, reflective traveler. Emotion: calm, distant, quietly melancholic.
  Soft timbre with gentle pacing and unannounced sadness; natural, slightly imperfect delivery, more
  like a person reflecting than cinematic narration.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 35-50. Ok quality.
  Persona: weary cosmic wanderer. Emotion: serene, quietly sad, calm. Deep timbre with slow pacing
  and understated sorrow beneath the calm; raw delivery, no sci-fi trailer grandiloquence.*

## Cowboy · *Guns & Sand*

**Essência**: a dureza nunca é o ponto — o que está embaixo dela é. Sentem tudo, mostram quase nada.
Silêncio é uma forma de diálogo que essas pessoas dominaram por necessidade.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 35-50. Ok
  quality. Persona: guarded frontier survivor. Emotion: restrained, weary, sincere. Dry, firm timbre
  with slow pacing and meaningful silences; natural, rustic delivery, understated rather than
  theatrical.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 40-55. Ok quality.
  Persona: quiet frontier elder. Emotion: restrained, weary, sincere. Deep, rough timbre with slow
  pacing, long pauses, weight carried in what's unsaid; raw delivery, understated rather than
  theatrical.*

## Punk Rock · *Anarchy & Riots*

**Essência**: gente jovem e inacabada, péssima em lidar com sentimento — um término tem peso de chefe
de fim de jogo, um show ruim parece morte pública. A voz que narra também é seu próprio personagem,
com atitude própria.

- **Feminino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Female, 20-28. Ok
  quality. Persona: restless young scene kid. Emotion: impatient, defensive, attached. Raw timbre,
  fast pacing when excited that freezes up on real feelings; very natural, imperfect delivery, like
  a voice message recorded for friends, not produced narration.*
- **Masculino** — Prompt: *Native Portuguese, Brazilian Portuguese (pt-BR). Male, 20-28. Ok quality.
  Persona: restless young scene kid. Emotion: excitable, guarded, attached. Slightly raspy, uneven
  timbre, fast pacing when excited that freezes on real feelings; slightly chaotic, off-the-cuff
  delivery, not a performed role.*

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
