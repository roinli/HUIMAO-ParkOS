package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 设备事件
 */
@ApiModel("设备事件日志")
@Data
@TableName("parking_io_event")
public class ParkingIoEvent
{
    @TableId
    @ApiModelProperty(value = "事件ID", example = "9001")
    private Long eventId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "通道ID", example = "3001")
    private Long gateId;
    @ApiModelProperty(value = "设备ID", example = "4001")
    private Long deviceId;
    @ApiModelProperty(value = "事件流水号", example = "PE20260423123000123456")
    private String eventNo;

    /**
     * 事件类型，见 ParkingConstants 中 EVENT_TYPE_* 常量。
     */
    @ApiModelProperty(value = "事件类型", example = "ENTRY", notes = "见 ParkingConstants.EVENT_TYPE_*")
    private String eventType;
    @ApiModelProperty(value = "车牌号", example = "粤B12345")
    private String plateNo;

    @ApiModelProperty(value = "事件时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    @ApiModelProperty(value = "图片地址")
    private String imageUrl;

    /**
     * 保存设备原始上报内容，便于联调和问题追踪。
     */
    @ApiModelProperty(value = "原始报文")
    private String rawContent;

    /**
     * 事件处理状态。
     * 当前实现里常见值有 INIT、SUCCESS、FAIL、WAIT_PAY。
     */
    @ApiModelProperty(value = "处理状态", example = "SUCCESS", notes = "常见值 INIT、SUCCESS、FAIL、WAIT_PAY")
    private String handleStatus;

    /**
     * 事件处理结果说明。
     */
    @ApiModelProperty(value = "处理结果说明", example = "入场处理成功")
    private String handleMsg;

    @ApiModelProperty(value = "创建时间", example = "2026-04-23 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-04-23 12:30:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
