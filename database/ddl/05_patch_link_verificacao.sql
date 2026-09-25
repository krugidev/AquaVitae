--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-18)
-- Estado e verificação diária dos links de compra: um link que deixa de existir
-- ou fica sem stock passa a is_ativo = 0 (esconde o botão de compra), mas a linha
-- mantém-se como histórico.
--
-- Só correr numa BD de DEV JÁ EXISTENTE (criada antes deste patch). Quem
-- reconstruir a BD do zero já tem estas colunas em 01_tables.sql — repetir isto
-- causaria erro de coluna duplicada. Tal como o 04_patch_endpoints.sql, NÃO faz
-- parte da sequência do run-migrations.ps1/.sh.
--
-- Uso manual (com NLS_LANG, ver database/README.md):
--   Get-Content ddl/05_patch_link_verificacao.sql -Raw | docker exec -i -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1"
--------------------------------------------------------------------------------

ALTER TABLE bebida_link_compra ADD (
    bebida_link_compra_url_verificacao   VARCHAR2(500 CHAR),
    bebida_link_compra_is_ativo          NUMBER(1) DEFAULT 1 NOT NULL CHECK (bebida_link_compra_is_ativo IN (0, 1)),
    bebida_link_compra_data_verificacao  TIMESTAMP,
    bebida_link_compra_falhas_seguidas   NUMBER DEFAULT 0 NOT NULL,
    bebida_link_compra_data_indisponivel TIMESTAMP,
    bebida_link_compra_motivo            VARCHAR2(200 CHAR)
);

COMMIT;
