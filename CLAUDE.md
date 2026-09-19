# AquaVitae — contexto para o Claude Code

App Android (Kotlin) de catalogação e comunidade de vinhos e bebidas espirituosas, focada no mercado
português. Contexto de produto completo em [`briefing/aqua-vitae-mvp-briefing.md`](briefing/aqua-vitae-mvp-briefing.md)
— lê isso primeiro se for a tua primeira vez neste projeto. **Estado atual e próximos passos em
[`PLANO.md`](PLANO.md) (começa pela secção "Retomar aqui")**; contrato dos endpoints (existentes e em falta) em
[`backend/API_ENDPOINTS.md`](backend/API_ENDPOINTS.md), que é a fonte da verdade da API.

## Stack e localização

```
briefing/    briefing de produto + schema DBML original
database/    Oracle XE (Docker) — DDL, triggers, seed
backend/     API Spring Boot + Kotlin (recursos estáticos, ex. avatares, em src/main/resources/static)
android/     app Android (Compose + MVVM + Hilt) — esqueleto, ainda sem UI final
docs/        landing page (GitHub Pages, só na branch main) — o URL foi enviado à Awin, não mexer no repo/domínio
```

- **BD:** Oracle XE 21c via Docker (`gvenzl/oracle-xe`), gerido por `database/docker-compose.yml`.
  Schema em `database/ddl/`, seed em `database/seed/`. `database/run-migrations.ps1` aplica tudo.
- **Backend:** Kotlin 2.4.20 + Spring Boot 3.5.16 + Gradle 9.7.1 (wrapper já commitado, `./gradlew bootRun`).
  Hibernate em `ddl-auto: validate` — o schema é sempre gerido pelo SQL em `database/ddl`, nunca pelo Hibernate.
- **Android:** Kotlin + Jetpack Compose + Hilt + Retrofit/Moshi + DataStore. Ainda por implementar a
  maior parte dos ecrãs — ver `PLANO.md`. O `AquaVitaeApi.kt` está desatualizado face ao backend (ver `PLANO.md`).

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
  `BebidaLinkCompraRepository.findAllParaVerificacao()`).
- **Jobs longos atualizam só as colunas que lhes dizem respeito** (`@Modifying` + `@Query`), nunca um `save()` da
  entidade carregada no início — senão sobrescrevem alterações feitas entretanto (p.ex. um preço mudado por SQL).
- **Toda a paginação tem `ORDER BY` determinístico** (sem ele o "carregar mais" repete/salta itens). Se o cliente
  não pedir `sort`, o service aplica um por omissão (ver `BebidaService.comOrdenacaoPadrao`).
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
  pelo utilizador (conta, reviews, favoritos, wishlist, cave, "provadas", preferências).
- **Catálogo curado vs. ofertas de afiliados:** os atributos de uma bebida (corpo, castas, botânicos, ...) são
  sempre do catálogo, curados à mão; dos afiliados só vêm as ofertas (`bebida_link_compra`: preço, link,
  retalhista). Não estamos presos a uma rede (`retalhista_rede_afiliados` é só uma etiqueta). **Atributo sem valor
  = `NULL` = "não disponível"** — todas as colunas de atributo são anuláveis de propósito; escalas 1–5 só com fonte.
- **Nunca pedir automaticamente um link de afiliado** (conta como cliques). A verificação diária de links usa
  `bebida_link_compra_url_verificacao` (página do produto sem tracking); ver `API_ENDPOINTS.md`.
- **Ao alterar o schema:** atualizar `database/ddl/01_tables.sql` (fonte da verdade, para quem reconstrói do zero)
  **e** criar um patch `database/ddl/0N_patch_*.sql` para a BD de dev já existente (fora do `run-migrations`; só o
  README da pasta `database/` explica). Seguir os padrões existentes: atributo categórico = tabela de lookup + FK
  (o utilizador já corrigiu um desvio a isto); booleano = `NUMBER(1)` + `CHECK`.
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

**Ferramentas nesta máquina (Windows)**

- **Gradle:** se depois de editar ficheiros o `compileKotlin`/`bootRun` disser `UP-TO-DATE` ou falhar com um bean
  em falta, forçar com `--rerun-tasks`; se falhar com "Could not delete ...caches-jvm", é o daemon Kotlin do VS Code
  a segurar ficheiros — voltar a correr sem `--rerun-tasks`. Testes: `.\gradlew.bat test` (os que usam HTTP levantam
  um `HttpServer` local do JDK, sem rede real).
- **Servidor a correr em background** (`bootRun`): para o parar, `Get-NetTCPConnection -LocalPort 8080` → `Stop-Process`.
- **`gh` (GitHub CLI):** instalado e autenticado, mas no Git Bash é preciso `export PATH="/c/Program Files/GitHub CLI:$PATH"`.
- **Git:** o trabalho corrente está numa branch de feature (ver `PLANO.md`); `docs/` (landing page) só existe na
  `main` — ao mudar de branch, ficheiros aparecem/desaparecem no disco, é esperado.
- **Versões:** ao escolher/atualizar versões de Gradle/Kotlin/Spring Boot, consulta a versão atual real em vez de
  usar um número de memória — `services.gradle.org/versions/current` para o Gradle,
  `repo1.maven.org/maven2/.../maven-metadata.xml` para bibliotecas/plugins Maven. Já aconteceu tentar usar
  versões desatualizadas que não corriam no JDK instalado nesta máquina. (`BodyHandlers.limiting` não existe no JDK 21.)

## Como correr tudo localmente

```powershell
cd database; cp .env.example .env; docker compose up -d; .\run-migrations.ps1   # 1. BD
cd ..\backend; .\gradlew.bat bootRun                                            # 2. API em localhost:8080
# 3. Android: abrir android/ no Android Studio (API_BASE_URL já aponta para 10.0.2.2:8080 no emulador)
```

Conta de teste já existente na BD de dev: `demo2@aquavitae.local` / `password123` (papel `Utilizador`). Para
testar endpoints de admin, promover temporariamente por SQL (`utilizador_role_id` → o de `Admin`) e reverter depois.

## Preferências de trabalho do utilizador

- **Desenvolvimento sequencial:** uma camada de cada vez (BD → API → Android), validada a correr de
  verdade antes de avançar para a seguinte — não scaffolding paralelo de múltiplas camadas.
- Comunica em **português de Portugal**; prefere respostas e nomes de ficheiros/variáveis em português.
- **Decisões de produto/schema:** apresentar a opção recomendada com o principal compromisso e deixar o utilizador
  aprovar ou ajustar *antes* de implementar; ele costuma responder com melhorias (p.ex. lookup em vez de coluna solta).
- **Commit, push e PR só quando o utilizador pedir** (costuma pedir no fim de cada fatia).
- Conceitos de escalabilidade/otimização ficam deliberadamente para depois de tudo funcionar ponta a ponta.
