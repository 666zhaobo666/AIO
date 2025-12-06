package com.aio.module.user.service;

import com.aio.api.model.ModelApiResponse;
import com.aio.api.model.UpdateUserInfoRequest;
import com.aio.common.exception.GlobalException;
import com.aio.common.enums.ExceptionEnum;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.module.user.entity.UserPasswordEntity;
import com.aio.module.user.repository.UserRepository;
import com.aio.module.user.repository.UserPasswordRepository;
import com.aio.module.user.utils.UserValidationUtils;

import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final PasswordEncoder passwordEncoder; // BCrypt加密�?
    private final UserValidationUtils userValidationUtils; // 校验工具�?

    // 注册
    @Transactional
    public UserEntity register(UserEntity user, String rawPassword) {
        // 校验用户信息
        validateUser(user, rawPassword);

        // save User
        userRepository.save(user);

        // save password
        userPasswordRepository.save(new UserPasswordEntity(user.getUserId(), passwordEncoder.encode(rawPassword)));

        return user;
    }

    // 用户信息校验
    public void validateUserUpdate(UserEntity user) {
        // 格式
        userValidationUtils.validateUserName(user.getUserName());
        userValidationUtils.validateEmail(user.getEmail());
        userValidationUtils.validatePhone(user.getPhone());
        userValidationUtils.validateBirthday(user.getBirthday());
        userValidationUtils.validateGender(user.getGender());
        userValidationUtils.validateRole(user.getRole());

        // 唯一�?
        userValidationUtils.validateUserAccountUniquenessForUpdate(user);
    }
    public void validateUser(UserEntity user, String rawPassword) {
        // 格式
        userValidationUtils.validateUserName(user.getUserName());
        userValidationUtils.validatePassword(rawPassword);
        userValidationUtils.validateEmail(user.getEmail());
        userValidationUtils.validatePhone(user.getPhone());
        userValidationUtils.validateBirthday(user.getBirthday());
        userValidationUtils.validateGender(user.getGender());
        userValidationUtils.validateRole(user.getRole());

        // 唯一�?
        userValidationUtils.validateUserAccountUniqueness(user);
    }

    // 登录
    public UserEntity login(String account, String rawPassword) {
        UserEntity user = findUserByAccount(account);
        // 用户不存�?
        if(user == null) {
            throw new GlobalException(ExceptionEnum.USER_NOT_EXIST);
        }
        // 验证密码
        if (!passwordEncoder.matches(rawPassword, userPasswordRepository.findPasswordByUserId(user.getUserId()))) {
            throw new GlobalException(ExceptionEnum.USER_PASSWORD_ERROR);
        }
        // 更新最后登录时�?
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    // 修改密码
    @Transactional
    public void updatePassword(Integer userId, String oldPassword, String newPassword) {

        // 验证旧密�?
        UserPasswordEntity userPassword = userPasswordRepository.findByUserId(userId)
                .orElseThrow(() -> new GlobalException(ExceptionEnum.USER_PASSWORD_NOT_EXIST));

        if (!passwordEncoder.matches(oldPassword, userPassword.getPassword())) {
            throw new GlobalException(ExceptionEnum.USER_OLD_PASSWORD_ERROR);
        }

        userPassword.setPassword(passwordEncoder.encode(newPassword));
        userPassword.setUpdatedTime(LocalDateTime.now());
        userPasswordRepository.save(userPassword);
    }


    // 删除用户（权限控制：管理员可删除所有用户，普通用户仅可删除自己）
    @Transactional
    public void deleteUser(Integer targetUserId, Integer currentUserId, String currentRole) {
        // 检查当前用户是否为管理�?
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(currentRole);
        // 非管理员只能删除自己
        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new GlobalException(ExceptionEnum.USER_NOT_ADMIN);
        }
        // 执行删除
        UserEntity user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GlobalException(ExceptionEnum.USER_NOT_EXIST));
        userRepository.delete(user);
    }

    // 修改用户信息（权限控制：管理员可修改所有用户，普通用户仅可修改自己）
    @Transactional
    public UserEntity updateUserInfo(Integer targetUserId, Integer currentUserId, String currentRole, UpdateUserInfoRequest request) {
        // 检查当前用户是否为管理�?
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(currentRole);
        // 非管理员只能修改自己
        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new GlobalException(ExceptionEnum.USER_NOT_ADMIN);
        }

        // 查找目标用户
        UserEntity user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GlobalException(ExceptionEnum.USER_NOT_EXIST));

        // 更新字段（仅更新非空值）
        if (request.getUsername() != null) {
            user.setUserName(request.getUsername());
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getOccupation() != null) {
            user.setOccupation(request.getOccupation());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }

        // 校验用户信息
        validateUserUpdate(user);

        return userRepository.save(user);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户唯一标识�?
     * @return ModelApiResponse 包含用户信息的响应对�?
     * @throws GlobalException 当用户不存在时抛�?USER_NOT_EXIST 异常
     */
    public ModelApiResponse getUserInfo(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ExceptionEnum.USER_NOT_EXIST));
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("查询成功");
        response.setTimeStamp(System.currentTimeMillis());
        response.setData(JsonNullable.of(user));
        return response;
    }

    // 辅助方法：通过账号（用户名/邮箱/手机号）查询用户（仅查询正常状态用户）
    private UserEntity findUserByAccount(String account) {
        return userRepository.findByUserNameAndStatus(account, 1)
                .orElseGet(() -> userRepository.findByEmailAndStatus(account, 1)
                        .orElseGet(() -> userRepository.findByPhoneAndStatus(account, 1)
                                .orElseThrow(() -> new GlobalException("用户不存在或已禁用"))));
    }
}
