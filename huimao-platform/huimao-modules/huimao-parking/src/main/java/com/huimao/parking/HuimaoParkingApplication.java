package com.huimao.parking;

import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 停车场模块
 */
@Slf4j
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
@EnableCaching
public class HuimaoParkingApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoParkingApplication.class, args);
        log.info("------huimao停车场模块启动成功------");
    }
}
