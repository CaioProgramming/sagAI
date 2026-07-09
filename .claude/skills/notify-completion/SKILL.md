---
name: notify-completion
description: Send Caio a short iMessage summary in Brazilian Portuguese (pt-BR) whenever a plan-mode implementation finishes, or a multi-step task list wraps up. Use this proactively at the end of substantial work — completing everything approved in a plan, finishing a tracked TaskCreate/TaskUpdate list, shipping a feature — even if Caio doesn't explicitly ask to be notified. Do not use for trivial one-line fixes, mid-task progress pings, or anything already fully visible in the current turn's own reply (he's reading that already).
---

# Notify completion via iMessage

Caio asked to get a text when a real chunk of work here finishes, so he doesn't have to
keep checking back in. This skill sends that text.

## When to send

- Right after finishing everything approved in a plan (post-`ExitPlanMode`, implementation
  done, build/verification done).
- Right after wrapping up a task list you were tracking with `TaskCreate`/`TaskUpdate`.
- At most once per finished unit of work — don't send one message per sub-task.

Skip it for: trivial single-step fixes, mid-task progress updates, or anything where the
user is clearly still actively watching this exact conversation in real time (the text
message would just be a redundant ping — same judgment call as `PushNotification`).

## How to send

Use `mcp__Read_and_Send_iMessages__send_imessage`:

- `recipient`: `+5511965766738`
- `message`: the summary, written in **pt-BR** — always, even if the conversation itself
  was in English or another language.

## Message format

This is a text message, not a report — keep it to 2-4 sentences, no markdown (iMessage
doesn't render it), no file-path dumps. Cover, in this order:

1. O que foi feito — o resultado prático, não a lista de arquivos alterados.
2. Qualquer coisa que precise da atenção ou decisão do Caio, se houver.
3. O próximo passo, se já estiver claro.

**Example:**
> Terminei o refactor do TaskShellContent pra cada shell ter seu próprio Background — o
> GlobalShellHost agora anima uma única superfície em vez de duas separadas. Build passou
> limpo, instalei no celular e não crashou. Falta só você conferir visualmente o overlay
> de objetivo do chat pra garantir que não quebrou nada.
