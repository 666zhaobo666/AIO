package com.aio.module.user.utils;

import com.aio.module.user.exception.UserException;
import com.aio.module.user.enums.UserExceptionEnum;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.module.user.enums.UserGenderEnum;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UserValidationUtils {
    private final UserRepository userRepository;

    public UserValidationUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

/** ==================== 长度限制常量（解决魔法数字警告）==================== */
    /** 用户名最小长度 */
    private static final int USERNAME_MIN_LENGTH = 4;
    /** 用户名最大长度 */
    private static final int USERNAME_MAX_LENGTH = 16;
    /** 密码最小长度 */
    private static final int PASSWORD_MIN_LENGTH = 6;
    /** 密码最大长度 */
    private static final int PASSWORD_MAX_LENGTH = 16;
    /** 邮箱顶级域名最小长度（如 .cn、.io） */
    private static final int EMAIL_TLD_MIN_LENGTH = 2;
    /** 邮箱顶级域名最大长度（如 .company、.museum） */
    private static final int EMAIL_TLD_MAX_LENGTH = 6;

    // ==================== 预编译正则 Pattern（线程安全，解决重复编译警告）====================
    /**
     * 用户名正则：仅允许字母、数字、下划线（下划线不能单独作为开头/结尾，需结合业务调整）
     * 匹配规则：
     * - 字符集：a-z、A-Z、0-9、下划线（_）
     * - 长度：4-16 位
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{" + USERNAME_MIN_LENGTH + "," + USERNAME_MAX_LENGTH + "}$"
    );

    /**
     * 密码正则：仅允许字母、数字、下划线（建议业务中扩展特殊字符，如 !@#$% 提高安全性）
     * 匹配规则：
     * - 字符集：a-z、A-Z、0-9、下划线（_）
     * - 长度：6-16 位
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{" + PASSWORD_MIN_LENGTH + "," + PASSWORD_MAX_LENGTH + "}$"
    );

    /**
     * 邮箱正则：符合 RFC 标准，支持常见合法格式（如 user.name+tag@example-co.com、a.b.c@x.y.z.cn）
     * 匹配规则：
     * - 用户名：字母、数字、点（.）、百分号（%）、加号（+）、连字符（-）、下划线（_）
     * - 域名：字母、数字、连字符（-）、点（.）（支持多级域名）
     * - 顶级域名：2-6 位字母（如 .com、.cn、.museum）
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{" + EMAIL_TLD_MIN_LENGTH + "," + EMAIL_TLD_MAX_LENGTH + "}$"
    );

    /**
     * 手机号正则：支持国内手机号（含可选前缀 +86/0）
     * 匹配规则：
     * - 前缀：可选 +86 或 0（如 139xxxx1234、+86139xxxx1234、0139xxxx1234）
     * - 号段：13/14(5/7/9)/15(0-3/5-9)/17(0/1/3/5-8)/18/19 开头
     * - 总长度：11 位核心数字（含前缀时总长度兼容）
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+86|0)?1(3\\d|4[579]|5[0-35-9]|7[0135678]|8\\d|9[013589])\\d{8}$"
    );

    // ==================== 校验方法 ====================
    // 校验用户名格式
    public void validateUserName(String username) {
        if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
            throw new UserException(UserExceptionEnum.USERNAME_VALIDATION_ERROR);
        }
    }

    // 校验密码格式
    public void validatePassword(String password) {
        if (password != null && !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new UserException(UserExceptionEnum.PASSWORD_VALIDATION_ERROR);
        }
    }

    // 校验邮箱格式
    public void validateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserException(UserExceptionEnum.EMAIL_VALIDATION_ERROR);
        }
    }

    // 校验手机号格式
    public void validatePhone(String phone) {
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new UserException(UserExceptionEnum.PHONE_VALIDATION_ERROR);
        }
    }

    // 校验生日格式
    public void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new UserException(UserExceptionEnum.BIRTHDAY_VALIDATION_ERROR);
        }
    }

    // 校验用户性别
    public void validateGender(String gender) {
        if (gender == null || !UserGenderEnum.isValid(gender)) {
            throw new UserException(UserExceptionEnum.GENDER_VALIDATION_ERROR);
        }
    }

    // 校验用户角色
    public void validateRole(String role) {
        if (role == null || !UserRoleEnum.isValid(role)) {
            throw new UserException(UserExceptionEnum.ROLE_VALIDATION_ERROR);
        }
    }

    // 校验用户名/邮箱/手机号唯一性
    public void validateUserAccountUniqueness(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserException(UserExceptionEnum.USERNAME_ALREADY_EXIST);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException(UserExceptionEnum.EMAIL_ALREADY_EXIST);
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new UserException(UserExceptionEnum.PHONE_ALREADY_EXIST);
        }
    }
}