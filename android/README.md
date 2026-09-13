# AquaVitae — Android

Esqueleto do projeto Android (Kotlin + Jetpack Compose) da app **AquaVitae**
(catalogação e comunidade de vinhos e bebidas espirituosas). Este módulo cobre
a estrutura do projeto, a arquitetura MVVM por feature e a ligação à API —
**não** inclui UI final nem lógica de negócio completa (ver `feature/*` para
placeholders assinalados com `TODO`).

## 1. Como abrir no Android Studio

1. Abre o Android Studio (recomendado: versão Koala/2024.1 ou mais recente,
   por compatibilidade com AGP 8.5.x / Compose Compiler 1.5.14).
2. **File > Open...** e seleciona esta pasta (`android/`) como raiz do
   projeto — não a pasta `AquaVitae/` completa, só `android/`.
3. Deixa o Android Studio indexar e sugerir o upgrade automático do AGP/Gradle
   se aparecer um prompt nesse sentido (o projeto foi criado com AGP 8.5.2 /
   Gradle 8.7, que são versões estáveis à data de criação; aceitar o upgrade
   sugerido é seguro).

## 2. Gradle wrapper — passo importante no primeiro sync

Este esqueleto **não inclui** o binário `gradle/wrapper/gradle-wrapper.jar`
nem os scripts `gradlew` / `gradlew.bat`, porque não é possível gerar esse
`.jar` corretamente fora do Android Studio/Gradle. Foi deixado apenas o
ficheiro de texto `gradle/wrapper/gradle-wrapper.properties`, já configurado
para a distribuição Gradle 8.7.

Duas formas de resolver isto, à escolha:

- **Opção A (recomendada): deixar o Android Studio tratar disto.**
  Ao abrir o projeto, se o wrapper estiver incompleto, o Android Studio
  normalmente oferece para o regenerar; caso contrário, corre
  **File > Sync Project with Gradle Files** e/ou **File > Settings > Build
  Tools > Gradle** e aponta temporariamente para uma instalação Gradle local
  ("Use Gradle from: Specified location") só para o primeiro sync — depois
  disso o wrapper fica disponível para os syncs seguintes.

- **Opção B: gerar o wrapper manualmente**, se tiveres o Gradle instalado
  localmente (`gradle -v` para confirmar):
  ```
  cd android
  gradle wrapper --gradle-version 8.7
  ```
  Isto cria `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar`
  automaticamente, respeitando a distribuição já definida em
  `gradle-wrapper.properties`.

## 3. Correr o backend local primeiro

A app aponta, em build de debug, para `http://10.0.2.2:8080/` — o alias que o
emulador Android usa para "localhost" da máquina anfitriã. Isto significa que,
antes de testar a app no emulador, o backend Spring Boot local tem de estar a
correr na porta 8080.

Ver **`../backend/README.md`** para instruções de arranque do backend (e
`../database/` para a base de dados/migrações que o backend usa). Sem o
backend a correr, os ecrãs de login/registo, catálogo, etc. vão mostrar o
estado de erro (a chamada de rede falha, mas a UI trata isso graciosamente).

Se testares num dispositivo físico em vez do emulador, `10.0.2.2` não
funciona — terás de mudar `API_BASE_URL` em `app/build.gradle.kts` (build type
`debug`) para o IP da tua máquina na rede local.

## 4. Estrutura de pastas

```
android/
├── settings.gradle.kts          Módulos incluídos (":app") e repositórios
├── build.gradle.kts             Plugins de topo (aplicados só nos módulos)
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml       Version catalog (todas as dependências)
│   └── wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts         Config do módulo app (SDKs, BuildConfig, deps)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/…             strings, themes, cores, ícone adaptativo
        │   └── kotlin/pt/aquavitae/android/
        │       ├── AquaVitaeApplication.kt   @HiltAndroidApp
        │       ├── MainActivity.kt           @AndroidEntryPoint, aloja o NavHost
        │       ├── data/
        │       │   ├── model/                DTOs (data classes @JsonClass)
        │       │   ├── network/              AquaVitaeApi, AuthInterceptor, NetworkModule
        │       │   ├── local/                TokenDataStore (DataStore Preferences)
        │       │   └── repository/           1 repositório por área (Auth, Bebida, Review, …)
        │       ├── di/                       Módulos Hilt adicionais (DataStoreModule)
        │       ├── feature/
        │       │   ├── auth/                 Login + Registo (lógica real — ecrã de referência)
        │       │   ├── onboarding/           Preferências (placeholder ligado à API)
        │       │   ├── catalog/              Lista/pesquisa de bebidas (lógica real)
        │       │   ├── detail/                Detalhe de bebida (+ secção de vinho condicional)
        │       │   ├── reviews/              Listar/submeter reviews
        │       │   ├── cave/                 Cave Virtual
        │       │   ├── wishlist/             Wishlist
        │       │   └── favoritos/            Favoritos
        │       ├── navigation/               AppDestinations + AppNavHost (Navigation Compose)
        │       └── ui/theme/                 Tema Material 3 (Color/Type/Theme.kt)
        └── debug/
            └── AndroidManifest.xml           usesCleartextTraffic="true" só em debug
```

## 5. Decisões de arquitetura tomadas neste esqueleto

- **JSON: Moshi** (com codegen via `kapt`) em vez de kotlinx.serialization —
  integra-se de forma muito direta com o `converter-moshi` do Retrofit.
- **DI: Hilt**, com `data/network/NetworkModule.kt` (Retrofit/OkHttp/Moshi) e
  `di/DataStoreModule.kt` (o `DataStore<Preferences>` usado para a sessão)
  separados por responsabilidade.
- **Sessão/token JWT**: guardado em DataStore Preferences via
  `TokenDataStore`, nunca em SharedPreferences. O `AuthInterceptor` lê o token
  (via `runBlocking`, aceitável numa thread de rede do OkHttp) e anexa
  `Authorization: Bearer <token>` a todos os pedidos — os endpoints públicos
  simplesmente ignoram o header no backend.
- **Cleartext HTTP só em debug**: `app/src/debug/AndroidManifest.xml` define
  `usesCleartextTraffic="true"` apenas nesse build type (merge de manifesto);
  o manifesto principal não define este atributo, pelo que o release mantém o
  valor por omissão (`false`, obriga HTTPS).
- **Tema Android (XML) mínimo**: `Theme.AquaVitae` usa `android:Theme.Material.Light.NoActionBar`
  (tema de plataforma) em vez de `Theme.Material3.*` da biblioteca
  `com.google.android.material`, para não introduzir essa dependência extra
  num projeto 100% Compose — a paleta/tipografia Material 3 reais vivem em
  `ui/theme/Theme.kt` (Compose).
- **`feature/wishlist/` e `feature/favoritos/`** ficaram em pacotes próprios
  (em vez de sub-ecrãs dentro de `feature/cave/`), espelhando 1:1 os
  repositórios `WishlistRepository`/`FavoritoRepository` — mantém a
  consistência "1 pacote de feature por área de repositório".
- **`auth/` e `catalog/`** foram implementados com lógica real (chamadas ao
  repositório, estados loading/sucesso/erro) conforme pedido, para servirem
  de referência do padrão a replicar nos restantes ecrãs (que ficaram como
  placeholders de UI já ligados aos respetivos repositórios/API).
- Sem ícones do Compose Material Icons (`androidx.compose.material:material-icons-*`)
  para manter a lista de dependências enxuta — os poucos sítios que
  pediriam um ícone (ex.: botão de pesquisa) usam texto simples por agora.

## 6. Notas finais

- `minSdk = 26`, `compileSdk = targetSdk = 35`.
- `applicationId` / `namespace`: `pt.aquavitae.android`.
- Nenhum ficheiro de teste unitário/instrumentado foi criado — só as
  dependências de teste (`junit`, `androidx-test-ext-junit`, `espresso-core`)
  já estão no version catalog para quando fizer sentido escrever os
  primeiros testes.
