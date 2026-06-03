package com.huimao.parking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * H5 待支付订单视图
 */
@ApiModel("H5待支付订单视图")
@Data
public class ParkingH5OrderVo
{
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;
    @ApiModelProperty(value = "停车会话ID", example = "2001")
    private Long sessionId;
    @ApiModelProperty(value = "订单号", example = "PO20260423123000123456")
    private String orderNo;
    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;

    /**
     * 会话状态，见 ParkingConstants 中 SESSION_STATUS_* 常量。
     */
    @ApiModelProperty(value = "会话状态", example = "WAIT_PAY", notes = "见 ParkingConstants.SESSION_STATUS_*")
    private String sessionStatus;

    /**
     * 订单状态，见 ParkingConstants 中 ORDER_STATUS_* 常量。
     */
    @ApiModelProperty(value = "订单状态", example = "UNPAID", notes = "见 ParkingConstants.ORDER_STATUS_*")
    private String orderStatus;

    /**
     * 支付状态，见 ParkingConstants 中 PAY_STATUS_* 常量。
     */
    @ApiModelProperty(value = "支付状态", example = "UNPAID", notes = "见 ParkingConstants.PAY_STATUS_*")
    private String payStatus;

    /**
     * 当前累计停车分钟数。
     */
    @ApiModelProperty(value = "当前累计停车分钟数", example = "135")
    private Integer parkingMinutes;

    /**
     * 当前应付金额。
     */
    @ApiModelProperty(value = "当前应付金额", example = "18.00")
    private BigDecimal payableAmount;

    /**
     * 当前已付金额。
     */
    @ApiModelProperty(value = "当前已付金额", example = "0.00")
    private BigDecimal paidAmount;

    @ApiModelProperty(value = "入场时间", example = "2026-04-23 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;

    @ApiModelProperty(value = "当前计费时间", example = "2026-04-23 12:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;

    @ApiModelProperty(value = "离场截止时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leaveDeadlineTime;
}
