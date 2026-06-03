package com.huimao.parking.simulator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 模拟车牌事件请求。
 */
@Data
@ApiModel("模拟车牌事件请求")
public class SimulatorPlateEventRequest
{
    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;

    @ApiModelProperty(value = "事件时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    @ApiModelProperty(value = "抓拍图片地址")
    private String imageUrl;

    @ApiModelProperty(value = "设备原始报文")
    private String rawContent;
}
