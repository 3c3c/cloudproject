package com.cloud.auth.init;

import com.cloud.auth.entity.SysUser;
import com.cloud.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动后把 data.sql 写入的明文密码转为 BCrypt（幂等）。
 * 检测规则：密码不以 "$2" 开头即视为明文并重新编码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordDataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        List<SysUser> users = userMapper.selectList(null);
        for (SysUser user : users) {
            String pwd = user.getPassword();
            if (pwd != null && !pwd.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(pwd));
                userMapper.updateById(user);
                log.info("已为用户 [{}] 初始化 BCrypt 密码", user.getUsername());
            }
        }
    }
}
