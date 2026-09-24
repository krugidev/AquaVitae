# AquaVitae — design dos ecrãs (referência para construir o frontend)

Os ecrãs vêm do Figma do utilizador, enviados como prints. Esta pasta guarda os prints e **o que foi pedido para cada ecrã**,
para não depender da conversa onde foram enviados. **Regra:** cada ecrã novo entra aqui (print + notas + estado) antes de se
construir, e o estado atualiza-se no fim de cada fatia (ver também `../README.md` e o `PLANO.md`).

```
design/
  auth-onboarding/   os 13 prints de 2026-09-21 (loading, login, recuperar password, registo, onboarding)
  homepage/          o mockup da homepage (fatia 2a), enviado em 2026-09-23
  catalogo/          os 2 mockups do catálogo/filtros (fatia 2b), enviados em 2026-09-23: popup de filtros + resultados
  caves/             os 5 mockups da cave e do popup de bebida (fatia 3), enviados em 2026-09-23 (ver "16." abaixo — feito todo)
  logos/             logótipo entregue pelo utilizador (2026-09-23): logo-homepage-normal.png
  icones/            os 6 ícones da barra de navegação, entregues pelo utilizador (2026-09-23)
  implementado/      screenshots do emulador do que já está feito, para comparar com os prints
                     (fatia-1a-resumo.png; 1b-NN-*.png = fatia 1b; 2a-NN-*.png = homepage; 2b-NN-*.png = catálogo/filtros;
                     3a-NN-*.png = popup de detalhe da bebida; 3b-NN-*.png = cave)
```

Os prints são frames do Figma a ~1× (auth ≈ 380×611, onboarding ≈ 290×475, escalados): **1 px ≈ 1 dp**. O utilizador autorizou
expressamente **melhorar o que fique mais bonito e limpo** ("se quiser alterar alguma parte do UI para ser mais bonita e clean pode
fazê-lo"): quando isso acontecer, anotar a diferença na coluna "Notas" abaixo.

## Tokens (medidos nos pixels dos prints; código em `app/.../ui/theme/`)

| O quê | Valor | Onde |
|---|---|---|
| Grená (botões, títulos, rótulos, linhas) | `#8F321D` | `Burgundy` |
| Tinta ("AQUA" do logótipo, texto escrito) | `#1B1714` | `Ink` |
| Fundo dos ecrãs | `#FFFFFF` | `Paper` |
| Cartão | `#F8F8F8` | `CardGray` |
| Botão "voltar" | `#E9E9E9` (seta preta) | `ChipGray` |
| Pontos do loading / erros | `#C41F21` | `DotsRed` / `ErrorRed` |
| Extremo dos sliders de doçura/acidez | `#1D5DFE` (degradê de branco a azul) | `SliderBlue` |
| Escurecimento atrás de popups | preto a 43 % (`#919191` sobre branco) | `ScrimBlack` |
| Formas | cartão/popup raio 24, botão grande raio 26 | `Shape.kt` |
| Tipo de letra | Inter (Bold nos rótulos e botões) | `Type.kt` (`AquaText`) |

**Emblema** (diâmetro 92): anel exterior fino 5, intervalo 6, anel grosso 10, buraco central 50 (`res/drawable/ic_logo_emblem.xml`).
Há 3 versões do logótipo: **empilhado** (emblema por cima do nome: login, registo, recuperar), **horizontal com a etiqueta
"ESPIRITUOSAS & FACTOS"** (loading) e **compacto** (emblema pequeno + nome, topo dos ecrãs de preferências). O nome "AQUAVITAE" é
texto Inter (aproximação): falta o **SVG do logótipo**.

**Padrões visuais:** cartão cinzento muito arredondado; campos só com linha grená por baixo e rótulo grená a negrito; botão grande
grená que **se sobrepõe à margem inferior do cartão** (login, registo); cabeçalho em **pílula grená** com título branco a negrito
(recuperar password, preferências); navegação por dois botões redondos (voltar cinzento, seguir grená) ou um "PRÓXIMO →" grená;
"IGNORAR" em texto por baixo.

## Ecrãs

Estado: ✅ feito e validado no emulador · ⏳ por fazer · 🔶 provisório.

| # | Ecrã | Print | Estado |
|---|---|---|---|
| 1 | Loading | `01-loading.png` | ✅ fatia 1a |
| 2 | Login | `02-login.png` | ✅ fatia 1a |
| 3a | Recuperar password — identificar (username ou email) | *(sem print)* | 🔶 desenhado por mim (fatia 1b), no estilo do 3 |
| 3 | Recuperar password — código | `03-recuperar-codigo.png` | ✅ fatia 1b (2026-09-22) |
| 4 | Recuperar password — nova password | `04-recuperar-nova-password.png` | ✅ fatia 1b |
| 5 | Modal "Password alterada!" | `05-password-alterada.png` | ✅ fatia 1b |
| 6 | Registo (fase 1) | `06-registo.png` | ✅ fatia 1a |
| 7 | Onboarding — nome | `07-onboarding-nome.png` | ✅ fatia 1b |
| 8 | Onboarding — nacionalidade e descrição | `08-onboarding-nacionalidade-descricao.png` | ✅ fatia 1b |
| 9 | Onboarding — avatar | `09-onboarding-avatar.png` | ✅ fatia 1b |
| 10 | Onboarding — tipos de bebida | `10-onboarding-tipos-bebida.png` | ✅ fatia 1b |
| 11 | Onboarding — doçura (slider) | `11-onboarding-docura.png` | ✅ fatia 1b |
| 12 | Onboarding — acidez (slider) | `12-onboarding-acidez.png` | ✅ fatia 1b |
| 13 | Onboarding — castas (opcional) | `13-onboarding-castas.png` | ✅ fatia 1b |
| — | Popup dos termos e condições | *(sem print)* | 🔶 desenhado por mim (fatia 1a); o utilizador pode enviar o do Figma |
| — | Ícone da app | *(sem print; só o emblema)* | ✅ emblema grená sobre branco, adaptativo + monocromático |
| 14 | Homepage (fatia 2a) | `homepage/01-homepage.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 15a | Catálogo — popup de filtros | `catalogo/01-filtros-popup.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 15b | Catálogo — resultados | `catalogo/02-catalogo-resultados.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 16a | As minhas Caves | `caves/01-caves-lista.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 16b | Popup "Nova cave" | `caves/02-nova-cave-popup.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 16c | Popup de detalhe da bebida — Detalhes | `caves/03-bebida-detalhe-popup.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 16d | Popup "Adicionar à cave" | `caves/04-adicionar-a-cave-popup.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |
| 16e | Popup de detalhe da bebida — Reviews | `caves/05-bebida-reviews-popup.png` | ✅ feita e validada ao vivo (2026-09-23) — ver secção própria abaixo |

## O que foi pedido para cada ecrã

**1. Loading.** Logótipo horizontal ao centro e, em baixo, **5 pontos vermelhos que aumentam e diminuem de tamanho em sequência**
("uma rodinha ou 5 pontos vermelhos, até prefiro esta segunda"). *Feito.* Notas: o fundo do print é cinzento `#D9D9D9` (a cor por
omissão do Figma); usei branco como nos outros ecrãs. Decide para onde ir pela sessão (ver `../README.md`).

**2. Login.** "Username ou Email", "Password" (olho para mostrar), "Esqueci-me da password", botão "LOGIN", "AINDA NÃO TENS CONTA?
REGISTA-TE" e "TERMOS E CONDIÇÕES" no fundo. *Feito.* Notas: o olho do print é laranja/amarelo, o meu é grená; depois do login,
quem ainda não aceitou os termos vê o popup.

**3–5. Recuperar password.** *Feito (fatia 1b, 2026-09-22).* (3) Cabeçalho "RECUPERAR PASSWORD", texto "Enviámos um código único para o email
'******ouser@gmail.com'", "INSIRA O CÓDIGO ABAIXO" e **5 espaços** para o código, com seta para trás e para a frente. (4) "Nova
Password" e "Repetir a Password", com as duas setas. (5) Depois de mudar: modal grená **"Password alterada!"** com um visto, por
cima do login escurecido.
- **O print não tem o 1.º passo** (onde se escreve o email/username): desenhei um no mesmo estilo (pílula, uma frase de apoio, o
  campo "Username ou Email", seta para trás = voltar ao login). **Decisão do utilizador (2026-09-22): username OU email**, como o login
  (o backend passou a aceitar `identificador` nos 3 endpoints).
- **O backend gera 6 dígitos**, o print tem 5 espaços: **usa 6** (não se mexeu no backend).
- O email mascarado só se mostra se o utilizador escreveu o **email** (`******o2@aquavitae.local`: as últimas letras do nome e o
  domínio); se escreveu o username, a frase é genérica ("Se a conta existir, enviámos um código único para o email associado."):
  senão revelava parte do email de uma conta a quem adivinhar um username.
- **Comportamento:** o teclado numérico abre sozinho no passo do código (dá para colar o código); a seta para trás e o gesto de voltar
  percorrem os passos (código → identificar → login); código errado/expirado → mensagem da API em vermelho e a caixa pintada de vermelho;
  passwords diferentes → "As passwords não coincidem." e o campo da repetição a vermelho; ao concluir volta ao login e mostra o popup
  "Password alterada!" (fecha-se sozinho passados ~2,6 s ou ao tocar fora: o desenho não tem botão).
- **Ideia do utilizador (a fazer a seguir a estes ecrãs, com o sistema de email):** um **temporizador de validade do código único** no
  ecrã do código e a possibilidade de **pedir um código novo quando o tempo acaba**. Pede backend: `POST /recuperar-password` devolver
  `expiraEm` (sempre, exista ou não a conta), um pedido novo **invalidar os códigos anteriores** (hoje ficam todos válidos 15 min)
  e um intervalo mínimo entre pedidos. *Adiado:* fica para quando houver o email real. **Até lá o código não chega por email: aparece no
  log da API** (`Código de recuperação de password para ...`), por isso o texto "Enviámos um código..." só passa a ser verdade com o email.

**6. Registo.** "Username", "Email", "Password" (olho), botão "COMEÇAR", "JÁ TENS UMA CONTA? LOGIN", "TERMOS E CONDIÇÕES". *Feito.*
"COMEÇAR" valida, mostra o popup dos termos e só cria a conta ao aceitar; depois segue para o onboarding.

**7–13. Onboarding** *(feito na fatia 1b, 2026-09-22)* (depois do registo; o utilizador pode **ignorar todas as perguntas** e isso
**não entra nas preferências**). Tudo é guardado **no fim** do último ecrã, de uma vez: `PUT /api/users/me` (só se preencheu algo do
perfil) e `PUT /api/users/me/preferencias` (só se respondeu a algo); se ignorou tudo não se faz pedido nenhum. Se falhar, fica no último
ecrã com a mensagem e pode tentar de novo. Se a app for fechada a meio perde-se o que estava por guardar (o perfil pode ser preenchido
mais tarde; o login de uma conta já criada vai direto para o catálogo).
- **7. Nome:** cabeçalho "QUAL É O TEU NOME ?", campos "Nome próprio" e "Apelido", "PRÓXIMO →". (`PUT /api/users/me`: `firstName`, `lastName`.)
- **8. Nacionalidade e descrição:** "ESTAMOS QUASE LÁ", "Nacionalidade" com **bandeira** e seta de lista, "Descrição do perfil" (caixa de
  texto com ícone de editar), "PRÓXIMO →". (`nationalityId`, `bioDesc`; `GET /api/lookup/nacionalidades`.) **Aberto:** a bandeira
  precisa de um código de país e `utilizador_nationality` só tem o texto ("Portuguesa") — ver decisões no `PLANO.md`.
- **9. Avatar:** "ESCOLHE UM AVATAR", grelha 3×3, "PRÓXIMO →". **Pedido: acrescentar o filtro por categoria de avatar, em pílulas**
  (Castas / Garrafas / Copos). (`GET /api/lookup/avatar-categorias`, `/avatares?categoriaId=`, `avatarId`; os avatares são SVG servidos
  pela API — requer a biblioteca Coil com suporte a SVG.)
- **10. Tipos de bebida:** "COMPLETA O PERFIL — QUE TIPOS DE BEBIDA PREFERES?" com VINHO, WHISKY, GIN, AGUARDENTE, LICOR, VODKA.
  **Pedido: permitir escolher vários.** (`categoriaIds` em `PUT /api/users/me/preferencias`; `GET /api/lookup/categorias-bebida`.)
- **11. Doçura:** "GOSTAS DE BEBIDAS MAIS DOCES OU SECAS?" — **slider vertical** (degradê de branco a azul) com MUITO SECA, SECA,
  EQUILIBRADA, DOCE, MUITO DOCE; seta para trás, seta para a frente e "IGNORAR". (`docuraMin`/`docuraMax`, 1 = muito seca … 5 = muito doce.)
- **12. Acidez:** "GOSTAS DE BEBIDAS MAIS MACIAS OU FRESCAS?" — igual ao anterior, com MUITO MACIA, MACIA, EQUILIBRADA, FRESCA, MUITO FRESCA.
  (`acidezMin`/`acidezMax`, 1 = muito macia … 5 = muito fresca.)
- **13. Castas (opcional):** "VINHO: QUE TIPOS DE CASTAS MAIS APRECIAS?", lista com TOURIGA NACIONAL, TOURIGA FRANCA, TINTA RORIZ, ALVARINHO,
  BAGA, ARINTO… com barra de scroll; seta para trás, para a frente e "IGNORAR". **Só aparece se o utilizador marcou VINHO no ecrã 10.**
  **Pedido: escolher várias e, ao chegar ao fim das primeiras 10, "carregar" mais 10 na lista.** (`castaIds`; `GET /api/lookup/castas`
  devolve as ~277 de uma vez, por isso a paginação de 10 em 10 faz-se na app.)
- **Sliders:** a API guarda **intervalos** e o slider escolhe **um nível**: **decidido (2026-09-22): enviar `min = max = nível`** (um
  "DOCE" vai como `docuraMin = docuraMax = 4`). Escolhe-se tocando num nível ou arrastando; sem tocar não há resposta (o ponteiro fica
  esbatido a meio).
- **Ordem:** nome → nacionalidade/descrição → avatar → tipos → doçura → acidez → castas (se vinho) → catálogo.
- Cabeçalho: nos ecrãs 10–13 o logótipo é o **compacto** (canto superior esquerdo); nos 7–9 o **empilhado** (o mesmo tamanho do login).
- **Nacionalidade:** **decidido (2026-09-22): código ISO na BD** (`utilizador_nationality.nationality_codigo_pais`, patch `13`); a app
  desenha a bandeira (emoji) a partir dele, na lista e no rebordo do campo. Uma nacionalidade nova é uma linha de SQL, sem mexer na app.
- **Castas:** **decidido (2026-09-22): destaques no topo** — as 6 do print, pela ordem dele (Touriga Nacional, Touriga Franca,
  Aragonez (Tinta Roriz), Alvarinho, Baga, Arinto) e depois as restantes por ordem alfabética; a lista mostra 10 e, ao chegar ao fim,
  mais 10 (as 277 já estão na app). Sem isto a Touriga Nacional caía na posição 253. (No print aparece "Tinta Roriz"; na BD chama-se
  "Aragonez (Tinta Roriz)".)

## Diferenças conscientes face aos prints (fatia 1a)

Fundo do loading branco; olho da password grená; rótulos dos campos que sobem e encolhem ao escrever (não desaparecem); sombra
suave nos botões grandes; popup dos termos meu; "LOGIN" e "COMEÇAR" com a mesma largura do cartão.

## Diferenças conscientes face aos prints (fatia 1b)

Diz-me se preferes o original em qualquer uma.

- **Recuperar password:** o 1.º passo (identificar) é meu; o código tem **6** espaços (o print tem 5); o email tapado só aparece a quem
  escreveu o email; o popup "Password alterada!" fecha-se sozinho (~2,6 s) porque o desenho não tem botão. O escurecimento atrás dos
  popups (este e o dos termos) passou de 60 % (omissão do Compose) para os **43 % do print** (`#919191` sobre branco, confirmado no pixel).
- **Opções de escolha (tipos de bebida, castas):** o círculo do print fica **cheio, com um visto** quando está escolhida (é escolha
  múltipla) e a linha ganha um fundo grená a 10 % e o contorno mais grosso, para se ver bem o que está marcado. As linhas do print são
  transparentes com contorno grená (igual).
- **Ordem dos tipos de bebida:** vem da API (Vinho, Whisky, Gin, **Vodka**, Aguardente, Licor); o print tem a Vodka no fim. Se quiseres a
  ordem do print, muda-se a ordem dos ids no seed (ou ordena-se na app).
- **Slider de doçura/acidez:** o ponteiro é **grená** (o do print é o cinzento `#D9D9D9` por omissão do Figma, quase invisível sobre o
  cartão); os níveis não escolhidos ficam esbatidos depois de escolher um; sem escolha o ponteiro fica esbatido a meio. Os rótulos
  distribuem-se pela altura toda (o do print está ligeiramente irregular).
- **Avatares:** grelha 3×3 de quadrados arredondados (como no print) e não os círculos da "especificação do perfil" do README dos
  avatares; **pílulas de categoria** (Castas, Garrafas, Copos) por cima, como pedido; o avatar escolhido fica com contorno grosso e fundo
  tingido; tocar nele outra vez tira-o.
- **Nacionalidade:** a lista (menu) mostra a bandeira e o nome; o campo mostra o nome escolhido, com o rótulo a subir, e a bandeira no
  rebordo (o print mostra a bandeira de Portugal por omissão; a app começa sem nada escolhido).
- **Navegação:** nos ecrãs 7–10 só há "PRÓXIMO →" (como nos prints); voltar atrás é pelo gesto/botão do sistema. Nos 11–13 há seta para
  trás, "IGNORAR" e seta para a frente.
- **Ecrãs de preferências (10–13):** o cartão ocupa a altura toda e a navegação fica encostada ao fundo (como no print); num telemóvel
  alto sobra bastante espaço vazio dentro do cartão nos ecrãs curtos (tipos de bebida).
- **Teclado:** nos campos de password a tecla "Seguinte" passa ao campo seguinte (Nova → Repetir): o olho da password deixou de ser
  focável pelo teclado (antes o foco parava nele). Nos ecrãs de recuperar password o conteúdo rola até ao fim quando o teclado abre,
  para os botões de navegação não ficarem tapados.

## 14. Homepage (fatia 2a, feita e validada ao vivo — 2026-09-23)

Mockup enviado a 2026-09-23 (`homepage/01-homepage.png`, 163×767 — muito pequeno; as medidas exatas de espaçamento/tamanho de letra
foram aproximadas, não pixel-medidas como nos ecrãs de auth). Junto com o mockup, o utilizador enviou o **logótipo real**
(`logos/logo-homepage-normal.png`, fundo branco — tratado para transparente em `res/drawable/ic_wordmark_home.png`, usado só na
homepage por agora) e os **6 ícones da barra de navegação** (`icones/*.png`: homepage, catálogo, cave, wishlist, favorito, perfil —
SVG-like em traço branco/cinza-claro sobre fundo transparente, pensados para se tingirem por código; copiados para
`res/drawable/ic_nav_*.png` e `ic_perfil.png`, sem os reexportar).

**O que o ecrã mostra e o que ficou feito nesta 1.ª parte:**
- **Cabeçalho:** logótipo (a imagem real) à esquerda, avatar (SVG do onboarding, ou as iniciais do nome/username) à direita; por
  baixo, o dia da semana + data por extenso, e "O que queres **provar** hoje?" numa serifa itálica (ver "Tipografia" abaixo).
  **Feito**, ligado a `GET /users/me` (nome, avatar).
- **Pesquisa:** a barra com o placeholder do mockup; **feita só como botão** — toca nela e vai para o `catalog` (o ecrã de
  pesquisa a sério é o placeholder do esqueleto, por refazer numa fatia própria). Sem campo de texto funcional aqui.
- **"Escolhido para ti":** pílulas de categoria (`GET /lookup/categorias-bebida`, a "Vinho" escolhida por omissão) que filtram
  `GET /bebidas?categoriaIds=&sort=ratingMedio,desc&size=2` — **decisão tomada ao construir** (documentada em "Diferenças
  conscientes" abaixo): usei o catálogo filtrado por categoria em vez de `GET /bebidas/sugeridas` (esse endpoint não aceita
  filtro de categoria, só preferências do utilizador). **Feito e validado ao vivo** (troquei para "Whisky", mostrou "Sem
  sugestões nesta categoria." — o resto do ecrã não recarregou, como previsto). **"Ver mais sugestões" feito (2026-09-23):** link
  no cabeçalho da secção (reutiliza o `link` do `SectionHeader`, já usado em "As minhas Caves"), vai para o `catalog`
  (placeholder do esqueleto, sem filtro pré-aplicado — o mesmo destino da pesquisa) — validado ao vivo.
- **"As minhas Caves":** pílulas com as caves do utilizador (`GET /users/me/caves`) + botão "+" (**sem ação própria ainda** —
  vai para o `cave`, placeholder); lista das garrafas da cave escolhida (`GET /caves/{id}`, `prontasAAbrir` + `emGuarda`) com
  quantidade, nome, categoria + janela de consumo, preço. **Validado ao vivo com garrafas a sério (2026-09-23):** criei pela API
  uma cave para `demo_user2` (`Adega Principal`) com 2 garrafas — uma "pronta a abrir" (janela já aberta) e outra "em guarda"
  (janela no futuro) — ambas renderizam corretamente (quantidade, nome, "VINHO • BEBER ENTRE aaaa-aaaa", preço `/UN`); a lista
  concatena as duas listas do `CaveDetailResponse` sem separador visual entre estados (prontas primeiro, depois em guarda — como
  a API devolve). A pílula extra do mockup ("Vinho 2025", por baixo do nome da cave) **continua por modelar** — não é claro o
  que representa (um filtro por categoria dentro da cave? uma "vintage"?) — a confirmar com o utilizador.
- **"Estatísticas":** as 3 caixas (provadas/wishlist/favoritos) — **feito e validado ao vivo** com números reais (`GET /users/me`
  já tinha os 3 campos, da fatia 1b).
- **Produtor em destaque:** cartão grená escuro com `GET /produtores/destaque` (nome, região, ano de fundação, "recebe visitas",
  história, nº de garrafas no catálogo). **Feito e validado ao vivo** ("Luís Pato", Bairrada). O botão "VER PRODUTOR" **ainda não
  tem destino** (a página de um produtor é um ecrã por construir).
- **Barra de navegação (`BottomNavBar`):** os 5 ícones do mockup, com o "Home" elevado ao centro num círculo grená com halo
  branco. **Feita e validada ao vivo**: troca de separador funciona (`Catálogo` ↔ `Home`), preserva o estado de cada aba
  (`saveState`/`restoreState`, o padrão standard de navegação por abas). Os ícones tingem-se por código (`ColorFilter`, branco
  cheio quando selecionado, 62% de opacidade quando não) — não precisei de reexportar variantes.

**Contas de teste usadas:** `demo_user2` (a mesma da fatia 1b) — tem 1 review (dá o "Barca Velha 2015" na sugestão) e, desde
2026-09-23, **uma cave real** (`Adega Principal`, id 21, criada pela API para a validação acima — ver `PLANO.md`, "Ambiente no
fim da sessão"): 2 garrafas (Barca Velha 2015 ×2, pronta; Esporão Reserva Tinto 2018 ×1, em guarda). Fica na BD de propósito,
para continuar a servir de dado de teste (o ecrã de "adicionar à cave" ainda não existe, por isso continua a ser a única forma
de lá pôr garrafas).

### Diferenças conscientes face ao mockup (fatia 2, homepage)

Diz-me se preferes o original em qualquer uma — nenhuma delas está fechada.

- **Tipografia serifada nos títulos:** o mockup usa uma serifa nos títulos de secção, no "O que queres provar hoje?" e no nome
  de uma bebida num cartão — diferente do Inter do resto da app. **Usei a serifa do sistema** (`FontFamily.Serif`, ver
  `AquaText.SectionSerif`/`GreetingSerif`/`BebidaNomeSerif` em `Type.kt`) como aproximação — não sei qual é a serifa exata do
  Figma. Se tiveres o nome da fonte (ou os ficheiros, como fizeste com a Inter), digo-te.
- **Cartão "Escolhido para ti":** o mockup mostra `"VINHO TINTO • ACIDEZ/DOÇURA 4 • LEVE"` — a categoria com o **tipo do vinho**
  (Tinto/Branco) e um número só para acidez/doçura combinadas. Nenhum destes dois vem em `BebidaSummaryDto` (só vêm `corpo`,
  `nivelAcidez` e `nivelDocura` em separado, e o tipo de vinho só está no *detalhe*, não no resumo). Mostrei
  `"VINHO • ENCORPADO"` (categoria + corpo) e omiti a linha de acidez/doçura, para não inventar uma fórmula que combine os
  dois. Se quiseres a linha completa, dá para acrescentar `tipo` a `BebidaSummaryDto` (backend) e decidir a fórmula do número
  combinado.
- **"Escolhido para ti" não usa `/bebidas/sugeridas`:** ver acima — as pílulas de categoria funcionam melhor com o catálogo
  filtrado. O endpoint `/sugeridas` fica por usar por agora.
- **Produtor da região do produtor** não mostrado como no mockup ("Melgaço" por baixo do nome do produtor, no cartão de
  bebida) — `BebidaSummaryDto` não traz a região do produtor, só o nome.
- **Escurecimento/cores exatas:** só medi um punhado de cores no mockup (163×767 px, pouca margem para acertar no pixel certo);
  usei sobretudo os tokens já existentes (`Burgundy`, `CardGray`, `MutedInk`, ...) + 2 novos (`WineDark` para o cartão do
  produtor, `RoseBorder` para o contorno das caixas de estatística) medidos à vista, não ao pixel.
- **Botão de pesquisa:** vai para o `catalog` — **desde a fatia 2b (2026-09-23) já é o ecrã a sério**, não o placeholder (ver
  "15. Catálogo / Filtros", abaixo). **"+" da cave** continua a ir para o `cave` placeholder do esqueleto (a fatia da cave ainda
  não foi construída). **"VER PRODUTOR"** continua sem destino (`{}`, não faz nada) — a página de um produtor é um ecrã que ainda
  não existe.

## 15. Catálogo / Filtros (fatia 2b, feita e validada ao vivo — 2026-09-23)

Dois mockups enviados a 2026-09-23 (`catalogo/01-filtros-popup.png`, 231×622, e `catalogo/02-catalogo-resultados.png`,
214×529 — outra vez muito pequenos, medidas aproximadas). O utilizador pediu-os assim, no chat: o popup de filtros
"é acedido ao clicar no ícone de filtro (substituir a lupa por filtro) ou ter um ícone ao lado no outro na barra de pesquisa";
os resultados aparecem "pela barra de pesquisa da homepage ou pelo ícone do catálogo na barra inferior". O próprio mockup do
popup trazia uma nota do utilizador por baixo, com o mapeamento dos campos: "categoria de bebida_categoria · origem de país e
produtor_região · preço de bebida_link_compra · o bloco de baixo troca de campos com a categoria escolhida (vinho_*, whisky_*,
gin_*...)" — usada tal e qual para decidir os filtros.

**Descoberta ao planear:** o backend já suportava **praticamente todos** os filtros do popup desde a fatia 4 (`GET /api/bebidas`
já aceitava `categoriaIds`, `paisId`, `regiaoIds`, `precoMin/Max`, `ratingMin`, `acidezMin/Max`, `docuraMin/Max`, `corpoId`,
`taninoId`, `tipoId`, `castaIds`) e todos os lookups precisos já existiam (`/lookup/paises`, `/lookup/regioes`,
`/lookup/vinho/corpos`, `/lookup/vinho/taninos`, `/lookup/vinho/tipos`) — só não estavam ligados ao Android. Só foi preciso
**um acrescento pequeno e seguro ao backend**: `tipo` e `tanino` não vinham no `BebidaSummaryDto` (só no detalhe), e
`produtorRegiao` (a região do produtor) também não — os três já existiam nas entidades, só não estavam expostos no resumo. Ver
`backend/API_ENDPOINTS.md`, "`BebidaSummaryDto`", 2026-09-23.

**O que foi pedido e o que ficou feito:**
- **Popup "Filtros" (`FiltrosSheet.kt`):** categoria (escolha única, ao contrário da homepage), origem (país num
  "dropdown" + pílulas de região do país escolhido, só as que têm bebidas), preço (`RangeSlider` do Material3, 0€–150€+),
  rating mínimo (Todos/3+/4+/4,5+), e, só com "Vinho" escolhido, os atributos do vinho: acidez e doçura (intervalo 1 a 5, ver
  `RangePillRow` abaixo), tipo e corpo (pílulas de escolha única) e castas (pesquisa por texto + pílulas escolhidas
  removíveis + sugestões). O botão "Ver N bebidas" mostra a **contagem ao vivo** do que os filtros em edição dariam (pedido
  à API com `size=1`, só para ler `totalElements`, com 350ms de atraso para não disparar 1 pedido por toque) — só aplica à
  lista de baixo quando se toca nele; fechar de outra forma descarta as alterações. "LIMPAR N" volta a um filtro só com a
  categoria. **Feito e validado ao vivo.**
- **Ecrã "Catálogo" (`CatalogScreen.kt`, substituiu o placeholder do esqueleto):** título serifado, barra de pesquisa com o
  ícone de filtro (funil, `Icons.Filled.FilterList` do Material Icons Extended — já uma dependência do projeto, não precisei
  de pedir um ícone novo), pílulas de categoria (sempre visíveis, trocam a categoria ativa na hora, sem passar pelo popup),
  contagem + "ORDENAR" (menu com "Melhor avaliadas"/"Nome (A-Z)" — ver "Diferenças conscientes"), pílulas removíveis dos
  filtros ativos (região, país, preço, rating, corpo, tipo, castas — tocar no "×" remove só esse filtro e atualiza a lista),
  lista de cartões (o mesmo `BebidaCard` da homepage, ver abaixo) e "Carregar mais N bebidas" (paginação, 20 de cada vez).
  **Feito e validado ao vivo:** filtrar por região "Douro" + rating "4+" (contagem ao vivo "Ver 1 bebidas" no popup, depois
  aplicado); remover a pílula "Douro ×" e voltar às 9 bebidas; trocar de categoria para "Whisky" (0 bebidas, "Sem bebidas com
  estes filtros.", sem rebentar). **Não validado ao vivo:** "Carregar mais" (o catálogo de teste só tem 9-16 bebidas por
  categoria, sempre cabe numa página) e o `RangeSlider` do preço (só visto no popup, nunca aplicado — a BD de teste não tem
  preços variados que valham a pena filtrar).
- **`BebidaCard` tornou-se partilhado** (`ui/components/BebidaCard.kt`, antes vivia só dentro do `HomeScreen.kt`): a
  homepage e o catálogo mostram bebidas exatamente da mesma forma. Trouxe consigo `BebidaSummary.linhaAtributos()`
  (`data/model/BebidaFormatacao.kt`, 8 testes) — a linha "VINHO TINTO • CORPO ENCORPADO • TANINO ELEVADO" do cartão, com uma
  fórmula fixa (até 2 atributos, por prioridade: corpo, depois tanino-se-tinto-senão-acidez, depois doçura) em vez da
  simplificação "categoria + corpo" da fatia 2a — **resolve** a diferença consciente que tinha ficado por fechar na homepage.
- **`RangePillRow` (novo, `ui/components/`):** o seletor 1–5 da acidez/doçura do popup — tocar num nível escolhe-o sozinho;
  tocar noutro estica o intervalo até lá (como um seletor de datas); tocar dentro do intervalo já escolhido volta a fechá-lo
  nesse nível. Não arrasta (só toques), simplificação consciente para a 1.ª versão.
- **Testes:** **53** unitários (45 + 8 do `BebidaFormatacaoTest`, cobrindo a fórmula da linha de atributos). Compilou
  (`assembleDebug`) e correu ao vivo sem *crashes* (`adb logcat -b crash` só tem o "uwb-service" do emulador, já conhecido).

**Defeito apanhado e corrigido a testar ao vivo:** o `BebidaCard` tem altura fixa (108dp); com `tipo`/`tanino` a linha de
atributos passou a ter 3 segmentos e, nalgumas bebidas, quebrava para 2 linhas — empurrava o preço para fora do cartão, a
sobrepor-se ao cartão seguinte (sem *crash*, só visual, só visto na screenshot). Corrigido com `maxLines = 1` +
`TextOverflow.Ellipsis` na linha de atributos, como as outras linhas do cartão já faziam.

### Diferenças conscientes face ao mockup (fatia 2b, catálogo/filtros)

Diz-me se preferes o original em qualquer uma — nenhuma delas está fechada.

- **"ORDENAR" só tem 2 opções** ("Melhor avaliadas", "Nome (A-Z)") — o mockup mostra o menu fechado, só com "Melhor
  avaliadas" escolhido, sem revelar as outras opções. Um "ordenar por preço" ficou de fora de propósito: `precoDesde` não é
  uma coluna da bebida (vem calculado dos links de compra), por isso não é ordenável pelo mecanismo de `Pageable`/`Sort` do
  Spring sem trabalho extra no backend — não construí isso sem o utilizador pedir.
- **Tipo de vinho:** o popup mostra 4 pílulas (Branco, Tinto, Rosé, Fortificado) — a API tem 7 tipos (`Tinto`, `Branco`,
  `Rosé`, `Espumante`, `Frisante`, `Generoso`, `Sobremesa`; "Generoso" é o termo daqui usado para vinho fortificado, tipo
  Porto). Mostrei os 7, todos os que a `/lookup/vinho/tipos` devolve, em vez de escolher 4 à mão — o padrão já usado nas
  outras pílulas do popup (mostrar sempre o que a API tem, nunca uma lista fixa no código).
- **Castas e Tipo/Corpo não quebram linha:** o mockup não deixa claro se as pílulas quebram para a linha de baixo num
  espaço estreito; usei scroll horizontal (`LazyRow`), como todas as outras pílulas da app (categoria, região, ...), em vez
  de trazer o `FlowRow` do Compose só para isto.
- **"Origem" (país):** o campo mostra um fundo rosado sempre (não só quando aberto) para se distinguir de um botão — no
  mockup parece um campo de formulário normal (fundo bege claro).
- **Preço nunca testado com dados a sério:** o range 0€–150€+ é uma aproximação (o catálogo de teste só tem uma dúzia de
  preços, entre 15€ e 180€) — o "150€+" pode não ser o teto certo para o catálogo real.

## 16. Cave e popup de detalhe da bebida (fatia 3, em curso — começada em 2026-09-23)

5 mockups enviados de uma vez a 2026-09-23 (`caves/01` a `05`), com notas do utilizador no chat e escritas dentro dos
próprios mockups (mapeamento de campos a tabelas — ver os footers de `03` e `04`). Cobre duas coisas distintas: a Cave
(`01` lista, `02` popup "Nova cave", `04` popup "Adicionar à cave") e o **popup de detalhe de uma bebida** (`03` tab
"Detalhes", `05` tab "Reviews") — este último é partilhado por toda a app: "abre-se ao pressionar num item duma bebida
no catálogo (após pesquisa, antes de pesquisa, nas caves, nos favoritos, etc.)". **Só o popup de detalhe (03 e 05) está
feito nesta 1.ª parte** — a lista de caves e os dois popups de cave (02, 04) ficam para a parte seguinte (a
infraestrutura já existe: `CaveRepository.createCave`/`addBebidaToCave` fazem exatamente o que o popup "Adicionar à
cave" pede, desde a fatia 2a).

**O que foi pedido e o que ficou feito (popup de detalhe da bebida):**
- **Interação:** o mockup só define o toque premido ("ao pressionar"); sem outro destino definido para o toque curto,
  fiz os dois abrirem o mesmo popup (`BebidaCard.combinedClickable`, `ExperimentalFoundationApi`) — documentado como
  diferença consciente. **Substitui** o antigo `DetailScreen`/`ReviewsScreen` (rotas `detail/{id}`/`reviews/{id}`, que
  ficam no código mas deixam de ser alcançadas por toque; por limpar numa próxima fatia).
- **Cabeçalho:** imagem, "categoria + tipo • ano", nome, produtor • região, rating + total de reviews. **Feito.**
- **Linha de ações:** preço + retalhista (se houver `linkCompra`; sem ele, os 3 botões vão só para a direita), favorito
  (coração), wishlist (marcador), "+" adicionar à cave (chama `onAdicionarACave`, por agora sem destino — ver acima).
  Favorito/wishlist são **otimistas** (mudam a cor logo ao tocar, revertem se o pedido falhar) e já ligados a
  `POST/DELETE /bebidas/{id}/favorito` e `/wishlist`. **Feito e validado ao vivo.**
- **Tab "Detalhes":** teor alcoólico, volume, país, ano (grelha 2×2); perfil sensorial (barras de acidez/doçura, só
  vinho); pílulas de corpo/tanino/tipo; castas com percentagem; cartão do produtor (nome, região, ano de fundação,
  "recebe visitas"). **Feito e validado ao vivo.**
- **Tab "Reviews":** a tua review (5 estrelas por toque — só inteiras, sem meias-estrelas do mockup, simplificação),
  comentário (até 4000), "Publicar"; distribuição por estrela (gráfico de barras) com a média **precisa** (a mesma do
  cabeçalho, `bebida.ratingMedio` — não recalculada a partir da distribuição, que vem arredondada por estrela e perderia
  a casa decimal); lista "Da comunidade" com avatar (`AvatarBadge` reaproveitado, iniciais a partir do "nome apelido"),
  nome, data relativa ("há N dias", "em mês"), rating, comentário. **Feito e validado ao vivo**, incluindo publicar uma
  review nova de propósito (ver abaixo).
- **Marcar como provada antes de avaliar:** a API só aceita uma review de uma bebida já `provada` (409 caso contrário,
  ver `backend/API_ENDPOINTS.md`) — o mockup não mostra esse passo, por isso o popup marca-a sozinho
  (`POST /bebidas/{id}/provada`, idempotente) mesmo antes de `POST .../reviews`, sem o utilizador ver nada disso.
- **Sincronização da API que isto obrigou:** `getReviews` mudou de `List<ReviewResponse>` para
  `ReviewsResponse { distribuicao, reviews }` (o backend já devolvia assim desde a fatia de reviews — só a app estava
  desatualizada); `ReviewResponse` ganhou `utilizadorNome`/`utilizadorAvatar`; novos `addProvada`/`removeProvada`
  (`ProvadaRepository`, novo). Ver `backend/API_ENDPOINTS.md`.
- **Sem argumento de navegação:** ao contrário dos ecrãs por rota, o popup não tem `SavedStateHandle` — usa
  `hiltViewModel(key = "bebida-detalhe-$bebidaId")` (uma chave por bebida em vez de injeção assistida do Hilt) e um
  `carregar(bebidaId)` chamado por `LaunchedEffect`, em vez do `init {}` habitual. Ver `CLAUDE.md` para o porquê.
- **Testes:** sem testes novos (a lógica do ViewModel — otimismo do toggle, o pré-requisito da provada — pediria
  `kotlinx-coroutines-test`, ainda não configurado no projeto; fica com a dívida já registada no `android/README.md`).

**Defeitos apanhados e corrigidos a construir/testar ao vivo:**
- **`data class Ready` sem `: BebidaDetalheUiState`** — faltava o supertipo depois dos parênteses do construtor (só
  `Loading`/`Error` o tinham); o Kotlin não avisa que uma sealed interface "perdeu" uma subclasse, só dá erros de tipo
  confusos ("Incompatible types: X e X") em sítios que não têm nada a ver com a causa. Ver `CLAUDE.md`.
- **`Modifier.weight()` fora de `ColumnScope`/`RowScope`:** uma função `@Composable` chamada de dentro de um `Column`
  não herda automaticamente o `ColumnScope` — só o tem se for declarada como `fun ColumnScope.Minha Função(...)`.
  "Unresolved reference: weight" apontava para a linha certa, mas a causa (falta do `ColumnScope` na assinatura) não era
  óbvia à primeira vista.
- **Pílulas de atributos sem `LazyRow`:** um `Row` normal, sem scroll nem quebra de linha, espreme os itens a mais
  quando não cabem (o texto de "VINHO TINTO" partiu-se em "VI"/"N") — o mesmo tipo de bug já apanhado no
  `FiltrosSheet`, desta vez esquecido aqui; corrigido para `LazyRow`.
- **Média da distribuição vs. `ratingMedio`:** a distribuição por estrela vem arredondada (uma review de 4,5 cai no
  balde "5"); calcular a média a partir dela dava "5,0" em vez de "4,5" — corrigido para mostrar sempre
  `bebida.ratingMedio` (a mesma fonte do cabeçalho).
- **Cabeçalho por atualizar depois de publicar:** `carregarReviews()` só volta a pedir a lista/distribuição, não a
  bebida — o cabeçalho ("4,5/5 • 1 reviews") ficava com os números de antes de publicar. Corrigido com
  `atualizarRatingDaBebida()`, um pedido leve só à bebida (não `carregar()` inteiro, que reporia a tab e as marcações
  otimistas de favorito/wishlist).

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): popup aberto por toque premido a
partir da homepage, nas duas tabs; alternar favorito e wishlist (cor muda logo, persiste ao reabrir); publicar uma
review nova numa bebida sem nenhuma (`Vinha Grande Tinto 2019`, agora com 1) — a distribuição, o total e o cabeçalho
todos a refletir o valor novo; confirmado por API (`GET /api/bebidas/2`) que `ratingMedio`/`totalReviews` batem certo.

**Em aberto / por fazer, na altura:** a lista de caves, "Nova cave" e "Adicionar à cave" (mockups 01, 02, 04) — **feitas na 2.ª
parte da sessão, ver abaixo**; as meias-estrelas do input da review (só inteiras por agora); os antigos
`DetailScreen`/`ReviewsScreen`/rotas ficam como código morto até se limparem.

### Cave: lista, "Nova cave" e "Adicionar à cave" (fatia 3b, feita e validada ao vivo — 2026-09-23)

Continuação direta do mesmo pedido (os 5 mockups de "16."), com um pedido extra do utilizador: no popup "Adicionar à cave",
**destacar as caves onde aquela bebida já está** — distinto de qual cave se escolhe para este "adicionar" (pode-se juntar mais
garrafas a uma cave onde já há).

**Backend:** um acrescento pequeno — `GET /api/users/me/caves` ganhou `?bebidaId=` (opcional), que acrescenta `temBebida: Boolean`
a cada `CaveResponse` (reaproveita a mesma query em lote já feita para os totais, não pede mais nada à BD). O
`POST .../consumir` já existia desde a fatia 4 do backend, só não estava ligado ao Android.

**Android:**
- **`feature/cave/CaveScreen.kt`** (reescrito, substitui o placeholder do esqueleto): pílulas de cave + "+" (popup "Nova
  cave"), 3 estatísticas (garrafas, investido, a abrir já — da cave escolhida), "Prontas a abrir" com botão "Consumir"
  (`POST .../consumir`, recarrega a cave depois), "Em guarda" com "ORDENAR" (preço/janela de consumo, `?sort=` já existia na
  API). "Ver as N garrafas" é só informativo por agora (a lista já mostra tudo, sem paginação — não há ecrã à parte).
- **`NovaCaveSheet.kt`** (novo): nome (100) + descrição opcional (500), com contadores; "JÁ LÁ DENTRO" é sempre "Ainda sem
  garrafas" (é uma cave nova). Reutilizado tal e qual dentro do `AdicionarACaveSheet` (o "+ nova cave" das pílulas de cave).
- **`AdicionarACaveSheet.kt`** (novo, aberto a partir do "+" do popup de detalhe da bebida): pílulas de cave com **duas
  marcas independentes** — cheia a grená se escolhida para este "adicionar", com um ✓ se a bebida já lá está
  (`CaveResponse.temBebida`); quantidade (stepper), preço pago, data de aquisição (`DatePicker` do Material3), janela de
  consumo (dois campos de ano, convertidos para `aaaa-01-01`/`aaaa-12-31` ao guardar), notas; "Guardar na cave" chama
  `CaveRepository.addBebidaToCave` (já existia desde a fatia 2a, sem mudanças).
- **Os dois popups empilham-se:** "Adicionar à cave" abre por cima do popup de detalhe da bebida (que fica aberto por
  baixo) — mais simples do que fechar um para abrir o outro, e o utilizador vê o resultado ao fechar só o de cima.
- **Testes:** sem testes novos (mesma dívida já registada — `kotlinx-coroutines-test`). Compilou e correu ao vivo sem
  *crashes*.

**Defeito apanhado e corrigido a testar ao vivo (condição de corrida):** criar uma cave nova, no `CaveViewModel`, fazia
`carregar()` (assíncrono) e depois `selecionarCave(nova.id)` — a resposta do `carregar()` podia chegar **depois** e repor a
seleção para a 1.ª cave da lista, perdendo a seleção da cave acabada de criar. A cave ficava criada mas não selecionada, sem
erro nenhum. Corrigido fazendo tudo na mesma corrotina, pela ordem certa (buscar a lista nova → só depois escolher a cave
nova). O `AdicionarACaveViewModel` já não tinha este problema (só uma chamada assíncrona a fazer as duas coisas de uma vez).

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): "Consumir" numa garrafa "pronta a abrir"
(quantidade e estatísticas a recalcular); criar uma cave nova a partir do "+" da Cave (ficou logo selecionada, depois do
conserto); abrir "Adicionar à cave" a partir do popup de detalhe da "Barca Velha 2015" — "Adega Principal" aparece com ✓
(já lá está) e selecionada, "Tintos de guarda" sem ✓; escolher "Tintos de guarda", preencher preço e janela de consumo
(2030–2035), guardar — confirmado por API (`GET /api/caves/22`: `janelaInicio: "2030-01-01"`, `janelaFim: "2035-12-31"`,
`precoPago: 34.5`); criar uma 2.ª cave nova a partir do popup "Adicionar à cave" (ficou logo selecionada).

**Em aberto / por fazer:** "Ver as N garrafas" sem destino próprio (a lista já mostra tudo); mover uma garrafa entre caves
(não pedido nos mockups); editar uma garrafa já na cave (o `PATCH` já existe na API, sem UI); os antigos
`DetailScreen`/`ReviewsScreen`/rotas continuam como código morto, por limpar. **Com isto, as fatias 1 a 3 cobrem o fluxo
principal do MVP** (login → homepage → catálogo → detalhe da bebida → cave) — falta produtor, perfil/listas e afinar o resto
(ver `PLANO.md`).

### Ajustes de feedback + "Já provadas" (fatia 3c, feita e validada ao vivo — 2026-09-23)

O utilizador testou a fatia 3 completa e devolveu uma lista de 5 ajustes, com um novo mockup (imagem 17, "Bebidas já
provadas") e 3 imagens de contexto (Favoritos/Wishlist/Caves, enviadas só para situar — **não pedidas para construir agora**).
**A imagem 17 não foi copiada para `design/caves/`** (chegou já depois de um resumo de contexto desta sessão, sem ficheiro em
disco para copiar) — fica por arquivar se o utilizador a reenviar numa próxima sessão.

**1. Popup de confirmação ao duplicar numa cave:** pedido porque o "✓" de "já tens esta bebida aqui" (fatia 3b) não impedia
adicionar de qualquer forma — faltava um passo de confirmação. `AdicionarACaveViewModel.guardar()` verifica
`caveSelecionadaJaTemBebida` (calculado de `CaveResponse.temBebida` da cave escolhida) e, se verdadeiro, mostra um
`AlertDialog` ("Já tens esta bebida aqui" / "A cave 'X' já tem garrafas desta bebida. Queres mesmo adicionar mais?") antes de
gravar; "Adicionar na mesma" segue com a gravação original, "Cancelar" só fecha o popup. **Feito e validado ao vivo.**

**2. Review a exigir "provada" (em vez de a marcar sozinha):** reversão deliberada da fatia 3a — antes, publicar uma review
marcava "provada" sozinho (`POST .../provada` antes de `POST .../reviews`), sem o utilizador ver esse passo. O pedido agora é
o oposto: **uma bebida só pode ter review de um utilizador quando ele já a tem na lista de consumidos** — por um dos dois
caminhos abaixo, nunca automaticamente ao avaliar. `BebidaDetalheViewModel.publicarReview()` deixou de chamar `addProvada`;
em vez disso, `MinhaReviewForm` (`BebidaDetalheSheet.kt`) verifica `bebida.isProvada` e, se `false`, mostra uma caixa
("AINDA NÃO PROVASTE ESTA BEBIDA — Só se pode avaliar uma bebida já marcada como consumida. Marca-a com 'Consumir' numa cave,
ou adiciona-a diretamente à lista de já provadas...") em vez do formulário de estrelas/comentário. **Feito e validado ao
vivo** (bloqueio confirmado no "Esporão Reserva Tinto 2018", `isProvada: false` para `demo_user2`).

**3. "Consumir" passa também a marcar "provada":** o 2.º caminho para a lista de consumidos. `CaveViewModel.consumir()`
chama `provadaRepository.addProvada(bebidaId)` a seguir a `POST .../consumir` (idempotente no backend, por isso não faz mal
mesmo que já estivesse provada). **Feito e validado ao vivo** (consumir "Barca Velha 2015" não alterou a "Já provadas" porque
já lá estava de uma review anterior — confirma a idempotência).

**4. Resumo "Já provadas" na Cave (pergunta do mockup, imagem 5 da fatia 3, respondida agora):** um `snippet` de até 5
bebidas por baixo de "Em guarda", com "ABRIR MAIS" a navegar para o ecrã inteiro. `CaveViewModel.carregar()` chama
`GET /users/me/provadas` uma vez (não depende da cave escolhida — é do utilizador, não da cave) e guarda `take(5)` em
`CaveUiState.Ready.provadasRecentes`; `ProvadaResumoRow` mostra nome, produtor e nota (`—` se sem nota). **Feito e validado
ao vivo** (mostrou corretamente "Vinha Grande Tinto 2019 · 4,0" e "Barca Velha 2015 · 4,5").

**5. Ecrã "Bebidas já provadas" completo** (`feature/provadas/`, mockup imagem 17): pílulas de filtro por nota (Todas/Com
nota/Sem nota) e por categoria (mutuamente exclusivas — escolher uma reset a outra, não há UI para as combinar no mockup);
agrupamento por mês (mais recente primeiro), com "Carregar mais antigas" a mostrar mais 2 meses de cada vez
(`mesesVisiveis`); estatística "N bebidas • M com nota tua". **Barra de pesquisa acrescentada, fora do mockup** (pedido
explícito do utilizador: "falta no mockup a barra de pesquisa alternativa caso o user queira adicionar uma por conta própria
que não estivesse na cave") — pesquisa com *debounce* de 350 ms (`BebidaRepository.searchBebidas`), cada resultado com um
"+" que chama `POST /bebidas/{id}/provada` diretamente (sem passar por nenhuma cave) e recarrega a lista. **Feito e validado
ao vivo**, incluindo o caminho da busca (adicionado "Gin 44°" por esta via, depois removido — dado de teste, não fica na BD).

**Resposta à pergunta do utilizador — "como surgem as garrafas 'Em guarda'":** é o `EstadoCaveBebida` calculado no backend
(`CaveRegras.kt`, sem mudanças nesta fatia) a partir da janela de consumo de cada garrafa: **sem `consumida`**, e **sem
`janelaInicio` nem `janelaFim`** → `EM_GUARDA` (não há informação para dizer que já está pronta); com `janelaInicio` no
futuro → também `EM_GUARDA` (ainda não chegou a data de começar a beber); dentro da janela → `PRONTA` (aparece em "Prontas a
abrir"); depois de `janelaFim` sem consumir → `EM_ATRASO` (também aparece em "Prontas a abrir", junto com `PRONTA` — ver
`onConsumirEhUtil` em `CaveScreen.kt`). Ou seja: uma garrafa cai em "Em guarda" sempre que não se indicou nenhuma janela de
consumo, ou a janela ainda não começou — o `AdicionarACaveSheet` deixa "Janela de consumo" opcional exatamente por isto.

**Defeito apanhado e corrigido a testar ao vivo:** as linhas de garrafa da Cave (`GarrafaRow`, tanto "Prontas a abrir" como
"Em guarda") e o novo `ProvadaResumoRow` **não tinham nenhum toque/premido ligado** — descoberto ao tentar testar o bloqueio
da review a partir da Cave (o mockup original da fatia 3 já dizia "nas caves" como um dos sítios onde o popup de detalhe
abre, mas essa ligação nunca tinha sido feita). Corrigido com o mesmo padrão do `BebidaCard`
(`combinedClickable(onClick, onLongClick)`, `ExperimentalFoundationApi`), passando `onBebidaClick: (Long) -> Unit` de
`CaveScreen` através de `CaveContent` até às duas linhas. **Lição:** um ecrã novo que mostra bebidas não herda
automaticamente a abertura do popup de outro ecrã — tem de se ligar explicitamente linha a linha.

**Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`, já registada). `assembleDebug testDebugUnitTest`
continua a passar (53 testes, sem alterações à suite).

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): as 5 verificações da lista acima, todas
com capturas de ecrã confirmando o comportamento esperado — ver o resumo de cada ponto.

**Em aberto / por fazer:** os antigos `DetailScreen`/`ReviewsScreen`/rotas continuam como código morto; Favoritos/Wishlist
(imagens 14/15, enviadas como contexto) continuam por construir — não pedidos nesta fatia (feitos a seguir, ver
"Favoritos e Wishlist"). A imagem 17 ("Bebidas já provadas") acabou por se copiar para `design/caves/06-ja-provadas-popup.png`
numa sessão seguinte (chegou por ficheiro entretanto).

### Favoritos e Wishlist (feita e validada ao vivo, 2026-09-24)

Prints em `android/design/favoritos-wishlist/01-wishlist.png` e `02-favoritos.png`, com anotações do próprio utilizador
por cima da imagem (não é o Figma original — um mockup feito à mão a partir da app, com notas técnicas sobre a origem de
cada dado). Substituem os placeholders do esqueleto que só liam mal o JSON (ver correção pós-3c).

**O que o mockup pede e o que ficou feito:**
- **Wishlist:** cabeçalho (wordmark + avatar, como a homepage), pílulas de ordenação "Recentes"/"Preço"/"Rating"
  (ordenadas no telemóvel — a lista já vem inteira, sem paginação), cartão por bebida (imagem, nome, produtor • região,
  atributos, preço mais barato, "ADICIONADA há N dias"/"em \<mês\>", marcador para remover, botões "Comprar em X" e
  "Para a cave"). **Feito**, exceto a nota "MENOR DE N RETALHISTAS"/"ATUALIZADA HOJE" e a deteção de descidas de preço
  ("3 baixaram de preço") — a própria anotação do utilizador no mockup diz para adiar isto ("A DETEÇÃO DE BAIXAR DE
  PREÇO É REMOVIDA PARA JÁ"); as outras duas pedem campos que a API ainda não devolve (ver "Por fazer depois" abaixo) —
  por agora "Comprar em X" abre o popup de detalhe da bebida em vez do link do retalhista.
- **Favoritos:** cabeçalho igual, estatísticas ("N favoritos • N provadas • N com nota tua" — as duas primeiras vêm de
  fontes diferentes, a 2.ª de `GET /users/me`), pílulas de categoria (só as presentes nos favoritos, não o lookup
  inteiro), cartão com nota própria vs. média da comunidade, e "Ver a tua review"/"Avaliar" conforme haja review
  publicada. **Feito**, exceto "PROVADA EM \<mês\>" e "N NA CAVE" (pedem campos novos, adiados — ver abaixo); sem essa
  informação, o cartão de uma bebida provada mostra só o botão "Ver a tua review", sem a pílula do mês.
- **"Ver as N garrafas favoritas":** só 3 cartões por omissão, com este link a mostrar os restantes (a API não pagina —
  é um `take(3)`/mostrar tudo no telemóvel, decisão combinada com o utilizador).
- **Remover um favorito/wishlist:** toca-se no coração/marcador do próprio cartão (otimista — sai da lista de imediato,
  volta se o pedido falhar), sem confirmação (o mesmo padrão do popup de detalhe).
- **"Para a cave":** ao contrário do popup de detalhe (que já tem o `BebidaDetail` carregado), o cartão da lista só tem
  `BebidaSummary` — o botão busca `GET /bebidas/{id}` sozinho antes de abrir o `AdicionarACaveSheet`, com um pequeno
  estado de carregamento ("A abrir…") no próprio botão.

**Decisão combinada com o utilizador (dados em falta):** em vez de alargar a API agora, ficam por fazer 5 campos que o
mockup pede e o `BebidaSummaryDto` não tem — o link de compra completo (`id`+`url`, para "Comprar" abrir o browser e
registar o clique em `POST .../links-compra/{id}/clique`, que já existe), a data de verificação do preço mais barato
("ATUALIZADA HOJE"), quantas ofertas ativas tem a bebida ("MENOR DE N RETALHISTAS"), quantas garrafas dela há numa cave
do utilizador ("N NA CAVE") e a data em que foi marcada como provada ("PROVADA EM \<mês\>"). Fica registado em
`PLANO.md`, "Por fazer depois", para uma sessão futura — a app já está pronta para os receber sem outra alteração de UI
(os campos correspondentes é que ainda faltam nos modelos/ecrãs).

**Testes:** sem testes novos. `assembleDebug testDebugUnitTest` continua a passar (53).

**Validado ao vivo** (emulador `Pixel_8`, conta pessoal do utilizador): filtro por categoria nos favoritos; "Ver a tua
review" a abrir o popup de detalhe da bebida certa; "Para a cave" na wishlist a buscar o detalhe e abrir
`AdicionarACaveSheet` diretamente (sem passar pelo popup); remover um item da wishlist pelo marcador (otimista,
confirmado a reaparecer ao voltar a marcá-lo).

**Em aberto / por fazer:** os 5 campos da API listados acima; os antigos `DetailScreen`/`ReviewsScreen`/rotas continuam
como código morto.

## Perfil (fatia 5, feita e validada ao vivo — 2026-09-24)

Prints em `android/design/perfil/01-perfil.png` a `04-editar-preferencias.png` (mockups feitos pelo utilizador a partir
da própria app, com notas escritas no chat, não no Figma original). Alcança-se tocando no avatar, repetido nos
cabeçalhos da homepage, wishlist e favoritos — nenhum tinha destino até agora.

**O que o mockup pede e o que ficou feito:**
- **Imagem 1 (Detalhes do perfil):** avatar + nome + "@username" + "nacionalidade · membro desde \<mês\> de \<ano\>",
  bio, 6 estatísticas (provadas/reviews/favoritos/wishlist/caves/garrafas — todas já vinham em `GET /users/me`), cartão
  "As minhas preferências" com resumo (categorias, acidez/doçura, castas) e "EDITAR", nav rows ("Histórico de
  provadas" → `ProvadasScreen`, fatia 3c; "As minhas reviews" e "Conta e segurança" → sem destino, mockups por chegar;
  "As minhas caves" → `CaveScreen`, fatia 3b) e "Terminar sessão". **Feito.**
- **Imagem 2 (Editar perfil):** "Cancelar"/"EDITAR PERFIL"/"Guardar" no topo, "Mudar avatar", nome/apelido, username
  (com contador — sem verificação de disponibilidade ao vivo, a API não tem esse endpoint; só valida ao gravar, com
  409 se já estiver em uso), nacionalidade (dropdown com bandeira, igual ao do onboarding), descrição/bio (com
  contador). **Feito, com uma diferença combinada com o utilizador:** por agora mostra-se o email como texto simples
  por baixo da bio, em vez da linha "Email e password" do mockup (essa linha pede o ecrã "Conta e segurança", ainda
  sem mockup). **Suporte de emojis na bio:** pedido para aplicar-se aqui e no onboarding — por código, nenhum dos dois
  campos restringe o tipo de teclado, e o Android troca sozinho para a fonte de emoji do sistema quando a Inter não
  tem o glifo (comportamento por omissão da plataforma, não pede código à parte). **Não validado ao vivo**: o
  `adb shell input text` não consegue enviar emoji (falha com `NullPointerException` mesmo para símbolos do plano
  básico, um limite da própria ferramenta) — falta o utilizador confirmar no teclado a sério.
- **Imagem 3 (Escolher avatar):** pílulas "Todos"/categorias + grelha 3×3, "SELECIONADO: \<nome\>" e "Usar este
  avatar" — só confirma localmente, quem grava tudo é o "Guardar" da imagem 2. **Feito**, reaproveitando a grelha do
  onboarding (`AvatarGridPicker` extraído para `ui/components/`, partilhado pelos dois — a pílula "Todos" é nova, só
  aqui).
- **Imagem 4 (Editar preferências):** as mesmas perguntas do onboarding (tipos de bebida, acidez/doçura, castas), num
  popup só com pílulas horizontais em vez das listas paginadas do onboarding — `RangePillRow` (já existia para o
  popup de filtros do catálogo) reaproveitado tal e qual para acidez/doçura; a pesquisa de castas espelha a do popup
  de filtros (chips removíveis + sugeridas). **Feito.** Guardar aplica-se de imediato em "Escolhido para ti" na
  homepage (mecanismo já existente, nada de novo aí).
- **"Terminar sessão":** chama `AuthRepository.logout()` (já existia) e limpa toda a pilha de navegação até ao login
  (`popUpTo(0)`). Sem popup de confirmação (o mockup não pede um, e é uma ação de baixo risco — voltar a entrar).

**Nacionalidade sem id na resposta:** `GET /users/me` só devolve o nome da nacionalidade (`nationality`), não o id —
resolve-se por nome na lista de `GET /lookup/nacionalidades` (uma lista curada, sem nomes repetidos), sem pedir
mudança nenhuma ao backend.

**Um gotcha já conhecido, repetido aqui:** os `Conteudo` dos dois popups novos (`EditarPreferenciasSheet`,
`EscolherAvatarSheet`) precisaram de `ColumnScope.Conteudo(...)` para o `Modifier.weight()` funcionar — o mesmo
"Unresolved reference: weight" já apanhado no popup de detalhe da bebida (fatia 3a), ver `CLAUDE.md`.

**Testes:** sem testes novos (mesma dívida do `kotlinx-coroutines-test`). `assembleDebug testDebugUnitTest` continua a
passar (53).

**Validado ao vivo** (emulador `Pixel_8`, conta `demo2@aquavitae.local`): ecrã de perfil com as 6 estatísticas e o
cartão de preferências corretos; "EDITAR PERFIL" com todos os campos pré-preenchidos (incluindo a nacionalidade
resolvida por nome); "Mudar avatar" a escolher "Caneca" e a pré-visualização a atualizar-se de imediato; "Guardar" a
persistir nome/avatar/bio e a voltar ao perfil já atualizado; "EDITAR" preferências a escolher Vinho + acidez 2 a 3 +
doçura 4 a 4 + casta "Alvarinho" (pesquisada e confirmada) e "Guardar preferências" a refletir tudo no cartão de
imediato; "Histórico de provadas" a abrir a `ProvadasScreen` certa; "Terminar sessão" a voltar ao login com a pilha
limpa.

**Em aberto / por fazer:** "As minhas reviews" e "Conta e segurança" sem destino (mockups por chegar); suporte de
emojis por confirmar no teclado a sério; a página do produtor ficou feita na fatia 6 (secção seguinte).

## Produtor (fatia 6, feita e validada ao vivo — 2026-09-24)

Print em `android/design/produtor/01-produtor.png` ("Detalhes do produtor", mockup feito pelo utilizador; as notas por
ecrã vieram no chat). Alcança-se pelo **cartão do produtor em destaque** da homepage (o cartão todo, não só o botão
"VER PRODUTOR") e pelo **cartão do produtor no fim da tab "Detalhes" do popup de uma bebida** (ganhou uma linha "VER
PRODUTOR →" e o cartão inteiro é tocável) — a partir de qualquer ecrã que abra esse popup (home, catálogo, cave,
favoritos, wishlist). **Não** aparece no popup aberto de dentro das páginas do próprio produtor (já estamos lá).

**O que o mockup pede e o que ficou feito:**
- **Cabeçalho:** imagem do produtor (`imagePath`; sem imagem, o degradê cor-de-rosa com "Imagem do produtor", como no
  mockup), botão de voltar redondo (fica fixo por cima do scroll), "PRODUTOR", nome (serifa grande), "Região, País ·
  fundada em AAAA". **Feito.**
- **Pílulas:** "RECEBE VISITAS" (só se `permiteVisitas`), o site (só o domínio em maiúsculas — `ferreirinha.pt` —, toca
  e abre o browser; só se há `website`) e "N GARRAFAS" (`totalProdutos`, singular com 1). Quebram linha em ecrãs
  estreitos (`FlowRow`). **Feito.**
- **Rating geral (sugestão do utilizador, não estava no mockup):** "3,4 /5 · 4 REVIEWS · RATING GERAL" por baixo da
  localização; sem reviews mostra "Sem reviews ainda" (nunca um 0,0). Calculado no backend — média das reviews de
  todas as bebidas do produtor, **ponderada pelo nº de reviews de cada uma** (ver `backend/API_ENDPOINTS.md`, "Rating
  geral do produtor"). **Feito.** Posição escolhida por mim (o mockup não tinha sítio): logo abaixo da localização, com
  o mesmo estilo do rating das garrafas.
- **História:** parágrafo até 4 linhas + "LER A HISTÓRIA COMPLETA" → popup `HistoriaProdutorSheet` (nome no topo, texto a
  deslizar, quebras de parágrafo da BD preservadas). Sem `historia`, nem parágrafo nem ligação; **se a história cabe
  toda nas 4 linhas a ligação também não aparece** (não haveria mais nada para ler — o mockup mostra-a sempre, mas o
  texto do mockup é mais comprido). **Feito.**
- **Localização (mudou depois da 1.ª versão — decisão do utilizador, 2026-09-24):** o mockup tinha um mapa desenhado
  ("Mapa da quinta"). O Maps SDK para Android é gratuito sem limite, mas pede um projeto Google Cloud com **faturação
  ativada** e uma chave de API, por isso **não há mapa embebido**. Em vez dele, um cartão **"LOCALIZAÇÃO"** com a **morada em
  texto** (coluna nova `produtor_morada`, patch `14`) e "Abrir no mapa", que abre a **app de mapas do telemóvel**. Com
  coordenadas usa o **ponto exato** (`geo:lat,lon?q=lat,lon(Nome)` — a morada postal de uma quinta pode ser a da sede, não a
  das vinhas); **sem coordenadas pesquisa pela morada** (`geo:0,0?q=morada`); sem app de mapas cai para o Google Maps no
  browser. O cartão aparece se há morada **ou** coordenadas (só coordenadas: mostra-as como texto); sem nenhuma, não
  aparece. **Feito.**
- **"Garrafas em catálogo":** título + contagem + seta (a linha toda é tocável) → **catálogo do produtor**. Mostra as
  **3 primeiras** (rating desc, como o catálogo) com "**CARREGAR MAIS N**" (N = as que faltam até 3, ex.: 1 se o
  produtor tem 4); depois do toque mostra **até 6 e não carrega mais** — pedido explícito. Cada linha (imagem, nome,
  linha de atributos, rating, preço) abre o popup de detalhe em toque curto **ou premido**. **Feito.** Um acrescento
  meu: depois de mostrar as 6, se o produtor tem mais, aparece "VER AS N GARRAFAS" (mesmo destino da seta) — sem ele
  não havia sinal de que existe mais para ver.
- **Catálogo do produtor** (rota `produtor/{id}/catalogo`): pedido como "parecido com o catálogo, mas só do produtor".
  **É o próprio `CatalogScreen`/`CatalogViewModel`** (a mesma pesquisa, o mesmo popup de filtros, "ORDENAR", `BebidaCard`,
  "Carregar mais"), fixo num produtor: a ViewModel lê o id do argumento de navegação (`SavedStateHandle`) e o
  `GET /api/bebidas` ganhou o filtro **`produtorId`** (aprovado pelo utilizador em 2026-09-24; na 1.ª versão da fatia 6
  tinha sido um ecrã simples à parte, sem pesquisa nem filtros — já apagado). **O que muda no modo produtor:** seta de
  voltar + "CATÁLOGO DO PRODUTOR / Garrafas de \<produtor\>" no lugar do título "Explora o catálogo todo"; pílulas "Todas" +
  **só as categorias que o produtor tem** (`ProdutorDetail.categorias`; só aparecem se são mais do que uma; o geral
  abre sempre numa categoria, este abre em "Todas"); o popup de filtros **sem "ORIGEM"** (país/região não se aplicam a um
  só produtor) e com "Todas" na categoria; sem barra de navegação por baixo (é uma rota à parte); a dica da pesquisa é
  "Pesquisar bebida ou casta..."; o popup de detalhe sem o atalho "VER PRODUTOR". A contagem diz "1 BEBIDA" no singular
  (também no catálogo geral). **Feito.** **Ainda não feito (combinado):** a secção "Produtor" no popup do catálogo
  **geral** (pesquisa + chips, como as castas) — pede um lookup de produtores e só compensa com centenas deles; fica para
  depois dos primeiros lotes. Componente `ContagemEOrdenacao` extraído para `ui/components/`.

**Estados:** a carregar (5 pontos), erro com "TENTAR DE NOVO", produtor sem garrafas ("Ainda sem garrafas no
catálogo."). Fechar o popup de uma bebida volta a pedir os dados em silêncio (sem ecrã de carregamento nem perder as 6
garrafas abertas) — uma review nova muda o rating dela e o rating geral.

**Testes:** +19 (72 no total): `ProdutorFormatacaoTest` (domínio do site, URL, coordenadas, morada, URI `geo:` por
coordenadas ou por morada com o nome escapado, singular/plural) e `ProdutorUiStateTest` (3 de início, "CARREGAR MAIS N",
nunca mais de 6). Backend: +5 (`ProdutorRatingTest`, 93 no total); a query com `produtorId` é validada pelo
`HqlQueriesTest` (sem BD) e ao vivo.

**Validado ao vivo** (emulador `Pixel_8` + API real, conta `demo2@aquavitae.local`): nenhum produtor da BD tinha
história, coordenadas nem imagem, por isso carreguei **dados de teste temporários** no produtor 1 (Casa Ferreirinha:
história de 4 parágrafos, coordenadas do mockup, uma imagem qualquer, mais 7 bebidas movidas de outros produtores, 8
vinhos + 1 gin) — **já revertidos** (SQL de reversão guardado; a BD ficou como estava). Confirmado: cabeçalho com todos
os campos; rating geral 3,4 (3,375 = (4,25×2 + 4,0 + 1,0)/4; a média simples das 3 bebidas avaliadas daria 3,08 — a ponderada é a certa);
pílulas (a do site é tocável); popup da história com os parágrafos; **"Abrir no mapa" abre o Google Maps exatamente nas
coordenadas com o marcador "Casa Ferreirinha"** (Peso da Régua); 3 garrafas → "CARREGAR MAIS 3" → 6 → "VER AS 9
GARRAFAS"; toque **e** premido numa garrafa a abrir o popup (sem o "VER PRODUTOR" lá dentro); catálogo do produtor
com "Todas/Vinho/Gin" (Gin → 1 bebida), "ORDENAR: Nome (A-Z)" e o popup; voltar pela pilha (o scroll e as 6 garrafas
abertas mantêm-se). Os 3 pontos de entrada: cartão em destaque da home, popup a partir do catálogo e popup a partir da
cave. Um produtor com poucos dados (Quinta do Vale Meão: sem site, ano, história, mapa nem reviews) mostra só o que
tem; uma história curta (cabe em 4 linhas) não mostra "LER A HISTÓRIA COMPLETA".

**2.ª ronda, validada ao vivo (2026-09-24, depois da aprovação da morada e do filtro por produtor):** patch `14` aplicado
(duas vezes: é repetível) e `rebuild-check.sh` a dar "reconstrução igual à dev"; dados de teste temporários outra vez
(morada + coordenadas no produtor 1, só morada no Vale Meão, 7 bebidas movidas para o produtor 1) — **revertidos e
conferidos**. Cartão "LOCALIZAÇÃO" com a morada nos dois produtores; **"Abrir no mapa" sem coordenadas (Vale Meão) abre o
Google Maps a pesquisar pela morada e reconhece a Quinta do Vale Meão** (adega, com fotos); com coordenadas (Casa
Ferreirinha) abre no ponto. Catálogo do produtor: `GET /api/bebidas?produtorId=1` devolve só as bebidas dele (2 no estado
original; com `search=barca` 1; com a categoria Gin 0), pesquisa "soalheiro" → 1 bebida, popup de filtros **sem "ORIGEM"**,
com "Todas/Vinho/Gin"; escolher "Vinho" mostra "ATRIBUTOS DE VINHO" e "Ver 1 bebidas" aplica; a seta de voltar e o gesto de
voltar levam à página do produtor e depois ao catálogo. **Regressão do catálogo geral:** continua com "Explora o catálogo
todo", sem "Todas" e com "ORIGEM/Portugal" no popup. Um botão flutuante "≡" do sistema (menu de acessibilidade do
emulador, não é da app) apareceu por cima da seta de voltar a meio dos testes e interceptava os toques — não é um bug da
app (tocar na parte livre da seta funciona).

**Em aberto / por fazer:** o `regiaoId` do produtor ainda não é usado; a página não tem "partilhar"; o mockup só tinha
o caso "com todos os dados" — os estados sem imagem/site/história/morada foram decisão minha (esconder o que não existe).
Nenhum produtor real tem ainda história, morada, coordenadas nem imagem: **é dado a preencher à mão** (SQL), como os
outros atributos de conteúdo, quando o utilizador os tiver. Dívida antiga vista de passagem: o cartão de bebida diz "1
reviews" (sem singular) no catálogo/cave/home e as linhas de `ProvadasScreen` não abrem o popup de detalhe.

## Comprar (ponto 1 a seguir à fatia 6, feito e validado ao vivo — 2026-09-24)

**Sem mockup** — pedido pelo utilizador em conversa ("o botão de comprar ainda não faz nada" era o ajuste mais importante:
o dinheiro do projeto vem dos links de afiliado). As decisões abaixo são minhas, para rever.

**O que ficou feito:**
- **A pílula do preço no popup de detalhe** ("175,00€ CONTINENTE ONLINE ›") é o botão "Comprar" principal: abre a **oferta
  mais barata das disponíveis** (a que a API já devolve em `BebidaDetail.linkCompra`) no browser e regista o clique. Ganhou
  uma seta para se perceber que se toca.
- **"ONDE COMPRAR"** (tab "Detalhes", por cima do cartão do produtor), **só quando há mais de uma oferta** (com uma só, a pílula
  já é essa oferta): uma linha por retalhista, do mais barato para o mais caro — nome, "atualizado hoje / há N dias" (a data do
  último preço), preço e um botão "COMPRAR"; a mais barata leva o selo **"MAIS BARATO"** (só com mais de uma disponível). As
  **indisponíveis** ficam esbatidas, sem botão, com o motivo ("Sem stock" / "Página indisponível") e o último preço riscado.
- **"Comprar em X" da wishlist** abre a loja: como a lista só tem `BebidaSummary` (sem o link), pede as ofertas da bebida ao
  tocar e abre a mais barata das disponíveis (um duplo toque não abre duas vezes); sem nenhuma oferta disponível abre o popup de
  detalhe. Não precisou dos 5 campos adiados na fatia 4.
- **O clique é registado em segundo plano** (`POST .../links-compra/{linkId}/clique`) **depois de a loja abrir**; se falhar
  (sem rede, 409 se o link ficou indisponível) nunca impede de comprar. **Nada se abre sem toque do utilizador.**
- **"Compraste esta bebida?"** (`CompraPromptHost`, na `MainActivity`, por cima de tudo — o briefing pedia "popup pós-clique,
  janela 24-72h"): ao voltar ao primeiro plano, se há um clique **com mais de 2 minutos** e **menos de 72 horas** ainda por
  perguntar, aparece o popup com a bebida, "Tocaste em "Comprar" (Retalhista) há N min." e três saídas: **"Sim, adicionar à
  cave"** (regista `COMPREI` e abre o "Adicionar à cave" com o **preço do link já preenchido**), **"NÃO COMPREI"** (regista
  `NAO_COMPREI` e nunca mais pergunta) e **"MAIS TARDE"** (não regista nada; não volta a perguntar **neste arranque da app**,
  mas reaparece no seguinte, dentro das 72 h). Os 2 minutos: quem volta segundos depois do clique quase de certeza não
  comprou. Fica de fora a bebida que o utilizador já adicionou a uma cave desde o dia do clique. A frase não leva artigo
  ("na Continente" / "no Continente" dependia do género da loja).
- A `CaveScreen` passou a voltar a pedir as caves ao entrar (a garrafa pode ter entrado por fora, pelo "Compraste?").

**Testes:** +10 Android (`CompraFormatacaoTest`) — 82 no total; +3 backend (`CliquePerguntaTest`) — 96.

**Validado ao vivo** (emulador `Pixel_8` + API real, `demo_user2`): como a BD só tinha 2 links, ambos `PLACEHOLDER`, montei
**dados de teste temporários** (uma 2.ª oferta no Continente a 175 €, uma oferta indisponível numa loja de teste, e os URLs
trocados por `https://example.com/...` — **nenhum link de afiliado real foi aberto**) — **já revertidos e conferidos**.
Confirmado: a pílula mostra a mais barata e abre o Chrome em `example.com/continente`; "Onde comprar" com "MAIS BARATO", "atualizado
há 3 dias / há 11 dias" e a indisponível riscada; cada "COMPRAR" abre a sua loja e grava o clique (`clique_compra`); voltar de
imediato **não** pergunta; com o clique "envelhecido" 5 minutos, ao voltar aparece "Compraste esta bebida?" ("há 7 min");
**Sim** → `COMPREI`, `is_perguntado=1`, o "Adicionar à cave" abre com **175,00** e a garrafa entra na cave; **Mais tarde** → não
volta a perguntar no mesmo arranque e **volta** depois de fechar e reabrir a app; **Não comprei** → `NAO_COMPREI` e nunca
mais; "Comprar em Continente Online" da wishlist abre a loja e regista o clique.
Um erro meu apanhado a meio: o controlador novo do backend não chegou a ser criado (um `&&` a seguir a um script falhado
saltou-o) — só se viu porque o `GET .../pendentes` deu 404; ficou corrigido e testado.

**Em aberto / por fazer:**
- **Ainda nunca se abriu um link de afiliado real** (todos os testes usaram `example.com`): quando houver o 1.º lote com links
  a sério, confirmar que o tracking sobrevive (o app abre o URL tal como está na BD).
- O preço sugerido no "Compraste?" é o do link **agora** (pode ter mudado desde o clique).
- Sem "voltar a perguntar mais tarde no mesmo dia": "Mais tarde" só volta no arranque seguinte da app.
- O "Compraste?" só corre ao voltar ao primeiro plano; não há notificação (nem se pede a permissão).

## Sessão renovável (ponto 2 a seguir à fatia 6, feito e validado ao vivo — 2026-09-24)

**Sem mockup** (é infraestrutura). O problema: o JWT de acesso do backend dura **60 minutos**, a app **não o renovava** e uma
resposta 401 a meio da utilização não levava a lado nenhum — passada 1 hora os pedidos falhavam sem explicação (só o arranque tratava
um 401).

**Como funciona:**
- **Backend:** `login`/`register` devolvem também um `refreshToken` (opaco, 30 dias, guardado só como hash SHA-256 na tabela nova
  `utilizador_refresh_token`, patch `15`); `POST /api/auth/refresh` troca-o por um par novo e **roda-o** (o usado fica revogado);
  `POST /api/auth/logout` revoga-o; redefinir a password revoga todos. Um token já rodado que volta a aparecer (fora de 60 s de
  tolerância para uma resposta perdida) revoga **todas** as sessões do utilizador. Detalhe em `backend/API_ENDPOINTS.md`, "Auth".
- **App:** o `TokenAuthenticator` (`Authenticator` do OkHttp) trata cada 401 de um pedido que levava sessão: renova **uma vez para
  todos os pedidos em simultâneo** (o refresh token roda, por isso usá-lo duas vezes seguidas falharia), repete o pedido com o token
  novo e, **se a renovação for recusada** (expirou, revogado, password mudou), apaga a sessão e o `AppNavHost` leva ao login, que
  mostra **"A tua sessão expirou. Entra novamente."** até haver uma entrada bem-sucedida. **Sem rede ou com erro do servidor a sessão
  mantém-se** (apagá-la por uma falha de rede punha quem tem má cobertura a fazer login a toda a hora); o pedido falha com o 401 e o
  utilizador tenta de novo. Um pedido anónimo (o login com a password errada) e os de `/api/auth/` nunca se renovam.
  "Terminar sessão" também revoga o token no servidor (melhor esforço).

**Decisões minhas:** 30 dias de validade e 60 s de tolerância (configuráveis em `jwt.*`); o texto do aviso do login; **não se renova
antes de expirar** (ver "Em aberto").

**Um defeito de desenho apanhado ao testar ao vivo (e dois nos testes):** (1) a tolerância de 60 s valia para **qualquer** token
revogado, por isso depois de uma revogação em massa (reutilização, password nova) os tokens ainda se aceitavam mais 60 s — agora só
vale para a **rotação** (coluna nova `refresh_token_revogado_motivo`: `ROTACAO`/`LOGOUT`/`PASSWORD`/`REUTILIZACAO`); (2) com um 401
persistente o `Authenticator` renovava **duas** vezes — apanhado pelo `TokenAuthenticatorTest`; agora uma por pedido.

**Testes:** +7 backend (`RefreshTokenRegrasTest`, agora 9 — validade, rotação, tolerância só para a rotação, hash SHA-256 com vetor
conhecido, tokens de 43 caracteres sem repetições; **105** no total) e +10 Android (`TokenAuthenticatorTest`, contra um
`MockWebServer` local, ferramenta de teste nova; **92** no total).

**Validado ao vivo** (API com `JWT_EXPIRATION_MINUTES=1`, emulador `Pixel_8`, `demo_user2`): login devolve o `refreshToken`; `refresh`
roda-o; repetir o antigo dentro de 60 s → 200; um inventado → 401; corpo vazio → 400; **fora da tolerância, reutilizar um rodado →
401 e o token novo também deixa de servir** (`REUTILIZACAO`); **na app, com o token de acesso expirado, abrir "Favoritos" carregou a
lista normalmente** (token 9 `ROTACAO`, novo token 10); **com a sessão revogada por SQL e o token expirado, a app voltou ao login com
"A tua sessão expirou"**; entrar de novo e **"Terminar sessão" deixou o token `LOGOUT`** no servidor.
Um erro meu apanhado no fim: ao repor os dados de teste do "Comprar" usei um URL truncado (a listagem cortava aos 70 caracteres) —
o `rebuild-check.sh` apontou "BEBIDA_LINK_COMPRA: mesmo nº de linhas mas texto diferente"; reposto pelo valor do seed.

**Em aberto:**
- **Não renova antes de expirar:** um pedido a um endpoint **público** com o token já expirado (o catálogo, por exemplo) não devolve 401
  — o backend trata-o como anónimo — e vem sem as marcações do utilizador (favorito/wishlist/provada) até haver um pedido autenticado
  (o arranque e a maioria dos ecrãs fazem-no logo). Se incomodar: renovar no `AuthInterceptor` quando o `exp` do JWT já passou.
- Sessões **de antes desta versão** (sem refresh token) acabam ao primeiro 401 e voltam ao login uma vez.
- Sem "terminar as outras sessões" (nem lista de dispositivos) — a coluna e a tabela já o permitiriam.
- Em produção o `JWT_SECRET` tem de ser definido (hoje há um valor de desenvolvimento por omissão).
