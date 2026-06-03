package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingWithdrawOrder;
import com.huimao.parking.dto.ParkingWithdrawReviewRequest;
import com.huimao.parking.service.IParkingAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 提现记录接口。
 */
@Api(tags = "停车提现记录")
@RestController
@RequestMapping("/admin/withdraw")
public class ParkingWithdrawController extends BaseController
{
    @Autowired
    private IParkingAccountService parkingAccountService;

    @ApiOperation("分页查询提现记录")
    @RequiresPermissions("parking:withdraw:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingWithdrawOrder query)
    {
        return AjaxResult.success(parkingAccountService.selectWithdrawPage(query));
    }

    @ApiOperation("审核提现记录")
    @RequiresPermissions("parking:withdraw:review")
    @Log(title = "停车提现审核", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public AjaxResult review(@RequestBody ParkingWithdrawReviewRequest request)
    {
        return toAjax(parkingAccountService.reviewWithdraw(request));
    }
}
