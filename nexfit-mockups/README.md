# NexFit — Mockups en Angular

Mockups navegables de NexFit construidos con **componentes standalone de Angular**
(sin NgModules), pensados para integrarse directamente como base del frontend
real del proyecto. Usan datos de ejemplo en memoria (arrays y objetos dentro de
cada componente) en lugar de llamadas a una API, así que ya tienen la forma
que tendrá el código final: solo falta conectar un servicio HTTP donde hoy hay
un array mockeado.

## Pantallas incluidas

| Pantalla | Ruta | Componente |
|---|---|---|
| Inicio de sesión | `/login` | `login/` |
| Dashboard | `/inicio` | `dashboard/` |
| Lista de rutinas (con filtros) | `/rutinas` | `rutinas/` |
| Entrenamiento activo | `/rutinas/:id` | `rutina-detalle/` |
| Progreso (gráficos) | `/progreso` | `progreso/` |
| Perfil | `/perfil` | `perfil/` |

**Flujo principal:** Login → Dashboard ("Comenzar entrenamiento") →
Entrenamiento activo (marcar ejercicios, cronómetro) → "Finalizar
entrenamiento" → Progreso. La navegación inferior (`shared/bottom-nav`)
conecta Inicio, Rutinas, Progreso y Perfil en todo momento.

## Cómo ponerlo a correr

Estos archivos son la carpeta `src/` de un proyecto Angular. Para probarlos:

```bash
npm install -g @angular/cli
ng new nexfit --standalone --style=css --routing=false
# Cuando pregunte, no es necesario mantener el routing generado: se reemplaza abajo.
```

Luego reemplaza el contenido de `src/` del proyecto recién creado por el
contenido de esta carpeta, y ejecuta:

```bash
cd nexfit
npm start
```

## Paleta y sistema de diseño

Todo el look institucional (amarillo/negro UPTC) vive centralizado en
`src/styles.css` como variables CSS (`--nf-yellow`, `--nf-black`, etc.), para
que un cambio de marca no implique tocar cada componente:

- `--nf-yellow: #FFC800` — acento principal, botones de acción y estados activos.
- `--nf-black: #000000` — superficies de énfasis (tarjeta del día, navegación, encabezados).
- `--nf-bg` / `--nf-surface` / `--nf-line` — neutros de soporte para que el
  amarillo y el negro no compitan entre sí en pantallas con mucho contenido.
- Tipografía: **Anton** para títulos (impacto, carácter deportivo) y
  **Work Sans** para texto de interfaz (legibilidad en pantallas pequeñas).

## Siguientes pasos sugeridos

1. Sustituir los arrays de ejemplo (`rutinas`, `usuario`, `peso`, etc.) por
   llamadas a servicios (`HttpClient`) contra la API real.
2. Añadir un `AuthGuard` en `app.routes.ts` para proteger las rutas distintas de `/login`.
3. Construir la pantalla de registro (hoy el enlace "Crea tu cuenta" es un
   placeholder que apunta al dashboard).
4. Revisar accesibilidad de color: los textos amarillo-sobre-negro y
   negro-sobre-amarillo ya cumplen contraste AA, pero conviene re-validar tras
   cualquier ajuste de tono.
