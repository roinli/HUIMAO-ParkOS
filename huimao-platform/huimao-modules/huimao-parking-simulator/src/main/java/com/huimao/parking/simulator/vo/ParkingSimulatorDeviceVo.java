package com.huimao.parking.simulator.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 模拟器设备列表视图。
 */
@Data
@ApiModel("模拟器设备视图")
public class ParkingSimulatorDeviceVo
{
    @ApiModelProperty(value = "设备ID", example = "4001")
    private Long deviceId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @ApiModelProperty(value = "通道ID", example = "3001")
    private Long gateId;

    @ApiModelProperty(value = "通道编码", example = "GATE-IN-01")
    private String gateCode;

    @ApiModelProperty(value = "通道名称", example = "北门入口")
    private String gateName;

    @ApiModelProperty(value = "通道类型", example = "1", notes = "1 入口，2 出口")
    private String gateType;

    @ApiModelProperty(value = "设备编码", example = "DEV-CAM-01")
    private String deviceCode;

    @ApiModelProperty(value = "设备名称", example = "北门入口相机")
    private String deviceName;

    @ApiModelProperty(value = "设备类型", example = "camera")
    private String deviceType;

    @ApiModelProperty(value = "厂商编码", example = "SIM")
    private String vendorCode;

    @ApiModelProperty(value = "接入方式", example = "SIMULATED")
    private String connectMode;

    @ApiModelProperty(value = "平台设备状态", example = "0", notes = "0 在线/启用，1 离线，2 停用")
    private String deviceStatus;

    @ApiModelProperty(value = "是否启用模拟器", example = "1")
    private String simulatorEnabled;

    @ApiModelProperty(value = "模拟器状态", example = "RUNNING")
    private String simulatorStatus;

    @ApiModelProperty(value = "在线状态", example = "ONLINE")
    private String onlineStatus;

    @ApiModelProperty(value = "是否自动心跳", example = "1")
    private String autoHeartbeat;

    @ApiModelProperty(value = "心跳间隔秒数", example = "30")
    private Integer heartbeatIntervalSec;

    @ApiModelProperty(value = "是否自动回调", example = "1")
    private String autoCallback;

    @ApiModelProperty(value = "回调延迟秒数", example = "1")
    private Integer callbackDelaySec;

    @ApiModelProperty(value = "默认模拟车牌号", example = "粤B12345")
    private String defaultPlateNo;

    @ApiModelProperty(value = "最近一次心跳时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeatTime;

    @ApiModelProperty(value = "最近一次请求时间", example = "2026-04-23 12:30:05")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRequestTime;

    @ApiModelProperty(value = "最近一次返回码", example = "200")
    private Integer lastResponseCode;

    @ApiModelProperty(value = "最近一次返回消息", example = "心跳成功")
    private String lastResponseMsg;

    @ApiModelProperty(value = "最近一次指令号", example = "PC20260423123000123456")
    private String lastCommandNo;

    @ApiModelProperty(value = "最近一次订单号", example = "PO20260423123000123456")
    private String lastOrderNo;

    @ApiModelProperty(value = "备注")
    private String remark;
}
