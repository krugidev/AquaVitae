# AquaVitae — contexto para o Claude Code

App Android (Kotlin) de catalogação e comunidade de vinhos e bebidas espirituosas, focada no mercado
português. Contexto de produto completo em [`briefing/aqua-vitae-mvp-briefing.md`](briefing/aqua-vitae-mvp-briefing.md)
— lê isso primeiro se for a tua primeira vez neste projeto. Estado atual e próximos passos em [`PLANO.md`](PLANO.md).

## Stack e localização

```
briefing/    briefing de produto + schema DBML original
database/    Oracle XE (Docker) — DDL, triggers, seed
backend/     API Spring Boot + Kotlin
android/     app Android (Compose + MVVM + Hilt) — esqueleto, ainda sem UI final
```

- **BD:** Oracle XE 21c via Docker (`gvenzl/oracle-xe`), gerido por `database/docker-compose.yml`.
  Schema em `database/ddl/`, seed em `database/seed/`. `database/run-migrations.ps1` aplica tudo.
- **Backend:** Kotlin 2.4.20 + Spring Boot 3.5.16 + Gradle 9.7.1 (wrapper já commitado, `./gradlew bootRun`).
  Hibernate em `ddl-auto: validate` — o schema é sempre gerido pelo SQL em `database/ddl`, nunca pelo Hibernate.
- **Android:** Kotlin + Jetpack Compose + Hilt + Retrofit/Moshi + DataStore. Ainda por implementar a
  maior parte dos ecrãs — ver `PLANO.md`.

## Convenções importantes (para não repetir bugs já apanhados)

- **Qualquer coluna Oracle `NUMBER(p,s)` mapeia para `java.math.BigDecimal` na entidade JPA, nunca
  `Double`/`Float`.** Hibernate mapeia Double para SQL `FLOAT`, que falha a validação de schema contra
  `NUMBER` (`SchemaManagementException`). Já apanhado e corrigido uma vez em vários campos
  (`bebida.ratingMedio`, `review.rating`, `produtor.latitude/longitude`, etc.) — aplica desde o início em
  qualquer entidade nova.
- **Nunca aceder a uma associação `@ManyToOne(fetch = LAZY)` dentro do `JwtAuthenticationFilter`** (ou
  qualquer código que corra antes do Open-Session-In-View estar ativo) sem um fetch explícito —
  `LazyInitializationException` aí manifesta-se ao cliente como um 401 genérico, difícil de diagnosticar.
  Ver `UtilizadorRepository.findByIdWithRole()` como padrão (query com `LEFT JOIN FETCH`).
- **Tabelas de conteúdo geridas pelo admin** (`bebida`, `produtor`, `retalhista`, `bebida_link_compra`,
  todos os lookups incluindo `casta`) **não têm endpoints de escrita na API** — só leitura. Inserção/edição
  é direta via SQL Developer ou `sqlplus` (decisão do briefing, secção 4). Só a API escreve dados gerados
  pelo utilizador (conta, reviews, favoritos, wishlist, cave, "provadas", preferências).
- **Padrão supertype/subtype:** `bebida` é a tabela mãe; cada categoria (`vinho`, `whisky`, `gin`, ...)
  tem tabela própria com `bebida_id` como PK e FK partilhada. No Kotlin isto é `@OneToOne` + `@MapsId`
  (ver `Vinho.kt`). Só `vinho` tem entidade/DTO completos para já — as outras categorias seguem o mesmo
  padrão quando forem necessárias (ver comentário em `BebidaService.buildVinhoDetalhe`).
- **Ao escolher/atualizar versões de Gradle/Kotlin/Spring Boot, consulta a versão atual real em vez de
  usar um número de memória** — `services.gradle.org/versions/current` para o Gradle,
  `repo1.maven.org/maven2/.../maven-metadata.xml` para bibliotecas/plugins Maven. Já aconteceu tentar usar
  versões desatualizadas que não corriam no JDK instalado nesta máquina.

## Como correr tudo localmente

```powershell
cd database; cp .env.example .env; docker compose up -d; .\run-migrations.ps1   # 1. BD
cd ..\backend; .\gradlew.bat bootRun                                            # 2. API em localhost:8080
# 3. Android: abrir android/ no Android Studio (API_BASE_URL já aponta para 10.0.2.2:8080 no emulador)
```

Conta de teste já existente na BD de dev: `demo2@aquavitae.local` / `password123`.

## Preferências de trabalho do utilizador

- **Desenvolvimento sequencial:** uma camada de cada vez (BD → API → Android), validada a correr de
  verdade antes de avançar para a seguinte — não scaffolding paralelo de múltiplas camadas.
- Prefere respostas e nomes de ficheiros/variáveis em português (o utilizador comunica em português).
- Conceitos de escalabilidade/otimização ficam deliberadamente para depois de tudo funcionar ponta a ponta.
