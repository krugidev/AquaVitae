--------------------------------------------------------------------------------
-- AquaVitae — Seed: produtores, retalhistas e bebidas de exemplo
-- Correr depois de 01_lookups.sql.
--
-- Nota: nomes de produtores/marcas e regiões refletem produtos portugueses
-- reais (Barca Velha, Licor Beirão, Gin Sharish, etc.). Anos de colheita,
-- preços, notas sensoriais (acidez/doçura/corpo/taninos) e associações de
-- castas são valores de desenvolvimento — plausíveis mas não verificados
-- contra a ficha técnica oficial de cada produto. Rever antes de qualquer
-- uso público. Cada bloco usa `RETURNING bebida_id INTO v_bebida_id` para
-- obter o ID gerado por `bebida` e reutilizá-lo na tabela subtype
-- (padrão supertype/subtype do schema) — é também o padrão recomendado
-- para a camada de inserção da API.
--------------------------------------------------------------------------------

-- ============================================================
-- PRODUTORES
-- ============================================================

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Casa Ferreirinha', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro', 1751, 'https://www.ferreirinha.pt', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Quinta do Crasto', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro', 1615, 'https://www.quintadocrasto.pt', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Herdade do Esporão', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Alentejo', 1973, 'https://www.esporao.com', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Quinta de Soalheiro', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Vinho Verde — Melgaço', 1974, 'https://www.soalheiro.com', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_website, produtor_permite_visitas)
VALUES ('Luís Pato', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Bairrada', 'https://www.luispato.com', 0);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Niepoort', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro', 1842, 'https://www.niepoort-vinhos.com', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_website, produtor_permite_visitas)
VALUES ('Quinta Nova de Nossa Senhora do Carmo', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro', 'https://www.quintanova.com', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_permite_visitas)
VALUES ('Quinta do Vale Meão', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao, produtor_ano_fundacao, produtor_website, produtor_permite_visitas)
VALUES ('Destilaria Sharish', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Arraiolos — Alentejo', 2016, 'https://www.sharish.pt', 1);

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Gin 44°', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Peniche');

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Licor Beirão', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Lousã — Beira Litoral');

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Fábrica de Ginjinha de Óbidos', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Óbidos');

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Ginjinha Sem Rival', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Lisboa — Rossio');

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Destilaria Artesanal do Algarve', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Algarve');

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao)
VALUES ('Adega Cooperativa do Douro', (SELECT produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Portugal'), 'Douro');

-- ============================================================
-- RETALHISTAS
-- ============================================================

INSERT INTO retalhista (retalhista_nome, retalhista_website, retalhista_tipo_id, retalhista_rede_afiliados, retalhista_codigo_afiliado, retalhista_comissao_percentagem, retalhista_is_ativo)
VALUES ('Garrafeira Nacional', 'https://www.garrafeiranacional.com',
        (SELECT retalhista_tipo_id FROM retalhista_tipo WHERE retalhista_tipo_value = 'Garrafeira Online'),
        'Awin', 'PLACEHOLDER_AFFILIATE_CODE', 5.00, 1);

INSERT INTO retalhista (retalhista_nome, retalhista_website, retalhista_tipo_id, retalhista_rede_afiliados, retalhista_codigo_afiliado, retalhista_comissao_percentagem, retalhista_is_ativo)
VALUES ('Continente Online', 'https://www.continente.pt',
        (SELECT retalhista_tipo_id FROM retalhista_tipo WHERE retalhista_tipo_value = 'Supermercado'),
        'Rakuten', 'PLACEHOLDER_AFFILIATE_CODE', 3.00, 1);

-- ============================================================
-- BEBIDAS — VINHO
-- ============================================================

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Barca Velha 2015',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Casa Ferreirinha'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2015, 13.5, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Elevado'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 40);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Franca'), 35);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Aragonez (Tinta Roriz)'), 25);

  INSERT INTO bebida_link_compra (bebida_id, retalhista_id, bebida_link_compra_url, bebida_link_compra_preco_atual)
  VALUES (v_bebida_id, (SELECT retalhista_id FROM retalhista WHERE retalhista_nome = 'Garrafeira Nacional'),
          'https://www.garrafeiranacional.com/produto/PLACEHOLDER?aff=PLACEHOLDER_AFFILIATE_CODE', 180.00);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Vinha Grande Tinto 2019',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Casa Ferreirinha'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2019, 13.5, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Médio'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Médio'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Franca'), 45);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 30);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Aragonez (Tinta Roriz)'), 25);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Quinta do Crasto Reserva Tinto 2018',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Quinta do Crasto'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2018, 14.0, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Elevado'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 35);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Franca'), 35);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Aragonez (Tinta Roriz)'), 30);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Esporão Reserva Tinto 2018',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Herdade do Esporão'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2018, 14.5, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          2, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Médio'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Trincadeira'), 40);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 30);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Castelão'), 30);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Soalheiro Alvarinho 2021',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Quinta de Soalheiro'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2021, 12.5, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Leve'),
          4, 2,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Suave'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Branco'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Alvarinho'), 100);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Vinha Formal Tinto 2017',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Luís Pato'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2017, 13.0, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          4, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Elevado'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Baga'), 100);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Redoma Branco 2020',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Niepoort'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2020, 13.0, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Médio'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Suave'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Branco'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Arinto'), 60);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Fernão Pires'), 40);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Quinta Nova Grande Reserva Tinto 2017',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Quinta Nova de Nossa Senhora do Carmo'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2017, 14.0, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Elevado'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 50);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Franca'), 30);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Aragonez (Tinta Roriz)'), 20);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_ano_producao, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Vinho'),
          'Vale Meão Tinto 2016',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Quinta do Vale Meão'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          2016, 14.0, 750)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO vinho (bebida_id, vinho_corpo_id, vinho_nivel_acidez, vinho_nivel_docura, vinho_tanino_id, vinho_tipo_id)
  VALUES (v_bebida_id,
          (SELECT vinho_corpo_id FROM vinho_corpo WHERE vinho_corpo_value = 'Encorpado'),
          3, 1,
          (SELECT vinho_tanino_id FROM vinho_tanino WHERE vinho_tanino_value = 'Elevado'),
          (SELECT vinho_tipo_id FROM vinho_tipo WHERE vinho_tipo_value = 'Tinto'));

  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Nacional'), 45);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Touriga Franca'), 30);
  INSERT INTO vinho_casta (vinho_id, casta_id, vinho_casta_percentagem) VALUES (v_bebida_id, (SELECT casta_id FROM casta WHERE casta_name = 'Aragonez (Tinta Roriz)'), 25);
END;
/

-- ============================================================
-- BEBIDAS — GIN
-- ============================================================

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Gin'),
          'Sharish Blue Magic Gin',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Destilaria Sharish'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          40.0, 700)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO gin (bebida_id, gin_tipo_destilacao_id, gin_corpo_id, gin_nivel_docura)
  VALUES (v_bebida_id,
          (SELECT gin_destilacao_id FROM gin_destilacao WHERE gin_destilacao_value = 'Alambique'),
          (SELECT gin_corpo_id FROM gin_corpo WHERE gin_corpo_value = 'Médio'),
          2);

  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Zimbro'));
  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Citrino'));
  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Herbáceo e Verde'));
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Gin'),
          'Gin 44°',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Gin 44°'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          40.0, 700)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO gin (bebida_id, gin_tipo_destilacao_id, gin_corpo_id, gin_nivel_docura)
  VALUES (v_bebida_id,
          (SELECT gin_destilacao_id FROM gin_destilacao WHERE gin_destilacao_value = 'Infusão de Vapor'),
          (SELECT gin_corpo_id FROM gin_corpo WHERE gin_corpo_value = 'Leve'),
          1);

  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Zimbro'));
  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Especiaria e Picante'));
  INSERT INTO gin_botanico (gin_id, botanico_id) VALUES (v_bebida_id, (SELECT botanico_id FROM botanico WHERE botanico_name = 'Citrino'));
END;
/

-- ============================================================
-- BEBIDAS — LICOR
-- ============================================================

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Licor'),
          'Licor Beirão',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Licor Beirão'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          22.0, 700)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO licor (bebida_id, licor_base_id, licor_nivel_docura)
  VALUES (v_bebida_id,
          (SELECT licor_base_id FROM licor_base WHERE licor_base_value = 'Vínica'),
          3);

  INSERT INTO licor_sabor (licor_id, sabor_licor_id) VALUES (v_bebida_id, (SELECT sabor_licor_id FROM sabor_licor WHERE sabor_licor_value = 'Ervas e Especiarias'));

  INSERT INTO bebida_link_compra (bebida_id, retalhista_id, bebida_link_compra_url, bebida_link_compra_preco_atual)
  VALUES (v_bebida_id, (SELECT retalhista_id FROM retalhista WHERE retalhista_nome = 'Continente Online'),
          'https://www.continente.pt/produto/PLACEHOLDER?aff=PLACEHOLDER_AFFILIATE_CODE', 6.50);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Licor'),
          'Ginjinha de Óbidos',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Fábrica de Ginjinha de Óbidos'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          20.0, 500)
  RETURNING bebida_id INTO v_bebida_id;

  -- Base sem equivalente nas notas de seed (Neutra/Vodka/Vínica/Rum/Whisky/Tequila/Mezcal): NULL = "não disponível"
  INSERT INTO licor (bebida_id, licor_base_id, licor_nivel_docura)
  VALUES (v_bebida_id, NULL, 4);

  INSERT INTO licor_sabor (licor_id, sabor_licor_id) VALUES (v_bebida_id, (SELECT sabor_licor_id FROM sabor_licor WHERE sabor_licor_value = 'Cereja'));
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Licor'),
          'Ginjinha Sem Rival',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Ginjinha Sem Rival'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          19.0, 500)
  RETURNING bebida_id INTO v_bebida_id;

  -- Base sem equivalente nas notas de seed (Neutra/Vodka/Vínica/Rum/Whisky/Tequila/Mezcal): NULL = "não disponível"
  INSERT INTO licor (bebida_id, licor_base_id, licor_nivel_docura)
  VALUES (v_bebida_id, NULL, 4);

  INSERT INTO licor_sabor (licor_id, sabor_licor_id) VALUES (v_bebida_id, (SELECT sabor_licor_id FROM sabor_licor WHERE sabor_licor_value = 'Cereja'));
END;
/

-- ============================================================
-- BEBIDAS — AGUARDENTE
-- ============================================================

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Aguardente'),
          'Medronho do Algarve',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Destilaria Artesanal do Algarve'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          48.0, 500)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO aguardente (bebida_id, aguardente_materia_prima_id, aguardente_corpo_id, aguardente_idade_anos)
  VALUES (v_bebida_id,
          (SELECT aguardente_materia_prima_id FROM aguardente_materia_prima WHERE aguardente_materia_prima_value = 'Fruta'),
          (SELECT aguardente_corpo_id FROM aguardente_corpo WHERE aguardente_corpo_value = 'Encorpado'),
          0);
END;
/

DECLARE
  v_bebida_id bebida.bebida_id%TYPE;
BEGIN
  INSERT INTO bebida (bebida_category_id, bebida_name, bebida_producer_id, bebida_pais_origem_id, bebida_teor_alcoolico, bebida_volume_ml)
  VALUES ((SELECT bebida_categoria_id FROM bebida_categoria WHERE bebida_categoria_value = 'Aguardente'),
          'Bagaceira Velha do Douro',
          (SELECT produtor_id FROM produtor WHERE produtor_nome = 'Adega Cooperativa do Douro'),
          (SELECT bebida_pais_id FROM pais WHERE bebida_pais_value = 'Portugal'),
          42.0, 500)
  RETURNING bebida_id INTO v_bebida_id;

  INSERT INTO aguardente (bebida_id, aguardente_materia_prima_id, aguardente_corpo_id, aguardente_idade_anos)
  VALUES (v_bebida_id,
          (SELECT aguardente_materia_prima_id FROM aguardente_materia_prima WHERE aguardente_materia_prima_value = 'Bagaço de Uva'),
          (SELECT aguardente_corpo_id FROM aguardente_corpo WHERE aguardente_corpo_value = 'Médio'),
          3);

  INSERT INTO aguardente_tipo_cask_relation (aguardente_id, aguardente_tipo_cask_id, aguardente_tipo_cask_ordem_estagio)
  VALUES (v_bebida_id, (SELECT aguardente_tipo_cask_id FROM aguardente_tipo_cask WHERE aguardente_tipo_cask_value = 'Carvalho'), 1);
END;
/

COMMIT;
