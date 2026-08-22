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

## 2. GitHub Actions CI/CD Integration (Automatic Keystore Generation)

The GitHub Actions workflows (`build.yml` and `release-tag.yml`) **automatically generate** all required keystores (`debug.keystore` and `my-upload-key.jks`) dynamically if they do not exist:
- **Zero-config builds**: Fresh repositories run and produce signed Debug and Release APK artifacts without requiring any setup or secrets.
- **Custom production signing (Optional)**: If you provide GitHub repository secrets, the workflow uses your official production key instead:

| Secret Name (Optional) | Value Description |
|---|---|
| `KEYSTORE_BASE64` | Base64 encoded contents of your custom `.jks` (`base64 -w 0 my-upload-key.jks`) |
| `STORE_PASSWORD` | Keystore password (defaults to `android`) |
| `KEY_PASSWORD` | Private key password (defaults to `android`) |
| `KEY_ALIAS` | Key alias (defaults to `upload`) |

If no secrets are supplied, the workflow automatically uses `keytool` on the runner to generate standard 2048-bit RSA keys with 10,000 days validity and signs both debug and release APKs.

---

## 3. Zepp OS Watch Developer Signature Keys (Amazfit Bip Max)

Zepp OS requires developer certificates for sideloading `.zab` binaries onto smartwatches over BLE:
- **Developer Key**: `keys/developer.key` (RSA 2048-bit private key)
- **Developer Certificate**: `keys/developer.cert` (X.509 signature)

### Zeus CLI Commands:
- `zeus cert` – Inspect current developer signature and device bindings.
- `zeus cert --generate` – Generate a new Zepp OS signature keypair and Android upload keystore.
- `zeus sign` – Sign the current active project into a production-ready `.zab` for Bip Max.
