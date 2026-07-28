#!/usr/bin/env bash
set -euo pipefail

# Re-encrypt all S3 objects under a prefix to SSE-S3 (AES256) in-place.
# Usage:
#   ./scripts/convert_disease_images_to_aes256.sh [bucket] [prefix]
# Example:
#   ./scripts/convert_disease_images_to_aes256.sh your-bucket-name uploads/diseases/

BUCKET="${1:-${AWS_S3_BUCKET:?bucket name required as $1 or AWS_S3_BUCKET env var}}"
PREFIX="${2:-uploads/diseases/}"

if ! command -v aws >/dev/null 2>&1; then
  echo "aws CLI is required but not found." >&2
  exit 1
fi

echo "Starting conversion to SSE-S3(AES256)"
echo "Bucket: $BUCKET"
echo "Prefix: $PREFIX"

TOKEN=""
COUNT=0

while :; do
  if [ -n "$TOKEN" ]; then
    RESP="$(aws s3api list-objects-v2 --bucket "$BUCKET" --prefix "$PREFIX" --continuation-token "$TOKEN")"
  else
    RESP="$(aws s3api list-objects-v2 --bucket "$BUCKET" --prefix "$PREFIX")"
  fi

  KEYS="$(
    echo "$RESP" | python3 -c '
import json,sys
d=json.load(sys.stdin)
for o in d.get("Contents", []):
    print(o["Key"])
'
  )"

  if [ -n "$KEYS" ]; then
    while IFS= read -r KEY; do
      [ -z "$KEY" ] && continue
      aws s3api copy-object \
        --bucket "$BUCKET" \
        --key "$KEY" \
        --copy-source "$BUCKET/$KEY" \
        --metadata-directive COPY \
        --server-side-encryption AES256 >/dev/null
      COUNT=$((COUNT + 1))
      echo "converted ($COUNT): $KEY"
    done <<< "$KEYS"
  fi

  TOKEN="$(
    echo "$RESP" | python3 -c '
import json,sys
d=json.load(sys.stdin)
print(d.get("NextContinuationToken",""))
'
  )"

  [ -z "$TOKEN" ] && break
done

echo "Done. Converted $COUNT objects to SSE-S3(AES256)."
