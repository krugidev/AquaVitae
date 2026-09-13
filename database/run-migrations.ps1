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
    Get-Content $path -Raw | docker exec -i $container sqlplus -s "$connectString"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falhou a correr $script"
        exit 1
    }
}

Write-Host "Concluído. Schema + seed aplicados em XEPDB1." -ForegroundColor Green
