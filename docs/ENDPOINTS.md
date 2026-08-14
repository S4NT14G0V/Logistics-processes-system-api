# ENDPOINTS — Logistics Processes System API

Documentación de los endpoints de **Package**, **Place** y **Auth**, junto con la colección de Postman.

- **Base URL**: `http://localhost:8080`
- **GraphQL endpoint**: `POST /graphql`
- **GraphiQL**: `http://localhost:8080/graphiql`
- **Auth (REST)**: `/auth/*`

---

## 1. Autenticación (REST)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| `POST` | `/auth/register` | `{ name, email, password }` | `{ accessToken, refreshToken }` |
| `POST` | `/auth/login` | `{ email, password }` | `{ accessToken, refreshToken }` |
| `POST` | `/auth/refresh` | `{ refreshToken }` | `{ accessToken, refreshToken }` |
| `POST` | `/auth/logout` | `{ refreshToken }` | `204 No Content` |

Para usar los endpoints GraphQL protegidos, envía el header:

```
Authorization: Bearer <accessToken>
```

> Los permisos viaja en el JWT junto con el rol, por lo que si cambias los permisos de un rol en la BD, **debes volver a hacer login** para obtener un token con los permisos actualizados.

---

## 2. Roles y permisos

| Permiso | CUSTOMER | ADMIN |
|---|---|---|
| `package:create:all` | <input type="checkbox"> | <input type="checkbox" checked> |
| `package:create:own` | <input type="checkbox" checked> | <input type="checkbox"> |
| `package:read:all`   | <input type="checkbox"> | <input type="checkbox" checked> |
| `package:read:own`   | <input type="checkbox" checked> | <input type="checkbox"> |
| `package:update:all` | <input type="checkbox"> | <input type="checkbox" checked> |
| `package:update:own` | <input type="checkbox" checked> | <input type="checkbox"> |
| `package:cancel:all` | <input type="checkbox"> | <input type="checkbox" checked> |
| `package:cancel:own` | <input type="checkbox" checked> | <input type="checkbox"> |

> **CUSTOMER** (usuario normal): solo opera sobre sus propios paquetes (`:own`).

> **ADMIN** (rol con permisos): opera sobre cualquier paquete (`:all`).

---

## 3. Queries de Package

| Query                        | Args                             | Permissions            | Descripción                                 | Notas |
|---                           |---                               |---                     |---                                          |---    |
| `findAllPackages`            | `page, size`                     | `read:all` / `read:own`| Lista paginada                              | `El customer ve solo sus paquetes` |
| `findPackageById`            | `id: ID!`                        | `read:all` / `read:own`| Detalle de un paquete (incluye `history`)   | `El customer ve solo sus paquetes` |
| `findPackageHistory`         | `packageId: ID!`                 | `read:all` / `read:own`| Historial de estados de un paquete          | `El customer ve solo sus paquetes` |
| `findPackageByTrackingCode`  | `trackingCode: String!`          | **Público**            | Rastreo por código           | `Devuelve PackageTrackingResponse (sin datos sensibles)` |
| `findPackagesByDateRange`    | `page, size, startDate, endDate` | `read:all` / `read:own`| Por rango de fechas          | `El customer ve solo sus paquetes` |
| `findPackageCountByUserId`   | `userId: ID!`                    | `read:all` / `read:own`| Conteo por usuario           | `El customer ve solo sus paquetes si el llamado tiene su userId` |
| `findPackagesByStatusIn`     | `page, size, packageStatuses`    | `read:all` / `read:own`| Por estados                  | `Si packageStatuses va vacío, devuelve todos.` |
| `findPackageCountByAllUsers` | —                                | `read:all`             | Conteo por cada usuario y el total  | `{ users, totalPackages } ` |
| `findPackageCountByAllStatus`| —                                | `read:all`             | Conteo por cada estado de paquete y el total  | `[ { statusCode, count } ] ` |
| `findAllPackagesByUserId`    | `page, size, userId`             | `read:all` / `read:own`| Paquetes de un usuario  | `El customer ve solo sus paquetes` |
| `findAllPackagesByPlace`     | `page, size, origin, destination`| `read:all` / `read:own`| Paquetes por origen/destino      | `El customer ve solo sus paquetes` |

---

## 4. Mutations de Package

| Mutation             | Args                   | Permissions  | Descripción                                 | Notas                                                  |
|---                   |---                     |---           |---                                          |---                                                     |
| `createPackage`      | `input: PackageInput!` | `create:all` | Crear un paquete                            | `(opcional ownerUserId para crear a nombre de otro)` |
| `proposePackage`     | `input: PackageInput!` | `create:own` | Customer propone un envío                   | `(queda PROPOSED y calcula precio)`                  |
| `approvePackage`     | `id: ID!`              | `update:all` | Se avala una propuesta                      | `PROPOSED → CREATED`                                   |
| `rejectPackage`      | `id: ID!, reason`      | `update:all` | Se rechaza una propuesta                    | `Cambia de estado de PROPOSED → CANCELLED`           |
| `reactivatePackage`  | `id: ID!`              | `update:all` | Descancela un paquete cancelado             | `Cambia de estado de CANCELLED → CREATED`            |
| `updatePackage`      | `id: ID!, input: PackageUpdateInput!` | `update:all` / `update:own` | Edita descripción/origen/destino | `Solo cambia cuando tiene el estado en CREATED` |
| `cancelPackage`      | `id: ID!, reason`      | `cancel:all` / `cancel:own` | Cancela el paquete, no lo elimina | `No se puede cancelar si está DELIVERED ni CANCELLED` |
| `changePackageStatus`| `id: ID!, statusCode: String!`        | `update:all` | Avanza la máquina de estados (solo admin). | — |

### Estados (`statusCode`)

`PROPOSED → CREATED → IN_TRANSIT → DELIVERED` · `PROPOSED/CREATED → CANCELLED`

---

## 5. Places (lugares predefinidos de la empresa)

| Operación | Args | Permissions |
|---|---|---|
| `findAllPlaces` | — | `read:all` / `read:own` |
| `findPlaceByUuid` | `uuid: ID!` | `read:all` / `read:own` |
| `createPlace` | `input: PlaceInput!` | `create:all` |
| `updatePlace` | `uuid: ID!, input: PlaceInput!` | `update:all` |
| `deletePlace` | `uuid: ID!` | `cancel:all` |

---

## 6. Qué puede hacer cada rol

### Customer (usuario normal)
- Proponer envíos (`proposePackage`) — calcula precio.
- Ver **sus** paquetes y su historial.
- Actualizar **sus** paquetes (solo en `CREATED`).
- Cancelar **sus** paquetes (no `DELIVERED`/`CANCELLED`).
- Rastrear cualquier paquete por `trackingCode` (público).
- Ver lugares (`findAllPlaces`).

### Admin / rol con permisos
- Crear paquetes (propios o a nombre de otro con `ownerUserId`).
- Ver todos los paquetes y estadísticas globales.
- Avalar/rechazar propuestas y descancelar.
- Cambiar estados (`changePackageStatus`).
- Administrar lugares (`createPlace`, `updatePlace`, `deletePlace`).

> Nota: `proposePackage` es exclusivo del CUSTOMER. El admin crea directamente con `createPackage`.

---

## 7. Flujo propuesta → aprobación

1. **CUSTOMER** `proposePackage` → paquete en `PROPOSED` con `price` calculado.
2. **ADMIN** `approvePackage` → `CREATED` (o `rejectPackage` → `CANCELLED`).
3. **ADMIN** `changePackageStatus` → `IN_TRANSIT` → `DELIVERED`.
4. Para deshacer un `CANCELLED`: **ADMIN** `reactivatePackage`.

---

## 8. Colección Postman

Archivo: `postman/Logistics-API.postman_collection.json`.

Variables de colección (llénalas según tu entorno):

| Variable | Uso |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `accessToken` / `refreshToken` | se llenan solos al hacer `Login`/`Register` |
| `adminEmail` / `customerEmail` / `password` | credenciales |
| `originPlaceId` / `destinationPlaceId` | UUIDs de `place` |
| `userId` | UUID del usuario |
| `packageUuid` | UUID de un paquete a consultar |
| `packageUuidPropose` | UUID del paquete propuesto (para `approvePackage`/`rejectPackage`) |
| `trackingCode` | código de rastreo |
| `placeUuid` | UUID de un lugar (para `updatePlace`/`deletePlace`) |

**Flujo recomendado en Postman**:
1. `Auth → Login (Admin)` (o `Register` para crear el customer).
2. `Place → createPlace` (o `findAllPlaces`) para obtener `originPlaceId`/`destinationPlaceId`.
3. `Package → proposePackage` (customer) y copiar el `uuid` a `packageUuidPropose`.
4. `Package → approvePackage` (admin).
5. `Package → findPackageById` para ver el `history`.
