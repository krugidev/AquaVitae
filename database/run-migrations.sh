#!/usr/bin/env bash
# Corre os scripts SQL (ddl + seed) dentro do container Oracle XE, por ordem.
# Uso: ./run-migrations.sh
set -euo pipefail

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Falta o ficheiro .env (copia .env.example para .env primeiro)." >&2
  exit 1
fi

set -a
source .env
set +a

CONTAINER="aquavitae-oracle-xe"
CONNECT_STRING="${ORACLE_APP_USER}/${ORACLE_APP_PASSWORD}@//localhost:1521/XEPDB1"

SCRIPTS=(
  "ddl/01_tables.sql"
  "ddl/02_constraints.sql"
  "ddl/03_triggers.sql"
  "seed/01_lookups.sql"
  "seed/02_bebidas.sql"
)

for script in "${SCRIPTS[@]}"; do
  echo "==> A correr ${script} ..."
  # NLS_LANG diz ao sqlplus que os bytes recebidos via stdin já são UTF-8 (a BD é
  # AL32UTF8) — sem isto, acentos ficam corrompidos (mojibake) nos INSERTs de texto
  # com "ã", "ç", "é", etc. Apanhado e corrigido em 2026-09-17 (ver PLANO.md).
  docker exec -i -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 "$CONTAINER" sqlplus -s "$CONNECT_STRING" < "$script"
done

echo "Concluído. Schema + seed aplicados em XEPDB1."
