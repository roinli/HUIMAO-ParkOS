package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huimao.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 停车通道
 */
@ApiModel("停车通道")
@Data
@TableName("parking_gate")
public class ParkingGate extends BaseEntity
{
    @TableId
    @ApiModelProperty(value = "通道ID", example = "3001")
    private Long gateId;

    @ApiModelProperty(value = "租户ID", example = "1")
    private Long tenantId;
    @ApiModelProperty(value = "停车场ID", example = "1001")
    private Long lotId;
    @ApiModelProperty(value = "通道编码", example = "GATE-IN-01")
    private String gateCode;
    @ApiModelProperty(value = "通道名称", example = "北门入口")
    private String gateName;

    /**
     * 通道类型，见 {@link com.huimao.parking.constant.ParkingConstants#GATE_TYPE_ENTRY}
     * 和 {@link com.huimao.parking.constant.ParkingConstants#GATE_TYPE_EXIT}。
     */
    @ApiModelProperty(value = "通道类型", example = "1", notes = "1 入口，2 出口")
    private String gateType;

    /**
     * 排序号，数值越小越靠前。
     */
    @ApiModelProperty(value = "排序号", example = "1")
    private Integer sortNum;

    /**
     * 通道状态，0 启用，1 停用。
     */
    @ApiModelProperty(value = "通道状态", example = "0", notes = "0 启用，1 停用")
    private String status;
    @ApiModelProperty(value = "删除标记", example = "0", notes = "0 正常，2 已删除")
    private String delFlag;
}
