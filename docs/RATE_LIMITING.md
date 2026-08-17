# Rate Limiting — cómo está implementado (y con Redis)

Protege la API de abuso (fuerza bruta en login, sobreuso de GraphQL, etc.)
limitando cuántas peticiones puede hacer un cliente en un periodo de tiempo.

## 1. Qué es rate limiting

Consiste en poner un **tope de peticiones por ventana de tiempo** a cada
"cliente" (identificado por IP o por usuario). Si se supera el tope, la API
responde `429 Too Many Requests`.

## 2. Algoritmo: token bucket

Se usa **Bucket4j**, que implementa el algoritmo de *token bucket*:

- Cada "bucket" (balde) tiene una **capacidad** (`capacity`) de tokens.
- Cada petición **consume 1 token** (`tryConsume(1)`).
- Los tokens se **reabastecen** a un ritmo fijo (`refillGreedy(capacity, ventana)`):
  cada `ventana` de tiempo se restauran `capacity` tokens.

Ejemplo: `capacity=10`, ventana `1m` → 10 peticiones por minuto. La petición
nº 11 dentro del mismo minuto recibe `429`; al minuto siguiente se "recargan"
los 10 tokens.

## 3. Cómo está implementado aquí

### Capas

| Clase | Rol |
|---|---|
| `RateLimitProperties` | Config (`app.rate-limit.*`): capacidades, ventana, límites GraphQL |
| `RateLimitFilter` | `OncePerRequestFilter` en la cadena de Spring Security, **después** del `JwtAuthenticationFilter` |
| `RateLimitBucketProvider` | Abstracción para resolver el bucket por clave |
| `RedisRateLimitBucketProvider` | Implementación distribuida con Redis |
| `RedisRateLimitConfig` | Crea el `RedisClient` de Lettuce |

### Clave (key) por petición

| Ruta | Clave | Límite |
|---|---|---|
| `/auth/login`, `/auth/register` | `auth:ip:<ip>` | `auth-capacity` (10/min) |
| `/graphql/**` y resto | `user:<email>` (o `ip:<ip>` si anónimo) | `api-capacity` (120/min) |
| `/events` (SSE) | — (se salta) | 1 conexión por usuario (en `SseEmitterService`) |

- La IP se toma de `X-Forwarded-For` (o `getRemoteAddr()` si no viene).
- El email sale del `SecurityContext` (poblado por el JWT), por eso el filtro va
  **después** del `JwtAuthenticationFilter`.

### Config (`application.properties`)

```properties
app.rate-limit.enabled=true
app.rate-limit.redis-url=${REDIS_URL:redis://localhost:6379}
app.rate-limit.auth-capacity=10        # login/register por IP
app.rate-limit.api-capacity=120        # graphql/rest por usuario
app.rate-limit.refill-duration=1m      # ventana de recarga
app.rate-limit.graphql-max-depth=10
app.rate-limit.graphql-max-complexity=100
```

## 4. Redis (distribuido)

`RedisRateLimitBucketProvider` usa `bucket4j-redis`: el **estado del bucket vive
en Redis**, así que todas las instancias comparten el mismo contador.

Cómo funciona `bucket4j-redis`:

1. `LettuceBasedProxyManager.builderFor(redisClient).build()` crea un
   `ProxyManager<byte[]>`.
2. `proxyManager.builder().build(key, BucketConfiguration)` devuelve un
   `BucketProxy`: un `Bucket` cuyo estado se lee/escribe en Redis.
3. Cada `tryConsume(1)` ejecuta una **operación atómica** en Redis (CAS/Lua), lo
   que garantiza que dos réplicas no "gasten" el mismo token.

### Cómo levantar Redis

Con el `compose.yaml` del proyecto ya se incluye un servicio `redis:7-alpine` en
la misma network que el backend (`REDIS_URL=redis://redis:6379`):

```shell
docker compose up -d
```

O local, sin Docker:

```shell
docker run -d -p 6379:6379 redis:7
```

## 5. Respuesta `429`

El filtro escribe directamente:

```json
{"code":"TOO_MANY_REQUESTS","status":429,"message":"Too many requests"}
```

## 6. Notas de testing

- En la suite de integración el rate limit se **desactiva**
  (`app.rate-limit.enabled=false` en `application-integration.properties`) para
  que los tests no sean flaky.
- El comportamiento de `429` se cubre con `RateLimitFilterTest` (unit, con un
  bucket de capacidad 1) en vez de un test HTTP completo.
