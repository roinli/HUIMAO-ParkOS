package com.huimao.parking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现申请
 */
@ApiModel("提现申请请求")
@Data
public class ParkingWithdrawApplyRequest
{
    @ApiModelProperty(value = "申请金额", required = true, example = "3000.00")
    private BigDecimal applyAmount;

    @ApiModelProperty(value = "申请备注", example = "周结算提现")
    private String applyRemark;
}
