package com.aio.module.user.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.MatchParam;
import com.aio.api.model.CaptchaVerifyDto;
import com.aio.common.service.ViteConfigService;
import com.aio.api.CaptchaApi;
import com.aio.api.model.ModelApiResponse;
import com.aio.common.entity.ViteConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class CaptchaController implements CaptchaApi{

    private final ViteConfigService viteConfigService;

    private static final String[] OPTIONS = {
        "SLIDER", "WORD_IMAGE_CLICK", "ROTATE", "CONCAT"
    };
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    ImageCaptchaApplication application;

    @Override
    public ResponseEntity<ModelApiResponse> apiCaptchaCheckPost() {
        ModelApiResponse response = new ModelApiResponse();
        ViteConfig viteConfig = viteConfigService.getOne(new QueryWrapper<ViteConfig>().eq("name", "captcha_enabled"));
        if (viteConfig == null) {
            response.setCode(200);
            response.setMsg("未配置验证码");
            response.setData(JsonNullable.of(0));
            return ResponseEntity.ok(response);
        }
        if (!Objects.equals(viteConfig.getValue(), "true")) {
            response.setCode(200);
            response.setMsg("未启用验证码");
            response.setData(JsonNullable.of(0));
            return ResponseEntity.ok(response);
        }
        response.setCode(200);
        response.setMsg("验证码已启用");
        response.setData(JsonNullable.of(1));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> apiCaptchaGeneratePost() {
        ViteConfig viteConfig = viteConfigService.getOne(new QueryWrapper<ViteConfig>().eq("name", "captcha_type"));
        String captchaType;
        if (viteConfig == null || Objects.equals(viteConfig.getValue(), "RANDOM")) {
            captchaType = getRandomOption();
        }else {
            captchaType = viteConfig.getValue();
        }
        CaptchaResponse<ImageCaptchaVO> captcha = application.generateCaptcha(captchaType);

        ModelApiResponse resp = new ModelApiResponse();
        resp.setCode(200);
        resp.setMsg("success");
        resp.setData(JsonNullable.of(captcha));
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<ModelApiResponse> apiCaptchaVerifyPost(@Valid @RequestBody CaptchaVerifyDto verifyDto) {
        ModelApiResponse response = new ModelApiResponse();
        ApiResponse<?> data = application.matching(verifyDto.getId(), (MatchParam) verifyDto.getData());
        if (data.isSuccess()) {
            response.setCode(200);
            response.setMsg("success");
            response.setData(JsonNullable.of(data));
            return ResponseEntity.ok(response);
        }
        response.setCode(400);
        response.setMsg("验证码错误");
        response.setData(JsonNullable.of(data));
        return ResponseEntity.badRequest().body(response);
    }

    public static String getRandomOption() {
        int index = SECURE_RANDOM.nextInt(OPTIONS.length);
        return OPTIONS[index];
    }
}
