package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 账户流水
 */
@ApiModel("账户流水")
@Data
@TableName("parking_account_flow")
public class ParkingAccountFlow
{
    @TableId
    @ApiModelProperty(value = "流水ID", example = "1")
    private Long flowId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "账户ID", example = "9001")
    private Long accountId;

    @ApiModelProperty(value = "业务类型", example = "WITHDRAW_FREEZE")
    private String bizType;

    @ApiModelProperty(value = "业务单号", example = "WD202604270001")
    private String bizNo;

    @ApiModelProperty(value = "变动方向", example = "OUT")
    private String changeType;

    @ApiModelProperty(value = "变动金额", example = "5000.00")
    private BigDecimal changeAmount;

    @ApiModelProperty(value = "变动前余额", example = "89174.16")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "变动后余额", example = "84174.16")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者", example = "admin")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间", example = "2026-04-27 10:20:00")
    private Date createTime;

    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
