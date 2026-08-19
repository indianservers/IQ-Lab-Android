# IQ Lab Phase 5 Status

## Audit

- Manifest has no explicit permissions and already sets `supportsRtl="true"`.
- Existing app has 150 playable catalogue entries through executable tests.
- Release minification/resource shrinking was disabled and has been enabled.
- No Room schema exists, so historical Room migration testing is not applicable.
- No signing credentials are present or committed.

## Completed

- [x] Phase 5 docs created.
- [x] Release R8/resource shrinking configured.
- [x] Versioned backup parser/exporter added.
- [x] Localisation scaffolding started.
- [x] Privacy, QA and store documentation created.
- [x] Unit tests passed after Phase 5 changes.
- [x] Lint passed.
- [x] Debug build passed.
- [x] Release build passed.
- [x] Bundle build passed.

## Validation Evidence

- `.\gradlew.bat :app:testDebugUnitTest` passed.
- `.\gradlew.bat :app:lintDebug` passed.
- `.\gradlew.bat :app:assembleDebug :app:assembleRelease :app:bundleRelease` passed.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`.
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`.
- R8 mapping: `app/build/outputs/mapping/release/mapping.txt`.

## Limitations

- Full manual device QA, screenshots, app icon redesign and production signing require human/device work.
- Compose UI tests and instrumented tests are not yet implemented in this compact codebase.
- Release APK is unsigned; production signing must be configured outside git.
