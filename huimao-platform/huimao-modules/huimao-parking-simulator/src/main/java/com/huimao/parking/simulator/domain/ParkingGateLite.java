package com.huimao.parking.simulator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.web.domain.BaseEntity;
import lombok.Data;

/**
 * 通道轻量对象。
 */
@Data
@TableName("parking_gate")
public class ParkingGateLite extends BaseEntity
{
    @TableId
    private Long gateId;

    private Long tenantId;
    private Long lotId;
    private String gateCode;
    private String gateName;
    private String gateType;
    private Integer sortNum;
    private String status;
    private String delFlag;
}
