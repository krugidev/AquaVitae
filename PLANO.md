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
  Dados já corrompidos na BD de dev corrigidos via UPDATE (`bebida`, `produtor` incl. `produtor_regiao`,
  `utilizador_nationality`, `pais`, `produtor_pais`, `vinho_tipo`, além das 2 castas já corrigidas antes).
  Os ficheiros `.sql` de seed em si nunca estiveram corrompidos — só os dados inseridos na BD.

**Próximo passo — camada backend:** implementar os endpoints novos/ajustados listados em
`API_ENDPOINTS.md` (auth de recuperação de password, `LookupController`, filtros expandidos do catálogo,
enriquecimento dos DTOs de bebida/cave/produtor/perfil), depois os ecrãs Android correspondentes.

## Por fazer depois (fora de âmbito imediato)

- Subtypes whisky/gin/licor/vodka/aguardente no `BebidaService` (replicar o padrão de `vinho/`).
- `bebida_link_compra` / `clique_compra`: endpoints de leitura de links de afiliado + registo de clique.
- Testes automatizados (nenhum ainda).
- CI/build pipeline, Dockerfile da API para deploy.
- Trocar `Page<BebidaSummaryDto>` por um DTO de paginação próprio antes de produção.
- Conceitos de escalabilidade/otimização — deliberadamente adiados até tudo funcionar ponta a ponta.

## Fora de âmbito deste MVP (fases futuras, ver briefing)

Categoria `rum`, notícias, eventos de prova cega/social, pairing por IA, scan de rótulos.
