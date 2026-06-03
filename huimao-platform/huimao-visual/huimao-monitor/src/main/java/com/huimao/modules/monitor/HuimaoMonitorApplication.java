package com.huimao.modules.monitor;

import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 监控中心
 *
 * @author huimao
 */
@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
@EnableScheduling
public class HuimaoMonitorApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoMonitorApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao监控中心模块启动成功---(^_^)／★＼(^_^) \n");
    }
}
