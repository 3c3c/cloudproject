package com.cloud.auth.service.impl;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.cloud.auth.service.RSAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RSA加密解密服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RSAServiceImpl implements RSAService {

    private final RSA rsaObject;

    @Override
    public String decryptPassword(String encryptedPassword) {
        try {
            log.debug("开始解密RSA加密的密码");
            // 使用私钥解密前端传来的密文
            String plainPassword = rsaObject.decryptStr(encryptedPassword, KeyType.PrivateKey);
            log.debug("密码解密成功");
            return plainPassword;
        } catch (Exception e) {
            log.error("密码解密失败", e);
            throw new RuntimeException("密码解密失败，请检查加密方式是否正确");
        }
    }
}
