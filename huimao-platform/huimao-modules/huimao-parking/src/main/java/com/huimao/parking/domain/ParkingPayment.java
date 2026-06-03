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
 * 支付流水
 */
@ApiModel("停车支付流水")
@Data
@TableName("parking_payment")
public class ParkingPayment extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "支付流水ID", example = "7001")
    private Long paymentId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "订单ID", example = "6001")
    private Long orderId;
    @ApiModelProperty(value = "订单号", example = "PO20260423123000123456")
    private String orderNo;
    @ApiModelProperty(value = "支付流水号", example = "PP20260423123000123456")
    private String paymentNo;

    /**
     * 支付渠道，首版默认 WECHAT，后续可扩展 ALIPAY、CASH、MANUAL 等。
     */
    @ApiModelProperty(value = "支付渠道", example = "WECHAT", notes = "首版默认 WECHAT，后续可扩展 ALIPAY、CASH、MANUAL")
    private String payChannel;

    @ApiModelProperty(value = "支付金额", example = "18.00")
    private BigDecimal payAmount;

    /**
     * 支付状态，见 ParkingConstants 中 PAY_STATUS_* 常量。
     */
    @ApiModelProperty(value = "支付状态", example = "SUCCESS", notes = "见 ParkingConstants.PAY_STATUS_*")
    private String payStatus;

    /**
     * 第三方支付平台返回的交易流水号。
     */
    @ApiModelProperty(value = "第三方交易流水号", example = "42000000000001")
    private String thirdTradeNo;

    /**
     * 支付回调或支付请求的原始报文，便于对账与排障。
     */
    @ApiModelProperty(value = "通知内容或支付原始报文")
    private String notifyContent;

    @ApiModelProperty(value = "支付成功时间", example = "2026-04-23 12:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paySuccessTime;
}
