package com.huimao.parking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 入场事件
 */
@ApiModel("设备入场事件请求")
@Data
public class DeviceEntryEventRequest
{
    /**
     * 停车场 ID，由模拟设备或中台上报，用于隔离不同停车场数据。
     */
    @ApiModelProperty(value = "停车场ID", required = true, example = "1001")
    @NotNull(message = "停车场ID不能为空")
    private Long lotId;

    /**
     * 通道编码，要求在当前停车场内唯一。
     */
    @ApiModelProperty(value = "通道编码", required = true, example = "GATE-IN-01")
    @NotBlank(message = "通道编码不能为空")
    private String gateCode;

    /**
     * 设备编码，要求在停车模块内唯一。
     */
    @ApiModelProperty(value = "设备编码", required = true, example = "DEV-CAM-01")
    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    /**
     * 识别到的车牌号。
     */
    @ApiModelProperty(value = "车牌号", required = true, example = "粤B12345")
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    /**
     * 事件发生时间，不传时业务层通常会回退为当前时间。
     */
    @ApiModelProperty(value = "事件发生时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    /**
     * 抓拍图片地址。
     */
    @ApiModelProperty(value = "抓拍图片地址", example = "http://127.0.0.1/images/entry.jpg")
    private String imageUrl;

    /**
     * 设备原始报文，方便后续替换真实厂商协议。
     */
    @ApiModelProperty(value = "设备原始报文")
    private String rawContent;
}
