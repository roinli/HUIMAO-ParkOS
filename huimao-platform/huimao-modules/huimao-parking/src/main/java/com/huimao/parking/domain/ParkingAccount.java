package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车账户
 */
@ApiModel("停车账户")
@Data
@TableName("parking_account")
public class ParkingAccount extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "账户ID", example = "9001")
    private Long accountId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "累计收入", example = "286530.50")
    private BigDecimal totalIncomeAmount;

    @ApiModelProperty(value = "账户余额", example = "84210.16")
    private BigDecimal balanceAmount;

    @ApiModelProperty(value = "可提现余额", example = "76880.16")
    private BigDecimal availableAmount;

    @ApiModelProperty(value = "冻结金额", example = "7330.00")
    private BigDecimal frozenAmount;

    @ApiModelProperty(value = "状态", example = "0")
    private String status;
}
