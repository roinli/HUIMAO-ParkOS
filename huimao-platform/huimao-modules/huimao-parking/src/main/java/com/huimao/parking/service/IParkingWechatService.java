package com.huimao.parking.service;

import java.util.Map;

/**
 * 停车模块微信能力服务。
 */
public interface IParkingWechatService
{
    /**
     * 生成微信公众号 JS-SDK 签名。
     *
     * @param appId 公众号 appId
     * @param appSecret 公众号 appSecret
     * @param url 当前页面 URL
     * @param timestamp 时间戳
     * @return 签名信息
     */
    Map<String, Object> generateWechatSignature(String appId, String appSecret, String url, Long timestamp);
}
