# AquaVitae — contexto para o Claude Code

App Android (Kotlin) de catalogação e comunidade de vinhos e bebidas espirituosas, focada no mercado
português. Contexto de produto completo em [`briefing/aqua-vitae-mvp-briefing.md`](briefing/aqua-vitae-mvp-briefing.md)
— lê isso primeiro se for a tua primeira vez neste projeto. **Estado atual e próximos passos em
[`PLANO.md`](PLANO.md) (começa pela secção "Retomar aqui")**; contrato dos endpoints (existentes e em falta) em
[`backend/API_ENDPOINTS.md`](backend/API_ENDPOINTS.md), que é a fonte da verdade da API. **Frontend (Android):** estado, estrutura,
convenções, componentes e mapa de ecrãs em [`android/README.md`](android/README.md); os prints do Figma e **o que o utilizador pediu para
cada ecrã** em [`android/design/README.md`](android/design/README.md) — ler os dois antes de mexer no Android.

## Stack e localização

```
briefing/    briefing de produto + schema DBML original
database/    Oracle XE (Docker) — DDL, triggers, seed
backend/     API Spring Boot + Kotlin (recursos estáticos, ex. avatares, em src/main/resources/static) + painel de administração web em /admin
android/     app Android (Compose + MVVM + Hilt) — construída por fatias (feitas: 1 a 6, até à página do produtor; ver android/README.md)
android/design/   prints do Figma + especificação por ecrã (o que foi pedido, estado, diferenças)
docs/        landing page (GitHub Pages, só na branch main) — o URL foi enviado à Awin, não mexer no repo/domínio
```

- **BD:** Oracle XE 21c via Docker (`gvenzl/oracle-xe`), gerido por `database/docker-compose.yml`.
  Schema em `database/ddl/`, seed em `database/seed/`. `database/run-migrations.ps1` aplica tudo.
- **Backend:** Kotlin 2.4.20 + Spring Boot 3.5.16 + Gradle 9.7.1 (wrapper já commitado, `./gradlew bootRun`).
  Hibernate em `ddl-auto: validate` — o schema é sempre gerido pelo SQL em `database/ddl`, nunca pelo Hibernate.
- **Android:** Kotlin + Jetpack Compose + Hilt + Retrofit/Moshi + DataStore. Construído por **fatias verticais**, uma por fluxo de ecrãs
  do Figma, ligada à API real (feitas as fatias 1a a 6: auth/onboarding, homepage, catálogo, popup de detalhe, cave, favoritos/wishlist,
  perfil e produtor; só `detail`/`reviews` continuam esqueleto morto, ver `android/README.md`). O `AquaVitaeApi.kt` está sincronizado com
  tudo o que as fatias usam (lista em `android/README.md`, "Lacunas conhecidas"); o que falta sincronizar está em
  `backend/API_ENDPOINTS.md`, "Sincronização pendente com o Android". No fim de cada fatia cumpre-se a checklist de `android/README.md`.

## Convenções importantes (para não repetir bugs já apanhados)

**Backend / JPA**

- **Qualquer coluna Oracle `NUMBER(p,s)` mapeia para `java.math.BigDecimal` na entidade JPA, nunca
  `Double`/`Float`.** Hibernate mapeia Double para SQL `FLOAT`, que falha a validação de schema contra
  `NUMBER` (`SchemaManagementException`). Já apanhado e corrigido uma vez em vários campos
  (`bebida.ratingMedio`, `review.rating`, `produtor.latitude/longitude`, etc.) — aplica desde o início em
  qualquer entidade nova.
- **Nunca aceder a uma associação `@ManyToOne(fetch = LAZY)` fora de um pedido web com sessão aberta** — no
  `JwtAuthenticationFilter` (corre antes do Open-Session-In-View), mas também em jobs `@Scheduled`/threads próprias
  — sem um fetch explícito: `LazyInitializationException` aí manifesta-se ao cliente como um 401 genérico (no
  filtro) ou só nos logs (nos jobs). Padrão: query com `JOIN FETCH` (`UtilizadorRepository.findByIdWithRole()`,
  `BebidaLinkCompraRepository.findAllParaVerificacao()`), ou `@Transactional(readOnly = true)` no service de leitura que
  navega LAZY em cadeia (ex.: `BebidaService.getDetail`, bebida → produtor → região). **O utilizador do token
  (`@AuthenticationPrincipal`) vem só com o papel carregado**: para ler o avatar ou a nacionalidade, recarregá-lo com
  `UtilizadorRepository.findByIdWithProfile()` (ver `ReviewService.autorComAvatar`).
- **Jobs longos atualizam só as colunas que lhes dizem respeito** (`@Modifying` + `@Query`), nunca um `save()` da
  entidade carregada no início — senão sobrescrevem alterações feitas entretanto (p.ex. um preço mudado por SQL).
- **Toda a paginação tem `ORDER BY` determinístico** (sem ele o "carregar mais" repete/salta itens). Se o cliente
  não pedir `sort`, o service aplica um por omissão (ver `Pageable.comOrdenacaoPadraoDeBebidas`, que também
  acrescenta o `id` como desempate quando o cliente escolhe o `sort`).
- **Padrão supertype/subtype:** `bebida` é a tabela mãe; cada categoria (`vinho`, `whisky`, `gin`, ...)
  tem tabela própria com `bebida_id` como PK e FK partilhada. No Kotlin isto é `@OneToOne` + `@MapsId`
  (ver `Vinho.kt`). Só `vinho` tem entidade/DTO completos para já — as outras categorias seguem o mesmo
  padrão quando forem necessárias (ver comentário em `BebidaService.buildVinhoDetalhe`).
- **Segurança:** o filtro JWT autentica sempre que houver um Bearer válido, mesmo em rotas `permitAll` (por isso
  `@AuthenticationPrincipal(errorOnInvalidType = false)` dá o utilizador se estiver logado). `/api/admin/**` exige
  `ROLE_ADMIN`. O `SecurityConfig` tem de deixar passar o dispatch `ERROR`, senão um 403 vira 401.

**Dados e schema**

- **Tabelas de conteúdo geridas pelo admin** (`bebida`, `produtor`, `retalhista`, `bebida_link_compra`,
  todos os lookups incluindo `casta`) **não têm endpoints de escrita na API** — só leitura. Inserção/edição
  é direta via SQL Developer ou `sqlplus` (decisão do briefing, secção 4). Só a API escreve dados gerados
  pelo utilizador (conta, reviews, favoritos, wishlist, cave, "provadas", preferências). **Mudou por fatias com o painel web `/admin`
  (abaixo): desde 2026-09-24 (fatia 2) os PRODUTORES criam-se e editam-se por lá; bebidas, links, retalhistas e lookups continuam só por
  SQL até às fatias 3+ (reescrever esta regra a cada fatia que escreva algo novo).**
- **Painel de administração web (2026-09-24, fatias 1 e 2: base, listas e formulário de produtores):** páginas HTML no próprio backend (`admin/` em
  `pt.aquavitae.api`, templates em `src/main/resources/templates/admin/`, CSS em `static/admin/css/`), **Thymeleaf + htmx** (o htmx vem
  do webjar `org.webjars.npm:htmx.org`, servido em `/webjars/htmx.org/dist/htmx.min.js`; sem CDN nem build de front-end). Tem **a sua
  própria `SecurityFilterChain` (`@Order(1)`, sessão + CSRF + login por formulário; a da API passou a `@Order(2)`)** e só deixa entrar
  o papel `Admin` (uma conta sem esse papel leva o mesmo "credenciais inválidas" de uma password errada). Contrato, filtros de
  qualidade, cabeçalhos do htmx e segurança em `backend/API_ENDPOINTS.md`, "Painel de administração web". **Regras deste código:**
  (1) listas por **queries com projeção** (`SELECT new ...`, sem entidades), com `countQuery` explícita — o `HqlQueriesTest` valida
  as duas; um critério novo = um código em `QualidadeBebida`/`QualidadeProdutor` **e** o ramo `:qualidade = '<código>'` no HQL;
  (2) **no Oracle `''` é NULL**: para "texto em branco" usar `TRIM(x) IS NULL`, nunca `TRIM(x) = ''` (nunca é verdadeiro);
  (3) a página inteira vs. só o bloco `resultado` decide-se pelo `HX-Request` (e o regresso pelo histórico, `HX-History-Restore-Request`,
  quer a página inteira) — as respostas levam `Vary: HX-Request`; (4) a CSP não permite estilos nem scripts embutidos: o htmx corre com
  `includeIndicatorStyles`/`allowEval` desligados (meta `htmx-config` no `layout.html`) e o CSS do indicador está no `admin.css`; (5) os
  formulários de escrita (fatias 2+) usam `th:action` (leva o token CSRF sozinho) e, nos pedidos do htmx, o token vai em `hx-headers`.
  **Testes:** `AdminWebTest` (contexto completo sem BD, `MockMvc`, serviço substituído por um falso com **transação de faz-de-conta** —
  o Spring põe um proxy `@Transactional` também à volta do falso e sem isto tenta abrir ligação ao Oracle) apanha erros de template,
  de segurança e de cabeçalhos; as queries só se validam ao vivo. **Formulários (fatia 2):** o texto vem tal como foi escrito num `data class` de `String`s (`ProdutorFormulario`) e a validação sem BD é uma
  função pura (`ProdutorValidacao`, com testes); o que depende da BD (país/região existem e combinam, nome repetido) está no serviço
  (`AdminProdutorService`), que devolve `Guardado`/`Invalido(erros por campo)`. Um `POST` com sucesso redireciona (F5 não reenvia); com
  erros devolve `200` com o formulário. **Uma coluna `DEFAULT SYSTIMESTAMP` não se preenche sozinha por JPA** (o `INSERT` leva `NULL`):
  o serviço põe `dataCriacao = Instant.now(clock)` (guardado em UTC, como o resto). **Ao testar escritas ao vivo:** (a) nunca em linhas do
  seed — criar produtores descartáveis ("Adega Teste A") e apagá-los por id no fim (mais `ALTER TABLE ... RESTART START WITH LIMIT VALUE`);
  (b) **nada de acentos em argumentos `-d` do `curl.exe`** (chegam estragados e gravam-se estragados) — usar `--data-urlencode "campo@ficheiro"`
  com o ficheiro em UTF-8. Aconteceu em 2026-09-24: um teste "editar outro produtor com o nome do 41" escreveu por cima do produtor 8 do seed
  (o nome do 41 tinha ficado estragado por um `-d` com acento, por isso o teste não viu o repetido); foi reposto à mão e o
  `rebuild-check.sh` confirmou "igual à dev". **Falta:** limite de tentativas de login (antes de expor o
  `/admin` à internet).
- **Catálogo curado vs. ofertas de afiliados:** os atributos de uma bebida (corpo, castas, botânicos, ...) são
  sempre do catálogo, curados à mão; dos afiliados só vêm as ofertas (`bebida_link_compra`: preço, link,
  retalhista). Não estamos presos a uma rede (`retalhista_rede_afiliados` é só uma etiqueta; a Awin é a 1.ª e uma bebida
  pode ter links de vários retalhistas). **Atributo sem valor = `NULL` = "não disponível"** — todas as colunas de
  atributo são anuláveis de propósito; escalas 1–5 só com fonte.
- **As bebidas entram em lotes (~30), criadas à mão — não automaticamente a partir do feed da Awin** (combinado em
  2026-09-19; fluxo completo no `PLANO.md`, "Como entram as bebidas"). O utilizador envia, por produto, o link da página do
  retalhista (sem tracking) e o link de afiliado (ou linhas do feed); o Claude devolve **primeiro uma tabela de revisão**
  (bebida → valores de lookup, `NULL` onde não houver fonte, duplicados assinalados) e **só depois de aprovada** gera o SQL
  do lote (padrão de `database/seed/02_bebidas.sql`, com as duas URLs de cada link). Não inventar atributos: só o que a
  página/feed diz ou o utilizador indica.
- **EAN e imagem de cada bebida (2026-09-20):** `bebida_ean` é texto, **único**, anulável, 8 a 14 dígitos (tirar espaços e
  hífenes ao gerar o SQL; `ORA-02290`/`ORA-00001` se falhar). **Antes de criar uma bebida num lote, procurar o EAN**: se já
  existir, só se acrescenta um `bebida_link_compra` novo (assinalar como duplicado na tabela de revisão). Sem EAN, comparar
  nome + produtor + volume. `bebida_path_image` (1000 caracteres) guarda o **URL absoluto da imagem do retalhista/feed** (ou
  um caminho relativo de um recurso estático); o Android usa-o tal como está se começar por `http(s)://`. Sem `bebida_ean`
  na API (é só interno).
- **Nunca abrir nem pedir automaticamente um link de afiliado** (conta como clique; nem o Claude nem o job). Ler só a
  página do produto sem tracking. A verificação diária de links usa `bebida_link_compra_url_verificacao` (página do
  produto sem tracking); ver `API_ENDPOINTS.md`.
- **Ao alterar o schema:** atualizar `database/ddl/01_tables.sql` (fonte da verdade, para quem reconstrói do zero)
  **e** criar um patch `database/ddl/0N_patch_*.sql` para a BD de dev já existente (fora do `run-migrations`; só o
  README da pasta `database/` explica). Seguir os padrões existentes: atributo categórico = tabela de lookup + FK
  (o utilizador já corrigiu um desvio a isto); booleano = `NUMBER(1)` + `CHECK`. **Depois, corre
  `database/verify/rebuild-check.sh`** (Git Bash): reconstrói a BD do zero num schema temporário e compara-a com a dev — apanha
  seeds que dependem de um nome de lookup alterado, `CREATE TABLE` com erros que os patches não exercitam, etc.
- **Colunas-flag com `DEFAULT` cujo campo Kotlin é não-nulo** (`produtor_permite_visitas`, `retalhista_is_ativo`,
  `avatar_is_active`, `cave_bebida_is_consumida`, `cave_bebida_quantidade`, `bebida_rating_medio`,
  `bebida_total_reviews`): ao inserir por SQL, omite a coluna ou usa 0/1 — **nunca `NULL` explícito** (a API
  rebenta a ler essa linha).
- **Encoding do `sqlplus` (mojibake):** com `docker exec ... sqlplus` passa sempre
  `-e NLS_LANG=AMERICAN_AMERICA.AL32UTF8` (os `run-migrations` já passam) e envia o SQL **a partir de um ficheiro**,
  não em strings inline do shell com acentos. Ao mexer em dados antigos, procurar 3 padrões de corrupção:
  `LIKE '%Ã%'`, `LIKE '%â€%'` e `LIKE '%Â%'`. **No Windows PowerShell 5.1** (o único instalado nesta máquina) o
  `Get-Content` sem `-Encoding UTF8` lê os `.sql` (UTF-8 sem BOM) como ANSI e os acentos chegam duplamente
  codificados mesmo com `NLS_LANG` certo — o `run-migrations.ps1` já passa `-Encoding UTF8`; para scripts avulsos,
  `docker cp` + `@ficheiro` dentro do contentor evita o PowerShell (no Git Bash: `MSYS_NO_PATHCONV=1` e `cygpath -w`
  na origem do `docker cp`).
- **`pais` e `produtor_pais` são duas tabelas com os MESMOS ids** (decisão do utilizador, 2026-09-19: manter as duas):
  o filtro do catálogo usa `pais.bebida_pais_id` e `/lookup/regioes` usa `produtor_pais_id`. Ao acrescentar um país,
  seguir a receita de `database/README.md` (mesmo id nas duas) e conferir com a query de verificação — "inserir na mesma
  ordem" não chega: os identity das duas divergem com inserts revertidos, cache ou reinícios (aconteceu na dev).

- **Regiões:** `regiao` (país + nome) é um lookup e `produtor.produtor_regiao_id` aponta para ele (nada de texto livre).
  A BD recusa uma região de outro país (FK composta com `produtor_pais_id`) e uma região sem país. Um produtor novo
  escolhe a região da lista; se não existir, acrescenta-se primeiro (receita em `database/README.md`). O filtro do
  catálogo (`regiaoIds`) e `/lookup/regioes` só mostram regiões com pelo menos uma bebida. **A região do whisky é da mesma
  tabela** (`whisky.whisky_regiao_id` → `regiao`; a tabela `whisky_regiao` já não existe, 2026-09-21): um whisky de um país sem
  regiões na lista fica sem região (`NULL`) mas mostra o país de origem. **Ao renomear um valor de lookup, procurar o nome
  antigo nos seeds** (`02_bebidas.sql` procura regiões/países por nome e devolve `NULL` em silêncio se não achar).
- **Rating geral do produtor (2026-09-24):** `ProdutorDetailDto.ratingMedio`/`totalReviews` = a média de todas as reviews das bebidas do
  produtor, **ponderada pelo nº de reviews** de cada bebida (`SUM(rating × reviews) / SUM(reviews)`, `ProdutorRating.media`; `null` sem
  reviews — a app mostra "Sem reviews ainda", nunca 0,0). O "produtor da semana" ordena pela média **simples** (aprovado em
  2026-09-19): são duas medidas diferentes de propósito, não uniformizar.
- **Termos e condições (2026-09-21, texto passou a JSON em 2026-09-22):** `utilizador_termos_aceites_em` (`NULL` = nunca aceitou; só a
  data, sem versão). O registo exige `aceitouTermos: true`; `GET /api/users/me` traz `termosAceitesEm` e `precisaAceitarTermos`; `POST
  /api/users/me/termos/aceitar` regista a aceitação; `aquavitae.termos.em-vigor-desde` força nova aceitação. O texto vive em
  `backend/src/main/resources/legal/termos.txt` (`# ` = título de secção; parágrafos por linha em branco) e serve-se em dois formatos
  a partir daí — `GET /api/legal/termos` (JSON, o popup `TermsSheet` da app) e `GET /legal/termos.html` (a página pública). **Hoje é um
  texto de exemplo (Lorem ipsum)**: o texto final é do utilizador — substitui-se só o `.txt`, sem mexer no backend nem na app.
- **Pesquisa de texto sem acentos (2026-09-24):** `GET /api/bebidas?search=` ignora **acentos e maiúsculas** (nome da bebida, do produtor e
  das castas) e trata `%`, `_` e `!` como texto. Faz-se com `CAST(FUNCTION('translate', UPPER(coluna), COM_ACENTO, SEM_ACENTO) AS String)
  LIKE :searchPattern ESCAPE '!'` — **o `CAST … AS String` é obrigatório** (o Hibernate 6 não conhece o tipo de um `FUNCTION()` e recusa o
  `LIKE` com "Operand of 'like' is of type Object"; o `HqlQueriesTest` apanha-o sem BD) — e o **mesmo mapa** normaliza o termo no Kotlin
  (`bebida/PesquisaTexto.kt`, a única fonte: as duas constantes entram na query por interpolação). **Não usar `NLS_COMP=LINGUISTIC`/
  `NLS_SORT=BINARY_AI`** (mexeria em todas as comparações de texto da BD). Um alfabeto novo (ł, ő, ž) acrescenta-se às duas constantes,
  com o mesmo comprimento (o `PesquisaTextoTest` confere-as). Na app, as pesquisas **locais** de castas usam `String.contemSemAcentos()`
  (`data/model/TextoSemAcentos.kt`), nunca `contains(ignoreCase = true)`.
- **Login e unicidade (2026-09-21):** `POST /api/auth/login` recebe `{ identificador, password }` — `identificador` é o username OU o
  email, **sem distinguir maiúsculas** e com `trim` (o teclado do telemóvel capitaliza e deixa espaços). A unicidade de username e email
  no registo também ignora maiúsculas (senão "Ana" e "ana" seriam duas contas e o login ficava ambíguo). **A recuperação de password
  usa o mesmo `identificador`** nos 3 passos (desde 2026-09-22; o helper partilhado é `UtilizadorRepository.findByIdentificador`).
- **Email de recuperação de password (2026-09-22):** o código vai por email real (`EmailService`, Spring Mail); sem `SPRING_MAIL_HOST`
  configurado, **em dev o código continua a aparecer no log da API** (`Código de recuperação de password para ...`), fora de dev só um
  aviso — nunca o código em log fora de dev. Um pedido novo invalida os códigos anteriores por usar; só se gera código (e email) novo
  passado `aquavitae.recuperacao.intervalo-minimo-segundos` (60 s) desde o último pedido da mesma conta.
  - **Testar sem enviar nada real (Mailpit local):** `docker compose -f backend/docker-compose.mail-dev.yml up -d` (SMTP em
    `localhost:1025`, caixa em `http://localhost:8025`) e `$env:SPRING_PROFILES_ACTIVE = "dev,mailpit"` antes do `bootRun`
    (`application-mailpit.yml` aponta para lá). `docker compose -f backend/docker-compose.mail-dev.yml down` para parar.
  - **Email real (Gmail, `aquavitaerecovery@gmail.com`, criado pelo utilizador em 2026-09-22):** o Gmail exige uma **"app
    password"** por SMTP (não a password da conta) — gera-se em `myaccount.google.com/apppasswords`, depois de ativar a
    "Verificação em duas etapas". Guarda-se num ficheiro local `backend/.env.mail` (fora do git, `.gitignore`; modelo em
    `backend/.env.mail.example`, que o utilizador preenche à mão — nunca colar a app password no chat). Carregar antes do
    `bootRun` (Git Bash, sem imprimir os valores):
    ```bash
    set -a; source backend/.env.mail; set +a
    ```
    ou em PowerShell:
    ```powershell
    Get-Content backend\.env.mail | ForEach-Object { if ($_ -match '^([A-Z_]+)=(.*)$') { Set-Item "env:$($matches[1])" $matches[2] } }
    ```
    Depois `$env:SPRING_PROFILES_ACTIVE = "dev"` (sem `mailpit`) e `bootRun` normal — os `SPRING_MAIL_*` do ficheiro sobrepõem-se aos
    valores por omissão do `application.yml`.
- **Nacionalidades e castas (2026-09-22):** `utilizador_nationality.nationality_codigo_pais` (ISO alfa-2, ex.: `PT`; patch `13`) — a app
  desenha a bandeira (emoji) a partir dele; uma nacionalidade nova é uma linha de SQL **com o código**. `/lookup/castas` traz 6 castas em
  destaque primeiro (`CASTAS_EM_DESTAQUE` em `OrdemAlfabetica.kt`, as do desenho do onboarding) e o resto por ordem alfabética.
  `PUT /api/users/me` valida os tamanhos (nome ≤ 25, apelido ≤ 40, username 3–30, descrição ≤ 1000).

**Ferramentas nesta máquina (Windows)**

- **Gradle:** se depois de editar ficheiros o `compileKotlin`/`bootRun` disser `UP-TO-DATE` ou falhar com um bean
  em falta, forçar com `--rerun-tasks`; se falhar com "Could not delete ...caches-jvm", é o daemon Kotlin do VS Code
  a segurar ficheiros — voltar a correr sem `--rerun-tasks`. Testes: `.\gradlew.bat test` (os que usam HTTP levantam
  um `HttpServer` local do JDK, sem rede real). **"Failed to compile with Kotlin daemon" seguido de "Unresolved reference" em testes
  que apontam para classes de `main`** é um soluço do daemon (o `main` não chegou a compilar): `.\gradlew.bat --stop` e repetir.
- **Servidor a correr em background** (`bootRun`): para o parar, `Get-NetTCPConnection -LocalPort 8080` → `Stop-Process`.
- **Docker Desktop depois de reiniciar o Windows:** o contentor Oracle fica parado (`docker compose up -d` em
  `database/`) e o próprio Docker Desktop pode crashar ao arrancar com `sailor-ingest.sock ... rename ... The file cannot
  be accessed by the system` — são sockets antigos que nem `del`/`fsutil` conseguem apagar; **resolveu-se reiniciando o
  Windows outra vez**. **Nunca** escolher "Reset to factory defaults" no diálogo de erro: apaga contentores e volumes,
  incluindo a BD Oracle.
- **Validar a API ao vivo:** `bootRun` em background + `curl.exe` (sem `jq` nesta máquina; no PowerShell 5.1 ler as
  respostas com `[Text.Encoding]::UTF8` e comparar números com `InvariantCulture`, senão saem com vírgula decimal); os
  ficheiros `.ps1` sem BOM são lidos como ANSI (acentos nos literais chegam duplamente codificados à API).
- **Android — compilar por linha de comandos (verificado em 2026-09-21):** o `android/` não tem `gradlew` (ver
  `android/README.md`). Compila-se com o Gradle 8.7 da cache (`~/.gradle/wrapper/dists/gradle-8.7-bin/*/gradle-8.7/bin/gradle.bat`)
  e o **JDK 21** (`~/.gradle/jdks/eclipse_adoptium-21-amd64-windows.2`) em `JAVA_HOME` — o JDK por omissão desta máquina é o 25,
  que o Gradle 8.7/AGP 8.5.2 não suportam (e o JDK do Android Studio também é o 25: no Studio, *Gradle JDK = 21*). Precisa de um
  `android/local.properties` (`sdk.dir=C\:\\Users\\migue\\AppData\\Local\\Android\\Sdk`; já está no `.gitignore`). Comando:
  `gradle.bat -p android assembleDebug --console=plain` → `app/build/outputs/apk/debug/app-debug.apk` (~2 min a 1.ª vez). O SDK
  precisa da Platform 35 e das Build-Tools 34 (instaladas por esse build; a máquina só tinha a 37). O esqueleto compilou tal como
  estava, só com avisos (kapt do Moshi obsoleto, `Divider` renomeado, AGP 8.5.2 testado até compileSdk 34).
- **Emulador Android e o ciclo compilar → ver (verificado em 2026-09-21):** há um AVD **`Pixel_8`** (Pixel 8, Android 17 / API
  37.2, imagem `google_apis_playstore_ps16k` x86_64, 2 GB de RAM), criado pelo utilizador no Device Manager; a aceleração (WHPX)
  funciona e no `adb` aparece como `emulator-5554`. Não há `cmdline-tools` (não se criam AVDs por linha de comandos). O emulador
  alcança o backend em `http://10.0.2.2:8080/` (o `API_BASE_URL` de debug). **Sem o Studio:** `adb` em
  `$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe`; `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`;
  `adb shell am start -W -n pt.aquavitae.android/.MainActivity`; `adb exec-out screencap -p > ecra.png` (1080×2400) e ler a
  imagem. **Esperar ~3 s** depois de arrancar: o 1.º screenshot apanha o ecrã de arranque do sistema (o ícone), não a app. Um
  "crash" de um processo `android.hardwar…` em `adb logcat -b crash` é do sistema do emulador, não da app. O AVD também se lista e
  arranca pela linha de comandos (`emulator.exe -list-avds`; `emulator.exe -avd Pixel_8`, este último não testado). **Não compilar pela
  linha de comandos ao mesmo tempo que o Android Studio** (dois builds na mesma pasta podem bloquear-se). Se a API estiver parada, a
  app mostra "Sem ligação ao servidor" com "Tentar de novo" (é o comportamento esperado do ecrã de loading).
- **Android — a UI (2026-09-21, atualizado em 2026-09-23):** tema **só claro** (o design é claro); tipografia **Inter** (`res/font/`,
  copiada do `testFramework.jar` do Android Studio; licença OFL, falta o aviso de licença antes de publicar) + **uma serifa do sistema**
  para os títulos da homepage/catálogo (`AquaText.SectionSerif`/`GreetingSerif`/`BebidaNomeSerif`, aproximação — falta saber qual é a
  exata do Figma); tokens medidos nos prints em `ui/theme/Color.kt`; componentes reutilizáveis em `ui/components/` (`AquaVitaeLogo`,
  `UnderlineField`, `PrimaryButton`, `AuthCard`/`AuthScaffold`/`overlapTop`, `LoadingDots`, `PillCard`/`NavRow`/`NextButton`,
  `OptionRow`/`PillChip`/`LookupContent`, `LevelSlider`, `RangePillRow`, `CodeInput`, `FlagChip`, `BottomNavBar`, `AvatarBadge`,
  `BebidaCard`, `ContagemEOrdenacao`, popups `TermsDialog`/`PasswordChangedDialog` com `DialogScrim`; lista completa em `android/README.md`) — reutilizar, não
  recriar. Cada ecrã novo aplica `systemBarsPadding()` (edge-to-edge); os ecrãs antigos do esqueleto vão embrulhados em `LegacyScreen`
  (`AppNavHost.kt`) até serem redesenhados (já saíram de lá `home` e `catalog`). **Imagens da API com Coil 2.7** (avatares SVG; o
  carregador com o `SvgDecoder` está em `AquaVitaeApplication`; `resolveImageUrl` decide URL absoluto vs. relativo). **Ícones PNG
  entregues pelo utilizador** (traço branco/cinza-claro sobre transparente, ex. `ic_nav_*.png`): tingem-se por código
  (`Icon(painter=..., tint=cor)`), sem reexportar variantes de cor — o Compose substitui a cor onde o alfa não é zero. **`Icon` de um
  `ImageVector` do Material Icons Extended** (`Icons.Filled.FilterList`, já dependência do projeto) é mais simples do que pedir um PNG
  novo ao utilizador quando o ícone é genérico o suficiente (funil de filtro, por exemplo) — só vale a pena pedir um ícone próprio
  quando a forma importa para a identidade visual. **O detalhe de uma bebida é sempre um popup** (`feature/bebidadetalhe/
  BebidaDetalheSheet`, 2026-09-23), nunca uma rota — abre-se em toque curto ou premido (`BebidaCard.combinedClickable`) a partir de
  qualquer ecrã; sem `SavedStateHandle`, usa `hiltViewModel(key = "bebida-detalhe-$id")` (ver `android/README.md`, "Convenções"). **O cartão do produtor no fim do popup abre a página do produtor** (fatia 6) por um parâmetro `onVerProdutor` do `BebidaDetalheSheet` — cada ecrã que o usa tem de o receber do `AppNavHost` e passá-lo; `null` (o valor por omissão) tira o atalho, e é o que as páginas do próprio produtor fazem.
  **`DatePicker`/`DatePickerDialog` do Material3** (`AdicionarACaveSheet`, fatia 3b) são `@ExperimentalMaterial3Api` — precisam de
  `@OptIn`, já usados pela 1.ª vez neste projeto (a "data de aquisição" do popup "Adicionar à cave").
  Testes unitários: `gradle.bat -p android testDebugUnitTest --console=plain` (97: registo, recuperar password, onboarding,
  bandeiras, URLs, iniciais do avatar, formatação de bebida, formatação e estado do produtor, ofertas de compra, renovação de sessão
  contra um `MockWebServer`, texto sem acentos).
- **Um `Row`/`Column` de altura fixa com texto de comprimento variável perde conteúdo sem erro nenhum (apanhado no `BebidaCard`,
  2026-09-23):** o cartão de bebida tem `height(108.dp)` fixo; ao acrescentar `tipo`/`tanino` à linha de atributos, algumas bebidas
  passaram a ter uma linha com 3 segmentos que quebrava para 2 linhas — sem `maxLines`, isso empurrava o preço (a `Row` seguinte) para
  fora da altura fixa do cartão, a sobrepor-se ao cartão seguinte. Sem exceção nem aviso, só visível na screenshot. Corrige-se com
  `maxLines = 1` + `TextOverflow.Ellipsis` em qualquer `Text` dentro de um contentor de altura fixa cujo comprimento dependa dos dados
  (nome, retalhista e a linha de atributos do `BebidaCard` já seguem este padrão).
- **Um membro de `sealed interface`/`sealed class` sem o supertipo depois do construtor não avisa a compilar até bem mais tarde**
  (apanhado no `BebidaDetalheUiState`, 2026-09-23): `data class Ready(...) : MinhaSealedInterface` — esquecer o
  `: MinhaSealedInterface` no fim (fácil de fazer ao copiar/colar de um `data object`/`data class` mais simples, sem parâmetros a
  fechar antes) não dá erro na própria declaração; `Ready` fica só uma classe normal, sem relação nenhuma com a sealed interface. O
  Kotlin só se queixa mais tarde, em sítios que usam `Ready` como se fosse a sealed interface (`_state.value = Ready(...)`, um `when`
  sobre o `state`), com mensagens confusas do tipo "Incompatible types: X and X" ou "Type mismatch: inferred type is X but X was
  expected" (o mesmo nome dos dois lados) — nada aponta para a declaração em falta. Ao ver este erro específico, confirmar primeiro
  se todos os membros da sealed interface/class têm o `: NomeDaSealed` no fim.
- **`Modifier.weight()` (ou outro extra de `ColumnScope`/`RowScope`) só funciona dentro do `Column`/`Row` que o declara** (apanhado
  no `BebidaDetalheSheet`, 2026-09-23): uma função `@Composable` chamada de dentro de um `Column { ... }` não herda o `ColumnScope`
  automaticamente — só o recebe se for declarada como `fun ColumnScope.MinhaFuncao(...)`. Sem isso, `Modifier.weight(1f)` dentro
  dela dá "Unresolved reference: weight", mesmo com o `Column` correto no sítio de onde é chamada.
- **Duas chamadas assíncronas seguidas ao mesmo `MutableStateFlow`, uma delas a repor um valor "por omissão", corrida
  clássica** (apanhado no `CaveViewModel.criarCave`, 2026-09-23): criar uma cave fazia `carregar()` (assíncrono, recarrega a
  lista e repõe `caveSelecionadaId` para a 1.ª cave) e logo a seguir `selecionarCave(nova.id)` — como `carregar()` só
  terminava depois (é uma corrotina lançada, não esperada), a sua resposta chegava **depois** de `selecionarCave` e repunha a
  seleção errada. A cave ficava criada mas não selecionada, sem exceção nem log — só visível comparando com o esperado.
  Corrige-se fazendo as duas coisas na mesma corrotina, pela ordem certa (`getCaves()` → só depois `copy(caveSelecionadaId =
  nova.id)`), em vez de duas chamadas que mexem no mesmo estado sem uma esperar pela outra.
- **Um `Dialog` (Compose) a ecrã inteiro precisa de dois truques, senão perde-se conteúdo sem erro nenhum (apanhado a construir o
  `TermsSheet`, 2026-09-22):** (1) a janela dimensiona-se por omissão ao conteúdo (mesmo com `usePlatformDefaultWidth = false`, que só
  afeta a largura) — força-se com `window.setLayout(MATCH_PARENT, MATCH_PARENT)` numa `SideEffect` (`DialogScrim.kt` →
  `DialogFillScreen()`). (2) com `decorFitsSystemWindows = false` (para o escurecimento cobrir a barra de estado), o conteúdo desenha
  por baixo da barra de navegação do sistema, e **`Modifier.navigationBarsPadding()` chamado DENTRO do `Dialog` não recebe o inset
  certo** (ficou a 0, sem erro): o botão do fundo do popup ficava tapado pela barra, só uma nesga de poucos pixels visível — sem
  exceção nem aviso, só se via comparando o resultado com o esperado. Corrige-se lendo o inset **antes** de entrar no `Dialog`
  (`WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()`, na janela principal, onde os insets estão corretos) e
  aplicando-o como padding fixo dentro do popup.
- **Testar a UI pelo `adb` (sem tocar no emulador):** `adb shell input tap X Y` (X,Y nativos, 1080×2400; uma imagem mostrada a
  900×2000 tem de se multiplicar por 1,2), `input text` (`@` funciona; **espaços escrevem-se `%s`**), `input keyevent 66` (Enter = a
  ação do teclado: "Seguinte" ou "Feito"), `67` (apagar), `123` (fim de linha), `4` (voltar; com o teclado aberto o 1.º só o fecha),
  `input swipe X1 Y1 X2 Y2 ms` (deslizar listas); `adb shell pm clear pt.aquavitae.android` apaga a sessão (volta ao login).
  **Screenshots: `adb exec-out screencap -p > f.png` só no Git Bash** (o `>` do PowerShell 5.1 estraga o binário). Um `UnderlineField`
  em `input text` + Enter avança de campo em campo, por isso um formulário inteiro preenche-se numa só chamada. Emblema/logótipo:
  desenhado em vetor (`ic_logo_emblem.xml`); a homepage já usa o wordmark real (`ic_wordmark_home.png`, fundo tornado transparente —
  ver "Android — a UI"), os outros ecrãs de auth ainda têm o nome em texto Inter.
- **Coordenadas de toque: nunca reutilizar as de uma sessão anterior — o layout muda.** Apanhado a testar o login da homepage
  (2026-09-23): tocar em coordenadas "conhecidas" de outra sessão/ecrã falhou vezes sem conta (texto a ir para o campo errado, botões
  que não reagem), sem nenhum erro — só dava para perceber pela screenshot a seguir. **Antes de cada sequência de toques**, correr
  `adb exec-out uiautomator dump /dev/tty 2>&1 | tr '>' '>\n' | grep -o 'text="X"[^>]*bounds="\[...\]"'` e tocar no **centro exato** dos
  `bounds` devolvidos — mais lento por toque, mas sem re-tentativas às cegas. Um campo que não reage a `input text` normalmente está
  focado no elemento errado (o toque anterior falhou), não é um bug da app. **`uiautomator dump /dev/tty` às vezes só imprime
  "UI hierchary dumped to: /dev/tty" sem o XML** (apanhado 2026-09-23): correr antes `uiautomator dump /sdcard/dump.xml` e
  `adb pull` (com `MSYS_NO_PATHCONV=1` no Git Bash, senão o `/sdcard/...` é convertido para um caminho Windows; **o destino tem de
  ser um caminho Windows** — `"$(cygpath -w ficheiro)"` —, porque o `adb.exe` é um binário Windows: com um caminho `/c/...` não dá erro, só
  não escreve o ficheiro e o `grep` seguinte falha em silêncio; e `adb` pode não estar no `PATH` do Git Bash, usar o caminho inteiro). Um botão/label do
  Compose às vezes só aparece na árvore por `content-desc`, não por `text=` (procurar os dois).
- **Um ecrã novo que mostra bebidas não herda o toque/premido do `BebidaDetalheSheet` de outro ecrã — tem de se ligar
  explicitamente linha a linha.** Apanhado no `CaveScreen` (2026-09-23): o mockup original já dizia "nas caves" como um dos
  sítios onde o popup de detalhe abre, mas a ligação nunca tinha sido feita — as linhas de garrafa (`GarrafaRow`) e o resumo de
  "já provadas" (`ProvadaResumoRow`) não tinham `combinedClickable` nenhum, sem erro nem aviso, só reparado ao tentar testar outra
  funcionalidade a partir daí. Padrão a repetir: `combinedClickable(onClick, onLongClick)` (`ExperimentalFoundationApi`, o mesmo do
  `BebidaCard`) em qualquer linha nova que representa uma bebida, com o `onClick` a subir até ao ecrã que guarda o
  `bebidaSelecionadaId` e monta o `BebidaDetalheSheet`.
- **Um popup que muda dados partilhados (`AdicionarACaveSheet`, aberto de vários ecrãs) não avisa o ecrã por trás para se
  atualizar — cada ecrã tem de pedir os dados de novo sozinho no seu próprio `onGuardado`.** Apanhado pelo utilizador a testar
  por conta própria (2026-09-23): guardar uma garrafa nova numa cave, de dentro da própria `CaveScreen` ou da `HomeScreen`
  (popup empilhado sobre `BebidaDetalheSheet`), não atualizava as pílulas "GARRAFAS"/"INVESTIDOS" — só a lista de garrafas
  (sempre pedida de novo ao trocar de cave) é que ficava certa. Causa: `CaveViewModel`/`HomeViewModel` só voltavam a pedir
  `GET /users/me/caves` em `carregar()`/`criarCave()`/`consumir()`; o `AdicionarACaveSheet` é uma `ViewModel` completamente à
  parte (`AdicionarACaveViewModel`), sem nenhuma ligação de volta ao ecrã que o abriu. Um `Composable` com Navigation-Compose
  em modo de abas (`saveState`/`restoreState`, ver "Convenções" do `android/README.md`) **mantém a mesma instância da
  ViewModel** ao trocar de aba — por isso nem sair e voltar ao ecrã garante dados frescos. Padrão a repetir: sempre que um
  popup que **muda** dados partilhados por vários ecrãs (aqui, os totais de uma cave) é aberto de dentro de outro ecrã, esse
  ecrã dá ao popup um `onGuardado` que pede os dados relevantes de novo (`CaveViewModel.atualizarAposGuardar()`/
  `HomeViewModel.atualizarAposGuardar()`: só `getCaves()` + o detalhe atual, sem repor a seleção nem mostrar o ecrã de
  carregamento inteiro) — nunca assumir que o popup "avisa" sozinho quem o abriu. **Quando não há nenhum popup a que
  ligar um `onGuardado`** (apanhado logo a seguir, 2026-09-24, em `FavoritosScreen`/`WishlistScreen` — marca-se um
  favorito a partir de qualquer ecrã, não só de um popup conhecido): a `ViewModel` só pedia os dados no `init {}`, por
  isso a aba ficava presa ao resultado da 1.ª vez que tinha sido aberta (às vezes "vazio"), sem erro nenhum. Aqui o
  padrão é outro: `LaunchedEffect(Unit) { viewModel.carregar() }` no topo do `@Composable` do ecrã — o Navigation
  Compose desmonta e volta a montar o conteúdo de uma rota a cada troca de aba (mesmo mantendo a mesma instância da
  ViewModel por trás, ver acima), por isso o `LaunchedEffect(Unit)` volta a correr, e portanto a pedir dados frescos,
  de cada vez que a aba é reaberta.
- **Testar a página do produtor (2026-09-24): nenhum produtor real tem `produtor_historia`, `produtor_morada`, `produtor_latitude`/`longitude` nem
  `produtor_path_imagem`**, por isso o ecrã só se exercita por completo com dados de teste temporários: `UPDATE produtor SET ...` no
  produtor 1 e mover bebidas de outros produtores para ele (`UPDATE bebida SET bebida_producer_id = 1 WHERE bebida_id IN (...)` — a
  coluna do produtor na `bebida` chama-se `bebida_producer_id`, não `produtor_id`; `ORA-00904` se se enganar). **Antes de mexer, guardar o
  mapeamento original (`SELECT bebida_id, bebida_producer_id FROM bebida`) e escrever já o SQL de reversão**; conferir no fim. Uma imagem
  de teste serve qualquer `icones/avatares/*.svg` (caminho relativo, servido pela API). **"Abrir no mapa"** abre o Google Maps do
  emulador, que mostra primeiro o seu arranque (login → "Skip"; permissão de localização → "Don't allow") antes de chegar ao ponto —
  **só na 1.ª vez**. Com coordenadas abre no ponto (`geo:lat,lon?q=...(Nome)`); só com morada pesquisa por ela (`geo:0,0?q=morada`), e
  ambos se conferem ao vivo. **O catálogo do produtor é o `CatalogScreen`/`CatalogViewModel` do catálogo geral** (`produtorId` lido do
  argumento de navegação por `SavedStateHandle`, `GET /api/bebidas?produtorId=`): ao mexer nos filtros do catálogo, testar também o
  modo produtor (sem "ORIGEM", categoria "Todas" = `null`) e vice-versa. **Um botão flutuante "≡" do emulador (menu de acessibilidade)
  pode aparecer por cima do canto superior esquerdo e engolir os toques na seta de voltar** — não é da app: tocar na parte livre ou
  usar `adb shell input keyevent 4`.
- **Testar "Comprar" (2026-09-24): nunca tocar num link de afiliado real.** Um toque em "COMPRAR"/na pílula do preço/"Comprar em X"
  abre o URL de `bebida_link_compra` no browser **e conta como clique** (comissão). A BD de dev só tem 2 links, ambos `PLACEHOLDER`; para
  ver o fluxo (Chrome a abrir, `clique_compra` a gravar, "Compraste?") trocar temporariamente o `bebida_link_compra_url` por
  `https://example.com/...` (e acrescentar 1-2 ofertas de teste), **guardar o URL original e escrever já o SQL de reversão**, e
  reverter no fim (incluindo `DELETE FROM clique_compra` e as garrafas que o "Sim, adicionar à cave" tenha criado). Para "envelhecer"
  um clique (o inquérito só pergunta passados 2 minutos): `UPDATE clique_compra SET clique_compra_data = clique_compra_data - INTERVAL '5'
  MINUTE WHERE clique_compra_is_perguntado = 0`, depois `input keyevent 3` (home) e `am start` para forçar o `ON_RESUME`. **O "Compraste?"
  vive na `MainActivity` (`CompraPromptHost`), não num ecrã**: aparece por cima de qualquer ecrã ao voltar ao primeiro plano.
- **Sessão renovável (2026-09-24) — como se testa e o que não se pode partir.** O JWT dura 60 min (`jwt.expiration-minutes`) e há um refresh
  token de 30 dias (tabela `utilizador_refresh_token`, só o hash, rotação a cada uso; ver `backend/API_ENDPOINTS.md`, "Auth"). **Para testar
  sem esperar uma hora:** arrancar a API com `$env:JWT_EXPIRATION_MINUTES = "1"` antes do `bootRun` (o Spring lê a variável), entrar na app,
  esperar ~70 s e usar um ecrã autenticado (a aba "Favoritos"): a app deve renovar sozinha (na BD: o token antigo `ROTACAO` e um novo).
  Para o caso da sessão morta: `UPDATE utilizador_refresh_token SET refresh_token_revogado_em = SYSTIMESTAMP, refresh_token_revogado_motivo =
  'PASSWORD' WHERE utilizador_id = ... AND refresh_token_revogado_em IS NULL` e esperar outros 70 s — a app deve voltar ao login com "A tua
  sessão expirou". **Reiniciar a API sem essa variável no fim.** Na app: **o `AuthRefreshApi` tem um cliente OkHttp à parte de propósito**
  (sem `AuthInterceptor` nem `TokenAuthenticator`; com o mesmo cliente a renovação disparava-se a si própria) e o `TokenAuthenticator` nunca
  renova `/api/auth/**` nem pedidos anónimos, e só uma vez por pedido. Um endpoint **público** chamado com o token expirado **não** dá 401
  (o backend trata-o como anónimo): não se renova, e vem sem favorito/wishlist/provada até haver um pedido autenticado.
- **Repor dados de teste = repor o valor EXATO do seed** (2026-09-24): ao guardar o "original" de uma coluna de texto, ler o valor **inteiro** —
  uma listagem `substr(coluna,1,70)` cortou um URL e o `rebuild-check.sh` apanhou "mesmo nº de linhas mas texto diferente" (tabela
  `bebida_link_compra`). Antes de mexer, `SELECT` da coluna toda (ou copiar do `database/seed/02_bebidas.sql`) para o ficheiro de reversão, e
  correr o `rebuild-check.sh` no fim.
- **Um script que falha a meio não deve encadear com `&&` a criação de outro ficheiro** (apanhado 2026-09-24): `perl script.pl && cat >
  Ficheiro.kt <<'EOF' ...` — o perl falhou e o `Ficheiro.kt` (um controlador) **nunca foi criado**, sem aviso; os testes e o `bootRun` passaram
  na mesma (só faltava a rota) e só se viu por um 404 ao vivo. Criar ficheiros novos com a ferramenta de escrita, à parte, e conferir com
  `ls`. O `perl -0pi` com padrões multi-linha e caracteres especiais falha em silêncio (nada muda) — confirmar sempre com um `grep`.
- **Android Studio (AI-261):** abre `android/` e o sync corre; usa como Gradle JDK um JBR 21 que ele próprio descarregou
  (`~/.jdks/jbr-21.0.11`, guardado em `android/.gradle/config.properties`, ignorado pelo git). Acrescentou uma linha
  (`org.gradle.tooling.parallel=true`) ao `android/gradle.properties` — não vale a pena commitá-la. Recusar o *AGP Upgrade
  Assistant* por agora (a toolchain só se atualiza depois da 1.ª versão).
- **`gh` (GitHub CLI):** instalado e autenticado, mas no Git Bash é preciso `export PATH="/c/Program Files/GitHub CLI:$PATH"`.
- **Git:** o trabalho corrente está numa branch de feature (ver `PLANO.md`); `docs/` (landing page) só existe na
  `main` — ao mudar de branch, ficheiros aparecem/desaparecem no disco, é esperado. **Nunca commitar** as notas do
  utilizador: `android/AQUAVITAESEEDS-NOTES` e, na raiz, `—--------------- DADOS A INSERIR NA.txt` (aparecem sempre como
  não rastreados; usar `git add` com caminhos explícitos, não `git add -A`).
- **Testes:** os nomes de teste em backticks não podem conter `:` nem `.` (a JVM recusa). Não há testes de
  services/controllers com BD: valida-se ao vivo (ver abaixo). `HqlQueriesTest` e `ContextoArrancaTest` apanham sem BD
  erros de HQL, de mapeamento e de injeção — correr sempre `--rerun-tasks` depois de criar ficheiros novos. **"Cannot access output
  property 'destinationDirectory' … NoSuchFileException" no `compileTestKotlin`** = duas sessões do daemon Kotlin a competir:
  `.\gradlew.bat --stop` e repetir (a compilação do `main` até já passou pelo "fallback sem daemon").
- **Kotlin: os comentários `/* */` aninham** (apanhado 2026-09-24): escrever `/admin/**` (ou qualquer `/**`) dentro de um bloco
  KDoc abre um comentário novo e o compilador queixa-se de "Unclosed comment" numa linha muito depois — nos KDoc escrever `/admin/...`;
  os `//` não têm o problema.
- **Versões:** ao escolher/atualizar versões de Gradle/Kotlin/Spring Boot, consulta a versão atual real em vez de
  usar um número de memória — `services.gradle.org/versions/current` para o Gradle,
  `repo1.maven.org/maven2/.../maven-metadata.xml` para bibliotecas/plugins Maven. Já aconteceu tentar usar
  versões desatualizadas que não corriam no JDK instalado nesta máquina. (`BodyHandlers.limiting` não existe no JDK 21.)

## Como correr tudo localmente

```powershell
cd database; cp .env.example .env; docker compose up -d; .\run-migrations.ps1   # 1. BD
cd ..\backend; .\gradlew.bat bootRun                                            # 2. API em localhost:8080
# 3. Android: abrir android/ no Android Studio (Gradle JDK = 21) OU compilar/instalar pela linha de comandos ("Compilar,
#    instalar e ver" em android/README.md); emulador Pixel_8; API_BASE_URL já aponta para 10.0.2.2:8080 no emulador
```

Conta de teste já existente na BD de dev: `demo2@aquavitae.local` / `password123` (papel `Utilizador`; username `demo_user2`). **Ao testar
o onboarding cria-se uma conta descartável no emulador (só o registo passa pelo onboarding) e apaga-se no fim** (perfil, preferências e
a conta, por SQL). Para
testar endpoints de admin **ou o painel `/admin`**, promover temporariamente por SQL (`utilizador_role_id` → o de `Admin`) e reverter
depois (ver `API_ENDPOINTS.md`, "Painel de administração web"); cada login da API deixa uma linha em `utilizador_refresh_token`
(`DELETE ... WHERE utilizador_id = 22` no fim). **Testar o painel ao vivo sem browser:** `curl -c/-b jar.txt` — `GET /admin/login`,
extrair o `_csrf` do HTML e `POST /admin/login` (`identificador`, `password`, `_csrf`) → `302 /admin`; depois `GET` das páginas com o
cookie (`-H "HX-Request: true"` para o bloco). **`curl.exe` estraga acentos em argumentos** (a linha de comandos do Windows é ANSI: "Vale
Meão" dava 0 resultados e "Vale%20Me%C3%A3o" 1): nas pesquisas com acentos, escrevê-los já em percentagem (UTF-8). Para **ver** as
páginas autenticadas no painel do browser sem pôr a password num campo: gravar o HTML com o `curl` e servi-lo de
`backend/build/resources/main/static/admin/css/` (com o `<link>` do CSS, não estilos embutidos — a CSP bloqueia-os; apagar depois).
**Paginação com dados a sério:** inserir umas 30 bebidas `ZZ Teste NN`, olhar para as páginas, apagá-las e reiniciar o identity
(`ALTER TABLE bebida MODIFY bebida_id GENERATED BY DEFAULT AS IDENTITY (RESTART START WITH LIMIT VALUE)`).

## Preferências de trabalho do utilizador

- **Desenvolvimento sequencial:** uma camada de cada vez (BD → API → Android), validada a correr de
  verdade antes de avançar para a seguinte — não scaffolding paralelo de múltiplas camadas.
- Comunica em **português de Portugal**; prefere respostas e nomes de ficheiros/variáveis em português.
- **Decisões de produto/schema:** apresentar a opção recomendada com o principal compromisso e deixar o utilizador
  aprovar ou ajustar *antes* de implementar; ele costuma responder com melhorias (p.ex. lookup em vez de coluna solta).
- **Commit, push e PR só quando o utilizador pedir** (costuma pedir no fim de cada fatia).
- **Frontend por fatias, guiado por prints (2026-09-21):** o utilizador envia os prints de **um fluxo inteiro de uma vez**, com notas por
  ecrã (o que ignorar, o que é opcional, listas paginadas, sliders...). Copiar os prints e as notas para `android/design/` **antes** de
  construir (a conversa não sobrevive a uma sessão nova). Construir ligado à API real, validar no emulador com screenshots e só depois
  avançar. Autorizou **melhorar o visual quando fique mais bonito e limpo** ("mais algum ajuste que queira fazer pode fazê-lo"): fazê-lo,
  mas registar cada diferença em `android/design/README.md` e dizê-lo. Se um ecrã pede algo que a API não tem, corrige-se a API primeiro.
- **O utilizador muda de sessão de propósito** ("continuamos numa nova sessão?"): no fim de cada sessão deixar o `PLANO.md` ("Retomar
  aqui"), o `android/README.md` e o `CLAUDE.md` prontos para retomar sem contexto da conversa (estado, por commitar, ambiente, decisões abertas).
- Conceitos de escalabilidade/otimização ficam deliberadamente para depois de tudo funcionar ponta a ponta.
