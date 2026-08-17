# TEST — Cobertura de endpoints

Suite de tests de integración que valida **todos los endpoints** (Auth REST + Package/Place GraphQL) con los dos roles (CUSTOMER = owner, ADMIN = `:all`).

## Cómo ejecutar

```shell
mvnw clean test
```

> Usar `clean` (no solo `test`) evita `.class` viejos compilados por el IDE (VS Code/JDT) que producen `Unresolved compilation problem`.

**Requisito**: Docker corriendo (se levanta un `postgres:18` con Testcontainers).

## Stack y decisión

| Aspecto | Elección |
|---|---|
| Base de datos | **Testcontainers** con `postgres:18` real (no H2) |
| Migraciones | Flyway corre `V1..V12` de verdad |
| Esquema | `ddl-auto=validate` (paridad con producción) |
| GraphQL | `HttpGraphQlTester` (tester oficial de Spring for GraphQL) |
| REST (auth) | `TestRestTemplate` |
| Seguridad | Flujo real: `register` → JWT → `JwtAuthenticationFilter` → `@PreAuthorize` |
| Validación de input | Bean Validation (`@Positive`, `@PositiveOrZero`, `@NotBlank`) + `@Valid` en `@Argument` |
| Seeder | No manual: roles/permisos/estados los siembra Flyway |

**¿Por qué Testcontainers y no H2?** H2 no ejecuta las migraciones Flyway (son Postgres: `gen_random_uuid()`, `DO $$`, `ON CONFLICT`, `uuid`, ...) y obligaba a un seeder manual de permisos/roles/estados. Testcontainers valida migraciones + esquema + endpoints con paridad total, a cambio de requerir Docker y ser un poco más lento. *(Decisión documentada en el README.)*

## Cómo se testea

- **3 archivos, una función por endpoint** (29 tests en total):
  - `AuthEndpointIntegrationTest` — 4 tests (`register`, `login`, `refresh`, `logout`).
  - `PackageEndpointIntegrationTest` — 20 tests (11 queries + 8 mutations + `fullLifecycle`).
  - `PlaceEndpointIntegrationTest` — 5 tests.
- `IntegrationTestBase` (abstract): levanta `postgres:18` (Testcontainers), cablea el datasource con `@DynamicPropertySource`, construye `HttpGraphQlTester` (apuntando al puerto random vía `@LocalServerPort`) y en `@BeforeAll` registra `admin@test.com` (ADMIN vía `app.admin.email`) y `customer@test.com` (CUSTOMER), hace login y crea 2 `place`. Un solo contenedor + un contexto cacheado para las 3 clases.
- **Decorador `@Operation`** (`/graphql{findPackageById}`, `/auth/register`, ...) + `OperationLoggerExtension`: cada test loguea el endpoint que ejercita antes de correr.
- **Contrato de respuesta** con `HttpGraphQlTester`: `.path("findPackageById.status.code").entity(String.class).isEqualTo(...)` y helpers `assertPackageShape`/`assertTrackingShape`/`assertPlaceShape`. En los negativos, `.errors().expect(err -> ...)` con `expectForbidden` / `expectErrorCode`.
- **Casos negativos de validación de input** (Bean Validation): `weightKg < 0`, `lengthCm = 0`, `PlaceInput.name` vacío → error `BAD_REQUEST`.
- **Aislamiento entre tests**: el contenedor es estático (compartido entre clases) y no hay rollback, así que los datos se acumulan; las aserciones de conteo son **estructurales** (presencia/tipo), no valores exactos. Trade-off de velocidad. *(Si en el futuro se quieren cantidades exactas, añadir `TRUNCATE` en `@AfterAll`.)*

## Auth (REST)

| # | Endpoint | Rol | Esperado | Check |
|---|---|---|---|---|
| 1 | `POST /auth/register` | — | `{ accessToken, refreshToken }` | <input type="checkbox" checked> |
| 2 | `POST /auth/login` | — | `{ accessToken, refreshToken }` | <input type="checkbox" checked> |
| 3 | `POST /auth/refresh` | — | `{ accessToken, refreshToken }` | <input type="checkbox" checked> |
| 4 | `POST /auth/logout` | — | `204 No Content` | <input type="checkbox" checked> |

## Queries de Package

| # | Endpoint | Rol | Esperado | Check |
|---|---|---|---|---|
| 1 | `findAllPackages` | customer | solo sus paquetes | <input type="checkbox" checked> |
| 2 | `findPackageById` | customer | propio ok; ajeno → `Forbidden` | <input type="checkbox" checked> |
| 3 | `findPackageByTrackingCode` | público (sin token) | responde `PackageTrackingResponse` | <input type="checkbox" checked> |
| 4 | `findPackageHistory` | customer | historial propio | <input type="checkbox" checked> |
| 5 | `findPackagesByDateRange` | customer | solo suyos | <input type="checkbox" checked> |
| 6 | `findPackageCountByUserId` | customer | propio ok; ajeno → `Forbidden` | <input type="checkbox" checked> |
| 7 | `findPackagesByStatusIn` | customer | con estados; `null` → todos | <input type="checkbox" checked> |
| 8 | `findPackageCountByAllUsers` | admin  / customer  | `{ users, totalPackages }` | <input type="checkbox" checked> |
| 9 | `findPackageCountByAllStatus` | admin  / customer  | `[ { statusCode, count } ]` | <input type="checkbox" checked> |
| 10 | `findAllPackagesByUserId` | admin/customer | propio ok; ajeno → `Forbidden` | <input type="checkbox" checked> |
| 11 | `findAllPackagesByPlace` | customer | solo suyos (filtra por dueño) | <input type="checkbox" checked> |

## Mutations de Package

| # | Endpoint | Rol | Esperado | Check |
|---|---|---|---|---|
| 1 | `createPackage` | admin  / customer  | crea (`ownerUserId` opcional) | <input type="checkbox" checked> |
| 2 | `proposePackage` | customer  / admin  | `PROPOSED` + precio | <input type="checkbox" checked> |
| 3 | `approvePackage` | admin | `PROPOSED → CREATED` | <input type="checkbox" checked> |
| 4 | `rejectPackage` | admin | `PROPOSED → CANCELLED` | <input type="checkbox" checked> |
| 5 | `reactivatePackage` | admin | `CANCELLED → CREATED` | <input type="checkbox" checked> |
| 6 | `updatePackage` | owner/admin | en `CREATED` ok; `PROPOSED` → error; ajeno → `Forbidden` | <input type="checkbox" checked> |
| 7 | `cancelPackage` | owner/admin | ok si no `DELIVERED`/`CANCELLED`; `DELIVERED` → error | <input type="checkbox" checked> |
| 8 | `changePackageStatus` | admin  / customer  | `IN_TRANSIT` → `DELIVERED` | <input type="checkbox" checked> |

## Places

| # | Endpoint | Rol | Esperado | Check |
|---|---|---|---|---|
| 1 | `findAllPlaces` | customer | ok | <input type="checkbox" checked> |
| 2 | `findPlaceByUuid` | customer | ok | <input type="checkbox" checked> |
| 3 | `createPlace` | admin  / customer  | crea | <input type="checkbox" checked> |
| 4 | `updatePlace` | admin | actualiza | <input type="checkbox" checked> |
| 5 | `deletePlace` | admin | elimina | <input type="checkbox" checked> |
