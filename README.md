# TechShop (Tienda en Línea)

Online store for computer hardware and electronics, built with Spring Boot 3 and Thymeleaf. Includes a product catalog, shopping cart, invoicing, role-based access control, user registration with email activation, image storage in Firebase, and a multilingual interface.

## Features

- **Catalog**: public home page with products grouped by category.
- **Cart and invoicing**: add, update, and remove items; checkout generates an invoice (`Activa`, `Pagada`, `Anulada`) with line items that store the historical price.
- **Admin CRUD**: categories, products, users, application constants.
- **Image upload**: category, product, and user images are stored in Firebase Storage.
- **Security**: form login, BCrypt password hashing, one concurrent session per user, and URL-to-role rules stored in the database (`ruta` table).
- **Registration flow**: new users receive an activation email; there is also a "remember username" flow.
- **Role management screen**: assign and remove roles per user (`/usuario_rol/mantenimiento`).
- **Query examples**: `/consultas/listado` demonstrates derived queries, JPQL, and native SQL.
- **i18n**: Spanish, English, French, and Portuguese. Switch with `?lang=es|en|fr|pt`.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 (Web, Data JPA, Security, Mail) |
| Views | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Frontend | Bootstrap 5.3.8, Font Awesome 7.0.1, jQuery 3.7.1, Popper 2.11.7 (via WebJars) |
| Database | MySQL (utf8mb4) |
| File storage | Firebase Storage (`firebase-admin` 9.7.0) |
| Build | Maven |
| Boilerplate | Lombok |
| Container | Docker (multi-stage build, non-root runtime user) |

## Project structure

```
src/main/java/com/tienda/
  TiendaApplication.java      Entry point
  SecurityConfig.java         Security filter chain, rules loaded from the `ruta` table
  ProjectConfig.java          View controllers, i18n (locale resolver, `lang` param)
  StorageConfig.java          Firebase / Google Cloud Storage client bean
  controller/                 Web controllers (producto, categoria, usuario, carrito, registro, ...)
  domain/                     JPA entities (Usuario, Rol, Producto, Categoria, Factura, Venta, Ruta, Constante, Item)
  repository/                 Spring Data repositories
  service/                    Business logic (cart, invoices, mail, Firebase, registration, ...)
src/main/resources/
  application.properties      App, database, Firebase, and mail configuration
  creaTablas.sql              Schema + sample data
  messages*.properties        i18n bundles (default, es, en, fr, pt)
  templates/                  Thymeleaf views
  static/                     CSS, JS, favicons, media
```

## Prerequisites

- JDK 21
- Maven 3.9+ (or use your IDE)
- MySQL 8+ (local or hosted)
- A Firebase project with Storage enabled and a service-account key
- A Gmail account with an app password, or any SMTP server

## Setup

### 1. Create the database

Run the script as a MySQL admin user:

```bash
mysql -u root -p < src/main/resources/creaTablas.sql
```

The script **drops and recreates** the `techshop` database. Do not run it against data you want to keep. It also creates two DB users (`usuario_prueba` with read/write, `usuario_reportes` with read-only). Change their passwords before using this anywhere beyond your own machine.

It loads sample data: 3 users, 4 categories, 16 products, 6 invoices, the roles `ADMIN`, `VENDEDOR`, `USER`, route-permission rules, and application constants.

### 2. Configure the application

Edit `src/main/resources/application.properties`. Do not commit real credentials.

| Property | Purpose |
|---|---|
| `spring.datasource.url` / `username` / `password` | MySQL connection. A local URL is provided commented out; the active one points to a hosted instance. |
| `server.port` | Defaults to `80`. Ports below 1024 need elevated privileges on Linux/macOS; use `8080` for local development. |
| `firebase.bucket.name` | Your Firebase Storage bucket |
| `firebase.storage.path` | Folder inside the bucket for uploads |
| `firebase.json.path` / `firebase.json.file` | Classpath location of the service-account JSON (`src/main/resources/firebase/`) |
| `spring.mail.username` / `password` | SMTP credentials for activation emails |

Prefer environment variables over editing the file. Spring Boot maps them automatically, for example:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/techshop
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
export SPRING_MAIL_USERNAME=...
export SPRING_MAIL_PASSWORD=...
export SERVER_PORT=8080
```

### 3. Run

```bash
mvn spring-boot:run
```

Or build and run the JAR (the artifact is named `app.jar`):

```bash
mvn clean package -DskipTests
java -jar target/app.jar
```

Open `http://localhost:<port>/`.

### Docker

```bash
docker build -t techshop .
docker run -p 80:80 techshop
```

The Dockerfile builds with Maven on JDK 21, then runs on a JRE 21 Alpine image as a non-root user. It exposes port 80. Because the container user is non-root, make sure the port you configure is allowed inside the container (the default setup uses 80; Docker handles this, but a bare host run may not).

## Roles and access control

Authorization is **data-driven**. `SecurityConfig` reads the `ruta` table at startup and registers one matcher per row. Any URL not listed requires authentication.

| Role | Example access |
|---|---|
| Public (no role) | `/`, `/index`, `/carrito/**`, `/registro/**`, static assets |
| `USER` | `/facturar/carrito` (checkout) |
| `VENDEDOR` | `/producto/listado`, `/categoria/listado` |
| `ADMIN` | create/edit/delete for products and categories, `/usuario/**`, `/constante/**`, `/usuario_rol/**`, `/ruta/**` |

Rules are loaded once at startup. After changing rows in `ruta`, restart the application.

The sample users are `juan` (ADMIN, VENDEDOR, USER), `rebeca` (VENDEDOR, USER), and `pedro` (USER). The script stores BCrypt hashes only, so the plaintext passwords are not documented here. Create your own users through `/registro/nuevo` or overwrite the hashes in the script with ones you generate.

## Main routes

| Route | Description |
|---|---|
| `/` | Home, product catalog |
| `/login`, `/logout` | Authentication |
| `/registro/nuevo` | Register; activation email follows |
| `/carrito/listado` | View cart |
| `/facturar/carrito` | Checkout (requires `USER`) |
| `/producto/listado`, `/categoria/listado` | Management lists |
| `/usuario/listado` | User administration |
| `/usuario_rol/mantenimiento` | Role assignment |
| `/constante/listado` | Application constants |
| `/consultas/listado` | Query demos |

## Data model

`categoria` 1—N `producto` · `usuario` 1—N `factura` 1—N `venta` N—1 `producto` · `usuario` N—N `rol` (via `usuario_rol`) · `ruta` N—1 `rol` · `constante` (key/value settings).

## Known limitations

- **PayPal is not implemented.** The `constante` table and `ruta` rows contain PayPal placeholders (`/paypal/**`), but no Java code uses them. Checkout currently only creates the invoice.
- **No automated tests.** There is no `src/test` directory.
- **Route rules require a restart** to take effect (see above).
- `/pruebas/**`, `/reportes/**`, `/role/**` appear in the route table but have no matching controllers.

## Security: fix before publishing this repository

The repository as provided contains live secrets committed in plain text. Treat all of them as compromised and rotate them:

1. **Firebase service-account private key** at `src/main/resources/firebase/*.json`. Revoke the key in Google Cloud Console and issue a new one. Anyone with it can read and write your storage bucket.
2. **Database password** in `application.properties` for the hosted MySQL instance.
3. **Gmail address and app password** in `application.properties`, including a stray line of what appears to be an app password sitting above the mail settings. Revoke it in your Google account.
4. **PayPal sandbox client ID and secret** inserted by `creaTablas.sql`.
5. **Default DB user passwords** hard-coded in `creaTablas.sql`.

Rotating alone is not enough: the old values stay in git history. Either rewrite history (`git filter-repo` or BFG) or publish from a fresh repository.

To stop this from recurring, add to `.gitignore`:

```
src/main/resources/firebase/*.json
src/main/resources/application-local.properties
.env
```

Then load secrets from environment variables or an untracked `application-local.properties`, and ship a sanitized `application.properties.example` instead.

## License

No license is declared. Add one before sharing the code publicly.
