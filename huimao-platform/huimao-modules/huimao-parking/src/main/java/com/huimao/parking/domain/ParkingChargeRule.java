package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收费规则
 */
@ApiModel("停车收费规则")
@Data
@TableName("parking_charge_rule")
public class ParkingChargeRule extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "规则ID", example = "2001")
    private Long ruleId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "是否默认规则", example = "1", notes = "0 否，1 是")
    private String defaultFlag;
    @ApiModelProperty(value = "规则名称", example = "临停标准计费")
    private String ruleName;

    /**
     * 免费时长，单位分钟。
     */
    @ApiModelProperty(value = "免费时长(分钟)", example = "15")
    private Integer freeMinutes;

    /**
     * 计费单位，单位分钟。
     */
    @ApiModelProperty(value = "计费单位(分钟)", example = "60")
    private Integer unitMinutes;

    /**
     * 每个计费单位对应单价。
     */
    @ApiModelProperty(value = "每个计费单位单价", example = "5.00")
    private BigDecimal unitPrice;

    /**
     * 后续计费单位单价。
     */
    @ApiModelProperty(value = "后续计费单位单价", example = "2.00")
    private BigDecimal followUnitPrice;

    /**
     * 24 小时封顶金额。
     */
    @ApiModelProperty(value = "24小时封顶金额", example = "30.00")
    private BigDecimal dayCapAmount;

    /**
     * 支付成功后的离场宽限期，单位分钟。
     */
    @ApiModelProperty(value = "支付后离场宽限期(分钟)", example = "15")
    private Integer payGraceMinutes;

    /**
     * 超时是否需要补费，0 否，1 是。
     */
    @ApiModelProperty(value = "超时是否补费", example = "1", notes = "0 否，1 是")
    private String overtimeRecalc;

    /**
     * 规则状态，0 启用，1 停用。
     */
    @ApiModelProperty(value = "规则状态", example = "0", notes = "0 启用，1 停用")
    private String status;

    @ApiModelProperty(value = "生效时间", example = "2026-04-23 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    @ApiModelProperty(value = "失效时间", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @ApiModelProperty(value = "删除标记", example = "0", notes = "0 正常，2 已删除")
    private String delFlag;
}
