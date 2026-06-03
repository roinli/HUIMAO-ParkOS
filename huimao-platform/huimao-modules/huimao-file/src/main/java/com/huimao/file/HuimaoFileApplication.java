package com.huimao.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.huimao.common.swagger.annotation.EnableCustomSwagger2;

/**
 * 文件服务
 *
 * @author huimao
 */
@Slf4j
@EnableCustomSwagger2
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class HuimaoFileApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HuimaoFileApplication.class, args);
        log.info(" (^^)／▽ ▽＼(^^)------huimao文件服务启动成功---(^_^)／★＼(^_^) \n");
    }
}
