--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-20)
-- Grafias PT-PT dos países (decisão do utilizador, 2026-09-20, ponto 7 das decisões antes do 1.º lote): 13 nomes que
-- estavam na forma inglesa ou brasileira passam à forma portuguesa (os 3 últimos — Bahrein, Bangladesh e Kiribati, que
-- têm as duas formas em uso em PT-PT — confirmados pelo utilizador em 2026-09-21). É só um RENOME: os ids e todas as
-- relações ficam iguais. `pais` e `produtor_pais` são renomeados pelo mesmo nome e no fim confere-se que continuam
-- alinhadas (mesmos ids, mesmos nomes — ver database/README.md, "Países: duas tabelas, os mesmos ids").
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem os nomes em seed/01_lookups.sql).
-- Tal como 04 a 08, NÃO faz parte do run-migrations. É repetível: se o nome antigo já não existe, não faz nada.
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/09_patch_paises_pt.sql aquavitae-oracle-xe:/tmp/09.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/09.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  v_desalinhados NUMBER;

  PROCEDURE renomear(p_antigo VARCHAR2, p_novo VARCHAR2) IS
    v_a NUMBER;
    v_b NUMBER;
  BEGIN
    UPDATE pais SET bebida_pais_value = p_novo WHERE bebida_pais_value = p_antigo;
    v_a := SQL%ROWCOUNT;
    UPDATE produtor_pais SET produtor_pais_value = p_novo WHERE produtor_pais_value = p_antigo;
    v_b := SQL%ROWCOUNT;
    IF v_a <> v_b THEN
      RAISE_APPLICATION_ERROR(-20002, 'renome de "' || p_antigo || '" desigual: pais=' || v_a || ', produtor_pais=' || v_b);
    END IF;
    IF v_a > 0 THEN
      DBMS_OUTPUT.PUT_LINE('renomeado: ' || p_antigo || ' -> ' || p_novo);
    END IF;
  END renomear;
BEGIN
  renomear('Botswana', 'Botsuana');
  renomear('Kuwait', 'Koweit');
  renomear('Djibouti', 'Jibuti');
  renomear('Quirguistão', 'Quirguizistão');
  renomear('Uzbequistão', 'Usbequistão');
  renomear('Zimbábue', 'Zimbabué');
  renomear('Malawi', 'Malávi');
  renomear('Seychelles', 'Seicheles');
  renomear('Sri Lanka', 'Sri Lanca');
  renomear('Trinidad e Tobago', 'Trindade e Tobago');
  renomear('Bahrein', 'Barém');
  renomear('Bangladesh', 'Bangladeche');
  renomear('Kiribati', 'Quiribáti');

  SELECT COUNT(*) INTO v_desalinhados
    FROM pais a FULL OUTER JOIN produtor_pais b
      ON b.produtor_pais_id = a.bebida_pais_id AND b.produtor_pais_value = a.bebida_pais_value
   WHERE a.bebida_pais_id IS NULL OR b.produtor_pais_id IS NULL;
  IF v_desalinhados > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'pais e produtor_pais desalinhados: ' || v_desalinhados || ' linha(s)');
  END IF;
END;
/

COMMIT;
EXIT
