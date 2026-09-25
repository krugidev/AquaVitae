--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-22)
-- Decisão do utilizador (2026-09-22): a bandeira ao lado da nacionalidade (ecrã 8 do onboarding) vem de um código de
-- país guardado na própria tabela `utilizador_nationality` (`nationality_codigo_pais`, ISO 3166-1 alfa-2: PT, ES, ...),
-- em vez de a app o mapear pelo nome ("Portuguesa" -> PT). Assim uma nacionalidade nova é só uma linha de SQL, sem
-- publicar nova versão da app. Anulável: uma nacionalidade sem código fica sem bandeira.
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem a coluna em 01_tables.sql e os
-- códigos em seed/01_lookups.sql). Tal como 04 a 12, NÃO faz parte do run-migrations. É repetível: se a coluna ou a
-- constraint já existem, ignora; os códigos só se preenchem onde ainda estão a NULL (não pisa um valor posto à mão).
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/13_patch_nacionalidade_codigo.sql aquavitae-oracle-xe:/tmp/13.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/13.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  -- Ignora "já existe": ORA-01430 (coluna), ORA-02264 (nome de constraint em uso).
  PROCEDURE ddl(p_sql VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE p_sql;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE NOT IN (-1430, -2264) THEN
        RAISE;
      END IF;
  END ddl;
BEGIN
  ddl('ALTER TABLE utilizador_nationality ADD (nationality_codigo_pais VARCHAR2(2 CHAR))');
  ddl('ALTER TABLE utilizador_nationality ADD CONSTRAINT ck_nationality_codigo_pais CHECK (REGEXP_LIKE(nationality_codigo_pais, ''^[A-Z]{2}$''))');
END;
/

-- As 8 nacionalidades do seed. O nome tem acentos (Britânica, Alemã): correr o ficheiro com NLS_LANG (ver acima).
UPDATE utilizador_nationality SET nationality_codigo_pais = 'PT' WHERE nationality_value = 'Portuguesa'      AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'ES' WHERE nationality_value = 'Espanhola'      AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'FR' WHERE nationality_value = 'Francesa'       AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'GB' WHERE nationality_value = 'Britânica'      AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'BR' WHERE nationality_value = 'Brasileira'     AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'DE' WHERE nationality_value = 'Alemã'          AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'IT' WHERE nationality_value = 'Italiana'       AND nationality_codigo_pais IS NULL;
UPDATE utilizador_nationality SET nationality_codigo_pais = 'US' WHERE nationality_value = 'Norte-Americana' AND nationality_codigo_pais IS NULL;

COMMIT;
EXIT
