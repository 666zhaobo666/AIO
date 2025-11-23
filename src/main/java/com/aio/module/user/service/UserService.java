package com.aio.module.user.service;

import com.aio.api.user.model.UserApiResponse;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.enmus.UserRoleEnmu;
import com.aio.module.user.enmus.UserExceptionEnum;
import com.aio.module.user.entity.UserPasswordEntity;
import com.aio.module.user.exception.UserException;
import com.aio.module.user.repository.UserRepository;
import com.aio.module.user.repository.UserPasswordRepository;
import com.aio.module.user.utils.UserValidationUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final PasswordEncoder passwordEncoder; // BCrypt加密器
    private final UserValidationUtils userValidationUtils; // 校验工具类

    // 注册
    @Transactional
    public UserEntity register(UserEntity user, String rawPassword) {
        // 校验用户信息
        validateUser(user, rawPassword);
        // 角色默认为user（前端不传则使用实体类默认值，管理员可手动指定其他角色）
        userRepository.save(user);
        // 加密密码
        userPasswordRepository.save(new UserPasswordEntity(user.getUserId(), passwordEncoder.encode(rawPassword)));

        return user;
    }

    // 用户信息校验
    public void validateUser(UserEntity user, String rawPassword) {
        // 格式
        userValidationUtils.validateUserName(user.getUsername());
        userValidationUtils.validatePassword(rawPassword);
        userValidationUtils.validateEmail(user.getEmail());
        userValidationUtils.validatePhone(user.getPhone());
        userValidationUtils.validateBirthday(user.getBirthday());
        userValidationUtils.validateGender(user.getGender());
        userValidationUtils.validateRole(user.getRole());

        // 唯一性
        userValidationUtils.validateUserAccountUniqueness(user);
    }

    // 登录
    public UserEntity login(String account, String rawPassword) {
        UserEntity user = findUserByAccount(account);
        // 用户不存在
        if(user == null) {
            throw new UserException(UserExceptionEnum.USER_NOT_EXIST);
        }
        // 验证密码
        if (!passwordEncoder.matches(rawPassword, userPasswordRepository.findPasswordByUserId(user.getUserId()))) {
            throw new UserException(UserExceptionEnum.USER_PASSWORD_ERROR);
        }
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    // 修改密码
    @Transactional
    public void updatePassword(UUID userId, String oldPassword, String newPassword) {

        // 验证旧密码
        UserPasswordEntity userPassword = (UserPasswordEntity) userPasswordRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserExceptionEnum.USER_PASSWORD_NOT_EXIST));

        if (!passwordEncoder.matches(oldPassword, userPassword.getPassword())) {
            throw new UserException(UserExceptionEnum.USER_OLD_PASSWORD_ERROR);
        }

        userPassword.setPassword(passwordEncoder.encode(newPassword));
        userPassword.setUpdatedTime(LocalDateTime.now());
        userPasswordRepository.save(userPassword);
    }


    // 删除用户（权限控制：管理员可删除所有用户，普通用户仅可删除自己）
    @Transactional
    public void deleteUser(UUID targetUserId, UUID currentUserId, String currentRole) {
        // 检查当前用户是否为管理员
        boolean isAdmin = UserRoleEnmu.ADMIN.getValue().equals(currentRole);
        // 非管理员只能删除自己
        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new UserException(UserExceptionEnum.USER_NOT_ADMIN);
        }
        // 执行删除
        userPasswordRepository.deleteById(targetUserId);
        userRepository.deleteById(targetUserId);
    }

    public UserApiResponse getUserInfo(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserExceptionEnum.USER_NOT_EXIST));
        UserApiResponse response = new UserApiResponse();
        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(user);
        return response;
    }

    // 辅助方法：通过账号（用户名/邮箱/手机号）查询用户（仅查询正常状态用户）
    private UserEntity findUserByAccount(String account) {
        return userRepository.findByUsernameAndStatus(account, 1)
                .orElseGet(() -> userRepository.findByEmailAndStatus(account, 1)
                        .orElseGet(() -> userRepository.findByPhoneAndStatus(account, 1)
                                .orElseThrow(() -> new UserException("用户不存在或已禁用"))));
    }
}