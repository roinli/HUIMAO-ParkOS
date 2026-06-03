package com.huimao.parking.controller.admin;

import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.parking.domain.ParkingChargeRule;
import com.huimao.parking.service.IParkingChargeRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收费规则后台管理接口。
 */
@Api(tags = "停车收费规则管理")
@RestController
@RequestMapping("/admin/rule")
public class ParkingChargeRuleController extends BaseController
{
    @Autowired
    private IParkingChargeRuleService parkingChargeRuleService;

    /**
     * 分页查询收费规则。
     */
    @ApiOperation("分页查询收费规则")
    @RequiresPermissions("parking:rule:list")
    @GetMapping("/list")
    public AjaxResult list(ParkingChargeRule query)
    {
        return AjaxResult.success(parkingChargeRuleService.selectPage(query));
    }

    /**
     * 查询收费规则详情。
     */
    @ApiOperation("查询收费规则详情")
    @RequiresPermissions("parking:rule:query")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@ApiParam(value = "规则ID", required = true) @PathVariable Long ruleId)
    {
        return AjaxResult.success(parkingChargeRuleService.selectById(ruleId));
    }

    /**
     * 新增收费规则。
     */
    @ApiOperation("新增收费规则")
    @RequiresPermissions("parking:rule:add")
    @Log(title = "收费规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ParkingChargeRule rule)
    {
        return toAjax(parkingChargeRuleService.insert(rule));
    }

    /**
     * 编辑收费规则。
     */
    @ApiOperation("编辑收费规则")
    @RequiresPermissions("parking:rule:edit")
    @Log(title = "收费规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ParkingChargeRule rule)
    {
        return toAjax(parkingChargeRuleService.update(rule));
    }

    /**
     * 删除收费规则。
     */
    @ApiOperation("删除收费规则")
    @RequiresPermissions("parking:rule:remove")
    @Log(title = "收费规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(value = "规则ID数组", required = true) @PathVariable Long[] ids)
    {
        return toAjax(parkingChargeRuleService.deleteByIds(ids));
    }
}
