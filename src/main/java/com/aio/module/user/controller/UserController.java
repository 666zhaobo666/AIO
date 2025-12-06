package com.aio.module.user.controller;

import com.aio.api.UserApi;
import com.aio.api.model.ModelApiResponse;
import com.aio.api.model.UserLoginDataResponse;
import com.aio.api.model.UpdatePasswordRequest;
import com.aio.api.model.UpdateUserInfoRequest;
import com.aio.api.model.UserLoginRequest;
import com.aio.api.model.UserRegisterRequest;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.common.security.SecurityContextUtils;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.service.UserService;
import com.aio.common.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    private final JwtUtils jwtUtils; // JWT工具类（生成/解析令牌）

    @Override
    public ResponseEntity<ModelApiResponse> register(UserRegisterRequest request) {
        UserEntity user = getUserEntity(request);
        // 调用注册服务（密码明文传入，服务层加密）
        UserEntity userinfo = userService.register(user, request.getPassword());

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(201);
        response.setMsg("用户注册成功");
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        response.setData(JsonNullable.of(userinfo));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private static UserEntity getUserEntity(UserRegisterRequest request) {
        UserEntity user = new UserEntity();
        // 复制请求参数到实体
        user.setUserName(request.getUserName());
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() != null ? request.getGender() : null);
        user.setBirthday(request.getBirthday());
        user.setOccupation(request.getOccupation());
        user.setSignature(request.getSignature());
        user.setRole(UserRoleEnum.USER.getValue());
        return user;
    }

    @Override
    public ResponseEntity<ModelApiResponse> login(UserLoginRequest request) {
        UserEntity user = userService.login(request.getAccount(), request.getPassword());
        // 生成JWT令牌（包含userId和role）
        String token = jwtUtils.generateToken(user.getUserId(), user.getRole());
        // 构建登录响应
        UserLoginDataResponse loginResponse = new UserLoginDataResponse();
        loginResponse.setToken(token);
        loginResponse.setUserId(user.getUserId());
        loginResponse.setUserName(user.getUserName());
        loginResponse.setRole(user.getRole());
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("登录成功");
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        response.setData(JsonNullable.of(loginResponse));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> updatePassword(UpdatePasswordRequest request) {
        // 从JWT认证上下文中获取当前用户ID
        Integer currentUserId = SecurityContextUtils.getCurrentUserId();
        if (currentUserId == null) {
            ModelApiResponse response = new ModelApiResponse();
            response.setCode(401);
            response.setMsg("未认证或认证已过期");

            return ResponseEntity.status(401).body(response);
        }
        userService.updatePassword(currentUserId, request.getOldPassword(), request.getNewPassword());

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("密码修改成功");
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> deleteUser(Integer userId) {
        // 从JWT认证上下文中获取当前用户ID和角色
        Integer currentUserId = SecurityContextUtils.getCurrentUserId();
        String currentRole = SecurityContextUtils.getCurrentUserRole();
        if (currentUserId == null || currentRole == null) {
            return ResponseEntity.status(401).build();
        }

        // 调用删除服务
        userService.deleteUser(userId, currentUserId, currentRole);
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("用户删除成功");
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> updateUserInfo(Integer userId, UpdateUserInfoRequest request) {
        // 从JWT认证上下文中获取当前用户ID和角色
        Integer currentUserId = SecurityContextUtils.getCurrentUserId();
        String currentRole = SecurityContextUtils.getCurrentUserRole();
        if (currentUserId == null || currentRole == null) {
            return ResponseEntity.status(401).build();
        }

        // 调用修改用户信息服务
        UserEntity updatedUser = userService.updateUserInfo(
                userId,
                currentUserId,
                currentRole,
                request
        );

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("用户信息修改成功");
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        response.setData(JsonNullable.of(updatedUser));
        return ResponseEntity.ok(response);
    }
}
