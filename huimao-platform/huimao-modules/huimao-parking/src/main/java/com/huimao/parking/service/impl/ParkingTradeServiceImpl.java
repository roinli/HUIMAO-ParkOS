package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.DateUtils;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.*;
import com.huimao.parking.dto.DeviceEntryEventRequest;
import com.huimao.parking.dto.DeviceExitEventRequest;
import com.huimao.parking.dto.DeviceHeartbeatRequest;
import com.huimao.parking.dto.GateCommandCallbackRequest;
import com.huimao.parking.dto.ParkingH5PayRequest;
import com.huimao.parking.mapper.*;
import com.huimao.parking.service.IParkingTradeService;
import com.huimao.parking.util.ParkingFeeCalculator;
import com.huimao.parking.util.ParkingNoUtils;
import com.huimao.parking.vo.DeviceProcessResult;
import com.huimao.parking.vo.ParkingH5LotVo;
import com.huimao.parking.vo.ParkingH5OrderVo;
import com.huimao.parking.vo.ParkingH5PaymentRecordVo;
import com.huimao.parking.vo.ParkingSessionDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 停车交易主流程实现。
 *
 * <p>首版以“模拟设备 + 模拟支付”为目标，先把停车链路跑通：
 * 入场建会话，出场试算，待支付则生成/更新订单，支付成功后进入离场宽限期，
 * 宽限期内出口识别直接放行。</p>
 */
@Service
public class ParkingTradeServiceImpl extends AbstractParkingTenantService implements IParkingTradeService
{
    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private ParkingGateMapper parkingGateMapper;

    @Autowired
    private ParkingDeviceMapper parkingDeviceMapper;

    @Autowired
    private ParkingChargeRuleMapper parkingChargeRuleMapper;

    @Autowired
    private ParkingSessionMapper parkingSessionMapper;

    @Autowired
    private ParkingOrderMapper parkingOrderMapper;

    @Autowired
    private ParkingPaymentMapper parkingPaymentMapper;

    @Autowired
    private ParkingAccountMapper parkingAccountMapper;

    @Autowired
    private ParkingAccountFlowMapper parkingAccountFlowMapper;

    @Autowired
    private ParkingIoEventMapper parkingIoEventMapper;

    @Autowired
    private ParkingGateCommandMapper parkingGateCommandMapper;

    @Autowired
    private SysTenantLiteMapper sysTenantLiteMapper;

    /**
     * 按停车场编码查询 H5 停车场公开信息。
     */
    @Override
    public ParkingH5LotVo queryLotByCode(String lotCode)
    {
        if (StringUtils.isEmpty(lotCode))
        {
            throw new ServiceException("停车场编码不能为空");
        }
        ParkingLot lot = parkingLotMapper.selectOne(Wrappers.<ParkingLot>lambdaQuery()
                .eq(ParkingLot::getLotCode, lotCode)
                .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .eq(ParkingLot::getStatus, ParkingConstants.LOT_STATUS_ENABLED)
                .last("limit 1"));
        if (lot == null)
        {
            throw new ServiceException("停车场不存在或已停用");
        }
        ParkingH5LotVo vo = new ParkingH5LotVo();
        vo.setLotId(lot.getLotId());
        vo.setLotCode(lot.getLotCode());
        vo.setLotName(lot.getLotName());
        vo.setProvinceName(lot.getProvinceName());
        vo.setCityName(lot.getCityName());
        vo.setAreaName(lot.getAreaName());
        vo.setAddress(lot.getAddress());
        vo.setLng(lot.getLng());
        vo.setLat(lot.getLat());
        vo.setFileUrl(lot.getFileUrl());
        vo.setTotalSpace(lot.getTotalSpace());
        vo.setTempSpace(lot.getTempSpace());
        vo.setMonthlySpace(lot.getMonthlySpace());
        vo.setContactName(lot.getContactName());
        vo.setContactPhone(lot.getContactPhone());
        vo.setPayQrContent(lot.getPayQrContent());
        vo.setPayQrUrl(lot.getPayQrUrl());
        return vo;
    }

    /**
     * 处理入场事件。
     *
     * <p>步骤：
     * 1. 校验入口通道和设备。
     * 2. 记录一条入口事件日志。
     * 3. 校验车辆是否已经存在未完成会话。
     * 4. 创建停车会话和首笔订单。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult handleEntry(DeviceEntryEventRequest request)
    {
        ParkingGate gate = getGate(request.getLotId(), request.getGateCode(), ParkingConstants.GATE_TYPE_ENTRY);
        ParkingDevice device = getDevice(gate.getGateId(), request.getDeviceCode());
        Date eventTime = request.getEventTime() == null ? DateUtils.getNowDate() : request.getEventTime();
        ParkingIoEvent event = buildIoEvent(gate.getTenantId(), request.getLotId(), gate.getGateId(), device.getDeviceId(),
                ParkingConstants.EVENT_TYPE_ENTRY, request.getPlateNo(), eventTime, request.getImageUrl(), request.getRawContent());
        parkingIoEventMapper.insert(event);

        // 同一停车场同一车牌同时只允许存在一条未完成停车会话。
        ParkingSession activeSession = findActiveSession(request.getLotId(), request.getPlateNo());
        if (activeSession != null)
        {
            markEvent(event, "FAIL", "车辆已存在未完成停车会话");
            return AjaxResult.error("车辆已在场内，无需重复入场");
        }

        ParkingSession session = new ParkingSession();
        session.setTenantId(gate.getTenantId());
        session.setLotId(request.getLotId());
        session.setPlateNo(request.getPlateNo());
        session.setEntryGateId(gate.getGateId());
        session.setEntryDeviceId(device.getDeviceId());
        session.setEntryTime(eventTime);
        session.setEntryImageUrl(request.getImageUrl());
        session.setStatus(ParkingConstants.SESSION_STATUS_IN_PARK);
        session.setParkingMinutes(0);
        session.setTotalPaidAmount(BigDecimal.ZERO);
        session.setTotalPayableAmount(BigDecimal.ZERO);
        session.setIsAbnormal("0");
        parkingSessionMapper.insert(session);

        // 入场即创建首笔订单，后续出场或 H5 查询时只需要补齐试算金额即可。
        ParkingOrder order = new ParkingOrder();
        order.setTenantId(session.getTenantId());
        order.setLotId(session.getLotId());
        order.setSessionId(session.getSessionId());
        order.setOrderNo(ParkingNoUtils.orderNo());
        order.setOrderType(ParkingConstants.ORDER_TYPE_NORMAL);
        order.setPlateNo(session.getPlateNo());
        order.setEntryTime(session.getEntryTime());
        order.setCalcTime(session.getEntryTime());
        order.setParkingMinutes(0);
        order.setPayableAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setOrderStatus(ParkingConstants.ORDER_STATUS_UNPAID);
        order.setPayStatus(ParkingConstants.PAY_STATUS_UNPAID);
        parkingOrderMapper.insert(order);

        session.setCurrentOrderNo(order.getOrderNo());
        parkingSessionMapper.updateById(session);
        markEvent(event, "SUCCESS", "入场处理成功");
        return AjaxResult.success("入场成功", session.getSessionId());
    }

    /**
     * 处理设备心跳。
     *
     * <p>当前逻辑只做两件事：刷新最后心跳时间，并把设备状态置为在线。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult handleHeartbeat(DeviceHeartbeatRequest request)
    {
        ParkingDevice device = parkingDeviceMapper.selectOne(Wrappers.<ParkingDevice>lambdaQuery()
                .eq(ParkingDevice::getDeviceCode, request.getDeviceCode())
                .eq(ParkingDevice::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .last("limit 1"));
        if (device == null)
        {
            return AjaxResult.error("设备不存在");
        }
        ParkingDevice update = new ParkingDevice();
        update.setDeviceId(device.getDeviceId());
        update.setLastHeartbeatTime(request.getHeartbeatTime() == null ? DateUtils.getNowDate() : request.getHeartbeatTime());
        update.setStatus(ParkingConstants.COMMON_STATUS_ENABLED);
        parkingDeviceMapper.updateById(update);

        ParkingIoEvent event = buildIoEvent(device.getTenantId(), device.getLotId(), device.getGateId(), device.getDeviceId(),
                ParkingConstants.EVENT_TYPE_HEARTBEAT, null, update.getLastHeartbeatTime(), null, request.getRawContent());
        event.setHandleStatus("SUCCESS");
        event.setHandleMsg("心跳成功");
        parkingIoEventMapper.insert(event);
        return AjaxResult.success("心跳成功");
    }

    /**
     * 处理开闸指令回调。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult handleCommandCallback(GateCommandCallbackRequest request)
    {
        ParkingGateCommand command = parkingGateCommandMapper.selectOne(Wrappers.<ParkingGateCommand>lambdaQuery()
                .eq(ParkingGateCommand::getCommandNo, request.getCommandNo())
                .last("limit 1"));
        if (command == null)
        {
            return AjaxResult.error("指令不存在");
        }
        command.setCommandStatus(Boolean.TRUE.equals(request.getSuccess()) ? ParkingConstants.COMMAND_STATUS_SUCCESS : ParkingConstants.COMMAND_STATUS_FAIL);
        command.setCallbackTime(request.getCallbackTime() == null ? DateUtils.getNowDate() : request.getCallbackTime());
        command.setCallbackContent(request.getRawContent());
        parkingGateCommandMapper.updateById(command);

        ParkingIoEvent event = buildIoEvent(command.getTenantId(), command.getLotId(), command.getGateId(), command.getDeviceId(),
                ParkingConstants.EVENT_TYPE_OPEN_RESULT, null, command.getCallbackTime(), null, request.getRawContent());
        event.setHandleStatus(command.getCommandStatus());
        event.setHandleMsg("开闸回调处理完成");
        parkingIoEventMapper.insert(event);
        return AjaxResult.success("回调成功");
    }

    /**
     * 处理出口事件。
     *
     * <p>优先判断是否处于已支付宽限期；若不是，则重新试算应付金额。
     * 0 元订单自动放行，非 0 元订单则进入待支付状态。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceProcessResult handleExit(DeviceExitEventRequest request)
    {
        ParkingGate gate = getGate(request.getLotId(), request.getGateCode(), ParkingConstants.GATE_TYPE_EXIT);
        ParkingDevice device = getDevice(gate.getGateId(), request.getDeviceCode());
        Date eventTime = request.getEventTime() == null ? DateUtils.getNowDate() : request.getEventTime();
        ParkingIoEvent event = buildIoEvent(gate.getTenantId(), request.getLotId(), gate.getGateId(), device.getDeviceId(),
                ParkingConstants.EVENT_TYPE_EXIT, request.getPlateNo(), eventTime, request.getImageUrl(), request.getRawContent());
        parkingIoEventMapper.insert(event);

        DeviceProcessResult result = new DeviceProcessResult();
        ParkingSession session = findActiveSession(request.getLotId(), request.getPlateNo());
        if (session == null)
        {
            markEvent(event, "FAIL", "未找到在场车辆");
            result.setAllowOpen(false);
            result.setMessage("未找到在场车辆");
            return result;
        }

        // 已支付且仍在离场宽限期内，不再重新计费，直接下发模拟开闸。
        if (ParkingConstants.SESSION_STATUS_PAID_PENDING_EXIT.equals(session.getStatus())
                && session.getLeaveDeadlineTime() != null
                && !eventTime.after(session.getLeaveDeadlineTime()))
        {
            ParkingOrder paidOrder = findCurrentOrder(session);
            ParkingGateCommand command = createOpenGateCommand(session, gate, device, paidOrder, true, "支付有效期内离场");
            completeSession(session, gate, device, eventTime, request.getImageUrl());
            markEvent(event, "SUCCESS", "支付后离场放行");
            result.setAllowOpen(true);
            result.setMessage("允许离场");
            result.setCommandNo(command.getCommandNo());
            result.setOrderNo(paidOrder == null ? null : paidOrder.getOrderNo());
            return result;
        }

        // 宽限期外统一重新试算，可能是首缴，也可能是超时补费。
        ParkingChargeRule rule = getEffectiveRule(session.getLotId());
        if (ParkingConstants.SESSION_STATUS_PAID_PENDING_EXIT.equals(session.getStatus()))
        {
            if (!"1".equals(rule.getOvertimeRecalc()))
            {
                ParkingOrder paidOrder = findCurrentOrder(session);
                ParkingGateCommand command = createOpenGateCommand(session, gate, device, paidOrder, true, "超时未开启补费，允许离场");
                completeSession(session, gate, device, eventTime, request.getImageUrl());
                markEvent(event, "SUCCESS", "超时未开启补费，允许离场");
                result.setAllowOpen(true);
                result.setMessage("超时未开启补费，允许离场");
                result.setCommandNo(command.getCommandNo());
                result.setOrderNo(paidOrder.getOrderNo());
                return result;
            }

            BigDecimal overtimeAmount = calcOvertimeAmount(session, rule, eventTime);
            if (overtimeAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                ParkingOrder paidOrder = findCurrentOrder(session);
                ParkingGateCommand command = createOpenGateCommand(session, gate, device, paidOrder, true, "已超时但未产生补费，允许离场");
                completeSession(session, gate, device, eventTime, request.getImageUrl());
                markEvent(event, "SUCCESS", "已超时但未产生补费，允许离场");
                result.setAllowOpen(true);
                result.setMessage("已超时但未产生补费，允许离场");
                result.setCommandNo(command.getCommandNo());
                result.setOrderNo(paidOrder.getOrderNo());
                result.setPayableAmount(BigDecimal.ZERO);
                return result;
            }
            ParkingOrder order = prepareOvertimePayableOrder(session, eventTime, rule);
            markSessionWaitPay(session, order);
            markEvent(event, "WAIT_PAY", "车辆超时待补费后离场");
            result.setAllowOpen(false);
            result.setMessage("车辆超时待补费后离场");
            result.setOrderNo(order.getOrderNo());
            result.setPayableAmount(order.getPayableAmount());
            return result;
        }
        ParkingOrder order = preparePayableOrder(session, eventTime, rule);
        if (order.getPayableAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            markOrderPaid(order, "FREE", eventTime);
            ParkingGateCommand command = createOpenGateCommand(session, gate, device, order, true, "零金额自动放行");
            completeSession(session, gate, device, eventTime, request.getImageUrl());
            markEvent(event, "SUCCESS", "零金额离场成功");
            result.setAllowOpen(true);
            result.setMessage("零金额自动放行");
            result.setCommandNo(command.getCommandNo());
            result.setOrderNo(order.getOrderNo());
            return result;
        }

        // 非零金额不允许直接放行，等待 H5 或其他渠道支付成功。
        markSessionWaitPay(session, order);
        markEvent(event, "WAIT_PAY", "车辆待支付后离场");
        result.setAllowOpen(false);
        result.setMessage("待支付后离场");
        result.setOrderNo(order.getOrderNo());
        result.setPayableAmount(order.getPayableAmount());
        return result;
    }

    /**
     * 查询当前待处理订单。
     *
     * <p>如果车辆尚未支付，则会实时重算当前金额，保证 H5 展示与当前停车时长一致。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingH5OrderVo queryPendingOrder(Long lotId, String plateNo)
    {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null || ParkingConstants.DEL_FLAG_DELETED.equals(lot.getDelFlag()))
        {
            throw new ServiceException("停车场不存在");
        }
        ParkingSession session = findActiveSession(lotId, plateNo);
        if (session == null)
        {
            throw new ServiceException("未查询到待处理订单");
        }

        ParkingOrder order = refreshH5PendingOrder(session, DateUtils.getNowDate());
        return buildH5OrderVo(lot, session, order);
    }

    /**
     * 按车牌查询当前待处理订单。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingH5OrderVo queryPendingOrder(String plateNo)
    {
        ParkingSession session = findActiveSession(plateNo);
        if (session == null)
        {
            throw new ServiceException("未查询到待处理订单");
        }
        ParkingLot lot = parkingLotMapper.selectById(session.getLotId());
        if (lot == null || ParkingConstants.DEL_FLAG_DELETED.equals(lot.getDelFlag()))
        {
            throw new ServiceException("停车场不存在");
        }

        ParkingOrder order = refreshH5PendingOrder(session, DateUtils.getNowDate());
        return buildH5OrderVo(lot, session, order);
    }

    /**
     * 按车牌查询 H5 缴费记录。
     */
    @Override
    public List<ParkingH5PaymentRecordVo> queryPaymentRecords(String plateNo)
    {
        if (StringUtils.isEmpty(plateNo))
        {
            throw new ServiceException("车牌号不能为空");
        }
        List<ParkingOrder> orders = parkingOrderMapper.selectList(Wrappers.<ParkingOrder>lambdaQuery()
                .eq(ParkingOrder::getPlateNo, plateNo)
                .orderByDesc(ParkingOrder::getCreateTime)
                .last("limit 50"));
        if (CollectionUtils.isEmpty(orders))
        {
            return Collections.emptyList();
        }

        Set<Long> orderIds = orders.stream().map(ParkingOrder::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(orderIds))
        {
            return Collections.emptyList();
        }

        List<ParkingPayment> payments = parkingPaymentMapper.selectList(Wrappers.<ParkingPayment>lambdaQuery()
                .in(ParkingPayment::getOrderId, orderIds)
                .eq(ParkingPayment::getPayStatus, ParkingConstants.PAY_STATUS_SUCCESS)
                .orderByAsc(ParkingPayment::getPaySuccessTime)
                .last("limit 50"));
        if (CollectionUtils.isEmpty(payments))
        {
            return Collections.emptyList();
        }

        Set<Long> lotIds = orders.stream().map(ParkingOrder::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ParkingOrder> orderMap = orders.stream()
                .filter(item -> item.getOrderId() != null)
                .collect(Collectors.toMap(ParkingOrder::getOrderId, item -> item, (a, b) -> a));
        Map<Long, String> lotNameMap = CollectionUtils.isEmpty(lotIds) ? Collections.emptyMap() : parkingLotMapper.selectList(
                        Wrappers.<ParkingLot>lambdaQuery().in(ParkingLot::getLotId, lotIds))
                .stream().collect(Collectors.toMap(ParkingLot::getLotId, ParkingLot::getLotName, (a, b) -> a));

        Map<Long, Date> lastPayTimeMap = new HashMap<>();
        List<ParkingH5PaymentRecordVo> records = payments.stream().map(payment -> {
            ParkingOrder order = orderMap.get(payment.getOrderId());
            ParkingH5PaymentRecordVo vo = new ParkingH5PaymentRecordVo();
            if (order == null)
            {
                vo.setLotId(payment.getLotId());
                vo.setLotName(lotNameMap.get(payment.getLotId()));
                vo.setOrderNo(payment.getOrderNo());
                vo.setPaymentNo(payment.getPaymentNo());
                vo.setPayChannel(payment.getPayChannel());
                vo.setPayAmount(payment.getPayAmount());
                vo.setPayStatus(payment.getPayStatus());
                vo.setPaySuccessTime(payment.getPaySuccessTime());
                return vo;
            }
            Date segmentEntryTime = lastPayTimeMap.get(order.getOrderId());
            if (segmentEntryTime == null)
            {
                segmentEntryTime = order.getEntryTime();
            }
            lastPayTimeMap.put(order.getOrderId(), payment.getPaySuccessTime());
            Date segmentLeaveDeadlineTime = getPaymentLeaveDeadlineTime(payment);
            vo.setLotId(order.getLotId());
            vo.setLotName(lotNameMap.get(order.getLotId()));
            vo.setSessionId(order.getSessionId());
            vo.setOrderNo(order.getOrderNo());
            vo.setPaymentNo(payment.getPaymentNo());
            vo.setPlateNo(order.getPlateNo());
            vo.setOrderType(order.getOrderType());
            vo.setPayChannel(payment.getPayChannel());
            vo.setPayAmount(payment.getPayAmount());
            vo.setPayStatus(payment.getPayStatus());
            vo.setParkingMinutes(ParkingFeeCalculator.calcParkingMinutes(segmentEntryTime, payment.getPaySuccessTime()));
            vo.setEntryTime(segmentEntryTime);
            vo.setCalcTime(payment.getPaySuccessTime());
            vo.setPaySuccessTime(payment.getPaySuccessTime());
            vo.setLeaveDeadlineTime(segmentLeaveDeadlineTime);
            return vo;
        }).collect(Collectors.toList());
        Collections.reverse(records);
        return records;
    }

    /**
     * H5 分页查询停车记录。
     */
    @Override
    public IPage<ParkingSession> queryH5SessionPage(ParkingSession query)
    {
        if (query == null || StringUtils.isEmpty(query.getPlateNo()))
        {
            throw new ServiceException("车牌号不能为空");
        }
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingSession::getPlateNo, query.getPlateNo());
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingSession::getLotId, query.getLotId());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingSession::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ParkingSession::getEntryTime);
        IPage<ParkingSession> page = parkingSessionMapper.selectPage(wrapper);
        refreshRuntimeSessionData(page.getRecords());
        enrichSessionPage(page.getRecords());
        return page;
    }

    /**
     * 执行模拟支付。
     *
     * <p>支付前会再次试算金额，避免用户打开 H5 到实际付款之间停车时长变化导致金额失真。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult payOrder(String orderNo, ParkingH5PayRequest request)
    {
        ParkingOrder order = parkingOrderMapper.selectOne(Wrappers.<ParkingOrder>lambdaQuery()
                .eq(ParkingOrder::getOrderNo, orderNo)
                .last("limit 1"));
        if (order == null)
        {
            return AjaxResult.error("订单不存在");
        }
        if (ParkingConstants.PAY_STATUS_SUCCESS.equals(order.getPayStatus()))
        {
            return AjaxResult.success("订单已支付");
        }
        ParkingSession session = parkingSessionMapper.selectById(order.getSessionId());
        if (session == null || ParkingConstants.SESSION_STATUS_COMPLETED.equals(session.getStatus()))
        {
            return AjaxResult.error("停车会话已结束");
        }

        // 首次支付前再次试算；补费订单已在超时查询/出场时锁定本次补费金额，不再按总时长覆盖。
        ParkingChargeRule rule = getEffectiveRule(order.getLotId());
        if (!ParkingConstants.SESSION_STATUS_WAIT_PAY.equals(session.getStatus()) || nullSafe(session.getTotalPaidAmount()).compareTo(BigDecimal.ZERO) <= 0)
        {
            order = preparePayableOrder(session, DateUtils.getNowDate(), rule);
        }
        markOrderPaid(order, request == null ? "WECHAT" : request.getPayChannel(), DateUtils.getNowDate());
        ParkingChargeRule effectiveRule = getEffectiveRule(order.getLotId());

        BigDecimal totalPaidAmount = nullSafe(session.getTotalPaidAmount()).add(nullSafe(order.getPaidAmount()));
        ParkingSession update = new ParkingSession();
        update.setSessionId(session.getSessionId());
        update.setStatus(ParkingConstants.SESSION_STATUS_PAID_PENDING_EXIT);
        update.setCurrentOrderNo(order.getOrderNo());
        update.setParkingMinutes(order.getParkingMinutes());
        update.setTotalPaidAmount(totalPaidAmount);
        update.setTotalPayableAmount(totalPaidAmount);
        update.setLeaveDeadlineTime(order.getLeaveDeadlineTime() != null ? order.getLeaveDeadlineTime()
                : plusMinutes(DateUtils.getNowDate(), effectiveRule.getPayGraceMinutes() == null ? 15 : effectiveRule.getPayGraceMinutes()));
        parkingSessionMapper.updateById(update);
        return AjaxResult.success("支付成功");
    }

    /**
     * 分页查询停车会话。
     */
    @Override
    public IPage<ParkingSession> selectSessionPage(ParkingSession query)
    {
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingSession::getTenantId, tenantId);
        }
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingSession::getLotId, query.getLotId());
        }
        if (StringUtils.isNotEmpty(query.getPlateNo()))
        {
            wrapper.like(ParkingSession::getPlateNo, query.getPlateNo());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingSession::getStatus, query.getStatus());
        }
        Object beginTime = query.getParams().get("beginEntryTime");
        Object endTime = query.getParams().get("endEntryTime");
        if (beginTime != null)
        {
            wrapper.ge(ParkingSession::getEntryTime, beginTime);
        }
        if (endTime != null)
        {
            wrapper.le(ParkingSession::getEntryTime, endTime);
        }
        wrapper.orderByDesc(ParkingSession::getEntryTime);
        IPage<ParkingSession> page = parkingSessionMapper.selectPage(wrapper);
        refreshRuntimeSessionData(page.getRecords());
        enrichSessionPage(page.getRecords());
        return page;
    }

    /**
     * 查询停车会话详情，同时返回关联订单、支付、开闸记录。
     */
    @Override
    public ParkingSessionDetailVo selectSessionDetail(Long sessionId)
    {
        ParkingSession session = parkingSessionMapper.selectById(sessionId);
        if (session == null)
        {
            throw new ServiceException("停车记录不存在");
        }
        validateTenant(session.getTenantId());
        refreshRuntimeSessionData(Collections.singletonList(session));
        enrichSessionPage(Collections.singletonList(session));

        List<ParkingOrder> orders = parkingOrderMapper.selectList(Wrappers.<ParkingOrder>lambdaQuery()
                .eq(ParkingOrder::getSessionId, sessionId)
                .orderByAsc(ParkingOrder::getCreateTime));
        List<Long> orderIds = orders.stream().map(ParkingOrder::getOrderId).collect(Collectors.toList());
        List<ParkingPayment> payments = CollectionUtils.isEmpty(orderIds) ? Collections.emptyList() : parkingPaymentMapper.selectList(
                Wrappers.<ParkingPayment>lambdaQuery().in(ParkingPayment::getOrderId, orderIds).orderByAsc(ParkingPayment::getCreateTime));
        List<ParkingGateCommand> commands = parkingGateCommandMapper.selectList(Wrappers.<ParkingGateCommand>lambdaQuery()
                .eq(ParkingGateCommand::getSessionId, sessionId)
                .orderByAsc(ParkingGateCommand::getCreateTime));
        if (CollectionUtils.isNotEmpty(commands) && CollectionUtils.isNotEmpty(orders))
        {
            Map<Long, String> orderNoMap = orders.stream()
                    .filter(item -> item.getOrderId() != null)
                    .collect(Collectors.toMap(ParkingOrder::getOrderId, ParkingOrder::getOrderNo, (a, b) -> a));
            for (ParkingGateCommand command : commands)
            {
                command.setOrderNo(orderNoMap.get(command.getOrderId()));
            }
        }

        ParkingSessionDetailVo vo = new ParkingSessionDetailVo();
        vo.setSession(session);
        vo.setOrders(orders);
        vo.setPayments(payments);
        vo.setCommands(commands);
        return vo;
    }

    /**
     * 按停车场、通道编码、通道类型查询通道。
     */
    private ParkingGate getGate(Long lotId, String gateCode, String gateType)
    {
        ParkingGate gate = parkingGateMapper.selectOne(Wrappers.<ParkingGate>lambdaQuery()
                .eq(ParkingGate::getLotId, lotId)
                .eq(ParkingGate::getGateCode, gateCode)
                .eq(ParkingGate::getGateType, gateType)
                .eq(ParkingGate::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .last("limit 1"));
        if (gate == null)
        {
            throw new ServiceException("通道不存在");
        }
        return gate;
    }

    /**
     * 按通道和设备编码查询设备。
     */
    private ParkingDevice getDevice(Long gateId, String deviceCode)
    {
        ParkingDevice device = parkingDeviceMapper.selectOne(Wrappers.<ParkingDevice>lambdaQuery()
                .eq(ParkingDevice::getGateId, gateId)
                .eq(ParkingDevice::getDeviceCode, deviceCode)
                .eq(ParkingDevice::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .last("limit 1"));
        if (device == null)
        {
            throw new ServiceException("设备不存在");
        }
        return device;
    }

    /**
     * 查询当前仍未闭环的停车会话。
     */
    private ParkingSession findActiveSession(Long lotId, String plateNo)
    {
        return parkingSessionMapper.selectOne(Wrappers.<ParkingSession>lambdaQuery()
                .eq(ParkingSession::getLotId, lotId)
                .eq(ParkingSession::getPlateNo, plateNo)
                .ne(ParkingSession::getStatus, ParkingConstants.SESSION_STATUS_COMPLETED)
                .orderByDesc(ParkingSession::getEntryTime)
                .last("limit 1"));
    }

    /**
     * 按车牌查询当前仍未闭环的停车会话。
     */
    private ParkingSession findActiveSession(String plateNo)
    {
        if (StringUtils.isEmpty(plateNo))
        {
            throw new ServiceException("车牌号不能为空");
        }
        return parkingSessionMapper.selectOne(Wrappers.<ParkingSession>lambdaQuery()
                .eq(ParkingSession::getPlateNo, plateNo)
                .ne(ParkingSession::getStatus, ParkingConstants.SESSION_STATUS_COMPLETED)
                .orderByDesc(ParkingSession::getEntryTime)
                .last("limit 1"));
    }

    /**
     * 构造 H5 待支付订单视图。
     */
    private ParkingH5OrderVo buildH5OrderVo(ParkingLot lot, ParkingSession session, ParkingOrder order)
    {
        ParkingH5OrderVo vo = new ParkingH5OrderVo();
        vo.setLotId(lot.getLotId());
        vo.setLotName(lot.getLotName());
        vo.setSessionId(session.getSessionId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPlateNo(session.getPlateNo());
        vo.setSessionStatus(session.getStatus());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setParkingMinutes(order.getParkingMinutes());
        vo.setPayableAmount(order.getPayableAmount());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setEntryTime(order.getEntryTime());
        vo.setCalcTime(order.getCalcTime());
        vo.setLeaveDeadlineTime(order.getLeaveDeadlineTime());
        return vo;
    }

    /**
     * 刷新 H5 当前应展示的订单。
     *
     * <p>已支付且仍在离场宽限期内，继续返回已支付订单；超过宽限期后才重新试算。
     * 重新试算后只有产生新增应缴金额时，才把会话转回待支付，避免 0 元补费订单干扰 H5。</p>
     */
    private ParkingOrder refreshH5PendingOrder(ParkingSession session, Date now)
    {
        ParkingOrder order = findCurrentOrder(session);
        ParkingChargeRule rule = getEffectiveRule(session.getLotId());
        if (ParkingConstants.SESSION_STATUS_PAID_PENDING_EXIT.equals(session.getStatus()))
        {
            Date deadline = session.getLeaveDeadlineTime() == null ? order.getLeaveDeadlineTime() : session.getLeaveDeadlineTime();
            if (deadline != null && !now.after(deadline))
            {
                return order;
            }
            if (!"1".equals(rule.getOvertimeRecalc()))
            {
                return order;
            }

            BigDecimal overtimeAmount = calcOvertimeAmount(session, rule, now);
            if (overtimeAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                ParkingOrder overtimeOrder = prepareOvertimePayableOrder(session, now, rule);
                markSessionWaitPay(session, overtimeOrder);
                return overtimeOrder;
            }
            return order;
        }

        if (ParkingConstants.SESSION_STATUS_WAIT_PAY.equals(session.getStatus())
                && nullSafe(session.getTotalPaidAmount()).compareTo(BigDecimal.ZERO) > 0)
        {
            return prepareOvertimePayableOrder(session, now, rule);
        }

        return preparePayableOrder(session, now, rule);
    }

    /**
     * 从支付离场宽限期结束后开始，按后续计费单价计算超时补费金额。
     */
    private BigDecimal calcOvertimeAmount(ParkingSession session, ParkingChargeRule rule, Date calcTime)
    {
        Date overtimeStartTime = session.getLeaveDeadlineTime();
        if (overtimeStartTime == null)
        {
            ParkingOrder order = findCurrentOrder(session);
            overtimeStartTime = order.getLeaveDeadlineTime();
        }
        return ParkingFeeCalculator.calcOvertimeAmount(rule, overtimeStartTime, calcTime);
    }

    /**
     * 获取当前会话正在处理的订单。
     *
     * <p>优先取 session.currentOrderNo；若为空，再回退到最近一笔订单。</p>
     */
    private ParkingOrder findCurrentOrder(ParkingSession session)
    {
        ParkingOrder order = null;
        if (StringUtils.isNotEmpty(session.getCurrentOrderNo()))
        {
            order = parkingOrderMapper.selectOne(Wrappers.<ParkingOrder>lambdaQuery()
                    .eq(ParkingOrder::getOrderNo, session.getCurrentOrderNo())
                    .last("limit 1"));
        }
        if (order == null)
        {
            order = parkingOrderMapper.selectOne(Wrappers.<ParkingOrder>lambdaQuery()
                    .eq(ParkingOrder::getSessionId, session.getSessionId())
                    .orderByDesc(ParkingOrder::getCreateTime)
                    .last("limit 1"));
        }
        if (order == null)
        {
            throw new ServiceException("停车订单不存在");
        }
        return order;
    }

    /**
     * 获取停车场当前生效的收费规则。
     *
     * <p>优先取停车场绑定规则，取不到再回退到租户默认规则。</p>
     */
    private ParkingChargeRule getEffectiveRule(Long lotId)
    {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null)
        {
            throw new ServiceException("停车场不存在");
        }
        if (lot.getChargeRuleId() != null)
        {
            ParkingChargeRule rule = parkingChargeRuleMapper.selectById(lot.getChargeRuleId());
            if (rule != null && ParkingConstants.DEL_FLAG_NORMAL.equals(rule.getDelFlag()))
            {
                return rule;
            }
        }
        ParkingChargeRule rule = parkingChargeRuleMapper.selectOne(Wrappers.<ParkingChargeRule>lambdaQuery()
                .eq(ParkingChargeRule::getTenantId, lot.getTenantId())
                .eq(ParkingChargeRule::getDefaultFlag, "1")
                .eq(ParkingChargeRule::getStatus, ParkingConstants.COMMON_STATUS_ENABLED)
                .eq(ParkingChargeRule::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .orderByDesc(ParkingChargeRule::getCreateTime)
                .last("limit 1"));
        if (rule == null)
        {
            rule = parkingChargeRuleMapper.selectOne(Wrappers.<ParkingChargeRule>lambdaQuery()
                    .eq(ParkingChargeRule::getTenantId, lot.getTenantId())
                    .eq(ParkingChargeRule::getStatus, ParkingConstants.COMMON_STATUS_ENABLED)
                    .eq(ParkingChargeRule::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                    .orderByDesc(ParkingChargeRule::getCreateTime)
                    .last("limit 1"));
        }
        if (rule == null)
        {
            throw new ServiceException("停车场未配置收费规则");
        }
        return rule;
    }

    /**
     * 生成或刷新当前应付订单。
     *
     * <p>如果存在未支付订单则直接更新；否则创建一笔新订单。
     * 当会话已经支付过且再次超时出场时，会生成补费订单，并扣减历史已付金额。</p>
     */
    private ParkingOrder preparePayableOrder(ParkingSession session, Date calcTime, ParkingChargeRule rule)
    {
        ParkingOrder order = parkingOrderMapper.selectOne(Wrappers.<ParkingOrder>lambdaQuery()
                .eq(ParkingOrder::getSessionId, session.getSessionId())
                .eq(ParkingOrder::getPayStatus, ParkingConstants.PAY_STATUS_UNPAID)
                .orderByDesc(ParkingOrder::getCreateTime)
                .last("limit 1"));
        if (order == null)
        {
            order = findCurrentOrder(session);
        }

        BigDecimal amount = ParkingFeeCalculator.calcAmount(rule, session.getEntryTime(), calcTime);
        if (ParkingConstants.ORDER_TYPE_OVERTIME.equals(order.getOrderType()))
        {
            // 补费订单只收新增差额，避免对历史已缴金额重复计费。
            amount = amount.subtract(nullSafe(session.getTotalPaidAmount()));
            if (amount.compareTo(BigDecimal.ZERO) < 0)
            {
                amount = BigDecimal.ZERO;
            }
        }
        order.setCalcTime(calcTime);
        order.setParkingMinutes(ParkingFeeCalculator.calcParkingMinutes(session.getEntryTime(), calcTime));
        order.setPayableAmount(amount);
        order.setOrderStatus(ParkingConstants.ORDER_STATUS_UNPAID);
        order.setPayStatus(ParkingConstants.PAY_STATUS_UNPAID);
        order.setLeaveDeadlineTime(null);
        parkingOrderMapper.updateById(order);

        ParkingSession update = new ParkingSession();
        update.setSessionId(session.getSessionId());
        update.setCurrentOrderNo(order.getOrderNo());
        update.setParkingMinutes(order.getParkingMinutes());
        update.setTotalPayableAmount(nullSafe(session.getTotalPaidAmount()).add(order.getPayableAmount()));
        parkingSessionMapper.updateById(update);
        return parkingOrderMapper.selectById(order.getOrderId());
    }

    /**
     * 复用当前订单生成超时补费待支付金额。
     */
    private ParkingOrder prepareOvertimePayableOrder(ParkingSession session, Date calcTime, ParkingChargeRule rule)
    {
        ParkingOrder order = findCurrentOrder(session);
        BigDecimal amount = calcOvertimeAmount(session, rule, calcTime);
        order.setCalcTime(calcTime);
        order.setParkingMinutes(ParkingFeeCalculator.calcParkingMinutes(session.getEntryTime(), calcTime));
        order.setPayableAmount(amount);
        order.setOrderStatus(ParkingConstants.ORDER_STATUS_UNPAID);
        order.setPayStatus(ParkingConstants.PAY_STATUS_UNPAID);
        order.setLeaveDeadlineTime(null);
        parkingOrderMapper.updateById(order);

        ParkingSession update = new ParkingSession();
        update.setSessionId(session.getSessionId());
        update.setCurrentOrderNo(order.getOrderNo());
        update.setParkingMinutes(order.getParkingMinutes());
        update.setTotalPayableAmount(nullSafe(session.getTotalPaidAmount()).add(amount));
        parkingSessionMapper.updateById(update);
        return parkingOrderMapper.selectById(order.getOrderId());
    }

    /**
     * 将会话标记为待支付并同步内存对象，避免 H5 返回旧状态。
     */
    private void markSessionWaitPay(ParkingSession session, ParkingOrder order)
    {
        ParkingSession update = new ParkingSession();
        update.setSessionId(session.getSessionId());
        update.setStatus(ParkingConstants.SESSION_STATUS_WAIT_PAY);
        update.setCurrentOrderNo(order.getOrderNo());
        parkingSessionMapper.updateById(update);
        session.setStatus(ParkingConstants.SESSION_STATUS_WAIT_PAY);
        session.setCurrentOrderNo(order.getOrderNo());
    }

    /**
     * 标记订单支付成功，并记录一条模拟支付流水。
     */
    private void markOrderPaid(ParkingOrder order, String payChannel, Date payTime)
    {
        ParkingChargeRule rule = getEffectiveRule(order.getLotId());
        BigDecimal currentPayAmount = nullSafe(order.getPayableAmount());
        BigDecimal totalPaidAmount = nullSafe(order.getPaidAmount()).add(currentPayAmount);
        ParkingPayment payment = new ParkingPayment();
        payment.setTenantId(order.getTenantId());
        payment.setLotId(order.getLotId());
        payment.setOrderId(order.getOrderId());
        payment.setOrderNo(order.getOrderNo());
        payment.setPaymentNo(ParkingNoUtils.paymentNo());
        payment.setPayChannel(StringUtils.isEmpty(payChannel) ? "WECHAT" : payChannel);
        payment.setPayAmount(currentPayAmount);
        payment.setPayStatus(ParkingConstants.PAY_STATUS_SUCCESS);
        payment.setThirdTradeNo(payment.getPaymentNo());
        payment.setNotifyContent("SIMULATED_PAYMENT");
        payment.setPaySuccessTime(payTime);
        parkingPaymentMapper.insert(payment);
        recordParkingIncome(order.getTenantId(), payment.getPaymentNo(), currentPayAmount);

        ParkingOrder update = new ParkingOrder();
        update.setOrderId(order.getOrderId());
        update.setOrderStatus(ParkingConstants.ORDER_STATUS_PAID);
        update.setPayStatus(ParkingConstants.PAY_STATUS_SUCCESS);
        update.setPaidAmount(totalPaidAmount);
        update.setPayableAmount(BigDecimal.ZERO);
        update.setPayTime(payTime);
        update.setLeaveDeadlineTime(plusMinutes(payTime, rule.getPayGraceMinutes() == null ? 15 : rule.getPayGraceMinutes()));
        parkingOrderMapper.updateById(update);

        order.setPaidAmount(currentPayAmount);
        order.setPayableAmount(BigDecimal.ZERO);
        order.setOrderStatus(ParkingConstants.ORDER_STATUS_PAID);
        order.setPayStatus(ParkingConstants.PAY_STATUS_SUCCESS);
        order.setPayTime(payTime);
        order.setLeaveDeadlineTime(update.getLeaveDeadlineTime());
    }

    /**
     * 支付成功后把停车收入入账到租户账户，并写账户流水。
     */
    private void recordParkingIncome(Long tenantId, String paymentNo, BigDecimal amount)
    {
        if (tenantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        ParkingAccount account = ensureParkingAccount(tenantId);
        BigDecimal beforeBalance = nullSafe(account.getBalanceAmount());
        BigDecimal afterBalance = beforeBalance.add(amount);

        ParkingAccount update = new ParkingAccount();
        update.setAccountId(account.getAccountId());
        update.setTotalIncomeAmount(nullSafe(account.getTotalIncomeAmount()).add(amount));
        update.setBalanceAmount(afterBalance);
        update.setAvailableAmount(nullSafe(account.getAvailableAmount()).add(amount));
        parkingAccountMapper.updateById(update);

        ParkingAccountFlow flow = new ParkingAccountFlow();
        flow.setTenantId(account.getTenantId());
        flow.setAccountId(account.getAccountId());
        flow.setBizType(ParkingConstants.ACCOUNT_BIZ_TYPE_PARKING_INCOME);
        flow.setBizNo(paymentNo);
        flow.setChangeType("IN");
        flow.setChangeAmount(amount);
        flow.setBeforeBalance(beforeBalance);
        flow.setAfterBalance(afterBalance);
        flow.setRemark("停车缴费入账");
        parkingAccountFlowMapper.insert(flow);
    }

    /**
     * 确保租户账户存在。
     */
    private ParkingAccount ensureParkingAccount(Long tenantId)
    {
        ParkingAccount account = parkingAccountMapper.selectOne(Wrappers.<ParkingAccount>lambdaQuery()
                .eq(ParkingAccount::getTenantId, tenantId)
                .last("limit 1"));
        if (account != null)
        {
            return account;
        }
        ParkingAccount create = new ParkingAccount();
        create.setTenantId(tenantId);
        create.setTotalIncomeAmount(BigDecimal.ZERO);
        create.setBalanceAmount(BigDecimal.ZERO);
        create.setAvailableAmount(BigDecimal.ZERO);
        create.setFrozenAmount(BigDecimal.ZERO);
        create.setStatus(ParkingConstants.ACCOUNT_STATUS_NORMAL);
        parkingAccountMapper.insert(create);
        return parkingAccountMapper.selectById(create.getAccountId());
    }

    /**
     * 创建一条模拟开闸指令。
     *
     * <p>当前版本没有真实下发硬件，所以这里直接把指令和回调结果一次性落库。</p>
     */
    private ParkingGateCommand createOpenGateCommand(ParkingSession session, ParkingGate gate, ParkingDevice device,
                                                     ParkingOrder order, boolean success, String remark)
    {
        ParkingGateCommand command = new ParkingGateCommand();
        command.setTenantId(session.getTenantId());
        command.setLotId(session.getLotId());
        command.setGateId(gate.getGateId());
        command.setDeviceId(device.getDeviceId());
        command.setSessionId(session.getSessionId());
        command.setOrderId(order == null ? null : order.getOrderId());
        command.setCommandNo(ParkingNoUtils.commandNo());
        command.setCommandType(ParkingConstants.COMMAND_TYPE_OPEN_GATE);
        command.setCommandStatus(success ? ParkingConstants.COMMAND_STATUS_SUCCESS : ParkingConstants.COMMAND_STATUS_FAIL);
        command.setCommandTime(DateUtils.getNowDate());
        command.setCallbackTime(DateUtils.getNowDate());
        command.setCallbackContent(success ? "SIMULATED_OPEN_GATE_SUCCESS" : "SIMULATED_OPEN_GATE_FAIL");
        command.setRemark(remark);
        command.setCreateTime(DateUtils.getNowDate());
        command.setUpdateTime(DateUtils.getNowDate());
        parkingGateCommandMapper.insert(command);
        return command;
    }

    /**
     * 按本次支付成功时间计算该流水对应的离场截止时间。
     */
    private Date getPaymentLeaveDeadlineTime(ParkingPayment payment)
    {
        if (payment == null || payment.getPaySuccessTime() == null)
        {
            return null;
        }
        ParkingChargeRule rule = getEffectiveRule(payment.getLotId());
        return plusMinutes(payment.getPaySuccessTime(), rule.getPayGraceMinutes() == null ? 15 : rule.getPayGraceMinutes());
    }

    /**
     * 把停车会话更新为已离场完成。
     */
    private void completeSession(ParkingSession session, ParkingGate gate, ParkingDevice device, Date exitTime, String imageUrl)
    {
        ParkingSession update = new ParkingSession();
        update.setSessionId(session.getSessionId());
        update.setExitGateId(gate.getGateId());
        update.setExitDeviceId(device.getDeviceId());
        update.setExitTime(exitTime);
        update.setExitImageUrl(imageUrl);
        update.setParkingMinutes(ParkingFeeCalculator.calcParkingMinutes(session.getEntryTime(), exitTime));
        update.setStatus(ParkingConstants.SESSION_STATUS_COMPLETED);
        parkingSessionMapper.updateById(update);
    }

    /**
     * 构造设备事件日志对象。
     */
    private ParkingIoEvent buildIoEvent(Long tenantId, Long lotId, Long gateId, Long deviceId, String eventType,
                                        String plateNo, Date eventTime, String imageUrl, String rawContent)
    {
        ParkingIoEvent event = new ParkingIoEvent();
        event.setTenantId(tenantId);
        event.setLotId(lotId);
        event.setGateId(gateId);
        event.setDeviceId(deviceId);
        event.setEventNo(ParkingNoUtils.eventNo());
        event.setEventType(eventType);
        event.setPlateNo(plateNo);
        event.setEventTime(eventTime);
        event.setImageUrl(imageUrl);
        event.setRawContent(rawContent);
        event.setHandleStatus("INIT");
        event.setCreateTime(DateUtils.getNowDate());
        event.setUpdateTime(DateUtils.getNowDate());
        return event;
    }

    /**
     * 更新事件处理结果。
     */
    private void markEvent(ParkingIoEvent event, String status, String msg)
    {
        ParkingIoEvent update = new ParkingIoEvent();
        update.setEventId(event.getEventId());
        update.setHandleStatus(status);
        update.setHandleMsg(msg);
        parkingIoEventMapper.updateById(update);
    }

    /**
     * 给停车会话结果补充停车场名称和租户名称，便于前端直接展示。
     */
    private void enrichSessionPage(List<ParkingSession> records)
    {
        if (CollectionUtils.isEmpty(records))
        {
            return;
        }
        Set<Long> lotIds = records.stream().map(ParkingSession::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> tenantIds = records.stream().map(ParkingSession::getTenantId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> lotNameMap = parkingLotMapper.selectList(Wrappers.<ParkingLot>lambdaQuery().in(ParkingLot::getLotId, lotIds))
                .stream().collect(Collectors.toMap(ParkingLot::getLotId, ParkingLot::getLotName, (a, b) -> a));
        Map<Long, String> tenantNameMap = sysTenantLiteMapper.selectList(Wrappers.<SysTenantLite>lambdaQuery().in(SysTenantLite::getId, tenantIds))
                .stream().collect(Collectors.toMap(SysTenantLite::getId, SysTenantLite::getTenantName, (a, b) -> a));
        for (ParkingSession record : records)
        {
            record.setLotName(lotNameMap.get(record.getLotId()));
            record.setTenantName(tenantNameMap.get(record.getTenantId()));
        }
    }

    private void refreshRuntimeSessionData(List<ParkingSession> records)
    {
        if (CollectionUtils.isEmpty(records))
        {
            return;
        }
        Date now = DateUtils.getNowDate();
        for (ParkingSession record : records)
        {
            if (record == null || record.getEntryTime() == null)
            {
                continue;
            }
            if (!ParkingConstants.SESSION_STATUS_COMPLETED.equals(record.getStatus()))
            {
                record.setParkingMinutes(ParkingFeeCalculator.calcParkingMinutes(record.getEntryTime(), now));
            }
        }
    }

    /**
     * BigDecimal 空值兜底。
     */
    private BigDecimal nullSafe(BigDecimal amount)
    {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 时间加分钟工具。
     */
    private Date plusMinutes(Date base, int minutes)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(base);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}
