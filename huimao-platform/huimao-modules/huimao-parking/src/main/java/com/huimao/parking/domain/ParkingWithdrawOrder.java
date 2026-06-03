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
 * 提现申请单
 */
@ApiModel("提现申请单")
@Data
@TableName("parking_withdraw_order")
public class ParkingWithdrawOrder
{
    @TableId
    @ApiModelProperty(value = "提现单ID", example = "1")
    private Long withdrawId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "提现单号", example = "WD202604270001")
    private String withdrawNo;

    @ApiModelProperty(value = "申请金额", example = "3000.00")
    private BigDecimal applyAmount;

    @ApiModelProperty(value = "手续费", example = "18.00")
    private BigDecimal feeAmount;

    @ApiModelProperty(value = "实到账金额", example = "2982.00")
    private BigDecimal realAmount;

    @ApiModelProperty(value = "手续费率", example = "0.0060")
    private BigDecimal feeRate;

    @ApiModelProperty(value = "状态", example = "WAIT_AUDIT")
    private String status;

    @ApiModelProperty(value = "审核人", example = "admin")
    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间", example = "2026-04-27 11:20:00")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "打款时间", example = "2026-04-27 12:00:00")
    private Date payTime;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者", example = "admin")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间", example = "2026-04-27 10:20:00")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新者", example = "admin")
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间", example = "2026-04-27 11:00:00")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "租户名称", example = "演示租户")
    private String tenantName;

    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
