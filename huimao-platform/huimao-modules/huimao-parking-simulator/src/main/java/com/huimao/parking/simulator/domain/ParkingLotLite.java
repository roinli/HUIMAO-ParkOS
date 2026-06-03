package com.huimao.parking.simulator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.web.domain.BaseEntity;
import lombok.Data;

/**
 * 停车场轻量对象。
 */
@Data
@TableName("parking_lot")
public class ParkingLotLite extends BaseEntity
{
    @TableId
    private Long lotId;

    private Long tenantId;
    private String lotCode;
    private String lotName;
    private String status;
    private String delFlag;
}
