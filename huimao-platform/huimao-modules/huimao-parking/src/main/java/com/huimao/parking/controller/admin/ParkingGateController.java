package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingGate;
import com.huimao.parking.service.IParkingGateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 停车通道后台管理接口。
 */
@Api(tags = "停车通道管理")
@RestController
@RequestMapping("/admin/gate")
public class ParkingGateController extends BaseController
{
    @Autowired
    private IParkingGateService parkingGateService;

    /**
     * 分页查询通道列表。
     */
    @ApiOperation("分页查询停车通道")
    @RequiresPermissions("parking:gate:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingGate query)
    {
        return AjaxResult.success(parkingGateService.selectPage(query));
    }

    /**
     * 查询通道详情。
     */
    @ApiOperation("查询停车通道详情")
    @RequiresPermissions("parking:gate:query")
    @GetMapping("/{gateId}")
    public AjaxResult getInfo(@ApiParam(value = "通道ID", required = true) @PathVariable Long gateId)
    {
        return AjaxResult.success(parkingGateService.selectById(gateId));
    }

    /**
     * 新增通道。
     */
    @ApiOperation("新增停车通道")
    @RequiresPermissions("parking:gate:add")
    @Log(title = "停车通道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ParkingGate parkingGate)
    {
        return toAjax(parkingGateService.insert(parkingGate));
    }

    /**
     * 编辑通道。
     */
    @ApiOperation("编辑停车通道")
    @RequiresPermissions("parking:gate:edit")
    @Log(title = "停车通道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ParkingGate parkingGate)
    {
        return toAjax(parkingGateService.update(parkingGate));
    }

    /**
     * 删除通道。
     */
    @ApiOperation("删除停车通道")
    @RequiresPermissions("parking:gate:remove")
    @Log(title = "停车通道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(value = "通道ID数组", required = true) @PathVariable Long[] ids)
    {
        return toAjax(parkingGateService.deleteByIds(ids));
    }
}
