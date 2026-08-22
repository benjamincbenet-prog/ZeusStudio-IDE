# 🔑 Signing Keys Guide: Android & Zepp OS (Amazfit Bip Max)

This repository supports complete cryptographically signed builds for both:
1. **Android APKs** (Debug and Release `.apk` via Gradle)
2. **Zepp OS Smartwatch Packages** (`.zab` packages via Zeus CLI for Amazfit Bip Max)

---

## 1. Android Release Keystore (`my-upload-key.jks`)

The Android build configuration in `app/build.gradle.kts` looks for a release keystore at:
- Path: `my-upload-key.jks` (or specified via `KEYSTORE_PATH` env var)
- Key Alias: `upload`
- Passwords: Read from `STORE_PASSWORD` and `KEY_PASSWORD` env vars.

### Quick Setup:
Run the included generator script:
```bash
bash generate-signing-key.sh
```

### Manual Key Generation via `keytool`:
```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your_store_password \
  -keypass your_key_password \
  -dname "CN=Zeus Developer, OU=Mobile, O=Zeus, L=San Francisco, ST=CA, C=US"
```

---

## 2. GitHub Actions CI/CD Integration

To automatically sign Release APKs in GitHub Actions (`build.yml` & `release-tag.yml`), configure these repository secrets:

| Secret Name | Value Description |
|---|---|
| `KEYSTORE_BASE64` | Base64 encoded contents of `my-upload-key.jks` (`base64 -w 0 my-upload-key.jks`) |
| `STORE_PASSWORD` | Keystore password |
| `KEY_PASSWORD` | Private key password |
| `KEY_ALIAS` | Key alias (default: `upload`) |

The CI/CD workflow automatically decodes `KEYSTORE_BASE64` into `my-upload-key.jks` during execution. If no custom secret is provided, it safely signs with development keys.

---

## 3. Zepp OS Watch Developer Signature Keys (Amazfit Bip Max)

Zepp OS requires developer certificates for sideloading `.zab` binaries onto smartwatches over BLE:
- **Developer Key**: `keys/developer.key` (RSA 2048-bit private key)
- **Developer Certificate**: `keys/developer.cert` (X.509 signature)

### Zeus CLI Commands:
- `zeus cert` – Inspect current developer signature and device bindings.
- `zeus cert --generate` – Generate a new Zepp OS signature keypair and Android upload keystore.
- `zeus sign` – Sign the current active project into a production-ready `.zab` for Bip Max.
