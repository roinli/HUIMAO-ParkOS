package com.huimao.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;

/**
 * 系统模块
 *
 * @author huimao
 */
@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
public class HuimaoDemoApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoDemoApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao Demo样例模块启动成功---(^_^)／★＼(^_^) \n");
    }
}
