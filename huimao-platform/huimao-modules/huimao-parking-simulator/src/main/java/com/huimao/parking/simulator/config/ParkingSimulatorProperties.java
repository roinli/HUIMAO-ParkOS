package com.huimao.parking.simulator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 模拟器配置。
 */
@Data
@ConfigurationProperties(prefix = "parking.simulator")
@Configuration
public class ParkingSimulatorProperties
{
    /**
     * 停车平台开放接口根地址，建议配置为网关 parking 路由地址。
     */
    @Value("${parking.simulator.platform-url:http://171.111.217.227:48080/parking}")
    private String platformUrl = "http://171.111.217.227:48080/parking" ;

    /**
     * 调度扫描周期，单位毫秒。
     */
    private Long scheduleDelayMs = 5000L;

    /**
     * 默认心跳间隔，单位秒。
     */
    private Integer defaultHeartbeatIntervalSec = 30;

    /**
     * 默认自动回调延迟，单位秒。
     */
    private Integer defaultCallbackDelaySec = 1;
}
