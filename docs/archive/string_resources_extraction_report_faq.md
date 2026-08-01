# String Extraction Report

## Summary

Successfully extracted **10** hardcoded strings from UI components and created localized resources
for English and Portuguese (pt-BR).

## Files Modified

- `app/src/main/java/com/ilustris/sagai/features/faq/ui/FAQView.kt`
- `app/src/main/java/com/ilustris/sagai/features/settings/ui/SettingsView.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-pt-rBR/strings.xml`

## Extracted Resources

| Key                             | English                                   | Portuguese                                  |
|---------------------------------|-------------------------------------------|---------------------------------------------|
| `faq_loading_message`           | Searching for information...              | Buscando informações...                     |
| `faq_ai_loading_message`        | Consulting the Saga Master...             | Consultando o Mestre da Saga...             |
| `faq_ai_reply_button`           | Got it!                                   | Entendido!                                  |
| `faq_title`                     | FAQ                                       | FAQ                                         |
| `faq_search_placeholder`        | Search questions...                       | Pesquisar dúvidas...                        |
| `faq_empty_state_title`         | We didn't find what you're looking for... | Não encontramos o que você procura...       |
| `faq_ask_ai_button`             | Ask the Saga Master                       | Perguntar ao Mestre da Saga                 |
| `faq_ask_ai_footer`             | Still unsure? Ask the Saga Master         | Ainda em dúvida? Pergunte ao Mestre da Saga |
| `settings_help_center_title`    | Help Center                               | Central de Ajuda                            |
| `settings_help_center_subtitle` | Tips, Tricks & Secrets.                   | Dicas, Truques e Segredos.                  |

## Validation

- Project compiled successfully (`./gradlew assembleDebug`).
- No regressions detected in string usage.
