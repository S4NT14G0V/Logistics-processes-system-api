# ENDPOINTS — Logistics Processes System API

Documentación de los endpoints del API: **Auth**, **User**, **Catalog**, **Package**, **Place**, **Location**, **Inventory** y **Alert**.

- **Base URL**: `http://localhost:8080`
- **GraphQL endpoint**: `POST /graphql`
- **GraphiQL**: `http://localhost:8080/graphiql`
- **Auth (REST)**: `/auth/*`

---

## 1. Autenticación (REST)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| `POST` | `/auth/register` | `{ name, email, password }` | `{ accessToken, refreshToken, changePasswordRequired }` |
| `POST` | `/auth/login` | `{ email, password }` | `{ accessToken, refreshToken, changePasswordRequired }` |
| `POST` | `/auth/refresh` | `{ refreshToken }` | `{ accessToken, refreshToken, changePasswordRequired }` |
| `POST` | `/auth/logout` | `{ refreshToken }` | `204 No Content` |
| `POST` | `/auth/change-password` | `{ currentPassword, newPassword }` | `204 No Content` |

> `changePasswordRequired` indica si el usuario (creado por un admin) aún usa contraseña temporal y debe cambiarla vía `/auth/change-password`.

Para los endpoints GraphQL protegidos, envía:

```
Authorization: Bearer <accessToken>
```

> Los permisos viajan en el JWT junto con el rol; si cambias permisos en la BD, **vuelve a hacer login**.

---

## 2. Permisos (resumen)

| Ámbito | Permisos |
|---|---|
| `package` | `create:all/own`, `read:all/own`, `update:all/own`, `cancel:all/own` |
| `user` | `create:all`, `read:all/own`, `update:all/own`, `delete:all` |
| `catalog` | `read` |
| `location` | `read:all/own`, `create:all`, `update:all`, `delete:all` |
| `inventory` | `read` |
| `alert` | `read:all/own`, `create:all` |

Roles: `ADMIN`, `LOGISTICS`, `WAREHOUSE`, `SUPERVISOR`, `CUSTOMER`.

---

## 3. Queries de Package

| Query | Args | Permissions | Descripción |
|---|---|---|---|
| `findAllPackages` | `page, size` | `read:all` / `read:own` | Lista paginada |
| `findPackageById` | `id: ID!` | `read:all` / `read:own` | Detalle (incluye `history`) |
| `findPackageHistory` | `packageId: ID!` | `read:all` / `read:own` | Historial de estados |
| `findPackageByTrackingCode` | `trackingCode: String!` | Público | Rastreo sin datos sensibles |
| `findPackagesByDateRange` | `page, size, startDate, endDate` | `read:all` / `read:own` | Por rango de fechas |
| `findPackageCountByUserId` | `userId: ID!` | `read:all` / `read:own` | Conteo por usuario |
| `findPackagesByStatusIn` | `page, size, packageStatuses` | `read:all` / `read:own` | Por estados |
| `findPackageCountByAllUsers` | — | `read:all` | Conteo global |
| `findPackageCountByAllStatus` | — | `read:all` | Conteo por estado |
| `findAllPackagesByUserId` | `page, size, userId` | `read:all` / `read:own` | Paquetes de un usuario |
| `findAllPackagesByPlace` | `page, size, origin, destination` | `read:all` / `read:own` | Por origen/destino |

## 4. Mutations de Package

| Mutation | Args | Permissions | Descripción |
|---|---|---|---|
| `createPackage` | `input: PackageInput!` | `create:all` | Crear paquete (opcional `ownerUserId`) |
| `proposePackage` | `input: PackageInput!` | `create:own` | Customer propone envío (`PROPOSED`) |
| `approvePackage` | `id: ID!` | `update:all` | `PROPOSED → CREATED` |
| `rejectPackage` | `id: ID!, reason` | `update:all` | `PROPOSED → CANCELLED` |
| `reactivatePackage` | `id: ID!` | `update:all` | `CANCELLED → CREATED` |
| `updatePackage` | `id: ID!, input: PackageUpdateInput!` | `update:all` / `update:own` | Editar (solo en `CREATED`) |
| `cancelPackage` | `id: ID!, reason` | `cancel:all` / `cancel:own` | Cancelar (no `DELIVERED`/`CANCELLED`) |
| `changePackageStatus` | `id: ID!, statusCode: String!` | `update:all` | Avanzar máquina de estados |

Estados: `PROPOSED → CREATED → IN_TRANSIT → DELIVERED` · `PROPOSED/CREATED → CANCELLED`.

---

## 5. User

| Operación | Args | Permissions |
|---|---|---|
| `findAllUsers` | — | `user:read:all` |
| `findUserById` | `id: ID!` | `user:read:all` |
| `getCurrentUserData` | — | `user:read:own` |
| `createUser` | `input: UserInput!` | `user:create:all` |
| `updateUser` | `id: ID!, input: UserUpdateInput!` | `user:update:all` |
| `updateCurrentUser` | `input: UserUpdateInput!` | `user:update:own` |
| `deleteUser` | `id: ID!` | `user:delete:all` |

- `UserInput`: `{ name, email, roleId, temporaryPassword }`.
- `UserUpdateInput`: `{ name, email, roleId }` (opcionales).
- `UserResponse`: `{ id, name, email, role }`.
- `createUser` crea el usuario con contraseña temporal (`changePasswordRequired=true`).
- `deleteUser` no permite borrar la propia cuenta.

---

## 6. Catalog

| Operación | Args | Permissions |
|---|---|---|
| `findAllRoles` | — | `catalog:read` |
| `findRoleById` | `id: Int!` | `catalog:read` |
| `findAllPackagesStatus` | — | `catalog:read` |
| `findPackageStatusById` | `id: Int!` | `catalog:read` |
| `findAllAlertTypes` | — | `catalog:read` |
| `findAlertTypeById` | `id: Int!` | `catalog:read` |

---

## 7. Place

| Operación | Args | Permissions |
|---|---|---|
| `findAllPlaces` | — | `package:read:all` / `read:own` |
| `findPlaceByUuid` | `uuid: ID!` | `package:read:all` / `read:own` |
| `createPlace` | `input: PlaceInput!` | `package:create:all` |
| `updatePlace` | `uuid: ID!, input: PlaceInput!` | `package:update:all` |
| `deletePlace` | `uuid: ID!` | `package:cancel:all` |

---

## 8. Location

| Operación | Args | Permissions |
|---|---|---|
| `findAllLocations` | — | `location:read:all` |
| `findLocationById` | `id: ID!` | `location:read:all` |
| `findAllLocationsByPackageId` | `packageId: ID!` | `read:all` / `read:own` |
| `findLocationsByTrackingCode` | `trackingCode: String!` | `read:all` / `read:own` |
| `findLastLocationByPackageId` | `packageId: ID!` | `read:all` / `read:own` |
| `findAllLocationsByUserId` | `userId: ID!` | `read:all` / `read:own` |
| `addLocation` | `packageId: ID!, input: LocationAddInput!` | `location:create:all` |
| `updateLocation` | `id: ID!, input: LocationAddInput!` | `location:update:all` |
| `deleteLocation` | `id: ID!` | `location:delete:all` |

- `LocationAddInput`: `{ latitude, longitude, address }`.
- `addLocation` usa el usuario autenticado como `handlerUser`.

---

## 9. Inventory

| Operación | Args | Permissions |
|---|---|---|
| `inventorySummary` | `periodStart, periodEnd, region` | `inventory:read` |

Devuelve `[{ region, inTransit, delivered, pending }]` agrupado por destino.

---

## 10. Alert

| Operación | Args | Permissions |
|---|---|---|
| `findAllAlerts` | — | `alert:read:all` |
| `findAllAlertsByUserId` | `userId: ID!` | `read:all` / `read:own` |
| `sendAlertToUser` | `userId, packageId, alertTypeId, description` | `alert:create:all` |

- `AlertResponse`: `{ id, description, registeredAt, alertType, packageId }`.
- `sendAlertToUser` crea la alerta y la emite por la suscripción (WebSocket).

---

## 11. Colección Postman

Archivo: `postman/Logistics-API.postman_collection.json`.

Variables de colección: `baseUrl`, `accessToken`, `refreshToken`, `adminEmail`, `customerEmail`, `password`, `originPlaceId`, `destinationPlaceId`, `userId`, `packageUuid`, `packageUuidPropose`, `trackingCode`, `placeUuid`.

**Flujo recomendado**:
1. `Auth → Login` (admin) y `Register` (customer).
2. `Catalog → findAllRoles` / `findAllAlertTypes`.
3. `Place → createPlace` para `originPlaceId`/`destinationPlaceId`.
4. `Package → proposePackage` (customer) → `approvePackage` (admin) → `changePackageStatus`.
5. `Location → addLocation` + `findLocationsByTrackingCode`.
6. `Alert → sendAlertToUser` + `findAllAlertsByUserId`.
7. `User → createUser` (temp password) → login → `change-password`.
