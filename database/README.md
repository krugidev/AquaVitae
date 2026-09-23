# AquaVitae — Base de dados (Oracle XE)

Schema Oracle gerado a partir do DBML do briefing do MVP. Ver `../briefing/aqua-vitae-mvp-briefing.md`
para o contexto completo do produto e as decisões de modelação.

## Estrutura

```
ddl/
  01_tables.sql        CREATE TABLE de todas as tabelas (sem FKs)
  02_constraints.sql    todas as foreign keys (ALTER TABLE), uma por Ref do DBML
  03_triggers.sql        trigger de sincronização bebida_rating_medio / bebida_total_reviews
  04_patch_endpoints.sql   patch one-off p/ BD de dev JÁ existente (casta_tipo, utilizador_password_reset)
  05_patch_link_verificacao.sql   patch one-off p/ BD de dev JÁ existente (estado/verificação dos links de compra)
  06_patch_seed_notas.sql         patch p/ BD de dev JÁ existente: barrica do vinho (cask_formato, vinho_cask) + valores das
                                  notas de seed (castas, países, botânicos, ...); repetível
  07_patch_regioes.sql            patch p/ BD de dev JÁ existente: regiões como lookup (`regiao` + `produtor_regiao_id`),
                                  migra o texto antigo; repetível
  08_patch_ean_imagem.sql         patch p/ BD de dev JÁ existente: `bebida_ean` (único, anulável) e `bebida_path_image`
                                  alargada a 1000; repetível
  09_patch_paises_pt.sql          patch p/ BD de dev JÁ existente: 13 países passam à grafia PT-PT (só renome); repetível
  10_patch_regioes_lista_final.sql  patch p/ BD de dev JÁ existente: lista final de regiões (159, 6 países); repetível
  11_patch_whisky_regiao.sql      patch p/ BD de dev JÁ existente: `whisky.whisky_regiao_id` passa a apontar para `regiao` e a
                                  tabela `whisky_regiao` desaparece; repetível
  12_patch_termos_aceites.sql     patch p/ BD de dev JÁ existente: `utilizador_termos_aceites_em` (data em que aceitou os
                                  termos); repetível
  13_patch_nacionalidade_codigo.sql  patch p/ BD de dev JÁ existente: `nationality_codigo_pais` (ISO alfa-2, para a bandeira
                                  da nacionalidade) + os 8 códigos; repetível
seed/
  01_lookups.sql         tabelas de lookup preenchidas (corpo, taninos, 277 castas, 218 países, casks, ...)
  02_bebidas.sql          produtores, retalhistas e ~16 bebidas de exemplo (maioritariamente portuguesas)
verify/
  rebuild-check.sh       reconstrói a BD do zero num schema temporário e compara-a com a de dev (Git Bash)
  extrair.sql            o que o script extrai de cada schema para os comparar
docker-compose.yml       Oracle XE 21c (imagem gvenzl/oracle-xe) para desenvolvimento local
.env.example             copiar para .env antes do primeiro arranque
run-migrations.ps1 / .sh  corre os scripts acima, por ordem, dentro do container
```

## Passo a passo (primeira vez)

```bash
cd database
cp .env.example .env        # ajusta as passwords se quiseres
docker compose up -d
docker compose ps           # espera até o estado ficar "healthy" (pode demorar ~1-2 min no 1º arranque)
```

Depois, com o container saudável:

```powershell
.\run-migrations.ps1
```

ou, em bash:

```bash
./run-migrations.sh
```

Isto corre `ddl/01_tables.sql`, `ddl/02_constraints.sql`, `ddl/03_triggers.sql`,
`seed/01_lookups.sql` e `seed/02_bebidas.sql`, por esta ordem, ligando como o `APP_USER` definido no `.env`.

Os ficheiros `04_*` a `12_*` **não** fazem parte desta sequência: as alterações que fazem já estão em
`01_tables.sql`/`02_constraints.sql` (e o `06`, `07`, `09`, `10` e `11` também em `seed/`), por isso só servem para pôr ao dia
uma BD que já existia antes delas (cada um explica no topo como se corre). Ao mexer no schema: atualizar
`01_tables.sql` **e** criar um patch novo.

## Verificar a reconstrução do zero

Depois de mexer em `01_tables.sql`, `02_constraints.sql` ou nos seeds, **corre `database/verify/rebuild-check.sh`** (Git Bash, com
o contentor a correr): cria um schema temporário (`aq_rebuild`), corre lá os 5 scripts do `run-migrations` e compara-o com a BD
de dev — estrutura (tabelas, colunas, constraints, trigger), regiões, produtor→região, `pais`/`produtor_pais` e o texto de cada
tabela. Sai com 0 se está tudo igual e com 1 se houver diferenças que importam (as regras estão no cabeçalho do script; dados
que só existem na dev, como contas ou lotes de bebidas, não contam). Demora ~15 s e apaga sempre o schema temporário. Foi assim
que se apanhou a "Quinta de Soalheiro" sem região depois de "Vinho Verde" passar a "Minho"; testado com dois erros de propósito
(esse e um nome de país diferente no seed), que detetou. O `.gitattributes` mantém os `.sh` em LF (com CRLF o Git Bash não os corre).

## Encoding (mojibake)

Os `.sql` são UTF-8 **sem BOM**. No Windows PowerShell 5.1, `Get-Content` sem `-Encoding UTF8` lê-os como ANSI e os
acentos chegam ao sqlplus duplamente codificados (`França` → `FranÃ§a`), mesmo com `NLS_LANG` certo — o
`run-migrations.ps1` já passa `-Encoding UTF8`. Para scripts avulsos (patches), a via mais segura é copiar o ficheiro
para o contentor e correr com `@`, sem PowerShell pelo meio:

```bash
docker cp ddl/06_patch_seed_notas.sql aquavitae-oracle-xe:/tmp/06.sql
docker exec -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 aquavitae-oracle-xe sqlplus -s "<user>/<pass>@//localhost:1521/XEPDB1" @/tmp/06.sql
```

(Git Bash: `MSYS_NO_PATHCONV=1` e `cygpath -w` no caminho de origem do `docker cp`.) Depois de carregar dados, procurar
`LIKE '%Ã%'`, `LIKE '%â€%'` e `LIKE '%Â%'` nas colunas de texto.

## Países: duas tabelas, os mesmos ids

`pais` (origem da bebida) e `produtor_pais` (país do produtor) são duas tabelas, mas têm de ter **as mesmas linhas com
os mesmos ids**: o filtro do catálogo usa o id de `pais` e `/api/lookup/regioes` o de `produtor_pais`, e o cliente
trata-os como um só. O seed e o patch 06 garantem-no. "Inserir nas duas pela mesma ordem" **não chega** (os identity
divergem com inserts revertidos, cache ou reinícios da BD), por isso, ao acrescentar um país:

```sql
INSERT INTO pais (bebida_pais_value) VALUES ('Novo País');
INSERT INTO produtor_pais (produtor_pais_id, produtor_pais_value)
  SELECT bebida_pais_id, bebida_pais_value FROM pais WHERE bebida_pais_value = 'Novo País';
ALTER TABLE produtor_pais MODIFY (produtor_pais_id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
COMMIT;

-- verificação (esperado: 0)
SELECT COUNT(*) FROM pais a FULL OUTER JOIN produtor_pais b
    ON b.produtor_pais_id = a.bebida_pais_id AND b.produtor_pais_value = a.bebida_pais_value
 WHERE a.bebida_pais_id IS NULL OR b.produtor_pais_id IS NULL;
```

O `ALTER TABLE` reinicia o identity no máximo+1 (ids explícitos não o avançam; sem isto, um `INSERT` posterior sem id
colidia). Foi ponderado unificar as duas tabelas numa só (`produtor` a apontar para `pais`) — ficou por decisão do
utilizador manter as duas.

## Barrica do vinho

`vinho_cask` liga um vinho a uma ou mais barricas (N:N, como `vinho_casta`): madeira (`cask`, partilhada com o whisky),
formato (`cask_formato`: Bordeaux, Burgundy, Demi-Muid, Tonel, Balseiro, Foudre, Oval) e meses de estágio
(`vinho_cask_estagio_meses`). Só existe na BD — ainda sem entidade nem endpoint (nenhum mockup mostra a barrica).

## Ligar via SQL Developer

Host: `localhost`, porta `1521`, serviço `XEPDB1`, utilizador/password conforme o `.env`
(`ORACLE_APP_USER` / `ORACLE_APP_PASSWORD`). É esta ligação que se usa para gestão de conteúdo
(inserir bebidas, produtores, retalhistas) durante o MVP — ver briefing secção 4.

## Notas sobre o schema gerado

- **Correção aplicada:** `vinho_tipo.vinho_tipo_value` estava como `integer` no DBML original —
  inconsistente com todas as outras tabelas de lookup do mesmo padrão (`vinho_corpo`, `vinho_tanino`,
  etc., todas `VARCHAR2`). Foi corrigido para `VARCHAR2(30 CHAR)`; sinalizado para confirmares que é
  mesmo o que querias.
- O briefing (secção 5, prosa) descreve `favorito` e `wishlist` como tendo **PK composta**, mas o DBML
  (secção 6, a fonte usada para gerar este SQL) define ambas com PK própria (`favorito_id`,
  `wishlist_id`, ambos `increment`) — segui o DBML, tal como pedido. Se a intenção era mesmo PK
  composta `(utilizador_id, bebida_id)`, diz para eu ajustar.
- Todas as FKs são `NO ACTION` em delete/update, tal como no DBML — apagar um utilizador ou bebida com
  histórico associado tem de ser um gesto explícito da aplicação (ou de quem gere a BD), nunca um efeito
  colateral em cascata.
- Seed: nomes de produtores/marcas refletem produtos portugueses reais (Barca Velha, Licor Beirão, Gin
  Sharish, etc.), mas anos de colheita, preços e notas sensoriais são valores de desenvolvimento — não
  verificados contra a ficha técnica oficial. Revê antes de qualquer uso público.

## Regiões

`regiao` (país + nome) é um lookup e `produtor.produtor_regiao_id` aponta para ele (antes era o texto livre
`produtor_regiao`, que gerava pílulas a mais no filtro do catálogo com gralhas ou variantes). **Lista final (2026-09-20,
feita pelo utilizador): 159 regiões em 6 países para começar — Portugal 14, Espanha 20, Escócia 30, Irlanda 32 (só os
condados), Canadá 13 e EUA 50 (estados).** As regiões dos outros países acrescentam-se à medida que entram produtos
(França, Itália e Inglaterra ficaram sem regiões). Nome PT-PT quando há forma estabelecida (Califórnia, Quebeque,
Astúrias, ...) e o nome local nos restantes (Speyside, Highlands, Cork, ...); **Douro e Porto são duas regiões**. Sem
sub-regiões por agora. Quando uma lista mistura níveis (Espanha: comunidades e denominações; Escócia: Speyside dentro de
Highlands), **escolhe-se a mais específica**. As escolhas estão explicadas no topo de `10_patch_regioes_lista_final.sql`;
o bloco de regiões de `seed/01_lookups.sql` tem de ficar igual a esse patch (foi verificado: dá o mesmo conjunto).

**Um produtor novo:** escolher a região da lista; se não existir, acrescentá-la primeiro (o país é o do produtor):

```sql
-- exemplo: um produtor da Alemanha (que ainda não tem regiões na lista)
INSERT INTO regiao (regiao_nome, regiao_pais_id)
  SELECT 'Mosel', produtor_pais_id FROM produtor_pais WHERE produtor_pais_value = 'Alemanha';

INSERT INTO produtor (produtor_nome, produtor_pais_id, produtor_regiao_id)
  SELECT 'Weingut X', p.produtor_pais_id, r.regiao_id
    FROM produtor_pais p JOIN regiao r ON r.regiao_pais_id = p.produtor_pais_id
   WHERE p.produtor_pais_value = 'Alemanha' AND r.regiao_nome = 'Mosel';
```

A BD garante a coerência: a região de um produtor tem de ser do país desse produtor (FK composta
`fk_produtor_regiao`, `ORA-02291` se não for) e um produtor com região tem sempre país (`ck_produtor_regiao_pais`,
`ORA-02290`). O nome de uma região é único por país (`ORA-00001`), não no total: pode haver o mesmo nome em dois países.
Sem região é permitido (`NULL` = "não disponível"). O filtro do catálogo só mostra as regiões que têm pelo menos uma
bebida, por isso acrescentar uma região não a faz aparecer até haver um produto dela.

**Região do whisky:** também é da tabela `regiao` (desde 2026-09-21): `whisky.whisky_regiao_id` aponta para `regiao(regiao_id)` e
a tabela `whisky_regiao` deixou de existir. Um whisky de um país sem regiões na lista fica sem região (`NULL`) mas mostra o
país (`bebida.bebida_pais_origem_id`); quando entrarem produtos desse país, acrescenta-se a região como para um produtor. A BD
**não** confere que a região do whisky é do país de origem da bebida (o `whisky` não tem país): fica para o script de
verificação do catálogo.

**Ao renomear um valor de lookup** (região, país, ...), procurar o nome antigo nos seeds: os `(SELECT ... WHERE nome = '...')`
de `seed/02_bebidas.sql` não encontram nada e devolvem `NULL` **em silêncio** (a "Quinta de Soalheiro" ficou sem região quando
"Vinho Verde" passou a "Minho"; apanhado ao reconstruir a BD do zero num schema temporário e compará-la com a de dev).

## EAN e imagem das bebidas

`bebida.bebida_ean` (GTIN) é **texto**, **único** e **anulável**: serve para reconhecer a mesma bebida quando chega por outro
retalhista — antes de criar uma bebida num lote, procurar o EAN (`SELECT bebida_id, bebida_name FROM bebida WHERE bebida_ean
= '...'`); se existir, só se acrescenta um `bebida_link_compra` novo. Aceita 8 a 14 dígitos (`ck_bebida_ean`, `ORA-02290`
com letras, espaços ou hífenes — tirá-los ao gerar o SQL) e recusa repetidos (`uq_bebida_ean`, `ORA-00001`); vários `NULL`
são permitidos. Sem EAN, a deteção de duplicados faz-se à mão (nome + produtor + volume). Não é exposto na API.

`bebida.bebida_path_image` (até 1000 caracteres) guarda um caminho relativo (recurso estático do backend) **ou o URL absoluto
da imagem do retalhista/feed**; o Android usa-o tal como está se começar por `http(s)://` (ver `backend/API_ENDPOINTS.md`,
"Imagens").
