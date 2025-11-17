package com.aio.module.user.service;

import com.aio.common.exception.BusinessException;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder; // BCrypt加密器

    // 注册
    @Transactional
    public void register(UserEntity user, String rawPassword) {
        // 校验用户名/邮箱/手机号唯一性
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new BusinessException("手机号已被注册");
        }
        // 加密密码
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        // 角色默认为user（前端不传则使用实体类默认值，管理员可手动指定其他角色）
        userRepository.save(user);
    }

    // 登录
    public UserEntity login(String account, String rawPassword) {
        UserEntity user = findUserByAccount(account);
        // 验证密码
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    // 修改密码
    @Transactional
    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        // 加密新密码并更新
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 删除用户（权限控制：管理员可删除所有用户，普通用户仅可删除自己）
    @Transactional
    public void deleteUser(UUID targetUserId, UUID currentUserId, String currentRole) {
        // 检查当前用户是否为管理员
        boolean isAdmin = "admin".equals(currentRole);
        // 非管理员只能删除自己
        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new BusinessException("无权限删除该用户");
        }
        // 执行删除
        userRepository.deleteById(targetUserId);
    }

    // 辅助方法：通过账号（用户名/邮箱/手机号）查询用户（仅查询正常状态用户）
    private UserEntity findUserByAccount(String account) {
        return userRepository.findByUsernameAndStatus(account, 1)
                .orElseGet(() -> userRepository.findByEmailAndStatus(account, 1)
                        .orElseGet(() -> userRepository.findByPhoneAndStatus(account, 1)
                                .orElseThrow(() -> new BusinessException("用户不存在或已禁用"))));
    }
}