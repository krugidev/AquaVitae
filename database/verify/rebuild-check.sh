#!/usr/bin/env bash
# Verifica que a BD se reconstrói do zero e que fica IGUAL à de dev.
#
# Cria um schema temporário (aq_rebuild), corre nele os mesmos 5 scripts do run-migrations (01_tables, 02_constraints,
# 03_triggers, seed/01_lookups, seed/02_bebidas), extrai a estrutura e o conteúdo dos dois schemas (extrair.sql) e compara.
# O utilizador temporário é sempre apagado no fim. Serve para apanhar o que só rebenta (ou fica errado em silêncio) a quem
# reconstrói do zero — p.ex. a "Quinta de Soalheiro" que ficou sem região quando "Vinho Verde" passou a "Minho".
#
# Uso (Git Bash):   database/verify/rebuild-check.sh
# Requer: contentor aquavitae-oracle-xe a correr e saudável; database/.env (ORACLE_SYS_PASSWORD, ORACLE_APP_USER).
# Sai com 0 se está tudo igual, 1 se houver diferenças que importam, 2 se não conseguiu correr.
# Nota: a password do SYS vai na linha de comando do `docker exec` (só nesta máquina, durante alguns segundos).
#
# Regras da comparação
#   * Estrutura (tabelas, colunas, constraints, triggers) e alinhamento pais/produtor_pais: têm de ser IDÊNTICAS.
#   * Regiões e produtor->região: o que só existe na RECONSTRUÇÃO é problema (o seed diz uma coisa e a dev outra); o que só
#     existe na dev é informação (dados acrescentados à mão depois do seed, p.ex. os lotes da Awin).
#   * Conteúdo de texto por tabela (nº de linhas + hash; os ids não entram): só conta nas tabelas que o seed preenche. Se a
#     dev tiver MAIS linhas é informação (contas de utilizadores, lotes); o mesmo nº de linhas com hash diferente, ou menos
#     linhas na dev, é problema.
# Limite: numa tabela em que a dev já tem linhas a mais (ex.: bebida depois de um lote), um erro numa linha do seed não se
# vê pelo hash — só nas verificações por linha (regiões e produtor->região).
set -u

DB="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HERE="$DB/verify"
export MSYS_NO_PATHCONV=1   # Git Bash: não converter caminhos como /tmp/... nos argumentos do docker

[ -f "$DB/.env" ] || { echo "Falta $DB/.env (copia .env.example para .env)."; exit 2; }
set -a; . <(tr -d '\r' < "$DB/.env"); set +a
: "${ORACLE_SYS_PASSWORD:?falta ORACLE_SYS_PASSWORD no .env}" "${ORACLE_APP_USER:?falta ORACLE_APP_USER no .env}"

CONTAINER=aquavitae-oracle-xe
DEV_OWNER="$(echo "$ORACLE_APP_USER" | tr '[:lower:]' '[:upper:]')"
TMP_OWNER=AQ_REBUILD
OUT="$(mktemp -d)"
PW="Aq$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 14)9x"
SYSCONN="sys/$ORACLE_SYS_PASSWORD@//localhost:1521/XEPDB1 as sysdba"
RBCONN="aq_rebuild/$PW@//localhost:1521/XEPDB1"
problemas=0

dk() { docker exec -i -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 "$CONTAINER" "$@"; }
winpath() { if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1"; else echo "$1"; fi; }

cleanup() {
  dk sqlplus -s "$SYSCONN" > "$OUT/drop.log" 2>&1 <<'EOSQL'
DROP USER aq_rebuild CASCADE;
EXIT
EOSQL
  local restam
  restam=$(dk sqlplus -s "$SYSCONN" 2>&1 <<'EOSQL' | tr -dc '0-9'
SET PAGESIZE 0 FEEDBACK OFF HEADING OFF
SELECT COUNT(*) FROM dba_users WHERE username = 'AQ_REBUILD';
EXIT
EOSQL
)
  echo "schema temporário aq_rebuild apagado (restam: ${restam:-?})"
  if [ "$problemas" -eq 0 ]; then rm -rf "$OUT"; else echo "logs e extrações em: $OUT"; fi
}
trap cleanup EXIT

docker ps --format '{{.Names}}' | grep -qx "$CONTAINER" || { echo "O contentor $CONTAINER não está a correr (docker compose up -d em database/)."; problemas=2; exit 2; }

echo "### 1) criar o schema temporário aq_rebuild"
dk sqlplus -s "$SYSCONN" > "$OUT/create.log" 2>&1 <<EOSQL
BEGIN EXECUTE IMMEDIATE 'DROP USER aq_rebuild CASCADE'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
WHENEVER SQLERROR EXIT FAILURE
CREATE USER aq_rebuild IDENTIFIED BY "$PW" DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE TO aq_rebuild;
EXIT
EOSQL
if [ $? -ne 0 ] || grep -q -E 'ORA-|SP2-' "$OUT/create.log"; then echo "  não consegui criar o schema temporário:"; cat "$OUT/create.log"; problemas=2; exit 2; fi
echo "  ok"

echo "### 2) correr os 5 scripts do run-migrations no schema temporário"
scripts_ok=1
for f in ddl/01_tables.sql ddl/02_constraints.sql ddl/03_triggers.sql seed/01_lookups.sql seed/02_bebidas.sql; do
  log="$OUT/$(basename "$f").log"
  dk sqlplus -s "$RBCONN" < "$DB/$f" > "$log" 2>&1
  erros=$(grep -c -E 'ORA-|SP2-' "$log")
  if [ "$erros" -eq 0 ]; then printf '  %-24s ok\n' "$f"; else printf '  %-24s %s linha(s) com erro (ver %s)\n' "$f" "$erros" "$log"; scripts_ok=0; fi
done
if [ "$scripts_ok" -eq 0 ]; then echo "PROBLEMA: um script falha numa reconstrução do zero."; problemas=1; exit 1; fi

echo "### 3) extrair estrutura e conteúdo dos dois schemas"
for own in "$DEV_OWNER" "$TMP_OWNER"; do
  sed "s/__OWN__/$own/g" "$HERE/extrair.sql" > "$OUT/extrair_$own.sql"
  docker cp "$(winpath "$OUT/extrair_$own.sql")" "$CONTAINER:/tmp/extrair_$own.sql" > /dev/null
  dk sqlplus -s "$SYSCONN" @/tmp/extrair_$own.sql 2>&1 | grep -v '^$' > "$OUT/schema_$own.txt"
  printf '  %-12s %s linhas\n' "$own" "$(wc -l < "$OUT/schema_$own.txt")"
done
DEV="$OUT/schema_$DEV_OWNER.txt"; RB="$OUT/schema_$TMP_OWNER.txt"
grep -q -E '^ORA-|^ERROR' "$DEV" "$RB" && { echo "A extração falhou:"; grep -h -E '^ORA-|^ERROR' "$DEV" "$RB" | head; problemas=2; exit 2; }

echo "### 4) comparar (dev vs reconstrução do zero)"
# a) tem de ser idêntico
for k in T C K TR X; do
  d=$(diff <(grep "^$k|" "$DEV") <(grep "^$k|" "$RB"))
  if [ -z "$d" ]; then printf '  %-3s idêntico (%s)\n' "$k" "$(grep -c "^$k|" "$RB")"
  else printf '  %-3s DIFERENTE:\n' "$k"; echo "$d" | head -10 | sed 's/^/        /'; problemas=1; fi
done
# b) o que só existe na reconstrução é problema; o que só existe na dev é informação
for k in R P; do
  so_rb=$(comm -13 <(grep "^$k|" "$DEV" | sort) <(grep "^$k|" "$RB" | sort))
  so_dev=$(comm -23 <(grep "^$k|" "$DEV" | sort) <(grep "^$k|" "$RB" | sort))
  if [ -z "$so_rb" ]; then printf '  %-3s ok (só na dev, informação: %s)\n' "$k" "$(printf '%s' "$so_dev" | grep -c .)"
  else printf '  %-3s PROBLEMA: o seed cria e a dev não tem:\n' "$k"; echo "$so_rb" | head -10 | sed 's/^/        /'; problemas=1; fi
done
# c) conteúdo de texto por tabela (só as que o seed preenche)
res=$(awk -F'|' '
  FNR==NR { if ($1=="H") { dn[$2]=$3; dh[$2]=$4 } ; next }
  $1=="H" && $3+0 > 0 {
    t=$2; d=(t in dn) ? dn[t]+0 : 0
    if (d < $3+0)                  print "PROBLEMA|" t "|a dev tem " d " linha(s) e o seed cria " $3
    else if (d == $3+0 && dh[t] != $4) print "PROBLEMA|" t "|mesmo nº de linhas (" $3 ") mas texto diferente entre a dev e o seed"
    else if (d > $3+0)             print "INFO|" t "|a dev tem mais " (d-$3) " linha(s) do que o seed (" d " vs " $3 ")"
  }' "$DEV" "$RB")
n_prob=$(printf '%s\n' "$res" | grep -c '^PROBLEMA')
n_info=$(printf '%s\n' "$res" | grep -c '^INFO')
if [ "$n_prob" -eq 0 ]; then printf '  H   ok (tabelas com mais linhas na dev, informação: %s)\n' "$n_info"
else printf '  H   PROBLEMA:\n'; printf '%s\n' "$res" | grep '^PROBLEMA' | head -10 | sed 's/^/        /'; problemas=1; fi
[ "$n_info" -gt 0 ] && printf '%s\n' "$res" | grep '^INFO' | sed 's/^INFO|/        info: /; s/|/ — /'

echo
if [ "$problemas" -eq 0 ]; then echo "RESULTADO: a reconstrução do zero é igual à dev (só diferem dados que existem apenas na dev)."; else echo "RESULTADO: há diferenças que importam (ver acima)."; fi
exit "$problemas"
