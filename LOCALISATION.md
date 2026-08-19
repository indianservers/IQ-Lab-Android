# Localisation

## Supported Locale Architecture

Resource folders are scaffolded for:

- English source locale
- Hindi
- Telugu
- Tamil
- Arabic
- Spanish
- French
- German

The current Compose implementation still contains hardcoded English strings and needs a deeper extraction pass before release. Arabic RTL is enabled at the manifest level through `supportsRtl`, but device-level RTL visual QA remains required.

## Content Policy

Math, visual, spatial and reaction games can be numerically localised. Reading, rhyme, analogy and verbal games need locale-specific validated content packs rather than mechanical translation.
