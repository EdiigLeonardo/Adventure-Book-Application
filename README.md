# Adventure Book Application

Adventure Book is a full-stack choose-your-own-adventure reader. A player selects a validated JSON story, reads one section at a time, chooses a path, and manages a ten-point health pool. The application also supports publishing new books and saving/resuming a game session.

This repository contains two independently runnable applications:

- `AdventureBookApplication/` — Spring Boot API and game engine.
- `frontend/` — Angular single-page application.

## What to expect when running it

The first screen is the **Adventure Library**. It presents the available stories and a JSON upload area. Selecting **Begin Quest** navigates to `/game`, where the player can start the selected book, read its current section, choose an option, see health/status changes, and save or resume progress. **Back to library** returns to `/`.

The backend is authoritative: the browser never calculates a destination, consequence, victory, or game-over result by itself.

## Prerequisites

- Java 25 (the version declared in `AdventureBookApplication/pom.xml`)
- Node.js 22 or newer and npm
- Internet access is not required at runtime; frontend icons are inline SVG and the development build does not download web fonts

## Run the backend

From the repository root:

```bash
cd AdventureBookApplication
./mvnw spring-boot:run
```

On Windows:

```powershell
cd AdventureBookApplication
./mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`. Sample books are loaded from `src/main/resources/books`. H2 runs in memory and is used for game-session persistence; restarting the backend resets saved sessions.

## Run the frontend

Open a second terminal:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. `frontend/proxy.conf.json` forwards every `/api` request to `http://localhost:8080`, so the backend must be running first.

Useful frontend commands:

```bash
npm run build   # production build in frontend/dist/frontend
npm test        # Angular unit tests (headless)
```

Backend tests:

```bash
cd AdventureBookApplication
./mvnw test
```

## Run with Docker

Docker Compose starts the Spring Boot backend and serves the Angular frontend through Nginx. Only the frontend port is exposed; requests under `/api` are proxied internally to the backend.

Prerequisites: Docker Engine with the Compose plugin.

From the repository root:

```bash
cp .env.example .env       # optional: adjust local settings, never add secrets here
docker compose up --build
```

Open `http://localhost:8080`. To stop the containers, press `Ctrl+C`; to remove the containers and network afterwards, run:

```bash
docker compose down
```

Runtime settings such as `FRONTEND_PORT`, upload limits, CORS origins, H2 console and JPA schema mode can be supplied through `.env` or the shell environment. Do not place API keys, passwords or tokens in the Dockerfiles, Compose file, `.env.example` or the frontend bundle. Use a secrets manager or an externally provisioned environment variable for sensitive values.

## API surface

The frontend uses these REST endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/books` | List the catalogue |
| `GET` | `/api/v1/books/{id}` | Read one book |
| `POST` | `/api/v1/games/start` | Start at the `BEGIN` section |
| `POST` | `/api/v1/games/{id}/choose` | Apply a choice and consequence |
| `POST` | `/api/v1/games/{id}/save` | Persist the current session |
| `GET` | `/api/v1/games/{id}/resume` | Restore a saved session |
| `POST` | `/api/v1/books` | Upload and validate a JSON book |

## Key technical decisions

### Domain and validation

- Book integrity is checked on the backend before a book enters the catalogue. Validation verifies one `BEGIN`, at least one `END`, valid reachable `gotoId` references, and options on every reachable non-ending section.
- Unreachable sections are reported as warnings rather than making an otherwise playable book invalid.
- IDs are treated as strings across the frontend so books using either numeric or string JSON identifiers work consistently.

### Game state

- `GameService` exposes an RxJS `BehaviorSubject<GameSession | null>`. Starting a game, making a choice, or resuming a session updates the subject and refreshes the game screen.
- The backend starts every session at 10 HP. HP reaching zero takes priority over an `END` destination and produces `GAME_OVER`; reaching `END` with positive HP produces `VICTORY`.
- `UiStateService` carries the selected book between the library route and the game route. The book is not serialised into the URL; the session and book content remain separate concerns.

### Frontend structure

- `/` renders `MainScreenComponent` (catalogue and upload).
- `/game` renders `GameScreenComponent` (gameplay and save/resume).
- `BookCatalogComponent`, `BookUploadComponent`, and `GamePlayComponent` remain reusable feature components; routed pages coordinate them and own navigation.
- The visual system follows the supplied references: parchment surfaces, ink-brown story masthead, saffron actions, restrained borders, and editorial serif headings. Icons are inline SVG to avoid a remote Material Icons font dependency.

### Persistence and integration

- H2 is intentionally in-memory for a small, deterministic assessment setup. Replace the datasource configuration for durable production storage.
- Spring AI MCP support is configured in the backend so catalogue capabilities can be exposed to MCP clients independently of the REST API used by the Angular app.

## Troubleshooting

- **Books do not load:** confirm the backend is running on port 8080 and inspect the browser network tab for `/api/v1/books`.
- **The page opens but choices do nothing:** start a session with **Begin Adventure** first; choices are disabled after `VICTORY` or `GAME_OVER`.
- **Resume cannot find a session:** H2 is in memory, so only sessions saved during the current backend process are available.
- **Port already in use:** stop the process using port 8080 or 4200 before starting the corresponding application.

## Repository guide

- `AdventureBookApplication/src/main/java/.../controller` — REST controllers.
- `AdventureBookApplication/src/main/java/.../service` — catalogue and game rules.
- `AdventureBookApplication/src/main/resources/books` — sample adventure JSON files.
- `frontend/src/app/pages` — routed screens.
- `frontend/src/app/features` — reusable UI features.
- `frontend/src/app/core` — models, HTTP services, and shared state.
