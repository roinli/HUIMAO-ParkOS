package com.huimao.parking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * H5 缴费记录视图
 */
@ApiModel("H5缴费记录视图")
@Data
public class ParkingH5PaymentRecordVo
{
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @ApiModelProperty(value = "停车会话ID", example = "5001")
    private Long sessionId;

    @ApiModelProperty(value = "订单号", example = "PO20260423123000123456")
    private String orderNo;

    @ApiModelProperty(value = "支付流水号", example = "PP20260423123000123456")
    private String paymentNo;

    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;

    @ApiModelProperty(value = "订单类型", example = "NORMAL")
    private String orderType;

    @ApiModelProperty(value = "支付渠道", example = "WECHAT")
    private String payChannel;

    @ApiModelProperty(value = "支付金额", example = "18.00")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "支付状态", example = "SUCCESS")
    private String payStatus;

    @ApiModelProperty(value = "停车时长(分钟)", example = "135")
    private Integer parkingMinutes;

    @ApiModelProperty(value = "入场时间", example = "2026-04-23 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;

    @ApiModelProperty(value = "计费时间", example = "2026-04-23 12:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;

    @ApiModelProperty(value = "支付成功时间", example = "2026-04-23 12:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paySuccessTime;

    @ApiModelProperty(value = "离场截止时间", example = "2026-04-23 12:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leaveDeadlineTime;
}
