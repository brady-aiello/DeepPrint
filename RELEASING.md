# Releasing

DeepPrint publishes to Maven Central through the
[Central Portal](https://central.sonatype.com). The previous setup published through
`s01.oss.sonatype.org`, which stopped working when OSSRH reached end of life on
30 June 2025.

## One-time setup

1. **Claim the namespace.** Sign in to the Central Portal and confirm that
   `com.bradyaiello` is listed and verified. Namespaces that had published through
   OSSRH were migrated, so it should already be there.
2. **Create a user token.** Portal → your account → *Generate User Token*. This gives a
   username and password pair. They are not the account credentials, and they are not
   the old OSSRH ones.
3. **Have a GPG key.** Central requires signed artifacts. The public half must be on a
   public keyserver; the private half is what CI signs with. Export it armoured:

   ```bash
   gpg --armor --export-secret-keys <KEY_ID>
   ```

4. **Set the repository secrets** (Settings → Secrets and variables → Actions):

   | Secret | Contents | Required |
   | --- | --- | --- |
   | `MAVEN_CENTRAL_USERNAME` | Portal user token username | yes |
   | `MAVEN_CENTRAL_PASSWORD` | Portal user token password | yes |
   | `SIGNING_SECRET_KEY` | armoured private key, including the `-----BEGIN` and `-----END` lines | yes |
   | `SIGNING_KEY_ID` | the key's short id | only if the key has subkeys |
   | `SIGNING_KEY_PASSWORD` | the key's passphrase | only if the key has one |

   Leave the optional two unset rather than empty. The workflow unsets empty values
   before running Gradle, because an empty id or passphrase is not the same as an
   absent one.

## Cutting a release

1. Set the new version in `gradle.properties`. Maven Central rejects a version that is
   already published, so this has to change — `0.1.0-alpha10` has been on Central since
   June 2023.
2. Commit that on `main`.
3. Tag it and push the tag. Existing tags are unprefixed:

   ```bash
   git tag 0.2.0-alpha01
   git push origin 0.2.0-alpha01
   ```

The `Publish` workflow runs on tag pushes. It builds every target, signs, and uploads a
deployment to the Portal.

4. **Finish the release in the Portal.** The upload lands as a pending deployment for
   you to check and publish. To have CI release it without that step, change the Gradle
   invocation in `.github/workflows/publish.yml` from `publishToMavenCentral` to
   `publishAndReleaseToMavenCentral`.

## Checking it locally

`./gradlew publishToMavenLocal` publishes every module and target to `~/.m2`, which
exercises the whole pipeline apart from the upload. Signing is skipped unless a signing
key is configured, so this works without one.
