# JWT认证集成说明

## 概述

本项目已成功集成JWT（JSON Web Token）认证系统，移除了所有硬编码的用户身份信息。

## 已完成的工作

### 1. 添加JWT依赖
在 `pom.xml` 中添加了 `jjwt` 库（v0.12.3）:
- `jjwt-api` - JWT API
- `jjwt-impl` - JWT实现
- `jjwt-jackson` - Jackson JSON处理

### 2. 核心组件

#### JwtUtils (com.aio.common.util.JwtUtils)
JWT工具类，提供：
- `generateToken(String userId, String role)` - 生成JWT令牌
- `getUserIdFromToken(String token)` - 从令牌中获取用户ID
- `getRoleFromToken(String token)` - 从令牌中获取用户角色
- `validateToken(String token)` - 验证令牌有效性

#### JwtAuthenticationFilter (com.aio.common.security.JwtAuthenticationFilter)
JWT认证过滤器，自动从请求头中提取和验证JWT令牌：
- 从 `Authorization` 头提取 `Bearer {token}`
- 验证令牌有效性
- 将认证信息设置到Spring Security上下文

#### SecurityContextUtils (com.aio.common.security.SecurityContextUtils)
安全上下文工具类，提供便捷方法：
- `getCurrentUserId()` - 获取当前认证用户ID
- `getCurrentUserRole()` - 获取当前用户角色
- `isAuthenticated()` - 检查是否已认证
- `hasRole(String role)` - 检查是否具有指定角色

#### SecurityConfig (com.aio.common.config.SecurityConfig)
Spring Security配置：
- 配置JWT过滤器
- 设置无状态会话管理
- 配置公开和受保护的接口路径

#### JwtProperties (com.aio.common.config.JwtProperties)
JWT配置属性类，从 `application.yml` 读取配置

### 3. 配置参数

在 `application.yml` 中配置：
```yaml
aio:
  jwt:
    secret: aio-platform-jwt-secret-key-1234567890  # JWT密钥（生产环境务必更换）
    expire-days: 7  # 令牌过期天数
```

### 4. API接口保护

#### 公开接口（无需认证）
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `/swagger-ui/**` - Swagger文档
- `/v3/api-docs/**` - OpenAPI文档

#### 受保护接口（需要JWT认证）
- `PUT /api/user/password` - 修改密码
- `DELETE /api/user/{userId}` - 删除用户
- 其他所有接口

## 使用方法

### 1. 用户注册
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

### 2. 用户登录
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "testuser",
    "password": "password123"
  }'
```

响应示例：
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "testuser",
  "role": "user"
}
```

### 3. 使用JWT令牌访问受保护接口
在请求头中添加 `Authorization: Bearer {token}`：

```bash
curl -X PUT http://localhost:8080/api/user/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "oldPassword": "password123",
    "newPassword": "newpassword456"
  }'
```

### 4. 在代码中获取当前用户信息

```java
// 获取当前用户ID
UUID currentUserId = SecurityContextUtils.getCurrentUserId();

// 获取当前用户角色
String currentRole = SecurityContextUtils.getCurrentUserRole();

// 检查是否已认证
boolean isAuth = SecurityContextUtils.isAuthenticated();

// 检查是否具有admin角色
boolean isAdmin = SecurityContextUtils.hasRole("admin");
```

## 安全建议

1. **生产环境密钥**: 务必在生产环境中更换JWT密钥为强随机字符串（至少32字符）
2. **HTTPS**: 在生产环境中使用HTTPS协议传输JWT令牌
3. **令牌过期时间**: 根据业务需求调整令牌过期时间
4. **刷新令牌**: 考虑实现刷新令牌机制以提升用户体验
5. **令牌黑名单**: 如需注销功能，考虑实现令牌黑名单机制

## 故障排查

### 401 Unauthorized
- 检查请求头是否包含正确的 `Authorization: Bearer {token}` 
- 检查令牌是否过期
- 检查JWT密钥配置是否正确

### 403 Forbidden
- 检查当前用户是否具有所需的角色权限

### 编译错误
如果遇到 `cannot find symbol` 错误：
```bash
mvn clean compile
```

## 测试

编译并运行项目：
```bash
mvn clean package -DskipTests
java -jar target/AIO-0.0.1-SNAPSHOT.jar
```

或使用Maven运行：
```bash
mvn spring-boot:run
```

访问Swagger UI测试API：
```
http://localhost:8080/swagger-ui/index.html
```

