package com.huimao.parking.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.DateUtils;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.simulator.config.ParkingSimulatorProperties;
import com.huimao.parking.simulator.constant.ParkingSimulatorConstants;
import com.huimao.parking.simulator.domain.ParkingDeviceLite;
import com.huimao.parking.simulator.domain.ParkingGateLite;
import com.huimao.parking.simulator.domain.ParkingLotLite;
import com.huimao.parking.simulator.domain.ParkingSimulatorDevice;
import com.huimao.parking.simulator.dto.ParkingSimulatorDeviceQuery;
import com.huimao.parking.simulator.dto.SimulatorDeviceConfigRequest;
import com.huimao.parking.simulator.dto.SimulatorPlateEventRequest;
import com.huimao.parking.simulator.mapper.ParkingDeviceLiteMapper;
import com.huimao.parking.simulator.mapper.ParkingGateLiteMapper;
import com.huimao.parking.simulator.mapper.ParkingLotLiteMapper;
import com.huimao.parking.simulator.mapper.ParkingSimulatorDeviceMapper;
import com.huimao.parking.simulator.service.IParkingSimulatorDeviceService;
import com.huimao.parking.simulator.vo.ParkingSimulatorDeviceVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 停车设备模拟器服务实现。
 */
@Slf4j
@Service
public class ParkingSimulatorDeviceServiceImpl extends AbstractSimulatorTenantService implements IParkingSimulatorDeviceService
{
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>()
    {
    };

    @Autowired
    private ParkingDeviceLiteMapper parkingDeviceLiteMapper;

    @Autowired
    private ParkingGateLiteMapper parkingGateLiteMapper;

    @Autowired
    private ParkingLotLiteMapper parkingLotLiteMapper;

    @Autowired
    private ParkingSimulatorDeviceMapper parkingSimulatorDeviceMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingSimulatorProperties parkingSimulatorProperties;

    /**
     * 分页查询模拟器设备。
     */
    @Override
    public IPage<ParkingSimulatorDeviceVo> selectPage(ParkingSimulatorDeviceQuery query)
    {
        LambdaQueryWrapper<ParkingDeviceLite> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingDeviceLite::getDelFlag, ParkingSimulatorConstants.DEL_FLAG_NORMAL);
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingDeviceLite::getTenantId, tenantId);
        }
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingDeviceLite::getLotId, query.getLotId());
        }
        if (StringUtils.isNotEmpty(query.getLotName()))
        {
            List<Long> lotIds = parkingLotLiteMapper.selectList(Wrappers.<ParkingLotLite>lambdaQuery()
                            .eq(ParkingLotLite::getDelFlag, ParkingSimulatorConstants.DEL_FLAG_NORMAL)
                            .like(ParkingLotLite::getLotName, query.getLotName()))
                    .stream().map(ParkingLotLite::getLotId).collect(Collectors.toList());
            if (lotIds.isEmpty())
            {
                return new Page<>(1, 10);
            }
            wrapper.in(ParkingDeviceLite::getLotId, lotIds);
        }
        if (StringUtils.isNotEmpty(query.getDeviceCode()))
        {
            wrapper.like(ParkingDeviceLite::getDeviceCode, query.getDeviceCode());
        }
        if (StringUtils.isNotEmpty(query.getDeviceName()))
        {
            wrapper.like(ParkingDeviceLite::getDeviceName, query.getDeviceName());
        }
        if (StringUtils.isNotEmpty(query.getSimulatorStatus()) || StringUtils.isNotEmpty(query.getAutoHeartbeat()))
        {
            LambdaQueryWrapper<ParkingSimulatorDevice> simulatorWrapper = Wrappers.lambdaQuery();
            if (StringUtils.isNotEmpty(query.getSimulatorStatus()))
            {
                simulatorWrapper.eq(ParkingSimulatorDevice::getSimulatorStatus, query.getSimulatorStatus());
            }
            if (StringUtils.isNotEmpty(query.getAutoHeartbeat()))
            {
                simulatorWrapper.eq(ParkingSimulatorDevice::getAutoHeartbeat, query.getAutoHeartbeat());
            }
            List<Long> deviceIds = parkingSimulatorDeviceMapper.selectList(simulatorWrapper)
                    .stream().map(ParkingSimulatorDevice::getDeviceId).collect(Collectors.toList());
            if (deviceIds.isEmpty())
            {
                return new Page<>(1, 10);
            }
            wrapper.in(ParkingDeviceLite::getDeviceId, deviceIds);
        }
        wrapper.orderByDesc(ParkingDeviceLite::getCreateTime);
        IPage<ParkingDeviceLite> page = parkingDeviceLiteMapper.selectPage(wrapper);
        Page<ParkingSimulatorDeviceVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(buildDeviceVos(page.getRecords()));
        return result;
    }

    /**
     * 查询模拟器设备详情。
     */
    @Override
    public ParkingSimulatorDeviceVo selectByDeviceId(Long deviceId)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        return buildDeviceVo(device,
                getGateMap(Collections.singleton(device.getGateId())).get(device.getGateId()),
                getLotMap(Collections.singleton(device.getLotId())).get(device.getLotId()),
                parkingSimulatorDeviceMapper.selectOne(Wrappers.<ParkingSimulatorDevice>lambdaQuery()
                        .eq(ParkingSimulatorDevice::getDeviceId, deviceId).last("limit 1")));
    }

    /**
     * 启动模拟器。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult startSimulator(Long deviceId)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = getOrCreateSimulator(deviceId);
        simulator.setSimulatorEnabled(ParkingSimulatorConstants.SIM_ENABLED_YES);
        simulator.setSimulatorStatus(ParkingSimulatorConstants.SIM_STATUS_RUNNING);
        simulator.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_ONLINE);
        simulator.setAutoHeartbeat(defaultIfEmpty(simulator.getAutoHeartbeat(), ParkingSimulatorConstants.YES));
        simulator.setHeartbeatIntervalSec(defaultIfNull(simulator.getHeartbeatIntervalSec(), parkingSimulatorProperties.getDefaultHeartbeatIntervalSec()));
        simulator.setAutoCallback(defaultIfEmpty(simulator.getAutoCallback(), ParkingSimulatorConstants.YES));
        simulator.setCallbackDelaySec(defaultIfNull(simulator.getCallbackDelaySec(), parkingSimulatorProperties.getDefaultCallbackDelaySec()));
        simulator.setDefaultPlateNo(defaultIfEmpty(simulator.getDefaultPlateNo(), "粤B12345"));
        saveSimulator(simulator);

        ParkingDeviceLite update = new ParkingDeviceLite();
        update.setDeviceId(device.getDeviceId());
        update.setConnectMode(ParkingSimulatorConstants.DEVICE_CONNECT_MODE_SIMULATED);
        if (StringUtils.isEmpty(device.getVendorCode()))
        {
            update.setVendorCode(ParkingSimulatorConstants.DEVICE_VENDOR_SIM);
        }
        parkingDeviceLiteMapper.updateById(update);
        return AjaxResult.success("模拟器已启动");
    }

    /**
     * 停止模拟器。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult stopSimulator(Long deviceId)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = getOrCreateSimulator(device.getDeviceId());
        simulator.setSimulatorEnabled(ParkingSimulatorConstants.SIM_ENABLED_NO);
        simulator.setSimulatorStatus(ParkingSimulatorConstants.SIM_STATUS_STOPPED);
        simulator.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_OFFLINE);
        saveSimulator(simulator);
        return AjaxResult.success("模拟器已停止");
    }

    /**
     * 设置模拟设备在线。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult online(Long deviceId)
    {
        assertSimulatorStarted(deviceId);
        ParkingSimulatorDevice simulator = getOrCreateSimulator(deviceId);
        simulator.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_ONLINE);
        simulator.setSimulatorEnabled(ParkingSimulatorConstants.SIM_ENABLED_YES);
        simulator.setSimulatorStatus(ParkingSimulatorConstants.SIM_STATUS_RUNNING);
        saveSimulator(simulator);
        return AjaxResult.success("设备已切换为在线");
    }

    /**
     * 设置模拟设备离线。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult offline(Long deviceId)
    {
        assertSimulatorStarted(deviceId);
        ParkingSimulatorDevice simulator = getOrCreateSimulator(deviceId);
        simulator.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_OFFLINE);
        saveSimulator(simulator);
        return AjaxResult.success("设备已切换为离线");
    }

    /**
     * 更新模拟器配置。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateConfig(SimulatorDeviceConfigRequest request)
    {
        getActiveDevice(request.getDeviceId());
        ParkingSimulatorDevice simulator = getOrCreateSimulator(request.getDeviceId());
        if (StringUtils.isNotEmpty(request.getAutoHeartbeat()))
        {
            simulator.setAutoHeartbeat(request.getAutoHeartbeat());
        }
        if (request.getHeartbeatIntervalSec() != null)
        {
            simulator.setHeartbeatIntervalSec(request.getHeartbeatIntervalSec());
        }
        if (StringUtils.isNotEmpty(request.getAutoCallback()))
        {
            simulator.setAutoCallback(request.getAutoCallback());
        }
        if (request.getCallbackDelaySec() != null)
        {
            simulator.setCallbackDelaySec(request.getCallbackDelaySec());
        }
        if (StringUtils.isNotEmpty(request.getDefaultPlateNo()))
        {
            simulator.setDefaultPlateNo(request.getDefaultPlateNo());
        }
        simulator.setRemark(request.getRemark());
        saveSimulator(simulator);
        return AjaxResult.success("配置已更新");
    }

    /**
     * 手动发送心跳。
     */
    @Override
    public AjaxResult heartbeat(Long deviceId)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = assertSimulatorOnline(deviceId);
        return heartbeatInternal(device, simulator);
    }

    /**
     * 模拟入场。
     */
    @Override
    public AjaxResult simulateEntry(Long deviceId, SimulatorPlateEventRequest request)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = assertSimulatorOnline(deviceId);
        ParkingGateLite gate = getGate(device.getGateId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lotId", device.getLotId());
        body.put("gateCode", gate.getGateCode());
        body.put("deviceCode", device.getDeviceCode());
        body.put("plateNo", resolvePlateNo(request, simulator));
        putEventFields(body, request);

        Map<String, Object> response = invokePlatform("/open/device/event/entry", body);
        updateInvokeResult(simulator, response, null);
        return toAjaxResult(response, "模拟入场完成");
    }

    /**
     * 模拟出场。
     */
    @Override
    public AjaxResult simulateExit(Long deviceId, SimulatorPlateEventRequest request)
    {
        ParkingDeviceLite device = getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = assertSimulatorOnline(deviceId);
        ParkingGateLite gate = getGate(device.getGateId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lotId", device.getLotId());
        body.put("gateCode", gate.getGateCode());
        body.put("deviceCode", device.getDeviceCode());
        body.put("plateNo", resolvePlateNo(request, simulator));
        putEventFields(body, request);

        Map<String, Object> response = invokePlatform("/open/device/event/exit", body);
        Map<String, Object> data = readDataMap(response);
        updateInvokeResult(simulator, response, data);
        autoCallbackIfNecessary(device, simulator, data);
        return toExitAjaxResult(response, data);
    }

    /**
     * 定时发送心跳。
     */
    @Override
    @Scheduled(fixedDelayString = "${parking.simulator.schedule-delay-ms:5000}")
    public void scheduleHeartbeat()
    {
        List<ParkingSimulatorDevice> simulators = parkingSimulatorDeviceMapper.selectList(Wrappers.<ParkingSimulatorDevice>lambdaQuery()
                .eq(ParkingSimulatorDevice::getSimulatorEnabled, ParkingSimulatorConstants.SIM_ENABLED_YES)
                .eq(ParkingSimulatorDevice::getSimulatorStatus, ParkingSimulatorConstants.SIM_STATUS_RUNNING)
                .eq(ParkingSimulatorDevice::getOnlineStatus, ParkingSimulatorConstants.ONLINE_STATUS_ONLINE)
                .eq(ParkingSimulatorDevice::getAutoHeartbeat, ParkingSimulatorConstants.YES));
        if (simulators.isEmpty())
        {
            return;
        }
        Map<Long, ParkingDeviceLite> deviceMap = parkingDeviceLiteMapper.selectList(Wrappers.<ParkingDeviceLite>lambdaQuery()
                        .in(ParkingDeviceLite::getDeviceId, simulators.stream().map(ParkingSimulatorDevice::getDeviceId).collect(Collectors.toList()))
                        .eq(ParkingDeviceLite::getDelFlag, ParkingSimulatorConstants.DEL_FLAG_NORMAL))
                .stream().collect(Collectors.toMap(ParkingDeviceLite::getDeviceId, item -> item, (a, b) -> a));
        for (ParkingSimulatorDevice simulator : simulators)
        {
            ParkingDeviceLite device = deviceMap.get(simulator.getDeviceId());
            if (device == null || !needHeartbeat(simulator))
            {
                continue;
            }
            try
            {
                heartbeatInternal(device, simulator);
            }
            catch (Exception ex)
            {
                log.warn("模拟器自动心跳失败, deviceId={}, msg={}", simulator.getDeviceId(), ex.getMessage());
            }
        }
    }

    private AjaxResult heartbeatInternal(ParkingDeviceLite device, ParkingSimulatorDevice simulator)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deviceCode", device.getDeviceCode());
        body.put("heartbeatTime", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getNowDate()));
        body.put("rawContent", "SIMULATOR_HEARTBEAT");
        Map<String, Object> response = invokePlatform("/open/device/event/heartbeat", body);
        updateInvokeResult(simulator, response, null);
        if (isSuccess(response))
        {
            simulator.setLastHeartbeatTime(DateUtils.getNowDate());
            parkingSimulatorDeviceMapper.updateById(simulator);
        }
        return toAjaxResult(response, "心跳发送完成");
    }

    private void autoCallbackIfNecessary(ParkingDeviceLite device, ParkingSimulatorDevice simulator, Map<String, Object> data)
    {
        if (data == null || !Objects.equals(Boolean.TRUE, data.get("allowOpen")))
        {
            return;
        }
        String commandNo = stringify(data.get("commandNo"));
        if (StringUtils.isEmpty(commandNo) || !ParkingSimulatorConstants.YES.equals(simulator.getAutoCallback()))
        {
            return;
        }
        int delaySec = defaultIfNull(simulator.getCallbackDelaySec(), parkingSimulatorProperties.getDefaultCallbackDelaySec());
        CompletableFuture.runAsync(() -> {
            try
            {
                Thread.sleep(Math.max(delaySec, 0) * 1000L);
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("commandNo", commandNo);
                body.put("success", true);
                body.put("callbackTime", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getNowDate()));
                body.put("rawContent", "SIMULATOR_AUTO_CALLBACK:" + device.getDeviceCode());
                Map<String, Object> response = invokePlatform("/open/device/command/callback", body);
                updateInvokeResult(simulator, response, null);
            }
            catch (Exception ex)
            {
                log.warn("模拟器自动回调失败, deviceId={}, commandNo={}, msg={}", device.getDeviceId(), commandNo, ex.getMessage());
            }
        });
    }

    private boolean needHeartbeat(ParkingSimulatorDevice simulator)
    {
        Integer intervalSec = defaultIfNull(simulator.getHeartbeatIntervalSec(), parkingSimulatorProperties.getDefaultHeartbeatIntervalSec());
        Date lastHeartbeatTime = simulator.getLastHeartbeatTime();
        if (lastHeartbeatTime == null)
        {
            return true;
        }
        return System.currentTimeMillis() - lastHeartbeatTime.getTime() >= intervalSec * 1000L;
    }

    private void putEventFields(Map<String, Object> body, SimulatorPlateEventRequest request)
    {
        if (request == null)
        {
            return;
        }
        if (request.getEventTime() != null)
        {
            body.put("eventTime", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, request.getEventTime()));
        }
        if (StringUtils.isNotEmpty(request.getImageUrl()))
        {
            body.put("imageUrl", request.getImageUrl());
        }
        if (StringUtils.isNotEmpty(request.getRawContent()))
        {
            body.put("rawContent", request.getRawContent());
        }
    }

    private String resolvePlateNo(SimulatorPlateEventRequest request, ParkingSimulatorDevice simulator)
    {
        if (request != null && StringUtils.isNotEmpty(request.getPlateNo()))
        {
            return request.getPlateNo();
        }
        if (StringUtils.isNotEmpty(simulator.getDefaultPlateNo()))
        {
            return simulator.getDefaultPlateNo();
        }
        return "粤B12345";
    }

    private Map<String, Object> invokePlatform(String path, Object body)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = StringUtils.stripEnd(parkingSimulatorProperties.getPlatformUrl(), "/") + path;
        Object response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Object.class);
        if (response == null)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put(AjaxResult.CODE_TAG, 500);
            result.put(AjaxResult.MSG_TAG, "平台接口无响应");
            return result;
        }
        return objectMapper.convertValue(response, MAP_TYPE);
    }

    private AjaxResult toAjaxResult(Map<String, Object> response, String defaultMsg)
    {
        Integer code = integerValue(response.get(AjaxResult.CODE_TAG), 500);
        String msg = stringify(response.get(AjaxResult.MSG_TAG));
        Object data = response.get(AjaxResult.DATA_TAG);
        if (code != null && code == 200)
        {
            return AjaxResult.success(StringUtils.isEmpty(msg) ? defaultMsg : msg, data);
        }
        return AjaxResult.error(StringUtils.isEmpty(msg) ? "调用平台接口失败" : msg, data);
    }

    private AjaxResult toExitAjaxResult(Map<String, Object> response, Map<String, Object> data)
    {
        Integer code = integerValue(response.get(AjaxResult.CODE_TAG), 500);
        String msg = stringify(response.get(AjaxResult.MSG_TAG));
        if (code == null || code != 200)
        {
            return AjaxResult.error(StringUtils.isEmpty(msg) ? "调用平台接口失败" : msg, response.get(AjaxResult.DATA_TAG));
        }
        if (data != null && !Objects.equals(Boolean.TRUE, data.get("allowOpen")))
        {
            String reason = stringify(data.get("message"));
            return AjaxResult.error(StringUtils.isEmpty(reason) ? "车辆未放行" : reason, data);
        }
        return AjaxResult.success(StringUtils.isEmpty(msg) ? "模拟出场完成" : msg, response.get(AjaxResult.DATA_TAG));
    }

    private boolean isSuccess(Map<String, Object> response)
    {
        return integerValue(response.get(AjaxResult.CODE_TAG), 500) == 200;
    }

    private Map<String, Object> readDataMap(Map<String, Object> response)
    {
        Object data = response.get(AjaxResult.DATA_TAG);
        if (data == null)
        {
            return null;
        }
        return objectMapper.convertValue(data, MAP_TYPE);
    }

    private void updateInvokeResult(ParkingSimulatorDevice simulator, Map<String, Object> response, Map<String, Object> data)
    {
        simulator.setLastRequestTime(DateUtils.getNowDate());
        simulator.setLastResponseCode(integerValue(response.get(AjaxResult.CODE_TAG), 500));
        simulator.setLastResponseMsg(stringify(response.get(AjaxResult.MSG_TAG)));
        if (data != null)
        {
            simulator.setLastCommandNo(stringify(data.get("commandNo")));
            simulator.setLastOrderNo(stringify(data.get("orderNo")));
        }
        saveSimulator(simulator);
    }

    private void saveSimulator(ParkingSimulatorDevice simulator)
    {
        if (simulator.getSimId() == null)
        {
            parkingSimulatorDeviceMapper.insert(simulator);
        }
        else
        {
            parkingSimulatorDeviceMapper.updateById(simulator);
        }
    }

    private ParkingSimulatorDevice getOrCreateSimulator(Long deviceId)
    {
        ParkingSimulatorDevice simulator = parkingSimulatorDeviceMapper.selectOne(Wrappers.<ParkingSimulatorDevice>lambdaQuery()
                .eq(ParkingSimulatorDevice::getDeviceId, deviceId)
                .last("limit 1"));
        if (simulator != null)
        {
            return simulator;
        }
        simulator = new ParkingSimulatorDevice();
        simulator.setDeviceId(deviceId);
        simulator.setSimulatorEnabled(ParkingSimulatorConstants.SIM_ENABLED_NO);
        simulator.setSimulatorStatus(ParkingSimulatorConstants.SIM_STATUS_STOPPED);
        simulator.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_OFFLINE);
        simulator.setAutoHeartbeat(ParkingSimulatorConstants.YES);
        simulator.setHeartbeatIntervalSec(parkingSimulatorProperties.getDefaultHeartbeatIntervalSec());
        simulator.setAutoCallback(ParkingSimulatorConstants.YES);
        simulator.setCallbackDelaySec(parkingSimulatorProperties.getDefaultCallbackDelaySec());
        simulator.setDefaultPlateNo("粤B12345");
        return simulator;
    }

    private ParkingSimulatorDevice assertSimulatorStarted(Long deviceId)
    {
        getActiveDevice(deviceId);
        ParkingSimulatorDevice simulator = getOrCreateSimulator(deviceId);
        if (!ParkingSimulatorConstants.SIM_ENABLED_YES.equals(simulator.getSimulatorEnabled())
                || !ParkingSimulatorConstants.SIM_STATUS_RUNNING.equals(simulator.getSimulatorStatus()))
        {
            throw new ServiceException("请先启动模拟器");
        }
        return simulator;
    }

    private ParkingSimulatorDevice assertSimulatorOnline(Long deviceId)
    {
        ParkingSimulatorDevice simulator = assertSimulatorStarted(deviceId);
        if (!ParkingSimulatorConstants.ONLINE_STATUS_ONLINE.equals(simulator.getOnlineStatus()))
        {
            throw new ServiceException("模拟设备当前处于离线状态");
        }
        return simulator;
    }

    private ParkingDeviceLite getActiveDevice(Long deviceId)
    {
        ParkingDeviceLite device = parkingDeviceLiteMapper.selectById(deviceId);
        if (device == null || !ParkingSimulatorConstants.DEL_FLAG_NORMAL.equals(device.getDelFlag()))
        {
            throw new ServiceException("停车设备不存在");
        }
        validateTenant(device.getTenantId());
        return device;
    }

    private ParkingGateLite getGate(Long gateId)
    {
        ParkingGateLite gate = parkingGateLiteMapper.selectById(gateId);
        if (gate == null || !ParkingSimulatorConstants.DEL_FLAG_NORMAL.equals(gate.getDelFlag()))
        {
            throw new ServiceException("停车通道不存在");
        }
        return gate;
    }

    private List<ParkingSimulatorDeviceVo> buildDeviceVos(List<ParkingDeviceLite> devices)
    {
        if (devices == null || devices.isEmpty())
        {
            return Collections.emptyList();
        }
        Set<Long> gateIds = devices.stream().map(ParkingDeviceLite::getGateId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> lotIds = devices.stream().map(ParkingDeviceLite::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> deviceIds = devices.stream().map(ParkingDeviceLite::getDeviceId).collect(Collectors.toSet());
        Map<Long, ParkingGateLite> gateMap = getGateMap(gateIds);
        Map<Long, ParkingLotLite> lotMap = getLotMap(lotIds);
        Map<Long, ParkingSimulatorDevice> simulatorMap = parkingSimulatorDeviceMapper.selectList(Wrappers.<ParkingSimulatorDevice>lambdaQuery()
                        .in(ParkingSimulatorDevice::getDeviceId, deviceIds))
                .stream().collect(Collectors.toMap(ParkingSimulatorDevice::getDeviceId, item -> item, (a, b) -> a));
        return devices.stream().map(device ->
                buildDeviceVo(device, gateMap.get(device.getGateId()), lotMap.get(device.getLotId()), simulatorMap.get(device.getDeviceId())))
                .collect(Collectors.toList());
    }

    private ParkingSimulatorDeviceVo buildDeviceVo(ParkingDeviceLite device, ParkingGateLite gate, ParkingLotLite lot, ParkingSimulatorDevice simulator)
    {
        ParkingSimulatorDeviceVo vo = new ParkingSimulatorDeviceVo();
        vo.setDeviceId(device.getDeviceId());
        vo.setTenantId(device.getTenantId());
        vo.setLotId(device.getLotId());
        vo.setLotName(lot == null ? null : lot.getLotName());
        vo.setGateId(device.getGateId());
        vo.setGateCode(gate == null ? null : gate.getGateCode());
        vo.setGateName(gate == null ? null : gate.getGateName());
        vo.setGateType(gate == null ? null : gate.getGateType());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceType(device.getDeviceType());
        vo.setVendorCode(device.getVendorCode());
        vo.setConnectMode(device.getConnectMode());
        vo.setDeviceStatus(device.getStatus());
        if (simulator == null)
        {
            vo.setSimulatorEnabled(ParkingSimulatorConstants.SIM_ENABLED_NO);
            vo.setSimulatorStatus(ParkingSimulatorConstants.SIM_STATUS_STOPPED);
            vo.setOnlineStatus(ParkingSimulatorConstants.ONLINE_STATUS_OFFLINE);
            vo.setAutoHeartbeat(ParkingSimulatorConstants.YES);
            vo.setHeartbeatIntervalSec(parkingSimulatorProperties.getDefaultHeartbeatIntervalSec());
            vo.setAutoCallback(ParkingSimulatorConstants.YES);
            vo.setCallbackDelaySec(parkingSimulatorProperties.getDefaultCallbackDelaySec());
            vo.setDefaultPlateNo("粤B12345");
            return vo;
        }
        BeanUtils.copyProperties(simulator, vo);
        return vo;
    }

    private Map<Long, ParkingGateLite> getGateMap(Set<Long> gateIds)
    {
        if (gateIds == null || gateIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        return parkingGateLiteMapper.selectList(Wrappers.<ParkingGateLite>lambdaQuery().in(ParkingGateLite::getGateId, gateIds))
                .stream().collect(Collectors.toMap(ParkingGateLite::getGateId, item -> item, (a, b) -> a));
    }

    private Map<Long, ParkingLotLite> getLotMap(Set<Long> lotIds)
    {
        if (lotIds == null || lotIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        return parkingLotLiteMapper.selectList(Wrappers.<ParkingLotLite>lambdaQuery().in(ParkingLotLite::getLotId, lotIds))
                .stream().collect(Collectors.toMap(ParkingLotLite::getLotId, item -> item, (a, b) -> a));
    }

    private String stringify(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private Integer integerValue(Object value, Integer defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception ex)
        {
            return defaultValue;
        }
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    private String defaultIfEmpty(String value, String defaultValue)
    {
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }
}
