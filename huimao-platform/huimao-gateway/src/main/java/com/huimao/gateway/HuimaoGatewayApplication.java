package com.huimao.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * 网关启动程序
 *
 * @author huimao
 */
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class HuimaoGatewayApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoGatewayApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao网关启动成功---(^_^)／★＼(^_^) \n");
    }
}
