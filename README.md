# ChangeScout

**Curso:** SI-988 · Soluciones Móviles II

**Institución:** Universidad Privada de Tacna

ChangeScout es una propuesta de aplicación móvil Android nativa orientada a apoyar las decisiones comerciales de micro-importadores que compran productos en dólares estadounidenses (USD) y los venden en soles peruanos (PEN).

## Problema

El costo real de un producto importado no se limita a su precio de compra: también depende del flete, seguro, aranceles, otros cargos y el tipo de cambio USD/PEN. Además, comparar manualmente publicaciones de marketplaces consume tiempo y puede producir conclusiones engañosas cuando los resultados incluyen artículos usados, réplicas, accesorios, repuestos o combos.

ChangeScout busca reunir el cálculo del costo en destino (*landed cost*), precios comparables, historial y tendencias para emitir un veredicto comercial claro que ayude a decidir si conviene mantener, ajustar, reponer o liquidar un producto.

## Usuarios objetivo

- Micro-importadores y pequeños comerciantes que compran en USD y venden en PEN.
- En una primera etapa, comerciantes de Tacna.

## Tecnologías previstas

- Aplicación Android nativa con Kotlin y Jetpack Compose.
- Arquitectura MVVM con dominio explícito.
- Room, Retrofit, Hilt, Coroutines y StateFlow/SharedFlow.
- Backend con Ktor.
- Supabase Auth con JWT para autenticación.
- Integración de marketplace mediante Apify/MercadoLibre a través del backend.
- Procesamiento NLP con Groq a través del backend.

Estas tecnologías forman parte de la planificación y no implican que ya estén implementadas o verificadas en este repositorio.

## Integrantes

| Integrante | Código |
| --- | --- |
| Loyola Vilca, Renzo Fernando | 2021072615 |

## Estructura del repositorio

```text
.
├── .github/workflows/       # Automatización de integración continua
├── app/                     # Aplicación Android (pendiente de incorporación)
├── docs/
│   ├── arquitectura/        # Documentación futura de arquitectura
│   ├── decisiones/          # Registros de decisiones de arquitectura (ADR)
│   ├── entorno/             # Verificación del entorno de desarrollo
│   ├── equipo/              # Equipo Scrum y acuerdos de trabajo
│   ├── evidencias/          # Evidencias organizadas por taller
│   ├── producto/            # Lean Canvas, validación y visión
│   └── sprints/             # Documentación futura de sprints
├── src/                     # Código fuente futuro
├── test/                    # Pruebas futuras
├── CONTRIBUTING.md          # Convenciones de colaboración
└── README.md
```

## Estado académico

El proyecto está siendo adaptado desde Soluciones Móviles I para evolucionarlo durante el curso Soluciones Móviles II. En esta etapa, el repositorio contiene principalmente documentación inicial; no se afirma que la aplicación, las entrevistas, las evidencias ni la integración continua hayan sido completadas.

## Seguridad

Ningún secreto debe versionarse. Las credenciales y claves se administrarán mediante variables de entorno o mecanismos seguros equivalentes. El repositorio debe mantenerse privado mientras contenga trabajo no publicado y las cuentas del equipo deben protegerse con autenticación de dos factores (2FA).
