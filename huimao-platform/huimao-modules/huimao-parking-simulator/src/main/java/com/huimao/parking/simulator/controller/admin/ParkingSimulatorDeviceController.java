package com.huimao.parking.simulator.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.simulator.dto.ParkingSimulatorDeviceQuery;
import com.huimao.parking.simulator.dto.SimulatorDeviceConfigRequest;
import com.huimao.parking.simulator.dto.SimulatorPlateEventRequest;
import com.huimao.parking.simulator.service.IParkingSimulatorDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 模拟器后台管理接口。
 */
@Api(tags = "停车设备模拟器管理")
@RestController
@RequestMapping("/admin/device")
public class ParkingSimulatorDeviceController extends BaseController
{
    @Autowired
    private IParkingSimulatorDeviceService parkingSimulatorDeviceService;

    @ApiOperation("分页查询模拟器设备")
    @RequiresPermissions("parking:simulator:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingSimulatorDeviceQuery query)
    {
        return AjaxResult.success(parkingSimulatorDeviceService.selectPage(query));
    }

    @ApiOperation("查询模拟器设备详情")
    @RequiresPermissions("parking:simulator:query")
    @GetMapping("/{deviceId}")
    public AjaxResult getInfo(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return AjaxResult.success(parkingSimulatorDeviceService.selectByDeviceId(deviceId));
    }

    @ApiOperation("启动模拟器")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/start")
    public AjaxResult start(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return parkingSimulatorDeviceService.startSimulator(deviceId);
    }

    @ApiOperation("停止模拟器")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/stop")
    public AjaxResult stop(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return parkingSimulatorDeviceService.stopSimulator(deviceId);
    }

    @ApiOperation("设置模拟设备在线")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/online")
    public AjaxResult online(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return parkingSimulatorDeviceService.online(deviceId);
    }

    @ApiOperation("设置模拟设备离线")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/offline")
    public AjaxResult offline(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return parkingSimulatorDeviceService.offline(deviceId);
    }

    @ApiOperation("更新模拟器配置")
    @RequiresPermissions("parking:simulator:edit")
    @PutMapping("/config")
    public AjaxResult updateConfig(@Validated @RequestBody SimulatorDeviceConfigRequest request)
    {
        return parkingSimulatorDeviceService.updateConfig(request);
    }

    @ApiOperation("立即发送一次心跳")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/heartbeat")
    public AjaxResult heartbeat(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return parkingSimulatorDeviceService.heartbeat(deviceId);
    }

    @ApiOperation("模拟设备入场")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/entry")
    public AjaxResult entry(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId,
                            @RequestBody(required = false) SimulatorPlateEventRequest request)
    {
        return parkingSimulatorDeviceService.simulateEntry(deviceId, request);
    }

    @ApiOperation("模拟设备出场")
    @RequiresPermissions("parking:simulator:operate")
    @PostMapping("/{deviceId}/exit")
    public AjaxResult exit(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId,
                           @RequestBody(required = false) SimulatorPlateEventRequest request)
    {
        return parkingSimulatorDeviceService.simulateExit(deviceId, request);
    }
}
