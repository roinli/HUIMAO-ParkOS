package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租户结算配置
 */
@ApiModel("租户结算配置")
@Data
@TableName("parking_tenant_settle_config")
public class ParkingTenantSettleConfig extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "配置ID", example = "1")
    private Long configId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "商户号", example = "M202604270001")
    private String merchantNo;

    @ApiModelProperty(value = "商户名称", example = "维途停车")
    private String merchantName;

    @ApiModelProperty(value = "提现账户名", example = "维途停车运营账户")
    private String withdrawAccountName;

    @ApiModelProperty(value = "提现账号", example = "6225888888888888")
    private String withdrawAccountNo;

    @ApiModelProperty(value = "开户行", example = "招商银行深圳科技园支行")
    private String withdrawBankName;

    @ApiModelProperty(value = "手续费率", example = "0.0060")
    private BigDecimal feeRate;

    @ApiModelProperty(value = "最小提现金额", example = "100.00")
    private BigDecimal minWithdrawAmount;

    @ApiModelProperty(value = "状态", example = "0")
    private String status;
}
