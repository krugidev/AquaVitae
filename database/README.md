# AquaVitae — Base de dados (Oracle XE)

Schema Oracle gerado a partir do DBML do briefing do MVP. Ver `../briefing/aqua-vitae-mvp-briefing.md`
para o contexto completo do produto e as decisões de modelação.

## Estrutura

```
ddl/
  01_tables.sql        CREATE TABLE de todas as tabelas (sem FKs)
  02_constraints.sql    todas as foreign keys (ALTER TABLE), uma por Ref do DBML
  03_triggers.sql        trigger de sincronização bebida_rating_medio / bebida_total_reviews
seed/
  01_lookups.sql         tabelas de lookup preenchidas (corpo, taninos, castas, países, ...)
  02_bebidas.sql          produtores, retalhistas e ~16 bebidas de exemplo (maioritariamente portuguesas)
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
