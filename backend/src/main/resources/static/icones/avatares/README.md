# Avatares AquaVitae

27 avatares de utilizador, 9 por categoria. Desenhos de linha em SVG, fundo transparente.

```
avatares-aquavitae/
├── Castas/     9 ficheiros  casta-*.svg
├── Garrafas/   9 ficheiros  garrafa-*.svg
└── Copos/      9 ficheiros  copo-*.svg
```

## Especificação do desenho

| Propriedade | Valor |
|---|---|
| viewBox | `0 0 100 100` |
| Traço | `#5A1E1E`, largura `3.2`, `linecap`/`linejoin` = `round` |
| Preenchimento interior | `#FFF3EE` (só em bagos, folha e formas fechadas) |
| Fundo | transparente (o círculo vem do container, não do SVG) |

Para mudar a cor do traço em runtime, substituir `stroke="#5A1E1E"` por `stroke="currentColor"` e definir a cor no elemento pai.

## Escalas

| Contexto | Círculo | SVG |
|---|---|---|
| Perfil | 96 dp | 66 dp |
| Review / comentário | 56 dp | 38 dp |
| Lista / linha compacta | 40 dp | 27 dp |

O SVG ocupa **68%** do diâmetro do círculo. Container: `background #F4EDE4`, `border 1.5dp #EFE0DD`, `border-radius 50%`.

## Estado selecionado

- Não selecionado: borda `1.5dp #EFE0DD`
- Selecionado: borda `2.5dp #9A3324`, mais halo duplo — `0 0 0 4dp #FBF7F2` seguido de `0 0 0 5.5dp #EFE0DD`
- Em fundo bordeaux (`#5A1E1E`): o traço inverte para creme (`#FFF3EE`)

## Tabela `utilizador_avatar`

| Coluna | Conteúdo |
|---|---|
| `avatar_name` | Nome visível (ver lista abaixo) |
| `avatar_path_image` | `icones/avatares/<slug>.svg` |
| `avatar_category` | `casta` \| `garrafa` \| `copo` |
| `avatar_is_active` | filtro do seletor — `false` esconde do ecrã de escolha sem apagar de quem já o usa |

O `slug` é o nome do ficheiro sem extensão e já contém a categoria como prefixo, por isso `avatar_category` é derivável do path — mantém-se como coluna própria para permitir ordenar e agrupar sem parsing.

## Lista completa

### Castas (`casta`)
| slug | avatar_name |
|---|---|
| `casta-cacho-conico` | Cacho cónico |
| `casta-cacho-cilindrico` | Cacho cilíndrico |
| `casta-cacho-alado` | Cacho alado |
| `casta-cacho-solto` | Cacho solto |
| `casta-bago` | Bago |
| `casta-folha` | Folha de videira |
| `casta-gavinha` | Gavinha |
| `casta-ramo` | Ramo |
| `casta-videira` | Videira |

### Garrafas (`garrafa`)
| slug | avatar_name |
|---|---|
| `garrafa-bordalesa` | Bordalesa |
| `garrafa-borgonhesa` | Borgonhesa |
| `garrafa-flauta` | Flauta |
| `garrafa-champanhe` | Champanhe |
| `garrafa-garrafao` | Garrafão |
| `garrafa-whisky` | Whisky |
| `garrafa-gin` | Gin |
| `garrafa-cerveja` | Cerveja |
| `garrafa-licor` | Licor |

### Copos (`copo`)
| slug | avatar_name |
|---|---|
| `copo-tulipa` | Tulipa |
| `copo-porto` | Copo de Porto |
| `copo-balao` | Balão |
| `copo-baixo` | Copo baixo |
| `copo-flute` | Flute |
| `copo-calice` | Cálice |
| `copo-gin` | Copo de gin |
| `copo-caneca` | Caneca |
| `copo-shot` | Shot |

## Notas

- As castas distinguem-se por **forma de cacho** e motivos de vinha, não por variedade. Castas específicas (Touriga Nacional, Alvarinho, …) não são distinguíveis em silhueta de linha.
- Nomes de ficheiro sem acentos nem espaços — seguros como identificadores de recurso Android (`casta_cacho_conico` se converteres para `res/drawable`, trocando `-` por `_`).
