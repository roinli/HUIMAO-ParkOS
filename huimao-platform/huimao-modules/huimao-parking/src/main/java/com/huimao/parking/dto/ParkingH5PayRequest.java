package com.huimao.parking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * H5 支付请求
 */
@ApiModel("H5模拟支付请求")
@Data
public class ParkingH5PayRequest
{
    /**
     * 支付渠道，首版默认 WECHAT。
     */
    @ApiModelProperty(value = "支付渠道", example = "WECHAT", notes = "可扩展 ALIPAY、CASH、MANUAL 等")
    private String payChannel = "WECHAT";
}
