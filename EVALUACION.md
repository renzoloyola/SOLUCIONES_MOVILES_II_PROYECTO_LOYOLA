# Evaluacion practica: puntos 2, 3 y 5

- **2:** timeout de 3 segundos inyectado con `ConfiguracionRed` y `FabricaClienteHttp`.
- **3:** `ErroresRed.kt` transforma timeout/socket/DNS en errores de dominio. El login muestra el mensaje y apaga el spinner.
- **5:** `ConectividadTest` prueba el ViewModel y repositorio reales con dos dobles: demora de 4 segundos y `SocketException`. Verifica error, spinner apagado y ausencia de reintentos.

## Ejecutar

Requiere JDK 21 para Gradle, Android SDK 36.1 y las dependencias en cache. No requiere emulador, celular ni acceso a Supabase.

En PowerShell, desde la raiz del proyecto:

```powershell
.\scripts\test-conectividad.ps1
```

El script usa valores ficticios de Supabase para las pruebas; no modifica `local.properties`.
El timeout de las pruebas se verifica con tiempo virtual: se comprueba que a los 2999 ms sigue cargando y a los 3000 ms ya muestra error.

Para descargar las dependencias la primera vez, ejecutar el comando Gradle del script sin `--offline`. Las ejecuciones posteriores con `--offline` no requieren red.

## Archivos de evidencia

- [PDF de conectividad](output/pdf/evidencia-conectividad.pdf)
- [Comando y salida completa](output/conectividad/terminal.txt)
- [Resultados JUnit](output/conectividad/resultados.xml)
- [Codigo de las pruebas](app/src/test/java/com/app/changescout/ui/viewmodel/ConectividadTest.kt)

El PDF reproduce el codigo y la salida reales. La captura visual del terminal se agrega cuando este disponible.
Para regenerar el PDF: instalar `reportlab` y `pymupdf`, y ejecutar `python scripts/crear-evidencia-conectividad.py`.

## Antes de subir a GitHub

Incluir los fuentes, scripts y evidencias. `local.properties`, `tmp/` y los directorios `build/` estan excluidos de Git.
No editar ni subir `ChangeScoutDatabase_Impl.kt`: Room/KSP lo genera automaticamente.
El limite de 3 segundos es el solicitado para la evaluacion; para consultas largas de marketplace/NLP se puede ajustar en `ModulosChangeScout`.
