# AquaVitae API

Backend Spring Boot + Kotlin para o AquaVitae. Liga-se ao Oracle XE definido em `../database` (docker-compose).

## Stack

- Kotlin 1.9 + Spring Boot 3.3 (Web, Data JPA, Security, Validation), Java 21.
- Oracle via `ojdbc11` (driver) + Hibernate (`ddl-auto: validate` — o schema é gerido pelos scripts SQL em `../database/ddl`, não pelo Hibernate).
- Autenticação: JWT (stateless), password hash com BCrypt.
- Sem gradle wrapper commitado (ver "Correr o projeto" abaixo).

## Estrutura de pacotes

```
pt.aquavitae.api
├── config/          SecurityConfig (JWT, regras de acesso)
├── security/         JwtService, JwtAuthenticationFilter
├── common/           exceções + @RestControllerAdvice partilhados
├── lookup/            entidades read-only (categorias, países, corpo, taninos, castas, ...)
├── utilizador/        Utilizador (conta), UtilizadorController (GET /me)
├── auth/              registo/login (gera JWT)
├── produtor/           GET /api/produtores/{id}
├── bebida/             catálogo (supertype) — pesquisa + detalhe
├── vinho/              subtype "Vinho" (padrão supertype/subtype, PK partilhada com bebida)
├── review/             reviews (a trigger da BD atualiza o rating agregado)
├── favorito/ wishlist/ provada/   as 3 relações utilizador<->bebida mais simples
├── cave/               cave virtual (cave + cave_bebida)
└── preferencia/        preferências de onboarding
```

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

Este projeto não inclui o binário `gradle-wrapper.jar` (não é código-fonte, não faz sentido gerá-lo à mão).
Antes do primeiro `./gradlew`, escolhe uma destas opções:

- **Opção A — VS Code:** instala as extensões "Extension Pack for Java" e "Gradle for Java" (Microsoft),
  abre a pasta `backend/` no VS Code. A extensão consegue correr as tasks Gradle sem precisares de
  instalar o Gradle no sistema; podes gerar o wrapper a partir dela (Gradle Side Bar > Tasks > build > wrapper)
  ou correr `bootRun` diretamente por ali.
- **Opção B — instalar o Gradle uma única vez, só para gerar o wrapper:** não precisas de admin nem de
  o deixar instalado depois. Em PowerShell, dentro de `backend/`:
  ```powershell
  Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.9-bin.zip" -OutFile "$env:TEMP\gradle.zip"
  Expand-Archive -Path "$env:TEMP\gradle.zip" -DestinationPath "$env:TEMP\gradle" -Force
  & "$env:TEMP\gradle\gradle-8.9\bin\gradle.bat" wrapper --gradle-version 8.9
  ```
  Depois disto `gradlew.bat` já existe na pasta e funciona sozinho — não precisas de repetir isto nem
  manter o Gradle descarregado.

Não é preciso teres Java 21 instalado especificamente — o `settings.gradle.kts` usa o plugin
`foojay-resolver-convention`, que faz o Gradle descarregar um JDK 21 próprio (isolado, em `~/.gradle/jdks`)
na primeira compilação, independentemente da versão de Java que já tenhas no sistema.

### 4. Arrancar a API

```bash
./gradlew bootRun
```

A API fica disponível em `http://localhost:8080`. Se o arranque falhar com erro de validação de schema
Hibernate, confirma que correste as migrations do passo 1 primeiro.

## Endpoints principais

```
POST   /api/auth/register
POST   /api/auth/login

GET    /api/bebidas?search=&categoriaId=&page=&size=
GET    /api/bebidas/{id}
GET    /api/produtores/{id}

GET    /api/bebidas/{bebidaId}/reviews
POST   /api/bebidas/{bebidaId}/reviews        (auth)
PUT    /api/reviews/{id}                       (auth, dono)
DELETE /api/reviews/{id}                       (auth, dono)

GET    /api/users/me                           (auth)
PUT    /api/users/me/preferencias              (auth)

GET    /api/users/me/favoritos   POST/DELETE /api/bebidas/{id}/favorito   (auth)
GET    /api/users/me/wishlist    POST/DELETE /api/bebidas/{id}/wishlist   (auth)
GET    /api/users/me/provadas    POST/DELETE /api/bebidas/{id}/provada    (auth)

GET    /api/users/me/caves                     (auth)
POST   /api/users/me/caves                     (auth)
GET    /api/caves/{id}                         (auth, dono)
POST   /api/caves/{id}/bebidas                 (auth, dono)
PATCH  /api/caves/{id}/bebidas/{caveBebidaId}   (auth, dono)
DELETE /api/caves/{id}/bebidas/{caveBebidaId}   (auth, dono)
```

Endpoints "(auth)" exigem header `Authorization: Bearer <token>` (token devolvido por
`/api/auth/login` ou `/api/auth/register`).

## Por fazer a seguir (fora deste esqueleto)

- `bebida_link_compra` / `clique_compra`: endpoints de leitura dos links de afiliado + registo de
  clique (fluxo descrito no briefing secção 4) — ainda não implementados.
- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`).
- Testes automatizados (nenhum incluído neste esqueleto).
- CI/build pipeline, Dockerfile da própria API para deploy.
- `GET /api/bebidas` devolve `Page<BebidaSummaryDto>` diretamente — o Spring avisa em runtime que
  serializar `Page`/`PageImpl` assim não é uma API pública estável; funciona bem para o MVP, mas vale a
  pena trocar por um DTO de paginação próprio (ou `Slice`) antes de ir para produção.
