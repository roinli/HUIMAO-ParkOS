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
 * 地图接口。
 */
@Api(tags = "地图服务")
@RestController
@RequestMapping("/qq/map")
public class MapController
{
    @Autowired
    private IMapLocationService mapLocationService;

    @ApiOperation("经纬度逆地址解析")
    @GetMapping("/geocoder")
    public AjaxResult geocoder(@RequestParam String lat, @RequestParam String lng)
    {
        return AjaxResult.success(mapLocationService.geocoder(lat, lng));
    }

    @ApiOperation("地址解析经纬度")
    @GetMapping("/geocoder/address")
    public AjaxResult geocoderByAddress(@RequestParam String address)
    {
        return AjaxResult.success(mapLocationService.geocoderByAddress(address));
    }

    @ApiOperation("地点输入提示")
    @GetMapping("/suggestion")
    public AjaxResult suggestion(@RequestParam String keyword,
                                 @RequestParam(required = false) String lat,
                                 @RequestParam(required = false) String lng)
    {
        return AjaxResult.success(mapLocationService.suggestion(keyword, lat, lng));
    }
}
