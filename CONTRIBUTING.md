# Guía de contribución

## Flujo de ramas

- `main`: rama estable y protegida; los cambios se integran mediante Pull Request.
- `develop`: rama de integración del trabajo aprobado.
- `feature/<US-xx>-descripcion`: desarrollo de una historia de usuario o funcionalidad.
- `fix/<descripcion>`: corrección de un defecto.
- `chore/<descripcion>`: mantenimiento, configuración o tareas sin funcionalidad de producto.

Las ramas de trabajo parten de `develop` y regresan a ella mediante Pull Request. La promoción de `develop` a `main` también se realiza mediante Pull Request.

## Commits convencionales

Formato:

```text
<tipo>(<alcance>): <descripción> [US-xx]
```

Tipos permitidos:

- `feat`: nueva funcionalidad.
- `fix`: corrección de un defecto.
- `docs`: documentación.
- `style`: cambios de formato sin alterar el comportamiento.
- `refactor`: reestructuración sin cambiar el comportamiento esperado.
- `test`: incorporación o modificación de pruebas.
- `chore`: mantenimiento y configuración.

Ejemplo:

```text
feat(auth): agregar inicio de sesión con PKCE [US-07]
```

## Pull Requests

Cada Pull Request debe:

- estar vinculado a una historia, incidencia o trabajo identificable;
- describir el objetivo, alcance y forma de verificación;
- mantener la integración continua en verde antes de integrarse;
- recibir revisión de un integrante distinto del autor;
- cumplir la Definition of Done vigente;
- evitar credenciales, datos personales innecesarios y cualquier otro secreto.

Mientras no exista un segundo revisor confirmado, la asignación de revisión queda `[POR CONFIRMAR]` y no debe presentarse como completada.
