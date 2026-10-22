# API

Documentación de los endpoints REST de `backend/` (rutas, roles permitidos, entradas y salidas). Se actualiza en el mismo PR que cambia la API.

## Información personal propia (NEX-27, US-17)

Rutas bajo `/api/me`. Basta una sesión válida (todos los roles); el usuario sale de la sesión y el establecimiento del activo, nunca de la ruta ni del cuerpo.

| Ruta | Quién | Qué hace |
|---|---|---|
| `GET /api/me` | Todos | Devuelve `identity` (documento, nombre, fecha de nacimiento, correo; solo lectura) y `affiliation` en el establecimiento activo (`organizationId`, `organizationName`, `roles`, `phone`, `epsId`, `epsName`, `emergencyContacts`). Para el master, `affiliation` es `null`. |
| `PATCH /api/me` | Todos (EPS y contactos, solo Cliente) | Cambia solo lo que venga: `phone` (todos), `epsId` y `emergencyContacts` (Cliente). `emergencyContacts` reemplaza la lista completa: agregar, editar o quitar un contacto es enviar la lista resultante. Devuelve el perfil actualizado. |

Cuerpo de `PATCH`: `{"phone": "3101234567", "epsId": 3, "emergencyContacts": [{"fullName": "…", "relationship": "…", "phone": "…", "primary": true}]}`.

Errores:
- `403 ACCESS_DENIED`: el cuerpo trae un campo que no se puede modificar (identidad, establecimiento, estado, ids de otra persona; RN-34), o `epsId` o `emergencyContacts` sin ser Cliente, o no hay establecimiento activo.
- `400`: dato inválido, EPS inexistente o inactiva, EPS vacía o lista de contactos vacía (RN-25), más de un contacto principal o más de 10 contactos.
- `401` sin sesión.
