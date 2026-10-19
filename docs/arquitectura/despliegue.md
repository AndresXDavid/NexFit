# Despliegue del backend (TT-09, NEX-16)

## Decisión: Azure Container Apps en lugar de Google Cloud Run

El backend se despliega en **Azure Container Apps** (Azure for Students, grupo de recursos `nexfit-rg`, región `eastus2`, ya creado), no en Google Cloud Run como dicen TT-04 y el SRS 2.4.

**Razón:** Google Cloud exige en Colombia un prepago de COP 100.000 que el equipo no puede pagar; Azure for Students no pide tarjeta.

> **Pendiente:** actualizar TT-04 y el SRS 2.4 (y el comentario «Cloud Run» de `application.yml`, el `Dockerfile` y `docs/CONVENCIONES.md`) para reflejar Azure.

## Arquitectura

```
Navegador ──HTTPS──> Vercel (frontend Angular)
                       │  rewrite /api/*
                       ▼
                 Azure Container Apps (backend Spring Boot, :8080)
                       │  JDBC + TLS
                       ▼
                     Neon (PostgreSQL)
```

El navegador solo habla con Vercel; la cookie de sesión es de ese dominio, por eso no hay CORS.

## Variables de entorno

Sin valores en el repositorio. Las sensibles se guardan como **secretos de Container Apps** y se referencian con `secretref:`.

| Variable | Tipo | Notas |
| -- | -- | -- |
| `DB_URL` | secreto | URL JDBC de Neon con `?sslmode=require` |
| `DB_USER` | secreto | |
| `DB_PASSWORD` | secreto | |
| `DB_POOL_SIZE` | variable | por defecto 5 |
| `COOKIE_SECURE` | variable | `true` en producción |
| `PORT` | variable | `8080` (debe coincidir con el puerto del ingress) |

## Pasos con `az`

Los comandos de esta sección **no se han ejecutado todavía**: están tomados de la CLI de Azure y deben comprobarse con `az containerapp <comando> --help` antes de usarlos. Los marcados «sin verificar» tienen además opciones cuya sintaxis exacta no se ha confirmado.

```bash
# 1. Extensión y proveedores (sin verificar)
az extension add --name containerapp --upgrade
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.OperationalInsights

# 2. Entorno de Container Apps en el grupo existente (sin verificar)
az containerapp env create --name nexfit-env --resource-group nexfit-rg --location eastus2

# 3. App desde backend/Dockerfile (sin verificar): construye la imagen en la nube a partir del código fuente
az containerapp up --name nexfit-api --resource-group nexfit-rg --environment nexfit-env \
  --source ./backend --ingress external --target-port 8080

# 4. Secretos y variables (sin verificar)
az containerapp secret set --name nexfit-api --resource-group nexfit-rg \
  --secrets db-url=<...> db-user=<...> db-password=<...>
az containerapp update --name nexfit-api --resource-group nexfit-rg \
  --set-env-vars DB_URL=secretref:db-url DB_USER=secretref:db-user DB_PASSWORD=secretref:db-password \
                 DB_POOL_SIZE=5 COOKIE_SECURE=true PORT=8080

# 5. Réplicas (sin verificar): máximo 1; mínimo por decidir (ver abajo)
az containerapp update --name nexfit-api --resource-group nexfit-rg --min-replicas 0 --max-replicas 1

# 6. Ingress solo HTTPS (sin verificar el nombre exacto de la opción)
az containerapp ingress update --name nexfit-api --resource-group nexfit-rg --allow-insecure false

# 7. URL pública (la que va en el rewrite de vercel.json)
az containerapp show --name nexfit-api --resource-group nexfit-rg \
  --query properties.configuration.ingress.fqdn -o tsv
```

**Réplicas.** Máximo 1. Mínimo 0 (gratis, pero el primer acceso tras inactividad paga el arranque en frío de Spring Boot) o 1 (sin arranque en frío, consume cuota). Decidirlo midiendo el tiempo de arranque en frío con `curl -w '%{time_total}'` sobre `/api/actuator/health` tras varios minutos sin tráfico.

**IP del cliente.** `application.yml` usa `server.forward-headers-strategy: native` y `server.tomcat.remoteip.internal-proxies: .*` para que `getRemoteAddr()` (y, por tanto, `audit_events.ip_address`) sea la primera IP de `X-Forwarded-For`, la del cliente, y no la del proxy.

## Monitor externo y cálculo del 95 % (RNF-04)

- **URL:** `https://<dominio>/api/actuator/health` (sin tocar la BD: `management.health.db.enabled=false`, así el monitor no despierta Neon).
- **Frecuencia:** cada 5 minutos, desde un servicio externo de monitoreo HTTP (que no esté en Azure), esperando HTTP 200 y con un tiempo de espera mayor que el arranque en frío.
- **Cálculo:** disponibilidad (%) = comprobaciones con éxito ÷ comprobaciones totales × 100, sobre una ventana fija (p. ej. 30 días). A 5 min son 288 comprobaciones al día y 8.640 en 30 días; el 95 % exige al menos 8.208 correctas, es decir, como máximo 432 fallidas (≈ 36 h de caída acumulada). Si el mínimo de réplicas es 0, los fallos por arranque en frío cuentan contra el 95 %.

## Riesgo aceptado

Si se llama al backend directamente, sin pasar por Vercel, `X-Forwarded-For` puede falsearse y la IP de auditoría sería falsa. Se acepta porque es un proyecto académico, los rangos de Vercel no son fijos (no se puede listar como proxy de confianza) y la IP solo se audita: no decide permisos.

## Verificación pendiente tras desplegar

- [ ] `http://<dominio>/...` (sin cifrar) no expone la app: redirige a HTTPS o se rechaza.
- [ ] A través de Vercel, el `LOGIN` de `audit_events` guarda la IP del cliente real, no la de Vercel ni la de Azure.
- [ ] **Bloqueante conocido:** el login desde Angular/Vercel no funcionará hasta corregir la cookie `XSRF-TOKEN` (el servidor no la emite porque `CsrfCookieFilter` corre antes que `CsrfFilter`; issue aparte, fuera de NEX-16).
- [ ] El monitor devuelve 200 y se mide el arranque en frío para decidir el mínimo de réplicas.
