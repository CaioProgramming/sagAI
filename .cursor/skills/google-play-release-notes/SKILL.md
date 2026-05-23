---
name: google-play-release-notes
description: >-
  Write Sagas Google Play release notes in a short, sarcastic, petulant voice.
  Use when the user asks for release notes, Play Console copy, what's new text,
  or docs/release_notes for sagas-android.
---

# Google Play release notes (Sagas)

## Regra de ouro

O usuário da Play Store **não quer changelog técnico**. Quer 3–5 frases com personalidade: benefício claro, humor seco, um toque de sarcasmo e petulância (como se o app soubesse que é bom e está fazendo um favor).

**Nunca** na cópia da Play:
- nomes de classes, flags, APIs, "auto-chain", mutex, ViewModel
- listas longas de bullet com emoji demais
- tom de documentação ou marketing corporativo

## Onde salvar

`docs/release_notes/release_[MAJOR.MINOR.PATCH].md` — versão em `version.properties`.

Use `git add -f` (a pasta `docs/` está no `.gitignore`).

## Formato do arquivo

```markdown
✦ Sagas X.Y.Z — Google Play

🇺🇸 English

[2–4 frases, uma ideia por parágrafo curto. Máx ~500 caracteres se for bloco único para Console.]

🇧🇷 Português

[Mesma energia em PT-BR natural, não tradução literal fria.]
```

**Opcional** (só se o usuário pedir arquivo interno / Firebase): seção `---` + notas longas para arquivo. Por padrão **não** escrever bloco "Full notes / archive" — manter enxuto.

## Voz (obrigatória)

| Faça | Evite |
|------|--------|
| Segunda pessoa ("você") ou "a gente" | "Implementamos", "Refatoramos" |
| Uma piada ou reviravolta por idioma | Explicar *como* o código funciona |
| Confiança meio arrogante, leve | Entusiasmo de startup ("incrível!!!") |
| Frases curtas | Parágrafos de 6 linhas |

**Referência de tom:** `docs/release_notes/release_1.10.2.md`, `release_1.10.4.md`.

### Exemplos de linha

- ✅ "Seu celular virou estéreo sem pedir licença. Agora toca uma vez, nos momentos que importam."
- ✅ "Chega de segurar o botão duas vezes só pra abrir um ato. O app faz a papelada; você fica com o drama."
- ❌ "Hybrid in-app banner and system notifications for saga activity."
- ❌ "Auto-executamos NarrativeAction.CreateAct via requestNarrativeProgression."

## Fluxo

1. Ler `version.properties` e `git log` da branch de release (15 commits ou diff vs `main`).
2. Agrupar mudanças em **1 tema** para o usuário (ex.: "menos clique", "notificações", "chat menos bugado").
3. Escrever EN + PT no formato acima; contar caracteres se for colar na Play (limite 500 por idioma).
4. Entregar o arquivo e lembrar: copiar para Play Console → Release notes.

## Checklist antes de entregar

- [ ] ≤500 caracteres por idioma (se for bloco único Play)
- [ ] Zero jargão de engenharia
- [ ] PT soa brasileiro e espirituoso, não traduzido
- [ ] Algo soa um pouco petulante ou sarcástico (sem ser ofensivo)
