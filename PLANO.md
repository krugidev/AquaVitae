# AquaVitae — Plano de trabalho

Roadmap vivo do MVP. Atualizar isto no fim de cada sessão de trabalho relevante — é o que uma sessão
nova do Claude Code (ou tu, ao voltares passado um tempo) deve ler primeiro para saber onde ficámos.

## ▶ Retomar aqui (última atualização: 2026-09-21)

**Ponto da situação:** **o backend da 1.ª versão está fechado.** Todos os endpoints do contrato estão implementados e
**validados ao vivo** contra o Oracle real, e a BD reconstruída do zero (num schema temporário, com os 5 scripts do
`run-migrations`) é idêntica à de dev. Em 2026-09-20 e 21 fecharam-se as decisões que faltavam: EAN, imagens (URL do
retalhista), lista final de regiões (também para o whisky), Portugal no topo dos países, grafias PT-PT e a data de
aceitação dos termos — detalhe em "Fecho do backend v1", mais abaixo. O que ficou fora do backend está adiado de propósito
(ver "Por fazer depois"). Falta só o **script de verificação do catálogo**, que se escreve com o 1.º lote de bebidas. O
próximo grande passo é o **Android**. Contrato dos endpoints em [`backend/API_ENDPOINTS.md`](backend/API_ENDPOINTS.md).

**Git:** branch `feature/api-endpoints-design`, PR aberto: https://github.com/krugidev/AquaVitae/pull/1. **Tudo commitado e
enviado em 2026-09-21** (último commit: "Fecho do backend v1: ...", ver `git log`) e a **descrição do PR atualizada** (cobre a
branch inteira, não só a 1.ª fatia). De fora dos commits, de propósito (regra do `CLAUDE.md`: não commitar as notas do
utilizador): `android/AQUAVITAESEEDS-NOTES` e, na raiz, `—--------------- DADOS A INSERIR NA.txt`, que estão **em stage**, e
`REGIÕES A AJUSTAR (...).txt` (a lista de regiões que enviaste, por rastrear). Se as quiseres no repo, commita-as tu (ou diz-me).
Ao commitar, usar `git commit <caminhos explícitos>`. A `main` só tem a landing page em `docs/` (GitHub Pages). Sobre o `gh`, ver
`CLAUDE.md` ("Ferramentas").

**Ambiente, como ficou:** BD de dev migrada até ao patch `12`; contentor `aquavitae-oracle-xe` a correr; API parada
(`bootRun` em `backend/`); contas de teste `demo2@aquavitae.local` (1 review na bebida 1, 1 "provada", sem avatar) e
`demo@aquavitae.local`, ambas **sem termos aceites** (`NULL`: são anteriores aos termos, por isso `precisaAceitarTermos =
true`). Se o Windows reiniciou: `docker compose up -d` em `database/` (e ver em `CLAUDE.md` o que fazer se o Docker Desktop
crashar). Testes: `.\gradlew.bat test` em `backend/` (69, não precisam de BD).

**Próximos passos, por ordem:**
1. **Android** — sincronizar o `AquaVitaeApi.kt` com o contrato (está desatualizado: reviews, favoritos/wishlist/provadas,
   filtros, regiões, Caves, termos, campos novos — ver o fim do `API_ENDPOINTS.md`) e construir os ecrãs por feature.
   Proposta de ordem: **registo/login em 2 fases com o popup dos termos** (o registo envia `aceitouTermos`; no login o
   `getMe()` diz se `precisaAceitarTermos`; o texto está em `GET /legal/termos.html`) → onboarding → catálogo → detalhe. O
   Android tem de aceitar **URL absoluto** nas imagens (ver "Imagens" no contrato). Ecrã "Onde comprar" desenhado em
   conversa (lista de retalhistas com disponível/indisponível, aviso de afiliação). **O registo que o Android faz hoje dá
   400** até o popup existir (campo novo obrigatório).
2. **1.º lote de ~30 bebidas da Awin** (em paralelo com o Android; não depende dele) — fluxo em "Como entram as bebidas"
   (logo abaixo). Escrevo então o **script de verificação do catálogo** (aprovado: mínimo por categoria = nome, categoria,
   produtor, país, teor, volume; vinho + tipo e castas; whisky + tipo e idade; gin + destilação; bebida sem linha no
   subtype da categoria e vice-versa; e whisky com região de um país diferente do país de origem da bebida). Antes, **tu**
   avalias os termos dos programas escolhidos na Awin (comparação de preços; uso das imagens do feed).
3. **Termos e condições — a concluir quando tiveres o texto (tu avisas):** substitui-se
   `backend/src/main/resources/static/legal/termos.html` (hoje é só um marcador "versão provisória") e fica a implementação
   concluída. O texto deve incluir a maioridade 18+ e o aviso de afiliação/comissões; convém revisão jurídica. Não bloqueia o
   Android, mas é preciso antes do lançamento (a Google Play também exige uma política de privacidade com URL público).
4. **Adiado — "Por fazer depois":** decidido em 2026-09-21 que se trabalha nessa lista **quando a 1.ª versão da app estiver
   pronta**. Inclui os subtypes das outras categorias (**nota:** se o 1.º lote trouxer gins/whiskies, o detalhe só mostra os
   campos gerais da bebida, sem os atributos da categoria), o email do código de recuperação de password, a pesquisa de
   produtores à parte, o `Page<...>` como DTO próprio, os testes com BD, a pesquisa sem distinção de acentos e o CI/Dockerfile.

**Como entram as bebidas (combinado em 2026-09-19):** as linhas `bebida` criam-se **à mão, em lotes** (~30), não
automaticamente a partir do feed da Awin. Da Awin vêm os **links de compra** (uma bebida pode ter vários, de retalhistas
diferentes — o schema já o permite) e a informação que ela traz (nome, marca, preço, volume, EAN, imagem, ...). Os
atributos que a Awin não traz (corpo, castas, botânicos, ...) só se preenchem se o utilizador os tiver; senão ficam `NULL`.
Porquê semi-manual: um feed traz acessórios, nomes inconsistentes e a mesma bebida em vários retalhistas (duplicados), e o
catálogo é curado. Fluxo de cada lote:
1. O utilizador escolhe os produtos na Awin e envia, **por produto**, o **link da página do produto no retalhista (sem
   tracking)** e o **link de afiliado** (Link Builder da Awin) — ou, melhor, as linhas do **feed de produtos** desses
   artigos (CSV) —, mais o que já souber (casta, região, ...).
2. O Claude **nunca abre o link de afiliado** (conta como clique); lê só a página do produto sem tracking (se o retalhista
   bloquear, o utilizador cola o texto).
3. O Claude devolve uma **tabela de revisão**: bebida → valores (categoria, tipo, produtor, país, região, ano, teor,
   volume, preço; o resto `NULL`; dúvidas assinaladas) e assinala **duplicados** (bebida já no catálogo, reconhecida pelo **EAN** quando o houver, senão por nome + produtor +
   volume → só um link novo); a tabela mostra também o EAN e o URL da imagem de cada bebida.
4. Aprovada a tabela, o Claude gera o **ficheiro SQL do lote** (padrão supertype/subtype de `database/seed/02_bebidas.sql`;
   produtores e regiões novos primeiro; retalhista se for novo; `bebida_link_compra` com **as duas URLs**: `url` =
   afiliado, `url_verificacao` = página do produto; `bebida_ean` só com dígitos, sem espaços nem hífenes;
   `bebida_path_image` = URL da imagem do retalhista/feed), corre-o e, no fim, `POST /api/admin/links-compra/verificar`.
5. Anota-se aqui o que ficou por preencher. As regiões novas acrescentam-se ao `regiao` à medida que aparecem.

**Awin — estado:** já és afiliado (dito por ti em 2026-09-19; candidatura submetida como Comparison Engine, sector "Wine,
spirits and tobacco"). Falta saber a que anunciantes estás ligado e se queres importar feeds no futuro. Mapeamento
previsto, **a confirmar com um feed real**: link de afiliado → `bebida_link_compra_url`; página do produto sem tracking →
`..._url_verificacao`; preço → `..._preco_atual`; stock → `is_ativo`. O URL de um feed leva uma chave: vai para variável de
ambiente, **nunca para o repo**. Automatizar mais tarde só o **preço e o stock dos links já existentes**, não a criação de
bebidas. Site enviado à Awin: `https://krugidev.github.io/AquaVitae/` — **não mudar o nome do repositório nem pôr "Custom
domain" nas definições do Pages** (aconteceu por engano uma vez e desviou o site; já revertido).

## Feito ✅

### Base de dados (validada de ponta a ponta, 2026-09-13/14)
- Schema Oracle completo gerado a partir do DBML do briefing: 53 tabelas, 60 foreign keys, trigger de
  sincronização de rating (`review` → `bebida.bebida_rating_medio`/`bebida_total_reviews`).
- Seed com 16 bebidas portuguesas reais (vinho, gin, licor, aguardente) + todos os lookups preenchidos.
- `docker-compose.yml` (Oracle XE local) + scripts de migração testados a correr do zero.
- Duas correções ao schema original: `vinho_tipo_value` de `integer` para `VARCHAR2`;
  `review_comment_value`/`utilizador_bio_desc` de `CLOB` para `VARCHAR2` limitado.
- Confirmado: contagens corretas, join supertype/subtype (vinho) correto, trigger testada nos dois
  sentidos (INSERT e DELETE).

### Backend (validado de ponta a ponta, 2026-09-14)
- Kotlin + Spring Boot: auth (JWT), catálogo de bebidas (com detalhe de vinho), produtor, reviews,
  favoritos, wishlist, "provadas", cave virtual, preferências de onboarding.
- Gradle wrapper gerado e commitado; app testada a arrancar mesmo contra a BD real.
- Ciclo completo POST/GET/PUT/DELETE demonstrado ao vivo (recurso Review).
- Dois bugs reais encontrados e corrigidos ao correr de verdade (ver `CLAUDE.md` → Convenções):
  mapeamento Double/BigDecimal, e `LazyInitializationException` mascarada de 401 no filtro JWT.
- VS Code configurado com Extension Pack for Java + Gradle for Java.

### Android
- Esqueleto de projeto criado (Compose + MVVM + Hilt + Retrofit/Moshi + DataStore), estrutura de
  pacotes por feature, `AquaVitaeApi` com o contrato completo dos endpoints já existentes.
- Ecrãs `auth` e `catalog` com lógica real ligada à API; os restantes são placeholders de UI já ligados
  aos repositórios correspondentes, ainda por desenhar/implementar a sério.

## Em curso 🔜

### Desenho dos endpoints (concluído, 2026-09-17)

Mockups Figma (29 ecrãs + 2 popups) lidos na íntegra e cruzados com o schema Oracle + backend atual.
Contrato completo (existente vs. a ajustar vs. novo) em [`backend/API_ENDPOINTS.md`](backend/API_ENDPOINTS.md)
— é a fonte da verdade a partir de agora, substitui a secção "Endpoints principais" do `backend/README.md`
assim que os itens novos forem implementados.

Decisões tomadas (detalhe em `API_ENDPOINTS.md`): registo em 2 fases (POST /register só com
username/email/password; resto do wizard via PUT /api/users/me + PUT preferencias, já autenticado);
produtor em destaque computado por rotação semanal (sem coluna nova); `casta` passa a ter `casta_tipo`
(tinta/branca); nova tabela `utilizador_password_reset` para o fluxo de recuperação por código.

### Camada BD para os novos endpoints (concluída, 2026-09-17)

- Tabela de lookup `casta_tipo` (Tinta/Branca) com `casta.casta_tipo_id` como FK — consistente com o
  padrão do resto do schema (`vinho_corpo`, `vinho_tanino`, ...), em vez de uma coluna solta (ajustado a
  pedido do utilizador depois da 1ª versão) — e tabela `utilizador_password_reset`. Ambas adicionadas a
  `01_tables.sql`/`02_constraints.sql` (fonte da verdade) e aplicadas à BD de dev via
  `database/ddl/04_patch_endpoints.sql` (script one-off, não faz parte do `run-migrations.ps1/.sh` — ver
  aviso no topo do próprio ficheiro).
- As 10 castas seed classificadas (Touriga Nacional/Franca, Tinta Roriz, Baga, Trincadeira, Castelão =
  tintas; Alvarinho, Arinto, Fernão Pires, Loureiro = brancas).
- **Mojibake corrigido (2026-09-17).** Causa raiz: `docker exec ... | sqlplus` sem `NLS_LANG` definido —
  o cliente assumia um characterset errado para os bytes UTF-8 vindos do Windows (a BD em si já era
  `AL32UTF8`, só o cliente sqlplus interpretava mal). `run-migrations.ps1`/`.sh` agora passam
  `-e NLS_LANG=AMERICAN_AMERICA.AL32UTF8` ao `docker exec`, prevenindo recorrência em rebuilds futuros.
  Dados já corrompidos na BD de dev corrigidos via UPDATE, em duas passagens (a 2ª apanhou tabelas que a
  1ª varredura tinha deixado escapar — o padrão "â€"/travessão corrompido é diferente do padrão "Ã"):
  `bebida`, `produtor` (nome + `produtor_regiao`), `utilizador_nationality`, `pais`, `produtor_pais`,
  `vinho_tipo`/`vinho_corpo`/`vinho_tanino`, `whisky_corpo`, `gin_corpo`, `botanico`, `licor_base`,
  `sabor_licor`, `aguardente_corpo`/`aguardente_tipo_cask`/`aguardente_materia_prima`, `avatar_categoria`,
  `utilizador_avatar`, além das castas já corrigidas antes. Confirmado que nenhum `vinho.vinho_corpo_id`/
  `vinho_tanino_id` ficou NULL por falha de match no seed (o SELECT de lookup corria na mesma sessão
  corrompida, por isso batia certo consigo mesmo). Os ficheiros `.sql` de seed em si nunca estiveram
  corrompidos — só os dados inseridos na BD.

### Backend — fatia 1: Lookups + Perfil/Preferências (concluída, 2026-09-17)

- `LookupController` novo (`/api/lookup/nacionalidades`, `/avatar-categorias`, `/avatares?categoriaId=`,
  `/categorias-bebida`, `/castas?tipoId=`, `/casta-tipos`, `/paises`, `/regioes?paisId=` — paisId aqui é
  `produtor_pais_id`, sem tabela nova, via `DISTINCT` —, `/vinho/corpos`, `/vinho/taninos`, `/vinho/tipos`).
  Entidades novas `CastaTipo`, `AvatarCategoria`, `UtilizadorAvatar`; `Casta` ganhou a relação `tipo`.
- `Utilizador` passou a mapear o avatar (`utilizador_avatar_photo_id`); `UtilizadorRepository.findByIdWithProfile`
  (fetch explícito de `nationality`+`avatar`, mesmo padrão do `findByIdWithRole` — ver convenção no
  `CLAUDE.md`).
- `GET /api/users/me` enriquecido (nacionalidade, bio, avatar, data de criação, contadores de
  provadas/reviews/favoritos/wishlist/caves/garrafas) + `PUT /api/users/me` novo — ambos em
  `UtilizadorService` novo (antes toda a lógica estava inline no controller).
- `GET /api/users/me/preferencias` novo (faltava para pré-popular o onboarding/editar preferências).
- Validado a correr de verdade contra a BD de dev (`bootRun` + Hibernate `ddl-auto: validate` sem erros
  + `curl` a todos os endpoints novos, incluindo `PUT /me` com nationality/avatar/bio).

### Avatares reais integrados (concluída, 2026-09-18)

- 27 SVGs (desenho de linha, 9 por categoria: Castas/Garrafas/Copos) entregues pelo utilizador, com README
  de spec de design (cores, escalas, estados) — tudo em
  `backend/src/main/resources/static/icones/avatares/`, servido em `GET /icones/avatares/<slug>.svg`.
- Seed (`database/seed/01_lookups.sql`) e BD de dev atualizados: `avatar_categoria` trocou o placeholder
  do esqueleto pelas 3 categorias reais; `utilizador_avatar` tem agora os 27 avatares reais.
- `SecurityConfig` liberta `GET /icones/**` sem auth (faltava — 401 apanhado e corrigido ao testar).
- Validado a servir com `content-type: image/svg+xml` correto via `curl`.
- Pasta de staging `AVATARESAQUAVITAE/` (só tinha o zip de entrega) removida da raiz do projeto depois de
  integrado — nunca chegou a ser commitada.

**Branch `feature/api-endpoints-design`** com todo este trabalho (desenho + camada BD + fatia 1 do
backend + avatares). PR aberto em https://github.com/krugidev/AquaVitae/pull/1 — `gh` CLI instalado e
autenticado nesta máquina a partir de 2026-09-17, próximos PRs podem ser abertos diretamente.

### Backend — fatia 2: auth de password + catálogo/bebida completo (concluída, 2026-09-18)

- **Recuperação de password**: `POST /api/auth/recuperar-password`, `/verificar-codigo`,
  `/redefinir-password` — código de 6 dígitos, 15 min de validade, `PasswordResetService` novo. Envio de
  email continua adiado (só log por agora); fluxo completo testado (código certo/errado/expirado/reusado).
- **Módulo `compra` novo** (não existia nada): `Retalhista`, `BebidaLinkCompra`, `CliqueCompra` +
  `GET /api/bebidas/{id}/links-compra` + `POST .../clique`.
- **`BebidaSummaryAssembler` novo** (`bebida` package): enriquece qualquer lista de bebidas (preço mais
  barato, corpo/acidez/doçura do vinho, flags `isFavorito/isWishlist/isProvada` + `notaPropria` do
  utilizador autenticado) sempre com queries em lote — usado por catálogo, sugeridas, favoritos, wishlist
  e provadas.
- **`GET /api/bebidas`**: filtros completos do popup de filtros (categorias, país, preço, rating,
  acidez/doçura, corpo/tanino/tipo de vinho, castas) — join "ad hoc" JPQL a `Vinho`/`VinhoCasta`.
- **`GET /api/bebidas/sugeridas`** novo (auth) — usa preferências do utilizador, cai para o catálogo
  inteiro se não houver preferências definidas (nunca vazio).
- **`GET /api/bebidas/{id}`**: produtor resumido, link de compra mais barato, flags do utilizador.
- **`GET /api/bebidas/{id}/reviews`**: agora devolve `{ distribuicao, reviews }`; `POST` de review passou
  a exigir `bebida_provada` (409 se não provou ainda).
- **Favoritos/wishlist/provadas**: GET passam a devolver `BebidaRelacaoDto` (bebida + data + hasReview);
  wishlist aceita `?sort=`, provadas aceita `?categoriaId=&ano=`.
- Tudo testado a correr contra a BD de dev real (filtros, sugeridas, fluxo de review bloqueado/permitido,
  reset de password ponta a ponta, contadores do perfil a bater certo com a trigger da BD).
- **Mojibake, 3º padrão apanhado**: "Â°" (símbolo de grau, ex. "Gin 44Â°") — padrão diferente de "Ã" e de
  "â€"; 2 registos corrigidos (`bebida`, `produtor`). Os 3 padrões já cobertos deviam apanhar a maioria dos
  casos, mas vale a pena um `LIKE '%Â%' OR LIKE '%Ã%'` de vez em quando ao mexer em dados antigos.
- Contrato Android (`AquaVitaeApi.kt`) ficou desatualizado em vários pontos (reviews, favoritos/wishlist/
  provadas, filtros do catálogo) — sincronizar só quando chegar a vez de construir esses ecrãs (ver secção
  própria no `API_ENDPOINTS.md`), não antes, para não quebrar o desenvolvimento sequencial.

- **Bug meu apanhado no fim do dia:** a pesquisa do catálogo ficou sem `ORDER BY` quando foi reescrita com os
  filtros, o que tornava a paginação não determinística. `BebidaService` usa agora a ordenação do mockup
  (`ratingMedio` desc, `nome`, `id`) quando o cliente não pede outra; `sort=` do cliente continua a ser
  respeitado. Verificado: 3 páginas, 16 bebidas, sem repetições.

(Próximos passos: ver "Retomar aqui", no topo.)

### Backend — fatia 3: verificação diária dos links de compra (concluída, 2026-09-18)

Ideia do utilizador: um job diário que verifica se os links continuam válidos e esconde o botão de compra
quando já não existem, mantendo a linha como histórico. Detalhe do funcionamento em `API_ENDPOINTS.md`
("Verificação diária dos links").

- **BD** (`05_patch_link_verificacao.sql`, aplicado à BD de dev; `01_tables.sql` atualizado): 6 colunas novas em
  `bebida_link_compra` — `url_verificacao`, `is_ativo` (`NOT NULL`, default 1), `data_verificacao`,
  `falhas_seguidas` (`NOT NULL`, default 0), `data_indisponivel`, `motivo`. Os links já existentes ficam ativos.
- **Decisão importante — não se pede o link de afiliado:** o job usa `url_verificacao` (página do produto sem
  tracking), porque pedir o link de tracking todos os dias contaria como cliques. **Ao preparar o seed:** para
  retalhistas com rede de afiliados, cada link precisa de **duas URLs** (a de afiliado em `url` e a do produto em
  `url_verificacao`); sem a segunda, o link nunca é verificado. Retalhistas sem rede (link direto) usam `url`.
- **API:** `GET /links-compra` devolve também os indisponíveis (marcados, no fim); `precoDesde`, `linkCompra` e os
  filtros de preço só contam links disponíveis; o clique num link indisponível dá 409.
- **Job** (`compra/verificacao/`): `@Scheduled` às 04:00 (`Europe/Lisbon`), configurável; `POST
  /api/admin/links-compra/verificar` (só admin) corre-o na hora — usar depois de cada lote de seed.
  Salvaguarda contra execuções em massa suspeitas; desativa à 2ª noite seguida com link inválido, mas logo se
  `OutOfStock`; reativa sozinho se a página voltar.
- **Primeiros testes automáticos do projeto:** 21 (`DisponibilidadeParserTest` 11, `LinkVerificadorTest` 10, este
  contra um servidor HTTP local do JDK: 404/410/403/500, soft 404, redirects, ligação recusada, User-Agent).
- **Validado de ponta a ponta** contra a BD real com um retalhista falso local: scheduler a disparar sozinho, link
  sem stock desativado logo, 404 e soft 404 só à 2ª falha, 403 ignorado, link de retalhista com rede verificado
  pelo `url_verificacao` (tracking nunca pedido), regresso automático a ativo, `precoDesde` a ignorar links
  indisponíveis, clique em indisponível = 409. Dados de teste removidos depois.
- **Bug antigo apanhado de caminho:** um utilizador autenticado sem permissão recebia **401 em vez de 403**
  (o reencaminhamento interno para `/error` não passa pelo filtro JWT). Corrigido em `SecurityConfig`
  (`dispatcherTypeMatchers(ERROR).permitAll()`); só era visível agora por ser o 1.º endpoint com um papel exigido.
- **Ainda por fazer:** atualizar o preço a partir do JSON-LD (já é lido, não é usado); para links da Awin, o
  melhor sinal de "ainda existe" será o próprio feed (produto saiu do feed / `in_stock = 0`) quando houver
  aprovação; o ecrã Android "Onde comprar" (ver secção seguinte do `API_ENDPOINTS.md` quando for a vez dele).

### Seeds das notas + barrica do vinho (concluída, 2026-09-19)

Fonte: `android/AQUAVITAESEEDS-NOTES`. Aplicado à BD de dev por `database/ddl/06_patch_seed_notas.sql` (repetível) e
refletido em `seed/01_lookups.sql` / `02_bebidas.sql` (rebuild do zero). **Validado:** reconstrução do zero num schema
temporário e patch sobre o estado antigo dão conteúdo idêntico (916 linhas de dump, incluindo os atributos das 16
bebidas demo), 0 mojibake, 25 testes verdes, API a arrancar com `ddl-auto: validate` e endpoints testados por `curl`.

- **Castas: 277** (145 tintas + 132 brancas). As notas têm 280: ficaram de fora 3 entradas "de enchimento" (`Tinto Sem
  Nome`, `Branco Desconhecido`, `Branco Especial`). `Tinta Roriz` = `Aragonez` → uma só linha `Aragonez (Tinta Roriz)`
  (mantém a ligação aos 5 vinhos demo e a pesquisa por casta acha ambos os nomes).
- **Países: 218** (as notas têm 222, e a lista `PAÍS` é igual à `PRODUTOR_PAÍS`): grafias em PT-PT e duplicados fundidos
  (Holanda = Países Baixos, Suazilândia = Essuatíni, `Congo`, Estados Federados da Micronésia). **Mantidas as duas
  tabelas** (decisão tua) com os mesmos ids — receita e verificação em `database/README.md`. As sequências identity da
  dev tinham divergido (inserts revertidos, cache, reinícios): o 1.º patch falhou na minha própria verificação e foi
  revertido; o patch final copia ids explícitos e reinicia o identity. Só corrigi erros claros (acentos em falta,
  "ê/ô" brasileiros) e duplicados; deixei como estavam formas menos óbvias que têm variante PT-PT (Botswana/Botsuana,
  Kuwait/Koweit, Djibouti/Jibuti, Quirguistão/Quirguizistão, Uzbequistão/Usbequistão, ...) — dizes se as queres trocadas.
  **Trocadas em 2026-09-20** (10 nomes), ver "Fecho do backend v1".
- **Barrica do vinho:** `cask_formato` (7) + `vinho_cask` (vinho, madeira, formato, meses de estágio) — só BD, sem
  entidade nem endpoint (nenhum mockup mostra barrica). `cask` passou a ter as 6 madeiras do vinho; saíram os 4
  placeholders de whisky (voltam quando houver whisky real).
- **Lookups trocados** (dados demo remapeados no sítio, ids e relações preservados): botânicos = famílias (Zimbro,
  Citrino, Especiaria e Picante, Floral, Herbáceo e Verde, Terroso, Frutado); destilação do gin (Alambique, Infusão de
  Vapor, Vácuo, Composto); base de licor (Neutra, Vodka, Vínica, Rum, Whisky, Tequila, Mezcal) — as 2 ginjinhas demo
  ficaram com base `NULL` por não terem equivalente; sabores de licor (17); aguardente: base = notas + `Bagaço de
  Uva` e `Vinho` (acrescentados por decisão tua), 15 tipos de cask, e `Inox (sem estágio)` deixou de existir (sem
  barrica = sem linhas na relação); `vinho_tipo`: `Verde` → `Frisante` + `Sobremesa`; whisky: tipos e regiões das
  notas; vodka: 12 matérias-primas. `Moderador` **não** inserido (as notas dizem "futuramente").
- **Bug do runner (não era só o `NLS_LANG`):** `run-migrations.ps1` corrompia acentos no Windows PowerShell 5.1
  (`Get-Content` lê UTF-8 sem BOM como ANSI). Corrigido com `-Encoding UTF8`; nota em `database/README.md` e `CLAUDE.md`.
- **API:** `/lookup/paises`, `/castas` e `/regioes` passam a vir por ordem alfabética (collator pt-PT — o Oracle ordena
  por código de carácter e poria "África" depois de "Zimbabué"; `OrdemAlfabetica.kt`, +4 testes). **Portugal no topo**
  da lista de países: decidido e feito em 2026-09-20, ver "Fecho do backend v1".
- **Pendente:** `Vinho` (entidade/DTO) ainda não expõe a barrica; whisky/vodka/gin/licor/aguardente continuam sem subtype
  no backend. (Os valores compostos de região dos demo, como `Vinho Verde — Melgaço`, ficaram resolvidos em 2026-09-19:
  as regiões passaram a lookup, ver "Regiões como lookup".)

### Backend — fatia 4: Produtor, Caves, reviews e catálogo (implementada e validada ao vivo, 2026-09-19)

Decisões tuas (2026-09-19): **(1)** garrafa cuja janela acabou sem ser consumida = continua na lista das prontas com um aviso
vermelho pequeno "Em atraso"; **(2)** "marcar como consumida" = consumir **uma** garrafa (decrementa `quantidade` e guarda a
consumida numa linha própria, com `data_consumo`, para não perder histórico); **(3)** o filtro do catálogo segue o mockup
do popup de Filtros (origem = país + regiões que mudam com o país). Contrato completo em `backend/API_ENDPOINTS.md`.

- **Produtor:** `GET /api/produtores/{id}` ganha `totalProdutos`; `GET /api/produtores/{id}/bebidas?categoriaId=`;
  `GET /api/produtores/destaque` (rotação semanal pelos 10 melhores por rating médio). Passou a ter `ProdutorService`.
- **Caves:** `estado` de cada garrafa (`EM_GUARDA`/`PRONTA`/`EM_ATRASO`/`CONSUMIDA`, calculado ao ler), totais
  (`totalGarrafas`, `valorTotal`, `totalProntasAAbrir`), detalhe separado em `prontasAAbrir[]`/`emGuarda[]` com `?sort=`,
  janelas na criação e no `PATCH`, e `POST .../consumir`. **Quebra o `PATCH`:** deixou de aceitar `isConsumida`/`dataConsumo`.
  As colunas já existiam na BD — **sem alterações de schema**. (O `API_ENDPOINTS.md` dizia que as janelas "já existiam no
  PATCH": não existiam em lado nenhum.)
- **Reviews:** cada review traz `utilizadorNome` e `utilizadorAvatar`; novo `GET /api/users/me/reviews`.
- **Catálogo:** `search` casa nome da bebida, do produtor **ou** de uma casta; novo filtro por região (na altura `regioes` em texto; passou a `regiaoIds` no mesmo dia, ver "Regiões como lookup"). Tudo em AND com os
  filtros aplicados — a pesquisa limita-se aos filtros ativos e "limpar" é o cliente deixar de os enviar (requisito teu).
  "Limpar" = **repor todos os filtros** ao estado inicial, para o utilizador poder fazer outra filtragem (confirmado por ti).
- **Assunções (aprovadas por ti em 2026-09-19):** sem janela = `EM_GUARDA`; `valorTotal` = preço pago × quantidade (não
  valor de mercado); `totalProntasAAbrir` conta garrafas (incluindo as em atraso); `?sort=preco` = o mais caro primeiro;
  produtor da semana = rotação pelos **10** primeiros (não por todos); o filtro por região aceita várias; `utilizadorNome` = "nome
  apelido" do perfil ("mais clean" nas reviews).
- **Extras:** `Pageable.comOrdenacaoPadraoDeBebidas` (o id desempata também quando o cliente escolhe o `sort`);
  `PedidoInvalidoException` → 400; `Clock` em `Europe/Lisbon`; `HqlQueriesTest` (valida o HQL de todas as `@Query` sem
  BD) e `ContextoArrancaTest` (o contexto Spring carrega sem BD: injeção, mapeamento, queries derivadas e rotas —
  ambos verificados com um controlo negativo, isto é, falham quando se estraga uma query de propósito). **Não substituem**
  o arranque contra o Oracle (`ddl-auto: validate`) nem executar SQL.
- **Validação ao vivo (feita em 2026-09-19, BD de dev, 99 verificações):** arranque com `ddl-auto: validate`; Produtor
  (`totalProdutos`, `/bebidas` com categoria, página e 404, `/destaque` = Licor Beirão, o que o cálculo à parte por SQL
  previa, estável entre chamadas); catálogo (`search` por casta e por produtor, região simples, várias e combinadas com
  `search`/`paisId`/categoria, 16 bebidas sem repetir na paginação); reviews (autor com nome e avatar; `POST` e `PUT` com
  um autor que TEM avatar, sem `LazyInitializationException`; `/me/reviews`); Caves (os 4 estados, totais por cave,
  ordenações, consumir 1 de 3, consumir a última, 409/400/404, `PATCH`, perfil coerente) e 4 consumos em simultâneo sobre a
  mesma linha (5 garrafas → 1 por consumir + 4 consumidas: nada se perdeu). Log da API sem erros. Dados de teste
  apagados e conta de teste reposta (avatar `NULL`).

### Regiões como lookup (implementada e validada ao vivo, 2026-09-19)

Decisão tua (opção B): `regiao` (país + nome) e `produtor.produtor_regiao_id` (FK) em vez do texto livre
`produtor_regiao`. Só regiões dos países mais populares; as dos outros acrescentam-se à medida que entrarem produtos
(podes enviar-me os links das bebidas e ajustamos os dados). **Douro e Porto são duas regiões.**

- **BD:** tabela `regiao` (nome único por país) e `produtor_regiao_id` no lugar do texto (`01_tables.sql`,
  `02_constraints.sql`, patch `07_patch_regioes.sql`, repetível). **Acrescentei uma garantia de coerência (a rever):** uma FK
  composta `(produtor_pais_id, produtor_regiao_id) → regiao(regiao_pais_id, regiao_id)` — a BD recusa uma região de outro
  país — e um `CHECK` (região sem país não é permitida). Testado com 9 casos, recusas incluídas. Receita para produtores e
  regiões novos em `database/README.md`. Reconstruir do zero e "estado antigo + patch" dão conteúdo idêntico.
- **Lista provisória (69 regiões, 7 países — a rever por ti):** Portugal 15 (Vinho Verde, Trás-os-Montes, Douro, Porto,
  Távora-Varosa, Dão, Bairrada, Beira Interior, Lisboa, Tejo, Península de Setúbal, Alentejo, Algarve, Madeira, Açores),
  Espanha 14, França 11, Itália 10, EUA 8, Escócia 6, Inglaterra 5 (ver `seed/01_lookups.sql`). Convenção: nome PT-PT quando
  há forma estabelecida (Bordéus, Borgonha, Califórnia, ...) e o nome local nos restantes (Rioja, Speyside, ...); sem
  sub-regiões (Napa Valley, Cima Corgo, ...).
- **Dados demo:** 13 produtores migrados (`Vinho Verde — Melgaço` → Vinho Verde; `Arraiolos — Alentejo` → Alentejo;
  `Óbidos` e `Lisboa — Rossio` → Lisboa). **Gin 44° (Peniche) e Licor Beirão (Lousã) ficam sem região (`NULL`):** a lista
  tem regiões, não localidades — e a localidade (Melgaço, Peniche, ...) perdeu-se.
- **API:** entidade `Regiao`; `/lookup/regioes?paisId=` passou de `List<String>` a `[{ id, nome }]`, só das regiões que
  têm bebidas; o filtro é `regiaoIds` (substitui `regioes`, que era texto); `regiaoId` em `ProdutorDetailDto` e
  `ProdutorResumoDto`; `BebidaService.getDetail` passou a `@Transactional(readOnly = true)` (navega bebida → produtor →
  região, ambos LAZY). Quebra o contrato do `regioes` da fatia 4 (ainda sem Android a usá-lo).
- **Validado ao vivo (28 verificações):** regiões de Portugal com bebidas por ordem alfabética, países sem regiões → lista
  vazia, filtro por 1 e várias regiões e combinado com `paisId`/categoria/`search`, `regiao`/`regiaoId` em produtor e
  detalhe da bebida (também com produtor sem região) e regressão do catálogo, do destaque e dos países.
- **Revista e substituída em 2026-09-20** pela lista do utilizador (159 regiões, 6 países) — ver "Fecho do backend v1".
  **Pendente:** acrescentar as regiões dos outros países com os produtos reais.

### Fecho do backend v1: EAN, imagens, regiões, países, whisky e termos (implementado e validado ao vivo, 2026-09-20 e 21)

Decisões tuas (2026-09-20), sobre as propostas de "antes do 1.º lote": **(1)** EAN: sim; **(2)** imagens = URL do
retalhista/feed, coluna alargada; **(3)** avalias tu os termos da Awin; **(4)** termos servidos pela API pelo padrão dos
avatares (o *ficheiro* é estático; na BD só ficará, se se fizer, a versão aceite por cada utilizador); **(5)** produtores na
pesquisa: próxima versão; **(6)** Portugal no topo dos países; **(7)** grafias PT-PT; **(8)** mínimo por categoria;
**(9)** FK composta das regiões mantida. Decisões tuas (2026-09-21): **(10)** `whisky_regiao` unificada em `regiao` (o país
do whisky continua a ser o da bebida); **(11)** os termos guardam só a data em que cada utilizador aceitou, na tabela
`utilizador` (o texto fica para ti); **(12)** a lista "Por fazer depois" mantém-se e trabalha-se quando a 1.ª versão da app
estiver pronta.

- **EAN e imagem (patch `08`, `01_tables.sql`, `Bebida.kt`):** `bebida_ean VARCHAR2(14)`, anulável, único (`uq_bebida_ean`) e
  só dígitos, 8 a 14 (`ck_bebida_ean`); `bebida_path_image` de 255 para 1000. Só interno (não vai para a API). Testado na BD
  (em transação revertida): duplicado recusado (`ORA-00001`), vários `NULL` aceites, letras e 7 dígitos recusados
  (`ORA-02290`), EAN-8 e EAN-13 com zero à esquerda aceites, URL de 900 caracteres aceite e de 1001 recusado. O patch é
  repetível (corrido 2 vezes). Ainda **não** há nada no Android para URLs absolutos (ecrãs por construir).
- **Portugal no topo (`OrdemAlfabetica.kt`, `LookupController.kt`):** `/lookup/paises` traz Portugal primeiro e o resto
  alfabético (218 itens); +3 testes.
- **Grafias PT-PT (patch `09`, seed, patch `06`):** 13 países, só renome, ids intactos, `pais` e `produtor_pais` conferidas
  depois: Botsuana, Koweit, Jibuti, Quirguizistão, Usbequistão, Zimbabué, Malávi, Seicheles, Sri Lanca, Trindade e Tobago
  e, por decisão tua de 2026-09-21 (as duas formas circulam em PT-PT), Barém, Bangladeche e Quiribáti.
- **Regiões, lista final (patch `10`, seed): 159 em 6 países** — Portugal 14, Espanha 20, Escócia 30, Irlanda 32, Canadá 13,
  EUA 50. Tratamento da tua lista: maiúsculas e sem acentos → nome próprio com acentos; PT-PT no Canadá, nos EUA e nos nomes
  portugueses/espanhóis onde há forma estabelecida (Quebeque, Ontário, Califórnia, Nova Iorque, Carolina do Norte, Astúrias,
  ...); Escócia e Irlanda com os nomes locais dos rótulos. **Escolhas minhas, que podes reverter:** Irlanda só com os 32
  condados (as 4 províncias são títulos de grupo; misturar os dois níveis dava "Cork" ou "Munster"?); `Vinho Verde` passou a
  `Minho` e `Península de Setúbal` a `Setúbal` (renome: o produtor que já usava o Minho manteve-o); **acrescentei Islay,
  Campbeltown e Islands** (regiões clássicas do Scotch que não estavam na tua lista) **e Castela-La Mancha** (a comunidade
  autónoma que faltava); ignorei uma linha de lixo de copy/paste (um nome de utilizador e uma hora) que estava entre a Escócia
  e a Irlanda; França, Itália e Inglaterra ficam sem regiões (fora dos países "para começar"; nenhum produtor as usava). Os 6
  condados do Ulster na Irlanda do Norte ficam sob "Irlanda" (whiskey irlandês, como na tua lista): um produtor de lá tem de
  ter o país "Irlanda" para a FK aceitar a região; com o país "Irlanda do Norte" não teria regiões. Verificado: patch aplicado
  e repetido; os 13 produtores com região mantiveram-na (só o nome do Minho mudou); o **bloco de regiões do seed, corrido
  sobre a tabela vazia, dá exatamente o mesmo conjunto** (159, mesmo hash).
- **Validação:** 60 testes verdes; `bootRun` com `ddl-auto: validate` sem erros (a coluna nova mapeada); `curl`:
  `/lookup/paises` (Portugal primeiro, 218, 10 nomes novos, 0 antigos), `/lookup/regioes?paisId=1` (Alentejo, Algarve,
  Bairrada, Douro, Lisboa, Minho), países sem bebidas → lista vazia, filtro `regiaoIds=1` → 1 bebida, detalhe com
  `regiao: Minho`, catálogo com 16; log sem erros.
- **Região do whisky (patch `11`, `01_tables.sql`, `02_constraints.sql`, seed, patch `06`):** `whisky.whisky_regiao_id` aponta
  agora para `regiao(regiao_id)` e a tabela `whisky_regiao` (16 valores planos: Speyside, Kentucky, Japão, Portugal, ...) foi
  apagada (`PURGE`). Antes de a apagar: o conteúdo era exatamente o do seed, só a FK do `whisky` lhe apontava, a `whisky` tinha
  0 linhas e nenhum objeto dependia dela; o patch pára sozinho se houver whiskies com região preenchida. Testado na BD (em
  transação revertida): região válida aceite, id inexistente recusado (`ORA-02291`), `NULL` aceite. Como notaste, um whisky da
  Índia, do Japão, ... fica sem região mas mostra o país (`bebida.bebida_pais_origem_id`, tabela `pais`). Sem endpoint nem DTO
  (não há subtype `whisky` no backend).
- **Termos e condições (patch `12`, `TermosRegras.kt`, `static/legal/termos.html`):** `utilizador_termos_aceites_em` (`NULL` =
  nunca aceitou; só data, sem versão). O registo exige `aceitouTermos: true`; `GET /api/users/me` traz `termosAceitesEm` e
  `precisaAceitarTermos`; `POST /api/users/me/termos/aceitar` regista a aceitação. `aquavitae.termos.em-vigor-desde` (data, ou
  a variável `AQUAVITAE_TERMOS_EM_VIGOR_DESDE`) força nova aceitação a quem aceitou antes, sem perder o histórico. O texto é
  `GET /legal/termos.html` (público), **hoje só um marcador provisório**: o texto final é teu. Contrato em "Termos e condições"
  no `API_ENDPOINTS.md`. **Quebra o registo** (campo novo obrigatório): o Android ainda não o envia.
- **Reconstrução do zero verificada:** num schema temporário (`aq_rebuild`, já apagado) corri os 5 scripts do `run-migrations`
  e comparei com a dev — 57 tabelas, 239 colunas (tipo, nulidade, identity), 219 constraints (estrutura e nomes), o trigger, o
  texto de todas as tabelas de lookup, 159 regiões por país, 15 produtores com região e `pais`/`produtor_pais` alinhadas:
  **tudo idêntico**; só diferem os dados de utilizador que só existem na dev (contas, reviews, favoritos, ...). **Apanhou um erro
  meu:** o `02_bebidas.sql` procurava a região `'Vinho Verde'` (renomeada para `Minho`) e, como a subconsulta não encontra nada,
  a Quinta de Soalheiro ficava **sem região, em silêncio**, numa BD reconstruída; corrigido. **Lição:** ao renomear um valor de
  lookup, procurar o nome antigo nos seeds. (O patch `11` também deixava a tabela apagada na reciclagem do Oracle — passou a
  `DROP ... PURGE`.)
- **Validação dos termos ao vivo (69 testes verdes, `bootRun` com `validate`):** `GET /legal/termos.html` dá 200 sem auth e um
  ficheiro inexistente dá 404 (não 401); registo sem o campo ou com `false` → 400, com `true` → 201; `/me` de uma conta nova →
  data preenchida e `precisaAceitarTermos: false`, de uma conta antiga → `null` e `true`; `POST .../aceitar` → 401 sem token e
  200 com; com `AQUAVITAE_TERMOS_EM_VIGOR_DESDE=2099-01-01` a conta que aceitou hoje passa a `true`, mantém a data e ao voltar a
  aceitar a data é substituída. Dados de teste apagados (conta criada; `demo2` reposta a `NULL`).

### Decisões de dados: catálogo curado vs. ofertas de afiliados (2026-09-18)

- **Catálogo** (`bebida` + subtypes, `produtor`) é curado à mão e é a fonte de todos os atributos mostrados
  na app (corpo, castas, botânicos, ...). **Ofertas** (`bebida_link_compra` + `retalhista`) é a única parte
  que vem de afiliados: preço, link (já com tracking), retalhista. Os feeds da Awin não trazem atributos
  sensoriais/de produção, por isso não é deles que dependemos.
- **Atualização (2026-09-19, esclarecido por ti):** já és afiliado da Awin e os **produtos vão entrar pela Awin**; os
  atributos que o feed não traz (corpo, castas, botânicos, ...) só se acrescentam **à mão quando os tiveres** — senão
  ficam `NULL` (por isso os atributos são anuláveis de propósito). **Resolvido no mesmo dia:** as linhas `bebida` criam-se
  **à mão, em lotes** (não automaticamente a partir do feed), com o feed/os links da Awin a alimentar sobretudo
  `bebida_link_compra` e a informação que traz — fluxo em "Retomar aqui", secção "Como entram as bebidas".
- **Não ficamos limitados à Awin**: `retalhista_rede_afiliados` é só uma etiqueta; retalhistas de redes
  diferentes ou com programa próprio convivem na mesma tabela. A app funciona sem afiliado nenhum (uma
  bebida sem oferta aparece só sem botão de compra).
- **`null` = "não disponível".** O schema já suporta isto: todas as colunas de atributo do catálogo são
  anuláveis e os `CHECK (... BETWEEN 1 AND 5)` aceitam NULL (só `utilizador_password_reset` tem `NOT NULL`).
  Regra de UI (a aplicar quando se construírem os ecrãs): omitir o que faltar nos cartões de lista; secção
  condicional no detalhe; "Não disponível" só em linhas rotuladas que o utilizador espera ver. Os filtros por
  acidez/doçura/corpo excluem bebidas sem esse valor.
- **Escalas 1–5 (acidez, doçura, defumado, suavidade) só com fonte** (ficha técnica, prova própria) — melhor
  `null` do que um valor inventado.
- **Cuidado ao inserir por SQL:** colunas-flag com `DEFAULT` cujo campo na entidade Kotlin é não-nulo
  (`produtor_permite_visitas`, `retalhista_is_ativo`, `avatar_is_active`, `cave_bebida_is_consumida`,
  `cave_bebida_quantidade`, `bebida_rating_medio`, `bebida_total_reviews`) — nunca inserir NULL explícito
  (a leitura pela API rebenta nessa linha); omitir a coluna ou usar 0/1. Sem reviews, `bebida_rating_medio`
  é 0: a UI deve usar `totalReviews == 0` para mostrar "sem avaliações", não 0 estrelas.
- **Mínimo por categoria ainda por fechar** (proposta: todas — nome, categoria, produtor, país, teor,
  volume; vinho + tipo e castas; whisky + tipo e idade; gin + destilação). A BD não o impõe (`bebida_name`,
  `bebida_category_id`, etc. são anuláveis) — fica por disciplina + um script de verificação a correr
  depois de cada lote de seed (por escrever, inclui bebida sem linha no subtype da sua categoria e vice-versa).

## Por fazer depois (fora de âmbito imediato)

**Decidido em 2026-09-21: esta lista mantém-se como está e trabalha-se nela quando a 1.ª versão da app estiver pronta**
(não antes; o desenvolvimento sequencial continua a ser BD → API → Android).

- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`). Até lá, uma bebida dessas
  categorias aparece no catálogo e no detalhe só com os campos gerais.
- Testes automatizados de services/controllers com BD (só há testes de regras puras, 69 no total, em `src/test`; os
  endpoints validam-se ao vivo com `bootRun` + `curl.exe`, ver `CLAUDE.md`).
- Script de verificação do catálogo depois de cada lote de seed (mínimo por categoria, subtype em falta) — passa a ser
  útil já com os lotes da Awin; **aprovado em 2026-09-20**, escreve-se com o 1.º lote (ver "Retomar aqui", passo 3).
- Gin sem "estilo" (London Dry, Old Tom, ...) — lacuna de schema só a decidir no 1.º lote com gins. (`bebida_ean` já
  existe desde 2026-09-20.)
- Pesquisa de produtores como resultado à parte (`GET /api/produtores?search=`) — próxima versão.
- CI/build pipeline, Dockerfile da API para deploy.
- Trocar `Page<BebidaSummaryDto>` por um DTO de paginação próprio antes de produção.
- Conceitos de escalabilidade/otimização — deliberadamente adiados até tudo funcionar ponta a ponta.

## Fora de âmbito deste MVP (fases futuras, ver briefing)

Categoria `rum`, notícias, eventos de prova cega/social, pairing por IA, scan de rótulos.
