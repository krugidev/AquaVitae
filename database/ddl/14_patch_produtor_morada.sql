--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-24)
-- Decisão do utilizador (2026-09-24, fatia 6 — página do produtor): em vez de um mapa desenhado (o Maps SDK exige um
-- projeto Google Cloud com faturação e chave), a página do produtor mostra a MORADA em texto ("Rua ..., código postal,
-- localidade") e "Abrir no mapa" abre a app de mapas — pelas coordenadas se o produtor as tiver, pela morada se não.
-- Coluna nova `produtor_morada` (texto livre, anulável: NULL = não disponível, como todos os atributos de conteúdo).
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem a coluna em 01_tables.sql). Tal
-- como 04 a 13, NÃO faz parte do run-migrations. É repetível: se a coluna já existe, ignora.
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/14_patch_produtor_morada.sql aquavitae-oracle-xe:/tmp/14.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/14.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  -- Ignora "já existe": ORA-01430 (coluna já existe na tabela).
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
  ddl('ALTER TABLE produtor ADD (produtor_morada VARCHAR2(300 CHAR))');
  DBMS_OUTPUT.PUT_LINE('produtor.produtor_morada: ok');
END;
/

EXIT
