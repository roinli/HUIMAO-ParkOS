package com.huimao.parking.simulator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huimao.common.core.web.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 停车设备轻量对象。
 */
@Data
@TableName("parking_device")
public class ParkingDeviceLite extends BaseEntity
{
    @TableId
    private Long deviceId;

    private Long tenantId;
    private Long lotId;
    private Long gateId;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String vendorCode;
    private String connectMode;
    private String deviceSecret;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeatTime;

    private String configJson;
    private String delFlag;
}
