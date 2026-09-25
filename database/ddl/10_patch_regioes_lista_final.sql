--------------------------------------------------------------------------------
-- AquaVitae — Patch incremental (2026-09-20)
-- Lista final de regiões (o utilizador enviou a sua em 2026-09-20; substitui a lista provisória do patch 07).
-- Países para começar: Portugal, Espanha, Escócia, Irlanda, Canadá e EUA. As regiões dos outros países acrescentam-se
-- à medida que entram produtos (receita em database/README.md). Sem sub-regiões (Napa Valley, Cima Corgo, ...).
--
-- Como a lista do utilizador foi tratada:
--   * Maiúsculas e sem acentos -> nome próprio, com acentos (ACORES -> Açores, TRAS-OS-MONTES -> Trás-os-Montes, ...).
--   * PT-PT onde há forma estabelecida (Canadá, EUA, Espanha, Portugal): Quebeque, Ontário, Colúmbia Britânica,
--     Califórnia, Nova Iorque, Carolina do Norte, Astúrias, ... Escócia e Irlanda mantêm os nomes locais (Speyside,
--     Highlands, Cork, Kerry, ...), que são os que vêm nos rótulos.
--   * Portugal: Vinho Verde passa a Minho e Península de Setúbal a Setúbal (renome: mantém o id e o produtor
--     que já o usa); Lourinhã acrescentada; Távora-Varosa e Beira Interior saem (não estão na lista nova).
--   * Irlanda: só os 32 condados. As 4 províncias (Connacht, Ulster, Munster, Leinster) eram títulos de grupo, e
--     misturar os dois níveis na mesma lista daria dados inconsistentes ("Cork" ou "Munster"?). Os 6 condados do
--     Ulster que estão na Irlanda do Norte ficam sob "Irlanda" (whiskey irlandês), tal como na lista do utilizador.
--   * Sugestões minhas (o utilizador pediu sugestões): Escócia + Islay, Campbeltown e Islands (regiões clássicas do
--     Scotch que faltavam; estavam na lista provisória); Espanha + Castela-La Mancha (a única comunidade autónoma
--     que faltava; substitui "La Mancha").
--   * Espanha mistura comunidades autónomas com denominações (La Rioja, Ribera del Duero, Jerez): escolhe-se a
--     mais específica da lista. Idem na Escócia (Speyside dentro de Highlands).
--   * França, Itália e Inglaterra ficam sem regiões (fora dos países "para começar"): nenhum produtor as usava.
--
-- Só correr numa BD de DEV criada antes deste patch (quem reconstruir do zero já tem a lista em seed/01_lookups.sql).
-- Tal como 04 a 09, NÃO faz parte do run-migrations. É repetível. Se uma região a remover ainda for usada por um
-- produtor, a FK (fk_produtor_regiao, ORA-02292) recusa e o patch reverte tudo.
--
-- Uso manual (com NLS_LANG, ver database/README.md). Envia sempre o ficheiro, nunca SQL inline:
--   docker cp ddl/10_patch_regioes_lista_final.sql aquavitae-oracle-xe:/tmp/10.sql
--   docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/10.sql
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON SIZE UNLIMITED
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  TYPE t_lista IS TABLE OF VARCHAR2(100 CHAR);

  FUNCTION id_pais(p_pais VARCHAR2) RETURN produtor_pais.produtor_pais_id%TYPE IS
    v_id produtor_pais.produtor_pais_id%TYPE;
  BEGIN
    SELECT produtor_pais_id INTO v_id FROM produtor_pais WHERE produtor_pais_value = p_pais;
    RETURN v_id;
  END id_pais;

  -- Renome no país (mantém o id e o que o usa). Não faz nada se o nome antigo já não existir ou o novo já existir.
  PROCEDURE renomear(p_pais VARCHAR2, p_antigo VARCHAR2, p_novo VARCHAR2) IS
    v_pais produtor_pais.produtor_pais_id%TYPE := id_pais(p_pais);
  BEGIN
    UPDATE regiao SET regiao_nome = p_novo
     WHERE regiao_pais_id = v_pais AND regiao_nome = p_antigo
       AND NOT EXISTS (SELECT 1 FROM regiao WHERE regiao_pais_id = v_pais AND regiao_nome = p_novo);
    IF SQL%ROWCOUNT > 0 THEN
      DBMS_OUTPUT.PUT_LINE('renomeada: ' || p_pais || ' / ' || p_antigo || ' -> ' || p_novo);
    END IF;
  END renomear;

  -- Deixa o país com exatamente estas regiões: acrescenta as que faltam e remove as que sobram.
  PROCEDURE sincronizar(p_pais VARCHAR2, p_nomes t_lista) IS
    v_pais produtor_pais.produtor_pais_id%TYPE := id_pais(p_pais);
  BEGIN
    FOR i IN 1 .. p_nomes.COUNT LOOP
      INSERT INTO regiao (regiao_nome, regiao_pais_id)
        SELECT p_nomes(i), v_pais FROM dual
         WHERE NOT EXISTS (SELECT 1 FROM regiao WHERE regiao_pais_id = v_pais AND regiao_nome = p_nomes(i));
      IF SQL%ROWCOUNT > 0 THEN
        DBMS_OUTPUT.PUT_LINE('acrescentada: ' || p_pais || ' / ' || p_nomes(i));
      END IF;
    END LOOP;
    FOR r IN (SELECT regiao_id, regiao_nome FROM regiao WHERE regiao_pais_id = v_pais ORDER BY regiao_id) LOOP
      IF NOT (r.regiao_nome MEMBER OF p_nomes) THEN
        DELETE FROM regiao WHERE regiao_id = r.regiao_id;
        DBMS_OUTPUT.PUT_LINE('removida   : ' || p_pais || ' / ' || r.regiao_nome);
      END IF;
    END LOOP;
  END sincronizar;

  PROCEDURE remover_todas(p_pais VARCHAR2) IS
    v_pais produtor_pais.produtor_pais_id%TYPE := id_pais(p_pais);
  BEGIN
    DELETE FROM regiao WHERE regiao_pais_id = v_pais;
    IF SQL%ROWCOUNT > 0 THEN
      DBMS_OUTPUT.PUT_LINE('removidas  : ' || SQL%ROWCOUNT || ' região(ões) de ' || p_pais);
    END IF;
  END remover_todas;
BEGIN
  renomear('Portugal', 'Vinho Verde', 'Minho');
  renomear('Portugal', 'Península de Setúbal', 'Setúbal');
  renomear('Espanha', 'Rioja', 'La Rioja');
  renomear('Espanha', 'La Mancha', 'Castela-La Mancha');

  sincronizar('Portugal', t_lista('Douro', 'Porto', 'Minho', 'Alentejo', 'Madeira', 'Açores', 'Dão', 'Bairrada',
                                  'Lourinhã', 'Setúbal', 'Lisboa', 'Tejo', 'Algarve', 'Trás-os-Montes'));

  sincronizar('Espanha', t_lista('La Rioja', 'Ribera del Duero', 'Jerez', 'Galiza', 'Catalunha', 'País Basco', 'Navarra',
                                 'Valência', 'Alicante', 'Andaluzia', 'Aragão', 'Astúrias', 'Ilhas Baleares',
                                 'Ilhas Canárias', 'Cantábria', 'Castela e Leão', 'Castela-La Mancha', 'Extremadura',
                                 'Madrid', 'Múrcia'));

  sincronizar('Escócia', t_lista('Aberdeen', 'Aberdeenshire', 'Angus', 'Dundee', 'Argyll e Ilhas', 'Ayrshire', 'Arran',
                                 'Dumfries', 'Galloway', 'Edinburgh', 'Lothians', 'Glasgow', 'Clyde Valley', 'Orkney',
                                 'Perthshire', 'Shetland', 'Highlands', 'Lowlands', 'Speyside', 'Reino de Fife',
                                 'Outer Hebrides', 'Scottish Borders', 'Loch Lomond', 'Forth Valley', 'Stirling',
                                 'Falkirk', 'Clackmannanshire', 'Islay', 'Campbeltown', 'Islands'));

  sincronizar('Irlanda', t_lista('Galway', 'Leitrim', 'Mayo', 'Roscommon', 'Sligo',
                                 'Antrim', 'Armagh', 'Cavan', 'Donegal', 'Down', 'Fermanagh', 'Londonderry', 'Monaghan',
                                 'Tyrone',
                                 'Clare', 'Cork', 'Kerry', 'Limerick', 'Tipperary', 'Waterford',
                                 'Carlow', 'Dublin', 'Kildare', 'Kilkenny', 'Laois', 'Longford', 'Louth', 'Meath',
                                 'Offaly', 'Westmeath', 'Wexford', 'Wicklow'));

  sincronizar('Canadá', t_lista('Terra Nova e Labrador', 'Ilha do Príncipe Eduardo', 'Nova Escócia', 'Novo Brunswick',
                                'Quebeque', 'Ontário', 'Manitoba', 'Saskatchewan', 'Alberta', 'Colúmbia Britânica',
                                'Nunavut', 'Territórios do Noroeste', 'Yukon'));

  sincronizar('Estados Unidos da América',
              t_lista('Alabama', 'Alasca', 'Arizona', 'Arkansas', 'Califórnia', 'Carolina do Norte', 'Carolina do Sul',
                      'Colorado', 'Connecticut', 'Dakota do Norte', 'Dakota do Sul', 'Delaware', 'Flórida', 'Geórgia',
                      'Havai', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Luisiana', 'Maine',
                      'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana',
                      'Nebraska', 'Nevada', 'Nova Hampshire', 'Nova Jérsia', 'Novo México', 'Nova Iorque', 'Ohio',
                      'Oklahoma', 'Oregon', 'Pensilvânia', 'Rhode Island', 'Tennessee', 'Texas', 'Utah', 'Vermont',
                      'Virgínia', 'Virgínia Ocidental', 'Washington', 'Wisconsin', 'Wyoming'));

  remover_todas('França');
  remover_todas('Itália');
  remover_todas('Inglaterra');
END;
/

COMMIT;
EXIT
