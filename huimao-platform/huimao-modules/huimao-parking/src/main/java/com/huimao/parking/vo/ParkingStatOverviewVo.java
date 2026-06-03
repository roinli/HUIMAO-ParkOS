package com.huimao.parking.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收入统计汇总
 */
@ApiModel("收入统计汇总")
@Data
public class ParkingStatOverviewVo
{
    @ApiModelProperty(value = "统计周期收入", example = "2680.00")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "总进场数", example = "120")
    private Integer totalEntry;

    @ApiModelProperty(value = "总出场数", example = "110")
    private Integer totalExit;

    @ApiModelProperty(value = "平均每单", example = "27.92")
    private BigDecimal avgOrder;
}
