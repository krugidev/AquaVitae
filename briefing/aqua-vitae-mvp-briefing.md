# Aqua Vitae — Briefing para arranque do MVP

## 1. Visão geral do projeto

App Android (Android Studio) para catalogação e comunidade de vinhos e bebidas espirituosas, com:
- Homepage com sugestões personalizadas (com base em onboarding de preferências)
- Filtros por características sensoriais (corpo, acidez, doçura, taninos, etc.)
- Scan de rótulos (fase posterior)
- Reviews e comentários (ratings)
- Catalogação com pesquisa
- Pairing por IA: foto do prato → sugestão de bebidas com explicação por característica (fase posterior)
- Cave Virtual (garrafas que o utilizador possui, organizadas por "caves" próprias)
- Wishlist e Favoritos (distintos entre si e da Cave)
- Histórico de "bebidas provadas"
- Notícias do mundo das bebidas (fase posterior)
- Páginas de produtores/quintas/destilarias (história, localização, visitas)
- Modo prova cega/social — eventos de prova entre convidados (fase posterior)
- Compra via **links de afiliado** para retalhistas/produtores (sem marketplace próprio)

**Diferenciação face a apps existentes (Vivino, Distiller, Delectable):**
- Multi-categoria (vinho + whisky + gin + rum + vodka + aguardente + licor) na mesma app
- Foco no mercado português (produtores nacionais, retalhistas nacionais)
- Conteúdo editorial (storytelling de produtores) combinado com social/reviews
- Pairing por IA com explicação, não só recomendação

## 2. Faseamento definido

**Fase 1 — MVP (este documento):**
Catalogação + pesquisa, perfil de utilizador + onboarding de preferências, página de item completa, reviews, Cave Virtual, Wishlist, Favoritos, ligação a retalhistas via afiliados, páginas de produtor.

**Fase 2 (próximo update, fora deste briefing):**
Categoria `rum` completa, notícias, eventos de prova cega/social, pairing por IA (visão computacional + lógica de sugestão).

**Fase 3:**
Scan de rótulos, otimizações de recomendação, possível expansão internacional.

## 3. Monetização (decisão tomada)

Duas vias prioritárias para o arranque:
1. **Afiliados** — links de compra para retalhistas/produtores (Continente, Pingo Doce se acessível via rede tipo Awin/Rakuten; garrafeiras online; produtores diretos)
2. **Subscrição premium leve** — desbloqueia limites da Cave Virtual, pairing IA ilimitado (fase 2+), sem anúncios

Publicidade display (AdMob) como opção secundária, com restrições fortes do Google Ads para anúncios de álcool.

**Nota legal (não vinculativa, revisar com advogado antes de lançar):** Portugal tem legislação de restrição à venda/consumo de álcool (DL 50/2013, alterado por DL 106/2015) e um código de autorregulação do setor (ICAP) que proíbe visar menores. Verificação de idade no registo é essencial.

## 4. Decisões de arquitetura ainda em aberto (para debater com o utilizador antes/durante o MVP)

- **Backend:** recomendação é API própria + BD Oracle (não só Room local), porque reviews/cave/wishlist precisam sincronizar entre dispositivos. Stack por decidir: PL/SQL packages expostos via API REST fina (aproveita a experiência Oracle do utilizador) vs. stack standard (Node/Spring).
- **Painel de administração:** tabelas de conteúdo (`bebida`, `produtor`, `retalhista`, `bebida_link_compra`, todos os lookups) são geridas só pelo admin (o utilizador), não pelos utilizadores finais da app. Para o MVP, inserção direta via SQL Developer é aceitável; painel web dedicado só se justifica com mais volume/colaboradores.
- **Fluxo de afiliados:** o "código de afiliado" é fornecido pela rede/retalhista e embutido no URL guardado em `bebida_link_compra`; nunca é algo colocado no site deles. Clique do utilizador é registado em `clique_compra` para métricas próprias (a rede de afiliados não dá dados ao nível de utilizador individual).
- **Registo de aquisição na Cave Virtual:** sempre auto-declarado pelo utilizador (não há confirmação real de compra via afiliado). UX prevista: popup pós-clique recente (janela 24-72h, tabela `clique_compra` com flag `perguntado`) + aba/botão permanente "Adicionar à Cave".

## 5. Convenções de modelação usadas no schema

- Nomenclatura: tabelas no singular, PK própria `tabela_id` (increment), colunas prefixadas com o nome da tabela, FKs sem prefixo (ex. `bebida_id`, `utilizador_id`).
- Padrão **supertype/subtype**: `bebida` é a tabela mãe (atributos comuns), cada categoria (`vinho`, `whisky`, `gin`, `vodka`, `aguardente`, `licor`, e futuramente `rum`) tem tabela própria com `bebida_id` como PK **e** FK para `bebida`.
- Atributos que podem ter múltiplos valores em simultâneo (castas, botânicos, casks, sabores de licor, matérias-primas de aguardente) → tabelas associativas N:N com PK própria.
- Atributos de valor único por bebida (corpo, taninos, tipo, região) → FK direto para tabela de lookup, **sempre separada por categoria** (ex. `vinho_corpo`, `whisky_corpo`, `gin_corpo` são tabelas distintas, por decisão consciente do utilizador, mesmo com duplicação de conteúdo).
- Escalas sensoriais numéricas (acidez, doçura, suavidade, defumado): `NUMBER(1)` de 1 a 5.
- `bebida_rating_medio` e `bebida_total_reviews` são campos **derivados/cache**, calculados a partir de `review` (recomenda-se trigger `AFTER INSERT/UPDATE/DELETE ON review`, ainda por implementar).
- Três tabelas de país distintas e propositadamente separadas: `pais` (origem da bebida), `utilizador_nationality` (nacionalidade de utilizador), `produtor_pais` (país do produtor).
- Cinco tabelas de relação utilizador↔bebida, todas independentes entre si (uma bebida pode estar em todas ao mesmo tempo para o mesmo utilizador):
  - `review` (PK própria, `UNIQUE(bebida_id, utilizador_id)` — usada também para verificar se o utilizador já avaliou)
  - `favorito` (PK composta)
  - `bebida_provada` (PK composta, flag simples "já experimentei")
  - `wishlist` (PK composta)
  - `cave` + `cave_bebida` (utilizador pode ter várias caves; uma bebida pode estar em várias caves e várias vezes na mesma cave — por isso `cave_bebida_id` é PK própria, não composta)

## 6. Schema atual (DBML completo)

```dbml
Table utilizador [headercolor: #175e7a] {
	utilizador_id integer [ pk, increment, not null ]
	utilizador_username varchar(30) [ unique ]
	utilizador_first_name varchar(25)
	utilizador_last_name varchar(40)
	utilizador_email varchar(100) [ unique ]
	utilizador_password varchar(160)
	utilizador_nationality_id integer
	utilizador_avatar_photo_id integer
	utilizador_account_created_at timestamp
	utilizador_bio_desc text
	utilizador_role_id integer
}

Table utilizador_nationality [headercolor: #175e7a] {
	nationality_id integer [ pk, increment, not null ]
	nationality_value varchar(30)
}

Table utilizador_avatar [headercolor: #175e7a] {
	avatar_id integer [ pk, increment, not null ]
	avatar_name varchar(50)
	avatar_category_id integer
	avatar_path_image varchar(255)
	avatar_is_active boolean
	avatar_data_criacao timestamp
}

Table avatar_categoria [headercolor: #175e7a] {
	avatar_categoria_id integer [ pk, increment, not null ]
	avatar_categoria_value varchar(20)
}

Table bebida_categoria [headercolor: #175e7a] {
	bebida_categoria_id integer [ pk, increment, not null ]
	bebida_categoria_value varchar(40) [ unique ]
}

Table bebida [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	bebida_category_id integer
	bebida_name varchar(150)
	bebida_producer_id integer
	bebida_pais_origem_id integer
	bebida_ano_producao numeric(4)
	bebida_teor_alcoolico numeric(4,2)
	bebida_volume_ml numeric
	bebida_path_image varchar(255)
	bebida_data_criacao timestamp
	bebida_rating_medio numeric(3,2) [ default: 0 ]
	bebida_total_reviews integer [ default: 0 ]
}

Table pais [headercolor: #175e7a] {
	bebida_pais_id integer [ pk, increment, not null ]
	bebida_pais_value varchar(60) [ unique ]
}

Table vinho [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	vinho_casta_id integer
	vinho_nivel_acidez integer
	vinho_corpo_id integer
	vinho_nivel_docura integer
	vinho_tanino_id integer
	vinho_tipo_id integer
}

Table casta [headercolor: #175e7a] {
	casta_id integer [ pk, increment, not null ]
	casta_name varchar(60)
}

Table vinho_casta [headercolor: #175e7a] {
	vinho_casta_id integer [ pk, increment, not null ]
	vinho_id integer
	casta_id integer
	vinho_casta_percentagem numeric(5,2)
}

Table vinho_corpo [headercolor: #175e7a] {
	vinho_corpo_id integer [ pk, increment, not null ]
	vinho_corpo_value varchar(30) [ unique ]
}

Table vinho_tanino [headercolor: #175e7a] {
	vinho_tanino_id integer [ pk, increment, not null ]
	vinho_tanino_value varchar(60) [ unique ]
}

Table vinho_tipo [headercolor: #175e7a] {
	vinho_tipo_id integer [ pk, increment, not null ]
	vinho_tipo_value integer
}

Table whisky [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	whisky_tipo_id integer
	whisky_regiao_id integer
	whisky_corpo_id integer
	whisky_idade integer
	whisky_is_turfado numeric(1) [ default: 0 ]
	whisky_nivel_docura numeric(1)
	whisky_nivel_defumado numeric(1)
}

Table cask [headercolor: #175e7a] {
	cask_id integer [ pk, increment, not null ]
	cask_value varchar(60)
}

Table whisky_cask [headercolor: #175e7a] {
	whisky_cask_id integer [ pk, increment, not null ]
	whisky_id integer
	cask_id integer
	whisky_cask_estagio numeric
}

Table whisky_tipo [headercolor: #175e7a] {
	whisky_tipo_id integer [ pk, increment, not null ]
	whisky_tipo_value varchar(30) [ unique ]
}

Table whisky_regiao [headercolor: #175e7a] {
	whisky_regiao_id integer [ pk, increment, not null ]
	whisky_regiao_nome varchar(50)
}

Table whisky_corpo [headercolor: #175e7a] {
	whisky_corpo_id integer [ pk, increment, not null ]
	whisky_corpo_value varchar(20)
}

Table gin [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	gin_tipo_destilacao_id integer
	gin_corpo_id integer
	gin_nivel_docura numeric(1)
}

Table gin_corpo [headercolor: #175e7a] {
	gin_corpo_id integer [ pk, increment, not null ]
	gin_corpo_value varchar(20) [ unique ]
}

Table gin_destilacao [headercolor: #175e7a] {
	gin_destilacao_id integer [ pk, increment, not null ]
	gin_destilacao_value varchar(40)
}

Table botanico [headercolor: #175e7a] {
	botanico_id integer [ pk, increment, not null ]
	botanico_name varchar(50)
}

Table gin_botanico [headercolor: #175e7a] {
	gin_botanico_id integer [ pk, increment, not null ]
	gin_id integer
	botanico_id integer
}

Table licor [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	licor_base_id integer
	licor_nivel_docura numeric(1)
}

Table licor_base [headercolor: #175e7a] {
	licor_base_id integer [ pk, increment, not null ]
	licor_base_value varchar(30)
}

Table sabor_licor [headercolor: #175e7a] {
	sabor_licor_id integer [ pk, increment, not null ]
	sabor_licor_value varchar(50)
}

Table licor_sabor [headercolor: #175e7a] {
	licor_sabor_id integer [ pk, increment, not null ]
	licor_id integer
	sabor_licor_id integer
}

Table vodka [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	vodka_materia_prima_id integer
	vodka_num_destilacoes numeric
	vodka_nivel_suavidade numeric(1)
}

Table vodka_materia_prima [headercolor: #175e7a] {
	vodka_materia_prima_id integer [ pk, increment, not null ]
	vodka_materia_prima_value varchar(30) [ unique ]
}

Table vodka_materia_prima_relation [headercolor: #175e7a] {
	vodka_materia_prima_relation_id integer [ pk, increment, not null ]
	vodka_id integer
	vodka_materia_prima_id integer
}

Table aguardente [headercolor: #175e7a] {
	bebida_id integer [ pk, increment, not null ]
	aguardente_materia_prima_id integer
	aguardente_corpo_id integer
	aguardente_idade_anos numeric
}

Table aguardente_corpo [headercolor: #175e7a] {
	aguardente_corpo_id integer [ pk, increment, not null ]
	aguardente_corpo_value varchar(20)
}

Table aguardente_tipo_cask [headercolor: #175e7a] {
	aguardente_tipo_cask_id integer [ pk, increment, not null ]
	aguardente_tipo_cask_value varchar(50)
}

Table aguardente_tipo_cask_relation [headercolor: #175e7a] {
	aguardente_tipo_cask_relation_id integer [ pk, increment, not null ]
	aguardente_id integer
	aguardente_tipo_cask_id integer
	aguardente_tipo_cask_ordem_estagio numeric
}

Table aguardente_materia_prima [headercolor: #175e7a] {
	aguardente_materia_prima_id integer [ pk, increment, not null ]
	aguardente_materia_prima_value varchar(30)
}

Table aguardente_materia_prima_relation [headercolor: #175e7a] {
	aguardente_materia_prima_relation_id integer [ pk, increment, not null ]
	aguardente_id integer
	aguardente_materia_prima_id integer
}

Table review [headercolor: #175e7a] {
	review_id integer [ pk, increment, not null ]
	bebida_id integer
	utilizador_id integer
	rating_value numeric(2,1)
	review_comment_value text
	review_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
	review_data_atualizacao timestamp
}

Table favorito [headercolor: #175e7a] {
	favorito_id integer [ pk, increment, not null ]
	utilizador_id integer
	bebida_id integer
	favorito_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table bebida_provada [headercolor: #175e7a] {
	bebida_provada_id integer [ pk, increment, not null ]
	utilizador_id integer
	bebida_id integer
	bebida_provada_data timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table wishlist [headercolor: #175e7a] {
	wishlist_id integer [ pk, increment, not null ]
	utilizador_id integer
	bebida_id integer
	wishlist_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table cave [headercolor: #175e7a] {
	cave_id integer [ pk, increment, not null ]
	utilizador_id integer
	cave_nome varchar(100)
	cave_descricao varchar(500)
	cave_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table cave_bebida [headercolor: #175e7a] {
	cave_bebida_id integer [ pk, increment, not null ]
	cave_id integer
	bebida_id integer
	cave_bebida_quantidade integer [ default: 1 ]
	cave_bebida_data_aquisicao date
	cave_bebida_preco_pago numeric(8,2)
	cave_bebida_janela_inicio date
	cave_bebida_janela_fim date
	cave_bebida_is_consumida boolean [ default: false ]
	cave_bebida_data_consumo date
	cave_bebida_notas varchar(500)
	cave_bebida_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table produtor [headercolor: #175e7a] {
	produtor_id integer [ pk, increment, not null ]
	produtor_nome varchar(150)
	produtor_pais_id integer
	produtor_regiao varchar(100)
	produtor_historia text
	produtor_ano_fundacao numeric(4)
	produtor_website varchar(255)
	produtor_path_imagem varchar(255)
	produtor_latitude numeric(9,6)
	produtor_longitude numeric(9,6)
	produtor_permite_visitas boolean [ default: false ]
	produtor_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table retalhista_tipo [headercolor: #175e7a] {
	retalhista_tipo_id integer [ pk, increment, not null ]
	retalhista_tipo_value varchar(30) [ unique ]
}

Table retalhista [headercolor: #175e7a] {
	retalhista_id integer [ pk, increment, not null ]
	retalhista_nome varchar(100)
	retalhista_path_logo varchar(255)
	retalhista_website varchar(255)
	retalhista_tipo_id integer
	retalhista_rede_afiliados varchar(50)
	retalhista_codigo_afiliado varchar(100)
	retalhista_comissao_percentagem numeric(5,2)
	retalhista_is_ativo boolean [ default: true ]
	retalhista_data_criacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table utilizador_role [headercolor: #175e7a] {
	utilizador_role_id integer [ pk, increment, not null ]
	utilizador_role_value varchar(30)
}

Table bebida_link_compra [headercolor: #175e7a] {
	bebida_link_compra_id integer [ pk, increment, not null ]
	bebida_id integer
	retalhista_id integer
	bebida_link_compra_url varchar(500)
	bebida_link_compra_preco_atual numeric(8,2)
	bebida_link_compra_data_atualizacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table clique_compra [headercolor: #175e7a] {
	clique_compra_id integer [ pk, increment, not null ]
	utilizador_id integer
	bebida_link_compra_id integer
	clique_compra_data timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
	clique_compra_is_perguntado boolean [ default: false ]
	clique_compra_resposta varchar(20)
}

Table utilizador_preferencia [headercolor: #175e7a] {
	utilizador_preferencia_id integer [ pk, increment, not null ]
	utilizador_id integer
	utilizador_preferencia_acidez_min integer
	utilizador_preferencia_acidez_max integer
	utilizador_preferencia_docura_min integer
	utilizador_preferencia_docura_max integer
	utilizador_preferencia_data_atualizacao timestamp [ default: 'DEFAULT SYSTIMESTAMP' ]
}

Table utilizador_categoria_preferida [headercolor: #175e7a] {
	utilizador_categoria_preferida_id integer [ pk, increment, not null ]
	utilizador_id integer
	bebida_categoria_id integer
}

Table utilizador_casta_preferida [headercolor: #175e7a] {
	utilizador_casta_preferida_id integer [ pk, increment, not null ]
	utilizador_id integer
	casta_id integer
}

Table produtor_pais [headercolor: #175e7a] {
	produtor_pais_id integer [ pk, increment, not null ]
	produtor_pais_value varchar(60) [ unique ]
}

Ref fk_utilizador_utilizador_nationality_id_utilizador_nationality {
	utilizador.utilizador_nationality_id > utilizador_nationality.nationality_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_avatar_photo_id_utilizador_avatar {
	utilizador.utilizador_avatar_photo_id > utilizador_avatar.avatar_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_avatar_avatar_category_id_avatar_categoria {
	utilizador_avatar.avatar_category_id > avatar_categoria.avatar_categoria_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_category_id_bebida_categoria {
	bebida.bebida_category_id > bebida_categoria.bebida_categoria_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_pais_origem_id_pais {
	bebida.bebida_pais_origem_id > pais.bebida_pais_id [ delete: no action, update: no action ]
}

Ref fk_vinho_casta_vinho_id_vinho {
	vinho_casta.vinho_id > vinho.bebida_id [ delete: no action, update: no action ]
}

Ref fk_vinho_casta_casta_id_casta {
	vinho_casta.casta_id > casta.casta_id [ delete: no action, update: no action ]
}

Ref fk_vinho_vinho_corpo_id_vinho_corpo {
	vinho.vinho_corpo_id > vinho_corpo.vinho_corpo_id [ delete: no action, update: no action ]
}

Ref fk_vinho_vinho_tanino_id_vinho_tanino {
	vinho.vinho_tanino_id > vinho_tanino.vinho_tanino_id [ delete: no action, update: no action ]
}

Ref fk_vinho_vinho_tipo_id_vinho_tipo {
	vinho.vinho_tipo_id > vinho_tipo.vinho_tipo_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_vinho {
	bebida.bebida_id - vinho.bebida_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_whisky {
	bebida.bebida_id - whisky.bebida_id [ delete: no action, update: no action ]
}

Ref fk_whisky_cask_whisky_id_whisky {
	whisky_cask.whisky_id > whisky.bebida_id [ delete: no action, update: no action ]
}

Ref fk_whisky_cask_cask_id_cask {
	whisky_cask.cask_id > cask.cask_id [ delete: no action, update: no action ]
}

Ref fk_whisky_whisky_tipo_id_whisky_tipo {
	whisky.whisky_tipo_id > whisky_tipo.whisky_tipo_id [ delete: no action, update: no action ]
}

Ref fk_whisky_whisky_regiao_id_whisky_regiao {
	whisky.whisky_regiao_id > whisky_regiao.whisky_regiao_id [ delete: no action, update: no action ]
}

Ref fk_whisky_whisky_corpo_id_whisky_corpo {
	whisky.whisky_corpo_id > whisky_corpo.whisky_corpo_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_gin {
	bebida.bebida_id - gin.bebida_id [ delete: no action, update: no action ]
}

Ref fk_gin_bebida_id_gin_botanico {
	gin.bebida_id < gin_botanico.gin_id [ delete: no action, update: no action ]
}

Ref fk_gin_botanico_botanico_id_botanico {
	gin_botanico.botanico_id > botanico.botanico_id [ delete: no action, update: no action ]
}

Ref fk_gin_gin_tipo_destilacao_id_gin_destilacao {
	gin.gin_tipo_destilacao_id > gin_destilacao.gin_destilacao_id [ delete: no action, update: no action ]
}

Ref fk_gin_gin_corpo_id_gin_corpo {
	gin.gin_corpo_id > gin_corpo.gin_corpo_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_licor {
	bebida.bebida_id - licor.bebida_id [ delete: no action, update: no action ]
}

Ref fk_licor_licor_base_id_licor_base {
	licor.licor_base_id > licor_base.licor_base_id [ delete: no action, update: no action ]
}

Ref fk_licor_bebida_id_licor_sabor {
	licor.bebida_id < licor_sabor.licor_id [ delete: no action, update: no action ]
}

Ref fk_licor_sabor_sabor_licor_id_sabor_licor {
	licor_sabor.sabor_licor_id > sabor_licor.sabor_licor_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_vodka {
	bebida.bebida_id - vodka.bebida_id [ delete: no action, update: no action ]
}

Ref fk_vodka_bebida_id_vodka_materia_prima_relation {
	vodka.bebida_id < vodka_materia_prima_relation.vodka_id [ delete: no action, update: no action ]
}

Ref fk_vodka_materia_prima_vodka_materia_prima_id_vodka_materia_prima_relation {
	vodka_materia_prima.vodka_materia_prima_id < vodka_materia_prima_relation.vodka_materia_prima_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_aguardente {
	bebida.bebida_id - aguardente.bebida_id [ delete: no action, update: no action ]
}

Ref fk_aguardente_aguardente_corpo_id_aguardente_corpo {
	aguardente.aguardente_corpo_id > aguardente_corpo.aguardente_corpo_id [ delete: no action, update: no action ]
}

Ref fk_aguardente_tipo_cask_relation_aguardente_id_aguardente {
	aguardente_tipo_cask_relation.aguardente_id > aguardente.bebida_id [ delete: no action, update: no action ]
}

Ref fk_aguardente_tipo_cask_relation_aguardente_tipo_cask_id_aguardente_tipo_cask {
	aguardente_tipo_cask_relation.aguardente_tipo_cask_id > aguardente_tipo_cask.aguardente_tipo_cask_id [ delete: no action, update: no action ]
}

Ref fk_aguardente_materia_prima_relation_aguardente_id_aguardente {
	aguardente_materia_prima_relation.aguardente_id > aguardente.bebida_id [ delete: no action, update: no action ]
}

Ref fk_aguardente_materia_prima_aguardente_materia_prima_id_aguardente_materia_prima_relation {
	aguardente_materia_prima.aguardente_materia_prima_id < aguardente_materia_prima_relation.aguardente_materia_prima_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_review {
	bebida.bebida_id < review.bebida_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_id_review {
	utilizador.utilizador_id < review.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_id_favorito {
	utilizador.utilizador_id < favorito.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_favorito_bebida_id_bebida {
	favorito.bebida_id > bebida.bebida_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_id_bebida_provada {
	utilizador.utilizador_id < bebida_provada.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_bebida_provada {
	bebida.bebida_id < bebida_provada.bebida_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_id_wishlist {
	utilizador.utilizador_id < wishlist.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_wishlist {
	bebida.bebida_id < wishlist.bebida_id [ delete: no action, update: no action ]
}

Ref fk_cave_bebida_cave_id_cave {
	cave_bebida.cave_id > cave.cave_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_id_cave_bebida {
	bebida.bebida_id < cave_bebida.bebida_id [ delete: no action, update: no action ]
}

Ref fk_bebida_bebida_producer_id_produtor {
	bebida.bebida_producer_id > produtor.produtor_id [ delete: no action, update: no action ]
}

Ref fk_retalhista_retalhista_tipo_id_retalhista_tipo {
	retalhista.retalhista_tipo_id > retalhista_tipo.retalhista_tipo_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_utilizador_role_id_utilizador_role {
	utilizador.utilizador_role_id > utilizador_role.utilizador_role_id [ delete: no action, update: no action ]
}

Ref fk_bebida_link_compra_bebida_id_bebida {
	bebida_link_compra.bebida_id > bebida.bebida_id [ delete: no action, update: no action ]
}

Ref fk_bebida_link_compra_retalhista_id_retalhista {
	bebida_link_compra.retalhista_id > retalhista.retalhista_id [ delete: no action, update: no action ]
}

Ref fk_clique_compra_utilizador_id_utilizador {
	clique_compra.utilizador_id > utilizador.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_clique_compra_bebida_link_compra_id_bebida_link_compra {
	clique_compra.bebida_link_compra_id > bebida_link_compra.bebida_link_compra_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_categoria_preferida_utilizador_id_utilizador {
	utilizador_categoria_preferida.utilizador_id > utilizador.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_categoria_preferida_bebida_categoria_id_bebida_categoria {
	utilizador_categoria_preferida.bebida_categoria_id > bebida_categoria.bebida_categoria_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_casta_preferida_utilizador_id_utilizador {
	utilizador_casta_preferida.utilizador_id > utilizador.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_casta_preferida_casta_id_casta {
	utilizador_casta_preferida.casta_id > casta.casta_id [ delete: no action, update: no action ]
}

Ref fk_utilizador_preferencia_utilizador_id_utilizador {
	utilizador_preferencia.utilizador_id > utilizador.utilizador_id [ delete: no action, update: no action ]
}

Ref fk_produtor_produtor_pais_id_produtor_pais {
	produtor.produtor_pais_id > produtor_pais.produtor_pais_id [ delete: no action, update: no action ]
}
```

## 7. Pedido para o Claude Code

Com base neste schema e contexto:

1. Gerar o SQL Oracle correspondente a partir do DBML acima (tipos de dados Oracle: `NUMBER` em vez de `integer`/`numeric`, `VARCHAR2` em vez de `varchar`, `SYSTIMESTAMP` para defaults, etc.).
2. Adicionar a trigger de sincronização `bebida_rating_medio` / `bebida_total_reviews` a partir de `review` (AFTER INSERT/UPDATE/DELETE).
3. Propor a estrutura de projeto Android (Kotlin, arquitetura MVVM sugerida) para os ecrãs do MVP, na ordem de dependência:
   registo/login + onboarding de preferências → catálogo/pesquisa → página de detalhe da bebida → reviews → cave virtual/wishlist/favoritos.
4. Ajudar a decidir e estruturar a camada de backend/API (ainda em aberto — ver secção 4) antes de avançar para o consumo de dados no Android.
5. Gerar scripts de seed/dados de teste (15-20 bebidas reais portuguesas, lookups preenchidos) para desenvolvimento.

Este ficheiro serve de contexto persistente do projeto — outras decisões (rum, notícias, eventos de prova, pairing por IA) serão trabalhadas em updates futuros e não fazem parte deste MVP.
