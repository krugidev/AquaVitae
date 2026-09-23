-- Extrai a estrutura e o conteúdo de um schema para comparação (usado por rebuild-check.sh, que substitui __OWN__ pelo
-- nome do schema em MAIÚSCULAS e corre isto como SYS). Uma linha por facto, com um prefixo que diz o que é:
--   T|tabela                                  as tabelas
--   C|tabela.coluna|tipo|tamanho|precisão|escala|null=|identity=     as colunas (sem ordem: um patch acrescenta ao fim)
--   K|tabela|tipo|colunas|referência|condição|nome    as constraints, por estrutura (o nome só se não for gerado)
--   TR|tabela|trigger|evento|estado           os triggers
--   H|tabela|nº de linhas|hash                o conteúdo de TEXTO de cada tabela (os ids não entram: a dev tem identity com
--                                             histórico e não coincidiria com uma BD nova)
--   R|país|região                             as regiões por país (nomes, não ids)
--   P|produtor|país|região                    produtor -> país -> região (nomes)
--   X|desalinhados=n                          pais e produtor_pais alinhadas (mesmos ids e nomes): tem de dar 0
SET PAGESIZE 0 FEEDBACK OFF LINESIZE 1000 TRIMSPOOL ON HEADING OFF VERIFY OFF SERVEROUTPUT ON SIZE UNLIMITED LONG 100000

-- T: tabelas
SELECT 'T|' || table_name FROM dba_tables WHERE owner = '__OWN__' ORDER BY 1;

-- C: colunas
SELECT 'C|' || c.table_name || '.' || c.column_name || '|' || c.data_type || '|' || NVL(TO_CHAR(c.char_length), '') || '|' ||
       NVL(TO_CHAR(c.data_precision), '') || '|' || NVL(TO_CHAR(c.data_scale), '') || '|null=' || c.nullable || '|identity=' || c.identity_column
  FROM dba_tab_columns c JOIN dba_tables t ON t.owner = c.owner AND t.table_name = c.table_name
 WHERE c.owner = '__OWN__' ORDER BY 1;

-- K: constraints por estrutura (tabela, tipo, colunas, referência, condição); o nome só conta se não for gerado pelo sistema
SELECT 'K|' || c.table_name || '|' || c.constraint_type || '|' ||
       (SELECT LISTAGG(cc.column_name, ',') WITHIN GROUP (ORDER BY cc.position) FROM dba_cons_columns cc WHERE cc.owner = c.owner AND cc.constraint_name = c.constraint_name) || '|' ||
       NVL((SELECT r.table_name || '(' || (SELECT LISTAGG(rc.column_name, ',') WITHIN GROUP (ORDER BY rc.position) FROM dba_cons_columns rc WHERE rc.owner = r.owner AND rc.constraint_name = r.constraint_name) || ')'
              FROM dba_constraints r WHERE r.owner = c.owner AND r.constraint_name = c.r_constraint_name), '') || '|' ||
       DECODE(c.constraint_type, 'C', c.search_condition_vc, '') || '|' ||
       CASE WHEN c.constraint_name LIKE 'SYS\_C%' ESCAPE '\' THEN '(gerado)' ELSE c.constraint_name END
  FROM dba_constraints c
 WHERE c.owner = '__OWN__' AND c.constraint_type IN ('P', 'U', 'R', 'C') ORDER BY 1;

-- TR: triggers
SELECT 'TR|' || table_name || '|' || trigger_name || '|' || triggering_event || '|' || status FROM dba_triggers WHERE owner = '__OWN__' ORDER BY 1;

-- H: contagem e hash do conteúdo de texto (VARCHAR2/CHAR) de cada tabela
DECLARE
  v_cols VARCHAR2(4000);
  v_n    NUMBER;
  v_h    NUMBER;
BEGIN
  FOR t IN (SELECT table_name FROM dba_tables WHERE owner = '__OWN__' ORDER BY table_name) LOOP
    SELECT LISTAGG('NVL("' || column_name || '", ''~'')', ' || ''|'' || ') WITHIN GROUP (ORDER BY column_id)
      INTO v_cols
      FROM dba_tab_columns
     WHERE owner = '__OWN__' AND table_name = t.table_name AND data_type IN ('VARCHAR2', 'CHAR');
    IF v_cols IS NULL THEN v_cols := ''''''; END IF;
    EXECUTE IMMEDIATE 'SELECT COUNT(*), NVL(SUM(ORA_HASH(' || v_cols || ')), 0) FROM "__OWN__"."' || t.table_name || '"' INTO v_n, v_h;
    DBMS_OUTPUT.PUT_LINE('H|' || t.table_name || '|' || v_n || '|' || v_h);
  END LOOP;
END;
/

-- R: regiões por país (nomes, não ids)
SELECT 'R|' || p.produtor_pais_value || '|' || r.regiao_nome
  FROM "__OWN__".regiao r JOIN "__OWN__".produtor_pais p ON p.produtor_pais_id = r.regiao_pais_id ORDER BY 1;

-- P: produtor -> país -> região (nomes)
SELECT 'P|' || pr.produtor_nome || '|' || NVL(p.produtor_pais_value, '~') || '|' || NVL(r.regiao_nome, '~')
  FROM "__OWN__".produtor pr
  LEFT JOIN "__OWN__".produtor_pais p ON p.produtor_pais_id = pr.produtor_pais_id
  LEFT JOIN "__OWN__".regiao r ON r.regiao_id = pr.produtor_regiao_id ORDER BY 1;

-- X: pais e produtor_pais alinhadas (mesmos ids e nomes): deve dar 0
SELECT 'X|desalinhados=' || COUNT(*)
  FROM "__OWN__".pais a FULL OUTER JOIN "__OWN__".produtor_pais b
    ON b.produtor_pais_id = a.bebida_pais_id AND b.produtor_pais_value = a.bebida_pais_value
 WHERE a.bebida_pais_id IS NULL OR b.produtor_pais_id IS NULL;
EXIT
