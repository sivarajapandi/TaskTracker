# 🎯 Hands-On Exercises: Session-Based Authentication

## Exercise 1: Create User Entity

**Goal:** Understand how to model users for authentication

**File to create:** `src/main/java/com/siva/taskTracker/entity/User.java`

**Code Structure:**
```java
@Entity
@Table(name = "users")
public class User {
    
    // ❓ QUESTIONS:
    // 1. Why use @Entity?
    // 2. Why make username unique?
    // 3. Should we store plain password?
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;  // HASHED password, not plain!
    
    @Column(nullable = false)
    private String role;  // "USER" or "ADMIN"
    
    private LocalDateTime createdAt;
    
    // TODO: Add @PrePersist method to set createdAt automatically
    
    // Add getter and setter for each field
}
```

**What to implement:**
- [ ] Add all fields with proper annotations
- [ ] Add `@PrePersist` to auto-set `createdAt` to current time
- [ ] Generate getters and setters (or use Lombok `@Getter @Setter`)
- [ ] Add `@NoArgsConstructor` and `@AllArgsConstructor` (Lombok)

**Test Question:** Why is the password field called "password" but we store hashed value?
> Answer: The field name doesn't matter. The value stored is hashed. When user logs in, Spring hashes their input and compares.

---

## Exercise 2: Create UserRepository

**Goal:** Understand database queries for authentication

**File to create:** `src/main/java/com/siva/taskTracker/repository/UserRepository.java`

**Code Structure:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ❓ QUESTION: Why Optional?
    // Answer: A user might not exist, Optional handles null safely
    
    Optional<User> findByUsername(String username);
    
    // BONUS: Add these methods
    // Optional<User> findByEmail(String email);
    // boolean existsByUsername(String username);
}
```

**What to implement:**
- [ ] Extend `JpaRepository<User, Long>`
- [ ] Add `findByUsername()` method (Spring will generate SQL)
- [ ] Add `findByEmail()` as bonus

**Test Question:** What SQL does Spring generate for `findByUsername()`?
> Answer: `SELECT * FROM users WHERE username = ?`

---

## Exercise 3: Implement UserService

**Goal:** Understand registration and password hashing

**File to update:** `src/main/java/com/siva/taskTracker/service/UserService.java`

**Code Structure:**
```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // ❓ QUESTION: Why inject BCryptPasswordEncoder?
    // Answer: To hash password before storing
    
    public void registerUser(String username, String email, String password) {
        // STEP 1: Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // STEP 2: Hash the password
        String hashedPassword = passwordEncoder.encode(password);
        
        // STEP 3: Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);  // Store HASHED password
        user.setRole("USER");
        
        // STEP 4: Save to database
        userRepository.save(user);
    }
    
    // BONUS METHODS:
    // public boolean authenticate(String username, String password) { }
    // public User findByUsername(String username) { }
}
```

**What to implement:**
- [ ] Inject `UserRepository` and `BCryptPasswordEncoder`
- [ ] Implement `registerUser()` method
- [ ] Check for duplicate username
- [ ] Hash password with BCrypt
- [ ] Set default role to "USER"

**Test Question:** What happens if you don't hash the password?
> Answer: SECURITY DISASTER! Anyone with database access sees plain passwords.

---

## Exercise 4: Implement CustomUserDetailsService

**Goal:** Understand how Spring loads users during login

**File to update:** `src/main/java/com/siva/taskTracker/security/CustomUserDetailsService.java`

**Code Structure:**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    // ❓ QUESTION: When does this method get called?
    // Answer: During POST /login, Spring Security calls this
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // STEP 1: Find user in database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // STEP 2: Build UserDetails object
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())  // Hashed password from database
            .authorities(getAuthorities(user.getRole()))
            .build();
    }
    
    // Helper method to convert role string to GrantedAuthority
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)
        );
    }
}
```

**What to implement:**
- [ ] Inject `UserRepository`
- [ ] Implement `loadUserByUsername()`
- [ ] Query database for user
- [ ] Return UserDetails object
- [ ] Implement `getAuthorities()` helper

**Test Question:** Why do we add "ROLE_" prefix?
> Answer: Spring Security convention. Roles like "USER" become "ROLE_USER" for authorization checks.

---

## Exercise 5: Update AuthController with Registration

**Goal:** Handle form submissions for registration

**File to update:** `src/main/java/com/siva/taskTracker/controller/AuthController.java`

**Code Structure:**
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
    
    // ❓ QUESTION: What does @RequestParam do?
    // Answer: Maps HTML form fields to method parameters
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        
        // STEP 1: Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match");
            return "register";  // Return form with error
        }
        
        // STEP 2: Validate password strength
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "register";
        }
        
        try {
            // STEP 3: Call service to register
            userService.registerUser(username, email, password);
            
            // STEP 4: Redirect to login with success message
            return "redirect:/login?success";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";  // Return form with error
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
```

**What to implement:**
- [ ] Inject `UserService`
- [ ] Implement `POST /register` method
- [ ] Validate password match
- [ ] Validate password strength (minimum length)
- [ ] Call `userService.registerUser()`
- [ ] Redirect on success
- [ ] Return form with error on failure

**Test Question:** Why use try-catch?
> Answer: To catch database errors (duplicate username) and show user-friendly message.

---

## Exercise 6: Update HTML Forms

**Goal:** Ensure forms submit data correctly

**File to check/update:** `src/main/resources/templates/register.html`

**Critical parts:**
```html
<!-- ❓ QUESTION: Why is name attribute important? -->
<!-- Answer: It maps HTML field to @RequestParam name -->

<form method="POST" action="/register">
    <!-- Must match @RequestParam String username -->
    <input type="text" name="username" required>
    
    <!-- Must match @RequestParam String email -->
    <input type="email" name="email" required>
    
    <!-- Must match @RequestParam String password -->
    <input type="password" name="password" required>
    
    <!-- Must match @RequestParam String confirmPassword -->
    <input type="password" name="confirmPassword" required>
    
    <button type="submit">Register</button>
</form>

<!-- Display errors if registration failed -->
<div th:if="${param.error}">
    <p th:text="${param.error}" style="color: red;"></p>
</div>

<!-- Display success if registration succeeded -->
<div th:if="${param.success}">
    <p style="color: green;">Account created! Please log in.</p>
</div>
```

**What to verify:**
- [ ] Form method is POST
- [ ] Form action is `/register`
- [ ] All input names match `@RequestParam` names
- [ ] Error messages display correctly

---

## Exercise 7: Test the Full Flow

**Goal:** Verify end-to-end authentication works

**Step 1: Run application**
```bash
./gradlew bootRun
```

**Step 2: Register new user**
- Navigate to: `http://localhost:8080/register`
- Fill form:
  - Username: `testuser`
  - Email: `test@example.com`
  - Password: `password123`
  - Confirm: `password123`
- Click "Register"
- ✅ Should redirect to `/login?success`

**Step 3: Test login**
- Enter username: `testuser`
- Enter password: `password123`
- Click "Login"
- ✅ Should show dashboard

**Step 4: Test session persistence**
- Refresh page: `F5` or `Ctrl+R`
- ✅ Should still see dashboard (session alive)

**Step 5: Test logout**
- Click "Logout" button
- ✅ Should show `/login?logout` message

**Step 6: Test protected page**
- Clear cookies (F12 → Application → Cookies → Delete)
- Navigate to: `http://localhost:8080/dashboard`
- ✅ Should redirect to `/login`

**Test Checklist:**
- [ ] Can register new user
- [ ] Can log in with correct credentials
- [ ] Cannot log in with wrong password
- [ ] Session persists after refresh
- [ ] Can log out successfully
- [ ] Protected pages redirect to login
- [ ] Cannot access /dashboard without login

---

## Exercise 8: Add Password Strength Indicator (Bonus)

**Goal:** Enhance registration form with real-time feedback

**File to update:** `src/main/resources/templates/register.html`

**Add this JavaScript:**
```html
<script>
function checkPasswordStrength(password) {
    const strength = {
        0: "Very Weak",
        1: "Weak", 
        2: "Fair",
        3: "Good",
        4: "Strong"
    };
    
    let score = 0;
    if (password.length >= 8) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[!@#$%^&*]/.test(password)) score++;
    
    const indicator = document.getElementById("strengthIndicator");
    indicator.textContent = strength[score];
    indicator.style.color = ["red", "orange", "yellow", "lightgreen", "green"][score];
}

document.getElementById("password").addEventListener("keyup", function() {
    checkPasswordStrength(this.value);
});
</script>
```

**Add HTML:**
```html
<div class="form-group">
    <label for="password">Password:</label>
    <input type="password" id="password" name="password" required>
    <small id="strengthIndicator">Enter password</small>
</div>
```

---

## 🎓 Learning Outcomes

After completing these exercises, you should understand:

- ✅ How to model users with JPA entities
- ✅ How to query database with repositories
- ✅ How to hash passwords with BCrypt
- ✅ How Spring Security loads users during login
- ✅ How to handle form submissions in controllers
- ✅ How sessions work and persist
- ✅ How to validate user input
- ✅ How to show error messages to users

---

## 🔍 Debugging Tips

**If registration fails:**
1. Check browser console for errors (F12)
2. Check IDE console for exceptions
3. Verify field names match `@RequestParam` names
4. Check database connection (MySQL running?)

**If login doesn't work:**
1. Verify user was registered (check database)
2. Check password is hashed (starts with `$2a$`)
3. Verify `CustomUserDetailsService` is being called
4. Check `UserRepository.findByUsername()` implementation

**If session doesn't persist:**
1. Check browser cookies (F12 → Application → Cookies)
2. Verify `JSESSIONID` cookie exists
3. Check session timeout settings in `application.properties`
4. Verify `/dashboard` is not bypassing security

---

## 📚 Related Topics to Learn Next

- [ ] Role-based access control (RBAC)
- [ ] Different password encoding strategies
- [ ] CSRF protection
- [ ] Remember-me functionality
- [ ] Social login (OAuth2)
- [ ] JWT tokens (stateless auth)
- [ ] Password reset functionality
- [ ] Email verification


