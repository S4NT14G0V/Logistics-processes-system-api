# Backend Courier Sync Feature 4

**Sistema Web para Optimización de Procesos Logísticos**

 A web-based system that optimizes a company's transportation and distribution processes, increasing operational efficiency, reducing management time, and improving the customer experience through automation, traceability, and real-time data analysis.

**Feature 4.**

  In-Transit Inventory Control Monitor packages at all stages of transportation, minimizing losses and errors.

**Constraints**
* Java 17
* Docker
* Docker [Compose]

---

## Configuración del archivo `.env`
Before running the project, create a `.env` file in the root with the following environment variables. Here's an example:

```env
SERVER_PORT=8080
FRONTEND_BASE_URL=http://localhost:3000
BACKEND_BASE_URL=http://localhost:8080
DB_NAME=mydatabase
DB_HOST=localhost
DB_USER=myuser
DB_PASSWORD=mypassword
DB_PORT=5432
JWT_SECRET=mysupersecretkey
JWT_EXPIRATION=3600000
OAUTH2_IDCLIENT=your-google-client-id.apps.googleusercontent.com
OAUTH2_SECRETCLIENT=your-google-client-secret
ADMIN_EMAIL=test@gmail.com
```

## Data Base
### Set up the database in Supabase
Go to Supabase and create a project, after that click on the button at the top called `Connect`, in this place click on `View parameters` and collect all values necessary to connect the database.

__Preview__:

![image](https://github.com/user-attachments/assets/5f18fe9e-3a23-46e8-a5f8-4d3d0e79d551)


### Replace the values of the Data Base

Go to the `.env` and replace the  values of
`DB_URL`, `DB_PORT`, `DB`, `DB_USER` and `DB_PASSWORD`
with your postgresql database values corresponding.

## Google Cloud API / Oauth2
Create an account in Google Cloud and in the search bar put auth, after that click on the first option called similar to `Oauth Consent Screen` fill all necessary information. Click in the `Clients` button and after that click on the `Add client` button to create a new client for the project, add the corresponding `Urls` for prepare the integration service correctly. For the last, save the `Client ID` and `Client Secret` values and replace all in `.env`.

__Preview__:

![image](https://github.com/user-attachments/assets/abdce81c-aa3b-4f0b-9f17-f7567c88937b)

![image](https://github.com/user-attachments/assets/c79fe0fa-18c2-4f82-b34a-8fcdc83e2368)

![image](https://github.com/user-attachments/assets/1f9713ed-189f-4811-8b98-214f99e7f81a)

## JWT
Establish the parameters of the jwt, that includes `Secret` and `Expiration`, the secret necessary need be `Base64` to function correctly and the expiration it is in milliseconds. Also replace this values in `.env`.

## Admin Setup
Also, for this sprint to validate everything, you need some special role; our feature includes authentication, but it is not based exclusively on that, then
you need to replace this value in `.env` (Note: This is provisional to show the results of the sprints).

---

## How to install it

### 1. Clone the repository:

```shell
git clone https://github.com/TeoGR25/Feature4_Backend.git
```

### 2. Put the `.env` file with all the values
Make sure you have the `.env` file configured as explained above.

### 3. Execute the project with Docker Compose
```docker-compose
docker-compose build
```
```docker-compose
docker-compose up
```
## How to test it

Enter to the url of backend that you put in the `.env` with the extension `/oauth2/authorization/google`, example: `localhost:8080/oauth2/authorization/google`, and in that site you log in with the google account, after you are going to be redirected to the frontend url of you `.env` wit the extension `/auth/callback?token=`, example: `http://localhost:3001/auth/callback?token=eyJhbGci8...`, then with the token of that url you can execute calls to the `/graphql` endpoint correctly.

Test the endpoint in the extension `/graphiql` of your backend url, at this point for testing you need to put `{"Authorization":"Bearer token"}` after replaced the token value. To see more about the data point access press the button in the sidebar called `Documentation`

---

## Testing automatizado (Testcontainers)

Los tests de integración usan **Testcontainers** (PostgreSQL real) en lugar de H2. Decisión:

- H2 no ejecuta las migraciones Flyway (son Postgres: `gen_random_uuid()`, `DO $$`, `uuid`, `ON CONFLICT`...) y obligaba a un seeder manual de permisos/roles/estados.
- Testcontainers levanta `postgres:18`, corre `V1..V12`, valida el esquema con `ddl-auto=validate` y ejercita el flujo HTTP real (`register` → JWT → `JwtAuthenticationFilter` → `@PreAuthorize`). Paridad total con producción, a cambio de requerir Docker.

```shell
mvnw test
```

Documentación:

- [`docs/TEST.md`](docs/TEST.md) — qué se testea (tabla con checks).
- [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md) — endpoints y permisos por rol.
