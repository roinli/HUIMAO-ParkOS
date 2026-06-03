package com.huimao.parking.domain;

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
 * 停车日统计
 */
@ApiModel("停车日统计")
@Data
@TableName("parking_daily_stat")
public class ParkingDailyStat
{
    @TableId
    @ApiModelProperty(value = "统计ID", example = "1")
    private Long statId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "统计日期", example = "2026-04-27")
    private Date statDate;

    @ApiModelProperty(value = "进场数", example = "120")
    private Integer entryCount;

    @ApiModelProperty(value = "出场数", example = "110")
    private Integer exitCount;

    @ApiModelProperty(value = "在场数", example = "45")
    private Integer inParkCount;

    @ApiModelProperty(value = "订单数", example = "96")
    private Integer orderCount;

    @ApiModelProperty(value = "已支付订单数", example = "92")
    private Integer paidOrderCount;

    @ApiModelProperty(value = "收入金额", example = "2680.00")
    private BigDecimal incomeAmount;

    @ApiModelProperty(value = "平均每单", example = "27.92")
    private BigDecimal avgOrderAmount;

    @ApiModelProperty(value = "周转率", example = "2.40")
    private BigDecimal turnoverRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间", example = "2026-04-27 00:10:00")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2026-04-27 00:15:00")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
