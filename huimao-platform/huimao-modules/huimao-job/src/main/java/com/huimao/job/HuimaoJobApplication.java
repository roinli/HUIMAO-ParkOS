package com.huimao.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;

@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
public class HuimaoJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuimaoJobApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao定时任务模块启动成功---(^_^)／★＼(^_^) \n");
    }

}
