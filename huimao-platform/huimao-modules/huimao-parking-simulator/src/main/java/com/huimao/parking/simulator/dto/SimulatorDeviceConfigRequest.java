package com.huimao.parking.simulator.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 模拟器配置请求。
 */
@Data
@ApiModel("模拟器配置请求")
public class SimulatorDeviceConfigRequest
{
    @NotNull(message = "设备ID不能为空")
    @ApiModelProperty(value = "设备ID", required = true, example = "4001")
    private Long deviceId;

    @ApiModelProperty(value = "是否自动心跳", example = "1", notes = "1 是，0 否")
    private String autoHeartbeat;

    @ApiModelProperty(value = "心跳间隔秒数", example = "30")
    private Integer heartbeatIntervalSec;

    @ApiModelProperty(value = "是否自动回调开闸结果", example = "1", notes = "1 是，0 否")
    private String autoCallback;

    @ApiModelProperty(value = "自动回调延迟秒数", example = "1")
    private Integer callbackDelaySec;

    @ApiModelProperty(value = "默认模拟车牌号", example = "粤B12345")
    private String defaultPlateNo;

    @ApiModelProperty(value = "备注")
    private String remark;
}
