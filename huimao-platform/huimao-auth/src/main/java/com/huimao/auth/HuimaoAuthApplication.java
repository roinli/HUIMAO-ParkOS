package com.huimao.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 认证授权中心
 *
 * @author huimao
 */
@Slf4j
@com.huimao.common.security.annotation.EnableHuimaoFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class HuimaoAuthApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoAuthApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao权限启动成功---(^_^)／★＼(^_^) \n");
    }
}
