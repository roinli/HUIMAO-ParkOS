package com.huimao.parking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信 JS-SDK 签名请求。
 */
@Data
@ApiModel("微信JS-SDK签名请求")
public class WechatSignatureRequest
{
    @ApiModelProperty(value = "公众号appId", required = true)
    private String appId;

    @ApiModelProperty(value = "公众号appSecret", required = true)
    private String appSecret;

    @ApiModelProperty(value = "当前页面URL", required = true)
    private String url;

    @ApiModelProperty(value = "时间戳", required = true)
    private Long timestamp;
}
