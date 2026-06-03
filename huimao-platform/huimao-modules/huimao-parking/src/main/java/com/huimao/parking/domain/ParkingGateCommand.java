package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 开闸指令
 */
@ApiModel("开闸指令")
@Data
@TableName("parking_gate_command")
public class ParkingGateCommand
{
    @TableId
    @ApiModelProperty(value = "指令ID", example = "8001")
    private Long commandId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "通道ID", example = "3002")
    private Long gateId;
    @ApiModelProperty(value = "设备ID", example = "4002")
    private Long deviceId;
    @ApiModelProperty(value = "停车会话ID", example = "5001")
    private Long sessionId;
    @ApiModelProperty(value = "订单ID", example = "6001")
    private Long orderId;
    @ApiModelProperty(value = "指令号", example = "PC20260423123000123456")
    private String commandNo;

    /**
     * 指令类型，当前仅支持 OPEN_GATE。
     */
    @ApiModelProperty(value = "指令类型", example = "OPEN_GATE")
    private String commandType;

    /**
     * 指令状态，见 ParkingConstants 中 COMMAND_STATUS_* 常量。
     */
    @ApiModelProperty(value = "指令状态", example = "SUCCESS", notes = "见 ParkingConstants.COMMAND_STATUS_*")
    private String commandStatus;

    @ApiModelProperty(value = "指令下发时间", example = "2026-04-23 12:21:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commandTime;

    @ApiModelProperty(value = "回调时间", example = "2026-04-23 12:21:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callbackTime;

    @ApiModelProperty(value = "回调内容")
    private String callbackContent;
    @ApiModelProperty(value = "备注", example = "支付有效期内离场")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2026-04-23 12:21:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-04-23 12:21:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单号", example = "PO20260423123000123456")
    private String orderNo;
}
