package com.cloud.auth.service;

public interface SmsService {

    /** 发送验证码，返回验证码内容（日志模拟，生产对接短信网关） */
    String sendCode(String mobile);

    /** 校验验证码，校验通过后删除（防重复使用）；失败抛业务异常 */
    void verifyCode(String mobile, String code);
}
