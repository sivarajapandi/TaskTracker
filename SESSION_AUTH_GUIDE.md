# 🎓 Session-Based Authentication Implementation Guide

## What We Just Built

You now have a **complete session-based authentication flow** in Spring Boot. Here's what each piece does:

---

## 🔑 Key Concepts

### 1. **What is Session-Based Authentication?**
- User logs in with username/password
- Spring Security **creates a session** on the server
- Session is stored in memory (server keeps track of who's logged in)
- Each request includes a session cookie
- Server validates cookie → knows user is authenticated
- When user logs out → session is destroyed

### 2. **Spring Security's `/login` Endpoint**
Spring Security **provides a default `/login` endpoint automatically** when you use `formLogin()`.

**How it works:**
```
GET /login  → Shows login form (your controller returns the HTML view)
POST /login → Spring Security handles authentication (you don't write code for this)
```

**Important:** Your controller's `GET /login` provides the **form**, but Spring Security handles the **POST**.

---

## 📁 File Structure Explained

```
src/main/java/com/siva/taskTracker/
├── config/
│   └── SecurityConfig.java          ← Configuration & security rules
├── controller/
│   └── AuthController.java          ← Handles GET requests for pages
├── security/
│   └── CustomUserDetailsService.java ← Loads users from database
└── service/
    └── UserService.java            ← Handles user registration

src/main/resources/
└── templates/
    ├── login.html                  ← Login form (HTML + CSS)
    ├── register.html               ← Registration form
    └── dashboard.html              ← Protected dashboard page
```

---

## 🔐 Authentication Flow

```
User tries to access /dashboard
         ↓
Spring Security checks: Is user authenticated?
         ↓
   NO → Redirect to /login
         ↓
User enters username & password
         ↓
Form POSTs to /login (Spring Security intercepts)
         ↓
Spring Security calls UserDetailsService.loadUserByUsername()
         ↓
UserDetailsService queries database for user
         ↓
Spring Security compares password (using BCryptPasswordEncoder)
         ↓
Password matches? → Create session + Redirect to /dashboard
Password wrong?  → Redirect to /login?error
```

---

## 📋 What Each File Does

### **SecurityConfig.java**
**Purpose:** Configure Spring Security rules

Key settings:
```java
.requestMatchers("/register", "/login", "/error", "/css/**", "/js/**").permitAll()
  ↓
  Only these URLs are accessible WITHOUT login

.anyRequest().authenticated()
  ↓
  ALL other URLs require authentication

.loginPage("/login")
  ↓
  When user not authenticated, redirect here

.loginProcessingUrl("/login")
  ↓
  Form posts to this URL (Spring Security handles it)

.defaultSuccessUrl("/dashboard", true)
  ↓
  After successful login, redirect here
```

### **AuthController.java**
**Purpose:** Return HTML pages

```java
@GetMapping("/login")
public String loginPage() {
    return "login"; // Returns templates/login.html
}
```

**Important:** Use `@Controller`, NOT `@RestController`
- `@RestController` → Returns JSON (API)
- `@Controller` → Returns HTML views (web pages)

### **CustomUserDetailsService.java**
**Purpose:** Load user from database during login

Spring Security calls this automatically:
```java
@Override
public UserDetails loadUserByUsername(String username) {
    // Query database
    // Return UserDetails object
}
```

Later you'll implement:
```java
User user = userRepository.findByUsername(username)
    .orElseThrow(...);
    
return User.builder()
    .username(user.getUsername())
    .password(user.getPassword()) // Already hashed with BCrypt
    .authorities(getAuthorities(user.getRole()))
    .build();
```

### **login.html**
**Critical parts:**
```html
<form method="POST" action="/login">
    <!-- These field names are REQUIRED -->
    <input type="text" name="username" />
    <input type="password" name="password" />
    <button type="submit">Login</button>
</form>
```

**Why these names?** Spring Security looks for `username` and `password` by default.

---

## ❌ Common Mistakes (Don't Do These!)

### ❌ Mistake 1: Forgetting to permit `/login`
```java
// WRONG:
.requestMatchers("/register").permitAll()
.anyRequest().authenticated()
```
Result: Redirect loop! User can't reach login page.

✅ **Fix:**
```java
.requestMatchers("/register", "/login").permitAll()
```

### ❌ Mistake 2: Using `@RestController` for login page
```java
// WRONG:
@RestController
public String loginPage() {
    return "Login Page"; // Returns JSON!
}
```
Result: User sees JSON text, not HTML form.

✅ **Fix:**
```java
@Controller
public String loginPage() {
    return "login"; // Returns HTML view
}
```

### ❌ Mistake 3: Wrong field names in HTML
```html
<!-- WRONG -->
<input type="text" name="user" />
<input type="password" name="pwd" />
```
Result: Spring Security doesn't find credentials.

✅ **Fix:**
```html
<input type="text" name="username" />
<input type="password" name="password" />
```

### ❌ Mistake 4: Not hashing passwords
```java
// WRONG: Storing plain password
userService.save(new User(username, password));
```
Result: Security breach!

✅ **Fix:**
```java
String hashedPassword = passwordEncoder.encode(password);
userService.save(new User(username, hashedPassword));
```

---

## 🚀 Next Steps (What to Implement)

### Step 1: Create User Entity
Create `src/main/java/com/siva/taskTracker/entity/User.java`:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;
    
    private String password; // Store HASHED password
    
    private String role; // e.g., "USER", "ADMIN"
    // getters/setters
}
```

### Step 2: Create UserRepository
Create `src/main/java/com/siva/taskTracker/repository/UserRepository.java`:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### Step 3: Implement UserService
Update `src/main/java/com/siva/taskTracker/service/UserService.java`:
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public void registerUser(String username, String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole("USER");
        userRepository.save(user);
    }
}
```

### Step 4: Update CustomUserDetailsService
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
            ))
            .build();
    }
}
```

### Step 5: Update AuthController
```java
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
        model.addAttribute("error", "Username or email already exists");
        return "register";
    }
}
```

---

## 🧪 Testing the Flow

1. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

2. **Test Registration:**
   - Go to `http://localhost:8080/register`
   - Fill form, click Register
   - Should redirect to login

3. **Test Login:**
   - Go to `http://localhost:8080/login`
   - Enter credentials
   - Should redirect to `/dashboard`

4. **Test Protected Page:**
   - Close browser (clear session)
   - Go to `http://localhost:8080/dashboard`
   - Should redirect to `/login` (not authenticated)

5. **Test Logout:**
   - Click "Logout" button
   - Should redirect to `/login?logout`

---

## 💡 Key Takeaways

1. **Spring Security provides `/login` automatically** via `formLogin()`
2. **Your controller only needs to provide the HTML form** (GET request)
3. **Spring Security handles POST /login** (you don't write code for it)
4. **You MUST permit `/login` in security config** or users can't reach it
5. **Use `@Controller` for views, `@RestController` for APIs**
6. **Passwords must be hashed with BCrypt** before storing
7. **UserDetailsService is called during login** to load user from database
8. **Session is created automatically** after successful authentication

---

## 📚 Reference Links

- Spring Security Docs: https://spring.io/projects/spring-security
- BCrypt Strength Guide: https://owasp.org/www-community/attacks/Password_Spraying_Attack
- Thymeleaf Docs: https://www.thymeleaf.org/


