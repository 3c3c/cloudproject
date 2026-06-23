spring boot 整合 jwt redis springsecurity 如何做登录权限功能



已阅读 10 个网页

# Spring Boot + Spring Security + JWT + Redis 实现登录权限功能

这是一个经典的企业级认证授权方案，核心思路是：**JWT 做无状态身份凭证，Redis 做有状态的令牌管理，Spring Security 做安全防线**。

------

## 一、架构设计思路

为什么需要同时使用 JWT 和 Redis？这是这套方案的关键所在。

| 组件                | 职责                                                 | 解决的痛点                                                   |
| :------------------ | :--------------------------------------------------- | :----------------------------------------------------------- |
| **JWT**             | 携带用户身份信息（userId、username），自带签名防篡改 | 无状态，服务端不存 session，便于分布式部署                   |
| **Redis**           | 存储有效 token，管理 token 生命周期                  | 实现“强制踢人下线”、“单点登录”、“退出登录”等 JWT 无法单独完成的能力 |
| **Spring Security** | 认证和授权的统一入口，过滤器链管理                   | 提供完整的权限控制体系，避免自己造轮子                       |

> JWT 本身无法撤销——签发后在有效期内始终有效。结合 Redis 后，每次请求都校验 Redis 中 token 是否存在且匹配，即可实现“主动失效”。

------

## 二、项目依赖配置

xml

```
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Data Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- JWT 库（jjwt 是目前最主流的选择） -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```



------

## 三、核心代码实现

### 1. JWT 工具类

java

```
@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String secret;  // 密钥，建议配置在 application.yml 中
    
    @Value("${jwt.expiration}")
    private Long expiration;  // 过期时间（毫秒）
    
    @Value("${jwt.header}")
    private String header;  // token 存放的请求头名称，通常为 "Authorization"
    
    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;  // token 前缀，通常为 "Bearer "
    
    /**
     * 生成 JWT token
     */
    public String generateToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        // 将权限集合转换为字符串
        String authoritiesStr = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("authorities", authoritiesStr)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从 token 中获取 Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 验证 token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```



### 2. 自定义 UserDetails 和 UserDetailsService

java

```
@Data
public class LoginUser implements UserDetails {
    
    private Long userId;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return true; }
}
```



java

```
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;  // 你的 DAO 层
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 查询用户权限（从角色-权限表获取）
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // ... 根据实际权限表结构填充 authorities
        
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());  // 数据库中存储的是加密后的密码
        loginUser.setAuthorities(authorities);
        
        return loginUser;
    }
}
```



### 3. JWT 认证过滤器（最关键的部分）

这个过滤器会在每次请求时拦截，验证 token 并将认证信息存入 Spring Security 上下文：

java

```
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        // 1. 获取 token
        String token = request.getHeader(jwtUtils.getHeader());
        if (token != null && token.startsWith(jwtUtils.getTokenPrefix())) {
            token = token.substring(jwtUtils.getTokenPrefix().length());
        }
        
        // 2. 如果没有 token 或不以指定前缀开头，放行让后续流程处理（可能会被拒绝访问）
        if (StringUtils.isEmpty(token)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 3. 校验 Redis 黑名单（退出登录的 token 会被加入黑名单）
        String blackKey = "blacklist:" + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blackKey))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token 已失效");
            return;
        }
        
        // 4. 验证 JWT token 有效性
        if (!jwtUtils.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 5. 从 token 解析用户信息
        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.getSubject();
        
        // 6. 【核心】Redis 双重校验：验证 Redis 中存储的 token 是否与当前 token 一致
        //    这步实现了"强制下线"和"单点登录"功能
        String redisToken = redisTemplate.opsForValue().get("login:token:" + username);
        if (redisToken == null || !redisToken.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token 已过期或被踢下线");
            return;
        }
        
        // 7. 从数据库/缓存获取用户信息，构建 Authentication 对象
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        // 8. 存入 SecurityContext，后续 @PreAuthorize 等注解就能生效
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 9. 继续执行后续过滤器
        chain.doFilter(request, response);
    }
}
```



### 4. Spring Security 配置类

java

```
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 开启方法级权限控制，支持 @PreAuthorize 注解[citation:6]
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // 前后端分离，关闭 CSRF
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 禁用 Session
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll()  // 登录注册放行
                .anyRequest().authenticated()  // 其他请求都需要认证
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // 插入自定义过滤器
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 密码加密
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```



### 5. 登录接口

java

```
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Spring Security 进行认证
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // 2. 认证成功，获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        
        // 3. 生成 JWT
        String token = jwtUtils.generateToken(
            loginUser.getUsername(), 
            loginUser.getUserId(), 
            loginUser.getAuthorities()
        );
        
        // 4. 【重要】将 token 存入 Redis，实现有状态管理
        //    设置过期时间与 JWT 过期时间一致
        String redisKey = "login:token:" + loginUser.getUsername();
        redisTemplate.opsForValue().set(redisKey, token, jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);
        
        // 5. 返回 token 给前端
        return ResponseEntity.ok(new LoginResponse(token));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // 去除前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 1. 解析 token 获取用户名，删除 Redis 中的 token
        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.getSubject();
        redisTemplate.delete("login:token:" + username);
        
        // 2. 将 token 加入黑名单（可选，防止已删除的 token 被恶意使用）
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set("blacklist:" + token, "1", ttl, TimeUnit.MILLISECONDS);
        }
        
        return ResponseEntity.ok("退出成功");
    }
}
```



------

## 四、权限控制（方法级）

在需要权限控制的方法上添加 `@PreAuthorize` 注解：

java

```
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        // 只有 ADMIN 角色才能访问
        return userService.list();
    }
    
    @PreAuthorize("hasAuthority('user:add')")
    @PostMapping("/user")
    public void addUser(@RequestBody User user) {
        // 需要 user:add 权限
        userService.save(user);
    }
}
```



> 注意：需要在 SecurityConfig 上添加 `@EnableMethodSecurity` 注解才能启用 `@PreAuthorize`。

------

## 五、Redis 存储结构设计

| Key 格式                 | Value            | 用途                          | 过期时间            |
| :----------------------- | :--------------- | :---------------------------- | :------------------ |
| `login:token:{username}` | JWT token 字符串 | 实现单点登录和强制下线        | 与 JWT 过期时间一致 |
| `blacklist:{token}`      | "1"              | 退出登录后阻止 token 继续使用 | 剩余有效期          |

**单点登录（一处登录，另一处自动下线）的实现原理**：当用户在新设备登录时，会生成新 token 并覆盖 Redis 中 `login:token:{username}` 的值。旧设备发起请求时，过滤器会发现 Redis 中的 token 与请求携带的不一致，从而拒绝访问，返回“被踢下线”。

------

## 六、流程总结

text

```
1. 用户登录 → Spring Security 校验账号密码 → 生成 JWT → JWT 存入 Redis → 返回 JWT 给前端

2. 前端请求带上 JWT（Header: Authorization: Bearer xxx）

3. JwtAuthenticationFilter 拦截：
   ├── 校验 token 格式和签名
   ├── 校验 Redis 中是否有该用户的 token（防踢下线）
   ├── 校验 token 是否在黑名单（防退出后继续使用）
   └── 校验通过 → 将用户信息存入 SecurityContext

4. 进入 Controller 前，Spring Security 根据 @PreAuthorize 进行权限校验

5. 退出登录 → 删除 Redis 中的 token → 加入黑名单
```



------

## 七、常见问题与注意事项

| 问题                                       | 解决方案                                                     |
| :----------------------------------------- | :----------------------------------------------------------- |
| **JWT 密钥长度不够**                       | HS256 要求密钥长度 ≥ 32 字节，可用 `Keys.secretKeyFor(SignatureAlgorithm.HS256)` 生成 |
| **无法注入 AuthenticationManager**         | 在 SecurityConfig 中通过 `@Bean` 暴露 `AuthenticationManager` |
| **Spring Boot 3 / Spring Security 6 变化** | `WebSecurityConfigurerAdapter` 已废弃，使用 `SecurityFilterChain` Bean 方式配置 |
| **Token 自动续期**                         | 可在过滤器中检查剩余时间，低于阈值时签发新 token 并通过响应头返回 |

------

如需参考完整源码，可以查看一些开源的实现，如 [spring-security-demo](https://gitee.com/nidayeyo/spring-security-demo) 项目。这套方案的核心是理解 JWT 和 Redis 的职责划分——JWT 负责身份声明，Redis 负责状态控制，两者结合才能构建生产可用的认证体系。

本回答由 AI 生成，内容仅供参考，请仔细甄别。

如何实现手机号验证码登录

# Spring Security 实现手机号验证码登录

手机号验证码登录是当前主流的方式，核心思路是：**将手机号作为用户标识，验证码代替密码进行认证**。

------

## 一、核心设计思路

手机号登录与传统的用户名密码登录在 Spring Security 中实现方式不同：

| 对比项   | 密码登录                              | 手机验证码登录                     |
| :------- | :------------------------------------ | :--------------------------------- |
| 认证凭证 | username + password                   | mobile + code                      |
| 认证方式 | `UsernamePasswordAuthenticationToken` | 自定义 `MobileAuthenticationToken` |
| 验证逻辑 | `UserDetailsService` + 密码比对       | 校验 Redis 中的验证码              |
| 用户来源 | 必须是已注册用户                      | 可自动注册（首次登录创建账号）     |

**核心需要自定义三个组件：**

1. **`MobileAuthenticationToken`** - 承载手机号和验证码的认证凭证
2. **`MobileAuthenticationProvider`** - 验证手机号和验证码的核心逻辑
3. **`MobileAuthenticationFilter`** - 拦截 `/login/mobile` 请求并创建 Token

------

## 二、实现步骤

### 1. 发送验证码接口

首先需要一个发送验证码的接口，将验证码存储到 Redis：

java

```
@RestController
@RequestMapping("/auth")
public class SmsController {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private SmsService smsService;  // 短信发送服务
    
    @PostMapping("/sendCode")
    public ResponseEntity<?> sendCode(@RequestParam String mobile) {
        
        // 1. 校验手机号格式
        if (!isValidMobile(mobile)) {
            return ResponseEntity.badRequest().body("手机号格式不正确");
        }
        
        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // 3. 存储到 Redis，有效期5分钟
        String key = "sms:code:" + mobile;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        
        // 4. 调用短信服务发送（示例中打印日志）
        smsService.send(mobile, code);
        log.info("验证码发送成功: {} -> {}", mobile, code);  // 实际生产不要打印
        
        return ResponseEntity.ok("验证码已发送");
    }
}
```



------

### 2. 自定义 MobileAuthenticationToken

这个 Token 用于承载手机号和验证码信息，类似于 `UsernamePasswordAuthenticationToken`：

java

```
public class MobileAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;  // 手机号
    private Object credentials;       // 验证码
    
    /**
     * 未认证时调用（构造未认证的 token，包含手机号和验证码）
     */
    public MobileAuthenticationToken(String mobile, String code) {
        super(null);
        this.principal = mobile;
        this.credentials = code;
        setAuthenticated(false);
    }
    
    /**
     * 认证成功后调用（此时只包含用户信息，不包含敏感凭证）
     */
    public MobileAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return credentials;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
}
```



------

### 3. 自定义 MobileAuthenticationProvider

这是核心认证逻辑，负责校验验证码和加载用户信息：

java

```
@Component
public class MobileAuthenticationProvider implements AuthenticationProvider {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;  // 仅用于自动注册时加密密码
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        // 1. 获取手机号和验证码
        String mobile = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();
        
        // 2. 从 Redis 校验验证码
        String redisKey = "sms:code:" + mobile;
        String expectedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (StringUtils.isEmpty(expectedCode)) {
            throw new BadCredentialsException("验证码已过期，请重新获取");
        }
        
        if (!expectedCode.equals(code)) {
            throw new BadCredentialsException("验证码错误");
        }
        
        // 3. 验证成功，删除验证码（防止重复使用）
        redisTemplate.delete(redisKey);
        
        // 4. 加载或创建用户
        UserDetails userDetails = loadOrCreateUser(mobile);
        
        // 5. 创建认证成功后的 Token
        MobileAuthenticationToken result = new MobileAuthenticationToken(
            userDetails,
            null,  // 认证成功后 credentials 置为 null
            userDetails.getAuthorities()
        );
        
        result.setDetails(authentication.getDetails());
        return result;
    }
    
    /**
     * 根据手机号加载或创建用户
     */
    private UserDetails loadOrCreateUser(String mobile) {
        try {
            // 尝试加载已存在的用户
            return userDetailsService.loadUserByUsername(mobile);
        } catch (UsernameNotFoundException e) {
            // 用户不存在，自动注册
            return autoRegisterUser(mobile);
        }
    }
    
    /**
     * 自动注册新用户
     */
    private UserDetails autoRegisterUser(String mobile) {
        // 生成随机密码（用户不会用密码登录，但数据库需要）
        String randomPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(randomPassword);
        
        // 保存到数据库（根据你的 DAO 层调整）
        User user = new User();
        user.setMobile(mobile);
        user.setUsername("user_" + mobile);  // 默认用户名
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        // userMapper.insert(user);
        
        // 返回 UserDetails，赋予默认角色
        return new LoginUser(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return MobileAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```



------

### 4. 自定义 MobileAuthenticationFilter

这个 Filter 负责拦截手机号登录请求，将请求参数封装成 `MobileAuthenticationToken`：

java

```
public class MobileAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    // 默认处理的请求路径和 Method
    private String mobileParameter = "mobile";
    private String codeParameter = "code";
    private boolean postOnly = true;
    
    public MobileAuthenticationFilter() {
        super(new AntPathRequestMatcher("/auth/mobile/login", "POST"));
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        
        if (postOnly && !"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        
        // 获取手机号和验证码
        String mobile = obtainMobile(request);
        String code = obtainCode(request);
        
        if (mobile == null) {
            mobile = "";
        }
        if (code == null) {
            code = "";
        }
        
        mobile = mobile.trim();
        
        // 创建未认证的 Token
        MobileAuthenticationToken authRequest = new MobileAuthenticationToken(mobile, code);
        
        // 将请求信息设置到 Token 中
        setDetails(request, authRequest);
        
        // 交给 AuthenticationManager 进行认证
        return this.getAuthenticationManager().authenticate(authRequest);
    }
    
    private String obtainMobile(HttpServletRequest request) {
        return request.getParameter(mobileParameter);
    }
    
    private String obtainCode(HttpServletRequest request) {
        return request.getParameter(codeParameter);
    }
    
    private void setDetails(HttpServletRequest request, MobileAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
    
    public void setMobileParameter(String mobileParameter) {
        this.mobileParameter = mobileParameter;
    }
    
    public void setCodeParameter(String codeParameter) {
        this.codeParameter = codeParameter;
    }
}
```



------

### 5. 更新 Spring Security 配置

将自定义的 Filter 和 Provider 整合到 Security 配置中：

java

```
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private MobileAuthenticationProvider mobileAuthenticationProvider;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // 配置手机号登录的 Filter
        MobileAuthenticationFilter mobileAuthenticationFilter = new MobileAuthenticationFilter();
        mobileAuthenticationFilter.setAuthenticationManager(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)));
        
        // 设置登录成功和失败的处理器
        mobileAuthenticationFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            // 登录成功，生成 JWT 并返回
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = generateJwt(loginUser);
            // 存入 Redis（参考上一节的实现）
            ResponseUtil.write(response, token);
        });
        
        mobileAuthenticationFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            ResponseUtil.writeError(response, exception.getMessage());
        });
        
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/sendCode", "/auth/mobile/login").permitAll()
                .anyRequest().authenticated()
            )
            // 添加自定义 Provider 和 Filter
            .authenticationProvider(mobileAuthenticationProvider)
            .addFilterBefore(mobileAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, MobileAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```



------

### 6. 优化 UserDetailsService 支持手机号查询

原有的 `UserDetailsService` 需要修改，支持通过手机号查询用户：

java

```
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 兼容：支持用户名或手机号登录
        User user = userMapper.findByUsernameOrMobile(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildLoginUser(user);
    }
    
    // 专门用于手机号登录的查询
    public UserDetails loadUserByMobile(String mobile) {
        User user = userMapper.findByMobile(mobile);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildLoginUser(user);
    }
    
    private LoginUser buildLoginUser(User user) {
        // 查询用户权限...
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }
}
```



------

## 三、登录流程总结

text

```
1. 用户输入手机号 → 点击"获取验证码" → POST /auth/sendCode
   ├── 生成6位随机验证码
   └── 存入 Redis（key: sms:code:{mobile}，过期5分钟）

2. 用户输入验证码 → 点击"登录" → POST /auth/mobile/login
   ├── MobileAuthenticationFilter 拦截请求
   ├── 提取 mobile 和 code，创建未认证的 MobileAuthenticationToken
   ├── 交给 AuthenticationManager
   ├── MobileAuthenticationProvider 执行认证：
   │   ├── 校验 Redis 中的验证码
   │   ├── 验证成功 → 删除验证码
   │   ├── 调用 UserDetailsService 加载或创建用户
   │   └── 返回认证成功的 Token
   └── 认证成功 → 生成 JWT → 存入 Redis → 返回给前端

3. 后续请求携带 JWT → 走上一节实现的 JwtAuthenticationFilter
```



------

## 四、扩展功能

### 1. 限制验证码发送频率

java

```
@PostMapping("/sendCode")
public ResponseEntity<?> sendCode(@RequestParam String mobile) {
    
    // 防止频繁发送：同一手机号60秒内只能发送一次
    String rateLimitKey = "sms:rate:" + mobile;
    if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
        return ResponseEntity.status(429).body("发送太频繁，请稍后再试");
    }
    
    // 发送验证码...
    
    // 设置发送频率限制（60秒）
    redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
    
    return ResponseEntity.ok("验证码已发送");
}
```



### 2. 图形验证码（防止短信轰炸）

在发送验证码前增加图形验证码校验：

java

```
@PostMapping("/sendCode")
public ResponseEntity<?> sendCode(@RequestParam String mobile, 
                                   @RequestParam String captchaKey,
                                   @RequestParam String captchaCode) {
    
    // 校验图形验证码
    String expected = redisTemplate.opsForValue().get("captcha:" + captchaKey);
    if (!captchaCode.equalsIgnoreCase(expected)) {
        return ResponseEntity.badRequest().body("图形验证码错误");
    }
    // 继续发送短信验证码...
}
```



### 3. 手机号+密码双重登录

可以在同一个登录接口中支持两种方式：

java

```
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 如果提供了密码，走密码登录
    if (StringUtils.hasText(request.getPassword())) {
        // 用户名密码登录
        UsernamePasswordAuthenticationToken token = 
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication auth = authenticationManager.authenticate(token);
        // ...
    } 
    // 否则走验证码登录
    else if (StringUtils.hasText(request.getCode())) {
        MobileAuthenticationToken token = 
            new MobileAuthenticationToken(request.getMobile(), request.getCode());
        Authentication auth = authenticationManager.authenticate(token);
        // ...
    }
}
```



------

## 五、注意事项

| 问题                         | 解决方案                                                     |
| :--------------------------- | :----------------------------------------------------------- |
| **验证码明文存储风险**       | Redis 存储即可，验证码本身短时效，不需要加密                 |
| **手机号未注册时的行为**     | 常见做法是自动注册，也可设计为只允许已注册用户               |
| **测试环境打印验证码**       | 生产环境务必移除，正式接入短信网关                           |
| **验证码暴力破解**           | 限制同一手机号尝试次数（如5次错误后锁定15分钟）              |
| **Spring Security 版本兼容** | Spring Boot 3+ 需要检查 `AbstractAuthenticationProcessingFilter` 的 API 变化 |

核心就是 **自定义一个认证 Provider + Filter**，理解了这个模式，你还可以扩展出扫码登录、邮箱登录等任意认证方式。

本回答由 AI 生成，内容仅供参考，请仔细甄别。

jwt token 访问其他服务， 其他服务应该做什么配置

# JWT Token 跨服务访问：其他服务的配置方案

当用户在一个服务（认证服务）登录获取 JWT 后，拿着这个 token 访问其他微服务时，**其他服务需要能够验证该 token 的合法性**，而不需要每次都去认证服务查询。

------

## 一、两种主流方案对比

| 方案                | 原理                                              | 优点                             | 缺点                                   |
| :------------------ | :------------------------------------------------ | :------------------------------- | :------------------------------------- |
| **无状态 JWT 验证** | 各服务使用相同的 JWT 密钥，本地验证签名           | 无需网络调用，性能高，完全无状态 | token 无法主动失效（除非结合 Redis）   |
| **网关统一验证**    | 所有请求先经过网关，网关验证 token 后转发用户信息 | 业务服务零配置，集中管理         | 网关成为瓶颈，内部服务间调用也需要处理 |

实际生产中常采用**混合方案**：网关做统一验证 + 业务服务本地验证（或透传用户信息）。

------

## 二、方案一：无状态 JWT 验证（各服务独立验证）

这是最简单的方案，**每个需要验证用户身份的服务都配置相同的 JWT 密钥**。

### 1. 各服务需要添加的依赖

xml

```
<!-- 每个业务服务都需要引入 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<!-- Spring Security（如果需要权限控制） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```



### 2. 各服务配置相同的 JWT 密钥

yaml

```
# application.yml（每个服务都要配置相同的值）
jwt:
  secret: ${JWT_SECRET:mySecretKeyForAllServicesAtLeast32BytesLong}
  header: Authorization
  token-prefix: Bearer 
```



> **⚠️ 关键**：所有服务必须使用**完全相同的 secret 密钥**，否则无法验证 token 签名。

### 3. 各服务配置 JWT 验证过滤器（简化版）

业务服务不需要用户登录逻辑，只需要验证 token 并提取用户信息：

java

```
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && validateToken(token)) {
            Claims claims = parseToken(token);
            
            // 从 token 中提取用户信息，构建 Authentication
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String authoritiesStr = claims.get("authorities", String.class);
            
            List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesStr.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
            
            // 创建用户详情对象
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "", authorities);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            
            // 可以将 userId 也存入上下文（通过自定义方式）
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        chain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
```



### 4. 各服务 Spring Security 配置（可选）

如果业务服务需要权限控制，配置 Security：

java

```
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                // 健康检查等端点放行
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```



### 5. 业务服务中获取当前用户信息

java

```
@RestController
public class OrderController {
    
    @GetMapping("/orders")
    public List<Order> getMyOrders() {
        // 方法1：从 SecurityContext 获取
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // 方法2：通过 @AuthenticationPrincipal 注解
        // 需要在 Controller 参数中声明
        return orderService.findByUsername(username);
    }
    
    @GetMapping("/user/info")
    public UserInfo getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return UserInfo.builder()
            .username(userDetails.getUsername())
            .authorities(userDetails.getAuthorities())
            .build();
    }
}
```



------

## 三、方案二：网关统一验证（推荐微服务架构）

在网关层（Spring Cloud Gateway）统一验证 JWT，业务服务完全不感知 token。

### 架构图

text

```
客户端 → Gateway（验证JWT）→ 转发请求（Header添加用户信息）→ 业务服务
```



### 1. 网关配置 JWT 验证过滤器

java

```
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 1. 从请求头获取 token
        String token = extractToken(request);
        
        if (token == null) {
            // 不需要认证的路径放行（如登录接口）
            if (isOpenPath(request.getPath().toString())) {
                return chain.filter(exchange);
            }
            return unauthorized(exchange, "缺少认证信息");
        }
        
        // 2. 验证 token
        Claims claims;
        try {
            claims = validateToken(token);
        } catch (Exception e) {
            return unauthorized(exchange, "Token 无效或已过期");
        }
        
        // 3. 从 token 中提取用户信息，添加到请求头中转发给下游服务
        String userId = claims.get("userId", String.class);
        String username = claims.getSubject();
        String authorities = claims.get("authorities", String.class);
        
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", userId)
            .header("X-Username", username)
            .header("X-User-Authorities", authorities)
            .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
    
    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"code\":401,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
    
    @Override
    public int getOrder() {
        return -100;  // 优先级最高
    }
}
```



### 2. 业务服务配置：接收 Header 中的用户信息

业务服务完全不需要 JWT 相关依赖，只需配置一个过滤器解析 Header：

java

```
@Component
public class UserContextFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String authorities = request.getHeader("X-User-Authorities");
        
        if (StringUtils.hasText(userId)) {
            // 构建用户认证信息
            List<SimpleGrantedAuthority> authorityList = 
                StringUtils.hasText(authorities) 
                    ? Arrays.stream(authorities.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    : Collections.emptyList();
            
            UserDetails userDetails = User.withUsername(username)
                .password("")
                .authorities(authorityList)
                .build();
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, authorityList);
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        chain.doFilter(request, response);
    }
}
```



### 3. 业务服务中获取用户信息

java

```
@RestController
public class BusinessController {
    
    @GetMapping("/data")
    public ResponseEntity<?> getData() {
        // 直接从 SecurityContext 获取
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // 或者从请求头获取（如果不想用 SecurityContext）
        // HttpServletRequest request = ...; String userId = request.getHeader("X-User-Id");
        
        return ResponseEntity.ok("User: " + username);
    }
    
    // 创建工具类方便获取
    public static class SecurityUtils {
        public static String getCurrentUserId() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() != null) {
                return auth.getDetails().toString();
            }
            return null;
        }
        
        public static String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        }
    }
}
```



------

## 四、方案三：结合 Redis 的 Token 验证（支持主动失效）

如果需要在 token 有效期内实现主动失效（踢人下线、退出登录），各服务需要**同时验证 JWT 签名和 Redis 中的 token 状态**。

### 各业务服务的过滤器需要增加 Redis 校验

java

```
@Component
public class JwtWithRedisFilter extends OncePerRequestFilter {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String token = extractToken(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        
        // 1. 验证 JWT 签名和有效期
        if (!jwtUtils.validateToken(token)) {
            sendError(response, "Token无效");
            return;
        }
        
        // 2. 验证 Redis 黑名单
        String blackKey = "blacklist:" + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blackKey))) {
            sendError(response, "Token已失效");
            return;
        }
        
        // 3. 验证 Redis 中的有效 token（可选）
        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.getSubject();
        String redisTokenKey = "login:token:" + username;
        String storedToken = redisTemplate.opsForValue().get(redisTokenKey);
        
        if (storedToken == null || !storedToken.equals(token)) {
            sendError(response, "Token已被踢下线");
            return;
        }
        
        // 4. 通过验证，构建 Authentication
        // ... 同前面的过滤逻辑
        
        chain.doFilter(request, response);
    }
}
```



------

## 五、方案选择建议

| 场景                       | 推荐方案              | 原因                     |
| :------------------------- | :-------------------- | :----------------------- |
| 2-3 个微服务，用户量不大   | 方案一（无状态 JWT）  | 实现简单，无需额外中间件 |
| 微服务数量多，需要统一管理 | 方案二（网关验证）    | 业务服务零配置，易于维护 |
| 需要强制踢人、单点登录     | 方案三（Redis 辅助）  | 支持 token 主动失效      |
| 高并发、对性能要求极高     | 方案一 + 黑名单 Cache | 避免每次请求查 Redis     |

------

## 六、内部服务之间的调用

服务 A 需要调用服务 B 时，需要将当前用户的 token 传递过去：

java

```
@Service
public class OrderServiceClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<Product> getProductsByOrder(String orderId) {
        // 获取当前请求的 token（从 RequestContextHolder）
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List<Product>> response = restTemplate.exchange(
            "http://product-service/products?orderId=" + orderId,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<Product>>() {}
        );
        
        return response.getBody();
    }
}
```



> 也可以使用 Feign 客户端的拦截器自动添加 token。

------

## 七、安全配置清单（其他服务）

| 配置项                     | 是否必需 | 说明                        |
| :------------------------- | :------- | :-------------------------- |
| JWT 密钥（与认证服务相同） | ✅ 必需   | 方案一、三必需              |
| JWT 验证过滤器             | ✅ 必需   | 所有接收 token 的服务都需要 |
| Spring Security 依赖       | 按需     | 需要权限控制时添加          |
| Redis 连接配置             | 可选     | 方案三需要                  |
| 网关用户信息传递           | 可选     | 方案二需要                  |

**核心原则**：所有需要验证用户身份的服务，要么能验证 JWT 签名，要么能从上游服务获取可信的用户信息。

本回答由 AI 生成，内容仅供参考，请仔细甄别。

如果是通过feign调用 不通过网关怎么办

# Feign 调用时的 JWT Token 传递方案（无网关场景）

当服务之间通过 Feign 直接调用（不经过网关）时，需要**手动将当前请求的 Token 传递给下游服务**。核心思路是使用 Feign 的请求拦截器，自动从当前请求上下文中提取 Token 并添加到 Feign 请求头中。

------

## 一、整体架构

text

```
用户请求 → 服务A（携带Token）→ Feign调用服务B → 服务B验证Token → 返回结果
                    ↓
            Feign拦截器自动传递Token
```



**关键点**：服务A需要将当前请求的Token原样传递给服务B，服务B独立验证Token。

------

## 二、Feign 客户端配置

### 1. 创建 Feign 请求拦截器（核心）

java

```
@Configuration
public class FeignConfig {
    
    /**
     * 请求拦截器：自动从当前请求上下文中获取 Token，添加到 Feign 请求头
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取当前 HTTP 请求（通过 RequestContextHolder）
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String token = request.getHeader("Authorization");
                    
                    if (StringUtils.hasText(token)) {
                        // 将 Token 传递给下游服务
                        template.header("Authorization", token);
                    }
                }
            }
        };
    }
    
    /**
     * Feign 日志配置（可选，方便调试）
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```



### 2. Feign 客户端接口定义

java

```
@FeignClient(
    name = "user-service", 
    url = "${user.service.url:http://localhost:8081}",
    configuration = FeignConfig.class  // 使用上面的配置
)
public interface UserServiceClient {
    
    @GetMapping("/api/users/info")
    UserInfo getUserInfo();
    
    @GetMapping("/api/users/{userId}")
    User getUserById(@PathVariable("userId") Long userId);
    
    @PostMapping("/api/users/update")
    Result updateUser(@RequestBody UserUpdateRequest request);
}
```



### 3. 在服务A中调用 Feign

java

```
@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    public OrderDetail getOrderDetail(Long orderId) {
        // 此时 Feign 拦截器会自动将当前请求的 Token 添加到调用 UserService 的请求中
        UserInfo userInfo = userServiceClient.getUserInfo();
        
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setUserInfo(userInfo);
        return detail;
    }
}
```



------

## 三、异步调用场景（重要）

**问题**：`RequestContextHolder` 依赖于当前线程的 `ServletRequestAttributes`。当使用 `@Async` 异步调用或线程池时，子线程无法获取父线程的请求上下文。

### 解决方案1：手动传递 Token

java

```
@Service
public class OrderService {
    
    @Async
    public CompletableFuture<UserInfo> getUserInfoAsync(String token) {
        // 手动传递 token 参数
        return CompletableFuture.completedFuture(
            userServiceClient.getUserInfoWithToken(token)
        );
    }
    
    // 调用处
    public void processOrder() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = attributes.getRequest().getHeader("Authorization");
        
        getUserInfoAsync(token).thenAccept(userInfo -> {
            // 处理异步结果
        });
    }
}
```



### 解决方案2：配置 RequestContextHolder 传递

java

```
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        
        // 关键：配置任务装饰器，传递请求上下文
        executor.setTaskDecorator(runnable -> {
            // 保存当前线程的请求上下文
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(context);
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        });
        
        executor.initialize();
        return executor;
    }
}
```



------

## 四、服务B（被调用方）的配置

服务B不需要知道是谁调用的，只需要正常验证 Token 即可：

java

```
// 服务B中的配置（与之前章节相同）
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

// 服务B中的 Controller
@RestController
public class UserController {
    
    @GetMapping("/api/users/info")
    public UserInfo getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // 从 Token 中获取的用户信息
        return UserInfo.builder()
            .username(userDetails.getUsername())
            .authorities(userDetails.getAuthorities())
            .build();
    }
}
```



------

## 五、高级场景：Token 增强与转换

### 1. 服务间调用时传递额外信息

有时候需要在服务间调用时传递用户ID等额外信息（而不只是原样传递 Token）：

java

```
@Configuration
public class FeignConfig {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 1. 传递原始 Token
                String token = request.getHeader("Authorization");
                if (token != null) {
                    template.header("Authorization", token);
                }
                
                // 2. 从 Token 中解析用户信息，添加到请求头（避免下游服务重复解析）
                String userId = request.getHeader("X-User-Id");
                if (userId == null && token != null) {
                    // 解析 Token 获取用户ID
                    userId = parseUserIdFromToken(token);
                    template.header("X-User-Id", userId);
                }
                
                // 3. 传递调用链追踪 ID
                String traceId = request.getHeader("X-Trace-Id");
                if (traceId == null) {
                    traceId = UUID.randomUUID().toString();
                }
                template.header("X-Trace-Id", traceId);
            }
        };
    }
    
    private String parseUserIdFromToken(String token) {
        // JWT 解析逻辑
        // ...
        return userId;
    }
}
```



### 2. 服务B接收增强后的 Header

java

```
@RestController
public class UserController {
    
    @GetMapping("/api/users/info")
    public UserInfo getUserInfo(@RequestHeader("X-User-Id") String userId,
                                @RequestHeader("X-Trace-Id") String traceId) {
        // 直接使用传递过来的用户ID，无需重新解析 Token
        log.info("TraceId: {}, 处理用户: {}", traceId, userId);
        return userService.getUserInfo(Long.parseLong(userId));
    }
}
```



------

## 六、Feign 调用时的认证失败处理

### 1. 配置 Feign 错误解码器

java

```
@Configuration
public class FeignErrorConfig {
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default() {
            @Override
            public Exception decode(String methodKey, Response response) {
                if (response.status() == 401) {
                    return new UnauthorizedException("下游服务认证失败，Token可能已过期");
                }
                if (response.status() == 403) {
                    return new ForbiddenException("下游服务权限不足");
                }
                return super.decode(methodKey, response);
            }
        };
    }
}
```



### 2. Token 自动刷新机制

java

```
@Configuration
public class FeignTokenInterceptor implements RequestInterceptor {
    
    @Autowired
    private TokenRefreshService tokenRefreshService;
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            
            // 检查 Token 是否即将过期（剩余时间 < 5分钟）
            if (isTokenExpiringSoon(token)) {
                // 刷新 Token（调用认证服务）
                String newToken = tokenRefreshService.refreshToken(token);
                if (newToken != null) {
                    token = newToken;
                    // 可选：更新当前请求上下文中的 Token
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest().setAttribute("new_token", newToken);
                }
            }
            
            template.header("Authorization", token);
        }
    }
}
```



------

## 七、两种方案对比

| 场景               | 方案                   | 优点                     | 缺点                   |
| :----------------- | :--------------------- | :----------------------- | :--------------------- |
| **有网关**         | 网关统一添加 Header    | 业务服务零配置，管理集中 | 增加一跳网络开销       |
| **无网关 + Feign** | Feign 拦截器传递 Token | 减少网络跳数，性能更好   | 每个服务都要配置拦截器 |

------

## 八、最佳实践总结

### 推荐配置清单

yaml

```
# 1. 所有服务都需要的依赖
dependencies:
  - spring-cloud-starter-openfeign
  - spring-boot-starter-security
  - jjwt (JWT库)

# 2. 每个服务都需要创建的配置类
- FeignConfig (请求拦截器)
- JwtAuthenticationFilter (Token验证过滤器)
- SecurityConfig (安全配置)

# 3. 配置文件
jwt:
  secret: ${JWT_SECRET}  # 所有服务使用相同密钥
```



### 完整的 Feign 客户端配置示例

java

```
@Configuration
@EnableFeignClients(basePackages = "com.example.client")
public class FeignAutoConfig {
    
    @Bean
    public RequestInterceptor feignTokenInterceptor() {
        return template -> {
            // 从当前请求获取 Token
            String token = getCurrentToken();
            if (token != null) {
                template.header("Authorization", token);
            }
            
            // 添加调用链追踪ID
            template.header("X-Trace-Id", getOrCreateTraceId());
        };
    }
    
    private String getCurrentToken() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }
}
```



### 关键注意事项

| 问题                           | 解决方案                                          |
| :----------------------------- | :------------------------------------------------ |
| **异步调用丢失 Token**         | 使用 `RequestContextHolder` 传递或手动传参        |
| **Token 过期导致链式调用失败** | 实现自动刷新机制，或使用 Refresh Token            |
| **服务B需要知道原始用户**      | 从 Token 中解析，或通过 Header 传递用户ID         |
| **循环依赖（A调B，B调A）**     | 使用异步或消息队列解耦，避免请求死循环            |
| **性能考虑**                   | 下游服务使用本地 JWT 验证，不要每次调用都查 Redis |

核心原则：**Feign 拦截器是实现 Token 传递的最优雅方式**，只需配置一次，所有 Feign 调用都会自动带上用户凭证，下游服务独立验证即可。