package com.huimao.parking.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 设备处理结果
 */
@ApiModel("设备出场处理结果")
@Data
public class DeviceProcessResult
{
    /**
     * 是否允许直接开闸放行。
     */
    @ApiModelProperty(value = "是否允许直接开闸", example = "true")
    private boolean allowOpen;

    /**
     * 给调用方的处理结果说明。
     */
    @ApiModelProperty(value = "处理结果说明", example = "允许离场")
    private String message;

    /**
     * 如需开闸，返回生成的开闸指令号。
     */
    @ApiModelProperty(value = "开闸指令号", example = "PC20260423123000123456")
    private String commandNo;

    /**
     * 如需先缴费，返回关联订单号。
     */
    @ApiModelProperty(value = "关联订单号", example = "PO20260423123000123456")
    private String orderNo;

    /**
     * 当前应付金额。
     */
    @ApiModelProperty(value = "当前应付金额", example = "12.50")
    private BigDecimal payableAmount;
}
