package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingSession;
import com.huimao.parking.service.IParkingTradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 停车记录后台查询接口。
 */
@Api(tags = "停车记录查询")
@RestController
@RequestMapping("/admin/session")
public class ParkingSessionController extends BaseController
{
    @Autowired
    private IParkingTradeService parkingTradeService;

    /**
     * 分页查询停车记录。
     */
    @ApiOperation("分页查询停车记录")
    @RequiresPermissions("parking:session:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingSession query)
    {
        return AjaxResult.success(parkingTradeService.selectSessionPage(query));
    }

    /**
     * 查询停车记录详情。
     */
    @ApiOperation("查询停车记录详情")
    @RequiresPermissions("parking:session:query")
    @GetMapping("/{sessionId}")
    public AjaxResult detail(@ApiParam(value = "停车会话ID", required = true) @PathVariable Long sessionId)
    {
        return AjaxResult.success(parkingTradeService.selectSessionDetail(sessionId));
    }
}
