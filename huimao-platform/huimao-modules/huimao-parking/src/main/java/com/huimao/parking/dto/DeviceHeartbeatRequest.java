package com.huimao.parking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 设备心跳
 */
@ApiModel("设备心跳请求")
@Data
public class DeviceHeartbeatRequest
{
    /**
     * 设备编码。
     */
    @ApiModelProperty(value = "设备编码", required = true, example = "DEV-CAM-01")
    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    /**
     * 心跳时间。
     */
    @ApiModelProperty(value = "心跳时间", example = "2026-04-23 12:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date heartbeatTime;

    /**
     * 心跳原始报文。
     */
    @ApiModelProperty(value = "心跳原始报文")
    private String rawContent;
}
