--------------------------------------------------------------------------------
-- AquaVitae — Seed: tabelas de lookup
-- Correr depois de 01_tables.sql / 02_constraints.sql / 03_triggers.sql.
--------------------------------------------------------------------------------

-- Papéis e nacionalidade
INSERT INTO utilizador_role (utilizador_role_value) VALUES ('Utilizador');
INSERT INTO utilizador_role (utilizador_role_value) VALUES ('Admin');

INSERT INTO utilizador_nationality (nationality_value) VALUES ('Portuguesa');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Espanhola');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Francesa');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Britânica');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Brasileira');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Alemã');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Italiana');
INSERT INTO utilizador_nationality (nationality_value) VALUES ('Norte-Americana');

-- Avatares
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Animais');
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Geométrico');
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Ilustração');

INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Raposa', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Animais'), '/avatars/raposa.png', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Coruja', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Animais'), '/avatars/coruja.png', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Hexágono Azul', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Geométrico'), '/avatars/hexagono_azul.png', 1);

-- Países (usado por bebida.origem, produtor.pais e utilizador.nationality são tabelas
-- separadas por decisão do schema — só a de bebida/produtor é populada aqui)
INSERT INTO pais (bebida_pais_value) VALUES ('Portugal');
INSERT INTO pais (bebida_pais_value) VALUES ('Espanha');
INSERT INTO pais (bebida_pais_value) VALUES ('França');
INSERT INTO pais (bebida_pais_value) VALUES ('Escócia');
INSERT INTO pais (bebida_pais_value) VALUES ('Itália');
INSERT INTO pais (bebida_pais_value) VALUES ('Estados Unidos');
INSERT INTO pais (bebida_pais_value) VALUES ('Reino Unido');
INSERT INTO pais (bebida_pais_value) VALUES ('Alemanha');

INSERT INTO produtor_pais (produtor_pais_value) VALUES ('Portugal');
INSERT INTO produtor_pais (produtor_pais_value) VALUES ('Espanha');
INSERT INTO produtor_pais (produtor_pais_value) VALUES ('França');
INSERT INTO produtor_pais (produtor_pais_value) VALUES ('Escócia');
INSERT INTO produtor_pais (produtor_pais_value) VALUES ('Itália');

-- Retalhistas
INSERT INTO retalhista_tipo (retalhista_tipo_value) VALUES ('Garrafeira Online');
INSERT INTO retalhista_tipo (retalhista_tipo_value) VALUES ('Supermercado');
INSERT INTO retalhista_tipo (retalhista_tipo_value) VALUES ('Produtor Direto');
INSERT INTO retalhista_tipo (retalhista_tipo_value) VALUES ('Marketplace');

-- Categorias de bebida (MVP: sem Rum — fica para a Fase 2, conforme briefing)
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Vinho');
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Whisky');
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Gin');
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Vodka');
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Aguardente');
INSERT INTO bebida_categoria (bebida_categoria_value) VALUES ('Licor');

-- Vinho
INSERT INTO vinho_corpo (vinho_corpo_value) VALUES ('Leve');
INSERT INTO vinho_corpo (vinho_corpo_value) VALUES ('Médio');
INSERT INTO vinho_corpo (vinho_corpo_value) VALUES ('Encorpado');

INSERT INTO vinho_tanino (vinho_tanino_value) VALUES ('Suave');
INSERT INTO vinho_tanino (vinho_tanino_value) VALUES ('Médio');
INSERT INTO vinho_tanino (vinho_tanino_value) VALUES ('Elevado');

INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Tinto');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Branco');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Rosé');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Espumante');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Verde');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Generoso');

INSERT INTO casta (casta_name) VALUES ('Touriga Nacional');
INSERT INTO casta (casta_name) VALUES ('Touriga Franca');
INSERT INTO casta (casta_name) VALUES ('Tinta Roriz');
INSERT INTO casta (casta_name) VALUES ('Alvarinho');
INSERT INTO casta (casta_name) VALUES ('Baga');
INSERT INTO casta (casta_name) VALUES ('Arinto');
INSERT INTO casta (casta_name) VALUES ('Fernão Pires');
INSERT INTO casta (casta_name) VALUES ('Trincadeira');
INSERT INTO casta (casta_name) VALUES ('Castelão');
INSERT INTO casta (casta_name) VALUES ('Loureiro');

-- Whisky (lookups preenchidos para catálogo futuro; sem itens seed nesta categoria
-- por não haver produção portuguesa relevante a usar como exemplo real)
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Single Malt');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Blended');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Bourbon');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Rye');

INSERT INTO whisky_regiao (whisky_regiao_nome) VALUES ('Speyside');
INSERT INTO whisky_regiao (whisky_regiao_nome) VALUES ('Islay');
INSERT INTO whisky_regiao (whisky_regiao_nome) VALUES ('Highlands');
INSERT INTO whisky_regiao (whisky_regiao_nome) VALUES ('Lowlands');
INSERT INTO whisky_regiao (whisky_regiao_nome) VALUES ('Kentucky');

INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Leve');
INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Médio');
INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Encorpado');

INSERT INTO cask (cask_value) VALUES ('Ex-Bourbon');
INSERT INTO cask (cask_value) VALUES ('Ex-Sherry');
INSERT INTO cask (cask_value) VALUES ('Ex-Vinho do Porto');
INSERT INTO cask (cask_value) VALUES ('Carvalho Novo');

-- Gin
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Leve');
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Médio');
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Encorpado');

INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Pot Still');
INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Column Still');
INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Vapor Infusion');

INSERT INTO botanico (botanico_name) VALUES ('Zimbro');
INSERT INTO botanico (botanico_name) VALUES ('Coentros');
INSERT INTO botanico (botanico_name) VALUES ('Casca de Citrinos');
INSERT INTO botanico (botanico_name) VALUES ('Alecrim');
INSERT INTO botanico (botanico_name) VALUES ('Alfazema');
INSERT INTO botanico (botanico_name) VALUES ('Erva-Príncipe');
INSERT INTO botanico (botanico_name) VALUES ('Cardamomo');

-- Licor
INSERT INTO licor_base (licor_base_value) VALUES ('Aguardente Vínica');
INSERT INTO licor_base (licor_base_value) VALUES ('Aguardente Bagaceira');
INSERT INTO licor_base (licor_base_value) VALUES ('Álcool Neutro');
INSERT INTO licor_base (licor_base_value) VALUES ('Aguardente de Cereja');

INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Ervas');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Cereja');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Citrinos');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Amêndoa');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Chocolate');

-- Vodka (lookups preenchidos para catálogo futuro; sem itens seed nesta categoria
-- pela mesma razão do whisky)
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Trigo');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Batata');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Centeio');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Milho');

-- Aguardente
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Leve');
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Médio');
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Encorpado');

INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Carvalho Novo');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Ex-Vinho do Porto');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Inox (sem estágio)');

INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Medronho');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Bagaço de Uva');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Cereja');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Cana-de-açúcar');

COMMIT;
