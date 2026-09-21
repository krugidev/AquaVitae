--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-21)
-- Decisão do utilizador (2026-09-21): a região do whisky passa a usar a MESMA lista de regiões dos produtores.
--   * `whisky.whisky_regiao_id` deixa de apontar para `whisky_regiao` e passa a apontar para `regiao(regiao_id)`.
--   * A tabela `whisky_regiao` (16 valores planos, sem país, que misturavam regiões e países: Speyside, Kentucky,
--     Japão, Portugal, ...) desaparece: uma só lista em vez de duas cópias que divergiriam (como aconteceu com
--     `pais`/`produtor_pais`). O país do whisky continua a ser o de origem da bebida (`bebida_pais_origem_id`), por isso
--     um whisky da Índia ou do Japão fica sem região (`NULL` = "não disponível") mas mostra o país. Quando entrarem
--     produtos de um país novo, acrescenta-se a região à `regiao` (receita em database/README.md).
--   * A coluna mantém-se no `whisky` (e não só no produtor) porque um engarrafador independente pode ter uma região
--     diferente da do produtor.
--
-- Segurança: se já houver algum whisky com região preenchida, o patch pára — esse id era de `whisky_regiao` e passaria
-- a significar outra coisa (id de `regiao`), por isso teria de ser passado à mão. Na dev a tabela `whisky` tem 0 linhas.
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem tudo em 01_tables.sql /
-- 02_constraints.sql / seed). Tal como 04 a 10, NÃO faz parte do run-migrations. É repetível: sem `whisky_regiao`, não
-- faz nada. (O DDL do Oracle confirma-se sozinho: se falhar a meio, voltar a correr retoma do ponto onde ficou.)
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/11_patch_whisky_regiao.sql aquavitae-oracle-xe:/tmp/11.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/11.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  v_existe NUMBER;
  v_usados NUMBER;
  v_fk     NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_existe FROM user_tables WHERE table_name = 'WHISKY_REGIAO';
  IF v_existe = 0 THEN
    DBMS_OUTPUT.PUT_LINE('whisky_regiao já não existe: nada a fazer.');
    RETURN;
  END IF;

  SELECT COUNT(*) INTO v_usados FROM whisky WHERE whisky_regiao_id IS NOT NULL;
  IF v_usados > 0 THEN
    RAISE_APPLICATION_ERROR(-20010,
      v_usados || ' whisky(s) com região preenchida: passar à mão para ids de `regiao` antes deste patch.');
  END IF;

  -- A FK antiga (para whisky_regiao) sai e entra a nova (para regiao), com o mesmo nome.
  SELECT COUNT(*) INTO v_fk FROM user_constraints WHERE constraint_name = 'FK_WHISKY_REGIAO';
  IF v_fk > 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE whisky DROP CONSTRAINT fk_whisky_regiao';
  END IF;
  EXECUTE IMMEDIATE 'ALTER TABLE whisky ADD CONSTRAINT fk_whisky_regiao
                       FOREIGN KEY (whisky_regiao_id) REFERENCES regiao (regiao_id)';
  -- PURGE: sem ele a tabela ficava na reciclagem do Oracle (BIN$...) e as suas constraints continuavam a aparecer.
  EXECUTE IMMEDIATE 'DROP TABLE whisky_regiao PURGE';
  DBMS_OUTPUT.PUT_LINE('whisky_regiao removida; whisky.whisky_regiao_id aponta agora para regiao.');
END;
/

COMMIT;
EXIT
