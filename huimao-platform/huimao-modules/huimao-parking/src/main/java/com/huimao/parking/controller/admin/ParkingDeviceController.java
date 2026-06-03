package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingDevice;
import com.huimao.parking.service.IParkingDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 停车设备后台管理接口。
 */
@Api(tags = "停车设备管理")
@RestController
@RequestMapping("/admin/device")
public class ParkingDeviceController extends BaseController
{
    @Autowired
    private IParkingDeviceService parkingDeviceService;

    /**
     * 分页查询设备列表。
     */
    @ApiOperation("分页查询停车设备")
    @RequiresPermissions("parking:device:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingDevice query)
    {
        return AjaxResult.success(parkingDeviceService.selectPage(query));
    }

    /**
     * 查询设备详情。
     */
    @ApiOperation("查询停车设备详情")
    @RequiresPermissions("parking:device:query")
    @GetMapping("/{deviceId}")
    public AjaxResult getInfo(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId)
    {
        return AjaxResult.success(parkingDeviceService.selectById(deviceId));
    }

    /**
     * 新增设备。
     */
    @ApiOperation("新增停车设备")
    @RequiresPermissions("parking:device:add")
    @Log(title = "停车设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ParkingDevice parkingDevice)
    {
        return toAjax(parkingDeviceService.insert(parkingDevice));
    }

    /**
     * 编辑设备。
     */
    @ApiOperation("编辑停车设备")
    @RequiresPermissions("parking:device:edit")
    @Log(title = "停车设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ParkingDevice parkingDevice)
    {
        return toAjax(parkingDeviceService.update(parkingDevice));
    }

    /**
     * 删除设备。
     */
    @ApiOperation("删除停车设备")
    @RequiresPermissions("parking:device:remove")
    @Log(title = "停车设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(value = "设备ID数组", required = true) @PathVariable Long[] ids)
    {
        return toAjax(parkingDeviceService.deleteByIds(ids));
    }
}
