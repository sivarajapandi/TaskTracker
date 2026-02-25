# Session-Based Authentication Flow Diagram

## 1️⃣ FIRST TIME USER (No Session)

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  User: "I want to access /dashboard"                           │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Spring Security Filter:                                        │
│  "Do you have a valid session cookie?"                         │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  "NO" → Redirect to /login                                     │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  GET /login                                                     │
│  AuthController.loginPage() returns login.html                 │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Browser displays login form                                   │
│  User enters: username=john, password=secret123               │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Form submits POST /login (Spring Security intercepts)         │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Spring Security calls:                                        │
│  UserDetailsService.loadUserByUsername("john")                 │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  CustomUserDetailsService queries database:                    │
│  SELECT * FROM users WHERE username='john'                     │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Spring Security compares passwords:                           │
│  BCryptPasswordEncoder.matches(                                │
│    "secret123" (from form),                                    │
│    "$2a$11$..." (from database)                                │
│  )                                                              │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Passwords match? ✅ YES                                        │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Spring Security creates session:                              │
│  ┌──────────────────────────────┐                              │
│  │ Session ID: abc123xyz        │                              │
│  │ Username: john               │                              │
│  │ Authorities: [ROLE_USER]     │                              │
│  │ Created: 2026-02-21 17:30    │                              │
│  └──────────────────────────────┘                              │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Send HTTP Response:                                           │
│  ┌─────────────────────────────────────────────┐               │
│  │ Status: 302 (Redirect)                      │               │
│  │ Location: /dashboard                        │               │
│  │ Set-Cookie: JSESSIONID=abc123xyz; Path=/   │               │
│  └─────────────────────────────────────────────┘               │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Browser redirects to /dashboard                              │
│  Includes cookie: JSESSIONID=abc123xyz                         │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  Spring Security checks session:                               │
│  "Is JSESSIONID=abc123xyz valid?"                              │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  "YES" → Allow access to /dashboard                            │
│                                                                 │
│              ↓                                                  │
│                                                                 │
│  AuthController.dashboard() returns dashboard.html             │
│  User sees: "Welcome to Dashboard! 👋"                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2️⃣ RETURNING USER (Has Session)

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  User clicks bookmark: /dashboard                           │
│                                                              │
│              ↓                                               │
│                                                              │
│  Browser sends request:                                     │
│  GET /dashboard                                             │
│  Cookie: JSESSIONID=abc123xyz                               │
│                                                              │
│              ↓                                               │
│                                                              │
│  Spring Security checks session:                            │
│  "JSESSIONID=abc123xyz exists in memory?"                   │
│                                                              │
│              ↓                                               │
│                                                              │
│  "YES" ✅ → No need to log in again!                         │
│                                                              │
│              ↓                                               │
│                                                              │
│  AuthController.dashboard() returns dashboard.html          │
│  Instant access, no login required                          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3️⃣ LOGOUT FLOW

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  User clicks "Logout" button                                │
│  GET /logout                                                │
│  Cookie: JSESSIONID=abc123xyz                               │
│                                                              │
│              ↓                                               │
│                                                              │
│  Spring Security processes logout:                          │
│  1. Remove JSESSIONID from server memory                    │
│  2. Invalidate session                                      │
│  3. Clear security context                                  │
│                                                              │
│              ↓                                               │
│                                                              │
│  Redirect to /login?logout                                  │
│  Response includes:                                         │
│  Set-Cookie: JSESSIONID=; Path=/; Max-Age=0               │
│  (This tells browser to DELETE the cookie)                  │
│                                                              │
│              ↓                                               │
│                                                              │
│  User sees: "You have been logged out successfully"         │
│                                                              │
│              ↓                                               │
│                                                              │
│  User tries to access /dashboard                           │
│  Browser doesn't send cookie (was deleted)                  │
│                                                              │
│              ↓                                               │
│                                                              │
│  Spring Security: "No valid session!"                       │
│  Redirect to /login                                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 4️⃣ SECURITY CONFIG RULES

```
┌──────────────────────────────────────────────────────────────┐
│  URL Access Rules (Priority Order)                          │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ✅ ALLOWED WITHOUT LOGIN:                                   │
│     • /login (POST and GET)                                 │
│     • /register                                             │
│     • /error                                                │
│     • /css/** (stylesheets)                                 │
│     • /js/** (javascript)                                   │
│                                                              │
│  ❌ REQUIRES LOGIN:                                          │
│     • /dashboard                                            │
│     • /tasks                                                │
│     • /profile                                              │
│     • (anything not in whitelist)                           │
│                                                              │
│  If not logged in:                                          │
│     ↓                                                       │
│     Spring Security redirects to /login                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 5️⃣ PASSWORD HASHING WITH BCrypt

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  REGISTRATION:                                              │
│  ┌────────────────────────────────────────────┐             │
│  │ User enters password: "secret123"          │             │
│  │                                            │             │
│  │ BCryptPasswordEncoder.encode("secret123")  │             │
│  │          ↓                                 │             │
│  │ "$2a$11$abcdefgh...ijklmnopqrst123456"    │             │
│  │          ↓                                 │             │
│  │ Store in database                          │             │
│  └────────────────────────────────────────────┘             │
│                                                              │
│  ================================                           │
│                                                              │
│  LOGIN:                                                     │
│  ┌────────────────────────────────────────────┐             │
│  │ User enters password: "secret123"          │             │
│  │                                            │             │
│  │ BCryptPasswordEncoder.matches(             │             │
│  │   "secret123",                             │             │
│  │   "$2a$11$abcdefgh...ijklmnopqrst123456"  │             │
│  │ )                                          │             │
│  │          ↓                                 │             │
│  │ Password matches? ✅ YES                   │             │
│  │          ↓                                 │             │
│  │ Allow login, create session                │             │
│  └────────────────────────────────────────────┘             │
│                                                              │
│  KEY POINTS:                                                │
│  • BCrypt is ONE-WAY (can't reverse)                        │
│  • Same password hashes differently every time             │
│  • BCrypt includes "salt" for extra security              │
│  • Strength parameter = how slow to hash (security trade) │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 6️⃣ WHERE SPRING SECURITY INTERCEPTS

```
Browser Request
     ↓
┌─────────────────────────────────────┐
│  Spring Security Filter Chain       │
├─────────────────────────────────────┤
│                                     │
│  1. Check: Is URL allowed?          │
│     ✅ /login → Allow              │
│     ❌ /dashboard → Need session    │
│                                     │
│  2. Check: Valid session cookie?    │
│     ✅ JSESSIONID valid → Allow    │
│     ❌ No cookie → Redirect /login │
│                                     │
│  3. Check: User authenticated?      │
│     ✅ Yes → Continue              │
│     ❌ No → Redirect /login        │
│                                     │
└─────────────────────────────────────┘
     ↓
Your Controller (AuthController.java)
     ↓
Response to Browser
```

---

## 7️⃣ CONTROLLER vs REPOSITORY vs SERVICE

```
WEB LAYER (Controllers)
┌──────────────────────────┐
│ AuthController           │
│ Handles HTTP requests    │
│ GET /login               │
│ POST /register           │
│ GET /dashboard           │
└──────┬───────────────────┘
       │ calls
       ↓
BUSINESS LOGIC LAYER (Services)
┌──────────────────────────┐
│ UserService              │
│ registerUser()           │
│ validatePassword()       │
│ (Business rules)         │
└──────┬───────────────────┘
       │ calls
       ↓
DATA ACCESS LAYER (Repositories)
┌──────────────────────────┐
│ UserRepository           │
│ findByUsername()         │
│ save()                   │
│ (Database queries)       │
└──────────────────────────┘
       │ accesses
       ↓
DATABASE
┌──────────────────────────┐
│ users table              │
│ id, username, password   │
│ email, role              │
└──────────────────────────┘

SECURITY LAYER (Called during login)
┌──────────────────────────────────────┐
│ CustomUserDetailsService             │
│ loadUserByUsername()                 │
│ ↓ calls UserRepository.findByUsername() │
│ ↓ returns UserDetails object         │
│                                      │
│ Spring Security uses this to         │
│ compare passwords & create session   │
└──────────────────────────────────────┘
```

---

## 8️⃣ REMEMBER THIS:

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Spring provides /login automatically                       │
│     ↓                                                       │
│  Your controller only handles GET /login (show form)        │
│     ↓                                                       │
│  Spring handles POST /login (check credentials)             │
│     ↓                                                       │
│  Spring calls your UserDetailsService to load user          │
│     ↓                                                       │
│  Spring compares password with BCryptPasswordEncoder        │
│     ↓                                                       │
│  If correct: Create session + Redirect /dashboard           │
│  If wrong:   Redirect /login?error                          │
│                                                             │
│  ✅ Session lasts until:                                    │
│     • User logs out                                         │
│     • Session timeout (default 30 minutes)                  │
│     • Server restart                                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

