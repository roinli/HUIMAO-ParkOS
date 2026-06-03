package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingAccountFlow;
import com.huimao.parking.domain.ParkingWithdrawOrder;
import com.huimao.parking.dto.ParkingWithdrawApplyRequest;
import com.huimao.parking.service.IParkingAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 我的账户接口。
 */
@Api(tags = "停车账户")
@RestController
@RequestMapping("/account")
public class ParkingAccountController extends BaseController
{
    @Autowired
    private IParkingAccountService parkingAccountService;

    @ApiOperation("查询账户总览")
    @RequiresPermissions("parking:account:summary")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(value = "tenantId", required = false) Long tenantId)
    {
        return AjaxResult.success(parkingAccountService.selectAccountSummary(tenantId));
    }

    @ApiOperation("查询账户信息")
    @RequiresPermissions("parking:account:summary")
    @GetMapping("/info")
    public AjaxResult info(@RequestParam(value = "tenantId", required = false) Long tenantId)
    {
        return AjaxResult.success(parkingAccountService.selectAccountInfo(tenantId));
    }

    @ApiOperation("分页查询账户提现记录")
    @RequiresPermissions("parking:account:summary")
    @GetMapping("/withdraw/list")
    public AjaxResult withdrawList(ParkingWithdrawOrder query)
    {
        return AjaxResult.success(parkingAccountService.selectWithdrawPage(query));
    }

    @ApiOperation("分页查询账户流水")
    @RequiresPermissions("parking:account:summary")
    @GetMapping("/flow/list")
    public AjaxResult flowList(ParkingAccountFlow query)
    {
        return AjaxResult.success(parkingAccountService.selectFlowPage(query));
    }

    @ApiOperation("发起提现申请")
    @RequiresPermissions("parking:withdraw:apply")
    @Log(title = "停车提现", businessType = BusinessType.INSERT)
    @PostMapping("/withdraw/apply")
    public AjaxResult apply(@RequestBody ParkingWithdrawApplyRequest request)
    {
        return toAjax(parkingAccountService.applyWithdraw(request));
    }
}
