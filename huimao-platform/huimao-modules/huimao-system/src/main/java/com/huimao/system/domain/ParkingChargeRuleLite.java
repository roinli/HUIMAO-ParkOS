package com.huimao.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 系统模块内部使用的停车收费规则轻量实体。
 *
 * <p>仅用于租户创建时初始化停车默认收费规则，避免 system 模块直接依赖 parking 模块的 Java 类型。</p>
 */
@Data
@TableName("parking_charge_rule")
public class ParkingChargeRuleLite extends BaseEntity
{
    @TableId
    private Long ruleId;

    private Long tenantId;

    private String defaultFlag;

    private String ruleName;

    private Integer freeMinutes;

    private Integer unitMinutes;

    private BigDecimal unitPrice;

    private BigDecimal followUnitPrice;

    private BigDecimal dayCapAmount;

    private Integer payGraceMinutes;

    private String overtimeRecalc;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
}
