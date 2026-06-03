package com.huimao.parking.simulator.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.simulator.dto.ParkingSimulatorDeviceQuery;
import com.huimao.parking.simulator.dto.SimulatorDeviceConfigRequest;
import com.huimao.parking.simulator.dto.SimulatorPlateEventRequest;
import com.huimao.parking.simulator.vo.ParkingSimulatorDeviceVo;

public interface IParkingSimulatorDeviceService
{
    IPage<ParkingSimulatorDeviceVo> selectPage(ParkingSimulatorDeviceQuery query);

    ParkingSimulatorDeviceVo selectByDeviceId(Long deviceId);

    AjaxResult startSimulator(Long deviceId);

    AjaxResult stopSimulator(Long deviceId);

    AjaxResult online(Long deviceId);

    AjaxResult offline(Long deviceId);

    AjaxResult updateConfig(SimulatorDeviceConfigRequest request);

    AjaxResult heartbeat(Long deviceId);

    AjaxResult simulateEntry(Long deviceId, SimulatorPlateEventRequest request);

    AjaxResult simulateExit(Long deviceId, SimulatorPlateEventRequest request);

    void scheduleHeartbeat();
}
