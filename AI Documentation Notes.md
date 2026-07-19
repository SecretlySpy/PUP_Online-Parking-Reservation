# AI Documentation Notes

> Machine-readable static analysis of the **PUP Online Parking Reservation** codebase.
> Generated per the workflow in `AGENTS.md`. Every module/function entry uses the uniform fields:
> **Purpose**, **Inputs**, **Outputs**, **Dependencies**, **Behavior**.
> Field values are literal and parseable; avoid narrative interpretation when consuming this file.

---

## 0. Document Metadata

- **Field:** Value
- **Analysis date:** 2026-07-18
- **Language / framework:** Java (SE 16+; compiled and verified under JDK 25), Java Swing GUI, JDBC.
- **Database:** MySQL 8.0 (Docker-provisioned), accessed via `mysql-connector-java-8.0.19.jar`.
- **Third-party libs:** `mysql-connector-java-8.0.19.jar`, `jcalendar-1.4.jar` (date pickers). Located in `build/lib/`.
- **Build system:** PowerShell scripts (`scripts/compile.ps1`), no Maven/Gradle build wiring active (POM/Gradle workflows in `.github/` are templates).
- **Entry points:** `main(String[])` in each Swing screen; canonical launchers are `usermanagement.login` and `adminmanagement.login`.
- **Packages:** `app` (shared core/services), `DatabaseConnection` (JDBC factory), `usermanagement` (customer UI + reset service), `adminmanagement` (admin UI).

### QA Verification Status (Workflow Steps 1–2)

- **Method:** Clean full compile of all 33 source files against `build/lib/*`.
- **Command:** `javac -Xlint:all -encoding UTF-8 -cp "build/lib/*" -d <out> @sources.txt`
- **Result:** `javac` exit code **0**; 57 `.class` files produced. **GATE PASSED.**
- **Errors:** None.
- **Warnings (non-blocking, expected for Swing):**
  - `[serial]` — `JFrame` subclasses lack `serialVersionUID`. Cosmetic; Swing windows are never serialized at runtime.
  - `[serial]` — non-transient `Connection`/`ReservationRepository` fields on serializable frames. Same rationale.
  - `[this-escape]` — `setContentPane(...)` called in constructors (JDK 21+ lint). Standard Swing construction pattern; no functional defect.
- **Conclusion:** Code is logically consistent and integrates cleanly. Static analysis (Step 3) and documentation (Step 4) proceed.

---

## 1. Architecture Overview

### 1.1 Layering

```
Swing Screens (usermanagement.*, adminmanagement.*)   <- presentation + controllers
        |
        v
Services / Core (app.*: AuthenticationService, ReservationRepository,
                  PasswordSecurity, FormValidator, SessionContext, AppTheme)
usermanagement.PasswordResetService                   <- domain logic
        |
        v
DatabaseConnection.ConnectionDB  +  app.AppConfig  +  app.DatabaseInitializer
        |
        v
MySQL (legacy: useraccount, adminaccount; enhancement: parking_slots,
       reservations, notification_log, system_activity, password_reset_tokens)
```

### 1.2 Control Flow — Connection Acquisition (shared by all screens)

1. Screen constructor calls `ConnectionDB.getConnection()`.
2. `ConnectionDB` loads driver, reads URL/creds from `AppConfig`, opens a `java.sql.Connection`.
3. `DatabaseInitializer.ensureEnhancementSchema(conn)` runs idempotent DDL (create-if-missing + migrate).
4. Connection returned to screen (or `null` on failure, logged to `System.err`).

### 1.3 Control Flow — Authentication

`usermanagement.login` / `adminmanagement.login`
→ `AuthenticationService.authenticate{Customer,Admin}(conn, username, password)`
→ `PasswordSecurity.verify(password, storedHash)`
→ on success + stale hash: `PasswordSecurity.needsRehash` → transparent upgrade `UPDATE`
→ `SessionContext.signIn(username, role)` → open `menu`.

### 1.4 Control Flow — Reservation Booking

`usermanagement.reservation` (or admin `parking_inventory_management_add_button`)
→ `ReservationRepository.createReservation(...)`
→ `FormValidator` checks → past-time check → `isSlotAvailable` overlap check
→ `INSERT reservations` → `logNotification` (queued email row) → `logActivity` → returns reservation code.

### 1.5 Control Flow — Password Reset (customer & admin)

> Admin login mirrors this flow via `requestAdminPasswordReset` / `completeAdminPasswordReset` (scope `adminaccount`).

Request: `login` → `PasswordResetService.requestCustomerPasswordReset(conn, idOrEmail)`
→ find account → expire pending tokens → generate token → store SHA-256 hash → SMTP email link.
Complete: `login` → `completeCustomerPasswordReset(conn, tokenOrLink, newPassword)`
→ extract token → strength check → validate active token (row-locked) → hash new password → update → mark token used.

### 1.6 Cross-Cutting Data-Access Conventions

- All user-supplied values bound via `PreparedStatement` parameters (no string concatenation of inputs). Table names are code-controlled constants/enums, never user input.
- Audit trail: state-changing repository operations append to `system_activity` via `logActivity`.
- Idempotent runtime schema migration: every `ConnectionDB.getConnection()` and `new ReservationRepository(conn)` triggers `DatabaseInitializer.ensureEnhancementSchema`.

---

## 2. Package `app` — Core & Services

### 2.1 `app.AppConfig` (final utility)

- **Purpose:** Resolve DB connection settings from system properties → environment variables → hard-coded defaults.
- **Inputs:** System properties `opr.db.*`; env vars `OPR_DB_*`. None required (defaults supplied).
- **Outputs:** `dbHost()`, `dbPort()`, `dbName()`, `dbUser()`, `dbPassword()` → `String`; `dbUrl()` → fully-formed JDBC URL string.
- **Dependencies:** None (JDK only).
- **Behavior:**
  - `read(prop, env, fallback)`: returns first non-blank of property, env, fallback (trimmed).
  - `dbUrl()`: formats `jdbc:mysql://host:port/name?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000&socketTimeout=10000`.
  - Defaults: host `localhost`, port `3306`, name `onlineparkingreservation`, user `root`, password `Qwerty123@`.
  - **Side effects:** none. Stateless.

### 2.2 `app.PasswordSecurity` (final utility)

- **Purpose:** PBKDF2 password hashing, verification, strength validation, constant-time comparison, legacy plaintext support.
- **Inputs:** `char[] password`, `String storedPassword`.
- **Outputs:**
  - `hash(char[])` → `String` formatted `pbkdf2_sha256$<iterations>$<base64 salt>$<base64 hash>` (throws `GeneralSecurityException`).
  - `verify(char[], String)` → `boolean`.
  - `needsRehash(String)` → `boolean`.
  - `strengthError(char[])` → `String` error message or `null` if valid.
  - `matches(char[], char[])` → `boolean` (constant-time).
  - `clear(char[])` → `void`.
- **Dependencies:** `javax.crypto` (PBKDF2WithHmacSHA256), `java.security.SecureRandom`, `java.util.Base64`.
- **Behavior:**
  - Constants: algorithm prefix `pbkdf2_sha256`, iterations `120000`, salt `16` bytes, derived key `32` bytes, password length `8..128`.
  - `verify`: if stored value is not hashed → constant-time legacy plaintext compare; else parse 4 `$`-parts, recompute PBKDF2 with stored iterations/salt, constant-time compare.
  - `needsRehash`: true if not hashed, malformed, or iterations below current.
  - `strengthError`: requires length bounds + uppercase + lowercase + digit + symbol.
  - `matches` / `constantTimeEquals`: length-independent XOR accumulation to resist timing attacks.
  - **Side effects:** `clear` zero-fills the array; `constantTimeLegacyEquals` zero-fills its temp byte buffers.

### 2.3 `app.AuthenticationService` (final utility)

- **Purpose:** Verify credentials without placing secrets in SQL; transparently upgrade legacy hashes on login.
- **Inputs:** `Connection conn`, `String username`, `char[] password`.
- **Outputs:** `authenticateCustomer(...)` / `authenticateAdmin(...)` → `boolean` (throws `Exception`).
- **Dependencies:** `app.PasswordSecurity`, JDBC.
- **Behavior:**
  - `authenticate(conn, tableName, ...)`: `SELECT Username,Password FROM <table> WHERE Username=? LIMIT 1` (parameterized username).
  - Verifies via `PasswordSecurity.verify`; on success + `needsRehash`, runs `upgradePasswordHash` (`UPDATE Password, RepeatPassword`).
  - Customer → table `useraccount`; Admin → table `adminaccount` (table name is code constant, not user input).
  - **Side effects:** may `UPDATE` the credential row (hash upgrade).

### 2.4 `app.ReservationRepository` (instantiable, holds `Connection`)

- **Purpose:** Data-access layer for slots, reservation scheduling, history, status updates, reports, and audit logging.
- **Inputs:** `Connection conn` (constructor); per-method query/filter/booking parameters.
- **Outputs / Public API:**
  | Method | Returns | Notes |
  | --- | --- | --- |
  | `findSlots(floor,type,availability,startTime,endTime)` | `List<ParkingSlot>` | computed availability for time window |
  | `createReservation(username,slotId,start,end,name,email,phone,plate)` | `String` reservation code | validates + inserts + logs |
  | `findReservations(username,status,floor)` | `List<ReservationRecord>` | dynamic filtered query |
  | `updateReservationStatus(code,status,actor)` | `void` | normalizes status, audits |
  | `deleteReservation(code,actor)` | `void` | audits |
  | `updateSlotBaseStatus(slotId,baseStatus,actor)` | `void` | audits |
  | `reportCounts()` | `Map<String,Integer>` | slots/available_now/reserved/occupied/completed/cancelled |
  | `recentActivity()` | `List<String[]>` | latest 50 `system_activity` rows |
  | `customerDefaults(username)` | `String[]` | {FullName, Email, Mobile, Plate} or blanks |
- **Dependencies:** `app.ParkingSlot`, `app.ReservationRecord`, `app.FormValidator`, `app.DatabaseInitializer`, JDBC, `java.time`.
- **Behavior:**
  - Constructor calls `DatabaseInitializer.ensureEnhancementSchema(conn)`.
  - **Availability model:** `base_status != available` → `Maintenance`; overlapping `reserved`/`occupied` reservation → labeled `Reserved`/`Occupied`; otherwise `Available`.
  - **Overlap rule:** reservation conflicts when `start_time < requestedEnd AND end_time > requestedStart` and status ∈ {reserved, occupied}.
  - `createReservation`: aggregates validation via `FormValidator.firstError`; rejects past start (`< now − 1 min`); rechecks `isSlotAvailable`; builds code `RSV-<yyyyMMddHHmmss>-S<slotId>`; builds `qr_payload` `PUP-PARKING|<code>|slot=<id>|user=<name>`; inserts with status `reserved`.
  - **Side effects:** writes to `reservations`, `notification_log` (status `queued`), `system_activity`; updates `reservations`/`parking_slots` rows.
  - All dynamic filters bound via `bind(statement, parameters)`; status/floor normalized with `Locale.ROOT`.

### 2.5 `app.DatabaseInitializer` (final utility)

- **Purpose:** Idempotent runtime creation/migration of enhancement schema so older local DBs keep working.
- **Inputs:** `Connection conn` (no-op if `null`).
- **Outputs:** `ensureEnhancementSchema(conn)` → `void` (errors swallowed, logged to `System.err`).
- **Dependencies:** JDBC `DatabaseMetaData`.
- **Behavior:**
  - `CREATE TABLE IF NOT EXISTS`: `parking_slots`, `reservations`, `notification_log`, `system_activity`, `password_reset_tokens`.
  - `ensureCredentialSchema` (for `useraccount`, `adminaccount`): normalize PK to `Username`; widen `Email`→120, `Password`/`RepeatPassword`→255; add unique index on `Email`.
  - `seedSlots`: `INSERT IGNORE` 12 default slots across Ground/Second/Third floors with varied types.
  - Helper introspection: `tableExists`, `primaryKeyColumns`, `columnSize`, `indexExists`, `ensureUniqueIndex`, `normalizePrimaryKey`.
  - **Side effects:** DDL/DML on the connected schema. Safe to call repeatedly.

### 2.6 `app.FormValidator` (final utility)

- **Purpose:** Reusable form validation with predictable, user-facing error strings.
- **Inputs:** field values / label / `LocalDateTime` range.
- **Outputs:** `required`, `email`, `timeRange` → error `String` or `null`; `firstError(String...)` → first non-null error or `null`.
- **Dependencies:** `java.time`.
- **Behavior:** `email` regex `^[^@\s]+@[^@\s]+\.[^@\s]+$`; `timeRange` requires non-null and `end > start`. Pure, no side effects.

### 2.7 `app.SessionContext` (final utility, static state)

- **Purpose:** Hold the signed-in user for the current desktop process.
- **Inputs:** `signIn(username, role)`.
- **Outputs:** `username()` → `String`, `role()` → `UserRole`, `isAdmin()` → `boolean`.
- **Dependencies:** `app.UserRole`.
- **Behavior:** Static mutable fields; `signOut()` nulls both. **Side effect:** process-global state (single-session desktop assumption).

### 2.8 `app.UserRole` (enum)

- **Purpose:** Distinguish `ADMIN` vs `USER` modules.
- **Values:** `ADMIN`, `USER`.

### 2.9 `app.AppOptions` (final utility)

- **Purpose:** Provide shared dropdown option arrays for forms.
- **Outputs:** `birthYearsThroughCurrentYear()` → `String[]` (1930..current year); `carTypes()` → `String[]` (defensive `clone()`).
- **Dependencies:** `java.time.Year`.
- **Behavior:** Pure; returns fresh arrays per call.

### 2.10 `app.ParkingSlot` (model)

- **Purpose:** Read model for a slot plus computed availability for a time range.
- **Fields:** `slotId`, `floor`, `slotType`, `baseStatus` (immutable post-construct); `availability`, `reservationCode` (mutable).
- **Behavior:** Constructor defaults `availability="Available"`, `reservationCode=""`. Plain getters; setters for the two computed fields.

### 2.11 `app.ReservationRecord` (model)

- **Purpose:** Read model for reservation history / admin review / reports.
- **Fields (all final, getter-only):** `reservationId`, `reservationCode`, `username`, `slotId`, `floor`, `slotType`, `startTime`, `endTime`, `status`, `customerName`, `email`, `phone`, `vehiclePlate`, `qrPayload`.
- **Behavior:** Immutable value object.

### 2.12 `app.AppTheme` (final utility, UI)

- **Purpose:** Single visual language for all Swing screens (colors, fonts, spacing, components, table styling, frame defaults).
- **Inputs:** component/label/title arguments per factory method.
- **Outputs:** Styled Swing components and layout helpers (see table below).
- **Dependencies:** `java.awt`, `javax.swing`.
- **Key API:**
  | Method | Returns | Purpose |
  | --- | --- | --- |
  | `install()` | void | system L&F + UIManager font defaults |
  | `shell(title,subtitle,content)` | JPanel | standard page scaffold |
  | `header` / `card` / `constraints` | JPanel / GridBagConstraints | layout primitives |
  | `label` / `sectionLabel` / `textField` / `passwordField` | components | styled inputs |
  | `primaryButton` / `secondaryButton` / `dangerButton` / `moduleButton` | JButton | themed buttons (`ThemeButton` rounded paint) |
  | `styleTable` / `styleResponsiveTable` / `refreshResponsiveTable` | void | table look + responsive column sizing |
  | `clockLabel()` | JLabel | live 1s `Timer` clock |
  | `showFrame(frame,title,w,h)` | void | size/center/show with min bounds |
  | `showError(parent,msg,ex)` | void | standardized error dialog |
- **Behavior:** Brand color `PRIMARY = RGB(128,0,32)` (PUP maroon). `ThemeButton` overrides `paintComponent` for antialiased rounded fills + hover/press/disabled states. `styleResponsiveTable` installs component/model listeners that proportionally resize columns (min 52px, clamped 72..260 preferred). **Side effects:** starts a Swing `Timer` in `clockLabel`.

---

## 3. Package `DatabaseConnection`

### 3.1 `DatabaseConnection.ConnectionDB`

- **Purpose:** JDBC connection factory for all screens.
- **Inputs:** none (reads `AppConfig`).
- **Outputs:** `getConnection()` → `Connection` or `null` on failure.
- **Dependencies:** `app.AppConfig`, `app.DatabaseInitializer`, MySQL driver `com.mysql.cj.jdbc.Driver`.
- **Behavior:** `Class.forName(DRIVER)` → `DriverManager.getConnection(url,user,pass)` → `DatabaseInitializer.ensureEnhancementSchema(conn)` → return. On any exception, logs `"Database connection failed: ..."` to `System.err` and returns `null`. **Side effect:** runs schema migration on every connect.

---

## 4. Package `usermanagement` (customer)

### 4.1 `usermanagement.PasswordResetService` (instantiable)

- **Purpose:** Issue one-time password reset links by email and apply token-verified password changes. Includes a self-contained SMTP client.
- **Inputs:** `Connection conn`, account identifier/email, token-or-link string, `char[] newPassword`.
- **Outputs:**
  - `requestCustomerPasswordReset(conn, accountInput)` → `PasswordResetRequestResult` (`isEmailSent`, `getMaskedEmail`, `getUsername`, `getExpiresAt`).
  - `completeCustomerPasswordReset(conn, tokenOrLink, newPassword)` → `PasswordResetCompletionResult` (`getUsername`).
  - `requestAdminPasswordReset(conn, accountInput)` → `PasswordResetRequestResult` (admin equivalent; scope `adminaccount`).
  - `completeAdminPasswordReset(conn, tokenOrLink, newPassword)` → `PasswordResetCompletionResult` (admin equivalent).
- **Dependencies:** `app.PasswordSecurity`, JDBC, `javax.net.ssl` (TLS sockets), `java.security.MessageDigest` (SHA-256), `java.net.Socket`.
- **Behavior:**
  - Constants: token `32` bytes (URL-safe Base64, unpadded), TTL `30` minutes, socket timeout `15000` ms.
  - **Request flow (transactional):** disable autocommit → find account by Username **or** Email (ambiguous match → treated as not found, prevents enumeration) → expire pending tokens → store **SHA-256 hash** of token (plaintext token only emailed) → send email → commit. Returns `notSent()` when no account/email (no information leak).
  - **Complete flow (transactional):** extract token from link (`token=` query param, URL-decoded) → enforce `PasswordSecurity.strengthError` → `SELECT ... FOR UPDATE` active, unused, unexpired token → hash new password → update `Password`/`RepeatPassword` (must affect exactly 1 row) → mark token used → commit.
  - **SMTP client (`SmtpSession`):** raw socket SMTP supporting plaintext, STARTTLS (port 587 style), and implicit SSL (port 465 style); `EHLO`→fallback `HELO`; `AUTH LOGIN` (Base64 creds); dot-stuffing of message body; multi-line response parsing.
  - **Config (`SmtpConfiguration.load`):** reads `parking.smtp.*` props / `PARKING_SMTP_*` env; requires host + from-address; if username set, password required. `buildResetLink` uses `parking.reset.baseUrl` / `PARKING_PASSWORD_RESET_BASE_URL` (default `https://pup-parking.local/reset-password`).
  - **Security side effects:** header injection prevented via `cleanHeader` (strips CR/LF); tokens never stored in plaintext; rollback on any failure (`rollbackQuietly`).
  - **Nested types:** `AccountScope` (enum: CUSTOMER→`useraccount`/`customer`, ADMIN→`adminaccount`/`admin`), `Account`, `TokenRecord`, `PasswordResetRequestResult`, `PasswordResetCompletionResult`, `SmtpConfiguration`, `SmtpSession`, `SmtpResponse`.
  - **Scope isolation:** tokens carry `account_type`; `find/insert/expire` are filtered by it, so an admin reset link can never unlock a customer account and vice versa.

### 4.2 Customer Swing Screens (controllers)

- **Common pattern:** `extends JFrame implements ActionListener`; constructor calls `AppTheme.install()` + `ConnectionDB.getConnection()`; builds UI via `AppTheme.*` factories; each declares a `main(String[])` launcher. Long-running operations (login, reset) use `SwingWorker`.

| Class | Purpose | Key methods | Dependencies / Notes |
| --- | --- | --- | --- |
| `login` | Customer sign-in, registration entry, password-reset workflow | `loginUser`, `openRegistration`, `openPasswordResetWorkflow`, `sendPasswordResetLink`, `completePasswordReset`, `openMenu` | `AuthenticationService`, `PasswordResetService`, `SessionContext.signIn(USER)`, `PasswordSecurity`; reset ops on separate try-with-resources connections via `SwingWorker` |
| `reg` | Customer registration | form build/submit | hashes password via `PasswordSecurity`; inserts into `useraccount` |
| `menu` | Customer dashboard / navigation | `openProfile`, `openCarProfile`, `openReservation`, `openHistory`, `logout` | `SessionContext.signOut()`; responsive module grid |
| `user_profile` | View signed-in customer profile | `loadProfile`, `columnNames` | reads `useraccount` |
| `car_profile` | View customer vehicle profile | `loadCarProfile`, `buildToolbar` | reads vehicle data |
| `reservation` | Create a reservation; live slot list | `loadSlots`, `loadCustomerDefaults`, `createReservation` call | `ReservationRepository.createReservation`, `customerDefaults` |
| `reservation_history` | Customer's past reservations | load/filter | `ReservationRepository.findReservations` |
| `slot` | Slot view (customer) | slot rendering | `ReservationRepository.findSlots` |
| `receipt` | Reservation receipt/confirmation | render | reservation code + qr payload |

---

## 5. Package `adminmanagement` (administrator)

- **Common pattern:** identical Swing controller convention as §4.2; admin context uses `SessionContext.username()` as the `actor` for audit logging.

| Class | Purpose | Key methods | Dependencies / Notes |
| --- | --- | --- | --- |
| `login` | Admin sign-in + self-service password reset | `loginAdmin`, `openMenu`, `resetForm`, `openPasswordResetWorkflow`, `sendPasswordResetLink`, `completePasswordReset`, `setLoginActionsEnabled` | `AuthenticationService.authenticateAdmin`, `SessionContext.signIn(ADMIN)`, `usermanagement.PasswordResetService.{requestAdminPasswordReset,completeAdminPasswordReset}`; reset ops use `SwingWorker` + short-lived connections |
| `reg` | Admin registration | form build/submit | hashes via `PasswordSecurity`; inserts `adminaccount` |
| `menu` | Admin dashboard | `openCustomerProfile`, `openInventory`, `openSlots`, `openBilling`, `openReservations`, `openReports`, `logout` | responsive grid; `SessionContext.signOut()` |
| `customer_profile` | Browse/search customers | `loadCustomers(filter)` | reads `useraccount` (parameterized search) |
| `park_inventory_management` | Reservation/vehicle inventory table | `loadInventory(filter)`, `buildFooter` | reads reservation/inventory data |
| `parking_inventory_management_add_button` | Admin-side reservation capture form | `submitReservation`, `isFormValid`, `chooseColor`, `addDateChooserRow` | `jcalendar` date pickers; `AppOptions.carTypes` |
| `parking_slot` | Live slot monitor + status edits | `loadSlots`, `updateSelectedSlot`, `stopRefresh`, `dispose` | `ReservationRepository.findSlots` / `updateSlotBaseStatus`; auto-refresh `Timer` stopped on `dispose` |
| `customer_reservation` | Filter reservations + update status | `loadReservations`, `updateSelectedReservation` | `ReservationRepository.findReservations` / `updateReservationStatus`; actor = `SessionContext.username()` |
| `billingmanagement` | Compute parking charges + summary | `calculateTotal`, `writeSummary`, `addPaymentRow` | local calculation UI |
| `reports` | Operational metrics dashboard | `loadReport`, `buildMetrics` | `ReservationRepository.reportCounts` / `recentActivity` |

---

## 6. Data Model (effective schema)

### 6.1 Legacy credential tables (seeded via SQL; migrated by `DatabaseInitializer`)

- `useraccount` — customer accounts. Key cols: `Username` (PK after migration), `Password`, `RepeatPassword` (≤255), `Email` (≤120, unique), `FirstName`, `LastName`, `MobileNumber`, `PlateNumber`, profile fields.
- `adminaccount` — administrator accounts. Same credential column contract.

### 6.2 Enhancement tables (created at runtime; also in SQL seed files)

| Table | Key columns | Role |
| --- | --- | --- |
| `parking_slots` | `slot_id` PK, `floor`, `slot_type`, `base_status` (default `available`), `is_active` | physical slot inventory; index `(floor,slot_type,base_status)` |
| `reservations` | `reservation_id` PK auto, `reservation_code` unique, `username`, `slot_id`, `start_time`, `end_time`, `status`, customer/contact cols, `qr_payload` | bookings; indexes on `(slot_id,start_time,end_time,status)` and `(username,created_at)` |
| `notification_log` | `notification_id` PK, `reservation_code`, `channel`, `recipient`, `message`, `status` | queued email-style notifications |
| `system_activity` | `activity_id` PK, `actor`, `activity_type`, `activity_message`, `created_at` | audit trail |
| `password_reset_tokens` | `reset_id` PK, `account_type`, `username`, `token_hash` (char 64, unique), `expires_at`, `used_at` | one-time reset tokens (SHA-256 hashed) |

- **Reservation `status` lifecycle:** `reserved` → `occupied` → `completed`; or `cancelled`.
- **Slot `base_status`:** `available` | `maintenance` (operational override independent of reservations).

### 6.3 SQL seed/migration files (`Online Parking Reservation Database/`)

`*_useraccount.sql`, `*_adminaccount.sql`, `*_inventory.sql`, `*_enhancements.sql`, `*_routines.sql`, `*_legacy_information_seed.sql`, `*_useraccount_migration_add_profile_fields.sql`. Imported via `scripts/import-database.ps1` / `docker/mysql/01-import.sh`.

---

## 7. Configuration & Operations Reference

### 7.1 Database env vars (defaults in `AppConfig`)

`OPR_DB_HOST` (localhost), `OPR_DB_PORT` (3306), `OPR_DB_NAME` (onlineparkingreservation), `OPR_DB_USER` (root), `OPR_DB_PASSWORD` (Qwerty123@). Property overrides: `opr.db.*`.

### 7.2 SMTP / reset env vars (read by `PasswordResetService`)

`PARKING_SMTP_HOST`, `PARKING_SMTP_PORT` (587/465 by TLS mode), `PARKING_SMTP_USERNAME`, `PARKING_SMTP_PASSWORD`, `PARKING_SMTP_FROM`, `PARKING_SMTP_STARTTLS` (true), `PARKING_SMTP_SSL` (false), `PARKING_PASSWORD_RESET_BASE_URL`. Property overrides: `parking.smtp.*`, `parking.reset.baseUrl`.

### 7.3 Build / run scripts

- `scripts/compile.ps1` — downloads jars into `build/lib`, compiles into `build/classes`.
- `scripts/import-database.ps1` — starts Docker DB, imports SQL seeds.
- `scripts/run-user.ps1` / `scripts/run-admin.ps1` — launch customer / admin login screens.
- `docker-compose.yml` — `db` (MySQL 8.0), `adminer` (port 8080), `app` (GUI profile).

---

## 8. Observations & Risk Notes (non-blocking)

1. **Default credentials in source** (`AppConfig.DEFAULT_DB_PASSWORD`, README demo logins) — acceptable for local/dev defaults; must be overridden via env vars in any shared/production environment.
2. **`SessionContext` is process-global static state** — correct for a single-session desktop app; not safe if the codebase is ever reused server-side.
3. **`ConnectionDB.getConnection()` may return `null`** — callers should null-check before use; UI screens generally surface errors via `AppTheme.showError`.
4. **Admin password reset:** ~~not wired~~ **RESOLVED (2026-06-28)** — `PasswordResetService.AccountScope` now includes `ADMIN`; `requestAdminPasswordReset`/`completeAdminPasswordReset` added and wired to a "Reset Password" workflow in `adminmanagement.login`. Tokens are scoped by `account_type` so customer/admin flows cannot cross. (Admin reset still depends on SMTP being configured per §7.2.)
5. **Swing lint warnings** (`serial`, `this-escape`) are expected and harmless (see §0).
6. **Inline comments:** existing source already carries purpose-oriented Javadoc/inline comments consistent with this document; no comment churn was introduced by this analysis pass (no code was modified).

---

*End of AI Documentation Notes. Update this file (revise/add/remove entries) whenever modules change, per `AGENTS.md` Step 4.*
