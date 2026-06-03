package com.huimao.parking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯地图接口配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "parking.tencent-map")
public class TencentMapProperties
{
    /**
     * 腾讯地图 WebService Key。
     */
    private String key = "E3VBZ-FSRL4-DENUC-KCIS7-N6B5H-UGBY6";
}
