package com.huimao.parking.simulator.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 模拟器设备查询参数。
 */
@Data
@ApiModel("模拟器设备查询参数")
public class ParkingSimulatorDeviceQuery
{
    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @ApiModelProperty(value = "设备编码", example = "DEV-CAM-01")
    private String deviceCode;

    @ApiModelProperty(value = "设备名称", example = "北门入口相机")
    private String deviceName;

    @ApiModelProperty(value = "模拟器状态", example = "RUNNING")
    private String simulatorStatus;

    @ApiModelProperty(value = "是否自动心跳", example = "1")
    private String autoHeartbeat;
}
