package com.huimao.gen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;

/**
 * 代码生成
 *
 * @author huimao
 */
@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
public class HuimaoGenApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoGenApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao代码生成模块启动成功---(^_^)／★＼(^_^) \n");
    }
}
