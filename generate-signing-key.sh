#!/usr/bin/env bash
# ==============================================================================
# Zeus CLI / Android IDE - Signing Key Generator
# Generates Android Release Keystore & Zepp OS Watch Developer Signatures
# ==============================================================================

set -e

KEYSTORE_NAME="my-upload-key.jks"
KEY_ALIAS="upload"
KEY_ALG="RSA"
KEY_SIZE="2048"
VALIDITY_DAYS="10000"
STORE_PASS="${STORE_PASSWORD:-android}"
KEY_PASS="${KEY_PASSWORD:-android}"

echo "============================================================"
echo "⚡ Zeus IDE & Android Release Signing Key Generator"
echo "============================================================"

# 1. Android Release Keystore
if [ -f "$KEYSTORE_NAME" ]; then
    echo "⚠️  Keystore '$KEYSTORE_NAME' already exists in workspace."
else
    echo "🔑 Generating Android Release Keystore: $KEYSTORE_NAME (Alias: $KEY_ALIAS)..."
    keytool -genkeypair \
        -v \
        -keystore "$KEYSTORE_NAME" \
        -alias "$KEY_ALIAS" \
        -keyalg "$KEY_ALG" \
        -keysize "$KEY_SIZE" \
        -validity "$VALIDITY_DAYS" \
        -storepass "$STORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Zeus Developer, OU=Mobile, O=Zeus, L=San Francisco, ST=CA, C=US"
    echo "✓ Generated $KEYSTORE_NAME successfully."
fi

# 2. Zepp OS Watch Developer Signature Keys
mkdir -p keys
if [ ! -f "keys/developer.key" ]; then
    echo "⌚ Generating Zepp OS Developer Private Key (keys/developer.key)..."
    openssl genrsa -out keys/developer.key 2048 2>/dev/null || true
fi

if [ ! -f "keys/developer.cert" ] && [ -f "keys/developer.key" ]; then
    echo "📜 Generating Zepp OS Developer Certificate (keys/developer.cert)..."
    openssl req -new -x509 -key keys/developer.key -out keys/developer.cert -days 3650 \
        -subj "/C=US/ST=CA/L=San Francisco/O=Zeus/OU=ZeppOS/CN=Zeus-BipMax-Dev" 2>/dev/null || true
fi

echo ""
echo "============================================================"
echo "📋 GitHub Actions CI/CD Configuration"
echo "============================================================"
echo "To sign Release APKs in GitHub Actions:"
echo "1. Go to your GitHub Repository -> Settings -> Secrets and variables -> Actions"
echo "2. Add the following repository secrets:"
echo "   • KEYSTORE_BASE64: Copy the base64 string below"
echo "   • STORE_PASSWORD:  $STORE_PASS"
echo "   • KEY_PASSWORD:    $KEY_PASS"
echo "   • KEY_ALIAS:       $KEY_ALIAS"
echo ""
echo "--- KEYSTORE_BASE64 (Copy everything below this line) ---"
if [ -f "$KEYSTORE_NAME" ]; then
    base64 "$KEYSTORE_NAME" | tr -d '\n'
    echo ""
elif [ -f "debug.keystore.base64" ]; then
    cat debug.keystore.base64
    echo ""
fi
echo "--- END KEYSTORE_BASE64 ---"
echo ""
echo "🎉 Keystore setup complete!"
