---
name: remote-config-version
description: Bump the Remote Config template's own version (`config_version`, like 1.18.3) every time a template is published to Firebase. Use whenever Remote Config is about to be deployed or promoted, from any session: editing params, publishing blueprints, running the promote script, a release's FAQ publish. The Settings screen shows the version the app is actually running on, so a stale cache is visible at a glance.
---

# Remote Config version

`config_version` is a string parameter on the template: the app's `MAJOR.MINOR` (from
`version.properties`) plus a patch that counts publishes, e.g. `1.18.3`. The patch resets to `1`
when the app moves to a new `MAJOR.MINOR` (a release or a new version line), and only then.

Why it exists: "last fetch time" in Settings says the fetch ran, not that the values in use are
the latest, because the app can still be on cached values. The Settings tile shows
`Config version: x.y.z` read from the **activated** values, so it is what the app is really using.
It reads "not published" until a template carries the parameter. There is no compiled fallback on
purpose (config is remote-only).

## Every publish

1. Build the template you are about to deploy (edited local JSON, or a fresh `remoteconfig:get`).
2. Bump it in place, against the project it is going to:

   ```bash
   .claude/skills/remote-config-version/scripts/bump-config-version.sh <project> <template.json>
   ```

   It prints `project: old -> new` and writes `config_version` into the file. Nothing is published
   by the script.
3. Deploy the template as usual (`firebase deploy --only remoteconfig --project <project>`).
4. Publishing to `sagai-ilustris` (prod) is outward-facing: confirm with Caio first, every time.

Projects: `sagai-hmg` (homologation), `sagai-ilustris` (prod).

## Promoting hmg to prod

`scripts/promote-remote-config.sh` copies the hmg template as it is, `config_version` included, so
prod ends up on hmg's version. Do not bump again afterwards; bump hmg when it is edited, then promote.

## Checking

`firebase remoteconfig:get --project <project> -o /tmp/x.json && jq -r '.parameters.config_version.defaultValue.value' /tmp/x.json`
compares the published value with what a device shows in Settings > Remote Config.
