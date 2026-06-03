package com.huimao.parking.util;

import com.huimao.common.core.utils.DateUtils;
import com.huimao.common.security.utils.SecurityUtils;

/**
 * 停车业务单号生成工具。
 *
 * <p>首版采用“前缀 + 时间戳 + 随机数”的轻量方案，方便联调和日志排查。
 * 如果后续要支持高并发或分布式有序单号，可以替换为雪花算法或号段模式。</p>
 */
public final class ParkingNoUtils
{
    private ParkingNoUtils()
    {
    }

    public static String orderNo()
    {
        // PO = Parking Order，停车订单号
        return "PO" + DateUtils.dateTimeNow() + SecurityUtils.randomPassword(6);
    }

    public static String paymentNo()
    {
        // PP = Parking Payment，支付流水号
        return "PP" + DateUtils.dateTimeNow() + SecurityUtils.randomPassword(6);
    }

    public static String eventNo()
    {
        // PE = Parking Event，设备事件流水号
        return "PE" + DateUtils.dateTimeNow() + SecurityUtils.randomPassword(6);
    }

    public static String commandNo()
    {
        // PC = Parking Command，设备下行指令号
        return "PC" + DateUtils.dateTimeNow() + SecurityUtils.randomPassword(6);
    }

    public static String withdrawNo()
    {
        // WD = Withdraw，提现申请单号
        return "WD" + DateUtils.dateTimeNow() + SecurityUtils.randomPassword(6);
    }

    public static String lotCode()
    {
        // 停车场编码对外用于二维码/短链场景，保持纯数字更方便录入和识别。
        return DateUtils.dateTimeNow() + SecurityUtils.randomPassword(4);
    }
}
