package com.huimao.parking.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * H5 停车场公开信息视图
 */
@ApiModel("H5停车场公开信息视图")
@Data
public class ParkingH5LotVo
{
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;

    @ApiModelProperty(value = "停车场编码", example = "LOT-001")
    private String lotCode;

    @ApiModelProperty(value = "停车场名称", example = "总部园区停车场")
    private String lotName;

    @ApiModelProperty(value = "省名称", example = "广东省")
    private String provinceName;

    @ApiModelProperty(value = "市名称", example = "深圳市")
    private String cityName;

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

    @ApiModelProperty(value = "总车位数", example = "500")
    private Integer totalSpace;

    @ApiModelProperty(value = "临停车位数", example = "400")
    private Integer tempSpace;

    @ApiModelProperty(value = "月租车位数", example = "100")
    private Integer monthlySpace;

    @ApiModelProperty(value = "联系人", example = "张三")
    private String contactName;

    @ApiModelProperty(value = "联系电话", example = "13800000000")
    private String contactPhone;

    @ApiModelProperty(value = "缴费二维码内容")
    private String payQrContent;

    @ApiModelProperty(value = "缴费二维码图片地址")
    private String payQrUrl;
}
