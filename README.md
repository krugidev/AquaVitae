# AquaVitae

App de catalogação e comunidade de vinhos e bebidas espirituosas (Portugal), com recomendações
personalizadas, reviews, cave virtual e compra via afiliados. Ver
[`briefing/aqua-vitae-mvp-briefing.md`](briefing/aqua-vitae-mvp-briefing.md) para o contexto de
produto completo e o schema de dados original.

Este repositório contém o esqueleto base do MVP: base de dados, API e projeto Android.

## Estrutura

```
briefing/    briefing de produto + schema original (contexto persistente do projeto)
database/    schema Oracle (DDL + triggers + seed) e docker-compose para Oracle XE local
backend/     API Spring Boot + Kotlin
android/     app Android (Kotlin + Jetpack Compose + MVVM)
```

## Decisões de arquitetura tomadas

- **Backend:** Spring Boot + Kotlin (não ORDS nem Node) — mesma linguagem do Android, tipagem forte.
- **Base de dados de desenvolvimento:** Oracle XE local via Docker (`gvenzl/oracle-xe`).
- **Autenticação:** JWT stateless, password hash com BCrypt.
- **Gestão de conteúdo (bebidas, produtores, retalhistas, lookups):** só pelo admin via SQL Developer
  para o MVP — a API não expõe escrita nessas tabelas (ver briefing secção 4). A API só escreve dados
  gerados pelo utilizador (conta, reviews, favoritos, wishlist, cave, "provadas", preferências).

Duas notas de correção/divergência do briefing original ficaram documentadas em
[`database/README.md`](database/README.md#notas-sobre-o-schema-gerado) — vale a pena confirmares que
concordas com as escolhas feitas.

## Arranque rápido

1. **Base de dados** — ver [`database/README.md`](database/README.md).
   ```bash
   cd database
   cp .env.example .env
   docker compose up -d
   ./run-migrations.ps1   # ou run-migrations.sh
   ```
2. **API** — ver [`backend/README.md`](backend/README.md).
   ```bash
   cd backend
   ./gradlew bootRun
   ```
3. **Android** — ver [`android/README.md`](android/README.md). Abre a pasta `android/` no Android
   Studio; com a API a correr em `localhost:8080`, o emulador já está configurado para lhe aceder via
   `10.0.2.2:8080`.

## Ordem de dependência dos ecrãs (MVP)

Registo/login + onboarding de preferências → catálogo/pesquisa → página de detalhe da bebida →
reviews → cave virtual / wishlist / favoritos. O backend e o Android skeleton foram estruturados
para seguir esta mesma ordem.

## Fora de âmbito deste MVP (fases futuras, ver briefing)

Categoria `rum`, notícias, eventos de prova cega/social, pairing por IA, scan de rótulos.
