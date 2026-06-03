package com.huimao.parking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.parking.domain.ParkingAccountFlow;
import com.huimao.parking.domain.ParkingWithdrawOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 账户总览
 */
@ApiModel("账户总览")
@Data
public class ParkingAccountSummaryVo
{
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

    @ApiModelProperty(value = "账户状态", example = "0")
    private String status;

    @ApiModelProperty(value = "手续费率", example = "0.0060")
    private BigDecimal feeRate;

    @ApiModelProperty(value = "最小提现金额", example = "100.00")
    private BigDecimal minWithdrawAmount;

    @ApiModelProperty(value = "结算商户名称", example = "维途停车")
    private String merchantName;

    @ApiModelProperty(value = "结算户名", example = "维途停车运营账户")
    private String settleMerchantName;

    @ApiModelProperty(value = "开户行", example = "招商银行深圳科技园支行")
    private String settleBankName;

    @ApiModelProperty(value = "结算账号", example = "6225888888888888")
    private String settleAccountNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2026-04-27 10:20:00")
    private Date updateTime;

    @ApiModelProperty("近期待提现记录")
    private List<ParkingWithdrawOrder> withdrawRecords;

    @ApiModelProperty("账户流水")
    private List<ParkingAccountFlow> flows;
}
