package com.huimao.parking.service;

import com.alibaba.fastjson2.JSONObject;
import com.huimao.parking.domain.vo.CityOptionVo;

import java.util.List;

public interface IMapLocationService
{
    List<CityOptionVo> getProvinceList();

    List<CityOptionVo> getCityList(String provinceId);

    List<CityOptionVo> getAreaList(String cityId);

    JSONObject geocoder(String lat, String lng);

    JSONObject geocoderByAddress(String address);

    List<JSONObject> suggestion(String keyword, String lat, String lng);
}
