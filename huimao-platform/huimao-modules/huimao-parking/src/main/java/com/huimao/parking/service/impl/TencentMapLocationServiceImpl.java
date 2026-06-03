package com.huimao.parking.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.common.core.utils.http.HttpUtils;
import com.huimao.parking.config.TencentMapProperties;
import com.huimao.parking.domain.CCity;
import com.huimao.parking.domain.vo.CityOptionVo;
import com.huimao.parking.mapper.CCityMapper;
import com.huimao.parking.service.IMapLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 腾讯地图位置服务。
 */
@Service
public class TencentMapLocationServiceImpl implements IMapLocationService
{
    private static final String GEOCODER_URL = "https://apis.map.qq.com/ws/geocoder/v1/";
    private static final String SUGGESTION_URL = "https://apis.map.qq.com/ws/place/v1/suggestion";

    @Autowired
    private TencentMapProperties properties;

    @Autowired
    private CCityMapper cCityMapper;

    @Override
    public List<CityOptionVo> getProvinceList()
    {
        return getCityOptions("0");
    }

    @Override
    public List<CityOptionVo> getCityList(String pid)
    {
        if (StringUtils.isBlank(pid))
        {
            return Collections.emptyList();
        }
        return getCityOptions(pid);
    }

    @Override
    public List<CityOptionVo> getAreaList(String cityId)
    {
        if (StringUtils.isBlank(cityId))
        {
            return Collections.emptyList();
        }
        return getCityOptions(cityId);
    }

    @Override
    public JSONObject geocoder(String lat, String lng)
    {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng))
        {
            throw new ServiceException("经纬度不能为空");
        }
        JSONObject response = request(GEOCODER_URL, "location=" + encode(lat + "," + lng));
        JSONObject result = response.getJSONObject("result");
        if (result == null)
        {
            return new JSONObject();
        }
        JSONArray pois = result.getJSONArray("pois");
        if (pois == null)
        {
            result.put("pois", new JSONArray());
        }
        return result;
    }

    @Override
    public JSONObject geocoderByAddress(String address)
    {
        if (StringUtils.isBlank(address))
        {
            throw new ServiceException("地址不能为空");
        }
        JSONObject response = request(GEOCODER_URL, "address=" + encode(address));
        JSONObject result = response.getJSONObject("result");
        if (result == null)
        {
            return new JSONObject();
        }
        return result;
    }

    @Override
    public List<JSONObject> suggestion(String keyword, String lat, String lng)
    {
        if (StringUtils.isBlank(keyword))
        {
            return Collections.emptyList();
        }
        StringBuilder params = new StringBuilder("keyword=").append(encode(keyword)).append("&region=全国");
        if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lng))
        {
            params.append("&location=").append(encode(lat + "," + lng));
        }
        JSONObject response = request(SUGGESTION_URL, params.toString());
        JSONArray data = response.getJSONArray("data");
        if (data == null)
        {
            return Collections.emptyList();
        }
        List<JSONObject> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++)
        {
            result.add(data.getJSONObject(i));
        }
        return result;
    }

    private List<CityOptionVo> getCityOptions(String pid)
    {
        List<CCity> records = cCityMapper.selectList(Wrappers.<CCity>lambdaQuery()
                .eq(CCity::getPid, Integer.valueOf(pid))
                .orderByAsc(CCity::getId));
        List<CityOptionVo> options = new ArrayList<>();
        for (CCity item : records)
        {
            options.add(new CityOptionVo(String.valueOf(item.getId()), String.valueOf(item.getPid()), item.getName()));
        }
        return options;
    }

    private JSONObject request(String url, String params)
    {
        if (StringUtils.isBlank(properties.getKey()))
        {
            throw new ServiceException("腾讯地图Key未配置");
        }
        String realParams = params + "&key=" + encode(properties.getKey());
        String response = HttpUtils.sendGet(url, realParams);
        if (StringUtils.isBlank(response))
        {
            throw new ServiceException("腾讯地图接口调用失败");
        }
        JSONObject json = JSON.parseObject(response);
        int status = json.getIntValue("status");
        if (status != 0)
        {
            throw new ServiceException(json.getString("message"));
        }
        return json;
    }

    private String encode(String value)
    {
        return UriUtils.encode(value, StandardCharsets.UTF_8);
    }
}
