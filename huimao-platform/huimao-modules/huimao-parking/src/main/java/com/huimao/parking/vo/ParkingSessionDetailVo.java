package com.huimao.parking.vo;

import com.huimao.parking.domain.ParkingGateCommand;
import com.huimao.parking.domain.ParkingOrder;
import com.huimao.parking.domain.ParkingPayment;
import com.huimao.parking.domain.ParkingSession;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 停车记录详情
 */
@ApiModel("停车记录详情视图")
@Data
public class ParkingSessionDetailVo
{
    /**
     * 停车会话主信息。
     */
    @ApiModelProperty(value = "停车会话主信息")
    private ParkingSession session;

    /**
     * 会话关联的停车订单列表，按时间正序返回。
     */
    @ApiModelProperty(value = "关联订单列表")
    private List<ParkingOrder> orders;

    /**
     * 会话关联的支付流水列表。
     */
    @ApiModelProperty(value = "关联支付流水列表")
    private List<ParkingPayment> payments;

    /**
     * 会话触发过的开闸指令记录。
     */
    @ApiModelProperty(value = "关联开闸指令列表")
    private List<ParkingGateCommand> commands;
}
