package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingLot;
import com.huimao.parking.service.IParkingLotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 停车场后台管理接口。
 */
@Api(tags = "停车场管理")
@RestController
@RequestMapping("/admin/lot")
public class ParkingLotController extends BaseController
{
    @Autowired
    private IParkingLotService parkingLotService;

    /**
     * 分页查询停车场列表。
     */
    @ApiOperation("分页查询停车场")
    @RequiresPermissions("parking:lot:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingLot query)
    {
        return AjaxResult.success(parkingLotService.selectPage(query));
    }

    /**
     * 查询停车场详情。
     */
    @ApiOperation("查询停车场详情")
    @RequiresPermissions("parking:lot:query")
    @GetMapping("/{lotId}")
    public AjaxResult getInfo(@ApiParam(value = "停车场ID", required = true) @PathVariable Long lotId)
    {
        return AjaxResult.success(parkingLotService.selectById(lotId));
    }

    /**
     * 新增停车场。
     */
    @ApiOperation("新增停车场")
    @RequiresPermissions("parking:lot:add")
    @Log(title = "停车场管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ParkingLot parkingLot)
    {
        return toAjax(parkingLotService.insert(parkingLot));
    }

    /**
     * 编辑停车场。
     */
    @ApiOperation("编辑停车场")
    @RequiresPermissions("parking:lot:edit")
    @Log(title = "停车场管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ParkingLot parkingLot)
    {
        return toAjax(parkingLotService.update(parkingLot));
    }

    /**
     * 删除停车场。
     */
    @ApiOperation("删除停车场")
    @RequiresPermissions("parking:lot:remove")
    @Log(title = "停车场管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(value = "停车场ID数组", required = true) @PathVariable Long[] ids)
    {
        return toAjax(parkingLotService.deleteByIds(ids));
    }
}
