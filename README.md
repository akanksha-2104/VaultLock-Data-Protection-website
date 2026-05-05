# VaultLock — Secure File Protection & Tracking System

**A production-grade secure document management system built to solve real data breach problems — featuring a honeypot deception system, AES-256 file encryption, real-time threat monitoring, and role-based access control.**

---

## The Problem I Set Out to Solve

Most file sharing tools treat security as an afterthought. They authenticate once, trust the session indefinitely, and keep no meaningful record of what happened. When a breach occurs — and they do — organizations cannot answer three critical questions:

- **Who** accessed which files?
- **When and from where** did they access them?
- **How did the attacker get in** and what did they take?

I built VaultLock to answer all three, before a breach happens.

The inspiration came from a real pattern: at law firms, hospitals, and startups, the most common attack is not sophisticated hacking — it is **credential stuffing**: trying thousands of username/password combinations until one works. Traditional systems respond by locking accounts, which tells the attacker they found a valid email and triggers social engineering attacks on support teams. VaultLock takes a different approach — it **lies to the attacker** by redirecting them to a convincing fake dashboard while the real account remains untouched and the security team receives an instant alert.

---

## What I Built

VaultLock is a full-stack secure document vault with five core guarantees:

### 1. Every file is encrypted on disk
Files are encrypted with **AES-256-GCM** before being written to storage. The raw bytes on disk are unreadable without the encryption key — meaning a stolen hard drive, a misconfigured cloud bucket, or a direct server intrusion yields nothing but ciphertext. Every file gets a unique 12-byte Initialization Vector, so encrypting the same file twice produces different ciphertext.

### 2. Only the right people access the right files
Three-tier access control enforced at the service layer, not just the UI:
- **PRIVATE** — owner only
- **PUBLIC** — anyone with the link, no authentication required
- **RESTRICTED** — a named whitelist of specific registered users

### 3. Every action is permanently logged
Every login, upload, download, view, edit, delete, failed login, and honeypot trigger is written to an immutable audit log with timestamp, IP address, and file context. This is the forensic record that answers "what happened" after any incident.

### 4. Attackers get deceived, not informed
After five failed login attempts, the system stops rejecting requests. Instead it silently serves the attacker a convincing fake dashboard — a honeypot. They believe they are logged in. They waste time. The security team receives a real-time WebSocket alert and an email notification. The real user's account is never affected.

### 5. Geographic anomalies are detected and flagged
Every login captures the IP address, approximate geographic location, and device information. If an account is accessed from a new city or country, the user receives an email alert and sees a warning banner on their next login.

---

## Features

### User Features
| Feature | Description |
|---|---|
| Secure Registration & Login | JWT-based stateless authentication with BCrypt password hashing |
| File Upload | Multipart upload with server-side AES-256-GCM encryption before disk write |
| File Download | Transparent decryption on download — user receives original plaintext file |
| Access Control | PRIVATE / PUBLIC / RESTRICTED tags with per-file email whitelist |
| File Sharing | Share restricted files with specific registered users by email |
| Version History | Every edit saves a snapshot — view and restore any previous version |
| Audit Log | Personal timeline of all account activity with IP and timestamp |
| Login History | Geographic login tracking with new-location alerts |

### Admin Features
| Feature | Description |
|---|---|
| Admin Dashboard | System-wide statistics: total users, files, events, threats |
| All-User Audit Log | Complete activity across every user account |
| Security Alerts | Dedicated feed of suspicious events with severity classification |
| Real-Time Alerts | WebSocket push notifications — attack events appear instantly, no refresh |
| Email Notifications | Automated email on new-location login and honeypot trigger |
| Honeypot System | Brute-force attackers redirected to decoy UI for 15 minutes |

---

## Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    BROWSER                                     │
│         React 18 + Vite (localhost:5173)                       │
│                                                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────────┐ │
│  │  Pages   │ │ Context  │ │  Axios   │ │  WebSocket/STOMP   │ │
│  │ (UI)     │ │ (State)  │ │ (HTTP)   │ │  (Live alerts)     │ │
│  └──────────┘ └──────────┘ └──────────┘ └────────────────────┘ │
└──────────────────────────┬──────────────────────┬──────────────┘
                           │ HTTP + JWT           │ ws://
                    (Vite proxy)                  │
┌──────────────────────────▼──────────────────────▼──────────────┐
│                    SPRING BOOT 3.2 (localhost:8080)            │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Spring Security                        │   │
│  │  JwtAuthFilter → SecurityFilterChain → @PreAuthorize    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Controllers  │→ │   Services   │→ │     Repositories     │  │
│  │              │  │              │  │   (Spring Data JPA)  │  │
│  │ AuthCtrl     │  │ AuthService  │  │                      │  │
│  │ FileCtrl     │  │ FileService  │  │  UserRepository      │  │
│  │ AuditCtrl    │  │ AuditLogSvc  │  │  FileDocumentRepo    │  │
│  │ AdminCtrl    │  │ AdminService │  │  FileVersionRepo     │  │
│  │ LocationCtrl │  │ JwtUtil      │  │  AuditLogRepository  │  │
│  └──────────────┘  │ EncryptUtil  │  │  LoginLocationRepo   │  │
│                    │ EmailSvc     │  └──────────────────────┘  │
│                    │ AlertService │                            │
│                    └──────────────┘                            │
│  ┌─────────────────────────────────────────┐                   │
│  │   WebSocket (STOMP + SockJS)            │                   │
│  │   /ws → /topic/security-alerts          │                   │
│  └─────────────────────────────────────────┘                   │
└────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                         DATABASE                                │
│   users │ file_documents │ file_versions │ audit_logs │         │
│   login_locations                                               │
└─────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Why |
|---|---|---|
| Frontend | React 18 + Vite | Component model, fast HMR, Vite proxy eliminates CORS in dev |
| Routing | React Router v6 | Declarative routes, PrivateRoute guard pattern |
| HTTP Client | Axios | Interceptor pattern for JWT attachment on every request |
| Real-time | SockJS + STOMP.js | WebSocket with fallback, topic-based pub/sub |
| Backend | Spring Boot 3.2 | Production-grade Java, Spring Security, DI, transactions |
| Authentication | JWT (JJWT 0.12) | Stateless — no server-side session, horizontally scalable |
| Authorization | Spring Security + @PreAuthorize | Method-level role checks, defense in depth |
| Encryption | Java AES-256-GCM | Authenticated encryption — detects tampering, not just encrypts |
| Database | JPA / Hibernate | ORM with repository pattern, transaction management |
| Email | Spring Mail + SMTP | Async notifications via @Async — never blocks login flow |
| Password Hashing | BCrypt | Adaptive cost factor, industry standard |

---

## Security Deep Dive

### JWT Authentication Flow

```
1. POST /api/auth/login { email, password }
2. AuthenticationManager.authenticate() → BCrypt verify
3. JwtUtil.generateToken(email, role)
   → Header:  { alg: HS256 }
   → Payload: { sub: email, role: USER/ADMIN, iat, exp }
   → Signed with HMAC-SHA256(secret)
4. Token returned to frontend → stored in sessionStorage
5. Every subsequent request: Authorization: Bearer <token>
6. JwtAuthFilter validates signature + expiry on every request
7. SecurityContext populated → Authentication.getName() = email
```

**Why JWT over sessions:** Sessions require server-side state. JWT is self-contained — the server validates the cryptographic signature without any database lookup, making the backend horizontally scalable with zero session synchronization overhead.

### Honeypot System — How Deception Works

```
Normal lockout (bad):
  5 failed attempts → "Account locked" → attacker confirms valid email
  → social engineering attack on support team begins

VaultLock honeypot (better):
  5 failed attempts → decoy redirect for 15 minutes
  → attacker sees a convincing fake loading screen
  → real account untouched, no information leaked
  → DECOY_REDIRECT event logged
  → admin gets WebSocket alert + email instantly
```

### AES-256-GCM Encryption

GCM mode provides two guarantees AES-CBC does not:
- **Confidentiality** — plaintext cannot be recovered without the key
- **Authenticity** — any modification to ciphertext is detected before decryption

Format stored on disk: `[12 bytes IV][16 bytes auth tag][N bytes ciphertext]`

### Defense in Depth

```
Layer 1:  HTTPS (TLS)           — transport encryption
Layer 2:  BCrypt                — password hashing, rainbow-table resistant
Layer 3:  JWT + HMAC-SHA256     — tamper-proof authentication
Layer 4:  Spring Security       — route-level access control
Layer 5:  @PreAuthorize         — method-level role enforcement
Layer 6:  Service-layer checks  — ownership verified per file operation
Layer 7:  AES-256-GCM           — encryption at rest on disk
Layer 8:  Honeypot              — active deception against brute force
Layer 9:  Audit log             — forensic record of every action
Layer 10: Geo-anomaly detection — alert on suspicious login location
```

---

## Project Structure

```
vaultlock/
├── backend/
│   └── src/main/java/com/dataprotection/
│       ├── config/
│       │   ├── SecurityConfig.java          JWT filter chain, CORS, roles
│       │   └── WebSocketConfig.java         STOMP broker, SockJS endpoint
│       ├── controller/
│       │   ├── AuthController.java          /api/auth/**
│       │   ├── FileController.java          /api/files/** + versions + rollback
│       │   ├── AuditLogController.java      /api/audit-logs
│       │   ├── LoginLocationController.java /api/login-locations
│       │   └── AdminController.java         /api/admin/** (ADMIN only)
│       ├── service/
│       │   ├── AuthService.java             Login, JWT generation, geo check
│       │   ├── FileService.java             Upload/download with AES-256-GCM
│       │   ├── AuditLogService.java         Append-only event logging
│       │   ├── AdminService.java            System-wide stats, all logs
│       │   ├── LoginAttemptService.java     Brute-force tracking, honeypot
│       │   ├── LoginLocationService.java    Geo tracking per login
│       │   ├── EmailNotificationService.java @Async email alerts
│       │   └── SecurityAlertService.java   WebSocket broadcast
│       ├── entity/
│       │   ├── User.java                   id, name, email, password, role
│       │   ├── FileDocument.java           File metadata + tag + sharedEmails
│       │   ├── FileVersion.java            Version snapshot
│       │   ├── AuditLog.java              Immutable event record
│       │   └── LoginLocation.java         Per-login geo snapshot
│       ├── filter/
│       │   └── JwtAuthFilter.java         Bearer token validation
│       └── util/
│           ├── JwtUtil.java               Token generation/validation
│           ├── FileEncryptionUtil.java    AES-256-GCM encrypt/decrypt
│           └── GeoLocationUtil.java       IP → location lookup
│
└── frontend/
    └── src/
        ├── api/axiosInstance.js           JWT interceptor, token store
        ├── context/AuthContext.jsx        Global auth, session restore
        ├── components/
        │   ├── Sidebar.jsx               Nav + admin + WS alert badge
        │   ├── PrivateRoute.jsx          Auth + role guard
        │   └── UI.jsx                   Shared design system
        └── pages/
            ├── LoginPage.jsx             POST /api/auth/login
            ├── RegisterPage.jsx          POST /api/auth/register
            ├── DecoyPage.jsx            Honeypot fake screen
            ├── FilesPage.jsx            File list + encryption badge
            ├── UploadPage.jsx           Upload + tag selector
            ├── FileVersionsPage.jsx     Version history + rollback
            ├── AuditLogPage.jsx         Personal activity timeline
            ├── LocationsPage.jsx        Login history + geo alerts
            ├── AdminDashboard.jsx       System stats + all-user logs
            └── SecurityAlertsPage.jsx   Live WS feed + threat history
```

---

## Setup

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+

### Backend

```bash
# 1. Generate secrets
openssl rand -base64 32   # for JWT secret
openssl rand -base64 32   # for AES encryption key

# 2. Update application.properties
app.jwt.secret=<jwt-secret>
app.encryption.key=<aes-key>

# 3. Add @EnableAsync to main class
@SpringBootApplication
@EnableAsync
public class DataprotectionApplication { ... }

# 4. Run
./mvnw spring-boot:run
# → http://localhost:8080
```

### Frontend

```bash
npm install
npm run dev
# → http://localhost:5173
```

### Make Yourself Admin

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

Log out and back in — Admin section appears in the sidebar.

---

## API Reference

### Authentication
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login, returns JWT |
| GET | `/api/auth/me` | JWT | Current user profile |

### Files
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/files/upload` | JWT | Upload + encrypt |
| GET | `/api/files` | JWT | List own files |
| GET | `/api/files/{id}/download` | JWT | Download + decrypt |
| DELETE | `/api/files/{id}` | JWT | Delete file |
| GET | `/api/files/{id}/versions` | JWT | Version history |
| POST | `/api/files/{id}/rollback/{vId}` | JWT | Restore version |
| GET | `/api/files/public/{id}/download` | None | Public download |

### Monitoring
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/audit-logs` | JWT | Own activity |
| GET | `/api/login-locations` | JWT | Own login history |

### Admin
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/stats` | JWT + ADMIN | System statistics |
| GET | `/api/admin/audit-logs` | JWT + ADMIN | All users' logs |
| GET | `/api/admin/security-events` | JWT + ADMIN | Threat feed |

### WebSocket
```
ws://localhost:8080/ws          STOMP endpoint (SockJS)
/topic/security-alerts          Subscribe for live alerts
```

---

## Key Learnings

Building VaultLock taught me lessons that no tutorial covers — lessons that only come from hitting real problems and thinking through why they exist.

### 1. Security is about threat modelling, not just adding features

The most important thing I learned is that security features only make sense when you define the threat first. A login lockout sounds secure, but it actually helps the attacker — it confirms they have a valid email. The honeypot only makes sense once you think like an attacker. Every security decision in this project forced me to ask: *what is the attacker's goal, and how does my response affect their ability to achieve it?* That shift in thinking — from "what should I add" to "what does the attacker learn from my response" — is the core of threat modelling.

### 2. Stateless authentication changes everything

Moving from session-based to JWT authentication was not just a library swap — it changed how I thought about state entirely. With sessions, the server owns the truth about who is logged in. With JWT, the token itself carries the truth and the server just verifies a mathematical signature. This means you can add more backend servers without any synchronization, but it also means you cannot instantly invalidate a token the way you can delete a session. I learned that stateless is not strictly better — it is a trade-off, and understanding that trade-off is more valuable than just knowing how to implement JWT.

### 3. Cryptography requires understanding modes, not just algorithms

I initially wrote "AES encryption" in my notes and thought that was specific enough. It is not. AES-CBC without authentication allows an attacker to flip bits in the ciphertext and corrupt the decrypted output in predictable ways. AES-GCM adds a Galois MAC tag that detects any modification to the ciphertext before decryption even runs. The algorithm (AES) is the same. The mode (GCM vs CBC) is what determines whether the system is actually secure. Cryptography is in the details.

### 4. Concurrency bugs are silent until they destroy you

The `LoginAttemptService` uses `ConcurrentHashMap.compute()` for atomic read-modify-write. I originally wrote it as a separate `get()` then `put()` — a classic race condition. Two simultaneous login attempts could both read `failedAttempts = 4`, both increment to `5`, and both fail to trigger the honeypot. The bug would never appear in testing and would only surface under real load. Learning to reason about concurrent state — and to reach for atomic operations rather than separate get/put pairs — is a skill that separates good backend engineers from great ones.

### 5. Frontend authentication is harder than backend authentication

The backend's job is clear: validate the token on every request. The frontend's job is subtle: persist the token across page refreshes, restore authentication state before any protected component renders, handle token expiry gracefully, and never block the UI while verification is in flight. The `AuthContext` checking state pattern — rendering a spinner while `/api/auth/me` verifies the token — took several iterations to get right. Auth state management is not a solved problem you can copy; you have to reason through your specific flow.

### 6. The Vite proxy fixed more than CORS — it fixed cookie scoping

I originally thought the Vite proxy was just a convenience to avoid CORS errors. It turned out to fix a deeper problem: cross-origin cookie scoping. When the frontend at port 5173 makes requests directly to port 8080, the browser treats them as cross-origin and applies strict SameSite cookie rules. The proxy makes all requests appear same-origin, which is why JWT in the Authorization header flows correctly. Understanding why the proxy works — not just that it works — helped me reason about what changes in a production deployment.

### 7. Real-time features require thinking about reconnection, not just connection

The WebSocket implementation looks simple: connect, subscribe, receive. What looks simple becomes complex when the server restarts or the network drops. The STOMP client's `reconnectDelay: 5000` automatically retries, but the component must handle the `connected` state transitioning without crashing. I learned that real-time features are not about the happy path — they are about what happens when the connection breaks and re-establishes.

### 8. @Async taught me that thread boundaries have consequences

Adding `@Async` to `EmailNotificationService` hides a significant architectural decision: the method now runs in a different thread from the caller, which means it cannot share a JPA transaction, cannot throw checked exceptions back to the caller, and cannot rely on `SecurityContextHolder` which is thread-local. Learning that `@Async` creates a thread boundary — and that thread boundaries have consequences for every stateful thing in Java — is a lesson I will carry into every distributed system I build.

### 9. Audit logs are useless without immutability

My first implementation gave the audit log service both `save()` and `delete()` access. Then I asked: if an attacker compromises an admin account, can they delete the entries proving they were there? Yes. The fix is architectural: the audit service should only ever call `save()`, and in production the database user should not have DELETE permission on audit_logs at all. Security is not just about the code — it is about the entire system including database permissions and operational procedures.

### 10. Building something complete is different from building something correct

Every tutorial shows how to build one feature correctly in isolation. VaultLock was the first project where all features had to work together — JWT authentication had to work with Spring Security's filter chain, which had to work with CORS, which had to work with the Vite proxy, which had to work with the frontend's session restore flow. The integrations are where real bugs live. The skill of holding the entire system in your head — understanding how a change in SecurityConfig affects the JWT filter which affects the frontend auth context — is the skill that actually matters in professional engineering.



---

<div align="center">
Built with care to solve a real security problem.<br/>
Every feature exists because a real attack made it necessary.
</div>
