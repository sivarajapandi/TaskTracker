# ❓ FAQ - Session-Based Authentication

## General Questions

### Q1: What exactly does Spring Security do?

**A:** Spring Security is a framework that protects your app from unauthorized access. It:

1. **Intercepts requests** → Checks if user is authenticated
2. **Redirects** → If not authenticated, sends to /login
3. **Validates credentials** → Checks username/password
4. **Creates session** → If credentials valid, creates session cookie
5. **Protects resources** → Subsequent requests checked against session

Think of it like a security guard at a club:
- You want to go to VIP area (dashboard)
- Guard checks: "Do you have ID (session)?"
- If no → Go get ID from front (login)
- If yes → Let you in (access dashboard)

---

### Q2: Why do I need SecurityConfig?

**A:** SecurityConfig tells Spring Security:
- Which URLs are public (`/login`, `/register`)
- Which URLs need authentication (`/dashboard`)
- Where is the login page (`/login`)
- What to do after successful login (redirect to `/dashboard`)
- What to do after logout

Without it, Spring Security has default rules that might not match your app.

---

### Q3: What's the difference between authentication and authorization?

**A:**
- **Authentication** = "Who are you?" (username/password)
- **Authorization** = "What can you do?" (permissions/roles)

Example:
```
Authentication: You log in → Spring Security verifies you're "john"
Authorization: Spring checks → "john" has role "USER" → Can access /dashboard
              but not "ADMIN" → Cannot access /admin panel
```

Session-based auth handles authentication. Roles/permissions are authorization.

---

## Configuration Questions

### Q4: Why do I need to permit `/login`?

**A:** Your SecurityConfig says: `anyRequest().authenticated()`

This means EVERY request needs authentication. But if `/login` is protected, users can't reach it! Circular problem.

Solution: `requestMatchers("/login").permitAll()` = Everyone can access `/login` without authentication.

---

### Q5: Why use `@Controller` and not `@RestController`?

**A:**
- `@Controller` → Returns views (HTML pages)
- `@RestController` → Returns JSON (API responses)

For session-based auth, you need HTML forms:
```java
@Controller  // ✅ Right
public String loginPage() {
    return "login";  // Returns login.html
}

@RestController  // ❌ Wrong
public String loginPage() {
    return "login";  // Returns JSON: "login"
}
```

Users see JSON text, not a form. Login fails.

---

### Q6: Why disable CSRF?

**A:** CSRF (Cross-Site Request Forgery) is a security measure that prevents unauthorized form submissions from external sites.

We disabled it for simplicity in this tutorial:
```java
.csrf(csrf -> csrf.disable())
```

**In production:** Enable CSRF! Add hidden token to forms:
```html
<form method="POST" action="/register">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    ...
</form>
```

---

### Q7: What does `loginProcessingUrl("/login")` do?

**A:** It tells Spring Security where to handle the login form submission.

Flow:
```
GET /login  → Your controller returns form (login.html)
↓
User fills form and clicks "Login"
↓
POST /login → Spring Security intercepts (you don't handle this!)
↓
Calls UserDetailsService to validate credentials
```

If you change it to `/authenticate`:
```
loginProcessingUrl("/authenticate")
```

Then form must post to `/authenticate`:
```html
<form method="POST" action="/authenticate">
```

---

## User & Password Questions

### Q8: Why is password hashing important?

**A:** If someone steals your database:

❌ Without hashing:
```
Database leaked:
username | password
john     | secret123  ← EXPOSED!
```
Hacker knows john's password. Can log in as john.

✅ With BCrypt hashing:
```
Database leaked:
username | password
john     | $2a$12$abcdefgh...  ← Useless!
```
Hacker can't reverse the hash. Can't log in.

**Even better:** BCrypt is SLOW on purpose. Makes brute-force attacks impractical.

---

### Q9: Why use BCrypt and not other algorithms?

**A:** Common algorithms:

| Algorithm | Speed | Security | Use Case |
|-----------|-------|----------|----------|
| MD5 | Very fast | BROKEN | Don't use! |
| SHA-256 | Very fast | Not enough | Not for passwords |
| BCrypt | Slow | Excellent | ✅ Use this |
| Argon2 | Slow | Excellent | Modern alternative |

BCrypt is **intentionally slow**. One password hash takes 100ms:
- User sees 100ms delay (acceptable)
- Hacker can try only 10 passwords/second (vs 1 million with MD5)

---

### Q10: What's BCrypt "strength" parameter?

**A:** Strength = how many "rounds" of hashing to apply.

```
Strength 10: Hash in ~10ms   (weak)
Strength 11: Hash in ~20ms   (weak)
Strength 12: Hash in ~40ms   (good)
Strength 13: Hash in ~80ms   (better)
Strength 14: Hash in ~160ms  (strong)
```

Higher strength = more secure but slower.

**Recommendation:**
```java
// During registration:
new BCryptPasswordEncoder(12)  // Balanced

// If you get complaints about slow logins:
new BCryptPasswordEncoder(11)  // Faster, still secure

// If you want maximum security:
new BCryptPasswordEncoder(13)  // Slower, more secure
```

---

### Q11: Can I change password strength later?

**A:** Yes! Old passwords still verify correctly because hash includes strength info:

```
Old password hash: $2a$11$...  (strength 11)
New code uses: $2a$13$...     (strength 13)
```

When user logs in with old password:
- Spring verifies against old hash (strength 11) ✅ Works
- New registrations use strength 13 automatically

---

### Q12: What if user forgets password?

**A:** Current system: You have to create that feature!

Basic flow:
1. User clicks "Forgot Password"
2. Enters email
3. App generates reset token
4. Sends email with reset link
5. User clicks link, enters new password
6. New password gets hashed and saved

This is NOT included in session-based auth tutorial. Advanced topic.

---

## Session & Cookie Questions

### Q13: How long does a session last?

**A:** Default: **30 minutes** of inactivity.

Every request "resets the timer". If user inactive for 30 mins → session expires.

Configure in `application.properties`:
```properties
server.servlet.session.timeout=60m
```

---

### Q14: Where is session data stored?

**A:** In **server memory** by default.

```
Server memory:
┌─────────────────────────┐
│ Session Store           │
├─────────────────────────┤
│ ID: abc123xyz           │
│ User: john              │
│ Roles: [ROLE_USER]      │
│ Created: 2026-02-21     │
├─────────────────────────┤
│ ID: def456abc           │
│ User: jane              │
│ Roles: [ROLE_USER]      │
└─────────────────────────┘
```

**Problem:** If server restarts, ALL sessions lost!

**Solution for production:** Use Redis or database:
```properties
# Store sessions in Redis (distributed)
spring.session.store-type=redis
```

---

### Q15: What is a JSESSIONID cookie?

**A:** It's the session ID. Browser keeps it and sends with every request.

Browser storage:
```
Cookie: JSESSIONID=abc123xyz; Path=/; HttpOnly
```

When browser requests `/dashboard`:
```
GET /dashboard
Cookie: JSESSIONID=abc123xyz
```

Server says: "I know this ID. This is john's session. Allow access."

**HttpOnly flag:** JavaScript cannot access it (security).

---

### Q16: Can multiple browsers have same session?

**A:** **No.** Each browser gets unique JSESSIONID.

Browser 1 (Firefox):
```
JSESSIONID=abc123xyz
→ Login as john
→ Can access john's dashboard
```

Browser 2 (Chrome):
```
JSESSIONID=def456abc
→ Not logged in
→ Redirected to /login
```

Different session IDs = different sessions.

---

## HTML Form Questions

### Q17: Why are field names important?

**A:** Spring Security looks for specific field names:

```java
.usernameParameter("username")  // Expects HTML field named "username"
.passwordParameter("password")  // Expects HTML field named "password"
```

If you use wrong names:
```html
<!-- WRONG: -->
<input type="text" name="user" />        ← Field named "user"
<input type="password" name="pwd" />     ← Field named "pwd"
```

Spring won't find them → "Invalid credentials"

**Correct:**
```html
<input type="text" name="username" />
<input type="password" name="password" />
```

---

### Q18: Can I change field names?

**A:** Yes! In SecurityConfig:

```java
.usernameParameter("email")    // Look for field named "email"
.passwordParameter("secret")   // Look for field named "secret"
```

Then in HTML:
```html
<input type="email" name="email" />
<input type="password" name="secret" />
```

**But:** Convention is to use "username" and "password". Stick with it.

---

### Q19: Why POST for login, not GET?

**A:** Security!

❌ GET is visible in URL:
```
http://localhost:8080/login?username=john&password=secret123
```
Password visible in browser history! 😱

✅ POST hides credentials in request body:
```
GET http://localhost:8080/login
Body: username=john&password=secret123  (not visible in URL)
```

Browser history doesn't show passwords.

---

## UserDetailsService Questions

### Q20: When is UserDetailsService called?

**A:** Only during login (POST /login):

```
User logs in
    ↓
Spring Security: "Let me get user details"
    ↓
Calls UserDetailsService.loadUserByUsername("john")
    ↓
You query database and return user
    ↓
Spring compares passwords
    ↓
Success? → Create session
Failure? → Show error
```

**NOT called on every request.** Only on login.

On subsequent requests, Spring uses session cookie (faster).

---

### Q21: What if UserDetailsService throws exception?

**A:** User sees "Invalid username or password" (safe).

Good practice: Always throw `UsernameNotFoundException`:

```java
@Override
public UserDetails loadUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
}
```

Never leak info like "Username doesn't exist" or "Email is invalid".

---

### Q22: Can I load user roles in UserDetailsService?

**A:** Yes! That's exactly what it does:

```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(getAuthorities(user.getRole()))  // ← Load roles
        .build();
}

private Collection<? extends GrantedAuthority> getAuthorities(String role) {
    return Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + role)
    );
}
```

---

## Error & Troubleshooting Questions

### Q23: "Invalid username or password" but credentials are correct?

**A:** Check these:

1. **Password not hashed during registration?**
   ```java
   // WRONG:
   user.setPassword(password);
   
   // RIGHT:
   user.setPassword(passwordEncoder.encode(password));
   ```

2. **UserDetailsService not loading user correctly?**
   - Add debug print: `System.out.println("Loading user: " + username);`
   - Check if user exists in database: `SELECT * FROM users;`

3. **Field names wrong in HTML?**
   ```html
   <!-- Must be exactly "username" and "password" -->
   <input name="username" />
   <input name="password" />
   ```

---

### Q24: Redirect loop between /login and homepage?

**A:** Usually `/login` not permitted:

```java
// WRONG:
.requestMatchers("/register").permitAll()  // Missing /login!

// RIGHT:
.requestMatchers("/register", "/login").permitAll()
```

---

### Q25: Session lost after page refresh?

**A:** Usually timeout:

```properties
# In application.properties
server.servlet.session.timeout=30m
```

Or check:
- Is JSESSIONID cookie present? (F12 → Application → Cookies)
- Did you close browser? (clears cookies)
- Did server restart? (clears in-memory sessions)

---

## Performance Questions

### Q26: Is session-based auth slow?

**A:** No! Very fast after login:

1st request (login):
```
POST /login → UserDetailsService query (10ms) → BCrypt verify (40ms) → Create session (1ms) = ~50ms
```

Subsequent requests:
```
GET /dashboard → Check session ID (1ms) → Return page (5ms) = ~6ms
```

Much faster than database query on every request!

---

### Q27: What if I have 1000 concurrent users?

**A:** All sessions stored in server memory:

```
1000 sessions × ~1KB per session = ~1MB RAM used
```

Minimal overhead. No problem.

**But:** If you deploy on multiple servers:
```
Server 1: Stores session for user A
Server 2: Receives request from user A
Server 2: Doesn't have session → Redirects to /login
```

Solution: Use shared session store (Redis, database).

---

## Next Steps Questions

### Q28: How do I add admin role?

**A:**
1. Add admin users in database with role="ADMIN"
2. In SecurityConfig:
   ```java
   .requestMatchers("/admin/**")
       .hasRole("ADMIN")  // Only ROLE_ADMIN can access
   ```
3. UserDetailsService already loads role automatically

---

### Q29: How do I log out programmatically?

**A:** Call SecurityContextHolder:

```java
@GetMapping("/logout-custom")
public String logoutCustom(HttpServletRequest request, HttpServletResponse response) {
    SecurityContextHolder.clearContext();  // Clear session
    new SecurityContextLogoutHandler().logout(request, response, null);
    return "redirect:/login";
}
```

Usually not needed. Use `/logout` instead.

---

### Q30: Can I remember login like "Remember Me"?

**A:** Yes! In SecurityConfig:

```java
.rememberMe(remember -> remember
    .rememberMeParameter("remember-me")
    .tokenValiditySeconds(2592000)  // 30 days
)
```

In HTML:
```html
<input type="checkbox" name="remember-me" /> Remember me
```

User stays logged in even after closing browser (for 30 days).

---

### Q31: How do I switch to JWT (stateless)?

**A:** Different approach entirely:

**Session-based (current):**
- Server stores session
- Browser sends JSESSIONID cookie
- Stateful

**JWT-based (stateless):**
- Server generates token with user info
- Browser sends token in Authorization header
- No server storage
- Better for distributed systems

JWT is more complex. Learn session-based first!

---

### Q32: How do I add email verification?

**A:** After registration:
1. Generate random token
2. Send email with verification link
3. User clicks link
4. Mark user as "verified" in database
5. Only verified users can log in

Advanced feature. Not in this tutorial.

---

## Final Tips

### Q33: What's the most common mistake?

**A:** Using `@RestController` instead of `@Controller`.

This breaks everything because:
- `/login` returns JSON instead of HTML
- Forms can't submit
- Authentication fails

### Q34: What should I study next after this?

**A:** In order:
1. ✅ Session-based auth (you are here)
2. Role-based access control (RBAC)
3. Password reset functionality
4. Email verification
5. Social login (OAuth2/Google)
6. Two-factor authentication (2FA)
7. JWT tokens (stateless auth)

### Q35: Where to get help?

**A:** Resources:
- Spring Security docs: https://spring.io/projects/spring-security
- Stack Overflow: Tag `spring-security`
- Spring Boot docs: https://spring.io/projects/spring-boot
- This cheat sheet: CHEAT_SHEET.md
- This guide: SESSION_AUTH_GUIDE.md

---

**If you have more questions, feel free to ask!** 🚀


