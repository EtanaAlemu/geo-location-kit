# Publishing to Maven Central

Geo Location Kit can be consumed from **Maven Central** (no GitHub token required):

```kotlin
// settings.gradle.kts — mavenCentral() is enough
implementation("io.github.etanaalemu:geo-compose:1.0.0")
```

**Published Maven coordinates** use `groupId` **`io.github.etanaalemu`** (verified namespace on Central Portal). **Kotlin/Android code** still uses packages `com.etanaalemu.geo.*` — that is normal and unrelated to the Maven groupId.

This guide is for **maintainers** publishing new versions.

## One-time setup

### 1. Namespace (done)

Namespace **`io.github.etanaalemu`** should show **Verified** on [central.sonatype.com](https://central.sonatype.com/) (GitHub account verification — no DNS TXT record).

`geo.group` in `gradle.properties` must stay `io.github.etanaalemu`.

### 2. Create a Portal user token

[Generate a token](https://central.sonatype.org/publish/generate-portal-token/) (not your GitHub password). The modal shows a **Username** and **Password** — use those exactly (not your GitHub login).

**Local** — `~/.gradle/gradle.properties` (never commit):

```properties
mavenCentralUsername=YOUR_PORTAL_USERNAME
mavenCentralPassword=YOUR_PORTAL_PASSWORD
```

**CI** — GitHub repo secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` (same values).

### 3. GPG signing key

Maven Central requires signed artifacts.

```bash
gpg --full-generate-key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --export-secret-keys --armor YOUR_KEY_ID   # for CI — keep secret
```

### 4. GitHub Actions secrets

In the repo **Settings → Secrets and variables → Actions**, add:

| Secret | Value |
|--------|--------|
| `MAVEN_CENTRAL_USERNAME` | Portal token username |
| `MAVEN_CENTRAL_PASSWORD` | Portal token password |
| `SIGNING_KEY` | Full armored output of `gpg --export-secret-keys --armor` |
| `SIGNING_PASSWORD` | GPG key passphrase (if any) |

GitHub Packages publishing still uses `GITHUB_TOKEN` in the release workflow.

### 5. Local `~/.gradle/gradle.properties` (optional)

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----...
signingInMemoryKeyPassword=...
```

## Publish a release

1. Bump `geo.version` in `gradle.properties`.
2. Tag and create a [GitHub Release](https://github.com/EtanaAlemu/geo-location-kit/releases) (e.g. `v1.0.1`).
3. The **Publish packages** workflow runs:
   - `publishAndReleaseToMavenCentral` → Maven Central (`io.github.etanaalemu:*`)
   - `publish` → GitHub Packages
4. Maven Central can take **10–30 minutes** after the workflow succeeds before artifacts appear on [search.maven.org](https://search.maven.org/).

### Manual publish

```bash
./gradlew publishAndReleaseToMavenCentral publish -Pgeo.version=1.0.0 -x test
```

Snapshots (no signing required):

```bash
# Set geo.version=1.0.1-SNAPSHOT in gradle.properties first
./gradlew publishToMavenCentral -x test
```

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Namespace not verified | Link GitHub on [Central Portal](https://central.sonatype.com/) → Namespaces |
| 401 on publish | Regenerate portal token; check secret names |
| Signing failed | Ensure `SIGNING_KEY` includes full `BEGIN`/`END` lines |
| Large AAR / timeout | CI timeout is 90m; full DB is built during publish |
| Old `com.etanaalemu.geo` coordinates | Republish under `io.github.etanaalemu` (e.g. `1.0.1+`) |

## Related

- [README — Installation](README.md#installation)
- [CONTRIBUTING.md](CONTRIBUTING.md#releases-and-versioning)
- [Vanniktech Maven Publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
