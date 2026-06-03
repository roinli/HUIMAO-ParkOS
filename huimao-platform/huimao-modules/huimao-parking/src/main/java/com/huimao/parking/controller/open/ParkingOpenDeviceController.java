package com.huimao.parking.controller.open;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.dto.DeviceEntryEventRequest;
import com.huimao.parking.dto.DeviceExitEventRequest;
import com.huimao.parking.dto.DeviceHeartbeatRequest;
import com.huimao.parking.dto.GateCommandCallbackRequest;
import com.huimao.parking.service.IParkingTradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 对设备开放的模拟接入接口。
 */
@Api(tags = "停车设备开放接口")
@RestController
@RequestMapping("/open/device")
public class ParkingOpenDeviceController extends BaseController
{
    @Autowired
    private IParkingTradeService parkingTradeService;

    /**
     * 设备上报入场识别事件。
     */
    @ApiOperation("设备上报入场识别事件")
    @PostMapping("/event/entry")
    public AjaxResult entry(@Validated @RequestBody DeviceEntryEventRequest request)
    {
        return parkingTradeService.handleEntry(request);
    }

    /**
     * 设备上报出场识别事件。
     */
    @ApiOperation("设备上报出场识别事件")
    @PostMapping("/event/exit")
    public AjaxResult exit(@Validated @RequestBody DeviceExitEventRequest request)
    {
        return AjaxResult.success(parkingTradeService.handleExit(request));
    }

    /**
     * 设备上报心跳。
     */
    @ApiOperation("设备上报心跳")
    @PostMapping("/event/heartbeat")
    public AjaxResult heartbeat(@Validated @RequestBody DeviceHeartbeatRequest request)
    {
        return parkingTradeService.handleHeartbeat(request);
    }

    /**
     * 设备回传开闸指令执行结果。
     */
    @ApiOperation("设备回传开闸指令执行结果")
    @PostMapping("/command/callback")
    public AjaxResult callback(@Validated @RequestBody GateCommandCallbackRequest request)
    {
        return parkingTradeService.handleCommandCallback(request);
    }
}
