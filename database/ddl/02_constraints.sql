--------------------------------------------------------------------------------
-- AquaVitae — DDL Oracle (foreign keys)
-- Uma FK por Ref do DBML original. Todas ON DELETE/UPDATE NO ACTION,
-- tal como definido no schema (sem cascade — apagar um utilizador ou
-- bebida com histórico associado deve ser um gesto explícito da app,
-- não um efeito colateral do Oracle).
--------------------------------------------------------------------------------

ALTER TABLE utilizador ADD CONSTRAINT fk_utilizador_nationality
    FOREIGN KEY (utilizador_nationality_id) REFERENCES utilizador_nationality (nationality_id);

ALTER TABLE utilizador ADD CONSTRAINT fk_utilizador_avatar
    FOREIGN KEY (utilizador_avatar_photo_id) REFERENCES utilizador_avatar (avatar_id);

ALTER TABLE utilizador ADD CONSTRAINT fk_utilizador_role
    FOREIGN KEY (utilizador_role_id) REFERENCES utilizador_role (utilizador_role_id);

ALTER TABLE utilizador_password_reset ADD CONSTRAINT fk_pwd_reset_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE utilizador_avatar ADD CONSTRAINT fk_avatar_categoria
    FOREIGN KEY (avatar_category_id) REFERENCES avatar_categoria (avatar_categoria_id);

ALTER TABLE bebida ADD CONSTRAINT fk_bebida_categoria
    FOREIGN KEY (bebida_category_id) REFERENCES bebida_categoria (bebida_categoria_id);

ALTER TABLE bebida ADD CONSTRAINT fk_bebida_pais_origem
    FOREIGN KEY (bebida_pais_origem_id) REFERENCES pais (bebida_pais_id);

ALTER TABLE bebida ADD CONSTRAINT fk_bebida_produtor
    FOREIGN KEY (bebida_producer_id) REFERENCES produtor (produtor_id);

-- Supertype/subtype: bebida_id da subtype é também FK 1:1 para bebida
ALTER TABLE vinho ADD CONSTRAINT fk_vinho_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE whisky ADD CONSTRAINT fk_whisky_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE gin ADD CONSTRAINT fk_gin_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE licor ADD CONSTRAINT fk_licor_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE vodka ADD CONSTRAINT fk_vodka_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE aguardente ADD CONSTRAINT fk_aguardente_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

-- Vinho
ALTER TABLE vinho ADD CONSTRAINT fk_vinho_corpo
    FOREIGN KEY (vinho_corpo_id) REFERENCES vinho_corpo (vinho_corpo_id);

ALTER TABLE vinho ADD CONSTRAINT fk_vinho_tanino
    FOREIGN KEY (vinho_tanino_id) REFERENCES vinho_tanino (vinho_tanino_id);

ALTER TABLE vinho ADD CONSTRAINT fk_vinho_tipo
    FOREIGN KEY (vinho_tipo_id) REFERENCES vinho_tipo (vinho_tipo_id);

ALTER TABLE vinho_casta ADD CONSTRAINT fk_vinho_casta_vinho
    FOREIGN KEY (vinho_id) REFERENCES vinho (bebida_id);

ALTER TABLE vinho_casta ADD CONSTRAINT fk_vinho_casta_casta
    FOREIGN KEY (casta_id) REFERENCES casta (casta_id);

ALTER TABLE vinho_cask ADD CONSTRAINT fk_vinho_cask_vinho
    FOREIGN KEY (vinho_id) REFERENCES vinho (bebida_id);

ALTER TABLE vinho_cask ADD CONSTRAINT fk_vinho_cask_cask
    FOREIGN KEY (cask_id) REFERENCES cask (cask_id);

ALTER TABLE vinho_cask ADD CONSTRAINT fk_vinho_cask_formato
    FOREIGN KEY (cask_formato_id) REFERENCES cask_formato (cask_formato_id);

ALTER TABLE casta ADD CONSTRAINT fk_casta_tipo
    FOREIGN KEY (casta_tipo_id) REFERENCES casta_tipo (casta_tipo_id);

-- Whisky
ALTER TABLE whisky ADD CONSTRAINT fk_whisky_tipo
    FOREIGN KEY (whisky_tipo_id) REFERENCES whisky_tipo (whisky_tipo_id);

ALTER TABLE whisky ADD CONSTRAINT fk_whisky_regiao
    FOREIGN KEY (whisky_regiao_id) REFERENCES whisky_regiao (whisky_regiao_id);

ALTER TABLE whisky ADD CONSTRAINT fk_whisky_corpo
    FOREIGN KEY (whisky_corpo_id) REFERENCES whisky_corpo (whisky_corpo_id);

ALTER TABLE whisky_cask ADD CONSTRAINT fk_whisky_cask_whisky
    FOREIGN KEY (whisky_id) REFERENCES whisky (bebida_id);

ALTER TABLE whisky_cask ADD CONSTRAINT fk_whisky_cask_cask
    FOREIGN KEY (cask_id) REFERENCES cask (cask_id);

-- Gin
ALTER TABLE gin ADD CONSTRAINT fk_gin_destilacao
    FOREIGN KEY (gin_tipo_destilacao_id) REFERENCES gin_destilacao (gin_destilacao_id);

ALTER TABLE gin ADD CONSTRAINT fk_gin_corpo
    FOREIGN KEY (gin_corpo_id) REFERENCES gin_corpo (gin_corpo_id);

ALTER TABLE gin_botanico ADD CONSTRAINT fk_gin_botanico_gin
    FOREIGN KEY (gin_id) REFERENCES gin (bebida_id);

ALTER TABLE gin_botanico ADD CONSTRAINT fk_gin_botanico_botanico
    FOREIGN KEY (botanico_id) REFERENCES botanico (botanico_id);

-- Licor
ALTER TABLE licor ADD CONSTRAINT fk_licor_base
    FOREIGN KEY (licor_base_id) REFERENCES licor_base (licor_base_id);

ALTER TABLE licor_sabor ADD CONSTRAINT fk_licor_sabor_licor
    FOREIGN KEY (licor_id) REFERENCES licor (bebida_id);

ALTER TABLE licor_sabor ADD CONSTRAINT fk_licor_sabor_sabor
    FOREIGN KEY (sabor_licor_id) REFERENCES sabor_licor (sabor_licor_id);

-- Vodka
ALTER TABLE vodka ADD CONSTRAINT fk_vodka_materia_prima
    FOREIGN KEY (vodka_materia_prima_id) REFERENCES vodka_materia_prima (vodka_materia_prima_id);

ALTER TABLE vodka_materia_prima_relation ADD CONSTRAINT fk_vodka_rel_vodka
    FOREIGN KEY (vodka_id) REFERENCES vodka (bebida_id);

ALTER TABLE vodka_materia_prima_relation ADD CONSTRAINT fk_vodka_rel_materia_prima
    FOREIGN KEY (vodka_materia_prima_id) REFERENCES vodka_materia_prima (vodka_materia_prima_id);

-- Aguardente
ALTER TABLE aguardente ADD CONSTRAINT fk_aguardente_corpo
    FOREIGN KEY (aguardente_corpo_id) REFERENCES aguardente_corpo (aguardente_corpo_id);

ALTER TABLE aguardente_tipo_cask_relation ADD CONSTRAINT fk_aguard_cask_rel_aguard
    FOREIGN KEY (aguardente_id) REFERENCES aguardente (bebida_id);

ALTER TABLE aguardente_tipo_cask_relation ADD CONSTRAINT fk_aguard_cask_rel_cask
    FOREIGN KEY (aguardente_tipo_cask_id) REFERENCES aguardente_tipo_cask (aguardente_tipo_cask_id);

ALTER TABLE aguardente_materia_prima_relation ADD CONSTRAINT fk_aguard_mp_rel_aguard
    FOREIGN KEY (aguardente_id) REFERENCES aguardente (bebida_id);

ALTER TABLE aguardente_materia_prima_relation ADD CONSTRAINT fk_aguard_mp_rel_mp
    FOREIGN KEY (aguardente_materia_prima_id) REFERENCES aguardente_materia_prima (aguardente_materia_prima_id);

-- Relações utilizador <-> bebida
ALTER TABLE review ADD CONSTRAINT fk_review_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE review ADD CONSTRAINT fk_review_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE favorito ADD CONSTRAINT fk_favorito_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE favorito ADD CONSTRAINT fk_favorito_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE bebida_provada ADD CONSTRAINT fk_bebida_provada_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE bebida_provada ADD CONSTRAINT fk_bebida_provada_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE wishlist ADD CONSTRAINT fk_wishlist_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE wishlist ADD CONSTRAINT fk_wishlist_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE cave ADD CONSTRAINT fk_cave_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE cave_bebida ADD CONSTRAINT fk_cave_bebida_cave
    FOREIGN KEY (cave_id) REFERENCES cave (cave_id);

ALTER TABLE cave_bebida ADD CONSTRAINT fk_cave_bebida_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

-- Produtor / retalhista
ALTER TABLE produtor ADD CONSTRAINT fk_produtor_pais
    FOREIGN KEY (produtor_pais_id) REFERENCES produtor_pais (produtor_pais_id);

ALTER TABLE regiao ADD CONSTRAINT fk_regiao_pais
    FOREIGN KEY (regiao_pais_id) REFERENCES produtor_pais (produtor_pais_id);

-- FK composta: a região de um produtor tem de ser do país desse produtor (inserir um produtor português com uma
-- região de Espanha falha com ORA-02291 em fk_produtor_regiao). O Oracle não confere FKs com colunas nulas, por isso
-- o CHECK garante que um produtor com região tem sempre país — sem ele, região sem país escapava à verificação.
ALTER TABLE produtor ADD CONSTRAINT fk_produtor_regiao
    FOREIGN KEY (produtor_pais_id, produtor_regiao_id) REFERENCES regiao (regiao_pais_id, regiao_id);

ALTER TABLE produtor ADD CONSTRAINT ck_produtor_regiao_pais
    CHECK (produtor_regiao_id IS NULL OR produtor_pais_id IS NOT NULL);

ALTER TABLE retalhista ADD CONSTRAINT fk_retalhista_tipo
    FOREIGN KEY (retalhista_tipo_id) REFERENCES retalhista_tipo (retalhista_tipo_id);

-- Compra / afiliados
ALTER TABLE bebida_link_compra ADD CONSTRAINT fk_link_compra_bebida
    FOREIGN KEY (bebida_id) REFERENCES bebida (bebida_id);

ALTER TABLE bebida_link_compra ADD CONSTRAINT fk_link_compra_retalhista
    FOREIGN KEY (retalhista_id) REFERENCES retalhista (retalhista_id);

ALTER TABLE clique_compra ADD CONSTRAINT fk_clique_compra_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE clique_compra ADD CONSTRAINT fk_clique_compra_link
    FOREIGN KEY (bebida_link_compra_id) REFERENCES bebida_link_compra (bebida_link_compra_id);

-- Preferências
ALTER TABLE utilizador_preferencia ADD CONSTRAINT fk_utilizador_pref_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE utilizador_categoria_preferida ADD CONSTRAINT fk_utilizador_cat_pref_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE utilizador_categoria_preferida ADD CONSTRAINT fk_utilizador_cat_pref_categoria
    FOREIGN KEY (bebida_categoria_id) REFERENCES bebida_categoria (bebida_categoria_id);

ALTER TABLE utilizador_casta_preferida ADD CONSTRAINT fk_utilizador_casta_pref_utilizador
    FOREIGN KEY (utilizador_id) REFERENCES utilizador (utilizador_id);

ALTER TABLE utilizador_casta_preferida ADD CONSTRAINT fk_utilizador_casta_pref_casta
    FOREIGN KEY (casta_id) REFERENCES casta (casta_id);
