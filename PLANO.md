# AquaVitae — Plano de trabalho

Roadmap vivo do MVP. Atualizar isto no fim de cada sessão de trabalho relevante — é o que uma sessão
nova do Claude Code (ou tu, ao voltares passado um tempo) deve ler primeiro para saber onde ficámos.

## ▶ Retomar aqui (última atualização: 2026-09-19)

**Ponto da situação:** BD e backend têm agora todos os endpoints da 1.ª versão implementados e validados ao vivo
(os últimos em 2026-09-19); o Android ainda é só esqueleto.
Contrato completo dos endpoints (o que existe, o que falta) em [`backend/API_ENDPOINTS.md`](backend/API_ENDPOINTS.md).

**Git:** todo o trabalho está na branch `feature/api-endpoints-design`, PR aberto:
https://github.com/krugidev/AquaVitae/pull/1 (a descrição do PR ainda só fala da 1.ª fatia — atualizar antes de
o dar por fechado). A `main` só tem a landing page em `docs/` (GitHub Pages) além do que já lá estava.
`gh` está instalado e autenticado, mas no Git Bash não está no PATH: `export PATH="/c/Program Files/GitHub CLI:$PATH"`.

**✅ Validado ao vivo (2026-09-19, contra o Oracle real):** os endpoints de Produtor, Caves, reviews (autor + histórico)
e a pesquisa/filtro por região do catálogo correram contra a BD de dev (99 verificações, ver "Backend — fatia 4") e
têm 57 testes automáticos que não precisam de BD. **Docker:** depois de um reinício do Windows o Docker Desktop pode
falhar a arrancar (`sailor-ingest.sock ... rename ... The file cannot be accessed by the system`; resolveu-se com um novo
reinício) e o contentor Oracle fica parado (`docker compose up -d` em `database/`). **Nunca "Reset to factory defaults"**
no diálogo do Docker: apaga contentores e volumes, incluindo a BD.

**O que falta no backend** (1.ª versão):
1. ✅ **Produtor, Caves e lacunas dos mockups** (reviews com autor, histórico de reviews, pesquisa por produtor/casta,
   filtro por região) — implementado e validado ao vivo em 2026-09-19, ver "Backend — fatia 4" mais abaixo.
2. **A decidir (produto):** termos e condições (popup no login/registo) — texto embutido na app ou servido pela API;
   produtores como resultado próprio da pesquisa (`GET /api/produtores?search=`).
   **Regiões por país — decidido em 2026-09-19 (opção B), a implementar a seguir:** tabela de lookup `regiao` (país +
   nome) e `produtor` passa a apontar para ela por FK, em vez do texto livre `produtor_regiao` (que gerava pílulas a
   mais com gralhas ou variantes). Regiões só para os países mais populares (Portugal primeiro; Espanha, EUA, Inglaterra,
   ...); as restantes acrescentam-se à medida que entrarem produtos. **Douro e Porto são duas regiões separadas.**
3. **Depende de dados reais:** detalhe/cartões das outras categorias (whisky/gin/licor/vodka/aguardente) — só `vinho`
   tem subtype implementado.
4. **Adiado:** email do código de recuperação de password (endpoint pronto, envio real por email adiado — só log).
5. **Transversal:** os testes são só de regras puras (verificação de links 21, ordenação alfabética 4, caves 22, produtor
   da semana 7, HQL das `@Query` 2, arranque do contexto sem BD 1); nenhum de services/controllers com BD. `Page<...>` sai como JSON do Spring (aviso de "não
   estável"): trocar por DTO próprio antes de produção. A pesquisa por texto distingue acentos (`Esporao` não acha
   `Esporão`).

**Coisas do utilizador em paralelo (não bloqueiam o backend):**
- Documento de **seeds** (`android/AQUAVITAESEEDS-NOTES`, não commitado; a cópia da raiz, `—--------------- DADOS A
  INSERIR NA.txt`, é a versão antiga). Castas, países e lookups **já estão no seed e na BD** (ver "Seeds das notas",
  2026-09-19). Falta o catálogo real de bebidas/produtores/retalhistas — a cada lote (semanal), o Claude ajuda a mapear as
  características de cada bebida aos valores dos lookups (NULL onde não houver fonte). Ver também "Decisões de dados"
  mais abaixo. Para cada link de retalhista com rede de afiliados são precisas **duas URLs** (afiliado + página do
  produto sem tracking, para a verificação diária). Correr `POST /api/admin/links-compra/verificar` depois de cada lote.
- **Awin:** já és afiliado (dito por ti em 2026-09-19; a candidatura tinha sido submetida como Comparison Engine, sector
  "Wine, spirits and tobacco"). **Para incorporar os produtos falta saber/decidir:** (a) a que anunciantes (retalhistas)
  estás ligado e quais interessam; (b) como obter os dados — feeds de produtos da Awin (o URL do feed leva uma chave:
  vai para variável de ambiente, **nunca para o repo**); (c) como casar cada produto do feed com uma bebida do catálogo
  (um `bebida_ean` ajudava — lacuna de schema já registada em "Por fazer depois" —, senão por nome). Mapeamento previsto,
  **a confirmar com um feed real**: link de afiliado → `bebida_link_compra_url`, link do produto sem tracking →
  `..._url_verificacao`, preço → `..._preco_atual`, stock → `is_ativo` (o feed é o melhor sinal de "ainda existe").
  Continua a valer: os atributos da bebida são curados à mão; da Awin só vêm as ofertas.
  Site enviado: `https://krugidev.github.io/AquaVitae/` — **não mudar o nome do
  repositório nem pôr "Custom domain" nas definições do Pages** (aconteceu por engano uma vez e desviou o site;
  já revertido). A ver outros afiliados/retalhistas com programa próprio.

**Depois do backend:** ecrãs Android por feature, contra o contrato validado. O `AquaVitaeApi.kt` está desatualizado
(reviews, favoritos/wishlist/provadas, filtros, campos novos) — sincronizar ecrã a ecrã, ver fim do `API_ENDPOINTS.md`.
Ecrã "Onde comprar" desenhado em conversa (lista de retalhistas com disponível/indisponível, aviso de afiliação).

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
  por código de carácter e poria "África" depois de "Zimbábue"; `OrdemAlfabetica.kt`, +4 testes). Se quiseres
  **Portugal no topo** da lista de países, é uma linha (decisão de produto, não tomada).
- **Pendente:** `Vinho` (entidade/DTO) ainda não expõe a barrica; whisky/vodka/gin/licor/aguardente continuam sem subtype
  no backend; `produtor_regiao` tem valores compostos nos demo (`Vinho Verde — Melgaço`, `Arraiolos — Alentejo`) que
  dariam pílulas feias no filtro por região — no catálogo real, usar só a região ("Vinho Verde", "Alentejo").

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
- **Catálogo:** `search` casa nome da bebida, do produtor **ou** de uma casta; novo filtro `regioes`. Tudo em AND com os
  filtros aplicados — a pesquisa limita-se aos filtros ativos e "limpar" é o cliente deixar de os enviar (requisito teu).
  "Limpar" = **repor todos os filtros** ao estado inicial, para o utilizador poder fazer outra filtragem (confirmado por ti).
- **Assunções (aprovadas por ti em 2026-09-19):** sem janela = `EM_GUARDA`; `valorTotal` = preço pago × quantidade (não
  valor de mercado); `totalProntasAAbrir` conta garrafas (incluindo as em atraso); `?sort=preco` = o mais caro primeiro;
  produtor da semana = rotação pelos **10** primeiros (não por todos); `regioes` aceita várias; `utilizadorNome` = "nome
  apelido" do perfil ("mais clean" nas reviews).
- **Extras:** `Pageable.comOrdenacaoPadraoDeBebidas` (o id desempata também quando o cliente escolhe o `sort`);
  `PedidoInvalidoException` → 400; `Clock` em `Europe/Lisbon`; `HqlQueriesTest` (valida o HQL de todas as `@Query` sem
  BD) e `ContextoArrancaTest` (o contexto Spring carrega sem BD: injeção, mapeamento, queries derivadas e rotas —
  ambos verificados com um controlo negativo, isto é, falham quando se estraga uma query de propósito). **Não substituem**
  o arranque contra o Oracle (`ddl-auto: validate`) nem executar SQL.
- **Validação ao vivo (feita em 2026-09-19, BD de dev, 99 verificações):** arranque com `ddl-auto: validate`; Produtor
  (`totalProdutos`, `/bebidas` com categoria, página e 404, `/destaque` = Licor Beirão, o que o cálculo à parte por SQL
  previa, estável entre chamadas); catálogo (`search` por casta e por produtor, `regioes` simples, várias e combinadas com
  `search`/`paisId`/categoria, 16 bebidas sem repetir na paginação); reviews (autor com nome e avatar; `POST` e `PUT` com
  um autor que TEM avatar, sem `LazyInitializationException`; `/me/reviews`); Caves (os 4 estados, totais por cave,
  ordenações, consumir 1 de 3, consumir a última, 409/400/404, `PATCH`, perfil coerente) e 4 consumos em simultâneo sobre a
  mesma linha (5 garrafas → 1 por consumir + 4 consumidas: nada se perdeu). Log da API sem erros. Dados de teste
  apagados e conta de teste reposta (avatar `NULL`).

### Decisões de dados: catálogo curado vs. ofertas de afiliados (2026-09-18)

- **Catálogo** (`bebida` + subtypes, `produtor`) é curado à mão e é a fonte de todos os atributos mostrados
  na app (corpo, castas, botânicos, ...). **Ofertas** (`bebida_link_compra` + `retalhista`) é a única parte
  que vem de afiliados: preço, link (já com tracking), retalhista. Os feeds da Awin não trazem atributos
  sensoriais/de produção, por isso não é deles que dependemos.
- **Atualização (2026-09-19, esclarecido por ti):** já és afiliado da Awin e os **produtos vão entrar pela Awin**; os
  atributos que o feed não traz (corpo, castas, botânicos, ...) só se acrescentam **à mão quando os tiveres** — senão
  ficam `NULL` (por isso os atributos são anuláveis de propósito). **A desenhar, com o 1.º feed real:** as linhas `bebida`
  são criadas automaticamente a partir do feed (nome, marca → produtor, preço, EAN) ou continuam a ser criadas à mão, com o
  feed a alimentar só `bebida_link_compra`? Muda o esforço semanal e o risco de duplicados (a mesma bebida em vários
  retalhistas).
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

- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`).
- Testes automatizados de services/controllers (só existem os 21 da verificação de links, em `src/test`).
- Script de verificação do catálogo depois de cada lote de seed (mínimo por categoria, subtype em falta).
- Gin sem "estilo" (London Dry, Old Tom, ...) e `bebida` sem EAN — lacunas de schema só a decidir se/quando forem
  precisas (ver conversa sobre valores de lookup).
- CI/build pipeline, Dockerfile da API para deploy.
- Trocar `Page<BebidaSummaryDto>` por um DTO de paginação próprio antes de produção.
- Conceitos de escalabilidade/otimização — deliberadamente adiados até tudo funcionar ponta a ponta.

## Fora de âmbito deste MVP (fases futuras, ver briefing)

Categoria `rum`, notícias, eventos de prova cega/social, pairing por IA, scan de rótulos.
