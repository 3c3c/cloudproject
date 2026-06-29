package com.cloud.auth.service;

/**
 * RSA加密解密服务接口
 */
public interface RSAService {

    /**
     * 解密前端RSA加密的密码
     *
     * @param encryptedPassword RSA加密的密文
     * @return 解密后的明文密码
     * @throws RuntimeException 解密失败时抛出异常
     */
    String decryptPassword(String encryptedPassword);
}
