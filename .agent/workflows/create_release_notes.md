---
description: Create release notes for Google Play Store
---

**Use the project skill:** `.cursor/skills/google-play-release-notes/SKILL.md` (tone: enxuto, sarcasmo, petulância — sem detalhe técnico).

1. **Context**
   - `version.properties` → versão
   - `git log -n 15 --oneline` na branch de release (ou diff vs `main`)
   - Um tema por release, não lista de commits

2. **Write**
   - `docs/release_notes/release_[version].md`
   - Só blocos 🇺🇸 English e 🇧🇷 Português (2–4 frases cada; ~500 chars se for colar na Play)
   - `git add -f docs/release_notes/release_[version].md`

3. **Handoff**
   - Lembrar: colar na Play Console
   - Não publicar nada automaticamente

Referência de tom: `release_1.10.2.md`, `release_1.10.4.md`.
