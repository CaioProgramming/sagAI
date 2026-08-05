# Feature Idea: Multi-Provider AI Request Routing (moved to sagas-kmp)

## Status

This idea has moved to the KMP monorepo. `sagAI` is no longer the source of truth for AI client
code — the Gemini client already lives in `shared/src/commonMain/kotlin/com/ilustris/sagai/shared/ai/GemmaClient.kt`
in `CaioProgramming/sagas-kmp` (see that repo's `KMP_MIGRATION.md`). Building the provider chain here
would mean redoing it once `android/` is fully on `shared`.

**Full plan**: `CaioProgramming/sagas-kmp`, branch `claude/multi-provider-request-optimization-3v1kku`,
`android/docs/feature_planning/multi_provider_ai_routing/task.md` (also listed as item #23 in
`android/docs/feature_planning/roadmap.md`).

## One-line summary

Put free-tier OpenAI-compatible providers (Groq first) in front of Gemini as a waterfall — try the
fast/free provider(s), fall back to Gemini on error or when disabled — with every provider toggled
independently via Remote Config. Normalizes on the OpenAI-compatible `chat/completions` response
shape (one adapter covers Groq + future providers); Gemini keeps its own adapter and stays the
guaranteed, always-enabled last hop.

## Why here and not implemented directly

Explicitly parked until iOS finishes moving off its native `GemmaClient.swift` onto `shared`'s
`GemmaClient` (per `sagas-kmp`'s `KMP_MIGRATION.md`, "D1 iOS Room" phase). Once that's done, this
lands once in `shared/` for both platforms instead of twice.
