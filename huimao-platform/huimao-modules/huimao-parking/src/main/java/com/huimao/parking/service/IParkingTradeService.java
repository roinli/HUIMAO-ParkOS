package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.domain.ParkingSession;
import com.huimao.parking.dto.DeviceEntryEventRequest;
import com.huimao.parking.dto.DeviceExitEventRequest;
import com.huimao.parking.dto.DeviceHeartbeatRequest;
import com.huimao.parking.dto.GateCommandCallbackRequest;
import com.huimao.parking.dto.ParkingH5PayRequest;
import com.huimao.parking.vo.DeviceProcessResult;
import com.huimao.parking.vo.ParkingH5LotVo;
import com.huimao.parking.vo.ParkingH5OrderVo;
import com.huimao.parking.vo.ParkingH5PaymentRecordVo;
import com.huimao.parking.vo.ParkingSessionDetailVo;

import java.util.List;

/**
 * 停车交易链路服务。
 *
 * <p>覆盖设备入场、出场、心跳、H5 缴费和停车记录查询等核心流程。</p>
 */
public interface IParkingTradeService
{
    /**
     * 处理入口设备上报的入场事件。
     */
    AjaxResult handleEntry(DeviceEntryEventRequest request);

    /**
     * 处理设备心跳，主要用于刷新在线状态。
     */
    AjaxResult handleHeartbeat(DeviceHeartbeatRequest request);

    /**
     * 处理设备执行开闸指令后的回调。
     */
    AjaxResult handleCommandCallback(GateCommandCallbackRequest request);

    /**
     * 处理出口识别事件，并返回是否允许开闸。
     */
    DeviceProcessResult handleExit(DeviceExitEventRequest request);

    /**
     * 按停车场编码查询 H5 停车场公开信息。
     */
    ParkingH5LotVo queryLotByCode(String lotCode);

    /**
     * 按停车场和车牌查询当前待缴费订单。
     */
    ParkingH5OrderVo queryPendingOrder(Long lotId, String plateNo);

    /**
     * 按车牌查询当前待缴费订单。
     */
    ParkingH5OrderVo queryPendingOrder(String plateNo);

    /**
     * 按车牌查询 H5 缴费记录。
     */
    List<ParkingH5PaymentRecordVo> queryPaymentRecords(String plateNo);

    /**
     * H5 分页查询停车记录。
     */
    IPage<ParkingSession> queryH5SessionPage(ParkingSession query);

    /**
     * 发起模拟缴费。
     */
    AjaxResult payOrder(String orderNo, ParkingH5PayRequest request);

    /**
     * 分页查询停车会话。
     */
    IPage<ParkingSession> selectSessionPage(ParkingSession query);

    /**
     * 查询停车会话详情，包括订单、支付、开闸记录。
     */
    ParkingSessionDetailVo selectSessionDetail(Long sessionId);
}
