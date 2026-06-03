package com.huimao.parking.controller.open;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.parking.domain.ParkingSession;
import com.huimao.parking.dto.ParkingH5PayRequest;
import com.huimao.parking.dto.WechatSignatureRequest;
import com.huimao.parking.service.IParkingWechatService;
import com.huimao.parking.service.IParkingTradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对车主 H5 页面开放的查询与缴费接口。
 */
@Api(tags = "停车H5开放接口")
@RestController
@RequestMapping("/open/h5")
public class ParkingOpenH5Controller extends BaseController
{
    @Autowired
    private IParkingTradeService parkingTradeService;

    @Autowired
    private IParkingWechatService parkingWechatService;

    /**
     * 按停车场编码查询停车场公开信息。
     */
    @ApiOperation("按停车场编码查询停车场公开信息")
    @GetMapping("/lot/code/{lotCode}")
    public AjaxResult queryLotByCode(@ApiParam(value = "停车场编码", required = true) @PathVariable String lotCode)
    {
        return AjaxResult.success(parkingTradeService.queryLotByCode(lotCode));
    }

    /**
     * 按停车场编码生成停车场二维码图片。
     */
    @ApiOperation("按停车场编码生成停车场二维码图片")
    @GetMapping("/lot/{lotCode}/qrcode")
    public void qrcode(@ApiParam(value = "停车场编码", required = true) @PathVariable String lotCode,
                       HttpServletResponse response) throws IOException
    {
        String content = parkingTradeService.queryLotByCode(lotCode).getPayQrContent();
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        QrCodeUtil.generate(content, 300, 300, "png", response.getOutputStream());
    }

    /**
     * 按停车场和车牌查询当前待支付订单。
     */
    @ApiOperation("按停车场和车牌查询当前待支付订单")
    @GetMapping("/lot/{lotId}/order")
    public AjaxResult query(@ApiParam(value = "停车场ID", required = true) @PathVariable Long lotId,
                            @ApiParam(value = "车牌号", required = true) @RequestParam String plateNo)
    {
        return AjaxResult.success(parkingTradeService.queryPendingOrder(lotId, plateNo));
    }

    /**
     * 按车牌查询当前待支付订单。
     */
    @ApiOperation("按车牌查询当前待支付订单")
    @GetMapping("/order")
    public AjaxResult queryByPlate(@ApiParam(value = "车牌号", required = true) @RequestParam String plateNo)
    {
        return AjaxResult.success(parkingTradeService.queryPendingOrder(plateNo));
    }

    /**
     * 按车牌查询 H5 缴费记录。
     */
    @ApiOperation("按车牌查询缴费记录")
    @GetMapping("/payment-records")
    public AjaxResult paymentRecords(@ApiParam(value = "车牌号", required = true) @RequestParam String plateNo)
    {
        return AjaxResult.success(parkingTradeService.queryPaymentRecords(plateNo));
    }

    /**
     * 按车牌分页查询 H5 停车记录。
     */
    @ApiOperation("按车牌分页查询停车记录")
    @GetMapping("/sessions")
    public AjaxResult sessions(ParkingSession query)
    {
        return AjaxResult.success(parkingTradeService.queryH5SessionPage(query));
    }

    /**
     * 发起模拟支付。
     */
    @ApiOperation("发起模拟支付")
    @PostMapping("/order/{orderNo}/pay")
    public AjaxResult pay(@ApiParam(value = "订单号", required = true) @PathVariable String orderNo,
                          @RequestBody(required = false) ParkingH5PayRequest request)
    {
        return parkingTradeService.payOrder(orderNo, request == null ? new ParkingH5PayRequest() : request);
    }

    /**
     * 生成微信公众号 JS-SDK 签名。
     */
    @ApiOperation("生成微信公众号JS-SDK签名")
    @GetMapping("/wechat/signature")
    public AjaxResult generateWechatSignature(WechatSignatureRequest request)
    {
        return AjaxResult.success(parkingWechatService.generateWechatSignature(
                request.getAppId(), request.getAppSecret(), request.getUrl(), request.getTimestamp()));
    }
}
