---
name: notify-completion
description: Send Caio a short iMessage + push notification summary in Brazilian Portuguese (pt-BR) whenever a plan-mode implementation finishes, or a multi-step task list wraps up. Use this proactively at the end of substantial work — completing everything approved in a plan, finishing a tracked TaskCreate/TaskUpdate list, shipping a feature — even if Caio doesn't explicitly ask to be notified. Do not use for trivial one-line fixes, mid-task progress pings, or anything already fully visible in the current turn's own reply (he's reading that already).
---

# Notify completion via iMessage + push

Caio asked to get a text when a real chunk of work here finishes, so he doesn't have to
keep checking back in. iMessage alone sometimes doesn't ping his phone (iOS suppresses
notifications for messages sent to yourself), so this skill sends both an iMessage and a
`PushNotification` as backup.

## When to send

- Right after finishing everything approved in a plan (post-`ExitPlanMode`, implementation
  done, build/verification done).
- Right after wrapping up a task list you were tracking with `TaskCreate`/`TaskUpdate`.
- At most once per finished unit of work — don't send one message per sub-task.

Skip it for: trivial single-step fixes, mid-task progress updates, or anything where the
user is clearly still actively watching this exact conversation in real time (the text
message would just be a redundant ping — same judgment call as `PushNotification`).

## How to send

Send both, every time — iMessage alone doesn't reliably notify (self-sent messages get
suppressed by iOS), so `PushNotification` is the backup that actually reaches his phone.

1. `mcp__Read_and_Send_iMessages__send_imessage`:
   - `recipient`: `+5511965766738`
   - `message`: the full summary, written in **pt-BR** — always, even if the conversation
     itself was in English or another language. Follow the tone/format below.
2. `PushNotification` right after, with `status: "proactive"`:
   - `message`: a one-line, under-200-char version in the same casual pt-BR tone — not a
     generic "task done", give it the actual headline (e.g. `Refactor do TaskShellContent
     terminado, build passou de primeira.`).

## Message format

Isso é uma mensagem de um parceiro de dev, não um release note. Escreve como se tivesse
mandando um zap contando o que rolou pro cara que senta do seu lado — pode usar gíria, ser
irônico, tirar sarro de um bug idiota que você corrigiu, reclamar se a tarefa foi chata ou
comemorar se saiu fácil. Sem "Summary:", sem markdown (iMessage não renderiza), sem lista de
arquivos alterados. 2-4 frases, tom de conversa mesmo — não de relatório.

Cobre isso, mas sem parecer um checklist:

1. O que rolou — o resultado prático, contado como quem tava lá.
2. Qualquer coisa que precise da atenção ou decisão do Caio, se houver.
3. O próximo passo, se já estiver claro.

**Exemplos (o tom importa mais que o texto exato):**
> Terminei o refactor do TaskShellContent — cada shell agora tem o próprio Background em
> vez de brigar por um só. Build passou de primeira, o que já é suspeito. Instalei no
> celular, não crashou. Só falta você dar uma olhada no overlay de objetivo do chat, não
> confio 100% nele ainda.

> Resolvi aquele bug do bitmap de hardware que tava quebrando o crop de foto de perfil —
> óbvio demais depois que achei, tipo sempre. Sem pendência dessa vez, pode testar quando
> quiser.
