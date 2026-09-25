# Corre os scripts SQL (ddl + seed) dentro do container Oracle XE, por ordem.
# Uso: pwsh ./run-migrations.ps1  (ou powershell ./run-migrations.ps1)
# Pré-requisitos: container "aquavitae-oracle-xe" já em execução e saudável
# (docker compose up -d ; docker compose ps  -> deve mostrar "healthy").

$ErrorActionPreference = "Stop"

# Sem isto, o PowerShell 5.1 injeta um BOM UTF-8 no início do stdin do
# processo nativo (sqlplus vê-lo como um comando desconhecido — inofensivo,
# mas gera um SP2-0734 em cada script).
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Error "Falta o ficheiro .env (copia .env.example para .env primeiro)."
    exit 1
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') {
        Set-Item -Path "env:$($matches[1].Trim())" -Value $matches[2].Trim()
    }
}

$container = "aquavitae-oracle-xe"
$connectString = "$env:ORACLE_APP_USER/$env:ORACLE_APP_PASSWORD@//localhost:1521/XEPDB1"

$scripts = @(
    "ddl/01_tables.sql",
    "ddl/02_constraints.sql",
    "ddl/03_triggers.sql",
    "seed/01_lookups.sql",
    "seed/02_bebidas.sql"
)

foreach ($script in $scripts) {
    $path = Join-Path $PSScriptRoot $script
    Write-Host "==> A correr $script ..." -ForegroundColor Cyan
    # NLS_LANG diz ao sqlplus que os bytes recebidos via stdin já são UTF-8 (a BD é
    # AL32UTF8) — sem isto, acentos ficam corrompidos (mojibake) nos INSERTs de texto
    # com "ã", "ç", "é", etc. Apanhado e corrigido em 2026-09-17 (ver PLANO.md).
    # -Encoding UTF8: os .sql não têm BOM e no Windows PowerShell 5.1 (o único instalado nesta máquina)
    # Get-Content lê ficheiros sem BOM como ANSI — sem isto, os acentos chegam ao sqlplus já
    # codificados duas vezes ("França" -> "FranÃ§a"), mesmo com NLS_LANG certo. Corrigido em 2026-09-19.
    Get-Content $path -Raw -Encoding UTF8 | docker exec -i -e NLS_LANG=AMERICAN_AMERICA.AL32UTF8 $container sqlplus -s "$connectString"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falhou a correr $script"
        exit 1
    }
}

Write-Host "Concluído. Schema + seed aplicados em XEPDB1." -ForegroundColor Green
