# Releasing

DeepPrint publishes to Maven Central through the
[Central Portal](https://central.sonatype.com). The previous setup published through
`s01.oss.sonatype.org`, which stopped working when OSSRH reached end of life on
30 June 2025.

## One-time setup

1. **Claim the namespace.** Sign in to the Central Portal
   (<https://central.sonatype.com>, note `.com`; `central.sonatype.org` is the
   documentation site) and confirm under Publishing Settings that
   `com.bradyaiello.deepprint` is listed and Verified. That is the whole group id every
   published module uses, so the namespace has to match it, not the shorter
   `com.bradyaiello`. Namespaces that had published through OSSRH were migrated, so it
   should already be there.

   Namespaces are tied to the sign-in method, not the email address. Signing in with
   GitHub and signing in with Google are separate accounts even for the same person, so
   if the namespace is missing, try the other method before registering anything.
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

## When signing fails

Signing runs before the upload, so a key problem hides any credentials problem behind
it. The message tells you which of the three secrets is at fault:

| Message | Meaning |
| --- | --- |
| `PGPException: checksum mismatch in checksum of 20 bytes` | The key decrypted with the wrong passphrase. `SIGNING_KEY_PASSWORD` does not match `SIGNING_SECRET_KEY`. |
| `Could not read PGP secret key` | `SIGNING_SECRET_KEY` is not a readable armoured private key -- a public key, a binary export, or truncated. |

Reproduce either locally without going through CI. This runs the same code path:

```bash
./gradlew :deep-print-annotations:signJvmPublication \
  -PsigningInMemoryKey="$(gpg --armor --export-secret-keys YOUR_KEY_ID)" \
  -PsigningInMemoryKeyPassword='your-passphrase'
```

Also check that the key has not expired. Central rejects signatures from an expired key,
and a two-year expiry is a common default.

## Checking it locally

`./gradlew publishToMavenLocal` publishes every module and target to `~/.m2`, which
exercises the whole pipeline apart from the upload. Signing is skipped unless a signing
key is configured, so this works without one.
