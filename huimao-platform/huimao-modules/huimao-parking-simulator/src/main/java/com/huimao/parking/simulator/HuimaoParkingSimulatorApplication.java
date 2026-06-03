package com.huimao.parking.simulator;

import com.huimao.common.security.annotation.EnableCustomConfig;
import com.huimao.common.security.annotation.EnableHuimaoFeignClients;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 停车设备模拟器模块。
 */
@Slf4j
@EnableScheduling
@EnableCustomConfig
@EnableCustomSwagger2
@EnableHuimaoFeignClients
@SpringBootApplication
public class HuimaoParkingSimulatorApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoParkingSimulatorApplication.class, args);
        log.info("------huimao停车设备模拟器模块启动成功------");
    }
}
