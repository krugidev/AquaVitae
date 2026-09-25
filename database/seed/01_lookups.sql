--------------------------------------------------------------------------------
-- AquaVitae — Seed: tabelas de lookup
-- Correr depois de 01_tables.sql / 02_constraints.sql / 03_triggers.sql.
--------------------------------------------------------------------------------

-- Papéis e nacionalidade
INSERT INTO utilizador_role (utilizador_role_value) VALUES ('Utilizador');
INSERT INTO utilizador_role (utilizador_role_value) VALUES ('Admin');
-- 'Moderador': fase futura (notas de seed) — não inserido por agora.

-- O 2.º valor é o código ISO do país (a app desenha a bandeira a partir dele; ver 13_patch_nacionalidade_codigo.sql).
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Portuguesa', 'PT');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Espanhola', 'ES');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Francesa', 'FR');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Britânica', 'GB');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Brasileira', 'BR');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Alemã', 'DE');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Italiana', 'IT');
INSERT INTO utilizador_nationality (nationality_value, nationality_codigo_pais) VALUES ('Norte-Americana', 'US');

-- Avatares — 27 desenhos de linha (SVG) fornecidos pelo utilizador, 9 por categoria.
-- Ficheiros em backend/src/main/resources/static/icones/avatares/ (servidos pelo Spring Boot
-- como recursos estáticos em GET /icones/avatares/<slug>.svg — ver backend/API_ENDPOINTS.md).
-- Spec de design (cores, escalas, estado selecionado) documentada no README.md ao lado dos SVGs.
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Castas');
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Garrafas');
INSERT INTO avatar_categoria (avatar_categoria_value) VALUES ('Copos');

INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cacho cónico', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-cacho-conico.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cacho cilíndrico', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-cacho-cilindrico.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cacho alado', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-cacho-alado.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cacho solto', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-cacho-solto.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Bago', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-bago.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Folha de videira', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-folha.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Gavinha', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-gavinha.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Ramo', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-ramo.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Videira', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Castas'), 'icones/avatares/casta-videira.svg', 1);

INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Bordalesa', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-bordalesa.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Borgonhesa', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-borgonhesa.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Flauta', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-flauta.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Champanhe', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-champanhe.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Garrafão', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-garrafao.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Whisky', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-whisky.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Gin', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-gin.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cerveja', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-cerveja.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Licor', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Garrafas'), 'icones/avatares/garrafa-licor.svg', 1);

INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Tulipa', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-tulipa.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Copo de Porto', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-porto.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Balão', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-balao.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Copo baixo', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-baixo.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Flute', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-flute.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Cálice', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-calice.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Copo de gin', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-gin.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Caneca', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-caneca.svg', 1);
INSERT INTO utilizador_avatar (avatar_name, avatar_category_id, avatar_path_image, avatar_is_active)
VALUES ('Shot', (SELECT avatar_categoria_id FROM avatar_categoria WHERE avatar_categoria_value = 'Copos'), 'icones/avatares/copo-shot.svg', 1);

-- Países: a MESMA lista nas duas tabelas — `pais` (origem da bebida) e `produtor_pais` (país do
-- produtor) —, inserida em simultâneo para os ids coincidirem. O filtro do catálogo usa
-- pais.bebida_pais_id e /api/lookup/regioes usa produtor_pais.produtor_pais_id, e o cliente trata-os
-- como um só id. Ao acrescentar um país: inserir com o MESMO id nas duas (receita e query de verificação em
-- database/README.md; "na mesma ordem" não chega, os identity divergem). Numa BD nova o loop abaixo alinha-os.
-- Fonte: android/AQUAVITAESEEDS-NOTES, com grafias em PT-PT e duplicados fundidos (Holanda = Países Baixos,
-- Suazilândia = Essuatíni, Congo, Estados Federados da Micronésia = Micronésia).
DECLARE
  TYPE t_lista IS TABLE OF VARCHAR2(60 CHAR);
  v_paises t_lista := t_lista(
    'Portugal',
    'Espanha',
    'França',
    'Escócia',
    'Itália',
    'Estados Unidos da América',
    'Reino Unido',
    'Alemanha',
    'Abecásia',
    'Afeganistão',
    'África do Sul',
    'Albânia',
    'Andorra',
    'Angola',
    'Antígua e Barbuda',
    'Arábia Saudita',
    'Argélia',
    'Argentina',
    'Arménia',
    'Aruba',
    'Austrália',
    'Áustria',
    'Azerbaijão',
    'Bahamas',
    'Barém',
    'Bangladeche',
    'Barbados',
    'Bélgica',
    'Belize',
    'Benim',
    'Bermudas',
    'Bielorrússia',
    'Bolívia',
    'Bósnia e Herzegovina',
    'Botsuana',
    'Brasil',
    'Brunei',
    'Bulgária',
    'Burkina Faso',
    'Burundi',
    'Butão',
    'Cabo Verde',
    'Camarões',
    'Camboja',
    'Canadá',
    'Catar',
    'Cazaquistão',
    'Chade',
    'Chile',
    'China',
    'Chipre',
    'Singapura',
    'Colômbia',
    'Comores',
    'Coreia do Norte',
    'Coreia do Sul',
    'Costa do Marfim',
    'Costa Rica',
    'Croácia',
    'Cuba',
    'Curaçau',
    'Dinamarca',
    'Jibuti',
    'Dominica',
    'Egito',
    'El Salvador',
    'Emirados Árabes Unidos',
    'Equador',
    'Eritreia',
    'Eslováquia',
    'Eslovénia',
    'Estónia',
    'Essuatíni',
    'Etiópia',
    'Fiji',
    'Filipinas',
    'Finlândia',
    'Gabão',
    'Gâmbia',
    'Gana',
    'Geórgia',
    'Granada',
    'Grécia',
    'Gronelândia',
    'Guadalupe',
    'Guiana Francesa',
    'Guatemala',
    'Guiana',
    'Guiné',
    'Guiné-Bissau',
    'Guiné Equatorial',
    'Haiti',
    'Honduras',
    'Hong Kong',
    'Hungria',
    'Iémen',
    'Índia',
    'Indonésia',
    'Inglaterra',
    'Ilhas Caimão',
    'Ilhas Marianas do Norte',
    'Ilhas Virgens',
    'Irão',
    'Iraque',
    'Irlanda do Norte',
    'Irlanda',
    'Islândia',
    'Israel',
    'Jamaica',
    'Japão',
    'Jordânia',
    'Quiribáti',
    'Kosovo',
    'Koweit',
    'Laos',
    'Lesoto',
    'Letónia',
    'Líbano',
    'Libéria',
    'Líbia',
    'Liechtenstein',
    'Lituânia',
    'Luxemburgo',
    'Macedónia do Norte',
    'Macau',
    'Madagascar',
    'Malásia',
    'Malávi',
    'Maldivas',
    'Mali',
    'Malta',
    'Marrocos',
    'Ilhas Marshall',
    'Maurícia',
    'Mauritânia',
    'México',
    'Mianmar',
    'Micronésia',
    'Moçambique',
    'Moldávia',
    'Mónaco',
    'Mongólia',
    'Montenegro',
    'Namíbia',
    'Nauru',
    'Nepal',
    'Nicarágua',
    'Níger',
    'Nigéria',
    'Noruega',
    'Nova Zelândia',
    'Omã',
    'Ossétia do Sul',
    'País de Gales',
    'Países Baixos',
    'Palau',
    'Palestina',
    'Panamá',
    'Papua-Nova Guiné',
    'Paquistão',
    'Paraguai',
    'Peru',
    'Polónia',
    'Polinésia Francesa',
    'Porto Rico',
    'Quénia',
    'Quirguizistão',
    'República Árabe Saaraui Democrática',
    'República Centro-Africana',
    'República Democrática do Congo',
    'República do Congo',
    'República Dominicana',
    'República Checa',
    'República Turca de Chipre do Norte',
    'Roménia',
    'Ruanda',
    'Rússia',
    'Ilhas Salomão',
    'Samoa',
    'San Marino',
    'Santa Lúcia',
    'São Cristóvão e Neves',
    'São Tomé e Príncipe',
    'São Vicente e Granadinas',
    'Senegal',
    'Serra Leoa',
    'Sérvia',
    'Seicheles',
    'Síria',
    'Somália',
    'Sri Lanca',
    'Sudão do Sul',
    'Sudão',
    'Suécia',
    'Suíça',
    'Suriname',
    'Tailândia',
    'Taiwan',
    'Tajiquistão',
    'Tanzânia',
    'Timor-Leste',
    'Togo',
    'Tonga',
    'Trindade e Tobago',
    'Tunísia',
    'Turquemenistão',
    'Turquia',
    'Tuvalu',
    'Ucrânia',
    'Uganda',
    'Uruguai',
    'Usbequistão',
    'Vanuatu',
    'Vaticano',
    'Venezuela',
    'Vietname',
    'Zâmbia',
    'Zimbabué'

  );
BEGIN
  FOR i IN 1 .. v_paises.COUNT LOOP
    INSERT INTO pais (bebida_pais_value) VALUES (v_paises(i));
    INSERT INTO produtor_pais (produtor_pais_value) VALUES (v_paises(i));
  END LOOP;
END;
/

-- Regiões: lista do utilizador (2026-09-20), só dos países para começar (Portugal, Espanha, Escócia, Irlanda, Canadá,
-- EUA); as restantes acrescentam-se à medida que entram produtos (basta um INSERT em `regiao` e escolher o país).
-- Nome PT-PT quando há forma estabelecida (Califórnia, Nova Iorque, Quebeque, Astúrias, ...) e o nome local nos
-- restantes (Speyside, Highlands, Cork, ...). Douro e Porto são regiões separadas. Irlanda: só os 32 condados (as
-- províncias eram títulos de grupo). Sem sub-regiões (Napa Valley, Cima Corgo, ...) por agora. Tem de ficar igual
-- ao patch database/ddl/10_patch_regioes_lista_final.sql (que explica as escolhas).
DECLARE
  TYPE t_lista IS TABLE OF VARCHAR2(100 CHAR);

  PROCEDURE regioes(p_pais VARCHAR2, p_nomes t_lista) IS
    v_pais produtor_pais.produtor_pais_id%TYPE;
  BEGIN
    SELECT produtor_pais_id INTO v_pais FROM produtor_pais WHERE produtor_pais_value = p_pais;
    FOR i IN 1 .. p_nomes.COUNT LOOP
      INSERT INTO regiao (regiao_nome, regiao_pais_id) VALUES (p_nomes(i), v_pais);
    END LOOP;
  END regioes;
BEGIN
  regioes('Portugal', t_lista('Douro', 'Porto', 'Minho', 'Alentejo', 'Madeira', 'Açores', 'Dão', 'Bairrada',
                              'Lourinhã', 'Setúbal', 'Lisboa', 'Tejo', 'Algarve', 'Trás-os-Montes'));

  regioes('Espanha', t_lista('La Rioja', 'Ribera del Duero', 'Jerez', 'Galiza', 'Catalunha', 'País Basco', 'Navarra',
                             'Valência', 'Alicante', 'Andaluzia', 'Aragão', 'Astúrias', 'Ilhas Baleares',
                             'Ilhas Canárias', 'Cantábria', 'Castela e Leão', 'Castela-La Mancha', 'Extremadura',
                             'Madrid', 'Múrcia'));

  regioes('Escócia', t_lista('Aberdeen', 'Aberdeenshire', 'Angus', 'Dundee', 'Argyll e Ilhas', 'Ayrshire', 'Arran',
                             'Dumfries', 'Galloway', 'Edinburgh', 'Lothians', 'Glasgow', 'Clyde Valley', 'Orkney',
                             'Perthshire', 'Shetland', 'Highlands', 'Lowlands', 'Speyside', 'Reino de Fife',
                             'Outer Hebrides', 'Scottish Borders', 'Loch Lomond', 'Forth Valley', 'Stirling',
                             'Falkirk', 'Clackmannanshire', 'Islay', 'Campbeltown', 'Islands'));

  regioes('Irlanda', t_lista('Galway', 'Leitrim', 'Mayo', 'Roscommon', 'Sligo',
                             'Antrim', 'Armagh', 'Cavan', 'Donegal', 'Down', 'Fermanagh', 'Londonderry', 'Monaghan',
                             'Tyrone',
                             'Clare', 'Cork', 'Kerry', 'Limerick', 'Tipperary', 'Waterford',
                             'Carlow', 'Dublin', 'Kildare', 'Kilkenny', 'Laois', 'Longford', 'Louth', 'Meath',
                             'Offaly', 'Westmeath', 'Wexford', 'Wicklow'));

  regioes('Canadá', t_lista('Terra Nova e Labrador', 'Ilha do Príncipe Eduardo', 'Nova Escócia', 'Novo Brunswick',
                            'Quebeque', 'Ontário', 'Manitoba', 'Saskatchewan', 'Alberta', 'Colúmbia Britânica',
                            'Nunavut', 'Territórios do Noroeste', 'Yukon'));

  regioes('Estados Unidos da América',
          t_lista('Alabama', 'Alasca', 'Arizona', 'Arkansas', 'Califórnia', 'Carolina do Norte', 'Carolina do Sul',
                  'Colorado', 'Connecticut', 'Dakota do Norte', 'Dakota do Sul', 'Delaware', 'Flórida', 'Geórgia',
                  'Havai', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Luisiana', 'Maine',
                  'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana',
                  'Nebraska', 'Nevada', 'Nova Hampshire', 'Nova Jérsia', 'Novo México', 'Nova Iorque', 'Ohio',
                  'Oklahoma', 'Oregon', 'Pensilvânia', 'Rhode Island', 'Tennessee', 'Texas', 'Utah', 'Vermont',
                  'Virgínia', 'Virgínia Ocidental', 'Washington', 'Wisconsin', 'Wyoming'));
END;
/

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
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Frisante');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Generoso');
INSERT INTO vinho_tipo (vinho_tipo_value) VALUES ('Sobremesa');

INSERT INTO casta_tipo (casta_tipo_value) VALUES ('Tinta');
INSERT INTO casta_tipo (casta_tipo_value) VALUES ('Branca');

-- Castas portuguesas (notas de seed do utilizador). Fora ficaram 3 entradas "de enchimento" (Tinto Sem
-- Nome, Branco Desconhecido, Branco Especial). Aragonez = Tinta Roriz (a mesma casta): uma só linha com os
-- dois nomes, para a pesquisa por casta encontrar ambos.
DECLARE
  TYPE t_lista IS TABLE OF VARCHAR2(60 CHAR);
  v_tinta   casta_tipo.casta_tipo_id%TYPE;
  v_branca  casta_tipo.casta_tipo_id%TYPE;
  v_tintas  t_lista := t_lista(
    'Alcoa',
    'Alfrocheiro',
    'Alicante Bouschet',
    'Alvarelhão',
    'Alvarelhão Ceitão',
    'Amaral',
    'Amor-Não-Me-Deixes',
    'Amostrinha',
    'Aragonez (Tinta Roriz)',
    'Aramon',
    'Arjunção',
    'Baga',
    'Barca',
    'Barreto',
    'Bastardo',
    'Bastardo Tinto',
    'Bonvedro',
    'Borraçal',
    'Bragão',
    'Branjo',
    'Cabinda',
    'Caladoc',
    'Calrão',
    'Camarate',
    'Campanário',
    'Carrega Burros',
    'Carrega Tinto',
    'Casculho',
    'Castelã',
    'Castelão',
    'Castelino',
    'Cidadelhe',
    'Cidreiro',
    'Complexa',
    'Concieira',
    'Coração de Galo',
    'Cornifesto',
    'Corropio',
    'Corvo',
    'Deliciosa',
    'Doçal',
    'Doce',
    'Donzelinho Tinto',
    'Engomada',
    'Esgana Cão Tinto',
    'Espadeiro',
    'Espadeiro Mole',
    'Farinheira',
    'Fepiro',
    'Ferral',
    'Galego',
    'Gonçalo Pires',
    'Gorda',
    'Gouveio Preto',
    'Graciosa',
    'Grangeal',
    'Grossa',
    'Jaen',
    'Lourela',
    'Lusitano',
    'Malandra',
    'Malvarisco',
    'Malvasia Preta',
    'Manteúdo Preto',
    'Mário Feld',
    'Marufo',
    'Melhorio',
    'Melra',
    'Mindelo',
    'Molar',
    'Monvedro',
    'Moreto',
    'Moscargo',
    'Moscatel Galego Tinto',
    'Mourisco',
    'Mourisco de Semente',
    'Mourisco de Trevões',
    'Mulata',
    'Negra Mole',
    'Nevoeira',
    'Padeiro',
    'Parreira Matias',
    'Patorra',
    'Pau Ferro',
    'Pedral',
    'Pero Pinhão',
    'Péxem',
    'Pical',
    'Pilongo',
    'Português Azul',
    'Preto Cardana',
    'Preto Martinho',
    'Primavera',
    'Rabo de Anho',
    'Rabo de Lobo',
    'Rabo de Ovelha Tinto',
    'Ramisco',
    'Ramisco Tinto',
    'Ricoca',
    'Rodo',
    'Roseira',
    'Rufete',
    'Saborinho',
    'Santareno',
    'São Saul',
    'Sevilhão',
    'Sousão',
    'Tinta',
    'Tinta Aguiar',
    'Tinta Aurélio',
    'Tinta Barroca',
    'Tinta Bastardinha',
    'Tinta Caiada',
    'Tinta Carvalha',
    'Tinta Fontes',
    'Tinta Francisca',
    'Tinta Lameira',
    'Tinta Lisboa',
    'Tinta Martins',
    'Tinta Mesquita',
    'Tinta Miúda',
    'Tinta Negra',
    'Tinta Penajóia',
    'Tinta Pereira',
    'Tinta Pomar',
    'Tinta Porto Santo',
    'Tinta Tabuaço',
    'Tintem',
    'Tintinha',
    'Tinto-cão',
    'Tinto Pegões',
    'Touriga Fêmea',
    'Touriga Franca',
    'Touriga Nacional',
    'Transâncora',
    'Trincadeira',
    'Triunfo',
    'Valbom',
    'Valdosa',
    'Varejoa',
    'Verdelho Tinto',
    'Verdial Tinto',
    'Vinhão',
    'Xara',
    'Zé do Telheiro'

  );
  v_brancas t_lista := t_lista(
    'Alicante Branco',
    'Almafra',
    'Almenhaca',
    'Antão Vaz',
    'Alvadurão',
    'Alvarinho',
    'Alvar',
    'Alvarelhão Branco',
    'Arinto',
    'Arinto do Interior',
    'Avesso',
    'Azal',
    'Babosa',
    'Barcelo',
    'Bastardo Branco',
    'Batoca',
    'Beba',
    'Bical',
    'Boal Barreiro',
    'Boal Branco',
    'Boal Espinho',
    'Branca de Anadia',
    'Branco Gouvães',
    'Branco Guimarães',
    'Branco João',
    'Branda',
    'Budelho',
    'Caínho',
    'Caracol',
    'Caramela',
    'Carão de Moça',
    'Carrasquenho',
    'Carrega Branco',
    'Cascal',
    'Castália',
    'Castelão Branco',
    'Castelo Branco',
    'Casteloa',
    'Cerceal Branco',
    'Cercial',
    'Côdega de Larinho',
    'Corval',
    'Crato Espanhol',
    'Dedo-de-Dama',
    'Diagalves',
    'Dona-branca',
    'Dona-joaquina',
    'Donzelinho Branco',
    'Dorinto',
    'Encruzado',
    'Esganinho',
    'Esganoso',
    'Estreito Macio',
    'Fernão Pires',
    'Folgasão',
    'Folha de Figueira',
    'Fonte Cal',
    'Galego Dourado',
    'Generosa',
    'Gigante',
    'Godelho',
    'Gouveio',
    'Gouveio Estimado',
    'Gouveio Real',
    'Granho',
    'Lameiro',
    'Larião',
    'Leira',
    'Lilás',
    'Loureiro',
    'Luzidio',
    'Malvasia',
    'Malvasia Bianca',
    'Malvasia Branca',
    'Malvasia Branca de S. Jorge',
    'Malvasia Cândida',
    'Malvasia Fina/Boal',
    'Malvasia Parda',
    'Malvasia Rei',
    'Malvasia Romana',
    'Malvia',
    'Malvoeira',
    'Manteúdo',
    'Marquinhas',
    'Molinha',
    'Moscatel Galego Branco',
    'Moscatel Graúdo',
    'Moscatel Nunes',
    'Mourisco Branco',
    'Naia',
    'Pé Comprido',
    'Perigo',
    'Perrum',
    'Pinheira Branca',
    'Pintosa',
    'Praça',
    'Promissão',
    'Rabigato',
    'Rabigato Franco',
    'Rabigato Moreno',
    'Rabo de Ovelha',
    'Ratinho',
    'Roupeiro Branco',
    'Sabro',
    'Samarrinho',
    'Santoal',
    'São Mamede',
    'Sarigo',
    'Seara Nova',
    'Semilão',
    'Sercial',
    'Sercialinho',
    'Síria',
    'Tália',
    'Tamarez',
    'Terrantês',
    'Terrantez da Terceira',
    'Terrantez do Pico',
    'Touriga Branca',
    'Trajadura',
    'Trincadeira Branca',
    'Trincadeira-das-pratas',
    'Uva Cão',
    'Uva Cavaco',
    'Uva Salsa',
    'Valente',
    'Valveirinho',
    'Vencedor',
    'Verdelho',
    'Verdial Branco',
    'Viosinho',
    'Vital'

  );
BEGIN
  SELECT casta_tipo_id INTO v_tinta  FROM casta_tipo WHERE casta_tipo_value = 'Tinta';
  SELECT casta_tipo_id INTO v_branca FROM casta_tipo WHERE casta_tipo_value = 'Branca';
  FOR i IN 1 .. v_tintas.COUNT LOOP
    INSERT INTO casta (casta_name, casta_tipo_id) VALUES (v_tintas(i), v_tinta);
  END LOOP;
  FOR i IN 1 .. v_brancas.COUNT LOOP
    INSERT INTO casta (casta_name, casta_tipo_id) VALUES (v_brancas(i), v_branca);
  END LOOP;
END;
/

-- Cask (madeira da barrica) e formato da barrica. Usados pelo vinho em vinho_cask; a madeira é também
-- partilhada com o whisky (whisky_cask) — os "Ex-Bourbon"/"Ex-Xerez"/... do whisky entram aqui quando
-- houver whisky real (as aguardentes têm lookup próprio, aguardente_tipo_cask).
INSERT INTO cask (cask_value) VALUES ('Carvalho Francês');
INSERT INTO cask (cask_value) VALUES ('Carvalho Americano');
INSERT INTO cask (cask_value) VALUES ('Carvalho Húngaro');
INSERT INTO cask (cask_value) VALUES ('Carvalho Esloveno');
INSERT INTO cask (cask_value) VALUES ('Acácia');
INSERT INTO cask (cask_value) VALUES ('Castanheiro');

INSERT INTO cask_formato (cask_formato_value) VALUES ('Bordeaux');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Burgundy');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Demi-Muid');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Tonel');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Balseiro');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Foudre');
INSERT INTO cask_formato (cask_formato_value) VALUES ('Oval');

-- Whisky (lookups preenchidos para catálogo futuro; sem itens seed nesta categoria
-- por não haver produção portuguesa relevante a usar como exemplo real)
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Single Malt');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Single Grain');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Blended Scotch');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Blended Malt');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Bourbon');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Rye');
INSERT INTO whisky_tipo (whisky_tipo_value) VALUES ('Tennessee');

-- (A região do whisky é a da tabela `regiao`, preenchida mais acima: Speyside, Islay, Kentucky, Cork, ...)

INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Leve');
INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Médio');
INSERT INTO whisky_corpo (whisky_corpo_value) VALUES ('Encorpado');

-- Gin
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Leve');
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Médio');
INSERT INTO gin_corpo (gin_corpo_value) VALUES ('Encorpado');

INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Alambique');
INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Infusão de Vapor');
INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Vácuo');
INSERT INTO gin_destilacao (gin_destilacao_value) VALUES ('Composto');

-- Botânicos: famílias aromáticas (não botânicos individuais)
INSERT INTO botanico (botanico_name) VALUES ('Zimbro');
INSERT INTO botanico (botanico_name) VALUES ('Citrino');
INSERT INTO botanico (botanico_name) VALUES ('Especiaria e Picante');
INSERT INTO botanico (botanico_name) VALUES ('Floral');
INSERT INTO botanico (botanico_name) VALUES ('Herbáceo e Verde');
INSERT INTO botanico (botanico_name) VALUES ('Terroso');
INSERT INTO botanico (botanico_name) VALUES ('Frutado');

-- Licor
INSERT INTO licor_base (licor_base_value) VALUES ('Neutra');
INSERT INTO licor_base (licor_base_value) VALUES ('Vodka');
INSERT INTO licor_base (licor_base_value) VALUES ('Vínica');
INSERT INTO licor_base (licor_base_value) VALUES ('Rum');
INSERT INTO licor_base (licor_base_value) VALUES ('Whisky');
INSERT INTO licor_base (licor_base_value) VALUES ('Tequila');
INSERT INTO licor_base (licor_base_value) VALUES ('Mezcal');

INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Cereja');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Limoncello');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Frutos do Bosque');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Pêssego');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Laranja');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Banana');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Maracujá');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Ervas e Especiarias');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Frutos Secos');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Chocolate');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Amêndoa');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Baunilha');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Cremosos');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Medronho');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Amora');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Mirtilo');
INSERT INTO sabor_licor (sabor_licor_value) VALUES ('Groselha');

-- Vodka (lookups preenchidos para catálogo futuro; sem itens seed nesta categoria
-- pela mesma razão do whisky)
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Trigo');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Centeio');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Cevada');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Milho');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Arroz');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Batata');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Mandioca');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Cana de Açúcar');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Melaço');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Fruta');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Beterraba Sacarina');
INSERT INTO vodka_materia_prima (vodka_materia_prima_value) VALUES ('Soro de Leite');

-- Aguardente. "Sem estágio" já não é um valor de cask: uma aguardente sem barrica não tem linhas em
-- aguardente_tipo_cask_relation (aguardente_idade_anos = 0). Base: as notas + Bagaço de Uva e Vinho
-- (bagaceira e aguardente vínica não se distinguiriam do medronho só com 'Fruta').
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Leve');
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Médio');
INSERT INTO aguardente_corpo (aguardente_corpo_value) VALUES ('Encorpado');

INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Carvalho');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Carvalho Americano');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Carvalho Francês');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Carvalho do Leste Europeu');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Madeira Nativa');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Amburana');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Bálsamo');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Jequitibá');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Castanheira');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Ipê');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Pau-Brasil');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Segundo-Uso');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Ex-Bourbon');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Ex-Xerez');
INSERT INTO aguardente_tipo_cask (aguardente_tipo_cask_value) VALUES ('Ex-Vinho');

INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Fruta');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Cereal');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Cana de Açúcar');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Agave');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Tubérculo');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Bagaço de Uva');
INSERT INTO aguardente_materia_prima (aguardente_materia_prima_value) VALUES ('Vinho');

COMMIT;
