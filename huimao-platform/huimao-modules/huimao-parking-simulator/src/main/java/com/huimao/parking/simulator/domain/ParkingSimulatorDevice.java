package com.huimao.parking.simulator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 模拟器设备状态表。
 */
@Data
@ApiModel("模拟器设备状态")
@TableName("parking_simulator_device")
public class ParkingSimulatorDevice extends BaseEntity
{
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "模拟器记录ID", example = "1")
    private Long simId;

    @ApiModelProperty(value = "停车设备ID", example = "4001")
    private Long deviceId;

    @ApiModelProperty(value = "是否启用模拟器", example = "1", notes = "1 是，0 否")
    private String simulatorEnabled;

    @ApiModelProperty(value = "模拟器状态", example = "RUNNING", notes = "RUNNING 运行中，STOPPED 已停止")
    private String simulatorStatus;

    @ApiModelProperty(value = "在线状态", example = "ONLINE", notes = "ONLINE 在线，OFFLINE 离线")
    private String onlineStatus;

    @ApiModelProperty(value = "是否自动心跳", example = "1", notes = "1 是，0 否")
    private String autoHeartbeat;

    @ApiModelProperty(value = "心跳间隔秒数", example = "30")
    private Integer heartbeatIntervalSec;

    @ApiModelProperty(value = "是否自动回调开闸结果", example = "1", notes = "1 是，0 否")
    private String autoCallback;

    @ApiModelProperty(value = "自动回调延迟秒数", example = "1")
    private Integer callbackDelaySec;

    @ApiModelProperty(value = "最近一次心跳时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeatTime;

    @ApiModelProperty(value = "最近一次请求时间", example = "2026-04-23 12:30:05")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRequestTime;

    @ApiModelProperty(value = "最近一次平台返回码", example = "200")
    private Integer lastResponseCode;

    @ApiModelProperty(value = "最近一次平台返回消息", example = "心跳成功")
    private String lastResponseMsg;

    @ApiModelProperty(value = "最近一次平台返回指令号", example = "PC20260423123000123456")
    private String lastCommandNo;

    @ApiModelProperty(value = "最近一次平台返回订单号", example = "PO20260423123000123456")
    private String lastOrderNo;

    @ApiModelProperty(value = "默认模拟车牌号", example = "粤B12345")
    private String defaultPlateNo;
}
