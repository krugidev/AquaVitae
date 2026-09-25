# AquaVitae API

Backend Spring Boot + Kotlin para o AquaVitae. Liga-se ao Oracle XE definido em `../database` (docker-compose).

## Stack

- Kotlin 2.4 + Spring Boot 3.5 (Web, Data JPA, Security, Validation), Gradle 9.7, Java 21 (toolchain).
- Oracle via `ojdbc11` (driver) + Hibernate (`ddl-auto: validate` — o schema é gerido pelos scripts SQL em `../database/ddl`, não pelo Hibernate).
- Autenticação: JWT (stateless), password hash com BCrypt.
- **Convenção:** qualquer coluna Oracle `NUMBER(p,s)` mapeia sempre para `java.math.BigDecimal` na
  entidade, nunca `Double`/`Float` — Hibernate mapeia Double para SQL `FLOAT`, o que falha a validação de
  schema contra `NUMBER` (`SchemaManagementException`). Já apanhado e corrigido uma vez; ao adicionar
  campos novos (whisky/gin/licor/vodka/aguardente), segue este padrão desde o início.
- Wrapper (`gradlew`) já commitado — gerado e testado a correr de verdade contra a BD em 2026-09-14.

## Estrutura de pacotes

```
pt.aquavitae.api
├── config/          SecurityConfig (JWT, regras de acesso), SchedulingConfig
├── security/         JwtService, JwtAuthenticationFilter
├── common/           exceções + @RestControllerAdvice partilhados
├── lookup/            entidades read-only + LookupController (/api/lookup/*)
├── utilizador/        conta + perfil (GET/PUT /api/users/me, com contadores)
├── auth/              registo/login (gera JWT) + recuperação de password por código
├── produtor/           GET /api/produtores/{id} (o resto do módulo por fazer)
├── bebida/             catálogo (supertype) — pesquisa com filtros, sugeridas, detalhe;
│                       BebidaSummaryAssembler enriquece as listas (preço, flags do utilizador)
├── vinho/              subtype "Vinho" (padrão supertype/subtype, PK partilhada com bebida)
├── review/             reviews (a trigger da BD atualiza o rating agregado)
├── favorito/ wishlist/ provada/   as 3 relações utilizador<->bebida mais simples
├── cave/               cave virtual (cave + cave_bebida) — agregados por fazer
├── compra/             retalhistas, links de compra, cliques (+ verificacao/: job diário de links)
└── preferencia/        preferências de onboarding
```

Testes em `src/test` (só os da verificação de links por agora): `.\gradlew.bat test`.

`vinho/` é o único subtype com entidades JPA completas (é a categoria com mais dados seed). As restantes
categorias (`whisky`, `gin`, `licor`, `vodka`, `aguardente`) seguem exatamente o mesmo padrão — ver o
comentário em `BebidaService.buildVinhoDetalhe` para onde estender.

**Importante:** as tabelas de conteúdo geridas pelo admin (`bebida`, `produtor`, `retalhista`,
`bebida_link_compra`, todos os lookups) não têm endpoints de escrita na API — só leitura. Para o MVP,
inserção/edição é feita diretamente via SQL Developer (decisão do briefing, secção 4). Os únicos dados
que a API escreve são os gerados pelo próprio utilizador (conta, reviews, favoritos, wishlist, cave,
provadas, preferências).

## Correr o projeto

### 1. Base de dados

Garante que o Oracle XE está a correr e com o schema aplicado — ver `../database/README.md`
(`docker compose up -d` + `./run-migrations.ps1`).

### 2. Variáveis de ambiente

O datasource (`src/main/resources/application-dev.yml`) lê:

- `ORACLE_APP_USER` / `ORACLE_APP_PASSWORD` — têm de ser os **mesmos** valores do `.env` usado no
  `docker-compose.yml` da base de dados (por omissão: `aquavitae` / `changeit123`).
- `JWT_SECRET` — opcional em dev (usa um valor por omissão inseguro só para desenvolvimento local).

### 3. Gradle wrapper

`gradlew` / `gradlew.bat` já estão commitados e prontos a usar — não precisas de instalar Gradle. Também
não precisas de ter Java 21 instalado especificamente: o `settings.gradle.kts` usa o plugin
`foojay-resolver-convention`, que faz o Gradle descarregar um JDK 21 próprio (isolado, em `~/.gradle/jdks`)
na primeira compilação, independentemente da versão de Java que já tenhas no sistema (este projeto foi
validado com JDK 25 instalado no sistema — funciona sem conflito).

### 4. Arrancar a API

```bash
./gradlew bootRun
```

A API fica disponível em `http://localhost:8080`. Se o arranque falhar com erro de validação de schema
Hibernate, confirma que correste as migrations do passo 1 primeiro.

### 5. Abrir no VS Code

Abre a pasta `backend/` no VS Code (extensões "Extension Pack for Java" e "Gradle for Java" já instaladas).
A aba "Gradle" na barra lateral mostra as tasks (`bootRun`, `test`, `build`, ...) — corres clicando, sem
precisares do terminal. O "Language Support for Java" dá-te autocompletar, ir-para-definição e deteção de
erros em tempo real nos ficheiros `.kt`.

## Exemplo completo: POST / GET / PUT / DELETE (Review)

O módulo `review/` é o exemplo de referência para os 4 verbos HTTP — usa-o como modelo ao construíres os
subtypes em falta (whisky/gin/licor/vodka/aguardente) ou outras features. Fluxo real, testado a correr:

```
POST /api/auth/register  { username, email, password, firstName, lastName }  -> { token, userId }
POST /api/bebidas/{bebidaId}/reviews  (auth)  { rating: 4.5, comment: "..." }  -> 201, review criada
GET  /api/bebidas/{bebidaId}/reviews                                          -> 200, lista com a review
PUT  /api/reviews/{reviewId}  (auth, dono)  { rating: 5.0, comment: "..." }    -> 200, review atualizada
DELETE /api/reviews/{reviewId}  (auth, dono)                                  -> 204
```

Ficheiros a seguir esta ordem para perceberes o padrão completo (Controller → Service → Repository →
Entity):
1. [`review/ReviewController.kt`](src/main/kotlin/pt/aquavitae/api/review/ReviewController.kt) — os 4 endpoints HTTP
2. [`review/ReviewService.kt`](src/main/kotlin/pt/aquavitae/api/review/ReviewService.kt) — a lógica (validações, dono da review)
3. [`review/ReviewRepository.kt`](src/main/kotlin/pt/aquavitae/api/review/ReviewRepository.kt) — acesso a dados (Spring Data JPA)
4. [`review/Review.kt`](src/main/kotlin/pt/aquavitae/api/review/Review.kt) — a entidade JPA (mapeamento para a tabela)
5. [`review/dto/ReviewDtos.kt`](src/main/kotlin/pt/aquavitae/api/review/dto/ReviewDtos.kt) — o que entra (`ReviewRequest`) e o que sai (`ReviewResponse`) pela API

Uma conta de teste já existe na BD para experimentares sem teres de registar outra: `demo2@aquavitae.local` / `password123`.

## Endpoints

A lista completa e atual — o que já existe, os parâmetros/DTOs e o que ainda falta — está em
[`API_ENDPOINTS.md`](API_ENDPOINTS.md), que é a fonte da verdade (esta secção já teve uma lista própria, que
ficou desatualizada). Endpoints marcados "(auth)" exigem `Authorization: Bearer <token>` (token devolvido por
`/api/auth/login` ou `/api/auth/register`); `/api/admin/**` exige ainda o papel `Admin`.

## Por fazer a seguir

Ver "Retomar aqui" no [`PLANO.md`](../PLANO.md) — é lá que fica a lista atual (produtor, caves, lacunas
encontradas nos mockups, testes). Notas soltas que continuam válidas:

- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`).
- CI/build pipeline, Dockerfile da própria API para deploy.
- `bebida_ano_producao`/`produtor_ano_fundacao` (mapeados como `Int`) ainda não foram testados contra
  `ddl-auto: validate` num cenário com dados — se aparecer o mesmo tipo de erro de schema que o
  Double/BigDecimal, é provavelmente o mesmo padrão de fundo (mas é menos provável, Integer/Long
  costumam bater certo com NUMBER sem drama).
- `GET /api/bebidas` devolve `Page<BebidaSummaryDto>` diretamente — o Spring avisa em runtime que
  serializar `Page`/`PageImpl` assim não é uma API pública estável; funciona bem para o MVP, mas vale a
  pena trocar por um DTO de paginação próprio (ou `Slice`) antes de ir para produção.
