# Release Checklist

- [ ] Confirm application ID `com.indianservers.iqlabs`.
- [ ] Bump `versionCode` and `versionName` before store upload.
- [x] Run unit tests, lint, debug build, release APK and release AAB.
- [ ] Configure signing through local Gradle properties or environment variables only.
- [ ] Preserve mapping files for every release.
- [x] Verify no keystore, password or service-account file is committed.
- [x] Review generated lint report.
- [ ] Perform clean install smoke test.
- [ ] Verify onboarding, catalogue, one game, progress, settings, challenge centre and backup parser paths.
- [ ] Review privacy/data-safety text with qualified counsel.
- [ ] Capture real screenshots from the app.
- [ ] Verify Arabic/RTL primary screens on device.
