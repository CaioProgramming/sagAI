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

3. **Scope boundary**
   - Estas notas dizem **o que mudou**, para quem já joga. Elas nunca tocam o `README.md`.
   - Se a release mudou **o que o app é** (e não só o que ele ganhou), isso é o passo 10 do
     `.agent/workflows/create_release.md` — README, outra voz, outro julgamento.

4. **Handoff**
   - Lembrar: colar na Play Console
   - Não publicar nada automaticamente

Referência de tom: `release_1.10.2.md`, `release_1.10.4.md`.
