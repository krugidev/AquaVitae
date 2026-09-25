--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-21)
-- Decisão do utilizador (2026-09-21): guardar a data/hora em que cada utilizador aceitou os termos e condições, na
-- própria tabela `utilizador` (`utilizador_termos_aceites_em`). NULL = nunca aceitou. Os utilizadores que já existem
-- ficam a NULL (nunca aceitaram nada: os termos ainda não existem), por isso a app pede-lhes a aceitação no próximo
-- login. Só se guarda a data (sem número de versão): se os termos mudarem, quem aceitou antes da data configurada em
-- `aquavitae.termos.em-vigor-desde` volta a aceitar, e o histórico de quando cada um aceitou não se perde.
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem a coluna em 01_tables.sql).
-- Tal como 04 a 11, NÃO faz parte do run-migrations. É repetível: se a coluna já existe, ignora.
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/12_patch_termos_aceites.sql aquavitae-oracle-xe:/tmp/12.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/12.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  -- Ignora "já existe": ORA-01430 (coluna).
  PROCEDURE ddl(p_sql VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE p_sql;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE NOT IN (-1430) THEN
        RAISE;
      END IF;
  END ddl;
BEGIN
  ddl('ALTER TABLE utilizador ADD (utilizador_termos_aceites_em TIMESTAMP)');
END;
/

COMMIT;
EXIT
