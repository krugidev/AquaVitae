# AquaVitae — Android

App Android (Kotlin + Jetpack Compose) da AquaVitae (catalogação e comunidade de vinhos e bebidas espirituosas). **Este ficheiro é o
documento do frontend**: estado, estrutura, convenções e mapa de ecrãs. O roadmap está no [`../PLANO.md`](../PLANO.md), o design
(prints + notas por ecrã) em [`design/README.md`](design/README.md) e o contrato da API em
[`../backend/API_ENDPOINTS.md`](../backend/API_ENDPOINTS.md).

## Estado (atualizar no fim de cada fatia)

O frontend constrói-se em **fatias verticais**, cada uma um fluxo de ecrãs desenhados pelo utilizador, ligada à API real desde o 1.º ecrã.

| Fatia | Conteúdo | Estado |
|---|---|---|
| 1a | ícone, loading, login, registo (+ popup dos termos), tema/componentes, sessão | ✅ feita (2026-09-21) |
| 1b | recuperar password (3 passos + modal) e onboarding (7 ecrãs de perfil e preferências), Coil + SVG | ✅ feita (2026-09-22) |
| 2a | homepage (saudação, pesquisa (botão), "Escolhido para ti", "As minhas Caves", estatísticas, produtor em destaque) + barra de navegação | ✅ feita (2026-09-23) |
| 2b | catálogo a sério: lista, pesquisa, popup de filtros completo (categoria, origem, preço, rating, atributos de vinho, castas) | ✅ feita (2026-09-23) |
| 3a | popup de detalhe da bebida (partilhado por toda a app): tabs Detalhes/Reviews, favorito/wishlist, publicar review | ✅ feita (2026-09-23) |
| 3b | cave: lista ("As minhas Caves"), popup "Nova cave", popup "Adicionar à cave" (com destaque das caves onde a bebida já está) | ✅ feita (2026-09-23) |
| 3c | ajustes de feedback: popup de confirmação ao duplicar numa cave, review a exigir "provada", "Consumir" a marcar provada, ecrã "Já provadas" | ✅ feita (2026-09-23) |
| — | correção pós-3c: `getFavoritos`/`getWishlist` com o modelo errado, totais da cave sem atualizar após "Adicionar à cave", e as duas telas presas ao resultado da 1.ª visita à aba | ✅ feita (2026-09-23/24) |
| 4 | Favoritos e Wishlist a sério (filtros/ordenação, nota própria vs. média, "Comprar"/"Para a cave") | ✅ feita (2026-09-24) |
| 5 | perfil: ver/editar dados e preferências, escolher avatar, terminar sessão | ✅ feita (2026-09-24) |
| 6 | página do produtor (imagem, rating geral, história, mapa, garrafas) + catálogo só desse produtor | ✅ feita (2026-09-24) |
| 7 | comprar: a pílula do preço e "COMPRAR" abrem a loja e registam o clique, "Onde comprar", "Compraste?" ao voltar do browser, "Comprar em X" da wishlist | ✅ feita (2026-09-24) |

Os ecrãs `detail` e `reviews` continuam **placeholders do esqueleto** (esqueleto morto, por limpar — ver abaixo): estão
embrulhados em `LegacyScreen` (`navigation/AppNavHost.kt`), que dá o espaço das barras do sistema. Ao redesenhar um,
tira-se o invólucro — já aconteceu a `home` (fatia 2a), a `catalog` (fatia 2b), a `cave` (fatia 3b) e, na fatia 4,
`wishlist`/`favoritos`. `home` é o ecrã de destino depois do login/onboarding (antes ia para `catalog`);
`catalog`/`cave`/`wishlist`/`favoritos` continuam acessíveis pela barra de navegação (`BottomNavBar`), que também
sobrepõe todos eles. `detail`/`reviews` deixaram de se alcançar por toque
desde a fatia 3a (o detalhe de uma bebida é sempre o popup `BebidaDetalheSheet`) — ficam como código morto, por limpar.

## Compilar, instalar e ver (Windows; detalhes e porquês no `../CLAUDE.md`)

O projeto não tem `gradlew` (ver "Android — compilar por linha de comandos" no `../CLAUDE.md`). Compila-se com o Gradle 8.7 da cache e o
**JDK 21** (o JDK por omissão da máquina, o 25, não serve). Com a API (`../backend`) e o emulador `Pixel_8` a correr:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$gradle = (Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.7-bin" -Recurse -Filter gradle.bat | Select-Object -First 1).FullName
& $gradle -p android assembleDebug testDebugUnitTest --console=plain --no-daemon     # compila e corre os testes
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb install -r android\app\build\outputs\apk\debug\app-debug.apk
& $adb shell am start -n pt.aquavitae.android/.MainActivity
& $adb exec-out screencap -p > ecra.png          # esperar ~3 s: o 1.º frame é o ecrã de arranque do sistema
```

Emulador: `& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_8` (ou o Device Manager do Android Studio, que abre `android/`
com *Gradle JDK = 21*). Em debug a app fala com `http://10.0.2.2:8080/` (o `localhost` do PC visto do emulador). Se estiver o Studio a
compilar, não compilar ao mesmo tempo pela linha de comandos.

## Estrutura

```
android/
├── design/                    prints do Figma + especificação por ecrã (design/README.md)
└── app/src/
    ├── main/kotlin/pt/aquavitae/android/
    │   ├── AquaVitaeApplication.kt      @HiltAndroidApp
    │   ├── MainActivity.kt              ecrã de arranque do sistema + edge-to-edge + tema + NavHost
    │   ├── data/
    │   │   ├── model/                   DTOs (@JsonClass): Auth, User, Lookup (+ LookupState), Preferencia, Bebida, Cave, Review, ...
    │   │   ├── network/                 AquaVitaeApi (Retrofit), AuthInterceptor, NetworkModule, ApiErrors.kt, ImageUrls.kt
    │   │   ├── local/                   TokenDataStore (sessão em DataStore)
    │   │   └── repository/              um por área (Auth, User, Lookup, Preferencia, Bebida, ...); devolvem Result<T>
    │   ├── di/                          módulos Hilt (DataStore)
    │   ├── feature/                     um pacote por fluxo: auth/ loading/ recovery/ onboarding/ home/ catalog/ detail/ ...
    │   ├── navigation/                  AppDestinations (rotas) + AppNavHost
    │   └── ui/
    │       ├── theme/                   Color, Type (Inter + AquaText + serifa da homepage), Shape, Theme (só claro)
    │       └── components/              componentes reutilizáveis (abaixo)
    ├── main/res/                        font/ (Inter), drawable/ (emblema, wordmark, ícones da nav — ic_nav_*/ic_perfil), values/,
    │                                    mipmap-anydpi-v26/ (ícone adaptativo)
    ├── debug/AndroidManifest.xml        cleartext HTTP só em debug
    └── test/kotlin/                     testes unitários (JUnit 4)
```

## Convenções

- **Estado dos ecrãs:** `ViewModel` (Hilt) com um `StateFlow` de uma `sealed interface` (`AuthUiState`, `SessionState`); o ecrã só
  desenha e chama o ViewModel. Repositórios devolvem `Result<T>` (`runCatching`); os erros mostram-se com `Throwable.toUserMessage()`
  (`data/network/ApiErrors.kt`: mensagens em português; 401/409 da API passam, 400 vira frase genérica, sem rede vira "Sem ligação…").
- **Componentes primeiro:** um ecrã novo usa `ui/components/` e `AquaText`/`Color.kt`; **nada de cores, tamanhos de letra ou formas
  soltas**. Se falta um componente, cria-se lá (e regista-se abaixo).
- **Barras do sistema (edge-to-edge, obrigatório no Android 15+):** todo o ecrã novo aplica `systemBarsPadding()` (ou usa `AuthScaffold`);
  o teclado usa `imePadding()`. A `MainActivity` deixa as barras transparentes com ícones escuros (o fundo é claro).
- **Tema só claro** (o design é claro). **Textos em PT-PT.** Validar o que se pode no cliente antes de enviar (`AuthValidation.kt`) e
  escrever essa validação em Kotlin puro para a poder testar em JVM.
- **Sessão:** `TokenDataStore` guarda o JWT; o `AuthInterceptor` acrescenta o `Bearer`. O arranque (`SessionViewModel`) valida a sessão com
  `GET /api/users/me`.
- **Imagens:** o `imagePath` de uma bebida pode ser um URL absoluto (`https://…`, do retalhista) ou um caminho relativo (os avatares:
  `icones/avatares/casta-bago.svg`): `resolveImageUrl(path)` (`data/network/ImageUrls.kt`) usa-o tal como está se começa por `http(s)://`,
  senão prefixa-se `API_BASE_URL`. Carregamento com **Coil 2.7** (`AsyncImage`); o carregador único da app (com o `SvgDecoder`, que deixa
  desenhar os avatares SVG) é criado em `AquaVitaeApplication`. A Coil 3 pede Kotlin 2: só depois da migração da toolchain.
- **Listas que vêm da API** (opções dos formulários): `LookupState` (`Loading` / `Error` / `Ready`) no ViewModel e `LookupContent` no ecrã
  (os 5 pontos, ou a mensagem com "TENTAR DE NOVO"); o ViewModel só volta a pedir as que não chegaram (`carregarLookups()`).
- **Regras em Kotlin puro** (`RecoveryRules.kt`, `OnboardingRules.kt`, `AuthValidation.kt`, `flagEmoji`, `resolveImageUrl`): tudo o que se
  pode testar sem Android fica em funções puras com testes JVM; os ViewModels só orquestram.
- **Teclado:** `imePadding()` no esqueleto; a tecla "Seguinte" tem de ir para o campo seguinte, por isso nada focável (ex.: o olho da
  password) fica entre dois campos — o `UnderlineField` já o trata (`focusProperties { canFocus = false }`). Onde o conteúdo rola por causa
  do teclado (`AuthScaffold(scrollState = ...)`), o ecrã pode rolar até ao fim quando o teclado abre (ver `RecoveryScreen`).
- **Popups (`Dialog`):** chamar `DialogScrim()` dentro do conteúdo, para o escurecimento ser o do desenho (43 %) e não o do Compose (60 %).
  Um `Dialog` a ecrã inteiro precisa também de `DialogFillScreen()` (a janela dimensiona-se por omissão ao conteúdo) — ver o "gotcha"
  completo no `../CLAUDE.md` (insets da barra de navegação dentro de um `Dialog`).
- **Ícones PNG que vêm do utilizador** (ex.: `ic_nav_*.png`, brancos/cinza-claro sobre transparente): não se reexportam por cor — usa-se
  `Icon(painter = painterResource(...), tint = cor)` (Compose tinge pelo canal alfa, a cor original do PNG não importa).
- **Uma fatia de cada vez**, com a API corrigida primeiro se o ecrã pedir algo que ela não tem (ver `../PLANO.md`).
- **Popup de filtros que só aplica ao confirmar** (padrão novo do `CatalogViewModel`, fatia 2b): o ecrã guarda um filtro
  **aplicado** (o que a lista de baixo usa) e um **rascunho** (o que o popup está a editar); só "Ver N bebidas" copia o
  rascunho para o aplicado. Fechar de qualquer outra forma (fora, ou o gesto de voltar) descarta o rascunho. Uma contagem ao
  vivo no botão consulta a mesma pesquisa com `size=1` (só para ler `totalElements`), com um pequeno atraso (`delay`, não
  `debounce` do Flow — mais simples de acertar com o `Job` cancelado a cada edição) para não disparar 1 pedido por toque.
- **Popup sem rota (`BebidaDetalheSheet`, fatia 3a):** um popup que abre por cima de qualquer ecrã (catálogo, caves, favoritos)
  não é uma entrada do `AppNavHost` — não há `SavedStateHandle` para lhe dar o id. Padrão: `hiltViewModel(key =
  "bebida-detalhe-$id")` (uma chave por id dá uma instância nova do ViewModel, com estado limpo, sem precisar de injeção
  assistida do Hilt) e um método público `carregar(id)` chamado por `LaunchedEffect(id) { viewModel.carregar(id) }` no ecrã, em
  vez do `init {}` habitual. O ecrã que mostra o popup guarda `var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }`
  e desenha o popup condicionalmente — o `BebidaCard` chama o mesmo `onClick` em toque curto e premido
  (`combinedClickable`, `ExperimentalFoundationApi`), já que o mockup só define o premido e não há outro destino para o curto.
  O mesmo padrão repete-se no `AdicionarACaveSheet` (fatia 3b, `hiltViewModel(key = "adicionar-cave-$bebidaId")`) — e os dois
  popups **empilham-se** (o de detalhe fica aberto por baixo do de "Adicionar à cave"), mais simples do que fechar um para
  abrir o outro.

## Componentes reutilizáveis (`ui/components/`)

| Componente | Para quê |
|---|---|
| `AquaVitaeLogo(LogoVariant.Stacked / Horizontal / Compact)` | logótipo (emblema em vetor + nome em texto) |
| `LoadingDots` | 5 pontos vermelhos em onda: o indicador de carregamento da app |
| `UnderlineField` | campo com linha grená e rótulo que sobe ao escrever; `isPassword` (olho), `isError` (vermelho) |
| `PrimaryButton` | botão grande grená; `loading` mostra uma roda |
| `LinkText` | texto clicável em maiúsculas (ligações por baixo do cartão) |
| `AuthCard`, `AuthScaffold`, `Modifier.overlapTop` | cartão cinzento; esqueleto do ecrã (fundo, scroll, teclado, rodapé "TERMOS E CONDIÇÕES"; aceita o `scrollState`); botão sobreposto à margem do cartão |
| `TermsDialog`, `PasswordChangedDialog`, `DialogScrim`, `DialogFillScreen` | popups grenás (aceitação dos termos; "Password alterada!" que se fecha sozinho); escurecimento a 43 %; janela do `Dialog` a preencher o ecrã (por omissão dimensiona-se ao conteúdo) |
| `feature/legal/TermsSheet` | o popup do texto dos termos e condições (sobe de baixo, arrasta para fechar, vem de `GET /api/legal/termos`) — não é `ui/components/` por ter ViewModel próprio |
| `PillCard(title, fillHeight)` | cartão cinzento com o cabeçalho em pílula grená (recuperar password, onboarding); com `fillHeight` o corpo ocupa a altura que sobra |
| `NavRow`, `RoundNavButton`, `NextButton` | linha de navegação (seta cinzenta para trás, seta grená para a frente, "IGNORAR" no meio); "PRÓXIMO →" |
| `OptionRow`, `PillChip`, `LookupContent` | opção de escolha múltipla (círculo com visto); pílula de filtro (categorias de avatar); mostra uma lista da API (a carregar / erro / pronta) |
| `LevelSlider` | slider vertical de níveis (degradê, ponteiro, rótulos; toque ou arrastar) |
| `CodeInput` | o código de recuperação: caixa com N espaços sublinhados, um campo de texto por baixo (teclado numérico, dá para colar) |
| `FlagChip`, `flagEmoji(codigoPais)` | rebordo com a bandeira e a seta de lista; bandeira (emoji) a partir do código ISO |
| `Modifier.verticalScrollbar(listState)` | a barra de deslocamento fina do lado direito de uma lista |
| `FloatingLabel` (em `UnderlineField.kt`) | o rótulo que sobe e encolhe, partilhado por campos que não são de escrever (nacionalidade) |
| `BottomNavBar`, `BottomNavItem`, `BottomNavContentPadding` | a barra de navegação principal (5 destinos, "Home" elevado ao centro); os ícones são os PNG do utilizador, tingidos por `ColorFilter` |
| `AvatarBadge`, `iniciaisDe(...)` | o avatar (SVG do onboarding, ou as iniciais do nome/username) — cabeçalho da homepage/wishlist/favoritos e autor de uma review; `onClick` opcional abre o perfil |
| `AvatarGridPicker`, `AvatarTileGrid` | pílulas de categoria + grelha 3×3 de avatares (`AvatarGridPicker`, usado no onboarding) e só a grelha (`AvatarTileGrid`, pública, usada pelo `EscolherAvatarSheet` do perfil, que tem uma pílula "Todos" extra sem equivalente no onboarding) |
| `BebidaCard` | o cartão de bebida (imagem, nome+ano, produtor•região, `linhaAtributos()`, rating, retalhista+preço) — homepage, catálogo; toque curto ou premido (`combinedClickable`) abrem o `BebidaDetalheSheet` |
| `RangePillRow` | o seletor de intervalo 1–5 (acidez/doçura do popup de filtros): toque escolhe um nível, toque noutro estica o intervalo |
| `ContagemEOrdenacao` | a linha "N BEBIDAS ..... ORDENAR: X ▾" com o menu de ordenação, genérica no tipo da opção — catálogo e catálogo do produtor |
| `feature/catalog/FiltrosSheet` | o popup "Filtros" do catálogo (sobe de baixo, sobre `Dialog` como o `TermsSheet`) — não é `ui/components/` por ter o `CatalogViewModel` próprio |
| `feature/bebidadetalhe/BebidaDetalheSheet` | o popup de detalhe de uma bebida (partilhado por toda a app): tabs Detalhes/Reviews, favorito/wishlist otimistas, publicar review. Sem rota/`SavedStateHandle` — `hiltViewModel(key = "bebida-detalhe-$bebidaId")` + `carregar(bebidaId)` por `LaunchedEffect` (ver "Convenções") |
| `feature/cave/NovaCaveSheet` | o popup "Nova cave" (nome + descrição) — reutilizado tal e qual dentro do `AdicionarACaveSheet` ("+ nova cave") |
| `feature/cave/AdicionarACaveSheet` | o popup "Adicionar à cave" (aberto do "+" do popup de detalhe): pílulas de cave com destaque independente de "já tem esta bebida" (✓) e "escolhida para este adicionar" (cor); quantidade, preço, `DatePicker` do Material3, janela de consumo, notas; se a cave escolhida já tiver a bebida (✓), confirma antes de guardar (`AlertDialog`, "Adicionar na mesma"/"Cancelar") |
| `feature/perfil/EscolherAvatarSheet` | o popup "Escolher avatar" do perfil: pílulas "Todos"/categorias + `AvatarTileGrid`; só confirma localmente (`onConfirmar`) — quem grava é o "Guardar" do `EditarPerfilScreen` |
| `feature/perfil/EditarPreferenciasSheet` | o popup "Editar preferências" do perfil: as mesmas do onboarding (tipos de bebida, acidez/doçura, castas), aqui em pílulas horizontais compactas (`RangePillRow` reutilizado tal e qual) em vez das listas paginadas do onboarding |
| `feature/produtor/HistoriaProdutorSheet` | o popup "Ler a história completa" da página do produtor: nome no topo, texto a deslizar, parágrafos da BD preservados |
| `feature/compra/CompraPromptHost` | o inquérito "Compraste?": vive na `MainActivity`, por cima do grafo de navegação; ao voltar ao primeiro plano (`ON_RESUME`) pede `GET /users/me/cliques-compra/pendentes` e mostra "Compraste esta bebida?" (Sim → "Adicionar à cave" com o preço do link; Não comprei; Mais tarde) |
| `ui/util/abrirLink` | `Context.abrirLink(url)`: abre um URL fora da app (browser/app da loja/site do produtor/mapas); devolve `false` se não houver nada que o abra |

**Tokens novos da homepage/catálogo** (`ui/theme/Color.kt`/`Type.kt`): `WineDark` (cartão do produtor em destaque), `RoseBorder`
(contorno das caixas de estatística e das pílulas de filtro removíveis); `AquaText.SectionSerif`/`SectionSerifAccent`/`GreetingSerif`/
`BebidaNomeSerif` (serifa do sistema, aproximação — ver "Lacunas conhecidas"). `BebidaSummary.linhaAtributos()` e `anoNoNome()`/
`formatarPrecoPt()` (`data/model/BebidaFormatacao.kt`) são as funções de formatação partilhadas pelo `BebidaCard`.

## Mapa de ecrãs (rota → ficheiro → API)

| Rota | Ficheiro | Estado | Endpoints |
|---|---|---|---|
| `loading` | `feature/loading/LoadingScreen.kt` (+ `SessionViewModel`) | ✅ | `GET /users/me` |
| `login` | `feature/auth/LoginScreen.kt` | ✅ | `POST /auth/login`, `GET /users/me`, `POST /users/me/termos/aceitar` |
| `register` | `feature/auth/RegisterScreen.kt` | ✅ | `POST /auth/register` |
| `recover` | `feature/recovery/RecoveryScreen.kt` (+ `RecoveryViewModel`, `RecoveryRules`) | ✅ | `POST /auth/recuperar-password`, `/verificar-codigo`, `/redefinir-password` (por `identificador`) |
| `onboarding` | `feature/onboarding/OnboardingScreen.kt` (+ `ProfileSteps`, `PreferenceSteps`, `OnboardingViewModel`, `OnboardingRules`) | ✅ | `PUT /users/me`, `PUT /users/me/preferencias`, `GET /lookup/{nacionalidades, avatar-categorias, avatares, categorias-bebida, castas}` |
| `home` | `feature/home/HomeScreen.kt` (+ `HomeViewModel`, `HomeUiState`) | ✅ | `GET /users/me`, `/lookup/categorias-bebida`, `/bebidas?categoriaIds=`, `/users/me/caves`, `/caves/{id}`, `/produtores/destaque` |
| `catalog` | `feature/catalog/CatalogScreen.kt` (+ `CatalogViewModel`, `CatalogUiState`, `FiltrosSheet`) | ✅ | `GET /bebidas` (todos os filtros: `categoriaIds`, `paisId`, `regiaoIds`, `precoMin/Max`, `ratingMin`, `acidezMin/Max`, `docuraMin/Max`, `corpoId`, `taninoId`, `tipoId`, `castaIds`, `search`, `sort`), `/lookup/{paises, regioes, vinho/corpos, vinho/taninos, vinho/tipos, castas, categorias-bebida}` |
| `cave` | `feature/cave/CaveScreen.kt` (+ `CaveViewModel`, `CaveUiState`, `NovaCaveSheet`) | ✅ | `GET /users/me/caves`, `/caves/{id}?sort=`, `POST /users/me/caves`, `POST .../consumir` (marca "provada" a seguir, `POST /bebidas/{id}/provada`), `GET /users/me/provadas` (resumo de 5) |
| `provadas` | `feature/provadas/ProvadasScreen.kt` (+ `ProvadasViewModel`) | ✅ | `GET /users/me/provadas?categoriaId=&ano=`, `/lookup/categorias-bebida`, `GET /bebidas` (busca), `POST /bebidas/{id}/provada` |
| `wishlist` | `feature/wishlist/WishlistScreen.kt` (+ `WishlistViewModel`) | ✅ | `GET /users/me/wishlist`, `GET /users/me`, `POST/DELETE .../wishlist`, `GET /bebidas/{id}` (para "Para a cave") |
| `favoritos` | `feature/favoritos/FavoritosScreen.kt` (+ `FavoritosViewModel`) | ✅ | `GET /users/me/favoritos`, `GET /users/me`, `DELETE .../favorito` |
| `perfil` | `feature/perfil/PerfilScreen.kt` (+ `PerfilViewModel`) | ✅ | `GET /users/me`, `/users/me/preferencias`, `/lookup/categorias-bebida`, `/lookup/castas` |
| `perfil/editar` | `feature/perfil/EditarPerfilScreen.kt` (+ `EditarPerfilViewModel`) | ✅ | `GET /users/me`, `/lookup/nacionalidades`, `PUT /users/me` |
| *(popup, sem rota)* | `feature/bebidadetalhe/BebidaDetalheSheet.kt` (+ `BebidaDetalheViewModel`) | ✅ | `GET /bebidas/{id}`, `/bebidas/{id}/reviews`, `POST/DELETE .../favorito`, `.../wishlist`, `POST .../reviews` (só se `isProvada == true`; já não marca "provada" sozinho, ver "3c") |
| *(popup, sem rota)* | `feature/cave/AdicionarACaveSheet.kt` (+ `AdicionarACaveViewModel`) | ✅ | `GET /users/me/caves?bebidaId=`, `POST /caves/{id}/bebidas`, `POST /users/me/caves` |
| *(popup, sem rota)* | `feature/perfil/EscolherAvatarSheet.kt` (+ `EscolherAvatarViewModel`) | ✅ | `GET /lookup/avatar-categorias`, `/lookup/avatares` |
| *(popup, sem rota)* | `feature/perfil/EditarPreferenciasSheet.kt` (+ `EditarPreferenciasViewModel`) | ✅ | `GET /lookup/categorias-bebida`, `/lookup/castas`, `PUT /users/me/preferencias` |
| `produtor/{id}` | `feature/produtor/ProdutorScreen.kt` (+ `ProdutorViewModel`) | ✅ | `GET /produtores/{id}`, `/produtores/{id}/bebidas?size=6` |
| `produtor/{id}/catalogo` | `feature/catalog/CatalogScreen.kt` (+ `CatalogViewModel`, **o mesmo ecrã do catálogo**, fixo num produtor: lê o id do argumento de navegação) | ✅ | `GET /produtores/{id}`, `GET /bebidas?produtorId=` |
| *(popup, sem rota)* | `feature/produtor/HistoriaProdutorSheet.kt` | ✅ | — (usa a `historia` do produtor) |
| *(popup global, sem rota)* | `feature/compra/CompraPromptHost.kt` (+ `CompraPromptViewModel`) | ✅ | `GET /users/me/cliques-compra/pendentes`, `POST .../{id}/resposta`, `GET /bebidas/{id}` |

Depois do login/onboarding vai-se para `home` (antes ia para `catalog`); só quem acabou de se registar passa pelo onboarding. A
recuperação de password volta ao login com o popup "Password alterada!" (o login recebe o aviso pelo `savedStateHandle`, ver
`AppNavHost`). `home`, `catalog`, `cave`, `wishlist` e `favoritos` têm a `BottomNavBar` sobreposta ao fundo (`TabScreen` em
`AppNavHost.kt`); trocar de separador preserva o estado de cada um (`saveState`/`restoreState`, o padrão de navegação por abas).
`provadas`, `perfil`/`perfil/editar` e `produtor/{id}`/`produtor/{id}/catalogo` **não são das 5 abas** — `provadas` alcança-se só a partir da Cave ("ABRIR MAIS" no
resumo de "Já provadas"), `perfil` tocando no avatar (repetido nos cabeçalhos de `home`/`wishlist`/`favoritos`) e
`perfil/editar` a partir do próprio `perfil` — `produtor/{id}` pelo cartão do produtor em destaque (home) e pelo cartão do produtor do popup de detalhe de uma bebida (`onVerProdutor`, ligado em home/catálogo/cave/favoritos/wishlist), e `produtor/{id}/catalogo` pela seta de "Garrafas em catálogo" — nenhum tem `BottomNavBar` própria, tal como o popup de detalhe da bebida.
**`detail/{id}`/`reviews/{id}` deixaram de se alcançar por toque** (fatia 3a: o detalhe de uma bebida é sempre o popup
`BebidaDetalheSheet`, aberto de dentro do próprio ecrã) — as rotas e os ficheiros `feature/detail`/`feature/reviews` ficam no
código como esqueleto morto, por limpar numa fatia futura.

## Testes

Unitários (JVM), **82**: `AuthValidationTest` (10, validação do registo), `RecoveryRulesTest` (10, email tapado, password nova),
`OnboardingRulesTest` (12, ordem dos ecrãs, montagem dos pedidos de perfil e preferências), `FlagEmojiTest` (3), `ImageUrlsTest` (4),
`AvatarBadgeTest` (6, iniciais do avatar), `BebidaFormatacaoTest` (8, a fórmula de `linhaAtributos()` e a extração do ano do nome). `ProdutorFormatacaoTest` (14, domínio do site, URL, coordenadas, morada, URI `geo:` por coordenadas ou por morada com o nome escapado, singular/plural) e `ProdutorUiStateTest` (5, 3 garrafas de início, "CARREGAR MAIS N", nunca mais de 6). `CompraFormatacaoTest` (10, a oferta principal é a mais barata das disponíveis com link, "MAIS BARATO" só com comparação, "há N min/horas/dias", "atualizado hoje").
Ainda **sem** testes de ViewModels nem de UI (pede `kotlinx-coroutines-test`; fica para quando compensar) — `HomeViewModel` e
`CatalogViewModel` também ainda não têm testes (o `RangePillRow` e a lógica de intervalo do popup de filtros também só validados ao
vivo, não têm teste unitário). `descricaoJanela` (`HomeScreen.kt`) continua `private`, sem teste. Os fluxos validam-se ao vivo no
emulador (compilar → instalar → `adb shell input tap/text` → screenshot), como se descreve no `../CLAUDE.md`.

## Lacunas conhecidas

- `AquaVitaeApi.kt` está sincronizado para auth (incl. recuperação), `/me` (agora com as estatísticas), termos, perfil e preferências do
  onboarding, os lookups do onboarding + os do popup de filtros (`paises`, `regioes`, `vinho/corpos`, `vinho/taninos`, `vinho/tipos`), a
  pesquisa de bebidas completa (`GET /bebidas` com todos os filtros do catálogo), `/bebidas/sugeridas`, `/produtores/destaque`, as
  caves (`CaveResponse`/`CaveDetailResponse`/`CaveBebidaResponse` com os totais, `prontasAAbrir`/`emGuarda`, `estado`, janelas,
  `temBebida`, `?sort=`, `POST .../consumir`), `GET /bebidas/{id}/reviews` (agora `ReviewsResponse`), as provadas
  (`addProvada`/`removeProvada`/`getProvadas`) e, desde a correção pós-3c, **`getFavoritos()`/`getWishlist()` também**
  (`List<BebidaRelacao>`, como o `getProvadas` — antes declaravam `List<BebidaSummary>` e o backend sempre devolveu
  `List<BebidaRelacaoDto>` `{ bebida, data, hasReview }`, o que **partia** os dois ecrãs com "Required value 'id' missing";
  apanhado pelo utilizador a testar por conta própria, corrigido e validado ao vivo). Com o modelo certo, sobrou um 2.º
  problema: as duas telas só pediam os dados no `init {}` da ViewModel, por isso ficavam presas ao resultado da 1.ª visita à
  aba (a navegação por abas preserva a instância da ViewModel — ver `../CLAUDE.md`); corrigido com
  `LaunchedEffect(Unit) { viewModel.carregar...() }` no topo de `FavoritosScreen`/`WishlistScreen`, que volta a correr (e a
  pedir dados frescos) sempre que a aba é reaberta. Resto da lista em `../backend/API_ENDPOINTS.md`, "Sincronização pendente
  com o Android". Sincroniza-se por fatia.
- **Homepage (fatia 2a), Catálogo (fatia 2b), popup de detalhe da bebida (fatia 3a), Cave (fatia 3b), ajustes + "Já provadas"
  (fatia 3c) e Favoritos/Wishlist (fatia 4), feitos:** ver a lista completa do que falta em `design/README.md`, secções "14.",
  "15.", "16." e "Favoritos e Wishlist" — em resumo: "VER PRODUTOR" já tem destino desde a fatia 6; "Ver as N garrafas" da cave é só informativo (a lista já mostra tudo); a secção "As minhas Caves" já foi
  validada com garrafas a sério; a serifa dos títulos é uma aproximação do sistema; "ORDENAR" do catálogo só tem 2 opções
  (falta ordenar por preço, que pede trabalho no backend); o `RangeSlider` do preço nunca foi testado com dados a sério; a
  review só aceita estrelas inteiras (sem as meias-estrelas do mockup); `DetailScreen`/`ReviewsScreen` (esqueleto antigo)
  ficam como código morto, por limpar; falta trocar `AquaVitaeLogo` (texto Inter) pelo wordmark real (`ic_wordmark_home`) nos
  outros ecrãs de auth; **Favoritos/Wishlist ficam sem 4 detalhes do mockup** ("ATUALIZADA HOJE"/"MENOR DE N RETALHISTAS",
  "N NA CAVE", "PROVADA EM \<mês\>", e o link no próprio `BebidaSummaryDto`) — pedem campos novos no `BebidaSummaryDto`, adiados
  a pedido do utilizador (ver `PLANO.md`, "Por fazer depois"); o "Comprar em X" já abre a loja sem esse campo (pede as ofertas
  ao tocar, ver "Comprar"); a deteção de descidas de preço da wishlist foi pedida para ficar de fora por agora.
- **Perfil (fatia 5), feito:** ver `design/README.md`, "Perfil", para a lista completa — em resumo: "As minhas reviews" e
  "Conta e segurança" ainda sem destino (mockups por chegar); o email aparece como texto simples por baixo da bio em vez da
  linha "Email e password" do mockup (também à espera do mockup de "Conta e segurança"); o suporte de emojis na bio não foi
  validado ao vivo (o `adb shell input text` não consegue enviar emoji, uma limitação da própria ferramenta — por código,
  o campo não restringe o tipo de teclado e o Android troca sozinho para a fonte de emoji do sistema quando a Inter não
  tem o glifo, o comportamento por omissão da plataforma).
- **Comprar (ponto 1 depois da fatia 6), feito:** ver `design/README.md`, "Comprar". Em resumo: sem mockup (decisões minhas, registadas lá);
  o "Compraste?" só pergunta por cliques com mais de 2 min e menos de 72 h; **falta testar com um link de afiliado real** (todos os
  testes usaram `example.com`); "Comprar em X" da wishlist faz um pedido extra ao tocar (não usa os 5 campos adiados).
- **Produtor (fatia 6), feito:** ver `design/README.md`, "Produtor", para a lista completa — em resumo: **sem mapa embebido** (o Maps
  SDK é grátis mas pede projeto Google Cloud com faturação e chave) — a página mostra a **morada em texto** (`produtor_morada`,
  patch `14`) e "Abrir no mapa" abre a app de mapas (pelas coordenadas, se houver; senão pela morada); o **catálogo do produtor é
  o próprio `CatalogScreen`** com o filtro `produtorId` (pesquisa + filtros, sem país/região); falta a secção "Produtor" no popup
  do catálogo geral (só compensa com centenas de produtores); o rating geral é a média ponderada pelas reviews (calculado no backend); **nenhum produtor real tem ainda
  história, morada, coordenadas nem imagem** (a página esconde o que não existe) — é dado a preencher à mão; o `regiaoId` do produtor
  ainda não é usado. Dívida vista de passagem: `BebidaCard` diz "1 reviews" (sem singular) e as linhas de `ProvadasScreen`
  não abrem o popup de detalhe.
- **Recuperar password:** o código já chega por email (backend, 2026-09-22; testado com Mailpit local — ver `../CLAUDE.md`); o
  temporizador de validade com "pedir código novo" fica para depois (pede backend, ver `design/README.md`).
- **Onboarding:** os pedidos só se enviam no último ecrã, por isso, se a app for fechada a meio, perde-se o que estava por guardar. O
  estado de erro das listas ("TENTAR DE NOVO") não foi exercitado ao vivo (as listas carregam todas no arranque do onboarding).
- Uma resposta 401 **durante** a utilização (token expirado a meio) ainda não leva ao login (só no arranque).
- O nome "AQUAVITAE" é texto Inter; falta o **SVG do logótipo**. A fonte **Inter** (OFL) precisa de aviso de licença antes de publicar.
- "TERMOS E CONDIÇÕES" e "LER OS TERMOS" abrem o popup `TermsSheet` (texto de `GET /api/legal/termos`, ainda um Lorem ipsum de
  exemplo); o texto final dos termos é do utilizador.
- O `API_BASE_URL` de release é um marcador (`https://api.aquavitae.pt/`).
- Toolchain antiga (AGP 8.5.2, Kotlin 1.9.24, kapt) — funciona; a migração (AGP 9, Kotlin 2, KSP) fica para depois da 1.ª versão.
- O 1.º arranque a frio em debug é lento (o ecrã de arranque do sistema demora ~1,8 s); a reavaliar em release.

## Como documentar cada fatia (checklist de fim de fatia)

1. Este ficheiro: tabela "Estado", mapa de ecrãs, componentes novos, lacunas.
2. `design/README.md`: estado de cada ecrã e as diferenças face aos prints; copiar para `design/` os prints novos.
3. `../PLANO.md`: secção "Android — fatia N" (o que foi feito, validação, decisões abertas) e o "Retomar aqui".
4. `../backend/API_ENDPOINTS.md`: "Sincronização pendente com o Android" (o que já está na app) e qualquer contrato que mudou.
5. `../CLAUDE.md` só se se aprendeu uma convenção ou ferramenta nova.
