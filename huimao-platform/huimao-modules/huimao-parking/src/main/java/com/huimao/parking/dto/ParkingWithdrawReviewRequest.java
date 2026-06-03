package com.huimao.parking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 提现审核
 */
@ApiModel("提现审核请求")
@Data
public class ParkingWithdrawReviewRequest
{
    @ApiModelProperty(value = "提现单ID", required = true, example = "1")
    private Long withdrawId;

    @ApiModelProperty(value = "审核结果，仅支持 PAID 或 REJECTED", required = true, example = "PAID")
    private String status;

    @ApiModelProperty(value = "审核备注", example = "审核通过")
    private String auditRemark;
}
