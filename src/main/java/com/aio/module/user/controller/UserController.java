package com.aio.module.user.controller;

import com.aio.api.user.UserApi;
import com.aio.api.user.model.ModelApiResponse;
import com.aio.api.user.model.LoginResponse;
import com.aio.api.user.model.UpdatePasswordRequest;
import com.aio.api.user.model.UserLoginRequest;
import com.aio.api.user.model.UserRegisterRequest;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.service.UserService;
import com.aio.common.util.JwtUtils; // 自定义JWT工具类
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    private final JwtUtils jwtUtils; // JWT工具类（生成/解析令牌）

    @Override
    public ResponseEntity<ModelApiResponse> register(UserRegisterRequest request) {
        UserEntity user = new UserEntity();
        // 复制请求参数到实体
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() != null ? request.getGender().toString() : null);
        user.setBirthday(request.getBirthday());
        user.setOccupation(request.getOccupation());
        user.setSignature(request.getSignature());
        // 调用注册服务（密码明文传入，服务层加密）
        userService.register(user, request.getPassword());

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMessage("注册成功");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LoginResponse> login(UserLoginRequest request) {
        UserEntity user = userService.login(request.getAccount(), request.getPassword());
        // 生成JWT令牌（包含userId和role）
        String token = jwtUtils.generateToken(user.getUserId().toString(), user.getRole());
        // 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setUserId(user.getUserId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setRole(user.getRole());
        return ResponseEntity.ok(loginResponse);
    }

    @Override
    public ResponseEntity<ModelApiResponse> updatePassword(UpdatePasswordRequest request) {
        // TODO: 从JWT认证信息中获取当前用户ID，暂时使用硬编码
        String currentUserId = "123e4567-e89b-12d3-a456-426614174000"; // 假设JWT的subject为userId
        userService.updatePassword(UUID.fromString(currentUserId), request.getOldPassword(), request.getNewPassword());

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMessage("密码修改成功");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        // TODO: 从认证信息中获取当前用户ID和角色，暂时使用硬编码
        String currentUserId = "123e4567-e89b-12d3-a456-426614174000";
        String currentRole = "admin"; // 假设权限格式为"ROLE_{role}"
        // 调用删除服务
        userService.deleteUser(userId, UUID.fromString(currentUserId), currentRole);
        return ResponseEntity.noContent().build();
    }
}