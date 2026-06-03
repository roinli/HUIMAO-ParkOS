package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingDailyStat;
import com.huimao.parking.service.IParkingStatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收入统计接口。
 */
@Api(tags = "停车收入统计")
@RestController
@RequestMapping("/admin/stat")
public class ParkingStatController extends BaseController
{
    @Autowired
    private IParkingStatService parkingStatService;

    @ApiOperation("分页查询收入统计")
    @RequiresPermissions("parking:stat:list")
    @GetMapping("/daily")
    public AjaxResult daily(ParkingDailyStat query)
    {
        IPage<ParkingDailyStat> page = parkingStatService.selectDailyPage(query);
        return AjaxResult.success(page);
    }

    @ApiOperation("查询收入统计汇总")
    @RequiresPermissions("parking:stat:query")
    @GetMapping("/overview")
    public AjaxResult overview(ParkingDailyStat query)
    {
        return AjaxResult.success(parkingStatService.selectOverview(query));
    }

    @ApiOperation("查询首页停车统计看板")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(Long lotId, String startTime, String endTime)
    {
        return AjaxResult.success(parkingStatService.selectDashboard(lotId, startTime, endTime));
    }

    @ApiOperation("刷新收入日统计")
    @RequiresPermissions("parking:stat:query")
    @GetMapping("/refresh")
    public AjaxResult refresh(ParkingDailyStat query)
    {
        return AjaxResult.success(parkingStatService.refreshDailyStat(query));
    }
}
