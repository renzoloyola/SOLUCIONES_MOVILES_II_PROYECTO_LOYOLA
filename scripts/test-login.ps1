$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:DEBUG = $null
New-Item -ItemType Directory -Force -Path 'output/login' | Out-Null
$comando = '.\gradlew.bat :app:testDebugUnitTest --tests com.app.changescout.data.auth.LoginTest --offline --rerun-tasks --console=plain -PSUPABASE_URL=https://login.example.test -PSUPABASE_PUBLISHABLE_KEY=sb_publishable_prueba'
"PS> $comando" | Tee-Object -FilePath 'output/login/terminal.txt'
& .\gradlew.bat :app:testDebugUnitTest --tests com.app.changescout.data.auth.LoginTest --offline --rerun-tasks --console=plain -PSUPABASE_URL=https://login.example.test -PSUPABASE_PUBLISHABLE_KEY=sb_publishable_prueba 2>&1 | Tee-Object -FilePath 'output/login/terminal.txt' -Append
$codigo = $LASTEXITCODE
"Codigo de salida: $codigo" | Tee-Object -FilePath 'output/login/terminal.txt' -Append
Write-Host "`nComando ejecutado:"
Write-Host $comando
if ($codigo -ne 0) { throw "Las pruebas fallaron: $codigo" }
Copy-Item 'app/build/test-results/testDebugUnitTest/TEST-com.app.changescout.data.auth.LoginTest.xml' 'output/login/resultados-login.xml'
