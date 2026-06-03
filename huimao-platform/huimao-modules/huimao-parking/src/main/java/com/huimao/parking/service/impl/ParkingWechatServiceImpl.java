package com.huimao.parking.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.parking.service.IParkingWechatService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 停车模块微信能力服务实现。
 */
@Service
public class ParkingWechatServiceImpl implements IParkingWechatService
{
    @Override
    public Map<String, Object> generateWechatSignature(String appId, String appSecret, String url, Long timestamp)
    {
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(appSecret))
        {
            throw new ServiceException("公众号配置不存在或不完整");
        }
        if (StringUtils.isEmpty(url) || timestamp == null)
        {
            throw new ServiceException("url 和 timestamp 不能为空");
        }
        return generateSignature(appId, appSecret, url, timestamp);
    }

    @Cacheable(value = "wechatAccessToken", key = "#appId", unless = "#result == null")
    public String getWechatAccessToken(String appId, String appSecret)
    {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + appId + "&secret=" + appSecret;
        JSONObject json = JSONObject.parseObject(cn.hutool.http.HttpUtil.get(url));
        if (json != null && json.containsKey("access_token"))
        {
            return json.getString("access_token");
        }
        throw new ServiceException("获取access_token失败");
    }

    @Cacheable(value = "wechatJsapiTicket", key = "#accessToken", unless = "#result == null")
    public String getWechatJsapiTicket(String accessToken)
    {
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken + "&type=jsapi";
        JSONObject json = JSONObject.parseObject(cn.hutool.http.HttpUtil.get(url));
        if (json != null && json.getIntValue("errcode") == 0)
        {
            return json.getString("ticket");
        }
        throw new ServiceException("获取jsapi_ticket失败");
    }

    private Map<String, Object> generateSignature(String appId, String appSecret, String url, Long timestamp)
    {
        url = url.split("#")[0];
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long timestampInSeconds = timestamp / 1000;
        String accessToken = getWechatAccessToken(appId, appSecret);
        String jsapiTicket = getWechatJsapiTicket(accessToken);
        String rawString = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%d&url=%s",
                jsapiTicket, nonceStr, timestampInSeconds, url);
        String signature = DigestUtils.sha1Hex(rawString);

        Map<String, Object> result = new HashMap<>();
        result.put("appId", appId);
        result.put("nonceStr", nonceStr);
        result.put("timestamp", timestampInSeconds);
        result.put("signature", signature);
        return result;
    }
}
