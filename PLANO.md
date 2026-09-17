# AquaVitae — Plano de trabalho

Roadmap vivo do MVP. Atualizar isto no fim de cada sessão de trabalho relevante — é o que uma sessão
nova do Claude Code (ou tu, ao voltares passado um tempo) deve ler primeiro para saber onde ficámos.

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
backend + avatares) — ainda sem PR aberto nesta sessão.

**Próximo passo — resto do backend:** recuperação de password (auth), catálogo/bebida (filtros
expandidos, enriquecimento dos DTOs, sugeridas, reviews com distribuição, links de compra), produtor,
caves, enriquecimento de favoritos/wishlist/provadas — tudo listado em `API_ENDPOINTS.md`. Depois os
ecrãs Android correspondentes. Avatares reais (PNGs) ainda por chegar do utilizador — ver secção
"Imagens" do `API_ENDPOINTS.md` para onde ficam (`backend/src/main/resources/static/avatars/`).

## Por fazer depois (fora de âmbito imediato)

- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`).
- `bebida_link_compra` / `clique_compra`: endpoints de leitura de links de afiliado + registo de clique.
- Testes automatizados (nenhum ainda).
- CI/build pipeline, Dockerfile da API para deploy.
- Trocar `Page<BebidaSummaryDto>` por um DTO de paginação próprio antes de produção.
- Conceitos de escalabilidade/otimização — deliberadamente adiados até tudo funcionar ponta a ponta.

## Fora de âmbito deste MVP (fases futuras, ver briefing)

Categoria `rum`, notícias, eventos de prova cega/social, pairing por IA, scan de rótulos.
