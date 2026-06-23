package com.cloud.auth.service.impl;

import com.cloud.auth.service.SmsService;
import com.cloud.common.constant.RedisConstants;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    @Override
    public String sendCode(String mobile) {
        // 60 秒内禁止重复发送
        if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisConstants.smsRateKey(mobile)))) {
            throw new BusinessException(ResultCode.SMS_CODE_RATE_LIMIT);
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(RedisConstants.smsCodeKey(mobile), code,
                RedisConstants.SMS_CODE_TTL, RedisConstants.SMS_CODE_UNIT);
        redisTemplate.opsForValue().set(RedisConstants.smsRateKey(mobile), "1",
                RedisConstants.SMS_RATE_TTL, RedisConstants.SMS_RATE_UNIT);
        // 生产环境请对接真实短信网关，切勿打印验证码
        log.info("[短信模拟] 向 {} 发送验证码: {}", mobile, code);
        return code;
    }

    @Override
    public void verifyCode(String mobile, String code) {
        String expected = redisTemplate.opsForValue().get(RedisConstants.smsCodeKey(mobile));
        if (!StringUtils.hasText(expected) || !expected.equals(code)) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(RedisConstants.smsCodeKey(mobile));
    }
}
