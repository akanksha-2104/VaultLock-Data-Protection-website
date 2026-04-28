# VaultLock UI — React Frontend

A secure file management frontend built with React + Vite, designed to integrate with the Spring Boot backend.

---

## Project Structure

```
vaultlock-ui/
├── index.html                         # HTML entry point
├── vite.config.js                     # Vite config (proxy → localhost:8080)
├── package.json
└── src/
    ├── main.jsx                       # React entry — mounts App with BrowserRouter + AuthProvider
    ├── App.jsx                        # All routes defined here
    ├── styles/
    │   └── global.css                 # CSS variables, resets, fonts
    ├── api/
    │   └── axiosInstance.js           # Axios base config (baseURL, withCredentials)
    ├── context/
    │   └── AuthContext.jsx            # Global auth state: user, login(), register(), logout()
    ├── components/
    │   ├── Sidebar.jsx                # Navigation sidebar with user badge + logout
    │   ├── UI.jsx                     # Shared components: AlertBanner, StatCard, TagPill,
    │   │                              #   SectionCard, IconButton, PrimaryButton,
    │   │                              #   SecondaryButton, PageHeader, AppLayout
    │   └── PrivateRoute.jsx           # Route guard — redirects to /login if not authenticated
    ├── pages/
    │   ├── LoginPage.jsx              # POST /api/auth/login
    │   ├── RegisterPage.jsx           # POST /api/auth/register
    │   ├── DecoyPage.jsx              # Fake loading screen shown after too many failed logins
    │   ├── FilesPage.jsx              # GET /api/files, GET /api/files/:id/download, DELETE
    │   ├── UploadPage.jsx             # POST /api/files/upload (multipart/form-data)
    │   ├── AuditLogPage.jsx           # GET /api/audit-logs
    │   ├── LocationsPage.jsx          # GET /api/login-locations
    │   └── NotFoundPage.jsx           # 404 fallback
    └── config/
        └── CorsConfig.java            # ← Copy this into your Spring Boot project:
                                       #   src/main/java/com/dataprotection/.../config/
```

---

## Setup Instructions

### 1. Install dependencies

```bash
cd vaultlock-ui
npm install
```

### 2. Copy CorsConfig.java to your Spring Boot project

```
src/config/CorsConfig.java
  → your-backend/src/main/java/com/dataprotection/dataprotection/config/CorsConfig.java
```

### 3. Start the Spring Boot backend

```bash
# Inside your Spring Boot project
./mvnw spring-boot:run
# Runs on http://localhost:8080
```

### 4. Start the React frontend

```bash
npm run dev
# Runs on http://localhost:5173
```

Open http://localhost:5173 in your browser.

---

## API Routes Used

| Page            | Method | Endpoint                         |
|-----------------|--------|----------------------------------|
| Login           | POST   | `/api/auth/login`                |
| Register        | POST   | `/api/auth/register`             |
| Get profile     | GET    | `/api/auth/me`                   |
| List files      | GET    | `/api/files`                     |
| Upload file     | POST   | `/api/files/upload`              |
| Download file   | GET    | `/api/files/{id}/download`       |
| View file       | GET    | `/api/files/{id}/view`           |
| Delete file     | DELETE | `/api/files/{id}`                |
| Audit logs      | GET    | `/api/audit-logs`                |
| Login history   | GET    | `/api/login-locations`           |

---

## Security Features in UI

- **Decoy screen** — After 5 failed logins the backend throws `SuspiciousLoginAttemptException`.
  The frontend catches it and redirects to `/decoy` — an infinite spinner that reveals nothing to the attacker.
- **New location alert** — After login, if `newLocationDetected: true` is returned, a warning banner
  is shown on the Files page.
- **Private route guard** — All pages except `/login`, `/register`, and `/decoy` require authentication.
  Unauthenticated users are redirected to `/login`.

---

## Build for Production

```bash
npm run build
# Output is in /dist — deploy this to any static host or serve via Spring Boot's static resources
```
