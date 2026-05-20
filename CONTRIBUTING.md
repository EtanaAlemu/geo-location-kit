# Contributing to Geo Location Kit

Thank you for your interest in contributing. This project is an Android multi-module library backed by the [dr5hn countries-states-cities-database](https://github.com/dr5hn/countries-states-cities-database).

## Code of conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to contribute

- **Bug reports** — reproducible steps, Android version, module used (`geo-compose`, `geo-database`, or `geo-database-lite`)
- **Feature requests** — describe the use case and which module should own the change
- **Pull requests** — fixes, tests, docs, or schema/API improvements
- **Data issues** — upstream dr5hn dataset problems should be reported there first; we regenerate assets from their releases

## Development setup

**Requirements:** JDK 17, Android SDK, stable network for the first full build.

```bash
git clone https://github.com/EtanaAlemu/geo-location-kit.git
cd geo-location-kit
./gradlew :app:assembleDebug
```

| Asset | In Git? | Notes |
|-------|---------|--------|
| `location-lite.db` | Yes | Lite module works immediately |
| `location.db` | No | Generated on first `:geo-database` build (~110 MB); cached under `geo-db-builder/build/dr5hn/` |

Force a full DB rebuild after schema or dr5hn changes:

```bash
./gradlew generateLocationDatabase -PforceDbGeneration
./gradlew generateLocationLiteDatabase -PforceDbGeneration
```

Optional dr5hn release pin:

```bash
./gradlew generateLocationDatabase -PforceDbGeneration -Pdr5hnReleaseTag=v3.2-export.2
```

## Running tests

Before opening a PR, run:

```bash
./gradlew :geo-core:test :geo-db-builder:test :geo-compose:test
```

Android instrumented tests (device or emulator):

```bash
./gradlew :geo-database:connectedDebugAndroidTest
./gradlew :geo-database-lite:connectedDebugAndroidTest
```

CI runs JVM/unit tests and `:app:assembleDebug` on every push to `main` (see [`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Pull request guidelines

1. **Branch** from `main` with a focused change (one concern per PR when possible).
2. **Describe** what changed, why, and how you tested it.
3. **Tests** — add or update unit tests for logic changes; migration tests for Room schema bumps.
4. **Schema changes** — update `DatabaseSchema`, builder, DAO, migrations, export JSON under `geo-database/schemas/`, and document in [MIGRATION.md](MIGRATION.md).
5. **Do not commit** `location.db`, `geo-db-builder/build/dr5hn/`, or local `**/bin/` outputs (see [.gitignore](.gitignore)).
6. **Attribution** — apps using geographic data must show ODbL attribution; do not remove `ATTRIBUTION.txt` or `GeoLocationKit.DATA_ATTRIBUTION`.

## Module map

| Module | Change here when… |
|--------|-------------------|
| `geo-core` | Public API, models, locale, picker config |
| `geo-database-common` | Shared entities, mappers, migrations SQL |
| `geo-database` | Full DB, repository, FTS, postcodes |
| `geo-database-lite` | Lite DB variant |
| `geo-compose` | Compose UI and `LocationFormViewModel` |
| `geo-db-builder` | Asset generation from dr5hn JSON |
| `app` | Demo only — not published as a library |

Avoid depending on both `geo-database` and `geo-database-lite` in the same app module.

## Code style

- Kotlin official style (`kotlin.code.style=official` in `gradle.properties`)
- Match existing naming and package layout (`com.etanaalemu.geo.*`)
- Prefer small, focused diffs over drive-by refactors
- Keep public API changes backward-compatible when possible, or document breaking changes in `MIGRATION.md` and release notes

## Releases and versioning

Library version is set in `gradle.properties` (`geo.version`, `geo.group`). Maven `groupId` is **`com.etanaalemu.geo`** (lowercase — required by [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)).

**Publish flow for maintainers:**

1. Bump `geo.version` in `gradle.properties` and merge to `main`.
2. Tag `vX.Y.Z` and push: `git tag v1.0.1 && git push origin v1.0.1`.
3. Create a [GitHub Release](https://github.com/EtanaAlemu/geo-location-kit/releases) from that tag — this runs **Publish to GitHub Packages** automatically.
4. Verify [Packages](https://github.com/EtanaAlemu/geo-location-kit/packages) and the [JitPack](https://jitpack.io/#EtanaAlemu/geo-location-kit) build for the tag.

**Local publish to GitHub Packages** (optional): add to `~/.gradle/gradle.properties` (never commit tokens):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=ghp_...   # classic PAT with write:packages (and read:packages)
```

```bash
./gradlew publish -Pgeo.version=1.0.0-SNAPSHOT -x test
```

Or trigger **Publish to GitHub Packages** manually under Actions → workflow_dispatch.

## Questions

Open a [GitHub Discussion or Issue](https://github.com/EtanaAlemu/geo-location-kit/issues) if something is unclear before starting large changes.

## License

By contributing, you agree that your contributions will be licensed under the same [MIT License](LICENSE) as the project code. Geographic data remains under [ODbL v1.0](https://opendatacommons.org/licenses/odbl/) from dr5hn — see [README — Attribution](README.md#attribution).
