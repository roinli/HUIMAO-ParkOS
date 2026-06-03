package com.huimao.parking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 出场事件
 */
@ApiModel("设备出场事件请求")
@Data
public class DeviceExitEventRequest
{
    /**
     * 停车场 ID。
     */
    @ApiModelProperty(value = "停车场ID", required = true, example = "1001")
    @NotNull(message = "停车场ID不能为空")
    private Long lotId;

    /**
     * 出口通道编码。
     */
    @ApiModelProperty(value = "通道编码", required = true, example = "GATE-OUT-01")
    @NotBlank(message = "通道编码不能为空")
    private String gateCode;

    /**
     * 触发识别的设备编码。
     */
    @ApiModelProperty(value = "设备编码", required = true, example = "DEV-CAM-02")
    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    /**
     * 车辆车牌号。
     */
    @ApiModelProperty(value = "车牌号", required = true, example = "粤B12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    /**
     * 出场识别时间。
     */
    @ApiModelProperty(value = "事件发生时间", example = "2026-04-23 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    /**
     * 出场抓拍图片地址。
     */
    @ApiModelProperty(value = "出场抓拍图片地址")
    private String imageUrl;

    /**
     * 设备原始上报内容。
     */
    @ApiModelProperty(value = "设备原始上报内容")
    private String rawContent;
}
