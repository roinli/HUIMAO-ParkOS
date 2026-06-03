package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 停车设备
 */
@ApiModel("停车设备")
@Data
@TableName("parking_device")
public class ParkingDevice extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "设备ID", example = "4001")
    private Long deviceId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "通道ID", example = "3001")
    private Long gateId;
    @ApiModelProperty(value = "设备编码", example = "DEV-CAM-01")
    private String deviceCode;
    @ApiModelProperty(value = "设备名称", example = "北门入口相机")
    private String deviceName;

    /**
     * 设备类型，当前约定可取 camera、barrier、allinone。
     */
    @ApiModelProperty(value = "设备类型", example = "camera", notes = "当前约定可取 camera、barrier、allinone")
    private String deviceType;

    /**
     * 厂商编码，首版默认 SIM，表示模拟设备。
     */
    @ApiModelProperty(value = "厂商编码", example = "SIM", notes = "首版默认 SIM，表示模拟设备")
    private String vendorCode;

    /**
     * 接入方式，首版默认 SIMULATED。
     */
    @ApiModelProperty(value = "接入方式", example = "SIMULATED")
    private String connectMode;
    @ApiModelProperty(value = "设备密钥")
    private String deviceSecret;

    /**
     * 设备状态。
     * 0 在线/启用
     * 1 离线
     * 2 停用
     */
    @ApiModelProperty(value = "设备状态", example = "0", notes = "0 在线/启用，1 离线，2 停用")
    private String status;

    @ApiModelProperty(value = "最后心跳时间", example = "2026-04-23 12:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeatTime;

    /**
     * 预留给设备厂商参数、鉴权参数、回调地址等配置。
     */
    @ApiModelProperty(value = "设备配置JSON")
    private String configJson;
    @ApiModelProperty(value = "删除标记", example = "0", notes = "0 正常，2 已删除")
    private String delFlag;
}
