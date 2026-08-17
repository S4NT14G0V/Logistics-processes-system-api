# Backend Courier Sync Feature 4

**Sistema Web para Optimización de Procesos Logísticos**

API de una empresa de paquetería (bodega a bodega) que optimiza el transporte y la
distribución: ciclo de vida de paquetes, rastreo, ubicaciones, catálogo, inventario,
usuarios y notificaciones en tiempo real.

**Feature** — *In-Transit Inventory Control*: monitorear paquetes en todas las
etapas del transporte, minimizando pérdidas y errores.

## Stack

- **Java 17** + **Spring Boot 3.4**
- **GraphQL** (`/graphql`) para queries/mutations + **REST** (`/auth/*`) para autenticación
- **PostgreSQL** + **Flyway** (migraciones en `db/migration`)
- **JWT** (access + refresh token) con roles y permisos (`:all`/`:own`)
- **MapStruct** para mapear entidad ↔ DTO
- **SSE** para notificaciones en tiempo real
- **Rate limiting** con Bucket4j (en memoria o Redis) + límites de complejidad GraphQL
- **Testcontainers** para tests de integración con Postgres real

## Configuración

Crea un archivo `.env` en la raíz:

```env
SERVER_PORT=8080
FRONTEND_BASE_URL=http://localhost:3000
BACKEND_BASE_URL=http://localhost:8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mydatabase
DB_USER=myuser
DB_PASSWORD=mypassword
DB_SSL_MODE=disable
JWT_SECRET=<clave Base64>
ACCESS_TOKEN_TTL=900000
REFRESH_TOKEN_TTL=2592000000
ADMIN_EMAIL=test@gmail.com
```

- `JWT_SECRET` debe ser una clave **Base64**.
- `ADMIN_EMAIL`: el email que recibe el rol `ADMIN` al registrarse vía `/auth/register`.

## Cómo ejecutar

```shell
mvn spring-boot:run
```

- GraphiQL: `http://localhost:8080/graphiql`
- Endpoint GraphQL: `POST /graphql`

## Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registro (ADMIN si el email coincide con `ADMIN_EMAIL`) |
| `POST` | `/auth/login` | Login → `{ accessToken, refreshToken, changePasswordRequired }` |
| `POST` | `/auth/refresh` | Renovar tokens con `refreshToken` |
| `POST` | `/auth/logout` | Revocar `refreshToken` |
| `POST` | `/auth/change-password` | Cambiar contraseña (requiere JWT) |

Para GraphQL y los endpoints protegidos, envía:

```
Authorization: Bearer <accessToken>
```

> Los permisos viajan en el JWT; si cambias permisos en la BD, vuelve a hacer login.

## Notificaciones en tiempo real (SSE)

El backend emite eventos al **dueño de cada paquete** (`package.*`) y a los
destinatarios de alertas (`alert.created`) vía **Server-Sent Events**.

```
GET /events
Authorization: Bearer <accessToken>
```

Ver [`docs/SSE.md`](docs/SSE.md) para el contrato completo y ejemplos de cliente.

> Cada usuario tiene **una única conexión SSE** activa: al reconectar, la anterior
> se cierra automáticamente.

## Rate limiting y límites de GraphQL

- **Rate limiting** (Bucket4j): `10 req/min` por IP en `/auth/login|register` y
  `120 req/min` por usuario en el resto, distribuido con Redis. Ver [`docs/RATE_LIMITING.md`](docs/RATE_LIMITING.md).
- **Límites de complejidad/profundidad** de GraphQL (`MaxQueryDepthInstrumentation`
  y `MaxQueryComplexityInstrumentation`) para evitar queries abusivas. Ver
  [`docs/GRAPHQL_LIMITS.md`](docs/GRAPHQL_LIMITS.md).

## Testing automatizado (Testcontainers)

```shell
mvn test
```

Los tests de integración levantan `postgres:18`, corren las migraciones Flyway y
ejercitan el flujo real (`register` → JWT → `@PreAuthorize`) más el round-trip de SSE.

## Documentación

- [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md) — endpoints y permisos por rol.
- [`docs/SSE.md`](docs/SSE.md) — notificaciones en tiempo real (eventos + cliente).
- [`docs/RATE_LIMITING.md`](docs/RATE_LIMITING.md) — rate limiting (Bucket4j + Redis).
- [`docs/GRAPHQL_LIMITS.md`](docs/GRAPHQL_LIMITS.md) — límites de complejidad/profundidad GraphQL.
- [`docs/TEST.md`](docs/TEST.md) — qué se testea.
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — features futuras.
- [`postman/Logistics-API.postman_collection.json`](postman/Logistics-API.postman_collection.json) — colección Postman.
