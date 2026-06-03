package com.huimao.parking.util;

import com.huimao.parking.domain.ParkingChargeRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 首版停车计费器。
 *
 * <p>当前计费模型比较简单：</p>
 * <p>1. 免费时长内不收费。</p>
 * <p>2. 超过免费时长后，首个计费单位按首小时价计费。</p>
 * <p>3. 后续计费单位按后续单价计费。</p>
 * <p>4. 最后应用 24 小时封顶金额。</p>
 *
 * <p>暂未支持时段计费、节假日计费、阶梯计费、会员减免等复杂规则。</p>
 */
public final class ParkingFeeCalculator
{
    private ParkingFeeCalculator()
    {
    }

    public static int calcParkingMinutes(Date entryTime, Date calcTime)
    {
        // 非法时间直接按 0 分钟处理，避免出现负数时长。
        if (entryTime == null || calcTime == null || calcTime.before(entryTime))
        {
            return 0;
        }
        long diffMillis = calcTime.getTime() - entryTime.getTime();
        long minutes = diffMillis / (60 * 1000);
        // 不是整分钟时按一整分钟计费，和多数停车计费规则一致。
        if (diffMillis % (60 * 1000) != 0)
        {
            minutes++;
        }
        return (int) minutes;
    }

    public static BigDecimal calcAmount(ParkingChargeRule rule, Date entryTime, Date calcTime)
    {
        // 没有规则时返回 0，交由业务层决定是否拦截。
        if (rule == null)
        {
            return BigDecimal.ZERO;
        }
        int parkingMinutes = calcParkingMinutes(entryTime, calcTime);
        int freeMinutes = rule.getFreeMinutes() == null ? 0 : rule.getFreeMinutes();
        if (parkingMinutes <= freeMinutes)
        {
            return BigDecimal.ZERO;
        }

        // unitMinutes 为 0 时兜底按 60 分钟一档计算，避免除零。
        int unitMinutes = rule.getUnitMinutes() == null || rule.getUnitMinutes() <= 0 ? 60 : rule.getUnitMinutes();
        BigDecimal unitPrice = rule.getUnitPrice() == null ? BigDecimal.ZERO : rule.getUnitPrice();
        BigDecimal followUnitPrice = rule.getFollowUnitPrice() == null ? unitPrice : rule.getFollowUnitPrice();
        int units = BigDecimal.valueOf(parkingMinutes)
                .divide(BigDecimal.valueOf(unitMinutes), 0, RoundingMode.CEILING)
                .intValue();
        BigDecimal amount = unitPrice;
        if (units > 1)
        {
            amount = amount.add(BigDecimal.valueOf(units - 1L).multiply(followUnitPrice));
        }
        BigDecimal capAmount = rule.getDayCapAmount();
        // 配置了封顶金额时，最终金额不能超过封顶值。
        if (capAmount != null && capAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(capAmount) > 0)
        {
            amount = capAmount;
        }
        return amount.max(BigDecimal.ZERO);
    }

    public static BigDecimal calcOvertimeAmount(ParkingChargeRule rule, Date overtimeStartTime, Date calcTime)
    {
        if (rule == null || overtimeStartTime == null || calcTime == null || !calcTime.after(overtimeStartTime))
        {
            return BigDecimal.ZERO;
        }
        int overtimeMinutes = calcParkingMinutes(overtimeStartTime, calcTime);
        int unitMinutes = rule.getUnitMinutes() == null || rule.getUnitMinutes() <= 0 ? 60 : rule.getUnitMinutes();
        BigDecimal unitPrice = rule.getUnitPrice() == null ? BigDecimal.ZERO : rule.getUnitPrice();
        BigDecimal followUnitPrice = rule.getFollowUnitPrice() == null ? unitPrice : rule.getFollowUnitPrice();
        BigDecimal units = BigDecimal.valueOf(overtimeMinutes)
                .divide(BigDecimal.valueOf(unitMinutes), 0, RoundingMode.CEILING);
        return units.multiply(followUnitPrice).max(BigDecimal.ZERO);
    }
}
