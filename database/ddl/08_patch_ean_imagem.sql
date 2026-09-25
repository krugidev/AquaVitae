--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-20)
-- Decisões do utilizador (2026-09-20), antes do 1.º lote de bebidas da Awin:
--   1. `bebida_ean` (GTIN, anulável, ÚNICO): reconhece a mesma bebida vinda de outro retalhista (só se junta um link
--      novo em vez de duplicar a bebida). Texto e não número (zeros à esquerda); o CHECK só aceita 8 a 14 dígitos
--      (EAN-8, UPC-A, EAN-13, GTIN-14). O UNIQUE aceita vários NULL. Ao gerar o SQL de um lote: tirar espaços/hífenes.
--   2. `bebida_path_image` alargada de 255 para 1000: passa a poder guardar o URL absoluto da imagem do retalhista/feed
--      (com parâmetros) além do caminho relativo de um recurso estático do backend. O Android trata os dois casos.
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem tudo em 01_tables.sql). Tal como
-- 04 a 07, NÃO faz parte do run-migrations. É repetível: cada passo ignora o que já existe.
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/08_patch_ean_imagem.sql aquavitae-oracle-xe:/tmp/08.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/08.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  -- Ignora "já existe": ORA-01430 (coluna), ORA-02261 (já há um UNIQUE nessa coluna), ORA-02264 (nome de constraint em uso).
  PROCEDURE ddl(p_sql VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE p_sql;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE NOT IN (-1430, -2261, -2264) THEN
        RAISE;
      END IF;
  END ddl;
BEGIN
  ddl('ALTER TABLE bebida ADD (bebida_ean VARCHAR2(14 CHAR))');
  ddl('ALTER TABLE bebida ADD CONSTRAINT ck_bebida_ean CHECK (REGEXP_LIKE(bebida_ean, ''^[0-9]{8,14}$''))');
  ddl('ALTER TABLE bebida ADD CONSTRAINT uq_bebida_ean UNIQUE (bebida_ean)');
  -- Alargar é repetível (voltar a correr com o mesmo tamanho não faz nada).
  ddl('ALTER TABLE bebida MODIFY (bebida_path_image VARCHAR2(1000 CHAR))');
END;
/

COMMIT;
EXIT
