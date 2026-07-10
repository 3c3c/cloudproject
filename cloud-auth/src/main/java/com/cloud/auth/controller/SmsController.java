package com.cloud.auth.controller;

import com.cloud.auth.dto.SendCodeRequest;
import com.cloud.auth.service.SmsService;
import com.cloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码接口。
 */
@RestController
@RequestMapping("/auth/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public Result<Void> send(@Valid @RequestBody SendCodeRequest req) {
        smsService.sendCode(req.getMobile());
        return Result.success();
    }
}
