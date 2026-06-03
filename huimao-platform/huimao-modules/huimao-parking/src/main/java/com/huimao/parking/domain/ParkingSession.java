package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 停车会话
 */
@ApiModel("停车会话")
@Data
@TableName("parking_session")
public class ParkingSession extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "停车会话ID", example = "5001")
    private Long sessionId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;
    @ApiModelProperty(value = "车牌颜色", example = "蓝")
    private String plateColor;

    /**
     * 车辆类型，当前仅预留，首版未做精细化计费。
     */
    @ApiModelProperty(value = "车辆类型", example = "TEMP")
    private String carType;
    @ApiModelProperty(value = "入口通道ID", example = "3001")
    private Long entryGateId;
    @ApiModelProperty(value = "入口设备ID", example = "4001")
    private Long entryDeviceId;

    @ApiModelProperty(value = "入场时间", example = "2026-04-23 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;

    @ApiModelProperty(value = "入场图片地址")
    private String entryImageUrl;
    @ApiModelProperty(value = "出口通道ID", example = "3002")
    private Long exitGateId;
    @ApiModelProperty(value = "出口设备ID", example = "4002")
    private Long exitDeviceId;

    @ApiModelProperty(value = "出场时间", example = "2026-04-23 12:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date exitTime;

    @ApiModelProperty(value = "出场图片地址")
    private String exitImageUrl;
    @ApiModelProperty(value = "停车时长(分钟)", example = "135")
    private Integer parkingMinutes;

    /**
     * 会话状态，见 ParkingConstants 中 SESSION_STATUS_* 常量。
     */
    @ApiModelProperty(value = "会话状态", example = "WAIT_PAY", notes = "见 ParkingConstants.SESSION_STATUS_*")
    private String status;

    /**
     * 当前待处理订单号。
     * 通常指当前会话最后一笔未完成订单，便于 H5 和出口快速定位。
     */
    @ApiModelProperty(value = "当前待处理订单号", example = "PO20260423123000123456")
    private String currentOrderNo;
    @ApiModelProperty(value = "累计应付金额", example = "18.00")
    private BigDecimal totalPayableAmount;
    @ApiModelProperty(value = "累计已付金额", example = "10.00")
    private BigDecimal totalPaidAmount;

    @ApiModelProperty(value = "离场截止时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leaveDeadlineTime;

    /**
     * 是否异常，0 否，1 是。
     */
    @ApiModelProperty(value = "是否异常", example = "0", notes = "0 否，1 是")
    private String isAbnormal;

    @TableField(exist = false)
    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @TableField(exist = false)
    @ApiModelProperty(value = "租户名称", example = "演示租户")
    private String tenantName;
}
