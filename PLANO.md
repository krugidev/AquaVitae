# AquaVitae — Plano de trabalho

Roadmap vivo do MVP. Atualizar isto no fim de cada sessão de trabalho relevante — é o que uma sessão
nova do Claude Code (ou tu, ao voltares passado um tempo) deve ler primeiro para saber onde ficámos.

## ▶ Retomar aqui (última atualização: 2026-09-24, fatias 1 a 6 do Android feitas — a seguir: ronda de bugs e os lotes de bebidas)

**Numa sessão nova, ler por esta ordem:** `CLAUDE.md` (convenções e ferramentas desta máquina) → esta secção → `android/README.md`
(estado do frontend, estrutura, mapa de ecrãs) → `android/design/README.md` (os prints e **o que o utilizador pediu para cada ecrã**,
secções "14. Homepage", "15. Catálogo / Filtros", "16. Cave e popup de detalhe da bebida", "Ajustes de feedback + 'Já provadas'",
"Favoritos e Wishlist", "Perfil" e "Produtor" têm o detalhe do que falta) → `backend/API_ENDPOINTS.md` (contrato). O frontend documenta-se
nesses dois ficheiros do `android/`; o PLANO guarda o roadmap e as decisões.

**Ponto da situação:** o backend da 1.ª versão está fechado. **Todas as fatias do Android até agora estão feitas e validadas ao
vivo:** 1a/1b (auth, onboarding), 2a (homepage), 2b (catálogo/filtros), 3a (popup de detalhe de uma bebida, partilhado por toda a
app), 3b (cave: lista, "Nova cave", "Adicionar à cave" com destaque de onde a bebida já está), 3c (ajustes de feedback: popup de
confirmação ao duplicar numa cave, review a exigir "provada" em vez de a marcar sozinha, "Consumir" a marcar "provada", resumo e
ecrã inteiro "Já provadas"), 4 (Favoritos e Wishlist a sério, a partir de um mockup do próprio utilizador — filtros/ordenação,
nota própria vs. média da comunidade, "Comprar em X"/"Para a cave") e **5 (ecrã de perfil: ver/editar dados e preferências,
escolher avatar, terminar sessão — a partir de um mockup do próprio utilizador)** e **6 (página do produtor: imagem, rating geral das
bebidas, história em popup, localização com a morada e "Abrir no mapa", garrafas em catálogo com "carregar mais 3" e o catálogo
só do produtor, com pesquisa e filtros)**. Entre a 3c e a 4, o utilizador testou por conta
própria (a sua conta pessoal, não a `demo_user2`) e apanhou 3 bugs, já corrigidos e validados ao vivo — Favoritos/Wishlist
vazios/em erro, "INVESTIDOS"/"GARRAFAS" da Cave sem atualizar depois de guardar uma garrafa, e as duas telas presas ao resultado
da 1.ª visita mesmo com o modelo certo (detalhe completo em `backend/API_ENDPOINTS.md`, "Sincronização pendente com o Android",
"correção pós-3c"). Isto fecha o **fluxo principal do MVP**: login → homepage → catálogo (com filtros) → detalhe de uma bebida
(favoritar, avaliar — só se já provada —, adicionar à cave) → cave (consumir, criar, ver já provadas) → favoritos/wishlist (a
sério) → perfil (dados, preferências, avatar, terminar sessão) → página do produtor (a partir do cartão em destaque da home
e do popup de detalhe de uma bebida). **Já não há nenhum destino da app sem forma de lá chegar**, exceto "As minhas reviews" e
"Conta e segurança" do perfil (mockups por chegar). **O utilizador combinou o que vem a seguir (2026-09-24):** uma **ronda de
bugs** e, só depois, começar a meter **bebidas reais por lotes vindas dos afiliados — da ordem de 100 por semana** (o fluxo
abaixo, "Como entram as bebidas", foi desenhado para ~30 por lote: rever o que muda com 100/semana) — ver "Próximos passos".

**Git:** duas branches. **`feature/api-endpoints-design`** = PR #1 (backend v1): já fundido/enviado. **`feature/android-app`** =
criada a partir dela em 2026-09-21 para o Android, com [PR #2](https://github.com/krugidev/AquaVitae/pull/2) aberto (para `main`,
ainda não fundido) — leva tudo desde a fatia 1a até à fatia 5 (incluindo a correção pós-3c; a 5 é o commit `d33d440`),
commitado por área/fatia (ver o próprio PR para a lista). **A fatia 6 (produtor) já está feita mas ainda não commitada nem
enviada** — `git status` mostra só os ficheiros dela: **backend** `ProdutorDetailDto.kt`, `ProdutorService.kt`,
`BebidaRepository.kt` (2 queries novas + o filtro `produtorId`), `BebidaService.kt`/`BebidaController.kt`/`BebidaDtos.kt` (o `produtorId`), `Produtor.kt` (a `morada`), `ProdutorRating.kt` (novo) + `ProdutorRatingTest.kt` (novo);
**BD** `ddl/01_tables.sql`, `ddl/14_patch_produtor_morada.sql` (novo), `database/README.md`; **Android**
`feature/produtor/` (novo, 3 ficheiros), `ui/components/ContagemEOrdenacao.kt` (novo), `data/model/ProdutorFormatacao.kt`
(novo) e alterações a `ProdutorModels.kt`, `AquaVitaeApi.kt`, `BebidaRepository.kt` (Android), `BebidaDetalheSheet.kt`,
`CatalogScreen.kt`/`CatalogViewModel.kt`/`FiltrosSheet.kt`/`CatalogFiltro.kt` (o catálogo também serve um só produtor; usa o `ContagemEOrdenacao` partilhado), `HomeScreen.kt`/`CaveScreen.kt`/`FavoritosScreen.kt`/`WishlistScreen.kt`
(o `onVerProdutor`), `AppDestinations.kt`, `AppNavHost.kt`; 2 testes novos em `android/app/src/test/`;
`android/design/produtor/` (novo, o mockup); `android/README.md`, `android/design/README.md`, `backend/API_ENDPOINTS.md`,
`CLAUDE.md` e este `PLANO.md` também têm alterações da fatia 6. **Ficam de fora dos commits, de propósito** (regra do `CLAUDE.md`): as notas do utilizador —
`android/AQUAVITAESEEDS-NOTES` e, na raiz, `—--------------- DADOS A INSERIR NA.txt`, `REGIÕES A AJUSTAR (...).txt` e
`NOVOSDADOS.txt` (este último parece um relatório de incidentes sem ligação ao projeto — vale a pena o utilizador confirmar se
foi parar ali por engano). Ao commitar, usar `git commit <caminhos explícitos>`, nunca `git add -A`. A `main` só tem a landing
page em `docs/`.

**Ambiente no fim da sessão:** BD de dev migrada até ao patch `14` (`produtor_morada`); contentor `aquavitae-oracle-xe` a correr; **API a correr**, perfil
`dev` (sem `mailpit`), com as credenciais do Gmail carregadas e já a servir `tipo`/`tanino`/`produtorRegiao`/`temBebida` e, desde a fatia 6, o
`ratingMedio`/`totalReviews`/`categorias` do produtor — se
reiniciar, ver `CLAUDE.md`, "Email de recuperação — Gmail", para voltar a carregar `backend/.env.mail` antes do `bootRun`; contentor
`aquavitae-mailpit` ainda a correr (não é preciso para o dia a dia — `docker compose -f backend/docker-compose.mail-dev.yml down`
para parar). **Emulador `Pixel_8` ligado, com a build mais recente instalada.** A sessão ativa é a conta de teste
`demo2@aquavitae.local` / `password123` (username `demo_user2`, papel `Utilizador`) — a sessão da **conta pessoal do
utilizador** (`miguelafsmcruz@gmail.com`, usada até à fatia 4) expirou a meio da fatia 5 e não se voltou a entrar (nunca se pede a
password real); os dados reais dessa conta (caves "Vinhos 2026"/"Gins 2026", 2 favoritos, 1 wishlist) continuam tal como
ficaram, por reverter só na próxima vez que o utilizador lá entrar. A `demo_user2` **continua com 3 caves de sessões
anteriores**: "Adega Principal" (id 21, 1 garrafa — Esporão Reserva Tinto 2018 ×1 em guarda), "Tintos de guarda" (id 22, 1
Barca Velha 2015 em guarda até 2035) e "Brancos frescos" (id 23, vazia) — **ficam na BD de propósito**, dado de teste
contínuo; termos repostos a `NULL` no fim desta sessão (a fatia 6 também os aceitou ao testar). **Os dados de teste temporários
da fatia 6** (história/morada/coordenadas/imagem nos produtores 1 e 8 e 7 bebidas movidas para o 1) **já foram revertidos** — a
BD de dev ficou como estava (nenhum produtor tem história, morada, coordenadas nem imagem; ver "Android — fatia 6"). Testes:
backend `.\gradlew.bat test` em `backend/` (**93**, sem BD, +5 do `ProdutorRatingTest`, corridos nesta sessão); Android
`testDebugUnitTest` (**72**, +19: `ProdutorFormatacaoTest` e `ProdutorUiStateTest`, ver `android/README.md`). Corri o
`database/verify/rebuild-check.sh` depois do patch `14` (`produtor_morada`): "reconstrução igual à dev". **Landing page (GitHub
Pages):** já está ativa em `https://krugidev.github.io/AquaVitae/` (fonte `main:/docs`, HTTPS, sem domínio próprio; HTTP 200
verificado em 2026-09-24) — serve de "website" nos pedidos a outras redes de afiliados; continua a não se mexer no repo/domínio.

**Arrancar tudo** (detalhes no `CLAUDE.md`): (1) `docker ps` — se o Oracle estiver parado, `docker compose up -d` em `database/`;
(2) API: carregar `backend/.env.mail` (ver acima) e `.\gradlew.bat bootRun` em `backend/` (em background), confirmar com
`curl.exe http://localhost:8080/api/lookup/categorias-bebida`; (3) emulador: Device Manager do Android Studio ou
`emulator.exe -avd Pixel_8`; (4) compilar, instalar e ver: `android/README.md`, "Compilar, instalar e ver". Se o Windows reiniciou, ver
no `CLAUDE.md` o que fazer se o Docker Desktop crashar.

**Próximos passos, por ordem:**
1. **Ronda de bugs (combinada com o utilizador em 2026-09-24, a seguir à fatia 6)**, antes de começar a meter bebidas reais.
   Candidatos já conhecidos, para não os perder: o cartão de bebida diz "1 reviews" (sem singular) no catálogo/cave/home;
   as linhas de `ProvadasScreen` não abrem o popup de detalhe (`combinedClickable` nunca foi ligado lá, o mesmo tipo de
   falha da `CaveScreen`, ver `CLAUDE.md`); uma resposta 401 a meio da utilização ainda não leva ao login; o emoji na bio
   nunca foi testado ao vivo; "Comprar em X" da wishlist ainda só abre o popup (falta o link real, ver item 7). O
   utilizador vai testar por conta própria (a conta pessoal) e devolver o que encontrar, como fez depois da 3c.
   **"As minhas reviews" e "Conta e segurança"** do perfil ficam para quando o utilizador enviar os mockups.
2. **Preparar os lotes de ~100 bebidas por semana** (o utilizador disse "por exemplo, irmos adicionando 100 bebidas por
   semana"; o fluxo de "Como entram as bebidas" foi escrito para ~30). O que rever antes do 1.º lote: o **script de
   verificação do catálogo** (item 5) passa a ser obrigatório, não opcional; a tabela de revisão de 100 linhas é grande
   demais para uma só mensagem (dividir em blocos de ~25 e aprovar por blocos, ou por categoria); a **verificação diária de
   links** (`bebida_link_compra_url_verificacao`) e o pedido às páginas dos retalhistas com 100+ links novos por semana —
   confirmar que o intervalo entre pedidos é educado e que uma página em baixo não atrasa o job todo; a **homepage/sugestões**
   e o **catálogo** com centenas de bebidas (paginação e `ORDER BY` já estão deterministas, mas nunca se mediu com dados a
   sério); e produtores sem história/coordenadas/imagem (a página do produtor esconde o que falta, mas ficará pobre até haver
   esses dados — decidir se se preenchem à mão ou se ficam para depois).
3. **O utilizador ainda pode dar feedback das fatias 4, 5 e 6** (Favoritos/Wishlist: filtros, ordenação, "Comprar em X"/"Para
   a cave"; Perfil: dados, preferências, avatar; Produtor: rating geral, morada/mapa, catálogo do produtor) e das anteriores — em
   especial as **diferenças conscientes** listadas em `android/design/README.md` ("14.", "15.", "16.", "Favoritos e
   Wishlist", "Perfil", "Produtor"): sem mapa embebido (a morada abre a app de mapas), a serifa aproximada, "ORDENAR" só com 2
   opções no catálogo, os 7 tipos de vinho mostrados (o mockup só tinha 4), o preço nunca testado com dados a sério, a review
   só com estrelas inteiras, o toque curto a abrir o mesmo popup do premido, a pílula "Vinho 2025" da cave (não modelada — o
   que representa?), "Ver as N garrafas" sem destino (só informativo), o email a mostrar-se sempre em "Editar perfil"
   (contrariamente ao mockup), o emoji na bio nunca testado ao vivo, e os 5 campos da API adiados na fatia 4 (ver item 7).
4. **Depois:** editar uma garrafa já na cave (o `PATCH` já existe na API e no repositório Android, sem UI); limpar o
   esqueleto morto (`feature/detail`, `feature/reviews`, as rotas `detail/{id}`/`reviews/{id}`, já sem forma de lá chegar por
   toque desde a fatia 3a).
5. **1.º lote de bebidas da Awin (~30, ou já ~100)** (em paralelo com o Android; não depende dele) — fluxo em "Como entram as bebidas"
   (logo abaixo). Escrevo então o **script de verificação do catálogo** (aprovado: mínimo por categoria = nome, categoria,
   produtor, país, teor, volume; vinho + tipo e castas; whisky + tipo e idade; gin + destilação; bebida sem linha no
   subtype da categoria e vice-versa; e whisky com região de um país diferente do país de origem da bebida). Antes, **o utilizador**
   avalia os termos dos programas escolhidos na Awin (comparação de preços; uso das imagens do feed).
6. **Texto final dos termos, quando o utilizador o tiver:** substitui-se `backend/src/main/resources/legal/termos.txt` (hoje é um
   texto de exemplo) — formato simples (`# ` = título de secção, parágrafos separados por linha em branco), sem mexer no backend nem
   na app. Deve incluir a maioridade 18+ e o aviso de afiliação/comissões; convém revisão jurídica. Não bloqueia o Android, mas é
   preciso antes do lançamento (a Google Play também exige uma política de privacidade com URL público).
7. **Adiado — "Por fazer depois":** decidido em 2026-09-21 que se trabalha nessa lista **quando a 1.ª versão da app estiver
   pronta**. Inclui os subtypes das outras categorias (**nota:** se o 1.º lote trouxer gins/whiskies, o detalhe só mostra os
   campos gerais da bebida, sem os atributos da categoria), a pesquisa de produtores à parte, o `Page<...>` como DTO próprio, os
   testes com BD, a pesquisa sem distinção de acentos, o CI/Dockerfile, a migração da toolchain do Android e o temporizador do código
   de recuperação com "pedir novo" (`expiraEm` na resposta — o intervalo mínimo entre pedidos já está feito). **Acrescentado na
   fatia 4 (2026-09-24, combinado com o utilizador):** 5 campos que o mockup de Favoritos/Wishlist pede e o `BebidaSummaryDto`
   não tem — `linkCompraId`/`linkCompraUrl` (para "Comprar em X" abrir o browser do retalhista e registar o clique em
   `POST /bebidas/{id}/links-compra/{linkId}/clique`, endpoint que já existe), `precoAtualizadoEm` ("ATUALIZADA HOJE"),
   `totalRetalhistasAtivos` ("MENOR DE N RETALHISTAS"), `quantidadeNaCave` ("N NA CAVE" nos favoritos) e `provadaEm`
   ("PROVADA EM \<mês\>"). Todos batch, via `BebidaSummaryAssembler` (o mesmo padrão de `isFavorito`/`isWishlist`/`isProvada`/
   `notaPropria`); a app já está pronta do lado da UI para os receber, só falta o backend e ligar os campos.

**Duas propostas aprovadas pelo utilizador e feitas (2026-09-24, a seguir à fatia 6):** (a) **morada do produtor** — coluna
`produtor_morada` (anulável, patch `14`) e, na página do produtor, um cartão "LOCALIZAÇÃO" com a morada em texto + "Abrir no
mapa" (com coordenadas usa-as, sem elas `geo:0,0?q=<morada>`), no lugar do placeholder "Mapa da quinta"; o Maps SDK para
Android é grátis sem limite mas exige projeto Google Cloud com faturação ativada + chave, por isso fica de fora da 1.ª versão.
(b) **filtro por produtor** — `produtorId` em `GET /api/bebidas`; o catálogo do produtor passou a ser o próprio `CatalogScreen`
com pesquisa e popup de filtros (sem país/região). **Fica para depois dos primeiros lotes:** uma secção "Produtor" no popup do
catálogo **geral** (pesquisa + chips, como as castas) — pede um lookup de produtores e só compensa com centenas deles.

**Ideia do utilizador para a versão 2 (2026-09-24): ler uma foto de uma refeição e sugerir bebidas.** Desenho sugerido: o
modelo de visão (chamado no backend, chave nunca na app) devolve só **atributos** (pratos, ingredientes, intensidade, gordura,
picante, acidez → perfil de bebida desejado: categoria, tipo, corpo, acidez, doçura, tanino); o backend consulta o catálogo com
esses atributos (a mesma pesquisa dos filtros) e ordena por rating/preferências; o modelo só escreve o "porquê". Assim nunca
sugere bebidas fora do catálogo e o destino continua a ser o link de afiliado. **Depende de** subtypes das outras categorias
(hoje só o vinho tem corpo/acidez/doçura/tanino) e de atributos bem preenchidos nos lotes. Cuidados: custo por foto (limite
por utilizador/dia, comprimir no telemóvel), privacidade (processar e descartar a imagem, dizê-lo nos termos), aviso 18+.

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
- **Esqueleto** (2026-09-12): Compose + MVVM + Hilt + Retrofit/Moshi + DataStore, pacotes por feature. Compila e corre no emulador
  (verificado em 2026-09-21).
- **Fatia 1a (2026-09-21): ícone, loading, login e registo** — tema, fonte Inter, componentes reutilizáveis, sessão, popup dos
  termos; ligada à API real e validada no emulador. Ver "Android — fatia 1a", em "Em curso", e `android/README.md`.
- **Fatia 1b (2026-09-22): recuperar password e onboarding** — 3 passos + popup, 7 ecrãs de perfil e preferências, Coil + SVG; ligada à
  API real e validada no emulador. Ver "Android — fatia 1b", em "Em curso".
- **Fatia 2a (2026-09-23): homepage** — cabeçalho, "Escolhido para ti", "As minhas Caves", estatísticas, produtor em destaque, barra
  de navegação principal; ligada à API real e validada no emulador (com garrafas a sério). Ver "Android — fatia 2a", em "Em curso".
- **Fatia 2b (2026-09-23): catálogo e popup de filtros** — lista de resultados, pesquisa, filtros completos (categoria, origem,
  preço, rating, atributos de vinho, castas) com contagem ao vivo; ligada à API real e validada no emulador. Ver "Android — fatia
  2b", em "Em curso".
- **Fatia 3a (2026-09-23): popup de detalhe da bebida** — partilhado por toda a app (catálogo, caves, favoritos, ...): tabs
  Detalhes/Reviews, favorito/wishlist/adicionar à cave, publicar review; ligada à API real e validada no emulador. Ver "Android —
  fatia 3a", em "Em curso".
- **Fatia 3b (2026-09-23): cave** — lista "As minhas Caves", popup "Nova cave", popup "Adicionar à cave" (com destaque das caves
  onde a bebida já está); ligada à API real e validada no emulador. Ver "Android — fatia 3b", em "Em curso". **Com isto fecha-se o
  fluxo principal do MVP** (login → homepage → catálogo → detalhe → cave).
- **Fatia 3c (2026-09-23): ajustes de feedback + "Já provadas"** — popup de confirmação ao duplicar numa cave, review a exigir
  "provada" em vez de a marcar sozinha, "Consumir" a marcar "provada", resumo de 5 na Cave + ecrã inteiro "Já provadas" (com busca
  para adicionar uma bebida fora de qualquer cave); validada no emulador. Ver "Android — fatia 3c", em "Em curso".
- **Correção pós-3c (2026-09-23/24):** 3 bugs apanhados pelo utilizador a testar por conta própria — Favoritos/Wishlist vazios/em
  erro (`getFavoritos`/`getWishlist` com o modelo errado), "INVESTIDOS"/"GARRAFAS" da Cave sem atualizar depois de guardar uma
  garrafa pelo popup "Adicionar à cave", e, logo a seguir, Favoritos/Wishlist **ainda** presos ao resultado da 1.ª visita à aba
  mesmo depois de corrigido o modelo (marcar um favorito noutro ecrã e voltar à aba não mostrava a mudança); todos corrigidos e
  validados ao vivo. Ver `backend/API_ENDPOINTS.md`, "Sincronização pendente com o Android".
- **Fatia 4 (2026-09-24): Favoritos e Wishlist a sério** — desenhadas a partir de um mockup do próprio utilizador: filtro por
  categoria e nota própria vs. média nos favoritos, ordenação por recentes/preço/rating na wishlist, "Comprar em X"/"Para a cave";
  5 campos do mockup adiados de propósito (ver "Adiado — Por fazer depois"). Ver "Android — fatia 4", em "Em curso". **Com isto,
  todos os ecrãs principais da barra de navegação têm o design final** (só faltam a página do produtor e o perfil).
- **Fatia 5 (2026-09-24): ecrã de perfil** — desenhado a partir de um mockup do próprio utilizador: ver dados e estatísticas
  (provadas/reviews/favoritos/wishlist/caves/garrafas), editar perfil (nome, apelido, username, nacionalidade, bio, avatar),
  escolher avatar num popup à parte, editar preferências (tipos de bebida, doçura/acidez, castas) num popup que já afeta
  "Escolhido para ti" na homepage, terminar sessão. Novo `GET /api/users/me/preferencias` ligado na app (já existia na API).
  Avatar do cabeçalho (Home, Favoritos, Wishlist) passa a abrir este ecrã. Ver "Android — fatia 5", em "Em curso". **Com isto
  fecham-se todos os destinos da app exceto a página do produtor.**
- **Fatia 6 (2026-09-24): página do produtor** — desenhada a partir de um mockup do próprio utilizador: imagem, nome, localização,
  **rating geral das bebidas** (sugestão do utilizador; média ponderada pelas reviews, calculada no backend), pílulas (visitas, site,
  nº de garrafas), história completa em popup, **morada** + "Abrir no mapa" (a app de mapas, pelas coordenadas ou pela morada;
  sem mapa embebido), "Garrafas em catálogo" (3 + "carregar mais 3", máximo 6) e o **catálogo só do produtor** (o próprio
  catálogo, com pesquisa e filtros, via `produtorId` em `GET /api/bebidas`). Acessível pelo cartão do produtor em destaque (home) e pelo do popup de
  detalhe de uma bebida. Ver "Android — fatia 6", em "Em curso". **Com isto não há nenhum destino da app sem forma de lá chegar**
  (fora "As minhas reviews"/"Conta e segurança" do perfil, à espera de mockups).
- Os restantes ecrãs (`detail`, `reviews`) continuam os placeholders do esqueleto (esqueleto morto, por limpar).

## Em curso 🔜

### Android — como vamos construir e ligar (combinado em 2026-09-21)

**Ponto de partida (verificado em 2026-09-21).** O esqueleto **compila tal como está**: `assembleDebug` deu `BUILD SUCCESSFUL` em
2 min 14 s (JDK 21 + Gradle 8.7 + AGP 8.5.2 + Kotlin 1.9.24), com um `app-debug.apk` de 11 MB e só avisos (kapt do Moshi obsoleto,
`Divider` renomeado, AGP 8.5.2 testado até compileSdk 34). Mas **nunca tinha sido compilado nem aberto no Android Studio** (não há
`.idea` nem `build`). Nesta máquina: Android Studio AI-261 (o JDK dele é o **25**, que o Gradle 8.7 e o AGP 8.5.2 não suportam: no
Studio, *Gradle JDK = 21*); o SDK só tinha a Platform 37 e o build de teste instalou a **Platform 35 e as Build-Tools 34** (o
projeto precisa delas); **sem emulador** (criado depois, ver "Preparação"; continua a não haver `cmdline-tools`); aceleração WHPX disponível, 32 GB de RAM.
**Não se atualiza já a toolchain** (AGP 9 / Kotlin 2 / KSP em vez de kapt): funciona e é uma migração à parte, fica para depois da
1.ª versão ("Por fazer depois"). Receita de compilação por linha de comandos no `CLAUDE.md`.

**Princípio:** fatias verticais, **ligadas à API real desde o 1.º ecrã** (o contrato já está validado; nada de dados falsos).

**Cada fatia** (um fluxo de ecrãs) faz-se assim:
1. Mandas os prints de **todos os ecrãs do fluxo de uma vez**, com o que os prints não mostram (estados vazios, erros,
   carregamento, animações).
2. Construo a UI (Compose) + o ViewModel + o repositório + as chamadas Retrofit, **só com os endpoints e DTOs desse fluxo** (o
   `AquaVitaeApi.kt` está desatualizado e sincroniza-se por fatia; a lista está no fim do `API_ENDPOINTS.md`).
3. Compilo por linha de comandos (e escrevo testes das regras e dos ViewModels onde fizer sentido).
4. Corre no emulador contra a API local (`10.0.2.2:8080`, com o Oracle e o `bootRun` a correr); tu testas e mandas feedback.
5. Se um ecrã pedir algo que a API não tem, **corrige-se primeiro a API** (o ciclo sequencial mantém-se) e atualiza-se o contrato.
6. No fim de cada fatia: PLANO e contrato atualizados; commit e push só quando pedires.

**Fundações (feitas uma vez, na 1.ª fatia):** o tema (cores, tipografia, formas) tirado dos prints — melhor ainda com os tokens do
Figma —, os componentes base reutilizáveis (botão, campo de texto, pílula/chip, cartão de bebida, estrelas, barra de navegação),
carregamento de imagens com Coil (+ SVG para os avatares, e a regra URL absoluto vs relativo), tratamento dos erros da API
(`{ status, error, message }`), sessão (401 → volta ao login) e navegação. O ecrã "Onde comprar" (lista de retalhistas com
disponível/indisponível e aviso de afiliação) foi desenhado em conversa e entra na fatia do detalhe.

**Ordem proposta:** (1) auth: login, registo em 2 fases com o popup dos termos (`aceitouTermos`; no login o `getMe()` diz se
`precisaAceitarTermos`; texto em `GET /legal/termos.html`) e recuperação de password; (2) onboarding: nacionalidade, avatar,
preferências; (3) home/catálogo: lista, pesquisa e popup de filtros; (4) detalhe da bebida: reviews, onde comprar, produtor;
(5) perfil e listas: favoritos, wishlist, provadas; (6) caves; (7) página do produtor.

**Preparação (feita em 2026-09-21).** O Android Studio abre `android/` e o sync corre (Gradle JDK = JBR 21, que o Studio descarregou);
o utilizador criou o emulador **`Pixel_8`** (Android 17 / API 37.2, x86_64) e a app do esqueleto foi **compilada por linha de
comandos (1 min 12 s), instalada e aberta nele, com screenshot**: o ciclo **compilar → instalar → abrir → ver o ecrã** já funciona
sem o Studio (comandos no `CLAUDE.md`), por isso consigo comparar cada ecrã com os teus prints. O esqueleto mostra o ecrã de login
provisório (título, email, password, botão grená, "Regista-te"). **Defeitos visíveis a tratar nas fundações:** os ícones da barra de
estado ficam invisíveis (claros sobre fundo claro: a app não trata o *edge-to-edge* do Android 15+) e o 1.º arranque tem *jank* ("Skipped
105 frames", normal em debug, a reavaliar depois). Correção do `.gitignore` do Android (`build/` em qualquer nível; ignorava só `/build`).

**O que preciso de ti:** (a) o SVG do logótipo (emblema + nome + etiqueta) e os ícones do Figma — hoje o emblema é um vetor desenhado
à medida e o nome é texto Inter; (b) *(feito: as decisões da 1b foram fechadas em 2026-09-22)*; (c) os prints de cada fluxo seguinte.

**Branch:** `feature/android-app` (criada em 2026-09-21 a partir da `feature/api-endpoints-design`; o PR #1 fica só com o backend;
ao fundir o PR #1, faz-se rebase).

### Android — fatia 1a: ícone, loading, login e registo (feita e validada no emulador, 2026-09-21)

Feita a partir dos prints do utilizador. **Medido nos pixels dos prints:** grená `#8F321D`, cartão `#F8F8F8`, tinta `#1B1714`, botão
"voltar" `#E9E9E9`, pontos do loading `#C41F21`, azul dos sliders `#1D5DFE`, escurecimento dos popups 43 % de preto; emblema de 92
de diâmetro = anel exterior 5, intervalo 6, anel grosso 10, buraco 50.

- **Fundações:** tema **só claro** (o design é claro; não segue o modo escuro do sistema), tipografia **Inter** (ficheiros copiados
  do Android Studio, licença OFL: falta o aviso de licença antes de publicar), formas, componentes reutilizáveis em `ui/components/`
  (logótipo em 3 variantes, campo sublinhado com rótulo flutuante, botão, cartão, `overlapTop`, popup dos termos, 5 pontos),
  **edge-to-edge** (corrige a barra de estado invisível), ecrã de arranque do sistema, **ícone da app** (adaptativo, com versão
  monocromática para os ícones temáticos), tradução dos erros da API para português (`{status,error,message}`), sessão. Dependências
  novas: `core-splashscreen` e `material-icons-extended`. Os ecrãs antigos do esqueleto ficaram embrulhados em `LegacyScreen`.
- **Loading** (`feature/loading`): logótipo horizontal + **5 pontos vermelhos que crescem e encolhem em onda**. Decide o destino: sem
  sessão → login; com sessão → `GET /me` (token recusado → apaga a sessão e vai ao login; os termos mudaram → login; servidor
  sem resposta → "Sem ligação ao servidor" + **TENTAR DE NOVO**). Mostra a marca pelo menos 1,2 s.
- **Login:** "Username ou Email" + password (com olho), "Esqueci-me da password" (vai para um placeholder até à fatia 1b), "LOGIN"
  sobreposto à margem do cartão, ligação ao registo e "TERMOS E CONDIÇÕES" (abre `/legal/termos.html` no browser). Depois do login
  faz `GET /me`: se `precisaAceitarTermos` aparece o popup e `POST /me/termos/aceitar`.
- **Registo (fase 1):** username, email, password, "COMEÇAR". Valida no cliente (username 3–30 sem espaços nem `@`, email, password
  8–72; o campo com problema fica vermelho), mostra o popup dos termos e **só cria a conta ao aceitar** (`aceitouTermos: true`);
  "Agora não" volta ao formulário sem criar nada. Erros da API em português (409: fica vermelho o campo em conflito). Depois do
  registo vai para o onboarding (ainda o placeholder do esqueleto).
- **Backend (mudou por causa do ecrã):** o campo diz "Username ou Email", por isso o login da API passou a aceitar `identificador`
  (username OU email), sem distinguir maiúsculas e com `trim`; o registo também verifica a unicidade sem maiúsculas (senão "Ana" e
  "ana" seriam duas contas e o login ficaria ambíguo). Ver `API_ENDPOINTS.md`.
- **Validado** (emulador `Pixel_8` + API + Oracle, tudo pelo `adb`): arranque (ecrã do sistema → loading → login); login com password
  errada (erro vermelho da API) e certo com `Demo_User2` (username em maiúsculas) → popup dos termos → data gravada na BD → catálogo
  com dados reais; registo com formulário vazio, email inválido, "Agora não" (BD sem conta nova), aceitar (conta criada, sessão
  guardada, onboarding) e conta já existente (409); sem servidor (mensagem + "Tentar de novo" volta a funcionar). **10 testes
  unitários Android** (validação do registo) e **69 do backend**. Dados de teste apagados (conta `teste_ecra`; `demo2` reposta).
- **Escolhas minhas face aos prints (diz-me se preferes o original):** fundo do loading branco (o cinzento `#D9D9D9` é a cor por
  omissão do Figma, deve ser um marcador); olho da password grená (o print tem um laranja/amarelo); o nome "AQUAVITAE" é texto Inter
  (aproximação: falta o SVG); **popup dos termos** desenhado por mim no estilo do "Password alterada!" (não estava nos prints);
  rótulos que sobem ao escrever (em vez de desaparecerem); sombra suave nos botões grandes; o login vai para o catálogo (placeholder)
  e só o registo passa pelo onboarding.

### Android — fatia 1b: recuperar password e onboarding (feita e validada no emulador, 2026-09-22)

Feita a partir dos 13 prints e das notas do utilizador (em `android/design/`). Detalhe dos ecrãs, do que foi pedido e das diferenças
face aos prints em `android/design/README.md`; componentes, estrutura e convenções em `android/README.md`.

**Decisões fechadas com o utilizador (2026-09-22), todas na opção recomendada:**
1. **Recuperar por username ou email** → os 3 endpoints passaram a aceitar `identificador` (como o login; helper partilhado
   `findByIdentificador`); o email tapado só aparece a quem escreveu o email.
2. **Bandeira da nacionalidade** → **código ISO na BD**: `utilizador_nationality.nationality_codigo_pais` (patch `13` + `01_tables.sql`
   + seed; `/lookup/nacionalidades` devolve `{ id, nome, codigoPais }`); a app desenha a bandeira (emoji).
3. **Sliders** → um nível enviado como `min = max`; "IGNORAR" não envia nada.
4. **Castas** (surgiu ao implementar: a Touriga Nacional caía na posição 253 de 277) → **6 castas em destaque no topo**, as do desenho e
   pela ordem dele, e depois as alfabéticas; a app mostra 10 e, ao chegar ao fim, mais 10.

**Backend (mudou por causa dos ecrãs):** recuperação de password por `identificador`; `codigoPais`; destaques em `/lookup/castas`
(`CASTAS_EM_DESTAQUE`); `PUT /api/users/me` passou a validar os tamanhos (400 em vez de um erro 500 da BD). Testes do backend: **71**
(+2 das castas). `rebuild-check.sh`: a BD reconstruída do zero é idêntica à de dev.

**Android:**
- **Recuperar password** (`feature/recovery`): 3 passos no mesmo cartão (identificar → código de 6 dígitos → password nova) e popup "Password
  alterada!" sobre o login. Teclado numérico automático, gesto de voltar por passos, erros em português com o campo pintado.
- **Onboarding** (`feature/onboarding`): 7 ecrãs — nome; nacionalidade (com bandeira) e descrição; avatar (pílulas de categoria + grelha 3×3
  de SVG); tipos de bebida (escolha múltipla); doçura e acidez (slider vertical); castas (só se escolheu vinho; lista de 10 em 10 com
  barra de deslocamento). Tudo opcional; **guarda no fim** (`PUT /users/me` e `PUT /users/me/preferencias`, só o que foi respondido);
  depois vai para o catálogo.
- **Técnico:** **Coil 2.7 + SVG** (carregador em `AquaVitaeApplication`, `resolveImageUrl`); `AquaVitaeApi.kt` sincronizado com a
  recuperação, o perfil, as preferências e os lookups; `LookupRepository`; `LookupState`/`LookupContent` para as listas da API; componentes
  novos (`PillCard`, `NavRow`, `OptionRow`, `LevelSlider`, `CodeInput`, `FlagChip`, `PasswordChangedDialog`, ...); o escurecimento dos
  popups passou a 43 % (`DialogScrim`, confirmado no pixel: `#919191`). **39 testes unitários** (10 antigos + 29 novos).
- **Defeitos apanhados a testar ao vivo e corrigidos:** a tecla "Seguinte" do teclado parava no olho da password em vez de ir para o campo
  seguinte (`UnderlineField`); os botões de navegação ficavam tapados pelo teclado no ecrã do código; o escurecimento dos popups era 60 %.

**Validação** (emulador `Pixel_8` + API + Oracle; conta descartável, apagada no fim): recuperação por username em maiúsculas e por email;
código errado ("Código inválido"); passwords diferentes; password alterada (código marcado `usado = 1` na BD) e popup no login; gesto de
voltar por passos e saída no login; registo → popup dos termos → onboarding completo (Ana Silva, Portuguesa, descrição, avatar, Vinho e Gin,
doçura "Doce", acidez ignorada, Touriga Nacional e Baga) — a BD ficou exatamente com isso (doçura 4/4, acidez vazia); 2.ª passagem a ignorar
quase tudo (perfil todo vazio, só acidez 2/2 e Whisky, zero castas: sem vinho o fluxo acabou na acidez); seta para trás da acidez para a
doçura (ignorada, por isso sem resposta); paginação das castas a deslizar. **Não exercitado ao vivo:** o estado de erro das listas ("TENTAR DE NOVO") e a falha ao
guardar no último ecrã (a API tinha de cair a meio); estão cobertos só por leitura de código e pelos testes das regras.

**Em aberto / a rever com o utilizador:** as diferenças face aos prints (`android/design/README.md`); o SVG do logótipo e os ícones do
Figma; o texto final dos termos (ver "Email de recuperação e popup dos termos", a seguir — o email já está ligado a sério).

### Email de recuperação e popup dos termos (feito e validado ao vivo, 2026-09-22)

Pedido do utilizador no fim da sessão da fatia 1b: fechar o dia com o envio real do código de recuperação por email e o popup do
texto dos termos (por agora com Lorem ipsum). Detalhe técnico em `backend/API_ENDPOINTS.md` ("Recuperação de password por email",
"Termos e condições") e `CLAUDE.md` (a receita do Mailpit, e um "gotcha" novo sobre `Dialog` a ecrã inteiro no Compose).

- **Backend — email:** `EmailService` (Spring Mail, `spring-boot-starter-mail`) envia o código em texto e HTML (sem ligações,
  só o código; nome do destinatário escapado no HTML). Servidor por `SPRING_MAIL_HOST`/`PORT`/`USERNAME`/`PASSWORD`
  (`AQUAVITAE_MAIL_FROM_ADDRESS`/`NAME` para o remetente) — **nunca no repositório**; sem `SPRING_MAIL_HOST`, em dev o código fica só
  no log (como antes), fora de dev só um aviso. Envio em `@Async`, para o pedido HTTP não esperar pelo SMTP nem denunciar pelo tempo
  de resposta se a conta existe. **Anti-abuso, pedido do utilizador:** um pedido novo invalida os códigos anteriores por usar, e só
  se gera código (e email) novo passado `aquavitae.recuperacao.intervalo-minimo-segundos` (60s) desde o último pedido da mesma
  conta — dentro do intervalo continua a responder 202. `PasswordResetRegras.kt` (3 testes) + `RecuperacaoPasswordEmail.kt` (6 testes).
- **Testado sem enviar nada real, depois a sério:** primeiro com um **Mailpit** local (`backend/docker-compose.mail-dev.yml`, perfil
  Spring `mailpit`) — confirmei ao vivo (`bootRun` + `curl`/PowerShell): o email chega, assunto e corpo em PT-PT corretos, HTML bem
  formatado (visto no browser pelo `Claude_Browser`); um 2.º pedido imediato não gera 2.º email (intervalo mínimo); passado o
  intervalo, gera um código novo e o antigo passa a dar 401 "Código inválido" (invalidado). **Depois, com o Gmail real** (o
  utilizador criou `aquavitaerecovery@gmail.com` e gerou a app password, guardada em `backend/.env.mail`, fora do git — eu nunca a
  vi): conta descartável com esse email, pedido de recuperação disparado, log confirma o envio, e o utilizador confirmou o email a
  chegar à caixa real. **Concluído.**
- **Backend — termos:** o texto passou de página estática (`static/legal/termos.html`) para um ficheiro simples,
  `src/main/resources/legal/termos.txt` (`# ` = título; parágrafos por linha em branco), lido por `TermosTexto.kt` (8 testes) e
  servido em dois formatos a partir da mesma fonte: `GET /api/legal/termos` (JSON, para o popup da app) e `GET /legal/termos.html`
  (a página pública, inalterada na URL). `termos.txt` tem hoje um texto de exemplo (Lorem ipsum, 4 secções) — combinado ficar assim
  até o utilizador dar o texto final.
- **Android — `TermsSheet`:** popup grená que sobe de baixo com o texto (`LegalRepository` + `TermsViewModel`, cache em memória
  depois do 1.º pedido), a substituir os antigos "abre `/legal/termos.html` no browser" do login, registo e recuperação de password.
  Fecha-se pelo botão "FECHAR", a arrastar o cabeçalho para baixo, a tocar fora ou com o gesto de voltar — as 4 validadas ao vivo.
  Feito sobre `Dialog` (não `ModalBottomSheet`, que não desenhava por baixo da barra de estado): apanhados e corrigidos dois defeitos
  do `Dialog` a ecrã inteiro no Compose (a janela dimensiona-se por omissão ao conteúdo; `navigationBarsPadding()` dentro do `Dialog`
  não recebia o inset certo e o botão ficava tapado pela barra de navegação, sem erro nenhum — só visível comparando com o esperado).
  Ambos documentados no `CLAUDE.md` para não se repetirem. Estado por defeito: a carregar (5 pontos) / erro com "TENTAR DE NOVO" / o
  texto, tudo ligado à API real.
- **Validado ao vivo** (emulador `Pixel_8` + API + Mailpit): recuperação de password ponta a ponta com o código a chegar por email
  real (Mailpit) em vez de por log; popup dos termos aberto a partir do rodapé do login, com as 4 formas de fechar testadas.

### Android — fatia 2a: homepage (feita e validada ao vivo, 2026-09-23)

Pedido do utilizador a abrir a sessão seguinte: o mockup da homepage (uma imagem só), o logótipo em `android/design/logos/`
(aplicado com fundo transparente) e os ícones da barra inferior em `android/design/icones/`. Detalhe completo do que foi pedido, do
que ficou feito e das diferenças conscientes em `android/design/README.md`, "14. Homepage" — aqui só o resumo. Prints e notas
copiados para `android/design/homepage/` antes de construir, como de costume.

**Backend:** nada por fazer para esta 1.ª versão — a homepage usa só endpoints já existentes (`GET /users/me`,
`GET /lookup/categorias-bebida`, `GET /bebidas` com filtro por categoria, `GET /caves` e `GET /caves/{id}`,
`GET /produtores/destaque`). O `getFavoritos`/`getWishlist` desatualizados (ver `backend/API_ENDPOINTS.md`) não são usados por este
ecrã, por isso não bloquearam.

**Android:**
- **Modelos sincronizados primeiro** (`BebidaModels`, `CaveModels`, `ProdutorModels`, `UserModels`) com o `BebidaSummaryDto` /
  `CaveResponse` / `CaveDetailResponse` / `UtilizadorMeDto` atuais do backend — trouxe campos novos (`precoDesde`,
  `retalhistaNome`, `corpo`, as estatísticas do perfil, o estado de cada garrafa na cave) e obrigou a rever os ecrãs que já usavam
  esses modelos (`CatalogScreen`, `DetailScreen`, `WishlistScreen`, `FavoritosScreen`, `CaveScreen` — só ajustes de nulidade, sem
  mudar comportamento).
- **`feature/home/`** (`HomeUiState`, `HomeViewModel`, `HomeScreen`): cabeçalho (saudação + data em PT-PT + avatar), pesquisa (só o
  botão — sem ecrã próprio ainda), "Escolhido para ti" (pílulas de categoria + 2 cartões, por rating desc.), "As minhas Caves"
  (pílulas de cave + lista de garrafas da selecionada + "+"), estatísticas do perfil (3 números), produtor em destaque. Carregamento
  independente por secção: só o `/users/me` bloqueia o ecrã (erro com "TENTAR DE NOVO"); sugestões/caves/produtor falham em
  silêncio para vazio/`null` sem arrastar o resto.
- **Barra de navegação principal** (`ui/components/BottomNavBar.kt`): 5 ícones (Catálogo, Cave, Home ao centro elevado, Wishlist,
  Favoritos), PNGs do Figma tingidos por `ColorFilter`/`Icon(tint=...)` (funciona seja qual for a cor original do PNG, desde que o
  alfa defina a forma — não foi preciso pedir variantes de cor). `AppNavHost.kt`: login/registo/onboarding passam a ir para `HOME`
  em vez de `CATALOG`; um `TabScreen` novo embrulha os 5 ecrãs principais com a barra sobreposta (`popUpTo(HOME){saveState=true}`).
- **`AvatarBadge.kt`** (novo, reutilizável): avatar SVG do onboarding ou, sem avatar, iniciais (nome+apelido, só nome, ou 2 letras
  do username) — **defeito apanhado a escrever os testes**: username vazio (`""`) não caía no `"?"`; corrigido com `.ifEmpty { null }`.
- **Assets:** logótipo com o fundo branco removido por chroma-key em PowerShell (`ic_wordmark_home.png`) e os 5 ícones da barra
  tingidos, todos copiados para `res/drawable/`. Fonte serifada dos títulos ("Escolhido para ti", saudação em itálico, nome da
  bebida): **aproximação com `FontFamily.Serif` do sistema** — a fonte exata do Figma é desconhecida, fica em aberto.
- **Decisão tomada sem parar para perguntar** (ainda de pé): "Escolhido para ti" usa
  `GET /bebidas?categoriaIds=X&sort=ratingMedio,desc&size=2` (não um endpoint de "sugestões" dedicado, que não existe com esse
  filtro). A linha de atributos do cartão, que na 1.ª parte da sessão mostrava só "VINHO • ENCORPADO" por faltar `tipo` no
  `BebidaSummaryDto`, **foi resolvida na fatia 2b** (`tipo`/`tanino` acrescentados à API + `BebidaCard` extraído para um
  componente partilhado com uma fórmula própria — ver "Android — fatia 2b").
- **Testes:** **45** unitários nesta parte (39 + 6 do `AvatarBadgeTest`; passou a 53 na fatia 2b). Compilou (`assembleDebug`) à
  primeira tentativa, sem erros.

**"Ver mais sugestões" e "As minhas Caves" fechados na 2.ª parte da sessão (2026-09-23):** o link "Ver mais sugestões" (mockup) não
tinha destino nem aparecia no ecrã — acrescentado ao `SectionHeader` de "Escolhido para ti" (o mesmo padrão já usado em "As minhas
Caves" → "Todas as caves"), a ir para o `catalog` (mesmo destino da pesquisa, sem filtro pré-aplicado — o catálogo a sério com o
filtro já escolhido fica para a fatia 2b). Para validar "As minhas Caves" com garrafas a sério, criei pela API uma cave para
`demo_user2` (`Adega Principal`, id 21) com 2 garrafas — uma "pronta a abrir" (`janelaInicio` no passado) e outra "em guarda"
(`janelaInicio` no futuro) — para exercitar os dois estados do `CaveDetailResponse`. Fica na BD de propósito como dado de teste.

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): login → homepage com saudação, avatar e
estatísticas reais; troca de categoria em "Escolhido para ti" e os cartões a atualizar; "Ver mais sugestões" a abrir o catálogo;
"As minhas Caves" com as 2 garrafas reais (quantidade, nome, categoria + janela de consumo formatada, preço `/UN`) — a lista
concatena `prontasAAbrir` + `emGuarda` sem separador visual entre estados, como esperado; navegação entre os 5 separadores da barra
inferior mantendo o estado (`saveState`/`restoreState`); produtor em destaque a aparecer quando existe um com bebidas.

**Em aberto / por fazer:** "VER PRODUTOR" e o "+" da cave continuam sem destino próprio — dependem de ecrãs que ainda não existem
(página do produtor, adicionar à cave), propositadamente fora desta fatia; decidir a pílula "Vinho 2025"; a fonte serifada exata. A
homepage em si está pronta para revisão do utilizador.

### Android — fatia 2b: catálogo e popup de filtros (feita e validada ao vivo, 2026-09-23)

Pedido do utilizador logo a seguir a fechar a fatia 2a, com dois mockups (`android/design/catalogo/`) e uma nota escrita dentro do
próprio mockup do popup, explicando o mapeamento dos campos a lookups da BD. Detalhe completo (o que foi pedido, o que ficou feito,
as diferenças conscientes) em `android/design/README.md`, "15. Catálogo / Filtros" — aqui só o resumo.

**Backend:** quase nada por fazer — `GET /api/bebidas` **já suportava desde a fatia 4** todos os parâmetros que o popup de filtros
precisa (`categoriaIds`, `paisId`, `regiaoIds`, `precoMin/Max`, `ratingMin`, `acidezMin/Max`, `docuraMin/Max`, `corpoId`, `taninoId`,
`tipoId`, `castaIds`), e todos os lookups também já existiam (`/lookup/paises`, `/lookup/regioes`, `/lookup/vinho/{corpos,taninos,
tipos}`) — só não estavam ligados ao Android. **Único acrescento:** `tipo`, `tanino` e `produtorRegiao` no `BebidaSummaryDto` (os
dois primeiros já existiam em `Vinho`, só não estavam expostos no resumo; o terceiro é uma navegação LAZY extra,
`bebida.produtor?.regiao?.nome`, o mesmo padrão já usado para `produtorNome`). Backend reiniciado a meio da sessão para carregar.

**Android:**
- **`CatalogFiltro.kt`** (novo, `data/model/`): espelha o `BebidaFiltro` do backend — um campo por filtro, mais `totalAtivos`
  (para o "LIMPAR N") e `limpo()` (mantém só a categoria).
- **`CatalogViewModel.kt`/`CatalogUiState`** (reescrito): guarda um filtro **aplicado** (o que a lista usa) e um **rascunho** (o
  que o popup edita) — só "Ver N bebidas" copia rascunho → aplicado; fechar de outra forma descarta. Uma contagem ao vivo no botão
  ("Ver N bebidas") pede a mesma pesquisa com `size=1` a cada edição, com 350ms de atraso (`Job` cancelado e relançado, não um
  operador `debounce` de Flow — mais simples de acertar). Também trata paginação ("Carregar mais", 20 de cada vez) e o sort.
- **`FiltrosSheet.kt`** (novo): o popup, sobre `Dialog` como o `TermsSheet` (`DialogScrim`/`DialogFillScreen`), mas sem arrasto
  para fechar (simplificação — o conteúdo já rola por dentro). Categoria (escolha única), origem (país + região, região só
  aparece depois de escolher país), preço (`RangeSlider` do Material3), rating mínimo, e, só com "Vinho", os atributos do vinho:
  acidez/doçura (`RangePillRow`, novo), tipo/corpo (pílulas) e castas (pesquisa + pílulas removíveis).
- **`RangePillRow.kt`** (novo, `ui/components/`): o seletor de intervalo 1–5 por toques (sem arrastar) — tocar noutro nível estica
  o intervalo até lá, tocar dentro do intervalo já escolhido fecha-o outra vez a um só nível.
- **`CatalogScreen.kt`** (reescrito, substitui o placeholder do esqueleto): título serifado, pesquisa com ícone de filtro
  (`Icons.Filled.FilterList`, já uma dependência — não precisei de pedir um ícone novo), pílulas de categoria sempre visíveis,
  contagem + "ORDENAR" (menu), pílulas removíveis dos filtros ativos, lista de cartões, "Carregar mais N bebidas".
- **`BebidaCard` tornou-se partilhado** (`ui/components/BebidaCard.kt`, saiu do `HomeScreen.kt`) com `BebidaSummary.linhaAtributos()`
  (`data/model/BebidaFormatacao.kt`, 8 testes): até 2 atributos por prioridade (corpo → tanino-se-tinto-senão-acidez → doçura) —
  substitui a simplificação "categoria + corpo" da fatia 2a nos dois ecrãs de uma vez.
- **Defeito apanhado e corrigido a testar ao vivo:** o `BebidaCard` tem altura fixa (108dp); com `tipo`/`tanino` a linha de
  atributos passou a ter 3 segmentos e, nalgumas bebidas, quebrava para 2 linhas — empurrava o preço para fora do cartão (sem
  *crash*, só visual). Corrigido com `maxLines = 1` + `TextOverflow.Ellipsis`, como as outras linhas do cartão já faziam. Ver
  `CLAUDE.md` para o "gotcha" completo (altura fixa + texto de comprimento variável).
- **Testes:** **53** unitários (45 + 8 do `BebidaFormatacaoTest`). Compilou (`assembleDebug`) e correu ao vivo sem *crashes*.

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): abrir o popup de filtros a partir do catálogo;
escolher região "Douro" + rating "4+" (contagem ao vivo "Ver 1 bebidas" a atualizar); aplicar (a lista filtra para 6 bebidas, chip
"Douro ×" aparece); remover a pílula (volta às 9); trocar de categoria para "Whisky" na lista principal (0 bebidas, "Sem bebidas com
estes filtros.", sem rebentar). **Não validado ao vivo:** "Carregar mais" (o catálogo de teste cabe sempre numa página) e o
`RangeSlider` do preço aplicado a sério (só visto a abrir, nunca com um intervalo estreito real).

**Em aberto / por fazer:** ordenar por preço (pede trabalho no backend, `precoDesde` não é uma coluna ordenável pelo `Pageable`
diretamente); os 7 tipos de vinho da API vs. os 4 do mockup (mostrei os 7, ver "Diferenças conscientes"); o preço nunca testado
com dados a sério. O catálogo em si está pronto para revisão do utilizador.

### Android — fatia 3a: popup de detalhe da bebida (feita e validada ao vivo, 2026-09-23)

Pedido do utilizador logo a seguir a fechar a fatia 2b, com 5 mockups de uma vez (`android/design/caves/`): a Cave (lista, "Nova
cave", "Adicionar à cave") e o **popup de detalhe de uma bebida** (tabs "Detalhes"/"Reviews"), partilhado por toda a app — "abre-se
ao pressionar num item duma bebida no catálogo (após pesquisa, antes de pesquisa, nas caves, nos favoritos, etc.)". **Só o popup de
detalhe ficou feito nesta 1.ª parte** (é a peça mais reutilizada, e a Cave também depende dele — o "+" que adiciona à cave vive lá).
Detalhe completo em `android/design/README.md`, "16." — aqui só o resumo.

**Backend:** um acrescento pequeno — `tipo`, `tanino` e `produtorRegiao` já tinham entrado no `BebidaSummaryDto` na fatia 2b, mas o
popup também precisava do `ReviewsResponse` (já existia desde a fatia de reviews, só a app é que estava desatualizada) e de marcar
"provada" antes de avaliar (`POST /bebidas/{id}/provada`, também já existia). **Nada de novo no backend.**

**Android:**
- **Interação:** o mockup só define o toque premido; sem outro destino para o toque curto, os dois abrem o mesmo popup
  (`BebidaCard.combinedClickable`, `ExperimentalFoundationApi`) — decisão documentada como diferença consciente. **Substitui** as
  antigas rotas `detail/{id}`/`reviews/{id}` (ficam no código como esqueleto morto, sem forma de lá chegar por toque).
- **`feature/bebidadetalhe/`** (`BebidaDetalheSheet`, `BebidaDetalheViewModel`, `BebidaDetalheUiState`): cabeçalho (imagem,
  categoria+tipo+ano, nome, produtor•região, rating); linha de ações (preço+retalhista se houver, favorito, wishlist, "+" adicionar
  à cave — otimistas, revertem se o pedido falhar); tabs "Detalhes" (teor/volume/país/ano, perfil sensorial em barras, pílulas de
  corpo/tanino/tipo, castas, cartão do produtor) e "Reviews" (a tua review — 5 estrelas + comentário + publicar —, distribuição por
  estrela, lista "Da comunidade" com avatar/nome/data relativa/rating/comentário).
- **Marca "provada" sozinho antes de publicar** (`POST .../provada`, idempotente) — o mockup não mostra esse passo, é o
  pré-requisito que a API já exigia desde a fatia de reviews (409 sem ele).
- **Sem rota:** `hiltViewModel(key = "bebida-detalhe-$bebidaId")` (uma chave por bebida, sem injeção assistida do Hilt) +
  `carregar(bebidaId)` por `LaunchedEffect`, em vez do `init {}`/`SavedStateHandle` habituais — ver `CLAUDE.md`.
- **`BebidaCard` e `linhaAtributos()` (fatia 2a/2b) validados na prática:** os cartões já mostravam "VINHO TINTO • CORPO ENCORPADO •
  TANINO ELEVADO" corretamente antes mesmo de construir este popup — a fórmula resolveu-se bem.
- **Testes:** sem testes novos (a lógica do ViewModel pediria `kotlinx-coroutines-test`, ainda não configurado — dívida já
  registada). Compilou e correu ao vivo sem *crashes*.

**Defeitos apanhados e corrigidos a construir/testar ao vivo** (detalhe em `android/design/README.md`, "16."): `data class Ready`
sem `: BebidaDetalheUiState` (esqueci o supertipo — Kotlin só avisa bem mais tarde, com erros de tipo confusos; ver `CLAUDE.md`);
`Modifier.weight()` usado fora de `ColumnScope` (função `@Composable` separada não herda o scope do `Column` que a chama); pílulas
de atributos sem `LazyRow` (texto a partir-se, o mesmo bug já visto no `FiltrosSheet`); a média grande da tab "Reviews" calculada a
partir da distribuição arredondada por estrela em vez de `bebida.ratingMedio` (dava "5,0" em vez de "4,5"); o cabeçalho por
atualizar depois de publicar uma review (só a lista de reviews recarregava, não a bebida).

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): popup aberto por toque premido a partir da
homepage; as duas tabs; alternar favorito e wishlist (cor muda logo, persiste ao reabrir); publicar uma review nova no "Vinha Grande
Tinto 2019" (0 reviews antes) — a distribuição, o total e o cabeçalho todos corretos, confirmado por `GET /api/bebidas/2`
(`ratingMedio: 4, totalReviews: 1`).

**Em aberto / por fazer, na altura:** a Cave (lista, "Nova cave", "Adicionar à cave" — feita a seguir, ver "fatia 3b"); meias-estrelas
na review (só inteiras, continua por fazer); limpar `feature/detail`/`feature/reviews` (esqueleto morto, continua por fazer).

### Android — fatia 3b: cave (feita e validada ao vivo, 2026-09-23)

Continuação direta do mesmo pedido da fatia 3a (os 5 mockups de uma vez), com um pedido extra do utilizador ao retomar: no popup
"Adicionar à cave", **destacar as caves onde aquela bebida já está**. Detalhe completo em `android/design/README.md`, "16." (secção
"Cave: lista, 'Nova cave' e 'Adicionar à cave'") — aqui só o resumo. Com esta fatia fecha-se o **fluxo principal do MVP**.

**Backend:** um acrescento pequeno — `GET /api/users/me/caves?bebidaId=` (opcional) acrescenta `temBebida: Boolean` a cada
`CaveResponse`, reaproveitando a mesma query em lote já feita para os totais (sem pedidos extra à BD). `POST .../consumir` já
existia desde a fatia 4 do backend, só não estava ligado ao Android.

**Android:**
- **`feature/cave/CaveScreen.kt`** (reescrito): pílulas de cave + "+" (popup "Nova cave"), 3 estatísticas da cave escolhida,
  "Prontas a abrir" com "Consumir" (recarrega a cave depois), "Em guarda" com "ORDENAR" (preço/janela).
- **`NovaCaveSheet.kt`** (novo): nome + descrição opcional, com contadores — reutilizado tal e qual dentro do
  `AdicionarACaveSheet` ("+ nova cave", sem sair do popup).
- **`AdicionarACaveSheet.kt`** (novo, do "+" do popup de detalhe): pílulas de cave com **duas marcas independentes** — cheia a
  grená se escolhida para este "adicionar", com ✓ se a bebida já lá está (`temBebida`); quantidade, preço, data de aquisição
  (`DatePicker` do Material3, 1.ª vez usado no projeto), janela de consumo (2 campos de ano), notas.
- **Os dois popups empilham-se** (detalhe fica aberto por baixo do "Adicionar à cave") em vez de um fechar o outro.
- **Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`). Compilou e correu ao vivo sem *crashes*.

**Defeito apanhado e corrigido a testar ao vivo (condição de corrida):** `CaveViewModel.criarCave` fazia `carregar()`
(assíncrono) e logo a seguir `selecionarCave(nova.id)` — a resposta do `carregar()` podia chegar depois e repor a seleção para
a 1.ª cave da lista. A cave nova ficava criada mas não selecionada, sem erro nenhum. Corrigido fazendo tudo na mesma corrotina,
pela ordem certa. Ver `CLAUDE.md` para o "gotcha" completo.

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): "Consumir" numa garrafa pronta (quantidade e
estatísticas a recalcular ao vivo); criar uma cave nova pelo "+" da Cave (selecionada, depois do conserto); "Adicionar à cave" a
partir do popup de detalhe da "Barca Velha 2015" — "Adega Principal" com ✓ e selecionada, "Tintos de guarda" sem ✓; escolher
"Tintos de guarda", preencher preço e janela (2030–2035), guardar — confirmado por `GET /api/caves/22`
(`janelaInicio: "2030-01-01"`, `janelaFim: "2035-12-31"`, `precoPago: 34.5`); criar uma 2.ª cave nova a partir do próprio popup
"Adicionar à cave" (também selecionada de imediato).

**Em aberto / por fazer:** "Ver as N garrafas" sem destino (a lista já mostra tudo, sem paginação); mover uma garrafa entre caves
(não pedido nos mockups); editar uma garrafa já na cave (o `PATCH` existe na API e no repositório, sem UI); limpar
`feature/detail`/`feature/reviews` (esqueleto morto).

### Android — fatia 3c: ajustes de feedback + "Já provadas" (feita e validada ao vivo, 2026-09-23)

O utilizador testou a fatia 3 completa ("Já testei tudo e adorei!") e devolveu 5 ajustes, com um mockup novo ("Bebidas já
provadas") e 3 imagens de contexto (Favoritos/Wishlist/Caves — não pedidas para construir agora). Detalhe completo em
`android/design/README.md`, secção "Ajustes de feedback + 'Já provadas'" — aqui só o resumo.

**Backend:** nenhuma mudança — tudo já existia (`GET /users/me/provadas?categoriaId=&ano=`, `POST/DELETE .../provada`,
`POST .../consumir`).

**Android:**
1. **Popup de confirmação ao duplicar numa cave:** `AdicionarACaveSheet` mostra um `AlertDialog` ("Já tens esta bebida
   aqui" / "Adicionar na mesma" / "Cancelar") se a cave escolhida já tiver a bebida (`temBebida`), em vez de gravar direto.
2. **Review a exigir "provada" (reversão da fatia 3a):** publicar uma review deixou de marcar "provada" sozinho; agora
   bloqueia o formulário com um aviso se `isProvada == false`. Regra do utilizador: "uma bebida só pode ter review de um
   utilizador quando ele já a tem na sua lista de consumidos e somente nessa lista" — nunca automaticamente ao avaliar.
3. **"Consumir" também marca "provada"** (`CaveViewModel.consumir` chama `addProvada` a seguir, idempotente) — o 2.º
   caminho é adicionar diretamente à lista de já provadas (ponto 5).
4. **Resumo "Já provadas" na Cave:** até 5 bebidas por baixo de "Em guarda", com "ABRIR MAIS" → ecrã inteiro.
5. **Ecrã "Já provadas" novo** (`feature/provadas/`): filtros (nota/categoria), agrupamento por mês com "carregar mais
   antigas", e uma **barra de pesquisa fora do mockup** (pedida explicitamente pelo utilizador) para adicionar uma bebida
   que não esteve em nenhuma cave.

**Resposta à pergunta do utilizador ("como surgem as garrafas 'Em guarda'"):** é o `EstadoCaveBebida` do backend
(`CaveRegras.kt`, sem mudanças): sem janela de consumo, ou com `janelaInicio` ainda no futuro → `EM_GUARDA`; dentro da
janela → `PRONTA`; depois de `janelaFim` sem consumir → `EM_ATRASO`. Ou seja, cai em "Em guarda" sempre que não há
informação (ou é cedo demais) para dizer que já está pronta a abrir.

**Defeito apanhado e corrigido a testar ao vivo:** as linhas de garrafa da Cave (`GarrafaRow`) e o novo `ProvadaResumoRow`
não tinham nenhum toque/premido ligado ao popup de detalhe — o mockup original da fatia 3 já dizia "nas caves" como um dos
sítios onde o popup abre, mas a ligação nunca tinha sido feita. Corrigido com o mesmo padrão do `BebidaCard`
(`combinedClickable`), `onBebidaClick` passado de `CaveScreen` até às duas linhas. Ver `CLAUDE.md` para a lição geral.

**Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`). `assembleDebug testDebugUnitTest` continua com 53,
todos a passar.

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): as 5 alterações, uma a uma — popup de
duplicado no "Esporão Reserva Tinto 2018" (já em "Adega Principal"); bloqueio da review no mesmo bebida (`isProvada: false`);
"Consumir" na "Barca Velha 2015" (idempotente, já estava provada de uma review anterior); resumo "Já provadas" na Cave
("Vinha Grande Tinto 2019 · 4,0", "Barca Velha 2015 · 4,5"); ecrã inteiro com filtros, agrupamento por mês e busca (adicionou
"Gin 44°" pela busca, depois removido — dado de teste, não fica na BD).

**Em aberto / por fazer:** o mockup "Bebidas já provadas" não foi copiado para `design/caves/` (chegou depois de um resumo
de contexto desta sessão, sem ficheiro em disco); Favoritos/Wishlist continuam por construir (não pedidos nesta fatia); os
antigos `DetailScreen`/`ReviewsScreen`/rotas continuam como código morto.

### Correção pós-3c: favoritos/wishlist e totais da cave (feita e validada ao vivo, 2026-09-23/24)

O utilizador testou a fatia 3c por conta própria, na sua conta pessoal (não a `demo_user2`), e devolveu 3 bugs (o 3.º só depois
de corrigido o 1.º) — resumo aqui, detalhe técnico em `backend/API_ENDPOINTS.md`, "Sincronização pendente com o Android"
("correção pós-3c").

**Bug 1 — "Favoritos"/"Wishlist" vazios ou em erro:** `AquaVitaeApi.getFavoritos`/`getWishlist` declaravam
`List<BebidaSummary>`, mas a API sempre devolveu `List<BebidaRelacaoDto>` (`{ bebida, data, hasReview? }`) — o Moshi falhava
com `Required value 'id' missing at $[1]` (tentava ler `id` no wrapper, não em `bebida.id`), e os dois ecrãs ficavam sempre
vazios ou com esse erro. Mesma classe de bug já corrigida no `getProvadas` (fatia 3c) — só faltava replicar aqui. Corrigido:
`AquaVitaeApi`/`FavoritoRepository`/`WishlistRepository` passam a `List<BebidaRelacao>` (`getWishlist` ganhou também `?sort=`,
que a API já aceitava); `FavoritosViewModel`/`WishlistViewModel`/`*Screen.kt` atualizados para desembrulhar `.bebida`. As duas
telas continuam com a UI simples do esqueleto (fatia 4 do plano original é que as desenha a sério) — só a leitura de dados é
que estava partida.

**Bug 2 — "INVESTIDOS"/"GARRAFAS" da Cave sem atualizar:** o utilizador criou "Vinhos 2026"/"Gins 2026" e reparou que, depois
de guardar uma garrafa pelo popup "Adicionar à cave" (aberto de dentro da própria Cave ou da homepage), os totais no topo
ficavam com o valor de antes de adicionar — só a lista de garrafas por baixo (sempre pedida de novo ao trocar de cave) é que
estava certa. **Não era bug do cálculo** (`CaveRegras.resumir`, backend: soma `precoPago × quantidade` de cada linha ativa,
exatamente a regra pedida pelo utilizador — "cada quantidade conta como mais 1 valor do preço a adicionar" — já estava
correta e confirmada ao vivo). Era só a app: `GET /users/me/caves` só era pedido de novo em `carregar()`/`criarCave()`/
`consumir()`; guardar pelo `AdicionarACaveSheet` (uma `ViewModel` à parte) não disparava nenhum desses. Corrigido com
`CaveViewModel.atualizarAposGuardar()`/`HomeViewModel.atualizarAposGuardar()` (pedem `getCaves()` de novo e o detalhe/garrafas
da cave atual, sem repor a seleção nem mostrar o ecrã de carregamento inteiro), chamados no `onGuardado` do
`AdicionarACaveSheet` nesses dois ecrãs.

**Bug 3 — Favoritos/Wishlist ainda presos à 1.ª visita, mesmo depois do bug 1:** com o modelo corrigido, o utilizador voltou a
testar e reparou que marcar um favorito noutro ecrã e voltar à aba "Favoritos" continuava a não mostrar a mudança (sem erro,
só "Ainda não tens favoritos"). Causa: `FavoritosViewModel`/`WishlistViewModel` só pediam os dados no `init {}`; como
`home`/`catalog`/`cave`/`wishlist`/`favoritos` usam `saveState`/`restoreState` (`AppNavHost.kt`), trocar de aba não recria a
ViewModel. Ao contrário do bug 2 (que tinha o `onGuardado` do `AdicionarACaveSheet` a que se ligar), aqui não há popup
nenhum — marca-se um favorito a partir de **qualquer** ecrã. Corrigido com `LaunchedEffect(Unit) { viewModel.load...() }` no
topo de `FavoritosScreen`/`WishlistScreen`: o Navigation Compose desmonta e remonta o conteúdo da rota a cada troca de aba, por
isso isto volta a pedir dados frescos de cada vez que a aba é reaberta (mesmo mantendo a mesma instância da ViewModel).

**Testes:** sem testes novos. `assembleDebug testDebugUnitTest` continua com 53, todos a passar.

**Validado ao vivo** (emulador `Pixel_8` + API real, **conta pessoal do utilizador**): criou "Gins 2026" (vazia), adicionou
"Esporão Reserva Tinto 2018" a 50,00€ pelo popup aberto de dentro da própria Cave — as pílulas "GARRAFAS"/"INVESTIDOS" da cave
atualizaram de imediato (1/50,00€), sem sair do ecrã; marcou favorito + wishlist na mesma bebida e os ecrãs "Favoritos"/
"Wishlist" passaram a mostrá-la (bug 1); depois, a partir da Cave, marcou/desmarcou o favorito da "Barca Velha 2015" e
confirmou que a aba "Favoritos" refletia a mudança ao reabri-la, nos dois sentidos (bug 3). Dados de teste revertidos no fim
(token extraído do `DataStore` via `adb run-as` só para chamar `DELETE .../bebidas/{id}` na cave, `.../favorito` e
`.../wishlist` — a conta do utilizador ficou tal como estava antes destes testes).

### Android — fatia 4: Favoritos e Wishlist a sério (feita e validada ao vivo, 2026-09-24)

O utilizador pediu para desenhar os dois ecrãs a partir de um mockup próprio (`android/design/favoritos-wishlist/`, feito a
partir da app com anotações técnicas por cima — não é o Figma original). Detalhe completo em `android/design/README.md`,
"Favoritos e Wishlist" — aqui só o resumo.

**Backend:** nenhuma mudança — decisão combinada com o utilizador (ver "Adiado — Por fazer depois"): 5 campos que o mockup
pede ficam para depois (link de compra completo, data de verificação, contagem de ofertas, quantidade na cave, data da
provada); por agora usa-se só o que o `BebidaSummaryDto` já tinha.

**Android:**
- **`feature/favoritos/`** (reescrito): filtro por categoria (só as presentes nos favoritos do utilizador, não o lookup
  inteiro), estatísticas "N favoritos • N provadas • N com nota tua" (a 2.ª vem de `GET /users/me`, as outras duas
  calculadas a partir da própria lista), cartão com nota própria vs. média da comunidade e "Ver a tua review"/"Avaliar"
  (abre o popup de detalhe da bebida — o próprio popup já bloqueia "Avaliar" se a bebida não estiver provada, fatia 3c);
  "Ver as N garrafas favoritas" mostra só 3 por omissão, expande ao tocar.
- **`feature/wishlist/`** (reescrito): pílulas de ordenação "Recentes"/"Preço"/"Rating" — ordenadas no telemóvel (a lista já
  vem inteira, sem paginação, decisão combinada com o utilizador); "ADICIONADA há N dias"/"em \<mês\>"; "Comprar em X" (por
  agora abre o popup de detalhe, falta o link real — ver acima) e "Para a cave" (busca `GET /bebidas/{id}` sozinho e abre o
  `AdicionarACaveSheet` diretamente, sem passar pelo popup de detalhe — a lista só tem `BebidaSummary`).
- **Remover um favorito/wishlist:** toca-se no coração/marcador do próprio cartão, otimista (sai da lista de imediato,
  volta a aparecer se o pedido falhar), sem confirmação.
- Os dois ecrãs saíram do `LegacyScreen` em `AppNavHost.kt` (já têm `systemBarsPadding()` próprio, como `home`/`catalog`/`cave`).
- **Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`). `assembleDebug testDebugUnitTest` continua com 53.

**Validado ao vivo** (emulador `Pixel_8`, conta pessoal do utilizador): filtro "Vinho" nos favoritos a mostrar só as 2 bebidas
de vinho; "Ver a tua review" a abrir o popup certo (Barca Velha 2015); "Para a cave" na wishlist a buscar o detalhe e abrir
`AdicionarACaveSheet` diretamente (confirmado com "Vinha Grande Tinto 2019", sem passar pelo popup); remover e voltar a
adicionar o mesmo item da wishlist pelo marcador — a lista atualizou-se corretamente nos dois sentidos.

**Em aberto / por fazer:** os 5 campos da API adiados (ver "Adiado — Por fazer depois"); os antigos
`DetailScreen`/`ReviewsScreen`/rotas continuam como código morto; a página do produtor e o perfil ficam para depois.

### Android — fatia 5: ecrã de perfil (feita e validada ao vivo, 2026-09-24)

O utilizador pediu para desenhar o ecrã de perfil a partir de 4 mockups próprios (`android/design/perfil/`): ver dados/editar
perfil, editar detalhes do perfil, escolher avatar (popup) e editar preferências (popup). Detalhe completo em
`android/design/README.md`, "Perfil" — aqui só o resumo.

**Backend:** nenhuma mudança — tudo já existia e validado (`GET/PUT /api/users/me`, `GET/PUT /api/users/me/preferencias`, o
`PUT` já aceitava `username`, validado e único sem distinguir maiúsculas, 409 se já em uso). Só faltava ligar o `GET` das
preferências na app (nunca tinha sido usado desde a fatia 2a) e acrescentar `username` ao `UtilizadorUpdateRequest` do Android.

**Android:**
- **`feature/perfil/`** (novo, 6 ficheiros): `PerfilScreen` — avatar+nome+"@username"+nacionalidade·membro desde, bio, grelha
  2×3 de estatísticas (provadas/reviews/favoritos/wishlist/caves/garrafas, tudo de `GET /users/me`), cartão de preferências
  (tipos de bebida, intervalo de doçura/acidez, castas, "EDITAR"), navegação para "Histórico de provadas" (já existe) e "As
  minhas caves" (já existe), "As minhas reviews"/"Conta e segurança" sem destino (mockups não enviados ainda), "Terminar
  sessão".
- **`EditarPerfilScreen`** — nome, apelido, username (com contador e aviso de unicidade), nacionalidade (dropdown com
  bandeira), bio (com contador, suporta emoji por fallback de fonte do sistema, sem código extra), avatar (toca para abrir o
  popup de escolha); **desvio combinado com o utilizador**: mostra o email associado por baixo da bio, ao contrário do mockup.
- **`EscolherAvatarSheet`** (popup) — igual ao passo do onboarding, mais uma pílula "Todos"; só confirma a escolha localmente,
  quem grava é o "Guardar" do ecrã de editar perfil. Componente `AvatarGridPicker`/`AvatarTileGrid` extraído para
  `ui/components/` e reaproveitado pelo passo de avatar do onboarding (~50 linhas de duplicação removidas).
  `nationalityId` (a API só devolve o nome) resolve-se do lado da app comparando o nome na lista de `GET
  /api/lookup/nacionalidades` — seguro porque é uma lista curada sem nomes repetidos, não pediu mudança no backend.
- **`EditarPreferenciasSheet`** (popup) — pílulas de tipos de bebida, `RangePillRow` reaproveitado do popup de filtros do
  catálogo para doçura e acidez, busca+chips de castas (mesmo padrão do `FiltrosSheet`); guarda com `PUT
  /users/me/preferencias` e já afeta "Escolhido para ti" na homepage (comportamento existente desde a fatia 2a).
- Avatar do cabeçalho de `HomeScreen`/`FavoritosScreen`/`WishlistScreen` passa a abrir o perfil (`AvatarBadge` ganhou
  `onClick` opcional); `HomeScreen` ganhou também `LaunchedEffect(Unit) { carregar() }` para mostrar avatar/nome atualizados
  ao voltar da edição (mesmo padrão da correção pós-3c em Favoritos/Wishlist).
- **Gotcha repetido (já em `CLAUDE.md`):** `Modifier.weight()` dentro do conteúdo de um `Dialog` só funciona se a função for
  `ColumnScope.Conteudo(...)` — apanhado de novo em `EditarPreferenciasSheet.kt` e `EscolherAvatarSheet.kt`, corrigido da
  mesma forma que no `BebidaDetalheSheet` (fatia 3a).
- **Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`). `assembleDebug testDebugUnitTest` continua com 53.

**Validado ao vivo** (emulador `Pixel_8`, conta `demo2@aquavitae.local` — a sessão da conta pessoal do utilizador tinha
expirado a meio da sessão; nunca se pediu a password real, regra absoluta): as 6 estatísticas, cartão de preferências,
`EditarPerfilScreen` com todos os campos pré-preenchidos (incluindo `nationalityId` resolvido por nome),
`EscolherAvatarSheet` ("Todos" + filtro por categoria + seleção + confirmar), `EditarPreferenciasSheet` (pílulas, intervalo de
doçura/acidez a estender corretamente nos dois lados, busca+seleção+remoção de castas), ida-e-volta de gravar e ver refletido
de imediato no cartão de perfil (perfil e preferências), navegação para "Histórico de provadas", "Terminar sessão" (limpa a
sessão e o backstack até ao login). Avatar a abrir o perfil a partir de Home, Favoritos e Wishlist, confirmado nos 3.

**Em aberto / por fazer:** "As minhas reviews" e "Conta e segurança" (mockups ainda não enviados); emoji na bio só verificado
por revisão de código, não ao vivo (`adb shell input text` não consegue injetar emoji — limitação da ferramenta de teste, não
da app).


### Android — fatia 6: página do produtor (feita e validada ao vivo, 2026-09-24)

O utilizador enviou um mockup ("Detalhes do produtor", `android/design/produtor/01-produtor.png`) com as notas: a página tem
detalhes do produtor, se recebe visitas, quantos itens tem no catálogo, localização (o mapa abre a app de mapas), "Ler a
história completa" (popup, se houver história), "Garrafas em catálogo" com **até 6 itens — os 3 primeiros com "CARREGAR MAIS
3", sem carregar mais que isso, e a seta a levar ao catálogo do produtor** (parecido com o catálogo, focado num só produtor),
o "manter premido" da bebida como em todo o lado, a imagem do produtor no cabeçalho, e uma **sugestão: um rating geral do
produtor**. Acessível pelo cartão do produtor em destaque e pelo cartão do produtor no fim do popup de detalhe de uma bebida.
Detalhe completo em `android/design/README.md`, "Produtor" — aqui só o resumo.

**Backend (2 acrescentos, o resto já existia):**
- `ProdutorDetailDto` ganhou **`ratingMedio`**, **`totalReviews`** e **`categorias`**. O rating geral é a média de todas as
  reviews das bebidas do produtor: `SUM(rating × reviews) / SUM(reviews)` sobre `bebida_rating_medio`/`bebida_total_reviews`
  (já mantidos pelo trigger de `review`), 2 casas (`ProdutorRating.media`, com 5 testes). **Ponderado de propósito** — uma
  bebida com 1 review de 5,0 não pesa como outra com 200 de 4,0; sem reviews é `null` ("Sem reviews ainda", nunca 0,0).
  O "produtor da semana" continua a ordenar pela média **simples** (aprovada em 2026-09-19): são duas medidas diferentes.
  `categorias` (`[{id, nome}]`) são as categorias em que o produtor tem bebidas — os separadores do catálogo do produtor.
  Duas queries novas em `BebidaRepository` (validadas pelo `HqlQueriesTest`, que já apanha erros de HQL sem BD).
- **Depois da aprovação do utilizador (mesmo dia):** coluna nova **`produtor_morada`** (`VARCHAR2(300 CHAR)`, anulável; `01_tables.sql`
  + patch `14`, repetível; `rebuild-check.sh` verde) → `Produtor.morada` e `ProdutorDetailDto.morada`; e o filtro **`produtorId`** em
  `GET /api/bebidas` (`BebidaRepository.search`, `BebidaFiltro`, controller e service; `sugeridas` passa `null`), validado ao
  vivo (`?produtorId=1` só devolve as dele; combina com `search` e `categoriaIds`).
- `GET /produtores/{id}/bebidas?categoriaId=&page=&size=&sort=` já existia e serve a lista de 6 da página do produtor.

**Android:**
- **`feature/produtor/`** (novo, 5 ficheiros): `ProdutorScreen` (cabeçalho com imagem sob a barra de estado e o cartão branco a
  sobrepor-se, botão de voltar fixo, pílulas em `FlowRow`, rating geral, história com "LER A HISTÓRIA COMPLETA" só se o texto
  passa das 4 linhas, cartão "LOCALIZAÇÃO", "Garrafas em catálogo" com a seta e as linhas planas do mockup), `ProdutorViewModel`
  (detalhe + 6 primeiras garrafas em paralelo; `atualizar()` silencioso ao fechar o popup de uma bebida), `HistoriaProdutorSheet`
  (popup). Rotas `produtor/{id}` e `produtor/{id}/catalogo` (id por `SavedStateHandle`).
- **Catálogo do produtor = o próprio `CatalogScreen`/`CatalogViewModel`** (a 1.ª versão era um ecrã simples à parte, sem pesquisa
  nem filtros — **apagado**): a ViewModel lê o `produtorId` do argumento de navegação e o ecrã muda de cara (seta de voltar,
  "Garrafas de <produtor>", pílulas "Todas" + só as categorias dele, popup de filtros **sem "ORIGEM"**, sem barra de navegação,
  sem atalho "VER PRODUTOR" no popup). `CatalogFiltro.produtorId`; `selecionarCategoriaAtiva/Rascunho` aceitam `null` ("Todas").
  Regressão do catálogo geral confirmada ao vivo.
- **Localização (sem mapa embebido):** cartão com a morada em texto (ou, sem morada, as coordenadas) e "Abrir no mapa" → intent
  `geo:` para a app de mapas do sistema: com coordenadas `geo:lat,lon?q=lat,lon(Nome)` (o ponto exato), sem elas
  `geo:0,0?q=<morada>`; sem app de mapas, o Google Maps no browser. (O Maps SDK é grátis sem limite mas pede projeto Google Cloud
  com faturação e chave — decisão do utilizador: sem mapa desenhado na 1.ª versão.) Validado ao vivo: por morada o Maps reconheceu
  a Quinta do Vale Meão; por coordenadas abriu no ponto certo.
- **`BebidaDetalheSheet`** ganhou `onVerProdutor: ((Long) -> Unit)? = null`: o cartão do produtor passa a ser tocável, com uma
  linha "VER PRODUTOR →"; fecha o popup e o ecrã navega. Ligado em home, catálogo, cave, favoritos e wishlist; **`null` nas
  páginas do próprio produtor** (não faz sentido "ver o produtor" onde já estamos). O cartão do produtor em destaque da home
  passou a ser tocável por inteiro (antes só o botão).
- **`ContagemEOrdenacao`** extraído do `CatalogScreen` para `ui/components/` (genérico no tipo da opção) e partilhado com o
  catálogo do produtor; `data/model/ProdutorFormatacao.kt` (domínio do site, URL, coordenadas, URI `geo:`).
- **Testes:** +15 Android (`ProdutorFormatacaoTest`, `ProdutorUiStateTest` — 68 no total) e +5 backend (93).

**Validado ao vivo** (emulador `Pixel_8` + API real, `demo_user2`): como nenhum produtor da BD tem história, coordenadas nem
imagem, carreguei **dados de teste temporários** no produtor 1 (Casa Ferreirinha) e movi 7 bebidas para ele (9 no total, 8
vinhos + 1 gin) — **já revertidos**, com SQL de reversão e conferência (mapeamento das 16 bebidas idêntico ao original). Cabeçalho,
pílulas, rating geral (3,38 = (4,25×2 + 4,0 + 1,0)/4), história em popup, **"Abrir no mapa" a abrir o Google Maps exatamente
nas coordenadas com o marcador "Casa Ferreirinha"**, 3 → "CARREGAR MAIS 3" → 6 → "VER AS 9 GARRAFAS", toque e premido a abrir o
popup, catálogo do produtor (Todas/Vinho/Gin, ordenar A-Z, popup, voltar pela pilha com o estado mantido), e os 3 pontos de
entrada (cartão em destaque, popup do catálogo, popup da cave). Um produtor com poucos dados (Quinta do Vale Meão) e uma história
curta (sem ligação "LER A HISTÓRIA COMPLETA") também confirmados. Não exercitado ao vivo: o estado de erro ("TENTAR DE NOVO") e a Wishlist/os Favoritos como ponto de entrada (o mesmo código
dos outros, só compilado).

**Em aberto / por fazer:** nenhum produtor real tem ainda história, morada, coordenadas nem imagem — **dado a preencher à mão**,
quando o utilizador o tiver; a secção "Produtor" (pesquisa + chips) no popup do catálogo **geral** — só com centenas de
produtores; `regiaoId` do produtor por usar; a dívida vista de passagem ("1 reviews", `ProvadasScreen` sem popup) está na
"Ronda de bugs" dos próximos passos.

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
- **Reconstrução do zero verificada** (o script ficou guardado em `database/verify/rebuild-check.sh`, com testes de controlo que
  o veem falhar de propósito): num schema temporário (`aq_rebuild`, já apagado) corri os 5 scripts do `run-migrations`
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
