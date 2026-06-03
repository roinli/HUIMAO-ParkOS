package com.huimao.parking.controller.common;

import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.service.IMapLocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行政区划接口。
 */
@Api(tags = "行政区划")
@RestController
@RequestMapping("/city")
public class CityController
{
    @Autowired
    private IMapLocationService mapLocationService;

    @ApiOperation("省份列表")
    @GetMapping("/getProvinceList")
    public AjaxResult getProvinceList()
    {
        return AjaxResult.success(mapLocationService.getProvinceList());
    }

    @ApiOperation("下级城市/区县列表")
    @GetMapping("/getCityList")
    public AjaxResult getCityList(@RequestParam(required = false) String pid)
    {
        return AjaxResult.success(mapLocationService.getCityList(pid));
    }

    @ApiOperation("城市列表")
    @GetMapping("/getCities")
    public AjaxResult getCities(@RequestParam String provinceId)
    {
        return AjaxResult.success(mapLocationService.getCityList(provinceId));
    }

    @ApiOperation("区县列表")
    @GetMapping("/getAreas")
    public AjaxResult getAreas(@RequestParam String cityId)
    {
        return AjaxResult.success(mapLocationService.getAreaList(cityId));
    }
}
