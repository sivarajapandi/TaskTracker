# 🚀 Session-Based Authentication - Quick Reference Cheat Sheet

## 1️⃣ Project Setup Checklist

```
☐ Add dependency: org.springframework.boot:spring-boot-starter-security
☐ Add dependency: org.springframework.boot:spring-boot-starter-thymeleaf
☐ Add dependency: com.mysql:mysql-connector-j (MySQL driver)
☐ Add dependency: org.projectlombok:lombok
☐ Configure: spring.datasource.url in application.properties
☐ Configure: spring.datasource.username in application.properties
☐ Configure: spring.datasource.password in application.properties
```

---

## 2️⃣ Critical File Locations

```
src/main/java/
├── config/
│   └── SecurityConfig.java ← Configure Spring Security
├── controller/
│   └── AuthController.java ← Handle /login, /register, /dashboard GET
├── entity/
│   └── User.java ← User model
├── repository/
│   └── UserRepository.java ← Database queries
├── security/
│   └── CustomUserDetailsService.java ← Load user during login (CRITICAL!)
└── service/
    └── UserService.java ← Register user, hash password

src/main/resources/
├── templates/
│   ├── login.html ← Login form
│   ├── register.html ← Registration form
│   └── dashboard.html ← Protected page
└── application.properties ← Database config
```

---

## 3️⃣ SecurityConfig.java Template

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/error", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength: 10-14 recommended
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build();
    }
}
```

---

## 4️⃣ AuthController.java Template

```java
@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match");
            return "register";
        }
        
        try {
            userService.registerUser(username, email, password);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
```

**🔴 COMMON MISTAKE:** Use `@Controller`, not `@RestController`!

---

## 5️⃣ CustomUserDetailsService.java Template

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword()) // Hashed password from DB
            .authorities(getAuthorities(user.getRole()))
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)
        );
    }
}
```

**🔴 COMMON MISTAKE:** Spring calls this during login automatically. Don't call it yourself!

---

## 6️⃣ UserService.java Template

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public void registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // HASH!
        user.setRole("USER");
        
        userRepository.save(user);
    }
}
```

**🔴 COMMON MISTAKE:** Don't store plain password! Use `passwordEncoder.encode()`!

---

## 7️⃣ User.java Entity Template

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // Hashed!
    
    @Column(nullable = false)
    private String role; // "USER" or "ADMIN"
    
    private LocalDateTime createdAt;
    
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

---

## 8️⃣ UserRepository.java Template

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

---

## 9️⃣ login.html Template

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Login</title>
</head>
<body>
    <h1>Login</h1>
    
    <!-- Error message if login failed -->
    <div th:if="${param.error}">
        <p style="color: red;">Invalid username or password</p>
    </div>
    
    <!-- Success message after registration -->
    <div th:if="${param.success}">
        <p style="color: green;">Account created! Please log in.</p>
    </div>
    
    <!-- LOGIN FORM (CRITICAL!) -->
    <form method="POST" action="/login">
        <!-- MUST be named "username" -->
        <input type="text" name="username" placeholder="Username" required>
        
        <!-- MUST be named "password" -->
        <input type="password" name="password" placeholder="Password" required>
        
        <button type="submit">Login</button>
    </form>
    
    <p>Don't have an account? <a href="/register">Register here</a></p>
</body>
</html>
```

**🔴 COMMON MISTAKES:**
- Form method must be `POST`
- Form action must be `/login`
- Input names MUST be `username` and `password`

---

## 🔟 register.html Template

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Register</title>
</head>
<body>
    <h1>Create Account</h1>
    
    <!-- Error messages from controller -->
    <div th:if="${error}">
        <p style="color: red;" th:text="${error}"></p>
    </div>
    
    <!-- REGISTRATION FORM -->
    <form method="POST" action="/register">
        <!-- These field names MUST match @RequestParam names -->
        <input type="text" name="username" placeholder="Username" required>
        <input type="email" name="email" placeholder="Email" required>
        <input type="password" name="password" placeholder="Password" required>
        <input type="password" name="confirmPassword" placeholder="Confirm Password" required>
        
        <button type="submit">Register</button>
    </form>
    
    <p>Already have an account? <a href="/login">Login here</a></p>
</body>
</html>
```

---

## 1️⃣1️⃣ BCrypt Strength Comparison

```
Strength | Time (ms) | Security | Use Case
---------|-----------|----------|----------
10       | 10ms      | Low      | Development/Testing
11       | 20ms      | Low      | Fast logins
12       | 40ms      | Medium   | ✅ Recommended (DEFAULT)
13       | 80ms      | High     | High security apps
14       | 160ms     | Very High| Financial/Healthcare
15+      | >300ms    | Maximum  | Too slow for users
```

**Recommendation:** Start with 12, adjust based on login response time.

---

## 1️⃣2️⃣ Common Errors & Solutions

### ❌ Error: "Invalid username or password" (correct credentials)

**Cause 1:** Password not hashed during registration
```
FIX: Use passwordEncoder.encode(password) before saving
```

**Cause 2:** CustomUserDetailsService not implemented
```
FIX: Create CustomUserDetailsService class and make sure it's injected
```

**Cause 3:** Field names don't match
```
FIX: Verify HTML input names are "username" and "password"
```

---

### ❌ Error: Redirect loop to /login

**Cause 1:** /login not permitted in SecurityConfig
```
FIX: Add "/login" to requestMatchers().permitAll()
```

**Cause 2:** /login endpoint not accessible
```
FIX: Create GET /login in AuthController
```

---

### ❌ Error: "No session found" after refresh

**Cause:** Session timeout (default 30 minutes)
```
FIX (in application.properties):
server.servlet.session.timeout=30m
```

---

### ❌ Error: "Cannot post to /login"

**Cause 1:** CSRF protection enabled (we disabled it)
```
FIX: Ensure .csrf(csrf -> csrf.disable()) is in SecurityConfig
```

**Cause 2:** Form method not POST
```
FIX: Change form method="POST"
```

---

## 1️⃣3️⃣ Testing Checklist

```
Registration Flow:
☐ Can register with new username
☐ Error on duplicate username
☐ Error if passwords don't match
☐ Error if password too short
☐ Redirects to login after success

Login Flow:
☐ Can login with correct credentials
☐ Error on wrong password
☐ Error on non-existent username
☐ Redirects to dashboard after success
☐ Shows error message on failure

Session Flow:
☐ Session persists after page refresh
☐ Can access dashboard multiple times
☐ Session expires after timeout
☐ Cannot access /dashboard without session

Logout Flow:
☐ Logout clears session
☐ Redirected to /login?logout
☐ Cannot access /dashboard after logout
☐ Must log in again

Protected Pages:
☐ Can access /dashboard when authenticated
☐ Redirected to /login when not authenticated
☐ Other protected URLs behave same way
```

---

## 1️⃣4️⃣ Quick Troubleshooting Flow

```
Are you getting a login page?
├─ YES → Can you log in?
│        ├─ YES → Session working? (Refresh page)
│        │        ├─ YES → ✅ Everything works!
│        │        └─ NO  → Check session timeout
│        └─ NO  → Check CustomUserDetailsService
└─ NO  → Check AuthController GET /login
         Check SecurityConfig permits /login
```

---

## 1️⃣5️⃣ Database Inspection (MySQL)

```sql
-- Check if users table exists
SHOW TABLES;

-- View all users
SELECT * FROM users;

-- Check specific user's password hash
SELECT username, password FROM users WHERE username = 'testuser';

-- Sample hashed password looks like:
-- $2a$12$abcdefghijklmnopqrstuvwxyz1234567890
-- └─┬─┘ └──┬─┘ └──────────────────────────────┘
--   └─ BCrypt prefix  └─ Cost factor  └─ Hash

-- Check if password is actually hashed (starts with $2a$)
SELECT username, LEFT(password, 5) as pwd_prefix FROM users;
```

---

## 🔑 KEY TAKEAWAYS (Memorize These!)

| Concept | Remember |
|---------|----------|
| `/login` | Spring provides it automatically. Your controller just handles GET (show form) |
| POST /login | Spring Security handles this. You don't write code for it |
| Password | ALWAYS hash with BCrypt before storing. Never store plain text |
| Session | Created automatically after login. Stored in server memory |
| UserDetailsService | Spring calls this during login to load user from database |
| `@Controller` | Use for views. Returns HTML |
| `@RestController` | Use for APIs. Returns JSON |
| Field names | HTML input names MUST be "username" and "password" |
| Permissions | Always permit /login, /register, /error in SecurityConfig |
| BCrypt strength | 12 is recommended. Higher = slower (more secure) |


