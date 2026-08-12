# Saga Wrapped — Renderização Temática (Signature)

## Origem

Achado de "Oportunidade de Premium" do `product_agent.md`: já que o Saga Wrapped é intencionalmente
free (compartilhamento orgânico), a ideia cogitada para o Signature é uma versão "refinada" da
retrospectiva com renderização específica por gênero — ex: um terminal de computador pro cyberpunk,
a leitura de um livro pro fantasy.

## Objetivo

Dar aos assinantes uma versão visualmente mais rica e específica do gênero do Wrapped, para
compartilhar — mantendo a versão atual (genérica, animada in-app) 100% free.

## O que já existe (reaproveitável)

- `ReviewExperience`/`DefaultReviewExperience` já monta o Wrapped em páginas (Intro, Vibe, Playstyle,
  Squad, Journey, Conclusion) com animações Compose (`PopIn`, `Typewriter`, `ReactiveShimmer`,
  linework dinâmico) — a estrutura de conteúdo e o texto gerado por IA não precisam mudar.
- `features/share/*` já resolve compartilhamento de **imagem estática**: captura de bitmap por
  página (`CharacterShareView`, `PlayStyleShareView`, `GTAStyleCover` etc.) + `ShareSheet` +
  `Intent.ACTION_SEND` com `type = "image/*"` (`ShareUtils.kt`).
- `core/media/MediaPlayerManagerImpl` / `SoundFxService` já tocam trilha sonora por gênero durante o
  jogo — os assets de música temática por gênero já existem e poderiam ser reaproveitados como trilha
  de um export.
- O sistema de tema por gênero (`Genre` enum, cores, fontes, efeitos) já é usado em toda a UI, então
  a "casca visual" por gênero (terminal/livro/etc.) tem onde se apoiar em termos de design system.

## O que falta

- **Não existe nenhuma capacidade de vídeo no projeto hoje.** Busquei por Media3, MediaMuxer,
  MediaCodec, ExoPlayer nas deps (`gradle/libs.versions.toml`, `app/build.gradle.kts`) e no código —
  nada encontrado. O compartilhamento atual é só imagem.
- Construir isso do zero exige: captura de frames da UI Compose animada (ex: via `GraphicsLayer` /
  `drawToBitmap` em loop), composição de vídeo (Media3 Transformer ou MediaCodec/MediaMuxer manual),
  e mixagem de áudio (a trilha do gênero) sincronizada com a duração do vídeo.
- Os templates visuais específicos por gênero (terminal, livro) são telas novas, não uma variação de
  estilo das páginas atuais — é UI nova por gênero, não um reskin simples.
- Processamento de vídeo no device tem custo de performance/bateria e tempo de espera que precisa ser
  desenhado na experiência (loading, feedback de progresso).

## Riscos técnicos

- Maior risco: nenhuma base de vídeo existente significa que a estimativa de esforço é sensível a uma
  decisão de arquitetura (Media3 Transformer vs. solução manual) que ainda não foi tomada — isso muda
  o escopo real bastante.
- Performance em devices mid-range/baixos pode tornar a geração do vídeo lenta ou instável; precisa
  de teste em hardware real antes de prometer isso como feature.
- Cada template por gênero (terminal, livro, etc.) multiplica o trabalho de design + implementação —
  não é "uma feature", é "N features" (uma por gênero suportado).

## Esforço estimado

**Alto.** Não é uma extensão do que já existe (a captura de vídeo é uma capacidade nova do zero), e o
escopo cresce por gênero suportado (N templates visuais, não um só).

## Recomendação

**Precisa de spike técnico antes de qualquer decisão de priorizar.** Sugestão de spike: validar em 1
dispositivo mid-range se dá pra gerar um vídeo curto (15-20s) a partir de uma tela Compose animada
com áudio sincronizado, usando Media3 Transformer, para 1 gênero apenas (prova de conceito) antes de
comprometer com N templates. Só depois desse spike faz sentido o marketing colocar isso no
cronograma.
