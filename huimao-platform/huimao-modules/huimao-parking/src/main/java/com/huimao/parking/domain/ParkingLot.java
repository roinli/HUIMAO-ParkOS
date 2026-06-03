package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.annotation.Excel;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车场主表
 */
@ApiModel("停车场")
@Data
@TableName("parking_lot")
public class ParkingLot extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @Excel(name = "租户ID")
    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;

    @Excel(name = "停车场编码")
    @ApiModelProperty(value = "停车场编码", example = "LOT-001")
    private String lotCode;

    @Excel(name = "停车场名称")
    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    /**
     * 省市区编码和名称，主要用于后续地图、区域筛选和统计。
     */
    @ApiModelProperty(value = "省编码", example = "440000")
    private String provinceCode;
    @ApiModelProperty(value = "省名称", example = "广东省")
    private String provinceName;
    @ApiModelProperty(value = "市编码", example = "440300")
    private String cityCode;
    @ApiModelProperty(value = "市名称", example = "深圳市")
    private String cityName;
    @ApiModelProperty(value = "区编码", example = "440305")
    private String areaCode;
    @ApiModelProperty(value = "区名称", example = "南山区")
    private String areaName;
    @ApiModelProperty(value = "详细地址", example = "科技园中区1号")
    private String address;
    @ApiModelProperty(value = "经度", example = "113.944")
    private BigDecimal lng;
    @ApiModelProperty(value = "纬度", example = "22.548")
    private BigDecimal lat;
    @ApiModelProperty(value = "停车场图片地址")
    private String fileUrl;
    @ApiModelProperty(value = "车位类型", example = "1", notes = "1 地面，2 地下，3 占道，4 地上/地下")
    private String spaceType;

    /**
     * 总车位数。
     */
    @ApiModelProperty(value = "总车位数", example = "500")
    private Integer totalSpace;

    /**
     * 临停车位数。
     */
    @ApiModelProperty(value = "临停车位数", example = "400")
    private Integer tempSpace;

    /**
     * 月租/固定车位数。
     */
    @ApiModelProperty(value = "月租车位数", example = "100")
    private Integer monthlySpace;
    @ApiModelProperty(value = "联系人", example = "张三")
    private String contactName;
    @ApiModelProperty(value = "联系电话", example = "13800000000")
    private String contactPhone;

    /**
     * 停车场状态，0 运营中，1 停用。
     */
    @ApiModelProperty(value = "停车场状态", example = "0", notes = "0 运营中，1 停用")
    private String status;

    /**
     * 默认收费规则 ID。
     */
    @ApiModelProperty(value = "默认收费规则ID", example = "2001")
    private Long chargeRuleId;

    /**
     * 缴费二维码内容，可以直接存二维码原文或支付链接。
     */
    @ApiModelProperty(value = "缴费二维码内容")
    private String payQrContent;

    /**
     * 缴费二维码图片地址，供前端直接展示。
     */
    @ApiModelProperty(value = "缴费二维码图片地址")
    private String payQrUrl;
    @ApiModelProperty(value = "删除标记", example = "0", notes = "0 正常，2 已删除")
    private String delFlag;
}
