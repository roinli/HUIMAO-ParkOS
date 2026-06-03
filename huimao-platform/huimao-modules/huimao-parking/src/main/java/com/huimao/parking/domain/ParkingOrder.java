package com.huimao.parking.domain;

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
 * 停车订单
 */
@ApiModel("停车订单")
@Data
@TableName("parking_order")
public class ParkingOrder extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "订单ID", example = "6001")
    private Long orderId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "停车会话ID", example = "5001")
    private Long sessionId;

    /**
     * 父订单 ID。
     * 首笔停车订单为空，补费订单会指向上一笔订单。
     */
    @ApiModelProperty(value = "父订单ID", example = "6000", notes = "首笔停车订单为空，补费订单会指向上一笔订单")
    private Long parentOrderId;
    @ApiModelProperty(value = "订单号", example = "PO20260423123000123456")
    private String orderNo;

    /**
     * 订单类型，见 ParkingConstants 中 ORDER_TYPE_* 常量。
     */
    @ApiModelProperty(value = "订单类型", example = "NORMAL", notes = "见 ParkingConstants.ORDER_TYPE_*")
    private String orderType;
    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;

    @ApiModelProperty(value = "入场时间", example = "2026-04-23 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;

    @ApiModelProperty(value = "计费时间", example = "2026-04-23 12:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;

    @ApiModelProperty(value = "停车时长(分钟)", example = "135")
    private Integer parkingMinutes;
    @ApiModelProperty(value = "应付金额", example = "18.00")
    private BigDecimal payableAmount;
    @ApiModelProperty(value = "优惠金额", example = "0.00")
    private BigDecimal discountAmount;
    @ApiModelProperty(value = "已付金额", example = "18.00")
    private BigDecimal paidAmount;

    /**
     * 业务订单状态，见 ParkingConstants 中 ORDER_STATUS_* 常量。
     */
    @ApiModelProperty(value = "订单状态", example = "UNPAID", notes = "见 ParkingConstants.ORDER_STATUS_*")
    private String orderStatus;

    /**
     * 支付状态，见 ParkingConstants 中 PAY_STATUS_* 常量。
     */
    @ApiModelProperty(value = "支付状态", example = "UNPAID", notes = "见 ParkingConstants.PAY_STATUS_*")
    private String payStatus;

    @ApiModelProperty(value = "支付时间", example = "2026-04-23 12:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @ApiModelProperty(value = "离场截止时间", example = "2026-04-23 12:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leaveDeadlineTime;

    @ApiModelProperty(value = "关闭时间", example = "2026-04-23 12:40:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;
}
